package com.android.server.connectivity;

import java.util.concurrent.atomic.AtomicReference;

public class AutodestructReference<T> {
    private final AtomicReference<T> mHeld;

    public AutodestructReference(T obj) {
        if (obj != null) {
            this.mHeld = new AtomicReference<>(obj);
            return;
        }
        throw new NullPointerException("Autodestruct reference to null");
    }

    public T getAndDestroy() {
        T obj = this.mHeld.getAndSet(null);
        if (obj != null) {
            return obj;
        }
        throw new NullPointerException("Already autodestructed");
    }
}
