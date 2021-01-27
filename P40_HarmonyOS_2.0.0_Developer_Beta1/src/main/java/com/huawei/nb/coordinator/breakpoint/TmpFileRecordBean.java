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

    public void setTmpFileName(String str) {
        this.tmpFileName = str;
    }

    public String getSignedUrl() {
        return this.signedUrl;
    }

    public void setSignedUrl(String str) {
        this.signedUrl = str;
    }

    public long getTotalFileLength() {
        return this.totalFileLength;
    }

    public void setTotalFileLength(long j) {
        this.totalFileLength = j;
    }

    public long getFileLength() {
        return this.fileLength;
    }

    public void setFileLength(long j) {
        this.fileLength = j;
    }

    public String getETag() {
        return this.eTag;
    }

    public void setETag(String str) {
        this.eTag = str;
    }
}
