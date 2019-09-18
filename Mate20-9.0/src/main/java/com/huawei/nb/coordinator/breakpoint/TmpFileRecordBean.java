package com.huawei.nb.coordinator.breakpoint;

public class TmpFileRecordBean {
    private String eTag;
    private long fileLength;
    private String signedUrl;
    private String tmpFileName;
    private long totalFileLength;

    public String getTmpFileName() {
        return this.tmpFileName;
    }

    public void setTmpFileName(String tmpFileName2) {
        this.tmpFileName = tmpFileName2;
    }

    public String getSignedUrl() {
        return this.signedUrl;
    }

    public void setSignedUrl(String signedUrl2) {
        this.signedUrl = signedUrl2;
    }

    public long getTotalFileLength() {
        return this.totalFileLength;
    }

    public void setTotalFileLength(long totalFileLength2) {
        this.totalFileLength = totalFileLength2;
    }

    public long getFileLength() {
        return this.fileLength;
    }

    public void setFileLength(long fileLength2) {
        this.fileLength = fileLength2;
    }

    public String getETag() {
        return this.eTag;
    }

    public void setETag(String eTag2) {
        this.eTag = eTag2;
    }
}
