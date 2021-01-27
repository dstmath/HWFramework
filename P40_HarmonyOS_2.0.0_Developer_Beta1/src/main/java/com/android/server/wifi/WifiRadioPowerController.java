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
import android.util.wifi.HwHiLog;

public class WifiRadioPowerController {
    private static final String DEFAULT_WIFI_POWER = "false,0,0,0,0,0";
    private static final int MSG_AUDIO_STATE_CHANGE = 2;
    private static final int MSG_PHONE_STATE_CHANGE = 0;
    private static final int MSG_WIFI_AP_STATE_CHANGE = 3;
    private static final int MSG_WIFI_STATE_CHANGE = 1;
    private static final String PHONE_AUDIO_MODE = "action.huawei.PHONE_AUDIO_MODE";
    private static final String TAG = "WifiRadioPowerController";
    private static final int WIFIPOWERLEVEL_LENGTH_AP = 6;
    private static final int WIFIPOWERLEVEL_LENGTH_STA = 4;
    private static final String WIFI_REDUCE_POWER = "ro.config.wifi_reduce_power";
    private static WifiRadioPowerController instance;
    private static int[] mWifiPowerLevels = new int[5];
    private BootCompleteReceiver mBootCompleteReceiver;
    private Context mContext;
    private HwInnerNetworkManagerImpl mINMServer;
    private boolean mIsPhone1Offhook = false;
    private boolean mIsPhone2Offhook = false;
    private boolean mIsReceiverMode = true;
    private boolean mIsWifiHotspotRedecePower = false;
    private PhoneStateListener mMSimPhoneStateListenerCard0 = null;
    private PhoneStateListener mMSimPhoneStateListenerCard1 = null;
    private RadioPowerHandler mRadioPowerHandler;
    private TelephonyManager mTelephonyManager;
    private HandlerThread mThread;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;
    private WifiRadioPowerReceiver mWifiRadioPowerReceiver;

    private WifiRadioPowerController(Context context, ClientModeImpl wsm, WifiNative wifiNative, HwInnerNetworkManagerImpl nmServer) {
        this.mINMServer = nmServer;
        this.mContext = context;
        this.mWifiNative = wifiNative;
        initPowerValue();
        this.mThread = new HandlerThread("RadioPowerThread");
        this.mThread.start();
        this.mRadioPowerHandler = new RadioPowerHandler(this.mThread.getLooper());
        registerBootComplete();
    }

