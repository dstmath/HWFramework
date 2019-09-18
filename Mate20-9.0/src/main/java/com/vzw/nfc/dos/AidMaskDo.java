package com.vzw.nfc.dos;

public class AidMaskDo extends VzwTlv {
    public static final int TAG = 195;
    private byte[] mAidMask = null;

    public AidMaskDo(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, TAG, valueIndex, valueLength);
    }

    public AidMaskDo(byte[] aid_mask) {
        super(null, TAG, 0, 0);
        this.mAidMask = aid_mask;
    }

    public byte[] getAidMask() {
        return this.mAidMask;
    }

    public void translate() throws DoParserException {
        this.mAidMask = null;
        byte[] data = getRawData();
        int index = getValueIndex();
        if (getValueLength() + index <= data.length) {
            this.mAidMask = new byte[getValueLength()];
            System.arraycopy(data, index, this.mAidMask, 0, getValueLength());
            return;
        }
        throw new DoParserException("Not enough data for AID-MASK-DO!");
    }
}
