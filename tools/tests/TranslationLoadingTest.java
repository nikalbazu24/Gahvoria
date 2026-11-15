package com.hatkid.mkxpz.translate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Large-catalog startup and cancellation, with no game data or neural inference. */
public final class TranslationLoadingTest {
    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("gahvoria-loading");
        Path catalog = dir.resolve("catalog.ready"), cache = dir.resolve("es.tsv");
        int count = 85484;
        try {
            try (BufferedWriter c = Files.newBufferedWriter(catalog, StandardCharsets.UTF_8);
                 BufferedWriter m = Files.newBufferedWriter(cache, StandardCharsets.UTF_8)) {
                for (int i = 0; i < count; i++) {
                    String source = "ELENA: Welcome to area " + i + ". Please open the map and speak with your friend before leaving.";
                    c.write(source + "\n");
                    m.write(source + "\tELENA: Bienvenido a la zona " + i + ". Abre el mapa y habla con tu amigo antes de salir.\n");
                }
            }
            long catalogSize = Files.size(catalog), memorySize = Files.size(cache);
            Set<TranslationWork.Phase> reached = EnumSet.noneOf(TranslationWork.Phase.class);
            long start = System.nanoTime();
            TranslationCoverage report = TranslationCoverage.read(dir.resolve("catalog").toFile(),
                new TranslationWork(() -> false, (phase, n) -> reached.add(phase)));
            if (report.total != count || report.translated != count || report.pending != 0) throw new AssertionError("Incomplete coverage");
            if (reached.size() != 3) throw new AssertionError("Missing loading stage");
            System.out.println("Large catalog: " + count + " rows in " + ((System.nanoTime()-start)/1e9) + " s (host JVM, not Android)");

            for (TranslationWork.Phase target : TranslationWork.Phase.values()) {
                AtomicBoolean cancelled = new AtomicBoolean();
                TranslationWork work = new TranslationWork(cancelled::get, (phase, n) -> {
                    if (phase == target) cancelled.set(true);
                });
                try {
                    TranslationCoverage.read(dir.resolve("catalog").toFile(), work);
                    throw new AssertionError("Ignored cancellation during " + target);
                } catch (InterruptedIOException expected) { }
            }
            try {
                TranslationCoverage.read(dir.resolve("catalog").toFile(), new TranslationWork(() -> true, (p,n) -> {}));
                throw new AssertionError("Started an already cancelled request");
            } catch (InterruptedIOException expected) { }

            AtomicBoolean cancelled = new AtomicBoolean();
            try (TranslationMemory unused = new TranslationMemory(cache.toFile(), true,
                    new TranslationWork(cancelled::get, (p,n) -> { if (p == TranslationWork.Phase.MEMORY) cancelled.set(true); }))) {
                throw new AssertionError("Writable load ignored cancellation");
            } catch (InterruptedIOException expected) { }
            // A cancelled constructor must release its file and exclusive writer lock.
            try (TranslationMemory reopened = new TranslationMemory(cache.toFile())) {
                if (reopened.get("ELENA: Welcome to area 0. Please open the map and speak with your friend before leaving.") == null)
                    throw new AssertionError("Cache not reusable after cancellation");
            }
            if (Files.size(catalog) != catalogSize || Files.size(cache) != memorySize) throw new AssertionError("Cancellation changed stored data");
            System.out.println("Catalog, memory and coverage cancellation passed; writer lock released");
        } finally {
            for (Path path : Arrays.asList(cache, catalog, dir)) Files.deleteIfExists(path);
        }
    }
}
