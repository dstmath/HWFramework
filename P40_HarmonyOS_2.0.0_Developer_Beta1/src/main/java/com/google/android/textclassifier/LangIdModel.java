package com.google.android.textclassifier;

import java.util.concurrent.atomic.AtomicBoolean;

public final class LangIdModel implements AutoCloseable {
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private long modelPtr;

    private native void nativeClose(long j);

    private native LanguageResult[] nativeDetectLanguages(long j, String str);

    private native float nativeGetLangIdThreshold(long j);

    private native int nativeGetVersion(long j);

    private static native int nativeGetVersionFromFd(int i);

    private static native long nativeNew(int i);

    private static native long nativeNewFromPath(String str);

    static {
        System.loadLibrary("textclassifier");
    }

    public LangIdModel(int fd) {
        this.modelPtr = nativeNew(fd);
        if (this.modelPtr == 0) {
            throw new IllegalArgumentException("Couldn't initialize LangId from given file descriptor.");
        }
    }

    public LangIdModel(String modelPath) {
        this.modelPtr = nativeNewFromPath(modelPath);
        if (this.modelPtr == 0) {
            throw new IllegalArgumentException("Couldn't initialize LangId from given file.");
        }
    }

    public LanguageResult[] detectLanguages(String text) {
        return nativeDetectLanguages(this.modelPtr, text);
    }

    @Override // java.lang.AutoCloseable
    public void close() {
        if (this.isClosed.compareAndSet(false, true)) {
            nativeClose(this.modelPtr);
            this.modelPtr = 0;
        }
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public static final class LanguageResult {
        final String mLanguage;
        final float mScore;

        LanguageResult(String language, float score) {
            this.mLanguage = language;
            this.mScore = score;
        }

        public final String getLanguage() {
            return this.mLanguage;
        }

        public final float getScore() {
            return this.mScore;
        }
    }

    public int getVersion() {
        return nativeGetVersion(this.modelPtr);
    }

    public float getLangIdThreshold() {
        return nativeGetLangIdThreshold(this.modelPtr);
    }

    public static int getVersion(int fd) {
        return nativeGetVersionFromFd(fd);
    }
}
