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

    public void setSegmentIndex(int segmentIndex2) {
        this.segmentIndex = segmentIndex2;
    }

    public String getFileId() {
        return this.fileId;
    }

    public void setFileId(String fileId2) {
        this.fileId = fileId2;
    }

    public int[] getOptionalData() {
        return this.optionalData;
    }

    public void setOptionalData(int[] optionalData2) {
        this.optionalData = optionalData2;
    }

    public boolean isLastSegment() {
        return this.lastSegment;
    }

    public void setLastSegment(boolean lastSegment2) {
        this.lastSegment = lastSegment2;
    }
}
