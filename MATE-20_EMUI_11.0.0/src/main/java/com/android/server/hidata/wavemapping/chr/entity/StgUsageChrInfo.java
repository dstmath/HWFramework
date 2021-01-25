package com.android.server.hidata.wavemapping.chr.entity;

public class StgUsageChrInfo {
    private float backupSize;
    private float dataSize;
    private float dbSize;
    private float logSize;
    private float modelSize;
    private float rawDataSize;

    public StgUsageChrInfo() {
    }

    public StgUsageChrInfo(float dbSize2, float rawDataSize2, float modelSize2, float logSize2, float dataSize2, float backupSize2) {
        this.dbSize = dbSize2;
        this.rawDataSize = rawDataSize2;
        this.modelSize = modelSize2;
        this.logSize = logSize2;
        this.dataSize = dataSize2;
        this.backupSize = backupSize2;
    }

    public float getDbSize() {
        return this.dbSize;
    }

    public void setDbSize(float dbSize2) {
        this.dbSize = dbSize2;
    }

    public float getRawDataSize() {
        return this.rawDataSize;
    }

    public void setRawDataSize(float rawDataSize2) {
        this.rawDataSize = rawDataSize2;
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

    public float getBackupSize() {
        return this.backupSize;
    }

    public void setBackupSize(float backupSize2) {
        this.backupSize = backupSize2;
    }

    public String toString() {
        return "StgUsageChrInfo{dbSize=" + this.dbSize + ", rawDataSize=" + this.rawDataSize + ", modelSize=" + this.modelSize + ", logSize=" + this.logSize + ", dataSize=" + this.dataSize + ", backupSize=" + this.backupSize + '}';
    }
}
