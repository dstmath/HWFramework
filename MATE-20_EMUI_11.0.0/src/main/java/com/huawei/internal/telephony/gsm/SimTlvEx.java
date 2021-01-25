package com.huawei.internal.telephony.gsm;

import com.android.internal.telephony.gsm.SimTlv;

public class SimTlvEx {
    private SimTlv mSimTlv = null;

    public SimTlvEx() {
    }

    public SimTlvEx(byte[] record, int offset, int length) {
        this.mSimTlv = new SimTlv(record, offset, length);
    }

    public void setSimTlv(SimTlv simTlv) {
        this.mSimTlv = simTlv;
    }

    public byte[] getData() {
        SimTlv simTlv = this.mSimTlv;
        if (simTlv != null) {
            return simTlv.getData();
        }
        return new byte[0];
    }

    public boolean nextObject() {
        SimTlv simTlv = this.mSimTlv;
        if (simTlv != null) {
            return simTlv.nextObject();
        }
        return false;
    }

    public boolean isValidObject() {
        SimTlv simTlv = this.mSimTlv;
        if (simTlv != null) {
            return simTlv.isValidObject();
        }
        return false;
    }

    public int getTag() {
        SimTlv simTlv = this.mSimTlv;
        if (simTlv != null) {
            return simTlv.getTag();
        }
        return 0;
    }
}
