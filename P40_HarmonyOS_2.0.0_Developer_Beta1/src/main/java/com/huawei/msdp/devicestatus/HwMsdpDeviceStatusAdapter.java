package com.huawei.msdp.devicestatus;

import android.content.Context;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwMsdpDeviceStatusAdapter {
    private HwMSDPDeviceStatus mDevMsdpDeviceStatus;

    private HwMsdpDeviceStatusAdapter() {
    }

    public HwMsdpDeviceStatusAdapter(Context context) {
        this.mDevMsdpDeviceStatus = new HwMSDPDeviceStatus(context);
    }

    public void connectService(HwMsdpDeviceStatusChangedCallBackAdapter callBack, HwMsdpDeviceStatusServiceConnectionAdapter connection) {
        this.mDevMsdpDeviceStatus.connectService(callBack.getCallBack(), connection.getConnection());
    }

    public void disconnectService() {
        this.mDevMsdpDeviceStatus.disconnectService();
    }

    public boolean enableDeviceStatusEvent(String deviceStatus, int eventType, long reportLatencyNs) {
        return this.mDevMsdpDeviceStatus.enableDeviceStatusEvent(deviceStatus, eventType, reportLatencyNs);
    }

    public boolean disableDeviceStatusEvent(String deviceStatus, int eventType) {
        return this.mDevMsdpDeviceStatus.disableDeviceStatusEvent(deviceStatus, eventType);
    }

    public HwMsdpDeviceStatusChangeEventAdapter getCurrentDeviceStatus() {
        HwMSDPDeviceStatusChangeEvent event = this.mDevMsdpDeviceStatus.getCurrentDeviceStatus();
        if (event == null) {
            return null;
        }
        return new HwMsdpDeviceStatusChangeEventAdapter(event);
    }
}
