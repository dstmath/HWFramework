package com.huawei.dmsdp.devicevirtualization;

public class VirtualDeviceInfo {
    private String mId;
    private String mName;
    private VirtualDeviceState mState;
    private VirtualDeviceType mType;

    VirtualDeviceInfo(VirtualDeviceType type, String virtualDeviceId, String virtualDeviceName, VirtualDeviceState state) {
        this.mType = type;
        this.mId = virtualDeviceId;
        this.mName = virtualDeviceName;
        this.mState = state;
    }

    public VirtualDeviceType getType() {
        return this.mType;
    }

    public String getId() {
        return this.mId;
    }

    public String getName() {
        return this.mName;
    }

    public VirtualDeviceState getState() {
        return this.mState;
    }
}
