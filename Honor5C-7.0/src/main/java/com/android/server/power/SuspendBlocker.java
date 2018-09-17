package com.android.server.power;

interface SuspendBlocker {
    void acquire();

    void release();
}
