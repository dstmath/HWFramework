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

    public void setErrorCode(int errorCode2) {
        this.errorCode = errorCode2;
    }

    public long getIndex() {
        return this.index;
    }

    public void setIndex(long index2) {
        this.index = index2;
    }

    public long getDeltaSize() {
        return this.deltaSize;
    }

    public void setDeltaSize(long deltaSize2) {
        this.deltaSize = deltaSize2;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public void setFinished(boolean finished2) {
        this.finished = finished2;
    }

    public void increaseIndex() {
        this.index++;
    }

    public long getDownloadedSize() {
        return this.downloadedSize;
    }

    public void setDownloadedSize(long size) {
        this.downloadedSize = size;
    }
}
