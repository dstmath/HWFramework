package com.vzw.nfc.dos;

public class AidRangeDo extends VzwTlv {
    public static final int TAG = 194;
    private byte[] mAidRange = null;

    public AidRangeDo(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, TAG, valueIndex, valueLength);
    }

    public AidRangeDo(byte[] aid_range) {
        super(null, TAG, 0, 0);
        this.mAidRange = aid_range;
    }

    public byte[] getAidRange() {
        return this.mAidRange;
    }

    public void translate() throws DoParserException {
        this.mAidRange = null;
        byte[] data = getRawData();
        int index = getValueIndex();
        if (getValueLength() + index <= data.length) {
            this.mAidRange = new byte[getValueLength()];
            System.arraycopy(data, index, this.mAidRange, 0, getValueLength());
            return;
        }
        throw new DoParserException("Not enough data for AID-RANGE-DO!");
    }
}
