package com.huawei.nb.searchmanager.utils.logger;

public final class Settings {
    private LogAdapter mLogAdapter;
    private int mMethodCount = 2;
    private int mMethodOffset = 0;
    private boolean mShowLineNumber = true;
    private boolean mShowMethodInfo = true;
    private boolean mShowThreadInfo = true;

    public Settings hideThreadInfo() {
        this.mShowThreadInfo = false;
        return this;
    }

    public Settings hideMethodInfo() {
        this.mShowMethodInfo = false;
        return this;
    }

    public Settings hideLineNumber() {
        this.mShowLineNumber = false;
        return this;
    }

    public Settings methodCount(int i) {
        this.mMethodCount = i;
        return this;
    }

    public Settings methodOffset(int i) {
        this.mMethodOffset = i;
        return this;
    }

    public Settings logAdapter(LogAdapter logAdapter) {
        this.mLogAdapter = logAdapter;
        return this;
    }

    public int getMethodCount() {
        return this.mMethodCount;
    }

    public boolean isShowThreadInfo() {
        return this.mShowThreadInfo;
    }

    public boolean isShowMethodInfo() {
        return this.mShowMethodInfo;
    }

    public boolean isShowLineNumber() {
        return this.mShowLineNumber;
    }

    public int getMethodOffset() {
        return this.mMethodOffset;
    }

    public LogAdapter getLogAdapter() {
        return this.mLogAdapter;
    }

    public void reset() {
        this.mMethodCount = 2;
        this.mMethodOffset = 0;
        this.mShowThreadInfo = true;
        this.mShowMethodInfo = true;
        this.mShowLineNumber = true;
    }
}
