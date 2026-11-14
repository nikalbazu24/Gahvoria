package com.hatkid.mkxpz.translate;

import android.content.Context;
import android.os.SystemClock;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Prepares the same private memory the game reads when it starts. */
public final class TranslationPreparation {
    private static final int BATCH_SIZE = 24;
    public interface Cancellation { boolean requested(); }
    public interface Progress { void update(int reviewed, int total, int translated); }

    private TranslationPreparation() {}

    private static BufferedReader open(File catalog) throws Exception {
        return new BufferedReader(new InputStreamReader(new FileInputStream(catalog), StandardCharsets.UTF_8));
    }

    public static void run(Context context, File catalog, Cancellation cancel, Progress progress) throws Exception {
        int total = 0;
        try (BufferedReader input = open(catalog)) {
            while (!cancel.requested() && input.readLine() != null) total++;
        }
        if (cancel.requested()) return;

        int reviewed = 0;
        int translated = 0;
        long lastUpdate = SystemClock.elapsedRealtime();
        progress.update(reviewed, total, translated);
        try (BufferedReader input = open(catalog);
             Inference model = new Inference(context);
             TranslationMemory memory = new TranslationMemory(new File(catalog.getParentFile(), "es.tsv"), true)) {
            List<String> pending = new ArrayList<>(BATCH_SIZE);
            while (!cancel.requested() && reviewed < total) {
                pending.clear();
                int read = 0;
                while (read < BATCH_SIZE && reviewed + read < total) {
                    String line = input.readLine();
                    if (line == null) throw new java.io.IOException("El catálogo de traducción cambió durante la preparación");
                    String original = TranslationMemory.unescape(line);
                    if (original != null && !original.trim().isEmpty()) {
                        if (memory.get(original) == null) pending.add(original);
                        else translated++;
                    }
                    read++;
                }
                if (!pending.isEmpty()) {
                    memory.remember(TranslationAudit.translate(pending, (o, p) -> model.translate(p)));
                    for (String original : pending) if (memory.get(original) != null) translated++;
                }
                reviewed += read;
                if (reviewed == TranslationOpening.MAX_ROWS && reviewed < total) {
                    TranslationOpening.publish(catalog, memory);
                }
                long now = SystemClock.elapsedRealtime();
                if (now - lastUpdate >= 500 || reviewed == total || cancel.requested()) {
                    progress.update(reviewed, total, translated);
                    lastUpdate = now;
                }
            }
            TranslationOpening.publish(catalog, memory);
        }
    }

    private static final class Inference implements AutoCloseable {
        private final Context context;
        private BergamotModel model;

        Inference(Context context) {
            this.context = context;
        }

        List<String> translate(List<String> texts) throws Exception {
            if (model == null) model = new BergamotModel(context);
            return NeuralText.translate(texts, model::translate, model::translateMarkup);
        }

        @Override public void close() {
            if (model != null) model.close();
        }
    }
}
