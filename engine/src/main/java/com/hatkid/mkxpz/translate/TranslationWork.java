package com.hatkid.mkxpz.translate;

import java.io.InterruptedIOException;

/** Cooperative cancellation and throttled progress for large local catalogs. */
public final class TranslationWork {
    public enum Phase { CATALOG, MEMORY, COVERAGE }
    public interface Cancellation { boolean requested(); }
    public interface Progress { void update(Phase phase, int reviewed); }
    private final Cancellation cancellation;
    private final Progress progress;
    private Phase lastPhase;
    private long nextUpdate;

    public TranslationWork() { this(() -> false, (phase, reviewed) -> {}); }
    public TranslationWork(Cancellation cancellation, Progress progress) {
        this.cancellation = cancellation;
        this.progress = progress;
    }
    void checkpoint(Phase phase, int reviewed) throws InterruptedIOException {
        if (cancellation.requested() || Thread.currentThread().isInterrupted()) {
            throw new InterruptedIOException("Revisión cancelada");
        }
        long now = System.nanoTime();
        if (phase != lastPhase || reviewed == 0 || now >= nextUpdate) {
            lastPhase = phase;
            nextUpdate = now + 250_000_000L;
            progress.update(phase, reviewed);
        }
    }
}
