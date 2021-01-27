package com.huawei.ace.plugin.clipboard;

public abstract class ClipboardPluginBase {
    public abstract void clear();

    public abstract String getData();

    /* access modifiers changed from: protected */
    public native void nativeInit();

    public abstract void setData(String str);
}
