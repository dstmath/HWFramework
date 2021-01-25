package com.android.server.fsm;

import java.io.PrintWriter;

interface PosturePreprocessPolicy {
    void dump(String str, PrintWriter printWriter);

    void turnOff();

    void turnOn(int i);
}
