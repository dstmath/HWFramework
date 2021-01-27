package com.android.server.rms.utils;

import java.util.concurrent.atomic.AtomicBoolean;

public class Interrupt {
    private final AtomicBoolean mInterrupt = new AtomicBoolean(false);

    public boolean checkInterruptAndReset() {
        if (!this.mInterrupt.get()) {
            return false;
        }
        reset();
        return true;
    }

    public void trigger() {
        this.mInterrupt.set(true);
    }

    public void reset() {
        this.mInterrupt.set(false);
    }
}
