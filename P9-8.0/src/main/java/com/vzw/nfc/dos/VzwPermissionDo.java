package com.vzw.nfc.dos;

public class VzwPermissionDo extends VzwTlv {
    public static final int TAG = 227;
    private boolean mVzwAllowed = false;

    public VzwPermissionDo(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, 227, valueIndex, valueLength);
    }

    public VzwPermissionDo(boolean allowed) {
        super(null, 227, 0, 0);
        this.mVzwAllowed = allowed;
    }

    public boolean isVzwAllowed() {
        return this.mVzwAllowed;
    }

    public void translate() throws DoParserException {
        boolean z = true;
        this.mVzwAllowed = false;
        byte[] data = getRawData();
        int index = getValueIndex();
        if (getValueLength() + index > data.length) {
            throw new DoParserException("Not enough data for VZW_AR_DO!");
        } else if (getValueLength() != 1) {
            throw new DoParserException("Invalid length of VZW-AR-DO!");
        } else {
            if (data[index] != (byte) 1) {
                z = false;
            }
            this.mVzwAllowed = z;
        }
    }
}
