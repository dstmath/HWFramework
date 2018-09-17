package com.android.server.power;

interface ScreenOnBlocker {
    void acquire();

    void release();
}
