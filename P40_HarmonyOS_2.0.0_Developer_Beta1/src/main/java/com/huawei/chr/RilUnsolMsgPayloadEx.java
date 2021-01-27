package com.huawei.chr;

import java.util.ArrayList;
import vendor.huawei.hardware.radio_radar.V1_0.RILUnsolMsgPayload;

public class RilUnsolMsgPayloadEx {
    private RILUnsolMsgPayload rilUnsolMsgPayload;

    RilUnsolMsgPayloadEx(RILUnsolMsgPayload rilUnsolMsgPayload2) {
        this.rilUnsolMsgPayload = rilUnsolMsgPayload2;
    }

    RilUnsolMsgPayloadEx(vendor.huawei.hardware.radio.chr.V1_0.RILUnsolMsgPayload rilUnsolMsgPayload2) {
        this.rilUnsolMsgPayload = new RILUnsolMsgPayload();
        this.rilUnsolMsgPayload.nData = rilUnsolMsgPayload2.nData;
        this.rilUnsolMsgPayload.nDatas = rilUnsolMsgPayload2.nDatas;
        this.rilUnsolMsgPayload.strData = rilUnsolMsgPayload2.strData;
        this.rilUnsolMsgPayload.strDatas = rilUnsolMsgPayload2.strDatas;
    }

    public int getIntData() {
        return this.rilUnsolMsgPayload.nData;
    }

    public ArrayList<Integer> getIntDatas() {
        return this.rilUnsolMsgPayload.nDatas;
    }

    public String getStringData() {
        return this.rilUnsolMsgPayload.strData;
    }

    public ArrayList<String> getStringDatas() {
        return this.rilUnsolMsgPayload.strDatas;
    }
}
