package com.android.server.hidata.wavemapping.chr.entity;

public class ModelInvalidChrInfo extends ChrInfo {
    private int identifyAll;
    private int identifyCell;
    private int identifyMain;
    private byte isPassAll;
    private int isPassCell;
    private byte isPassMain;
    private int modelCell;
    private int modelMain;
    private int ref;
    private int updateAll;
    private int updateCell;
    private int updateMain;

    public int getIdentifyAll() {
        return this.identifyAll;
    }

    public void setIdentifyAll(int identifyAll2) {
        this.identifyAll = identifyAll2;
    }

    public byte getIsPassAll() {
        return this.isPassAll;
    }

    public void setIsPassAll(byte isPassAll2) {
        this.isPassAll = isPassAll2;
    }

    public int getUpdateAll() {
        return this.updateAll;
    }

    public void setUpdateAll(int updateAll2) {
        this.updateAll = updateAll2;
    }

    public int getIdentifyMain() {
        return this.identifyMain;
    }

    public void setIdentifyMain(int identifyMain2) {
        this.identifyMain = identifyMain2;
    }

    public byte getIsPassMain() {
        return this.isPassMain;
    }

    public void setIsPassMain(byte isPassMain2) {
        this.isPassMain = isPassMain2;
    }

    public int getUpdateMain() {
        return this.updateMain;
    }

    public void setUpdateMain(int updateMain2) {
        this.updateMain = updateMain2;
    }

    public int getModelMain() {
        return this.modelMain;
    }

    public void setModelMain(int modelMain2) {
        this.modelMain = modelMain2;
    }

    public int getIdentifyCell() {
        return this.identifyCell;
    }

    public void setIdentifyCell(int identifyCell2) {
        this.identifyCell = identifyCell2;
    }

    public int getIsPassCell() {
        return this.isPassCell;
    }

    public void setIsPassCell(int isPassCell2) {
        this.isPassCell = isPassCell2;
    }

    public int getUpdateCell() {
        return this.updateCell;
    }

    public void setUpdateCell(int updateCell2) {
        this.updateCell = updateCell2;
    }

    public int getModelCell() {
        return this.modelCell;
    }

    public void setModelCell(int modelCell2) {
        this.modelCell = modelCell2;
    }

    public int getRef() {
        return this.ref;
    }

    public void setRef(int ref2) {
        this.ref = ref2;
    }

    public String toString() {
        return "ModelInvalidChrInfo{loc=" + ((int) this.loc) + ", identifyAll=" + this.identifyAll + ", isPassAll=" + ((int) this.isPassAll) + ", updateAll=" + this.updateAll + ", modelAll=" + this.modelAll + ", identifyMain=" + this.identifyMain + ", isPassMain=" + ((int) this.isPassMain) + ", updateMain=" + this.updateMain + ", modelMain=" + this.modelMain + ", identifyCell=" + this.identifyCell + ", isPassCell=" + this.isPassCell + ", updateCell=" + this.updateCell + ", modelCell=" + this.modelCell + ", label=" + this.label + ", ref=" + this.ref + '}';
    }
}
