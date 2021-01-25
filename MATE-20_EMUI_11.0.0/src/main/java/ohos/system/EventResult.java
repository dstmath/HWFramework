package ohos.system;

/* compiled from: Events */
final class EventResult {
    private String errorInfo = "unknown error";
    private boolean ok = false;

    EventResult() {
    }

    /* access modifiers changed from: package-private */
    public boolean isOk() {
        return this.ok;
    }

    /* access modifiers changed from: package-private */
    public String getErrorInfo() {
        return this.errorInfo;
    }
}
