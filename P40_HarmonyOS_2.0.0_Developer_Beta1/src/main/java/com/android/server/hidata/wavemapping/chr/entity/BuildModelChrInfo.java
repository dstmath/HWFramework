package com.android.server.hidata.wavemapping.chr.entity;

public class BuildModelChrInfo extends ChrInfo {
    private ApChrStatInfo apType = new ApChrStatInfo();
    private int batchAll;
    private int batchCell;
    private int batchMain;
    private int configVerAll;
    private int configVerCell;
    private int configVerMain;
    private int fingerAll;
    private int fingerCell;
    private int fingerMain;
    private int firstTimeAll;
    private int firstTimeCell;
    private int firstTimeMain;
    private float maxDistAll;
    private float maxDistCell;
    private int maxDistMain;
    private int modelCell;
    private int modelMain;
    private int ref;
    private int retAll;
    private int retCell;
    private int retMain;
    private StgUsageChrInfo storage;
    private int testDataAll;
    private int testDataCell;
    private int testDataMain;
    private int trainDataAll;
    private int trainDataCell;
    private int trainDataMain;
    private int updateAll;
    private int updateCell;
    private int updateMain;

    public int getBatchAll() {
        return this.batchAll;
    }

    public void setBatchAll(int batchAll2) {
        this.batchAll = batchAll2;
    }

    public int getFingerAll() {
        return this.fingerAll;
    }

    public void setFingerAll(int fingerAll2) {
        this.fingerAll = fingerAll2;
    }

    public int getRetAll() {
        return this.retAll;
    }

    public void setRetAll(int retAll2) {
        this.retAll = retAll2;
    }

    public int getTrainDataAll() {
        return this.trainDataAll;
    }

    public void setTrainDataAll(int trainDataAll2) {
        this.trainDataAll = trainDataAll2;
    }

    public int getTestDataAll() {
        return this.testDataAll;
    }

    public void setTestDataAll(int testDataAll2) {
        this.testDataAll = testDataAll2;
    }

    public int getUpdateAll() {
        return this.updateAll;
    }

    public void setUpdateAll(int updateAll2) {
        this.updateAll = updateAll2;
    }

    public int getFirstTimeAll() {
        return this.firstTimeAll;
    }

    public void setFirstTimeAll(int firstTimeAll2) {
        this.firstTimeAll = firstTimeAll2;
    }

    public float getMaxDistAll() {
        return this.maxDistAll;
    }

    public void setMaxDistAll(float maxDistAll2) {
        this.maxDistAll = maxDistAll2;
    }

    public int getConfigVerAll() {
        return this.configVerAll;
    }

    public void setConfigVerAll(int configVerAll2) {
        this.configVerAll = configVerAll2;
    }

    public ApChrStatInfo getApType() {
        return this.apType;
    }

    public void setApType(ApChrStatInfo apType2) {
        this.apType = apType2;
    }

    public int getBatchMain() {
        return this.batchMain;
    }

    public void setBatchMain(int batchMain2) {
        this.batchMain = batchMain2;
    }

    public int getFingerMain() {
        return this.fingerMain;
    }

    public void setFingerMain(int fingerMain2) {
        this.fingerMain = fingerMain2;
    }

    public int getRetMain() {
        return this.retMain;
    }

    public void setRetMain(int retMain2) {
        this.retMain = retMain2;
    }

    public int getTrainDataMain() {
        return this.trainDataMain;
    }

    public void setTrainDataMain(int trainDataMain2) {
        this.trainDataMain = trainDataMain2;
    }

    public int getTestDataMain() {
        return this.testDataMain;
    }

    public void setTestDataMain(int testDataMain2) {
        this.testDataMain = testDataMain2;
    }

    public int getUpdateMain() {
        return this.updateMain;
    }

    public void setUpdateMain(int updateMain2) {
        this.updateMain = updateMain2;
    }

    public int getFirstTimeMain() {
        return this.firstTimeMain;
    }

    public void setFirstTimeMain(int firstTimeMain2) {
        this.firstTimeMain = firstTimeMain2;
    }

