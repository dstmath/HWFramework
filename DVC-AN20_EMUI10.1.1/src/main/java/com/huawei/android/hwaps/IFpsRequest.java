package com.huawei.android.hwaps;

public interface IFpsRequest {
    int getCurFPS();

    void start(int i);

    void stop();
}
