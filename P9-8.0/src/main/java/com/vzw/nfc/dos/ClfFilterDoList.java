package com.vzw.nfc.dos;

import java.util.ArrayList;

public class ClfFilterDoList extends VzwTlv {
    private ArrayList<ClfFilterDo> mClfFilterDos = new ArrayList();

    public ClfFilterDoList(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, 0, valueIndex, valueLength);
    }

    public ArrayList<ClfFilterDo> getClfFilterDos() {
        return this.mClfFilterDos;
    }

    public void translate() throws DoParserException {
        this.mClfFilterDos.clear();
        byte[] data = getRawData();
        int index = getValueIndex();
        if (getValueLength() != 0) {
            if (getValueLength() + index > data.length) {
                throw new DoParserException("Not enough data for ALL_CLF_FILTER_DO!");
            }
            int currentPos = index;
            int endPos = index + getValueLength();
            do {
                VzwTlv temp = VzwTlv.parse(data, currentPos);
                if (temp.getTag() == 254) {
                    ClfFilterDo tmpClfFilterDo = new ClfFilterDo(data, temp.getValueIndex(), temp.getValueLength());
                    tmpClfFilterDo.translate();
                    this.mClfFilterDos.add(tmpClfFilterDo);
                    currentPos = temp.getValueIndex() + temp.getValueLength();
                } else {
                    throw new DoParserException("Invalid DO in ALL_CLF_FILTER_DO!");
                }
            } while (currentPos < endPos);
        }
    }
}
