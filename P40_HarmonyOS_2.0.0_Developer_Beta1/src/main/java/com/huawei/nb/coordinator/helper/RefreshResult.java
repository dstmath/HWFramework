package com.huawei.nb.coordinator.helper;

public class RefreshResult {
    private long deltaSize;
    private long downloadedSize;
    private int errorCode;
    private boolean finished;
    private long index;

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setErrorCode(int i) {
        this.errorCode = i;
    }

    public long getIndex() {
        return this.index;
    }

    public void setIndex(long j) {
        this.index = j;
    }

    public long getDeltaSize() {
        return this.deltaSize;
    }

    public void setDeltaSize(long j) {
        this.deltaSize = j;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void setFinished(boolean z) {
        this.finished = z;
    }

    public void increaseIndex() {
        this.index++;
    }

    public long getDownloadedSize() {
        return this.downloadedSize;
    }

    public void setDownloadedSize(long j) {
        this.downloadedSize = j;
    }
}
