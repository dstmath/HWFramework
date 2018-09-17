package com.vzw.nfc.dos;

public class RoutingModeDo extends VzwTlv {
    public static final int TAG = 178;
    private boolean mFullPowerModeAllowed = false;
    private boolean mLowPowerModeAllowed = false;
    private boolean mNoPowerModeAllowed = false;
    private byte mRoutingInfo = (byte) 0;

    public RoutingModeDo(byte[] rawData, int valueIndex, int valueLength) {
        super(rawData, 178, valueIndex, valueLength);
    }

    public RoutingModeDo(boolean low_power, boolean full_power, boolean no_power) {
        super(null, 178, 0, 0);
        this.mLowPowerModeAllowed = low_power;
        this.mFullPowerModeAllowed = full_power;
        this.mNoPowerModeAllowed = no_power;
    }

    public RoutingModeDo(byte route_info) {
        super(null, 178, 0, 0);
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
            if ((this.mRoutingInfo & 1) != 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            this.mNoPowerModeAllowed = z2;
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
