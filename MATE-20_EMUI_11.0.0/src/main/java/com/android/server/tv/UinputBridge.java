package com.android.server.tv;

import android.os.IBinder;
import dalvik.system.CloseGuard;
import java.io.IOException;

public final class UinputBridge {
    private final CloseGuard mCloseGuard = CloseGuard.get();
    private long mPtr;
    private IBinder mToken = null;

    private static native void nativeClear(long j);

    private static native void nativeClose(long j);

    private static native long nativeOpen(String str, String str2, int i, int i2, int i3);

    private static native void nativeSendKey(long j, int i, boolean z);

    private static native void nativeSendPointerDown(long j, int i, int i2, int i3);

    private static native void nativeSendPointerSync(long j);

    private static native void nativeSendPointerUp(long j, int i);

    private static native void nativeSendTimestamp(long j, long j2);

    public UinputBridge(IBinder token, String name, int width, int height, int maxPointers) throws IOException {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Touchpad must be at least 1x1.");
        } else if (maxPointers < 1 || maxPointers > 32) {
            throw new IllegalArgumentException("Touchpad must support between 1 and 32 pointers.");
        } else if (token != null) {
            this.mPtr = nativeOpen(name, token.toString(), width, height, maxPointers);
            if (this.mPtr != 0) {
                this.mToken = token;
                this.mCloseGuard.open("close");
                return;
            }
            throw new IOException("Could not open uinput device " + name);
        } else {
            throw new IllegalArgumentException("Token cannot be null");
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            close(this.mToken);
        } finally {
            this.mToken = null;
            super.finalize();
        }
    }

    public void close(IBinder token) {
        if (isTokenValid(token) && this.mPtr != 0) {
            clear(token);
            nativeClose(this.mPtr);
            this.mPtr = 0;
            this.mCloseGuard.close();
        }
    }

    public IBinder getToken() {
        return this.mToken;
    }

    /* access modifiers changed from: protected */
    public boolean isTokenValid(IBinder token) {
        return this.mToken.equals(token);
    }

    public void sendTimestamp(IBinder token, long timestamp) {
        if (isTokenValid(token)) {
            nativeSendTimestamp(this.mPtr, timestamp);
        }
    }

    public void sendKeyDown(IBinder token, int keyCode) {
        if (isTokenValid(token)) {
            nativeSendKey(this.mPtr, keyCode, true);
        }
    }

    public void sendKeyUp(IBinder token, int keyCode) {
        if (isTokenValid(token)) {
            nativeSendKey(this.mPtr, keyCode, false);
        }
    }

    public void sendPointerDown(IBinder token, int pointerId, int x, int y) {
        if (isTokenValid(token)) {
            nativeSendPointerDown(this.mPtr, pointerId, x, y);
        }
    }

    public void sendPointerUp(IBinder token, int pointerId) {
        if (isTokenValid(token)) {
            nativeSendPointerUp(this.mPtr, pointerId);
        }
    }

    public void sendPointerSync(IBinder token) {
        if (isTokenValid(token)) {
            nativeSendPointerSync(this.mPtr);
        }
    }

    public void clear(IBinder token) {
        if (isTokenValid(token)) {
            nativeClear(this.mPtr);
        }
    }
}
