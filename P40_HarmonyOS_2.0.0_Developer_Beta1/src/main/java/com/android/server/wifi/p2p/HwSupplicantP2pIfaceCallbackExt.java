package com.android.server.wifi.p2p;

import android.util.Log;

public class HwSupplicantP2pIfaceCallbackExt implements IHwSupplicantP2pIfaceCallbackExt {
    private static final int MAC_LENGTH = 6;
    private static final String TAG = "HwSupplicantP2pIfaceCallbackExt";
    private String mInterface;
    private WifiP2pMonitor mMonitor;

    HwSupplicantP2pIfaceCallbackExt(String iface, WifiP2pMonitor wifiMonitor) {
        this.mInterface = iface;
        this.mMonitor = wifiMonitor;
    }

    public static HwSupplicantP2pIfaceCallbackExt createHwSupplicantP2pIfaceCallbackExt(String iface, WifiP2pMonitor wifiMonitor) {
        return new HwSupplicantP2pIfaceCallbackExt(iface, wifiMonitor);
    }

    public void onHwDeviceFound(byte[] srcAddress, byte[] p2pDeviceAddress, byte[] primaryDeviceType, String deviceName, short configMethods, byte deviceCapabilities, int groupCapabilities, byte[] wfdDeviceInfo) {
        Log.i(TAG, "onHwDeviceFound is called");
        if (deviceName == null) {
            Log.e(TAG, "Missing hw device name.");
        } else if (wfdDeviceInfo != null && wfdDeviceInfo.length > 6) {
            try {
                byte[] hwInfo = new byte[(p2pDeviceAddress.length + wfdDeviceInfo.length)];
                System.arraycopy(p2pDeviceAddress, 0, hwInfo, 0, p2pDeviceAddress.length);
                System.arraycopy(wfdDeviceInfo, 0, hwInfo, p2pDeviceAddress.length, wfdDeviceInfo.length);
                this.mMonitor.getIHwWifiP2pMonitorExt().broadcastHwP2pDeviceFound(this.mInterface, hwInfo);
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "send hw p2p device info broadcast error, array index out of bounds.");
            }
        }
    }
}
