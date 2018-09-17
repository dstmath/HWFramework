package com.vzw.nfc.dos;

public class RoutingModeDo extends VzwTlv {
    public static final int _TAG = 178;
    private boolean mFullPowerModeAllowed;
    private boolean mLowPowerModeAllowed;
    private boolean mNoPowerModeAllowed;
    private byte mRoutingInfo;

    public RoutingModeDo(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, _TAG, valueIndex, valueLength);
        this.mRoutingInfo = (byte) 0;
        this.mLowPowerModeAllowed = false;
        this.mFullPowerModeAllowed = false;
        this.mNoPowerModeAllowed = false;
    }

    public RoutingModeDo(boolean low_power, boolean full_power, boolean no_power) {
        super(null, _TAG, 0, 0);
        this.mRoutingInfo = (byte) 0;
        this.mLowPowerModeAllowed = false;
        this.mFullPowerModeAllowed = false;
        this.mNoPowerModeAllowed = false;
        this.mLowPowerModeAllowed = low_power;
        this.mFullPowerModeAllowed = full_power;
        this.mNoPowerModeAllowed = no_power;
    }

    public RoutingModeDo(byte route_info) {
        super(null, _TAG, 0, 0);
        this.mRoutingInfo = (byte) 0;
        this.mLowPowerModeAllowed = false;
        this.mFullPowerModeAllowed = false;
        this.mNoPowerModeAllowed = false;
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

    public void translate() throws DoParserException {
        boolean z = true;
        this.mRoutingInfo = (byte) 0;
        byte[] data = getRawData();
        int index = getValueIndex();
        if (getValueLength() + index > data.length) {
            throw new DoParserException("Not enough data for FILTER_CONDITION_TAG_DO!");
        } else if (getValueLength() != 1) {
            throw new DoParserException("Invalid length of FILTER_CONDITION_TAG_DO!");
        } else {
            boolean z2;
            this.mRoutingInfo = data[index];
            this.mNoPowerModeAllowed = (this.mRoutingInfo & 1) != 0;
            if ((this.mRoutingInfo & 2) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mLowPowerModeAllowed = z2;
            if ((this.mRoutingInfo & 4) == 0) {
                z = false;
            }
            this.mFullPowerModeAllowed = z;
        }
    }
}
