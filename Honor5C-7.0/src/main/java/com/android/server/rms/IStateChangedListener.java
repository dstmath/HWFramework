package com.android.server.rms;

public interface IStateChangedListener {
    void onInterrupt();

    void onTrigger();
}
