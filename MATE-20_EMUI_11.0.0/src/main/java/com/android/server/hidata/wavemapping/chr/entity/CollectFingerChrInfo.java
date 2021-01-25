package com.android.server.hidata.wavemapping.chr.entity;

public class CollectFingerChrInfo {
    private int batchAll;
    private int batchCell;
    private int batchMain;
    private int fingerActiveAll;
    private int fingersCell;
    private int fingersMain;
    private int fingersPassiveAll;
    private int updateAll;
    private int updateCell;
    private int updateMain;

    public int getBatchAll() {
        return this.batchAll;
    }

    public void setBatchAll(int batchAll2) {
        this.batchAll = batchAll2;
    }

    public int getFingersPassiveAll() {
        return this.fingersPassiveAll;
    }

    public void setFingersPassiveAll(int fingersPassiveAll2) {
        this.fingersPassiveAll = fingersPassiveAll2;
    }

    public int getFingerActiveAll() {
        return this.fingerActiveAll;
    }

    public void setFingerActiveAll(int fingerActiveAll2) {
        this.fingerActiveAll = fingerActiveAll2;
    }

    public int getUpdateAll() {
        return this.updateAll;
    }

    public void setUpdateAll(int updateAll2) {
        this.updateAll = updateAll2;
    }

    public int getBatchMain() {
        return this.batchMain;
    }

    public void setBatchMain(int batchMain2) {
        this.batchMain = batchMain2;
    }

    public int getFingersMain() {
        return this.fingersMain;
    }

    public void setFingersMain(int fingersMain2) {
        this.fingersMain = fingersMain2;
    }

    public int getUpdateMain() {
        return this.updateMain;
    }

    public void setUpdateMain(int updateMain2) {
        this.updateMain = updateMain2;
    }

    public int getBatchCell() {
        return this.batchCell;
    }

    public void setBatchCell(int batchCell2) {
        this.batchCell = batchCell2;
    }

    public int getFingersCell() {
        return this.fingersCell;
    }

    public void setFingersCell(int fingersCell2) {
        this.fingersCell = fingersCell2;
    }

    public int getUpdateCell() {
        return this.updateCell;
    }

    public void setUpdateCell(int updateCell2) {
        this.updateCell = updateCell2;
    }

    public String toString() {
        return "CollectFingerChrInfo{batchAll=" + this.batchAll + ", fingersPassiveAll=" + this.fingersPassiveAll + ", fingerActiveAll=" + this.fingerActiveAll + ", updateAll=" + this.updateAll + ", batchMain=" + this.batchMain + ", fingersMain=" + this.fingersMain + ", updateMain=" + this.updateMain + ", batchCell=" + this.batchCell + ", fingersCell=" + this.fingersCell + ", updateCell=" + this.updateCell + '}';
    }
}
