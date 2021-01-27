package ohos.event.commonevent;

public final class AsyncCommonEventResult {
    final boolean orderedCommonEvent;
    private final AsyncCommonEventResultProxy proxy;
    final boolean stickyCommonEvent;

    public void setCode(int i) {
        this.proxy.setResultCode(i);
    }

    public int getCode() {
        return this.proxy.getResultCode();
    }

    public void setData(String str) {
        this.proxy.setResultData(str);
    }

    public String getData() {
        return this.proxy.getResultData();
    }

    public void setCodeAndData(int i, String str) {
        this.proxy.setResult(i, str);
    }

    public void abortCommonEvent() {
        this.proxy.abortCommonEvent();
    }

    public boolean getAbortCommonEvent() {
        return this.proxy.getAbortCommonEvent();
    }

    public void clearAbortCommonEvent() {
        this.proxy.clearAbortCommonEvent();
    }

    public void finishCommonEvent() {
        this.proxy.finish();
    }

    AsyncCommonEventResult(Object obj, boolean z, boolean z2) {
        if (obj instanceof AsyncCommonEventResultProxy) {
            this.proxy = (AsyncCommonEventResultProxy) obj;
            this.orderedCommonEvent = z;
            this.stickyCommonEvent = z2;
            return;
        }
        throw new IllegalArgumentException("theProxy is null or not the type of AsyncCommonEventResultProxy.");
    }

    /* access modifiers changed from: package-private */
    public void async() {
        this.proxy.async();
    }
}
