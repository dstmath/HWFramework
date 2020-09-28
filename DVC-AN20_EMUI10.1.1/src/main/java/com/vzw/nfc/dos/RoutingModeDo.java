package com.vzw.nfc.dos;

public class RoutingModeDo extends VzwTlv {
    public static final int TAG = 178;
    private boolean mFullPowerModeAllowed = false;
    private boolean mLowPowerModeAllowed = false;
    private boolean mNoPowerModeAllowed = false;
    private byte mRoutingInfo = 0;

    public RoutingModeDo(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, TAG, valueIndex, valueLength);
    }

    public RoutingModeDo(boolean low_power, boolean full_power, boolean no_power) {
        super(null, TAG, 0, 0);
        this.mLowPowerModeAllowed = low_power;
        this.mFullPowerModeAllowed = full_power;
        this.mNoPowerModeAllowed = no_power;
    }

    public RoutingModeDo(byte route_info) {
        super(null, TAG, 0, 0);
        this.mRoutingInfo = route_info;
    }

    public byte getRoutingInfo() {
        return this.mRoutingInfo;
    }

    public boolean isLowPowerModeAllowed() {
        return this.mLowPowerModeAllowed;
    }

    public boolean isFullPowerModeAllowed() {
        return this.mFullPowerModeAllowed;
    }

    public boolean isNoPowerModeAllowed() {
        return this.mNoPowerModeAllowed;
    }

    @Override // com.vzw.nfc.dos.VzwTlv
    public void translate() throws DoParserException {
        boolean z = false;
        this.mRoutingInfo = 0;
        byte[] data = getRawData();
        int index = getValueIndex();
        if (getValueLength() + index > data.length) {
            throw new DoParserException("Not enough data for FILTER_CONDITION_TAG_DO!");
        } else if (getValueLength() == 1) {
            this.mRoutingInfo = data[index];
            this.mNoPowerModeAllowed = (this.mRoutingInfo & 1) != 0;
            this.mLowPowerModeAllowed = (this.mRoutingInfo & 2) != 0;
            if ((this.mRoutingInfo & 4) != 0) {
                z = true;
            }
            this.mFullPowerModeAllowed = z;
        } else {
            throw new DoParserException("Invalid length of FILTER_CONDITION_TAG_DO!");
        }
    }
}
