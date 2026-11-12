package io.github.nikalbazu24.gahvoria.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class CoreTest {

    private fun folder(vararg files: String): File {
        val root = Files.createTempDirectory("game").toFile()
        for (f in files) File(root, f).apply { parentFile.mkdirs(); writeText("x") }
        return root
    }

    @Test fun detectsRpgMakerVersionsAndEssentials() {
        val xp = folder("Game.ini", "Data/Scripts.rxdata", "Data/messages.dat")
        File(xp, "Game.ini").writeText("[Game]\r\nLibrary=RGSS104E.dll\r\nTitle=Ópalo Edición\r\n")
        val i = GameInspector.inspect(xp)
        assertEquals(EngineKind.RPG_MAKER_XP, i.kind)
        assertTrue(i.essentials)
        assertEquals("Ópalo Edición", i.title)
        assertEquals(Support.PLAYABLE, i.support)
        assertEquals("RPG Maker XP + Essentials", i.engineLabel)

        assertEquals(EngineKind.RPG_MAKER_VXACE, GameInspector.inspect(folder("Game.rgss3a")).kind)
        assertEquals(EngineKind.RPG_MAKER_VX, GameInspector.inspect(folder("Data/Map001.rvdata")).kind)
        assertEquals(EngineKind.RPG_MAKER_XP, GameInspector.inspect(folder("Game.rgssad")).kind)
        assertFalse(GameInspector.inspect(folder("Game.rgssad")).essentials)
    }

    @Test fun detectsGodotUnityAndFoldersThatAreNotGames() {
        val godot = folder("project.godot")
        File(godot, "project.godot").writeText("[application]\nconfig/name=\"Aster\"\n")
        GameInspector.inspect(godot).let {
            assertEquals(EngineKind.GODOT, it.kind); assertEquals("Aster", it.title); assertEquals(Support.NEXT_PHASE, it.support)
        }
        val unity = folder("Nube_Data/globalgamemanagers", "Nube.exe")
        assertEquals(Support.RESEARCH, GameInspector.inspect(unity).support)
        val photos = folder("foto.jpg")
        assertEquals(Support.NOT_A_GAME, GameInspector.inspect(photos).support)
        assertEquals(photos.name, GameInspector.inspect(photos).title)
    }

    @Test fun libraryRoundTripsAndOnlyForgetsTheEntry() {
        val dir = folder("Game.rgssad")
        val lib = Library(File(Files.createTempDirectory("lib").toFile(), "library.tsv"))
        val g = Game(dir.path, "Con\ttab y \\barra\nlínea", EngineKind.RPG_MAKER_XP, true, addedAt = 5)
        lib.add(g)
        assertEquals(listOf(g), lib.load())
        lib.add(g.copy(title = "Nuevo"))
        assertEquals(1, lib.load().size)
        lib.update(g.path) { it.copy(lastPlayed = 9, engine = Game.ENGINE_CLASSIC) }
        assertEquals(Game.ENGINE_CLASSIC, lib.load().single().engine)
        lib.remove(g.path)
        assertTrue(lib.load().isEmpty())
        assertTrue("The game folder is not touched", File(dir, "Game.rgssad").isFile)
        assertNull(Library.parse("otra-version\tx"))
    }

    @Test fun orderPutsTheLastPlayedFirst() {
        val a = Game("/a", "A", EngineKind.RPG_MAKER_XP, false, addedAt = 1, lastPlayed = 0)
        val b = Game("/b", "B", EngineKind.RPG_MAKER_XP, false, addedAt = 2, lastPlayed = 50)
        val c = Game("/c", "C", EngineKind.RPG_MAKER_XP, false, addedAt = 3, lastPlayed = 0)
        assertEquals(listOf("/b", "/c", "/a"), Library.ordered(listOf(a, b, c)).map { it.path })
    }

    @Test fun savesStayWhereEssentiaAndJoiPlayLookForThem() {
        val game = File("/storage/emulated/0/Juegos/Ópalo")
        val root = File("/storage/emulated/0")
        assertEquals(game, Saves.dirFor(game, "Ópalo", root, true))
        assertEquals(File("/storage/emulated/0/Essentia/Partidas/Ópalo: 2".replace(":", "")),
            Saves.dirFor(game, "Ópalo: 2", root, false))
        assertTrue(Saves.looksLikeSave("Save1.rxdata"))
        assertTrue(Saves.looksLikeSave("Game.rvdata2"))
        assertFalse(Saves.looksLikeSave("Scripts.rxdata"))
        assertFalse(Saves.looksLikeSave("Map001.rxdata"))
    }

    @Test fun everyPaletteIsReadableInBothModes() {
        for (p in Palettes.all) for (s in listOf(p.dark, p.light)) {
            val id = p.id + (if (s === p.dark) "/oscuro" else "/claro")
            assertTrue("$id texto", Palettes.contrast(s.ink, s.bg) >= 7.0)
            assertTrue("$id texto suave", Palettes.contrast(s.muted, s.bg) >= 4.5)
            assertTrue("$id botón", Palettes.contrast(s.onAccent, s.accent) >= 4.5)
        }
        assertEquals("grafito", Palettes.byId(null).id)
        assertEquals(Palettes.byId("grafito").dark, Palettes.scheme(null, ThemeMode.SYSTEM, systemDark = true))
        assertEquals(Palettes.byId("vino").light, Palettes.scheme("vino", ThemeMode.LIGHT, systemDark = true))
    }

    @Test fun releaseManifestIsReadSafely() {
        val r = Release.parse("""{"versionCode": 12, "versionName": "0.1.12", "signed": true, "apk": "gahvoria.apk"}""")
        assertEquals(Release(12, "0.1.12", "gahvoria.apk"), r)
        assertTrue(r!!.isNewerThan(11))
        assertFalse(r.isNewerThan(12))
        assertNull(Release.parse("""{"versionCode": 3, "versionName": "x", "apk": "../../otro.sh"}"""))
        assertNull(Release.parse("no es json"))
    }

    // ---- Essentials version and automatic engine ---------------------------------------------

    private fun marshalNum(n: Int): ByteArray = when {
        n == 0 -> byteArrayOf(0)
        n in 1..122 -> byteArrayOf((n + 5).toByte())
        n in 123..255 -> byteArrayOf(1, n.toByte())
        else -> byteArrayOf(2, (n and 0xFF).toByte(), (n shr 8).toByte())
    }

    private fun zlib(text: String): ByteArray {
        val d = java.util.zip.Deflater()
        d.setInput(text.toByteArray())
        d.finish()
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(4096)
        while (!d.finished()) out.write(buf, 0, d.deflate(buf))
        return out.toByteArray()
    }

    /** A Scripts.rxdata like RPG Maker writes: [[id, name, zlib(code)], ...] in Ruby Marshal. */
    private fun scriptsFile(vararg sections: Pair<String, String>): File {
        val out = java.io.ByteArrayOutputStream()
        out.write(byteArrayOf(4, 8, '['.code.toByte()))
        out.write(marshalNum(sections.size))
        sections.forEachIndexed { i, (name, code) ->
            out.write('['.code); out.write(marshalNum(3))
            out.write('i'.code); out.write(marshalNum(i + 1))
            out.write('"'.code); out.write(marshalNum(name.length)); out.write(name.toByteArray())
            val z = zlib(code)
            out.write('"'.code); out.write(marshalNum(z.size)); out.write(z)
        }
        val root = Files.createTempDirectory("game").toFile()
        File(root, "Data").mkdirs()
        File(root, "Data/Scripts.rxdata").writeBytes(out.toByteArray())
        return root
    }

    @Test fun essentials17And18GetTheClassicEngineAutomatically() {
        val v17 = GameProfile.read(scriptsFile("Main" to "ESSENTIALS_VERSION = \"17.2\"\nGAME_VERSION = \"1.0.3\"\n"))
        assertTrue(v17.essentials)
        assertEquals("17.2", v17.essentialsVersion)
        assertEquals("1.0.3", v17.gameVersion)
        assertEquals(Game.ENGINE_CLASSIC, v17.recommendedEngine)
        assertEquals("Essentials 17.2", v17.label)

        val v18 = GameProfile.read(scriptsFile("Settings" to "module Essentials\n  VERSION = \"18.1\"\nend\n"))
        assertEquals(Game.ENGINE_CLASSIC, v18.recommendedEngine)
    }

    @Test fun otherGamesKeepTheModernEngine() {
        assertEquals(Game.ENGINE_MODERN, GameProfile.read(scriptsFile("Settings" to "module Essentials\n  VERSION = \"21.1\"\nend\n")).recommendedEngine)
        assertEquals(Game.ENGINE_MODERN, GameProfile.read(scriptsFile("Settings" to "ESSENTIALS_VERSION = \"16.2\"")).recommendedEngine)
        // the old window resizer marks Essentials up to v16-17, which stays on the modern engine
        val resizer = GameProfile.read(scriptsFile("Sprite_Resizer" to "x", "Main" to "ESSENTIALS_VERSION = \"17.2\""))
        assertTrue(resizer.oldResizer)
        assertEquals(Game.ENGINE_MODERN, resizer.recommendedEngine)
        val plain = GameProfile.read(scriptsFile("Main" to "puts 1"))
        assertFalse(plain.essentials)
        assertEquals("", plain.label)
        assertEquals(Game.ENGINE_MODERN, plain.recommendedEngine)
    }

    @Test fun brokenOrMissingScriptsNeverCrash() {
        assertEquals(GameProfile(), GameProfile.read(folder("Game.ini")))
        val root = folder("Data/Scripts.rxdata")  // a file that is not Marshal data
        assertEquals(GameProfile(), GameProfile.read(root))
        File(root, "Data/Scripts.rxdata").writeBytes(byteArrayOf(4, 8, '['.code.toByte(), 100, 1))
        assertEquals(GameProfile(), GameProfile.read(root))
    }
}
