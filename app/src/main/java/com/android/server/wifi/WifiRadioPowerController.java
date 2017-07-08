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
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;

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
    private static int[] mWifiPowerLevels;
    private BootCompleteReceiver mBootCompleteReceiver;
    private Context mContext;
    private HwInnerNetworkManagerImpl mINMServer;
    private boolean mIsPhone1Offhook;
    private boolean mIsPhone2Offhook;
    private boolean mIsReceiverMode;
    private boolean mIsWifiHotspotRedecePower;
    private PhoneStateListener mMSimPhoneStateListenerCard0;
    private PhoneStateListener mMSimPhoneStateListenerCard1;
    private RadioPowerHandler mRadioPowerHandler;
    private TelephonyManager mTelephonyManager;
    private HandlerThread mThread;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;
    private WifiRadioPowerReceiver mWifiRadioPowerReceiver;

    /* renamed from: com.android.server.wifi.WifiRadioPowerController.1 */
    class AnonymousClass1 extends PhoneStateListener {
        final /* synthetic */ int val$mSubscription;

        AnonymousClass1(int $anonymous0, int val$mSubscription) {
            this.val$mSubscription = val$mSubscription;
            super($anonymous0);
        }

        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case WifiRadioPowerController.MSG_PHONE_STATE_CHANGE /*0*/:
                    if (this.val$mSubscription == 0) {
                        WifiRadioPowerController.this.mIsPhone1Offhook = false;
                    } else if (this.val$mSubscription == WifiRadioPowerController.MSG_WIFI_STATE_CHANGE) {
                        WifiRadioPowerController.this.mIsPhone2Offhook = false;
                    }
                    WifiRadioPowerController.this.mRadioPowerHandler.sendMessage(WifiRadioPowerController.this.mRadioPowerHandler.obtainMessage(WifiRadioPowerController.MSG_PHONE_STATE_CHANGE));
                    break;
                case WifiRadioPowerController.MSG_AUDIO_STATE_CHANGE /*2*/:
                    if (this.val$mSubscription == 0) {
                        WifiRadioPowerController.this.mIsPhone1Offhook = true;
                    } else if (this.val$mSubscription == WifiRadioPowerController.MSG_WIFI_STATE_CHANGE) {
                        WifiRadioPowerController.this.mIsPhone2Offhook = true;
                    }
                    WifiRadioPowerController.this.mRadioPowerHandler.sendMessage(WifiRadioPowerController.this.mRadioPowerHandler.obtainMessage(WifiRadioPowerController.MSG_PHONE_STATE_CHANGE));
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }

    private class BootCompleteReceiver extends BroadcastReceiver {
        private BootCompleteReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                WifiRadioPowerController.this.mTelephonyManager = (TelephonyManager) WifiRadioPowerController.this.mContext.getSystemService("phone");
                WifiRadioPowerController.this.startMonitoring(context);
            }
        }
    }

    private class RadioPowerHandler extends Handler {
        private int mPrevPower;

        RadioPowerHandler(Looper looper) {
            super(looper);
            this.mPrevPower = 100;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WifiRadioPowerController.MSG_PHONE_STATE_CHANGE /*0*/:
                case WifiRadioPowerController.MSG_WIFI_STATE_CHANGE /*1*/:
                case WifiRadioPowerController.MSG_AUDIO_STATE_CHANGE /*2*/:
                case WifiRadioPowerController.MSG_WIFI_AP_STATE_CHANGE /*3*/:
                    checkCurrentState();
                default:
            }
        }

        private void checkCurrentState() {
            WifiRadioPowerController.this.mWifiManager = (WifiManager) WifiRadioPowerController.this.mContext.getApplicationContext().getSystemService("wifi");
            boolean isWifiEnable = WifiRadioPowerController.this.mWifiManager.isWifiEnabled();
            boolean isWifiApEnabled = WifiRadioPowerController.this.mWifiManager.isWifiApEnabled();
            Log.d(WifiRadioPowerController.TAG, "isWifiEnable = " + isWifiEnable + " mIsReceiverMode = " + WifiRadioPowerController.this.mIsReceiverMode + " mIsPhone1Offhook = " + WifiRadioPowerController.this.mIsPhone1Offhook + " mIsPhone2Offhook = " + WifiRadioPowerController.this.mIsPhone2Offhook + " isWifiApEnabled = " + isWifiApEnabled);
            if ((isWifiEnable || isWifiApEnabled) && WifiRadioPowerController.this.mIsReceiverMode) {
                if (WifiRadioPowerController.this.mIsPhone1Offhook && WifiRadioPowerController.this.mIsPhone2Offhook) {
                    sendCommand(WifiRadioPowerController.mWifiPowerLevels[WifiRadioPowerController.MSG_AUDIO_STATE_CHANGE]);
                } else if (WifiRadioPowerController.this.mIsPhone1Offhook || WifiRadioPowerController.this.mIsPhone2Offhook) {
                    if (WifiRadioPowerController.this.mIsWifiHotspotRedecePower) {
                        sendCommand(WifiRadioPowerController.mWifiPowerLevels[4]);
                    } else {
                        sendCommand(WifiRadioPowerController.mWifiPowerLevels[WifiRadioPowerController.MSG_WIFI_STATE_CHANGE]);
                    }
                } else if (WifiRadioPowerController.this.mIsWifiHotspotRedecePower) {
                    sendCommand(WifiRadioPowerController.mWifiPowerLevels[WifiRadioPowerController.MSG_WIFI_AP_STATE_CHANGE]);
                } else {
                    sendCommand(WifiRadioPowerController.mWifiPowerLevels[WifiRadioPowerController.MSG_PHONE_STATE_CHANGE]);
                }
            } else if (WifiRadioPowerController.this.mIsWifiHotspotRedecePower) {
                sendCommand(WifiRadioPowerController.mWifiPowerLevels[WifiRadioPowerController.MSG_WIFI_AP_STATE_CHANGE]);
            } else {
                sendCommand(WifiRadioPowerController.mWifiPowerLevels[WifiRadioPowerController.MSG_PHONE_STATE_CHANGE]);
            }
        }

        private void sendCommand(int level) {
            String wifiPower = "TX_POWER " + level;
            if (!WifiRadioPowerController.this.mIsWifiHotspotRedecePower) {
                Log.d(WifiRadioPowerController.TAG, "WifiRadioPowerController setWifiPowerCommand wpa mode: " + wifiPower);
                WifiRadioPowerController.this.mWifiNative.sendWifiPowerCommand(wifiPower);
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.WifiRadioPowerController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.WifiRadioPowerController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.WifiRadioPowerController.<clinit>():void");
    }

    private WifiRadioPowerController(Context context, WifiStateMachine wsm, WifiNative wifiNative, HwInnerNetworkManagerImpl nmServer) {
        this.mIsPhone1Offhook = false;
        this.mIsPhone2Offhook = false;
        this.mIsReceiverMode = true;
        this.mIsWifiHotspotRedecePower = false;
        this.mMSimPhoneStateListenerCard0 = null;
        this.mMSimPhoneStateListenerCard1 = null;
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
            if ("true".equals(wifiPowerLevels[MSG_PHONE_STATE_CHANGE])) {
                Log.d(TAG, "RadioPowerEnabled");
                return true;
            }
        } else if (wifiPowerLevels.length == 6 && "true".equals(wifiPowerLevels[MSG_PHONE_STATE_CHANGE])) {
            Log.d(TAG, "WifiAPRadioPowerEnabled");
            return true;
        }
        Log.d(TAG, "RadioPower disabled");
        return false;
    }

    private void initPowerValue() {
        String[] wifiPowerLevels = SystemProperties.get(WIFI_REDUCE_POWER, DEFAULT_WIFI_POWER).split(",");
        int i;
        if (wifiPowerLevels.length == 4) {
            i = MSG_WIFI_STATE_CHANGE;
            while (i < 4) {
                try {
                    mWifiPowerLevels[i - 1] = Integer.parseInt(wifiPowerLevels[i]);
                    Log.d(TAG, "WifiPowerLevels P" + i + " value:" + mWifiPowerLevels[i - 1]);
                    i += MSG_WIFI_STATE_CHANGE;
                } catch (NumberFormatException e) {
                    mWifiPowerLevels[MSG_PHONE_STATE_CHANGE] = 15;
                    mWifiPowerLevels[MSG_WIFI_STATE_CHANGE] = 14;
                    mWifiPowerLevels[MSG_AUDIO_STATE_CHANGE] = 13;
                    return;
                }
            }
        } else if (wifiPowerLevels.length == 6) {
            this.mIsWifiHotspotRedecePower = true;
            i = 4;
            while (i < 6) {
                try {
                    mWifiPowerLevels[i - 1] = Integer.parseInt(wifiPowerLevels[i]);
                    Log.d(TAG, "WifiSta&APPowerLevels P" + i + " value:" + mWifiPowerLevels[i - 1]);
                    i += MSG_WIFI_STATE_CHANGE;
                } catch (NumberFormatException e2) {
                    mWifiPowerLevels[MSG_WIFI_AP_STATE_CHANGE] = 18;
                    mWifiPowerLevels[4] = 12;
                    return;
                }
            }
        } else {
            mWifiPowerLevels[MSG_PHONE_STATE_CHANGE] = 15;
            mWifiPowerLevels[MSG_WIFI_STATE_CHANGE] = 14;
            mWifiPowerLevels[MSG_AUDIO_STATE_CHANGE] = 13;
            mWifiPowerLevels[MSG_WIFI_AP_STATE_CHANGE] = 18;
            mWifiPowerLevels[4] = 12;
        }
    }

    private void registerCallStateListener() {
        if (isMultiSimEnabled()) {
            if (this.mMSimPhoneStateListenerCard0 == null) {
                this.mMSimPhoneStateListenerCard0 = getPhoneStateListener(MSG_PHONE_STATE_CHANGE);
            }
            if (this.mMSimPhoneStateListenerCard1 == null) {
                this.mMSimPhoneStateListenerCard1 = getPhoneStateListener(MSG_WIFI_STATE_CHANGE);
            }
            this.mTelephonyManager.listen(this.mMSimPhoneStateListenerCard0, 32);
            this.mTelephonyManager.listen(this.mMSimPhoneStateListenerCard1, 32);
            return;
        }
        if (this.mMSimPhoneStateListenerCard0 == null) {
            this.mMSimPhoneStateListenerCard0 = getPhoneStateListener(MSG_PHONE_STATE_CHANGE);
        }
        this.mTelephonyManager.listen(this.mMSimPhoneStateListenerCard0, 32);
    }

    private PhoneStateListener getPhoneStateListener(int subscription) {
        int mSubscription = subscription;
        return new AnonymousClass1(subscription, subscription);
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

    private void handleWifiStateChange(Intent intent) {
        switch (intent.getIntExtra("wifi_state", 4)) {
            case MSG_WIFI_AP_STATE_CHANGE /*3*/:
                this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(MSG_WIFI_STATE_CHANGE));
                break;
        }
        if (this.mIsWifiHotspotRedecePower) {
            switch (intent.getIntExtra("wifi_state", 4)) {
                case MessageUtil.MSG_WIFI_IS_PORTAL /*13*/:
                    this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(MSG_WIFI_AP_STATE_CHANGE));
                default:
            }
        }
    }

    private void handleReceiveModeChange(Intent intent) {
        int state = intent.getIntExtra("audio_state", -1);
        Log.d(TAG, "state = " + state);
        switch (state) {
            case MSG_WIFI_STATE_CHANGE /*1*/:
                this.mIsReceiverMode = true;
                this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(MSG_AUDIO_STATE_CHANGE));
            default:
                this.mIsReceiverMode = false;
                this.mRadioPowerHandler.sendMessage(this.mRadioPowerHandler.obtainMessage(MSG_AUDIO_STATE_CHANGE));
        }
    }
}
