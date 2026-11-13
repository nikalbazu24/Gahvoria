package com.hatkid.mkxpz.translate;

final class BergamotNative {
    private BergamotNative() {}

    static native long nativeNewService(int cacheSize);
    static native void nativeFreeService(long handle);
    static native long nativeLoadModel(String model, String lex, String[] vocabs, boolean int8shiftAll);
    static native void nativeFreeModel(long handle);
    static native byte[][] nativeTranslate(long service, long model, byte[][] inputs);
    static native String nativeUnzstd(String source, String target);
}
