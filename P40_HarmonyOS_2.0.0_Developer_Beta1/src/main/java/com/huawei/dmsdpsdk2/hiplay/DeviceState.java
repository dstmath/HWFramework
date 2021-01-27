package com.huawei.dmsdpsdk2.hiplay;

import com.huawei.dmsdp.devicevirtualization.Capability;

public class DeviceState {
    public static final String CONTINUTING_FAILED = "CONTINUTING_FAILED";
    public static final String CONTIUITING = "CONTIUITING";
    public static final String NO_CONTINUTING = "NO_CONTINUTING";
    public static final String STOP_CONTINUTING_FAILED = "STOP_CONTINUTING_FAILED";
    private Capability capability;
    private String connState;
    private String deviceId;

    public DeviceState(String deviceId2, Capability capability2, String connState2) {
        this.deviceId = deviceId2;
        this.capability = capability2;
        this.connState = connState2;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId2) {
        this.deviceId = deviceId2;
    }

    public Capability getCapability() {
        return this.capability;
    }

    public void setCapability(Capability capability2) {
        this.capability = capability2;
    }

    public String getConnState() {
        return this.connState;
    }

    public void setConnState(String connState2) {
        this.connState = connState2;
    }
}
