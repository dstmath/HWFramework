package com.huawei.server.fsm;

import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public interface PosturePreprocessPolicy {
    void dump(String str, PrintWriter printWriter);

    void turnOff();

    void turnOn(int i);
}
