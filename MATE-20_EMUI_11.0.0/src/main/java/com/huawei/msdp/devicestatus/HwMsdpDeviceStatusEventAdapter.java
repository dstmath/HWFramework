package com.huawei.msdp.devicestatus;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public final class HwMsdpDeviceStatusEventAdapter {
    private HwMSDPDeviceStatusEvent mHwMsdpDevStatusEvent;

    private HwMsdpDeviceStatusEventAdapter() {
    }

    public HwMsdpDeviceStatusEventAdapter(HwMSDPDeviceStatusEvent event) {
        this.mHwMsdpDevStatusEvent = event;
    }

    public long getmTimestampNs() {
        return this.mHwMsdpDevStatusEvent.getmTimestampNs();
    }

    public String getmDeviceStatus() {
        return this.mHwMsdpDevStatusEvent.getmDeviceStatus();
    }
}
