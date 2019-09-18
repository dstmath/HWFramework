package com.android.server.hidata.mplink;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.util.List;

public class HwMpLinkWifiImpl {
    public static final int BAND_WIDTH_160MHZ = 160;
    public static final int BAND_WIDTH_20MHZ = 20;
    public static final int BAND_WIDTH_40MHZ = 40;
    public static final int BAND_WIDTH_80MHZ = 80;
    private static final String TAG = "HiData_HwMpLinkWifiImpl";
    private Context mContext;
    private String mCurrentBssid = null;
    public int mCurrentWifiBandWidth = 0;
    public int mCurrentWifiFreq = 0;
    private Handler mHandler;
    private boolean mWifiConnectState = false;
    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;
    private boolean mWifiVpnConnected = false;
    private List<ScanResult> scanResultList;

    public HwMpLinkWifiImpl(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
    }

    private void sendMessage(int what) {
        this.mHandler.sendMessage(Message.obtain(this.mHandler, what));
    }

    public boolean isWifiConnected() {
        return this.mWifiConnectState;
    }

    public boolean getCurrentWifiConnectState() {
        return this.mWifiConnectState;
    }

    public boolean getCurrentWifiVpnState() {
        return this.mWifiVpnConnected;
    }

    public void setCurrentWifiVpnState(boolean vpnconnected) {
        this.mWifiVpnConnected = vpnconnected;
    }

    public void handleWifiNetworkStateChanged(NetworkInfo netInfo) {
        if (netInfo != null) {
            MpLinkCommonUtils.logI(TAG, "WIFI NETWORK_STATE_CHANGED_ACTION state:" + netInfo.getState());
            if (netInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                this.mWifiConnectState = false;
                HwMpLinkContentAware.getInstance(this.mContext).resetAiDeviceType();
                this.mCurrentBssid = null;
                this.mCurrentWifiFreq = 0;
                this.mCurrentWifiBandWidth = 0;
                sendMessage(HwMpLinkServiceImpl.MPLINK_MSG_WIFI_DISCONNECTED);
            } else if (netInfo.getState() == NetworkInfo.State.CONNECTED || netInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                this.mWifiConnectState = true;
                this.mWifiInfo = this.mWifiManager.getConnectionInfo();
                if (this.mWifiInfo != null) {
                    this.mCurrentBssid = this.mWifiInfo.getBSSID();
                    this.mCurrentWifiFreq = this.mWifiInfo.getFrequency();
                    this.mCurrentWifiBandWidth = getCurrentWifiBandWidth();
                    if (this.mCurrentWifiBandWidth == 0) {
                        if (ScanResult.is5GHz(this.mCurrentWifiFreq)) {
                            this.mCurrentWifiBandWidth = 40;
                        } else {
                            this.mCurrentWifiBandWidth = 20;
                        }
                    }
                    MpLinkCommonUtils.logD(TAG, "Freq:" + this.mCurrentWifiFreq + ", BandWidth:" + this.mCurrentWifiBandWidth);
                }
                sendMessage(HwMpLinkServiceImpl.MPLINK_MSG_WIFI_CONNECTED);
            }
        }
    }

    public void handleVpnStateChange(boolean vpnconnected) {
        if (this.mWifiVpnConnected != vpnconnected) {
            this.mWifiVpnConnected = vpnconnected;
            if (vpnconnected) {
                MpLinkCommonUtils.logD(TAG, "WIFI_VPN_CONNETED");
                sendMessage(HwMpLinkServiceImpl.MPLINK_MSG_WIFI_VPN_CONNETED);
                return;
            }
            MpLinkCommonUtils.logD(TAG, "WIFI_VPN_DISCONNETED");
            sendMessage(HwMpLinkServiceImpl.MPLINK_MSG_WIFI_VPN_DISCONNETED);
        }
    }

    private int getCurrentWifiBandWidth() {
        this.scanResultList = this.mWifiManager.getScanResults();
        if (this.scanResultList == null || this.scanResultList.isEmpty()) {
            return 0;
        }
        for (ScanResult scanResult : this.scanResultList) {
            if (scanResult != null && !TextUtils.isEmpty(scanResult.BSSID) && !TextUtils.isEmpty(this.mCurrentBssid) && this.mCurrentBssid.equals(scanResult.BSSID)) {
                MpLinkCommonUtils.logD(TAG, "channelWidth:" + scanResult.channelWidth);
                return wifiBandWidthConverter(scanResult.channelWidth);
            }
        }
        return 0;
    }

    public int wifiBandWidthConverter(int channelWidth) {
        if (channelWidth == 0) {
            return 20;
        }
        if (channelWidth == 1) {
            return 40;
        }
        if (channelWidth == 2) {
            return 80;
        }
        if (channelWidth == 3 || channelWidth == 4) {
            return BAND_WIDTH_160MHZ;
        }
        return 0;
    }
}
