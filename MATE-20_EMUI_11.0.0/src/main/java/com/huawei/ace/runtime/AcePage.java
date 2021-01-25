package com.huawei.ace.runtime;

public final class AcePage {
    private AceEventCallback callback;
    private final int pageId;

    public AcePage(int i, AceEventCallback aceEventCallback) {
        this.pageId = i;
        this.callback = aceEventCallback;
    }

    public int getId() {
        return this.pageId;
    }

    public AceEventCallback getCallback() {
        return this.callback;
    }
}
