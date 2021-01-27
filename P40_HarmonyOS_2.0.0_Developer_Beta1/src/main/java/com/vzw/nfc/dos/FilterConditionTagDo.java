package com.vzw.nfc.dos;

public class FilterConditionTagDo extends VzwTlv {
    public static final byte SCREEN_OFF_TAG = -15;
    public static final int TAG = 210;
    private byte mFilterConditionTag = 0;

    public FilterConditionTagDo(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, TAG, valueIndex, valueLength);
    }

    public FilterConditionTagDo(byte filter_cond_tag) {
        super(null, TAG, 0, 0);
        this.mFilterConditionTag = filter_cond_tag;
    }

    public byte getFilterConditionTag() {
        return this.mFilterConditionTag;
    }

    @Override // com.vzw.nfc.dos.VzwTlv
    public void translate() throws DoParserException {
        this.mFilterConditionTag = 0;
        byte[] data = getRawData();
        int index = getValueIndex();
        if (getValueLength() + index > data.length) {
            throw new DoParserException("Not enough data for FILTER_CONDITION_TAG_DO!");
        } else if (getValueLength() == 1) {
            this.mFilterConditionTag = data[index];
        } else {
            throw new DoParserException("Invalid length of FILTER_CONDITION_TAG_DO!");
        }
    }
}
