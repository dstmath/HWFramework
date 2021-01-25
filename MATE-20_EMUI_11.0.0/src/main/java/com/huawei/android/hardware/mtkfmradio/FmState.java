package com.huawei.android.hardware.mtkfmradio;

import java.util.concurrent.atomic.AtomicInteger;

/* access modifiers changed from: package-private */
public class FmState {
    private static final String TAG = "FmState";
    private final AtomicInteger mFmSearchState = new AtomicInteger(-1);
    private final AtomicInteger mFmState = new AtomicInteger(2);

    public void setFmPowerState(int state) {
        this.mFmState.set(state);
    }

    public void setSearchState(int state) {
        this.mFmSearchState.set(state);
        int i = this.mFmSearchState.get();
        if (i == 0 || i == 1 || i == 2) {
            setFmPowerState(1);
        } else if (i == 3) {
            this.mFmSearchState.set(-1);
            setFmPowerState(0);
        } else if (i != 4) {
            this.mFmSearchState.set(-1);
        }
    }

    public int getSyncFmPowerState() {
        return this.mFmState.get();
    }

    public int getSyncFmSearchState() {
        return this.mFmSearchState.get();
    }
}
