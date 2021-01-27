package com.huawei.msdp.devicestatus;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwMsdpDeviceStatusChangedCallBackAdapter {
    private HwMSDPDeviceStatusChangedCallBack mAdapter = new HwMSDPDeviceStatusChangedCallBack() {
        /* class com.huawei.msdp.devicestatus.HwMsdpDeviceStatusChangedCallBackAdapter.AnonymousClass1 */

        @Override // com.huawei.msdp.devicestatus.HwMSDPDeviceStatusChangedCallBack
        public void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent event) {
            HwMsdpDeviceStatusChangedCallBackAdapter.this.onDeviceStatusChanged(new HwMsdpDeviceStatusChangeEventAdapter(event));
        }
    };

    public void onDeviceStatusChanged(HwMsdpDeviceStatusChangeEventAdapter event) {
    }

    public HwMSDPDeviceStatusChangedCallBack getCallBack() {
        return this.mAdapter;
    }
}