    public static synchronized void setInstance(Context context, ClientModeImpl wsm, WifiNative wifiNative, HwInnerNetworkManagerImpl nmServer) {
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
                HwHiLog.d(TAG, false, "RadioPowerEnabled", new Object[0]);
                return true;
            }
        } else if (wifiPowerLevels.length == 6 && "true".equals(wifiPowerLevels[0])) {
            HwHiLog.d(TAG, false, "WifiAPRadioPowerEnabled", new Object[0]);
            return true;
        }
        HwHiLog.d(TAG, false, "RadioPower disabled", new Object[0]);
        return false;
    }

    private void initPowerValue() {
        String[] wifiPowerLevels = SystemProperties.get(WIFI_REDUCE_POWER, DEFAULT_WIFI_POWER).split(",");
        if (wifiPowerLevels.length == 4) {
            for (int i = 1; i < 4; i++) {
                try {
                    mWifiPowerLevels[i - 1] = Integer.parseInt(wifiPowerLevels[i]);
                    HwHiLog.d(TAG, false, "WifiPowerLevels P %{public}d value:%{public}d", new Object[]{Integer.valueOf(i), Integer.valueOf(mWifiPowerLevels[i - 1])});
                } catch (NumberFormatException e) {
                    int[] iArr = mWifiPowerLevels;
                    iArr[0] = 15;
                    iArr[1] = 14;
                    iArr[2] = 13;
                    return;
                }
            }
        } else if (wifiPowerLevels.length == 6) {
            this.mIsWifiHotspotRedecePower = true;
            for (int i2 = 4; i2 < 6; i2++) {
                try {
                    mWifiPowerLevels[i2 - 1] = Integer.parseInt(wifiPowerLevels[i2]);
                    HwHiLog.d(TAG, false, "WifiSta&APPowerLevels P %{public}d value:%{public}d", new Object[]{Integer.valueOf(i2), Integer.valueOf(mWifiPowerLevels[i2 - 1])});
                } catch (NumberFormatException e2) {
                    int[] iArr2 = mWifiPowerLevels;
                    iArr2[3] = 18;
                    iArr2[4] = 12;
                    return;
                }
            }
        } else {
            int[] iArr3 = mWifiPowerLevels;
            iArr3[0] = 15;
            iArr3[1] = 14;
            iArr3[2] = 13;
            iArr3[3] = 18;
            iArr3[4] = 12;
        }
    }

    /* access modifiers changed from: private */
    public class BootCompleteReceiver extends BroadcastReceiver {
        private BootCompleteReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                WifiRadioPowerController wifiRadioPowerController = WifiRadioPowerController.this;
                wifiRadioPowerController.mTelephonyManager = (TelephonyManager) wifiRadioPowerController.mContext.getSystemService("phone");
                WifiRadioPowerController.this.startMonitoring(context);
            }
        }
    }

    /* access modifiers changed from: private */
    public class WifiRadioPowerReceiver extends BroadcastReceiver {
        private WifiRadioPowerReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
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
    }

    /* access modifiers changed from: private */
    public class RadioPowerHandler extends Handler {
        private int mPrevPower = 100;

        RadioPowerHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0 || i == 1 || i == 2 || i == 3) {
                checkCurrentState();
            }
        }

        private void checkCurrentState() {
            WifiRadioPowerController wifiRadioPowerController = WifiRadioPowerController.this;
            wifiRadioPowerController.mWifiManager = (WifiManager) wifiRadioPowerController.mContext.getApplicationContext().getSystemService("wifi");
            boolean isWifiEnable = WifiRadioPowerController.this.mWifiManager.isWifiEnabled();
            boolean isWifiApEnabled = WifiRadioPowerController.this.mWifiManager.isWifiApEnabled();
            HwHiLog.d(WifiRadioPowerController.TAG, false, "isWifiEnable = %{public}s mIsReceiverMode = %{public}s mIsPhone1Offhook = %{public}s mIsPhone2Offhook = %{public}s isWifiApEnabled = %{public}s", new Object[]{String.valueOf(isWifiEnable), String.valueOf(WifiRadioPowerController.this.mIsReceiverMode), String.valueOf(WifiRadioPowerController.this.mIsPhone1Offhook), String.valueOf(WifiRadioPowerController.this.mIsPhone2Offhook), String.valueOf(isWifiApEnabled)});
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
                HwHiLog.d(WifiRadioPowerController.TAG, false, "WifiRadioPowerController setWifiPowerCommand wpa mode: %{public}s", new Object[]{wifiPower});
                WifiRadioPowerController.this.mWifiNative.mHwWifiNativeEx.sendWifiPowerCommand(level);
            } else if (this.mPrevPower == level) {
                HwHiLog.d(WifiRadioPowerController.TAG, false, "WifiRadioPowerController setWifiPowerCommand mPrevPower == level: %{public}s", new Object[]{wifiPower});
            } else {
                try {
                    HwHiLog.d(WifiRadioPowerController.TAG, false, "WifiRadioPowerController setWifiPowerCommand netd mode: %{public}s", new Object[]{wifiPower});
                    WifiRadioPowerController.this.mINMServer.setWifiTxPower(wifiPower);
                    this.mPrevPower = level;
                } catch (Exception e) {
                    HwHiLog.e(WifiRadioPowerController.TAG, false, "WifiRadioPowerController setWifiPowerCommand failed", new Object[0]);
                }
            }
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

    private PhoneStateListener getPhoneStateListener(final int subscription) {
        return new PhoneStateListener(Integer.valueOf(subscription)) {
            /* class com.android.server.wifi.WifiRadioPowerController.AnonymousClass1 */

            @Override // android.telephony.PhoneStateListener
            public void onCallStateChanged(int state, String incomingNumber) {
                if (state == 0) {
                    int i = subscription;
                    if (i == 0) {
                        WifiRadioPowerController.this.mIsPhone1Offhook = false;
                    } else if (i == 1) {
                        WifiRadioPowerController.this.mIsPhone2Offhook = false;
                    }
                    WifiRadioPowerController.this.mRadioPowerHandler.sendMessage(WifiRadioPowerController.this.mRadioPowerHandler.obtainMessage(0));
                } else if (state == 2) {
                    int i2 = subscription;
                    if (i2 == 0) {
                        WifiRadioPowerController.this.mIsPhone1Offhook = true;
                    } else if (i2 == 1) {
                        WifiRadioPowerController.this.mIsPhone2Offhook = true;
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
            HwHiLog.e(TAG, false, "isMultiSimEnabled failed", new Object[0]);
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
    /* access modifiers changed from: public */
    private void startMonitoring(Context context) {
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
    /* access modifiers changed from: public */
    private void handleWifiStateChange(Intent intent) {
        if (intent.getIntExtra("wifi_state", 4) == 3) {
            this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(1));
        }
        if (this.mIsWifiHotspotRedecePower && intent.getIntExtra("wifi_state", 4) == 13) {
            this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(3));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReceiveModeChange(Intent intent) {
        int state = intent.getIntExtra("audio_state", -1);
        HwHiLog.d(TAG, false, "state = %{public}d", new Object[]{Integer.valueOf(state)});
        if (state != 1) {
            this.mIsReceiverMode = false;
            this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(2));
            return;
        }
        this.mIsReceiverMode = true;
        this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(2));
    }
}