    public int getModelMain() {
        return this.modelMain;
    }

    public void setModelMain(int modelMain2) {
        this.modelMain = modelMain2;
    }

    public int getMaxDistMain() {
        return this.maxDistMain;
    }

    public void setMaxDistMain(int maxDistMain2) {
        this.maxDistMain = maxDistMain2;
    }

    public int getConfigVerMain() {
        return this.configVerMain;
    }

    public void setConfigVerMain(int configVerMain2) {
        this.configVerMain = configVerMain2;
    }

    public int getBatchCell() {
        return this.batchCell;
    }

    public void setBatchCell(int batchCell2) {
        this.batchCell = batchCell2;
    }

    public int getFingerCell() {
        return this.fingerCell;
    }

    public void setFingerCell(int fingerCell2) {
        this.fingerCell = fingerCell2;
    }

    public int getRetCell() {
        return this.retCell;
    }

    public void setRetCell(int retCell2) {
        this.retCell = retCell2;
    }

    public int getTrainDataCell() {
        return this.trainDataCell;
    }

    public void setTrainDataCell(int trainDataCell2) {
        this.trainDataCell = trainDataCell2;
    }

    public int getTestDataCell() {
        return this.testDataCell;
    }

    public void setTestDataCell(int testDataCell2) {
        this.testDataCell = testDataCell2;
    }

    public int getUpdateCell() {
        return this.updateCell;
    }

    public void setUpdateCell(int updateCell2) {
        this.updateCell = updateCell2;
    }

    public int getFirstTimeCell() {
        return this.firstTimeCell;
    }

    public void setFirstTimeCell(int firstTimeCell2) {
        this.firstTimeCell = firstTimeCell2;
    }

    public int getModelCell() {
        return this.modelCell;
    }

    public void setModelCell(int modelCell2) {
        this.modelCell = modelCell2;
    }

    public float getMaxDistCell() {
        return this.maxDistCell;
    }

    public void setMaxDistCell(float maxDistCell2) {
        this.maxDistCell = maxDistCell2;
    }

    public int getConfigVerCell() {
        return this.configVerCell;
    }

    public void setConfigVerCell(int configVerCell2) {
        this.configVerCell = configVerCell2;
    }

    public int getRef() {
        return this.ref;
    }

    public void setRef(int ref2) {
        this.ref = ref2;
    }

    public StgUsageChrInfo getStorage() {
        return this.storage;
    }

    public void setStorage(StgUsageChrInfo storage2) {
        this.storage = storage2;
    }

    public String toString() {
        return "BuildModelChrInfo{batchAll=" + this.batchAll + ", fingerAll=" + this.fingerAll + ", retAll=" + this.retAll + ", trainDataAll=" + this.trainDataAll + ", testDataAll=" + this.testDataAll + ", updateAll=" + this.updateAll + ", firstTimeAll=" + this.firstTimeAll + ", maxDistAll=" + this.maxDistAll + ", configVerAll=" + this.configVerAll + ", apType=" + this.apType.toString() + ", batchMain=" + this.batchMain + ", fingerMain=" + this.fingerMain + ", retMain=" + this.retMain + ", trainDataMain=" + this.trainDataMain + ", testDataMain=" + this.testDataMain + ", updateMain=" + this.updateMain + ", firstTimeMain=" + this.firstTimeMain + ", modelMain=" + this.modelMain + ", maxDistMain=" + this.maxDistMain + ", configVerMain=" + this.configVerMain + ", batchCell=" + this.batchCell + ", fingerCell=" + this.fingerCell + ", retCell=" + this.retCell + ", trainDataCell=" + this.trainDataCell + ", testDataCell=" + this.testDataCell + ", updateCell=" + this.updateCell + ", firstTimeCell=" + this.firstTimeCell + ", modelCell=" + this.modelCell + ", maxDistCell=" + this.maxDistCell + ", configVerCell=" + this.configVerCell + ", ref=" + this.ref + '}';
    }
}
