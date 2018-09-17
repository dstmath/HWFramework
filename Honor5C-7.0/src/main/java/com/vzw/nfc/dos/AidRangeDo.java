package com.vzw.nfc.dos;

public class AidRangeDo extends VzwTlv {
    public static final int _TAG = 194;
    private byte[] mAidRange;

    public AidRangeDo(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, _TAG, valueIndex, valueLength);
        this.mAidRange = null;
    }

    public AidRangeDo(byte[] aid_range) {
        super(null, _TAG, 0, 0);
        this.mAidRange = null;
        this.mAidRange = aid_range;
    }

    public byte[] getAidRange() {
        return this.mAidRange;
    }

    public void translate() throws DoParserException {
        this.mAidRange = null;
        byte[] data = getRawData();
        int index = getValueIndex();
        if (getValueLength() + index > data.length) {
            throw new DoParserException("Not enough data for AID-RANGE-DO!");
        }
        this.mAidRange = new byte[getValueLength()];
        System.arraycopy(data, index, this.mAidRange, 0, getValueLength());
    }
}
