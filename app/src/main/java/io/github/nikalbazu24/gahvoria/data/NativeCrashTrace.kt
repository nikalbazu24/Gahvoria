package io.github.nikalbazu24.gahvoria.data

import java.io.EOFException
import java.io.InputStream

/** Reads only the crashing thread and library frames from Android's tombstone protobuf. */
internal object NativeCrashTrace {
    private const val MAX_BYTES = 2 * 1024 * 1024

    fun read(input: InputStream): String? {
        val bytes = ByteArray(MAX_BYTES)
        var size = 0
        while (size < bytes.size) {
            val count = input.read(bytes, size, bytes.size - size)
            if (count <= 0) break
            size += count
        }
        var tid = -1L
        val threads = mutableMapOf<Long, Proto>()
        Proto(bytes, 0, size).fields { number, wire ->
            when {
                number == 6 && wire == 0 -> tid = varint()
                number == 16 && wire == 2 -> {
                    var id = -1L
                    var thread: Proto? = null
                    message().fields { field, type ->
                        when {
                            field == 1 && type == 0 -> id = varint()
                            field == 2 && type == 2 -> thread = message()
                            else -> skip(type)
                        }
                    }
                    thread?.let { if (id >= 0) threads[id] = it }
                }
                else -> skip(wire)
            }
        }
        val frames = mutableListOf<String>()
        threads[tid]?.fields { number, wire ->
            if (number == 4 && wire == 2 && frames.size < 12) {
                var pc = 0L
                var library = ""
                var function = ""
                var buildId = ""
                message().fields { field, type ->
                    when {
                        field == 1 && type == 0 -> pc = varint()
                        field == 4 && type == 2 -> function = text(180)
                        field == 6 && type == 2 -> library = text(1024).substringAfterLast('/')
                        field == 8 && type == 2 -> buildId = text(64)
                        else -> skip(type)
                    }
                }
                if (library.isNotEmpty()) {
                    val symbol = if (function.isEmpty()) "" else " ($function)"
                    val build = if (buildId.isEmpty()) "" else " [$buildId]"
                    frames.add(library + " +0x" + pc.toString(16) + symbol + build)
                }
            } else skip(wire)
        }
        return frames.takeIf { it.isNotEmpty() }?.joinToString("\n", prefix = "Traza nativa de Android\n")
    }

    private class Proto(private val bytes: ByteArray, private var position: Int, private val end: Int) {
        fun fields(read: Proto.(Int, Int) -> Unit) {
            while (position < end) {
                val tag = varint()
                val number = (tag ushr 3).toInt()
                require(number > 0)
                read(number, (tag and 7).toInt())
            }
        }

        fun varint(): Long {
            var value = 0L
            for (shift in 0..63 step 7) {
                if (position == end) throw EOFException()
                val byte = bytes[position++].toInt() and 255
                value = value or ((byte and 127).toLong() shl shift)
                if (byte < 128) return value
            }
            throw IllegalArgumentException("Invalid protobuf varint")
        }

        fun message(): Proto {
            val size = varint()
            require(size in 0..(end - position).toLong())
            val start = position
            position += size.toInt()
            return Proto(bytes, start, position)
        }

        fun text(limit: Int): String {
            val field = message()
            return bytes.decodeToString(field.position, minOf(field.end, field.position + limit))
                .filter { !it.isISOControl() }
        }

        fun skip(wire: Int) {
            when (wire) {
                0 -> varint()
                2 -> message()
                1, 5 -> {
                    val size = if (wire == 1) 8 else 4
                    if (end - position < size) throw EOFException()
                    position += size
                }
                else -> throw IllegalArgumentException("Unsupported protobuf wire type")
            }
        }
    }
}
