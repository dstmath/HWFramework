package ohos.backgroundtaskmgr;

public final class DelaySuspendInfo {
    private final int actualDelayTime;
    private final int requestId;

    public int getRequestId() {
        return this.requestId;
    }

    public int getActualDelayTime() {
        return this.actualDelayTime;
    }

    DelaySuspendInfo(int i, int i2) {
        this.requestId = i;
        this.actualDelayTime = i2;
    }
}
