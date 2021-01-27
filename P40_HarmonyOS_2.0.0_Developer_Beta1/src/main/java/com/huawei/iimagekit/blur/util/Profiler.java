package com.huawei.iimagekit.blur.util;

public class Profiler {
    private long mHandle = 0;

    private native void destroy(long j);

    private native long init(String str);

    public native void begin();

    public native void end();

    public native void reset();

    public native void setEnableFences(boolean z);

    public Profiler(String name) {
        this.mHandle = init(name);
    }
}
