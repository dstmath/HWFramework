package com.huawei.zxing.pdf417;

import java.io.Serializable;

public final class PDF417ResultMetadata implements Serializable {
    private String fileId;
    private boolean lastSegment;
    private int[] optionalData;
    private int segmentIndex;

    public int getSegmentIndex() {
        return this.segmentIndex;
    }

    public void setSegmentIndex(int segmentIndex) {
        this.segmentIndex = segmentIndex;
    }

    public String getFileId() {
        return this.fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public int[] getOptionalData() {
        return this.optionalData;
    }

    public void setOptionalData(int[] optionalData) {
        this.optionalData = optionalData;
    }

    public boolean isLastSegment() {
        return this.lastSegment;
    }

    public void setLastSegment(boolean lastSegment) {
        this.lastSegment = lastSegment;
    }
}
