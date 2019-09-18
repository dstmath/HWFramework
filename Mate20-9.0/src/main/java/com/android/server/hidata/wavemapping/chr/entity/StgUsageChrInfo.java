package com.android.server.hidata.wavemapping.chr.entity;

public class StgUsageChrInfo {
    private float backupsize;
    private float dataSize;
    private float dbSize;
    private float logSize;
    private float modelSize;
    private float rawdataSize;

    public StgUsageChrInfo() {
    }

    public StgUsageChrInfo(float dbSize2, float rawdataSize2, float modelSize2, float logSize2, float dataSize2, float backupsize2) {
        this.dbSize = dbSize2;
        this.rawdataSize = rawdataSize2;
        this.modelSize = modelSize2;
        this.logSize = logSize2;
        this.dataSize = dataSize2;
        this.backupsize = backupsize2;
    }

    public float getDbSize() {
        return this.dbSize;
    }

    public void setDbSize(float dbSize2) {
        this.dbSize = dbSize2;
    }

    public float getRawdataSize() {
        return this.rawdataSize;
    }

    public void setRawdataSize(float rawdataSize2) {
        this.rawdataSize = rawdataSize2;
    }

    public float getModelSize() {
        return this.modelSize;
    }

    public void setModelSize(float modelSize2) {
        this.modelSize = modelSize2;
    }

    public float getLogSize() {
        return this.logSize;
    }

    public void setLogSize(float logSize2) {
        this.logSize = logSize2;
    }

    public float getDataSize() {
        return this.dataSize;
    }

    public void setDataSize(float dataSize2) {
        this.dataSize = dataSize2;
    }

    public float getBackupsize() {
        return this.backupsize;
    }

    public void setBackupsize(float backupsize2) {
        this.backupsize = backupsize2;
    }

    public String toString() {
        return "StgUsageChrInfo{dbSize=" + this.dbSize + ", rawdataSize=" + this.rawdataSize + ", modelSize=" + this.modelSize + ", logSize=" + this.logSize + ", dataSize=" + this.dataSize + ", backupsize=" + this.backupsize + '}';
    }
}
