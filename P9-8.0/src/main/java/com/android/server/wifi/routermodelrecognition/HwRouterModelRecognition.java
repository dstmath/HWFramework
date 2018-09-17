package com.android.server.wifi.routermodelrecognition;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkUtils;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.android.server.wifi.HwWifiCHRStateManager;
import com.android.server.wifi.HwWifiCHRStateManagerImpl;
import java.net.InetAddress;

public class HwRouterModelRecognition {
    private static final String ACTION_ROUTER_MODEL_RECOGNITION_RESULT = "com.huawei.wifiprobqeservice.action.ROUTER_MODEL_RECOGNITION_RESULT";
    private static final String ACTION_START_ROUTER_MODEL_RECOGINITON = "com.huawei.wifiprobqeservice.action.ROUTER_MODEL_RECOGNITION_SERVICE";
    private static final int DELAY_MSECS_STOP_RMC_SERVICE = 60000;
    private static final int DELAY_START_ROUTER_RECOGNITION = 300000;
    private static final boolean HWFLOW;
    private static final String INTENT_EXTRA_VALUE_ROUTER_BRAND = "ROUTER_BRAND";
    private static final String INTENT_EXTRA_VALUE_ROUTER_NAME = "ROUTER_NAME";
    private static final String INTENT_EXTRA_VALUE_UPLOAD_DFT = "UPLOAD_DFT";
    private static final String LOG_TAG = "HwRouterModelRecognition";
    private static final int MSG_START_RMC_SERVICE = 1000;
    private static final int MSG_STOP_RMC_SERVICE = 1001;
    private static final String PACKAGE_ROUTER_MODEL_RECOGINITON = "com.huawei.wifiprobqeservice";
    private static final String ROUTER_MODEL_RECOGNITION_PERMISSION = "com.huawei.permission.WIFIPRO_ROUTER_MODEL_RECOGNITION_SERVICE";
    private static final String SERVICE_ROUTER_MODEL_RECOGINITON = "com.huawei.wifiprobqeservice.HwRouterModelRecognition";
    private static boolean mIsReceiverRegistered = false;
    private static boolean mIsWifiConnected = false;
    private static HwRouterModelRecognition routerModelInstance = null;
    private Context mContext = null;
    private String mGateway = "";
    private String mGatewayLast = "";
    private NetworkInfo mNetworkInfo = new NetworkInfo(1, 0, "WIFI", "");
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private static final /* synthetic */ int[] -android-net-NetworkInfo$DetailedStateSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$android$net$NetworkInfo$DetailedState;

