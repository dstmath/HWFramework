package com.huawei.msdp.devicestatus;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwMsdpDeviceStatusServiceConnectionAdapter {
    private HwMSDPDeviceStatusServiceConnection mConnection = new HwMSDPDeviceStatusServiceConnection() {
        /* class com.huawei.msdp.devicestatus.HwMsdpDeviceStatusServiceConnectionAdapter.AnonymousClass1 */

        @Override // com.huawei.msdp.devicestatus.HwMSDPDeviceStatusServiceConnection
        public void onServiceConnected() {
            HwMsdpDeviceStatusServiceConnectionAdapter.this.onServiceConnected();
        }

        @Override // com.huawei.msdp.devicestatus.HwMSDPDeviceStatusServiceConnection
        public void onServiceDisconnected() {
            HwMsdpDeviceStatusServiceConnectionAdapter.this.onServiceDisconnected();
        }
    };

    public void onServiceConnected() {
    }

    public void onServiceDisconnected() {
    }

    public HwMSDPDeviceStatusServiceConnection getConnection() {
        return this.mConnection;
    }
}
