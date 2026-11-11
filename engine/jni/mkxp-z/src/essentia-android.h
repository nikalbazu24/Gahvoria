/* Essentia: small helpers to talk to the Android app (MainActivity) from the engine. */
#ifndef ESSENTIA_ANDROID_H
#define ESSENTIA_ANDROID_H

#ifdef MKXPZ_BUILD_ANDROID
#include <jni.h>
#include <SDL_system.h>

/* Tells the loading screen which stage the game is in ("files", "scripts", "start"). */
static inline void essentiaLoadPhase(const char *phase)
{
    JNIEnv *env = (JNIEnv *)SDL_AndroidGetJNIEnv();
    jobject activity = (jobject)SDL_AndroidGetActivity();
    if (!env || !activity)
        return;
    jclass cls = env->GetObjectClass(activity);
    jmethodID mID = env->GetStaticMethodID(cls, "onLoadPhase", "(Ljava/lang/String;)V");
    if (mID) {
        jstring s = env->NewStringUTF(phase);
        env->CallStaticVoidMethod(cls, mID, s);
        env->DeleteLocalRef(s);
    }
    if (env->ExceptionCheck())
        env->ExceptionClear();
    env->DeleteLocalRef(cls);
    env->DeleteLocalRef(activity);
}
#else
static inline void essentiaLoadPhase(const char *) {}
#endif

#endif
