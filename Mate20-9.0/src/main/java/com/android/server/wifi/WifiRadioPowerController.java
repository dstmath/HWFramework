package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.HwInnerNetworkManagerImpl;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class WifiRadioPowerController {
    private static final String DEFAULT_WIFI_POWER = "false,0,0,0,0,0";
    private static final int MSG_AUDIO_STATE_CHANGE = 2;
    private static final int MSG_PHONE_STATE_CHANGE = 0;
    private static final int MSG_WIFI_AP_STATE_CHANGE = 3;
    private static final int MSG_WIFI_STATE_CHANGE = 1;
    private static final String PHONE_AUDIO_MODE = "action.huawei.PHONE_AUDIO_MODE";
    private static final String TAG = "WifiRadioPowerController";
    private static final String WIFI_REDUCE_POWER = "ro.config.wifi_reduce_power";
    private static WifiRadioPowerController instance;
    /* access modifiers changed from: private */
    public static int[] mWifiPowerLevels = new int[5];
    private BootCompleteReceiver mBootCompleteReceiver;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public HwInnerNetworkManagerImpl mINMServer;
    /* access modifiers changed from: private */
    public boolean mIsPhone1Offhook = false;
    /* access modifiers changed from: private */
    public boolean mIsPhone2Offhook = false;
    /* access modifiers changed from: private */
    public boolean mIsReceiverMode = true;
    /* access modifiers changed from: private */
    public boolean mIsWifiHotspotRedecePower = false;
    private PhoneStateListener mMSimPhoneStateListenerCard0 = null;
    private PhoneStateListener mMSimPhoneStateListenerCard1 = null;
    /* access modifiers changed from: private */
    public RadioPowerHandler mRadioPowerHandler;
    /* access modifiers changed from: private */
    public TelephonyManager mTelephonyManager;
    private HandlerThread mThread;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    /* access modifiers changed from: private */
    public WifiNative mWifiNative;
    private WifiRadioPowerReceiver mWifiRadioPowerReceiver;

    private class BootCompleteReceiver extends BroadcastReceiver {
        private BootCompleteReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                TelephonyManager unused = WifiRadioPowerController.this.mTelephonyManager = (TelephonyManager) WifiRadioPowerController.this.mContext.getSystemService("phone");
                WifiRadioPowerController.this.startMonitoring(context);
            }
        }
    }

    private class RadioPowerHandler extends Handler {
        private int mPrevPower = 100;

        RadioPowerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                case 1:
                case 2:
                case 3:
                    checkCurrentState();
                    return;
                default:
                    return;
            }
        }

        private void checkCurrentState() {
            WifiManager unused = WifiRadioPowerController.this.mWifiManager = (WifiManager) WifiRadioPowerController.this.mContext.getApplicationContext().getSystemService("wifi");
            boolean isWifiEnable = WifiRadioPowerController.this.mWifiManager.isWifiEnabled();
            boolean isWifiApEnabled = WifiRadioPowerController.this.mWifiManager.isWifiApEnabled();
            Log.d(WifiRadioPowerController.TAG, "isWifiEnable = " + isWifiEnable + " mIsReceiverMode = " + WifiRadioPowerController.this.mIsReceiverMode + " mIsPhone1Offhook = " + WifiRadioPowerController.this.mIsPhone1Offhook + " mIsPhone2Offhook = " + WifiRadioPowerController.this.mIsPhone2Offhook + " isWifiApEnabled = " + isWifiApEnabled);
            if ((isWifiEnable || isWifiApEnabled) && WifiRadioPowerController.this.mIsReceiverMode) {
                if (WifiRadioPowerController.this.mIsPhone1Offhook && WifiRadioPowerController.this.mIsPhone2Offhook) {
                    sendCommand(WifiRadioPowerController.mWifiPowerLevels[2]);
                } else if (WifiRadioPowerController.this.mIsPhone1Offhook || WifiRadioPowerController.this.mIsPhone2Offhook) {
                    if (WifiRadioPowerController.this.mIsWifiHotspotRedecePower) {
                        sendCommand(WifiRadioPowerController.mWifiPowerLevels[4]);
                    } else {
                        sendCommand(WifiRadioPowerController.mWifiPowerLevels[1]);
                    }
                } else if (WifiRadioPowerController.this.mIsWifiHotspotRedecePower) {
                    sendCommand(WifiRadioPowerController.mWifiPowerLevels[3]);
                } else {
                    sendCommand(WifiRadioPowerController.mWifiPowerLevels[0]);
                }
            } else if (WifiRadioPowerController.this.mIsWifiHotspotRedecePower) {
                sendCommand(WifiRadioPowerController.mWifiPowerLevels[3]);
            } else {
                sendCommand(WifiRadioPowerController.mWifiPowerLevels[0]);
            }
        }

        private void sendCommand(int level) {
            String wifiPower = "TX_POWER " + level;
            if (!WifiRadioPowerController.this.mIsWifiHotspotRedecePower) {
                Log.d(WifiRadioPowerController.TAG, "WifiRadioPowerController setWifiPowerCommand wpa mode: " + wifiPower);
                WifiRadioPowerController.this.mWifiNative.sendWifiPowerCommand(level);
            } else if (this.mPrevPower == level) {
                Log.d(WifiRadioPowerController.TAG, "WifiRadioPowerController setWifiPowerCommand mPrevPower == level: " + wifiPower);
            } else {
                try {
                    Log.d(WifiRadioPowerController.TAG, "WifiRadioPowerController setWifiPowerCommand netd mode: " + wifiPower);
                    WifiRadioPowerController.this.mINMServer.setWifiTxPower(wifiPower);
                    this.mPrevPower = level;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class WifiRadioPowerReceiver extends BroadcastReceiver {
        private WifiRadioPowerReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                WifiRadioPowerController.this.handleWifiStateChange(intent);
            } else if (WifiRadioPowerController.PHONE_AUDIO_MODE.equals(action)) {
                WifiRadioPowerController.this.handleReceiveModeChange(intent);
            } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                WifiRadioPowerController.this.handleWifiStateChange(intent);
            }
        }
    }

    private WifiRadioPowerController(Context context, WifiStateMachine wsm, WifiNative wifiNative, HwInnerNetworkManagerImpl nmServer) {
        this.mINMServer = nmServer;
        this.mContext = context;
        this.mWifiNative = wifiNative;
        initPowerValue();
        this.mThread = new HandlerThread("RadioPowerThread");
        this.mThread.start();
        this.mRadioPowerHandler = new RadioPowerHandler(this.mThread.getLooper());
        registerBootComplete();
    }

    public static synchronized void setInstance(Context context, WifiStateMachine wsm, WifiNative wifiNative, HwInnerNetworkManagerImpl nmServer) {
        synchronized (WifiRadioPowerController.class) {
            if (instance == null) {
                instance = new WifiRadioPowerController(context, wsm, wifiNative, nmServer);
            }
        }
    }

    public static boolean isRadioPowerEnabled() {
        String[] wifiPowerLevels = SystemProperties.get(WIFI_REDUCE_POWER, DEFAULT_WIFI_POWER).split(",");
        if (wifiPowerLevels.length == 4) {
            if ("true".equals(wifiPowerLevels[0])) {
                Log.d(TAG, "RadioPowerEnabled");
                return true;
            }
        } else if (wifiPowerLevels.length == 6 && "true".equals(wifiPowerLevels[0])) {
            Log.d(TAG, "WifiAPRadioPowerEnabled");
            return true;
        }
        Log.d(TAG, "RadioPower disabled");
        return false;
    }

    private void initPowerValue() {
        String[] wifiPowerLevels = SystemProperties.get(WIFI_REDUCE_POWER, DEFAULT_WIFI_POWER).split(",");
        if (wifiPowerLevels.length == 4) {
            int i = 1;
            while (i < 4) {
                try {
                    mWifiPowerLevels[i - 1] = Integer.parseInt(wifiPowerLevels[i]);
                    Log.d(TAG, "WifiPowerLevels P" + i + " value:" + mWifiPowerLevels[i - 1]);
                    i++;
                } catch (NumberFormatException e) {
                    mWifiPowerLevels[0] = 15;
                    mWifiPowerLevels[1] = 14;
                    mWifiPowerLevels[2] = 13;
                    return;
                }
            }
        } else if (wifiPowerLevels.length == 6) {
            this.mIsWifiHotspotRedecePower = true;
            int i2 = 4;
            while (i2 < 6) {
                try {
                    mWifiPowerLevels[i2 - 1] = Integer.parseInt(wifiPowerLevels[i2]);
                    Log.d(TAG, "WifiSta&APPowerLevels P" + i2 + " value:" + mWifiPowerLevels[i2 - 1]);
                    i2++;
                } catch (NumberFormatException e2) {
                    mWifiPowerLevels[3] = 18;
                    mWifiPowerLevels[4] = 12;
                    return;
                }
            }
        } else {
            mWifiPowerLevels[0] = 15;
            mWifiPowerLevels[1] = 14;
            mWifiPowerLevels[2] = 13;
            mWifiPowerLevels[3] = 18;
            mWifiPowerLevels[4] = 12;
        }
    }

    private void registerCallStateListener() {
        if (!isMultiSimEnabled()) {
            if (this.mMSimPhoneStateListenerCard0 == null) {
                this.mMSimPhoneStateListenerCard0 = getPhoneStateListener(0);
            }
            this.mTelephonyManager.listen(this.mMSimPhoneStateListenerCard0, 32);
            return;
        }
        if (this.mMSimPhoneStateListenerCard0 == null) {
            this.mMSimPhoneStateListenerCard0 = getPhoneStateListener(0);
        }
        if (this.mMSimPhoneStateListenerCard1 == null) {
            this.mMSimPhoneStateListenerCard1 = getPhoneStateListener(1);
        }
        this.mTelephonyManager.listen(this.mMSimPhoneStateListenerCard0, 32);
        this.mTelephonyManager.listen(this.mMSimPhoneStateListenerCard1, 32);
    }

    private PhoneStateListener getPhoneStateListener(int subscription) {
        final int mSubscription = subscription;
        return new PhoneStateListener(Integer.valueOf(subscription)) {
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == 0) {
                    if (mSubscription == 0) {
                        boolean unused = WifiRadioPowerController.this.mIsPhone1Offhook = false;
                    } else if (mSubscription == 1) {
                        boolean unused2 = WifiRadioPowerController.this.mIsPhone2Offhook = false;
                    }
                    WifiRadioPowerController.this.mRadioPowerHandler.sendMessage(WifiRadioPowerController.this.mRadioPowerHandler.obtainMessage(0));
                } else if (state == 2) {
                    if (mSubscription == 0) {
                        boolean unused3 = WifiRadioPowerController.this.mIsPhone1Offhook = true;
                    } else if (mSubscription == 1) {
                        boolean unused4 = WifiRadioPowerController.this.mIsPhone2Offhook = true;
                    }
                    WifiRadioPowerController.this.mRadioPowerHandler.sendMessage(WifiRadioPowerController.this.mRadioPowerHandler.obtainMessage(0));
                }
                super.onCallStateChanged(state, incomingNumber);
            }
        };
    }

    private static boolean isMultiSimEnabled() {
        try {
            return TelephonyManager.getDefault().isMultiSimEnabled();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void registerBootComplete() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mBootCompleteReceiver = new BootCompleteReceiver();
        this.mContext.registerReceiver(this.mBootCompleteReceiver, filter);
    }

    /* access modifiers changed from: private */
    public void startMonitoring(Context context) {
        registerCallStateListener();
        this.mWifiRadioPowerReceiver = new WifiRadioPowerReceiver();
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        if (this.mIsWifiHotspotRedecePower) {
            wifiFilter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        }
        this.mContext.registerReceiver(this.mWifiRadioPowerReceiver, wifiFilter);
        IntentFilter phoneFilter = new IntentFilter();
        phoneFilter.addAction(PHONE_AUDIO_MODE);
        this.mContext.registerReceiver(this.mWifiRadioPowerReceiver, phoneFilter, "com.huawei.wifi.permission.PHONE_AUDIO_MODE", null);
    }

    /* access modifiers changed from: private */
    public void handleWifiStateChange(Intent intent) {
        if (intent.getIntExtra("wifi_state", 4) == 3) {
            this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(1));
        }
        if (this.mIsWifiHotspotRedecePower && intent.getIntExtra("wifi_state", 4) == 13) {
            this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(3));
        }
    }

    /* access modifiers changed from: private */
    public void handleReceiveModeChange(Intent intent) {
        int state = intent.getIntExtra("audio_state", -1);
        Log.d(TAG, "state = " + state);
        if (state != 1) {
            this.mIsReceiverMode = false;
            this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(2));
            return;
        }
        this.mIsReceiverMode = true;
        this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(2));
    }
}
