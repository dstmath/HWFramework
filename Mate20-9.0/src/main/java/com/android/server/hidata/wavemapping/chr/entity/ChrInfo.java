package com.android.server.hidata.wavemapping.chr.entity;

import com.android.server.hidata.wavemapping.cons.Constant;

public class ChrInfo {
    protected static final byte ALLAP = 0;
    protected static final byte HOME_LOC = 0;
    protected static final byte MAINAP = 1;
    protected static final byte OFFICE_LOC = 1;
    protected static final byte OTHER_LOC = 2;
    protected int label;
    protected byte loc;
    protected int modelAll;

    public int getLabel() {
        return this.label;
    }

    public void setLabel(boolean isMainAp) {
        this.label = isMainAp;
    }

    public byte getLoc() {
        return this.loc;
    }

    public void setLoc(String loc2) {
        if (loc2.equals(Constant.NAME_FREQLOCATION_HOME)) {
            this.loc = 0;
        } else if (loc2.equals(Constant.NAME_FREQLOCATION_OFFICE)) {
            this.loc = 1;
        } else {
            this.loc = OTHER_LOC;
        }
    }

    public int getModelAll() {
        return this.modelAll;
    }

    public void setModelAll(int modelName) {
        this.modelAll = modelName;
    }
}