        private static /* synthetic */ int[] -getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
            if (-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
                return -android-net-NetworkInfo$DetailedStateSwitchesValues;
            }
            int[] iArr = new int[DetailedState.values().length];
            try {
                iArr[DetailedState.AUTHENTICATING.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DetailedState.BLOCKED.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 5;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[DetailedState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[DetailedState.CONNECTING.ordinal()] = 6;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[DetailedState.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[DetailedState.DISCONNECTING.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[DetailedState.FAILED.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[DetailedState.IDLE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[DetailedState.OBTAINING_IPADDR.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[DetailedState.SCANNING.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[DetailedState.SUSPENDED.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            -android-net-NetworkInfo$DetailedStateSwitchesValues = iArr;
            return iArr;
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (action != null) {
                    if (action.equals("android.net.wifi.STATE_CHANGE")) {
                        HwRouterModelRecognition.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (HwRouterModelRecognition.this.mNetworkInfo != null) {
                            switch (AnonymousClass1.-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[HwRouterModelRecognition.this.mNetworkInfo.getDetailedState().ordinal()]) {
                                case 1:
                                    if (!HwRouterModelRecognition.mIsWifiConnected) {
                                        HwRouterModelRecognition.mIsWifiConnected = true;
                                        HwRouterModelRecognition.this.mSSID = HwRouterModelRecognition.this.getSsid();
                                        HwRouterModelRecognition.this.mGateway = HwRouterModelRecognition.this.getGateway();
                                        if (!(HwRouterModelRecognition.this.mSSID == null || (HwRouterModelRecognition.this.mSSID.isEmpty() ^ 1) == 0 || HwRouterModelRecognition.this.mGateway == null || (HwRouterModelRecognition.this.mGateway.isEmpty() ^ 1) == 0 || (HwRouterModelRecognition.this.mSSID.equals(HwRouterModelRecognition.this.mSSIDLast) && (HwRouterModelRecognition.this.mGateway.equals(HwRouterModelRecognition.this.mGatewayLast) ^ 1) == 0))) {
                                            HwRouterModelRecognition.this.mRouterHandler.sendEmptyMessageDelayed(1000, 300000);
                                            if (HwRouterModelRecognition.HWFLOW) {
                                                Log.d(HwRouterModelRecognition.LOG_TAG, "sendEmptyMessage MSG_START_RMC_SERVICE");
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                case 2:
                                    HwRouterModelRecognition.mIsWifiConnected = false;
                                    HwRouterModelRecognition.this.mRouterHandler.removeMessages(1000);
                                    break;
                            }
                        }
                    } else if (action.equals(HwRouterModelRecognition.ACTION_ROUTER_MODEL_RECOGNITION_RESULT)) {
                        HwRouterModelRecognition.this.mSSIDLast = HwRouterModelRecognition.this.mSSID;
                        HwRouterModelRecognition.this.mGatewayLast = HwRouterModelRecognition.this.mGateway;
                        String routerName = intent.getExtras().getString(HwRouterModelRecognition.INTENT_EXTRA_VALUE_ROUTER_NAME);
                        String routerBrand = intent.getExtras().getString(HwRouterModelRecognition.INTENT_EXTRA_VALUE_ROUTER_BRAND);
                        boolean uploadDFT = intent.getExtras().getBoolean(HwRouterModelRecognition.INTENT_EXTRA_VALUE_UPLOAD_DFT);
                        if (HwRouterModelRecognition.HWFLOW) {
                            Log.d(HwRouterModelRecognition.LOG_TAG, "received ACTION_ROUTER_MODEL_RECOGNITION_RESULT broadcast, routerName = " + routerName + ", routerBrand = " + routerBrand);
                        }
                        HwRouterModelRecognition.this.mWcsmImpl.updateRouterModelInfo(routerName, routerBrand, uploadDFT);
                        HwRouterModelRecognition.this.mRouterHandler.removeMessages(1001);
                        HwRouterModelRecognition.this.mRouterHandler.sendEmptyMessage(1001);
                    }
                }
            }
        }
    };
    RouterHandler mRouterHandler = null;
    private String mSSID = "";
    private String mSSIDLast = "";
    private Intent mServiceIntent = null;
    private WifiManager mWM = null;
    private HwWifiCHRStateManager mWcsm = null;
    private HwWifiCHRStateManagerImpl mWcsmImpl = null;

    protected class RouterHandler extends Handler {
        public RouterHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    if (HwRouterModelRecognition.HWFLOW) {
                        Log.d(HwRouterModelRecognition.LOG_TAG, "handleMessage startRmcService");
                    }
                    HwRouterModelRecognition.this.startRmcService();
                    sendEmptyMessageDelayed(1001, 60000);
                    return;
                case 1001:
                    HwRouterModelRecognition.this.stopService();
                    if (HwRouterModelRecognition.HWFLOW) {
                        Log.d(HwRouterModelRecognition.LOG_TAG, "handleMessage stopService");
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(LOG_TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public static boolean startInstance(Context context) {
        if (routerModelInstance != null) {
            return true;
        }
        if (context == null) {
            return false;
        }
        routerModelInstance = new HwRouterModelRecognition();
        return routerModelInstance.init(context);
    }

    private boolean init(Context context) {
        this.mContext = context;
        this.mWcsm = HwWifiCHRStateManagerImpl.getDefault();
        if (this.mContext == null || this.mWcsm == null || ((this.mWcsm instanceof HwWifiCHRStateManagerImpl) ^ 1) != 0) {
            Log.e(LOG_TAG, "handleMessage MSG_PROBE_WEB_RET hmWcsmImpl instanceof HwWifiCHRStateManagerImpl error");
            return false;
        }
        this.mWcsmImpl = (HwWifiCHRStateManagerImpl) this.mWcsm;
        HandlerThread ht = new HandlerThread("RouterModelRecognitionThread");
        ht.start();
        this.mRouterHandler = new RouterHandler(ht.getLooper());
        registerForBroadcasts();
        if (HWFLOW) {
            Log.d(LOG_TAG, "enter HwRouterModelRecognition");
        }
        return true;
    }

    private void registerForBroadcasts() {
        if (!mIsReceiverRegistered) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            intentFilter.addAction(ACTION_ROUTER_MODEL_RECOGNITION_RESULT);
            this.mContext.registerReceiver(this.mReceiver, intentFilter);
            mIsReceiverRegistered = true;
        }
    }

    private void stopService() {
        ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
        if (am != null) {
            am.forceStopPackage(PACKAGE_ROUTER_MODEL_RECOGINITON);
            if (HWFLOW) {
                Log.d(LOG_TAG, "force stop RouterModelRecognitionService");
            }
        } else {
            this.mContext.stopService(this.mServiceIntent);
            if (HWFLOW) {
                Log.d(LOG_TAG, "stop RouterModelRecognitionService");
            }
        }
        if (mIsReceiverRegistered) {
            this.mContext.unregisterReceiver(this.mReceiver);
            mIsReceiverRegistered = false;
        }
    }

    private String getSsid() {
        String ssid = "";
        if (this.mWM == null) {
            this.mWM = (WifiManager) this.mContext.getSystemService("wifi");
        }
        if (this.mWM == null) {
            return ssid;
        }
        WifiInfo wifiInfo = this.mWM.getConnectionInfo();
        if (wifiInfo != null) {
            return wifiInfo.getSSID();
        }
        return ssid;
    }

    private String getGateway() {
        InetAddress gateway = null;
        if (this.mWM == null) {
            this.mWM = (WifiManager) this.mContext.getSystemService("wifi");
        }
        if (this.mWM != null) {
            DhcpInfo dhcpInfo = this.mWM.getDhcpInfo();
            if (!(dhcpInfo == null || dhcpInfo.gateway == 0)) {
                gateway = NetworkUtils.intToInetAddress(dhcpInfo.gateway);
            }
        }
        if (gateway == null) {
            return null;
        }
        this.mGateway = gateway.getHostAddress();
        return this.mGateway;
    }

    private void startRmcService() {
        this.mServiceIntent = new Intent(ACTION_START_ROUTER_MODEL_RECOGINITON).setPackage(PACKAGE_ROUTER_MODEL_RECOGINITON);
        try {
            this.mContext.startService(this.mServiceIntent);
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "startRmcService() got SecurityException!");
        }
    }
}
