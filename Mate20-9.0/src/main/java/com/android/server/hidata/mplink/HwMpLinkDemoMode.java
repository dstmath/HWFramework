package com.android.server.hidata.mplink;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.widget.Toast;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMService;
import huawei.android.net.hwmplink.MpLinkCommonUtils;

public class HwMpLinkDemoMode {
    private static final String EXTRA_NETWORK_CHANGE_TYPE = "extra_network_change_type";
    private static final String TAG = "HiData_HwMpLinkDemoMode";
    private static final String TESTMODE_BCM_PERMISSION = "huawei.permission.RECEIVE_WIFI_PRO_STATE";
    private static final int TESTMODE_MSG_LTE_TEST = 10000;
    private static final String TESTMODE_NETWORK_CHANGE_ACTION = "huawei.wifi.pro.NETWORK_CHANGE";
    private static final int UNKOWEN_NETWORK = -1;
    private IntentFilter intentFilter = new IntentFilter();
    /* access modifiers changed from: private */
    public LocationManager locationManager;
    private BroadcastReceiver mBroadcastReceiver = new MpLinkTestCaseBroadcastReceiver();
    /* access modifiers changed from: private */
    public ConnectivityManager mConnectivityManager;
    private Context mContext;
    /* access modifiers changed from: private */
    public boolean mDeviceBootCommlied;
    private Handler mHandler;
    private MyNetworkCallback mNetworkCallback;

    private class MpLinkTestCaseBroadcastReceiver extends BroadcastReceiver {
        private MpLinkTestCaseBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (HwMpLinkDemoMode.this.mDeviceBootCommlied && "android.bluetooth.adapter.action.STATE_CHANGED".equals(action)) {
                int blueState = intent.getIntExtra("android.bluetooth.adapter.extra.STATE", 0);
                if (blueState == 10) {
                    HwMpLinkDemoMode.this.requestWiFiAndCellCoexist(false);
                } else if (blueState == 12) {
                    HwMpLinkDemoMode.this.requestWiFiAndCellCoexist(true);
                } else if (blueState == 10000) {
                    HwMpLinkDemoMode.this.lteActivating();
                }
            } else if (!HwMpLinkDemoMode.this.mDeviceBootCommlied || !"android.location.PROVIDERS_CHANGED".equals(action)) {
                if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "BOOT_COMPLETED");
                    boolean unused = HwMpLinkDemoMode.this.mDeviceBootCommlied = true;
                    LocationManager unused2 = HwMpLinkDemoMode.this.locationManager = (LocationManager) context.getSystemService("location");
                    ConnectivityManager unused3 = HwMpLinkDemoMode.this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
                }
            } else if (HwMpLinkDemoMode.this.locationManager == null) {
                MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "locationManager == null");
            } else {
                try {
                    if (HwMpLinkDemoMode.this.locationManager.isProviderEnabled("gps")) {
                        MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "Gps provider enabled.");
                        HwMpLinkDemoMode.this.handleBindProcessToNetwork();
                        return;
                    }
                    MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "Gps provider disEnabled.");
                    HwMpLinkDemoMode.this.handleClearBindProcessToNetwork();
                } catch (IllegalArgumentException e) {
                    MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "e:" + e);
                }
            }
        }
    }

    public class MyNetworkCallback extends ConnectivityManager.NetworkCallback {
        public MyNetworkCallback() {
        }

        public void onPreCheck(Network network) {
            MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "onPreCheck,network: " + network);
        }

        public void onAvailable(Network network) {
            MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "onAvailable,network: " + network);
        }

        public void onUnavailable() {
            MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "onUnavailable");
        }

        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "onCapabilitiesChanged,network: " + network);
        }

        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "onLinkPropertiesChanged,network: " + network);
        }

        public void onNetworkSuspended(Network network) {
            MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "onNetworkSuspended,network: " + network);
        }

        public void onNetworkResumed(Network network) {
            MpLinkCommonUtils.logD(HwMpLinkDemoMode.TAG, "onNetworkResumed,network: " + network);
        }
    }

    public HwMpLinkDemoMode(Context context, Handler handler) {
        this.mContext = context;
        this.mHandler = handler;
        registerBroadcastReceiver();
    }

    private void registerBroadcastReceiver() {
        this.intentFilter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        this.intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        this.intentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.intentFilter);
    }

    public void lteActivating() {
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addCapability(12);
        builder.addTransportType(0);
        NetworkRequest networkRequest = builder.build();
        this.mNetworkCallback = new MyNetworkCallback();
        this.mConnectivityManager.requestNetwork(networkRequest, this.mNetworkCallback, LaserTSMService.EXCUTE_OTA_RESULT_SUCCESS);
    }

    public void notificateNetWorkHandover(int state) {
        MpLinkCommonUtils.logD(TAG, "notificateNetWorkHandover state = " + state);
        Intent intent = new Intent(TESTMODE_NETWORK_CHANGE_ACTION);
        intent.setFlags(67108864);
        intent.putExtra(EXTRA_NETWORK_CHANGE_TYPE, state);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, TESTMODE_BCM_PERMISSION);
    }

    private int getNetworkID(int networkType) {
        Network[] networks = this.mConnectivityManager.getAllNetworks();
        if (networks != null) {
            int length = networks.length;
            for (int i = 0; i < length; i++) {
                NetworkInfo netInfo = this.mConnectivityManager.getNetworkInfo(networks[i]);
                if (netInfo != null && netInfo.getType() == networkType) {
                    Network network = networks[i];
                    if (network != null) {
                        return network.netId;
                    }
                }
            }
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public void handleBindProcessToNetwork() {
        int netid = getNetworkID(0);
        MpLinkCommonUtils.logD(TAG, "Bluetooth STATE_ON. LTE netid:" + netid);
        if (netid != -1) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 2, netid, MpLinkCommonUtils.getForegroundAppUid(this.mContext), new MpLinkQuickSwitchConfiguration()));
            notificateNetWorkHandover(1);
            return;
        }
        MpLinkCommonUtils.logD(TAG, "LTE is disconneced");
    }

    /* access modifiers changed from: private */
    public void handleClearBindProcessToNetwork() {
        int wifinetid = getNetworkID(1);
        MpLinkCommonUtils.logD(TAG, "Bluetooth STATE_OFF. wifi netid:" + wifinetid);
        if (wifinetid != -1) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 3, wifinetid, MpLinkCommonUtils.getForegroundAppUid(this.mContext), new MpLinkQuickSwitchConfiguration()));
            notificateNetWorkHandover(2);
            return;
        }
        MpLinkCommonUtils.logD(TAG, "wifi is disconneced");
    }

    /* access modifiers changed from: private */
    public void requestWiFiAndCellCoexist(boolean enabled) {
        if (enabled) {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 103, 0, 0));
        } else {
            this.mHandler.sendMessage(Message.obtain(this.mHandler, 103, 1, 0));
        }
    }

    public void showToast(String info) {
        Toast.makeText(this.mContext, info, 1).show();
    }

    public void wifiplusHandover(IRFInterferenceCallback callback) {
        MpLinkCommonUtils.logD(TAG, "wifiplusHandover");
    }
}
