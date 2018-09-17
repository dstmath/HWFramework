package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.GroupInfoListener;
import android.net.wifi.p2p.WifiP2pManagerHisiExt;
import android.os.AsyncResult;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.server.wifi.wifipro.hwintelligencewifi.MessageUtil;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieController;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class IsmCoexWifiStateTrack implements HwIsmCoexWifiStateTrack {
    private static final /* synthetic */ int[] -android-net-wifi-SupplicantStateSwitchesValues = null;
    private static final String ATCOMMAND_PATH = "android.telephony.HwTelephonyManagerInner";
    private static final int AT_COEX_PRAM_NUM = 6;
    private static final String[] AT_COMMAND;
    private static final String[] AT_COMMAND_HI110X;
    private static final int[] CHNANNEL;
    private static final int COEX_CONFIG_FREQ = 0;
    private static final int COEX_CONFIG_SIMPLE = 1;
    private static final int COEX_PRAM_NUM = 6;
    private static final boolean DEBUG = true;
    private static final String ISM_COEX_CONFIG = "ro.config.hw_ismcoexconfig";
    private static final String ISM_COEX_ON = "ro.config.hw_ismcoex";
    private static final String TAG = "IsmCoexWifiStateTrack";
    private static final String WIFI_LTE_CONFIG_FILE;
    private static final int WIFI_NOT_CONNECT_INDEX = 15;
    private static final String WIFI_NOT_WORK = "0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0";
    private static final int WIFI_NOT_WORK_INDEX = 15;
    private static final int WIFI_SCAN = 14;
    private static final boolean mIs110xConnectivity;
    private static final boolean mIsHisiConnectivity;
    private int mAtCommandIndex;
    private AtomicBoolean mAtCommandInstall;
    private String[] mAtCommandLoad;
    private String[] mAtCommandLoadHi110x;
    private Method mCallATCommand;
    private int mChnlIndex;
    private boolean mClassExsit;
    private Context mContext;
    private Method mGetInstance;
    private GroupInfoListener mGroupInfoListener;
    private boolean mIsLteWork;
    private IsmCoexHandler mIsmCoexHandler;
    private int[] mLteFreqCoex;
    private boolean mMethodExsit;
    private Channel mP2pChannel;
    private AtomicBoolean mP2pConnected;
    private AtomicBoolean mP2pDiscovery;
    private int mP2pFrequency;
    private AtomicBoolean mP2pState;
    private WifiP2pDeviceList mPeers;
    private TelephonyManager mPhone;
    PhoneStateListener mPhoneStateListener;
    private Class<?> mReflect;
    private String mSendString;
    private AtomicBoolean mWifiConnect;
    private int mWifiFrequence;
    private WifiNative mWifiNative;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManagerHisiExt mWifiP2pManagerHisiExt;
    private AtomicBoolean mWifiScaning;
    private AtomicBoolean mWifiState;
    private WifiStateMachine mWifiStateMachine;
    private BroadcastReceiver mWifiStateReceiver;
    private boolean mWlanCoexIsOn;

    private class IsmCoexHandler extends Handler {
        private static final int MSG_LTE_FREQ_COEX_CHANGED = 2;
        private static final int MSG_SEND_AT_COMMAND = 0;
        private static final int MSG_SET_ISMCOEX_MODE = 1;
        private static final boolean SET_ISMCOEX_MODE_OFF = false;
        private static final boolean SET_ISMCOEX_MODE_ON = true;

        IsmCoexHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SEND_AT_COMMAND /*0*/:
                    if (IsmCoexWifiStateTrack.isHisiConnectivity()) {
                        IsmCoexWifiStateTrack.this.sendAtCommandHi110x(((Boolean) msg.obj).booleanValue());
                    } else {
                        IsmCoexWifiStateTrack.this.sendAtCommand();
                    }
                case MSG_SET_ISMCOEX_MODE /*1*/:
                    IsmCoexWifiStateTrack.logd("MSG_SET_ISMCOEX_MODE token:" + ((Boolean) msg.obj));
                    IsmCoexWifiStateTrack.this.mWifiNative.setIsmcoexMode(((Boolean) msg.obj).booleanValue());
                case MSG_LTE_FREQ_COEX_CHANGED /*2*/:
                    IsmCoexWifiStateTrack.logd("MSG_LTE_FREQ_COEX_CHANGED:");
                    IsmCoexWifiStateTrack.this.onLteFreqCoexChanged((AsyncResult) msg.obj);
                default:
            }
        }
    }

    private class WifiStateReceiver extends BroadcastReceiver {
        private WifiStateReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            IsmCoexWifiStateTrack.logd("onReceive:" + action);
            if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                IsmCoexWifiStateTrack.this.handleWifiStateChange(intent);
            } else if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action)) {
                IsmCoexWifiStateTrack.this.handleSupplicantStateChange(intent);
            } else if ("android.net.wifi.p2p.STATE_CHANGED".equals(action)) {
                IsmCoexWifiStateTrack.this.handleP2pState(intent);
            } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                IsmCoexWifiStateTrack.this.handleP2pConnectState(intent);
            } else if ("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE".equals(action)) {
                IsmCoexWifiStateTrack.this.handleP2pDiscoveryChange(intent);
            } else if ("android.net.wifi.p2p.PEERS_CHANGED".equals(action)) {
                IsmCoexWifiStateTrack.this.mPeers = (WifiP2pDeviceList) intent.getParcelableExtra("wifiP2pDeviceList");
                IsmCoexWifiStateTrack.this.handlePeersChanged(intent);
            }
            if (IsmCoexWifiStateTrack.isHisiConnectivity()) {
                int chnlid = IsmCoexWifiStateTrack.this.getLocalCoexChnlId();
                if (chnlid != IsmCoexWifiStateTrack.this.mChnlIndex) {
                    IsmCoexWifiStateTrack.this.mChnlIndex = chnlid;
                    IsmCoexWifiStateTrack.this.processLteWithWlanCoex();
                }
            } else if (IsmCoexWifiStateTrack.this.mIsmCoexHandler != null) {
                IsmCoexWifiStateTrack.this.mIsmCoexHandler.sendEmptyMessage(IsmCoexWifiStateTrack.COEX_CONFIG_FREQ);
            }
        }
    }

    private static /* synthetic */ int[] -getandroid-net-wifi-SupplicantStateSwitchesValues() {
        if (-android-net-wifi-SupplicantStateSwitchesValues != null) {
            return -android-net-wifi-SupplicantStateSwitchesValues;
        }
        int[] iArr = new int[SupplicantState.values().length];
        try {
            iArr[SupplicantState.ASSOCIATED.ordinal()] = COEX_CONFIG_SIMPLE;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[SupplicantState.ASSOCIATING.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[SupplicantState.AUTHENTICATING.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[SupplicantState.COMPLETED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[SupplicantState.DISCONNECTED.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[SupplicantState.DORMANT.ordinal()] = COEX_PRAM_NUM;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[SupplicantState.FOUR_WAY_HANDSHAKE.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[SupplicantState.GROUP_HANDSHAKE.ordinal()] = 8;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[SupplicantState.INACTIVE.ordinal()] = 9;
        } catch (NoSuchFieldError e9) {
        }
        try {
            iArr[SupplicantState.INTERFACE_DISABLED.ordinal()] = 10;
        } catch (NoSuchFieldError e10) {
        }
        try {
            iArr[SupplicantState.INVALID.ordinal()] = 11;
        } catch (NoSuchFieldError e11) {
        }
        try {
            iArr[SupplicantState.SCANNING.ordinal()] = 12;
        } catch (NoSuchFieldError e12) {
        }
        try {
            iArr[SupplicantState.UNINITIALIZED.ordinal()] = 13;
        } catch (NoSuchFieldError e13) {
        }
        -android-net-wifi-SupplicantStateSwitchesValues = iArr;
        return iArr;
    }

    static {
        CHNANNEL = new int[]{WifiScanGenieController.CHANNEL_1_FREQ, 2417, 2422, 2427, 2432, WifiScanGenieController.CHANNEL_6_FREQ, 2442, 2447, 2452, 2457, WifiScanGenieController.CHANNEL_11_FREQ, 2467, 2472, 2484};
        WIFI_LTE_CONFIG_FILE = Environment.getDataDirectory() + "/cust/wifi/ism_coex.conf";
        String[] strArr = new String[WIFI_NOT_WORK_INDEX];
        strArr[COEX_CONFIG_FREQ] = "1 2380 2400 15 2380 2400,1 2380 2400 15 2380 2400,1 2380 2400 15 2380 2400,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400";
        strArr[COEX_CONFIG_SIMPLE] = "1 2380 2400 15 2380 2400,1 2380 2400 15 2380 2400,1 2380 2400 15 2380 2400,1 2380 2400 5 2380 2400,1 2380 2400 5 2380 2400,1 2380 2400 5 2380 2400";
        strArr[2] = "0 0 0 0 0 0,0 0 0 0 0 0,1 2380 2400 15 2380 2400,1 2380 2400 5 2380 2400,1 2380 2400 5 2380 2400,1 2380 2400 5 2380 2400";
        strArr[3] = "0 0 0 0 0 0,0 0 0 0 0 0,1 2380 2400 15 2380 2400,1 2380 2400 5 2380 2400,1 2380 2400 5 2380 2400,1 2380 2400 5 2380 2400";
        strArr[4] = "0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,1 2380 2400 5 2380 2400,1 2380 2400 5 2380 2400,1 2380 2400 5 2380 2400";
        strArr[5] = "0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,1 2380 2400 5 2380 2400,1 2380 2400 10 2380 2400,1 2380 2400 10 2380 2400";
        strArr[COEX_PRAM_NUM] = "0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,1 2380 2400 5 2380 2400,1 2380 2400 15 2380 2400,1 2380 2400 15 2380 2400";
        strArr[7] = "0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,1 2380 2400 15 2380 2400";
        strArr[8] = "0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,1 2380 2400 15 2380 2400";
        strArr[9] = WIFI_NOT_WORK;
        strArr[10] = WIFI_NOT_WORK;
        strArr[11] = WIFI_NOT_WORK;
        strArr[12] = WIFI_NOT_WORK;
        strArr[13] = WIFI_NOT_WORK;
        strArr[WIFI_SCAN] = "1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400";
        AT_COMMAND = strArr;
        mIs110xConnectivity = "hi110x".equalsIgnoreCase(SystemProperties.get("ro.connectivity.chiptype"));
        mIsHisiConnectivity = "hisi".equalsIgnoreCase(SystemProperties.get("ro.connectivity.chiptype"));
        AT_COMMAND_HI110X = new String[]{"0 0 0 0 0 0,0 0 0 0 0 0,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400", "0 0 0 0 0 0,0 0 0 0 0 0,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400", "0 0 0 0 0 0,0 0 0 0 0 0,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400", "0 0 0 0 0 0,0 0 0 0 0 0,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400", "0 0 0 0 0 0,0 0 0 0 0 0,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400", "0 0 0 0 0 0,0 0 0 0 0 0,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400", "0 0 0 0 0 0,0 0 0 0 0 0,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400", "0 0 0 0 0 0,0 0 0 0 0 0,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400,1 2390 2400 0 2390 2400", "0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,0 0 0 0 0 0,1 2390 2400 0 2390 2400", WIFI_NOT_WORK, WIFI_NOT_WORK, WIFI_NOT_WORK, WIFI_NOT_WORK, WIFI_NOT_WORK, "0 0 0 0 0 0,0 0 0 0 0 0,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400,1 2380 2400 0 2380 2400", WIFI_NOT_WORK};
    }

    public static HwIsmCoexWifiStateTrack createIsmCoexWifiStateTrack(Context context, WifiStateMachine wifiStateMachine, WifiNative wifiNative) {
        if (SystemProperties.getBoolean(ISM_COEX_ON, false) && isHisiConnectivity()) {
            Log.d(TAG, "hisi-Connectivity chip and ISM_COEX_ON switch is on, need new IsmCoexWifiStateTrack");
            return new IsmCoexWifiStateTrack(context, wifiStateMachine, wifiNative);
        }
        Log.d(TAG, "no need new IsmCoexWifiStateTrack");
        return null;
    }

    static boolean isHisiConnectivity() {
        return !mIsHisiConnectivity ? mIs110xConnectivity : DEBUG;
    }

    public IsmCoexWifiStateTrack(Context context, WifiStateMachine wsm, WifiNative wifiNative) {
        this.mAtCommandLoad = new String[WIFI_NOT_WORK_INDEX];
        this.mWifiState = new AtomicBoolean(false);
        this.mWifiConnect = new AtomicBoolean(false);
        this.mWifiScaning = new AtomicBoolean(false);
        this.mWifiFrequence = COEX_CONFIG_FREQ;
        this.mP2pState = new AtomicBoolean(false);
        this.mP2pConnected = new AtomicBoolean(false);
        this.mP2pDiscovery = new AtomicBoolean(false);
        this.mP2pFrequency = COEX_CONFIG_FREQ;
        this.mSendString = "";
        this.mAtCommandInstall = new AtomicBoolean(false);
        this.mMethodExsit = false;
        this.mClassExsit = false;
        this.mIsLteWork = false;
        this.mLteFreqCoex = null;
        this.mWlanCoexIsOn = false;
        this.mAtCommandIndex = WIFI_NOT_WORK_INDEX;
        this.mChnlIndex = WIFI_NOT_WORK_INDEX;
        this.mAtCommandLoadHi110x = new String[16];
        this.mWifiP2pManagerHisiExt = null;
        this.mPeers = new WifiP2pDeviceList();
        this.mPhoneStateListener = new PhoneStateListener() {
            public void onServiceStateChanged(ServiceState state) {
                if (IsmCoexWifiStateTrack.isHisiConnectivity()) {
                    IsmCoexWifiStateTrack.this.onServiceStateChangedHi110x(state);
                    return;
                }
                IsmCoexWifiStateTrack.logd("onServiceStateChanged:" + state);
                if (state.getNetworkType() == 13 || state.getDataNetworkType() == 13) {
                    IsmCoexWifiStateTrack.this.mIsLteWork = IsmCoexWifiStateTrack.DEBUG;
                    if (IsmCoexWifiStateTrack.this.mWifiState.get() && IsmCoexWifiStateTrack.this.mIsmCoexHandler != null) {
                        IsmCoexWifiStateTrack.this.mIsmCoexHandler.sendMessage(Message.obtain(IsmCoexWifiStateTrack.this.mIsmCoexHandler, IsmCoexWifiStateTrack.COEX_CONFIG_SIMPLE, Boolean.valueOf(IsmCoexWifiStateTrack.DEBUG)));
                    }
                    IsmCoexWifiStateTrack.this.sendAtCommand();
                    return;
                }
                IsmCoexWifiStateTrack.this.mIsLteWork = false;
                if (IsmCoexWifiStateTrack.this.mWifiState.get() && IsmCoexWifiStateTrack.this.mIsmCoexHandler != null) {
                    IsmCoexWifiStateTrack.this.mIsmCoexHandler.sendMessage(Message.obtain(IsmCoexWifiStateTrack.this.mIsmCoexHandler, IsmCoexWifiStateTrack.COEX_CONFIG_SIMPLE, Boolean.valueOf(false)));
                }
            }

            public void onOemHookRawEvent(byte[] result) {
                String OEM_IDENTIFIER = "HISIHOOK";
                int OEM_HEADER = "HISIHOOK".length() + 8;
                int[] iArr = null;
                if (!IsmCoexWifiStateTrack.isHisiConnectivity()) {
                    IsmCoexWifiStateTrack.logd("ignore, not isHisiConnectivity");
                } else if (result.length < OEM_HEADER) {
                    IsmCoexWifiStateTrack.loge("onOemHookRawEvent verfiy fail,result.length:" + Arrays.toString(result));
                } else {
                    ByteBuffer byteData = ByteBuffer.wrap(result);
                    byteData.order(ByteOrder.nativeOrder());
                    IsmCoexWifiStateTrack.logd("onOemHookRawEvent:" + byteData);
                    byte[] oemIdBytes = new byte["HISIHOOK".length()];
                    byteData.get(oemIdBytes);
                    try {
                        String oemIdString = new String(oemIdBytes, "UTF-8");
                        if ("HISIHOOK".equals(oemIdString)) {
                            byteData.getInt();
                            byteData.getInt();
                            int _event = byteData.getInt();
                            byte _length = byteData.get();
                            if (_event == 100 && 24 <= _length) {
                                iArr = new int[(_length / 4)];
                                for (int i = IsmCoexWifiStateTrack.COEX_CONFIG_FREQ; i < iArr.length; i += IsmCoexWifiStateTrack.COEX_CONFIG_SIMPLE) {
                                    iArr[i] = byteData.getInt();
                                }
                            }
                            if (iArr != null && IsmCoexWifiStateTrack.this.checkFreqCoexParam(iArr) == 0) {
                                IsmCoexWifiStateTrack.this.mLteFreqCoex = iArr;
                                IsmCoexWifiStateTrack.this.processLteWithWlanCoex();
                                return;
                            }
                            return;
                        }
                        IsmCoexWifiStateTrack.loge("onOemHookRawEvent identifier not correct," + oemIdString);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        Log.d(TAG, "IsmCoexWifiStateTrack is called!");
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mWifiNative = wifiNative;
        startMonitoring(context);
        loadConfigFile();
        initAtCommandClass();
        registerPhoneStateListener(context);
        this.mIsmCoexHandler = new IsmCoexHandler(this.mContext.getMainLooper());
        if (mIs110xConnectivity) {
            this.mWifiP2pManagerHisiExt = new WifiP2pManagerHisiExt();
        }
        this.mWifiP2pManager = (WifiP2pManager) context.getSystemService("wifip2p");
        initP2p(this.mWifiP2pManager);
    }

    protected void registerPhoneStateListener(Context context) {
        this.mPhone = (TelephonyManager) context.getSystemService("phone");
        if (isHisiConnectivity()) {
            this.mPhone.listen(this.mPhoneStateListener, 32769);
        } else {
            this.mPhone.listen(this.mPhoneStateListener, COEX_CONFIG_SIMPLE);
        }
    }

    private void handleP2pDiscoveryChange(Intent intent) {
        int state = intent.getIntExtra("discoveryState", COEX_CONFIG_SIMPLE);
        if (state == COEX_CONFIG_SIMPLE) {
            this.mP2pDiscovery.set(false);
        } else if (state == 2) {
            this.mP2pDiscovery.set(DEBUG);
        }
    }

    private void handlePeersChanged(Intent intent) {
        if (!this.mP2pDiscovery.get()) {
            int mConnectedDevices = COEX_CONFIG_FREQ;
            for (WifiP2pDevice peer : this.mPeers.getDeviceList()) {
                if (peer.status == 0) {
                    mConnectedDevices += COEX_CONFIG_SIMPLE;
                }
            }
            logd("handlePeersChanged mConnectedDevices " + mConnectedDevices);
            if (mConnectedDevices > 0) {
                this.mP2pConnected.set(DEBUG);
            } else {
                this.mP2pConnected.set(false);
            }
        }
    }

    private void startMonitoring(Context context) {
        logd("startMonitoring");
        this.mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
        filter.addAction("android.net.wifi.p2p.STATE_CHANGED");
        filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        filter.addAction("android.net.wifi.p2p.DISCOVERY_STATE_CHANGE");
        filter.addAction("android.net.wifi.p2p.PEERS_CHANGED");
        this.mWifiStateReceiver = new WifiStateReceiver();
        this.mContext.registerReceiver(this.mWifiStateReceiver, filter);
    }

    private void handleWifiStateChange(Intent intent) {
        int state = intent.getIntExtra("wifi_state", 4);
        logd("handleWifiStateChange: " + state);
        switch (state) {
            case COEX_CONFIG_SIMPLE /*1*/:
                this.mWifiState.set(false);
                this.mWifiConnect.set(false);
                this.mWifiFrequence = COEX_CONFIG_FREQ;
            case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                this.mWifiState.set(DEBUG);
                if (!isHisiConnectivity() && this.mIsLteWork && this.mIsmCoexHandler != null) {
                    this.mIsmCoexHandler.sendMessage(Message.obtain(this.mIsmCoexHandler, COEX_CONFIG_SIMPLE, Boolean.valueOf(DEBUG)));
                }
            default:
        }
    }

    private void handleSupplicantStateChange(Intent intent) {
        if (!isHisiConnectivity()) {
            SupplicantState state = (SupplicantState) intent.getParcelableExtra("newState");
            logd("handleSupplicantStateChange: " + state);
            if (state != null) {
                switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
                    case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                        this.mWifiConnect.set(DEBUG);
                        WifiInfo connectWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
                        logd("connectWifiInfo:" + connectWifiInfo);
                        for (ScanResult result : this.mWifiStateMachine.syncGetScanResultsList()) {
                            if (result.BSSID.equals(connectWifiInfo.getBSSID())) {
                                this.mWifiFrequence = result.frequency;
                                logd("result:" + result + "\n" + "FrequencyBand:" + this.mWifiStateMachine.getFrequencyBand());
                            }
                        }
                        break;
                    case MessageUtil.MSG_WIFI_FIND_TARGET /*5*/:
                        this.mWifiConnect.set(false);
                        this.mWifiFrequence = COEX_CONFIG_FREQ;
                        break;
                    case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                        this.mWifiScaning.set(DEBUG);
                        break;
                    default:
                        this.mWifiScaning.set(false);
                        this.mWifiConnect.set(false);
                        break;
                }
            }
            return;
        }
        handleSupplicantStateChangeHi110x(intent);
    }

    private void handleP2pConnectState(Intent intent) {
        logd("handleP2pConnectState: networkInfo = " + ((NetworkInfo) intent.getParcelableExtra("networkInfo")));
        if (this.mP2pConnected.get()) {
            this.mWifiP2pManager.requestGroupInfo(this.mP2pChannel, this.mGroupInfoListener);
        } else {
            this.mP2pFrequency = COEX_CONFIG_FREQ;
        }
    }

    private void handleP2pState(Intent intent) {
        int state = intent.getIntExtra("wifi_p2p_state", COEX_CONFIG_SIMPLE);
        logd("handleP2pState: state = " + state);
        switch (state) {
            case COEX_CONFIG_SIMPLE /*1*/:
                this.mP2pState.set(false);
                this.mP2pConnected.set(false);
                this.mP2pFrequency = COEX_CONFIG_FREQ;
            case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                this.mP2pState.set(DEBUG);
            default:
        }
    }

    private void sendAtCommand() {
        logd(" mWifiState:" + this.mWifiState + "\n mP2pState:" + this.mP2pState + "\n mWifiConnected:" + this.mWifiConnect + "\n mFrequencyWifi:" + this.mWifiFrequence + "\n mP2pConnected:" + this.mP2pConnected + "\n mFrequencyP2p:" + this.mP2pFrequency + "\n mP2pDiscovery:" + this.mP2pDiscovery + "\n mAtCommandInstall:" + this.mAtCommandInstall + "\n mIsLteWork:" + this.mIsLteWork);
        if (!this.mIsLteWork) {
            loge("sendAtCommand fail because mIsLteWork is false");
        } else if (!this.mWifiState.get() || isWifiClose()) {
            atCommand(WIFI_NOT_WORK);
        } else if (this.mP2pConnected.get()) {
            if (!this.mWifiConnect.get() || this.mP2pFrequency == this.mWifiFrequence) {
                atCommand(getAtCommand(getChannelId(this.mP2pFrequency)));
            } else {
                atCommand(getAtCommand(WIFI_SCAN));
            }
        } else {
            if (this.mWifiConnect.get() && this.mWifiFrequence != 0) {
                atCommand(getAtCommand(getChannelId(this.mWifiFrequence)));
            } else if (this.mWifiState.get()) {
                atCommand(getAtCommand(WIFI_SCAN));
            } else {
                atCommand(WIFI_NOT_WORK);
                loge("should not be here");
            }
        }
    }

    private boolean isWifiClose() {
        if (Global.getInt(this.mContext.getContentResolver(), "wifi_on", COEX_CONFIG_FREQ) == 0) {
            return isScanAlwaysInvailable();
        }
        return false;
    }

    private boolean isScanAlwaysInvailable() {
        return Global.getInt(this.mContext.getContentResolver(), "wifi_scan_always_enabled", COEX_CONFIG_FREQ) == 0 ? DEBUG : false;
    }

    private String getAtCommand(int channel) {
        if (this.mAtCommandInstall.get()) {
            return this.mAtCommandLoad[channel];
        }
        return AT_COMMAND[channel];
    }

    private int getChannelId(int frequence) {
        for (int i = COEX_CONFIG_FREQ; i < CHNANNEL.length; i += COEX_CONFIG_SIMPLE) {
            if (frequence == CHNANNEL[i]) {
                return i;
            }
        }
        if (isHisiConnectivity()) {
            return WIFI_NOT_WORK_INDEX;
        }
        return COEX_CONFIG_FREQ;
    }

    protected void initAtCommandClass() {
        try {
            this.mReflect = Class.forName(ATCOMMAND_PATH);
            Class[] clsArr = new Class[COEX_CONFIG_SIMPLE];
            clsArr[COEX_CONFIG_FREQ] = String.class;
            this.mCallATCommand = this.mReflect.getMethod("setISMCOEX", clsArr);
            this.mGetInstance = this.mReflect.getMethod("getDefault", (Class[]) null);
            this.mClassExsit = DEBUG;
            this.mMethodExsit = DEBUG;
        } catch (RuntimeException e) {
            this.mClassExsit = false;
            this.mMethodExsit = false;
            Log.e(TAG, e.toString());
        } catch (Exception e2) {
            Log.e(TAG, e2.toString());
        }
    }

    Boolean callATCommand(String atCommand) {
        Boolean response = Boolean.valueOf(false);
        if (this.mClassExsit && this.mMethodExsit) {
            try {
                Object obj = this.mGetInstance.invoke(null, (Object[]) null);
                Method method = this.mCallATCommand;
                Object[] objArr = new Object[COEX_CONFIG_SIMPLE];
                objArr[COEX_CONFIG_FREQ] = atCommand;
                response = (Boolean) method.invoke(obj, objArr);
                Log.d(TAG, atCommand);
                return response;
            } catch (RuntimeException e) {
                Log.e(TAG, e.toString());
                return response;
            } catch (Exception e2) {
                Log.e(TAG, e2.toString());
                return response;
            }
        }
        Log.d(TAG, "class or method does not exsit");
        return response;
    }

    private void atCommand(String string) {
        logd("atCommand:" + string);
        if (!this.mSendString.equals(string)) {
            this.mSendString = string;
            if (!this.mClassExsit) {
                initAtCommandClass();
            }
            callATCommand(string);
        }
    }

    private static void logd(String s) {
        Log.d(TAG, s);
    }

    private static void loge(String s) {
        Log.e(TAG, s);
    }

    public void initP2p(WifiP2pManager wifiP2pManager) {
        Log.d(TAG, "initP2p is called!");
        this.mWifiP2pManager = wifiP2pManager;
        this.mP2pChannel = this.mWifiP2pManager.initialize(this.mContext, this.mContext.getMainLooper(), null);
        this.mGroupInfoListener = new GroupInfoListener() {
            public void onGroupInfoAvailable(WifiP2pGroup group) {
                IsmCoexWifiStateTrack.this.mP2pFrequency = group.getFrequence();
                IsmCoexWifiStateTrack.logd("onGroupInfoAvailable: group=" + group);
                if (IsmCoexWifiStateTrack.isHisiConnectivity()) {
                    int chnlid = IsmCoexWifiStateTrack.this.getLocalCoexChnlId();
                    if (chnlid != IsmCoexWifiStateTrack.this.mChnlIndex) {
                        IsmCoexWifiStateTrack.this.mChnlIndex = chnlid;
                        IsmCoexWifiStateTrack.logd("IsmCoexWifiStateTrack.java InitP2p");
                        IsmCoexWifiStateTrack.this.processLteWithWlanCoex();
                        return;
                    }
                    return;
                }
                IsmCoexWifiStateTrack.this.sendAtCommand();
            }
        };
    }

    private void loadConfigFile() {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            InputStreamReader in = new InputStreamReader(new FileInputStream(WIFI_LTE_CONFIG_FILE), "UTF-8");
            try {
                BufferedReader br = new BufferedReader(in);
                int i = COEX_CONFIG_FREQ;
                while (true) {
                    try {
                        String line = br.readLine();
                        if (line == null) {
                            break;
                        }
                        if (isHisiConnectivity()) {
                            this.mAtCommandLoadHi110x[i] = line.split(":")[COEX_CONFIG_SIMPLE];
                        } else {
                            this.mAtCommandLoad[i] = line.split(":")[COEX_CONFIG_SIMPLE];
                        }
                        i += COEX_CONFIG_SIMPLE;
                    } catch (FileNotFoundException e3) {
                        e = e3;
                        bufferedReader = br;
                        inputStreamReader = in;
                    } catch (IOException e4) {
                        e2 = e4;
                        bufferedReader = br;
                        inputStreamReader = in;
                    } catch (Throwable th2) {
                        th = th2;
                        bufferedReader = br;
                        inputStreamReader = in;
                    }
                }
                this.mAtCommandInstall.set(DEBUG);
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
                if (in != null) {
                    in.close();
                }
            } catch (FileNotFoundException e5) {
                e = e5;
                inputStreamReader = in;
                try {
                    this.mAtCommandInstall.set(false);
                    loge(e.getMessage());
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (isHisiConnectivity()) {
                        this.mAtCommandLoadHi110x[WIFI_NOT_WORK_INDEX] = WIFI_NOT_WORK;
                    }
                    logd("loadConfigFile:mAtCommandInstall:" + this.mAtCommandInstall);
                } catch (Throwable th3) {
                    th = th3;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                            throw th;
                        }
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    throw th;
                }
            } catch (IOException e6) {
                e2222 = e6;
                inputStreamReader = in;
                this.mAtCommandInstall.set(false);
                loge(e2222.getMessage());
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e22222) {
                        e22222.printStackTrace();
                    }
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (isHisiConnectivity()) {
                    this.mAtCommandLoadHi110x[WIFI_NOT_WORK_INDEX] = WIFI_NOT_WORK;
                }
                logd("loadConfigFile:mAtCommandInstall:" + this.mAtCommandInstall);
            } catch (Throwable th4) {
                th = th4;
                inputStreamReader = in;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e7) {
            e = e7;
            this.mAtCommandInstall.set(false);
            loge(e.getMessage());
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (isHisiConnectivity()) {
                this.mAtCommandLoadHi110x[WIFI_NOT_WORK_INDEX] = WIFI_NOT_WORK;
            }
            logd("loadConfigFile:mAtCommandInstall:" + this.mAtCommandInstall);
        } catch (IOException e8) {
            e22222 = e8;
            this.mAtCommandInstall.set(false);
            loge(e22222.getMessage());
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (isHisiConnectivity()) {
                this.mAtCommandLoadHi110x[WIFI_NOT_WORK_INDEX] = WIFI_NOT_WORK;
            }
            logd("loadConfigFile:mAtCommandInstall:" + this.mAtCommandInstall);
        }
        if (isHisiConnectivity()) {
            this.mAtCommandLoadHi110x[WIFI_NOT_WORK_INDEX] = WIFI_NOT_WORK;
        }
        logd("loadConfigFile:mAtCommandInstall:" + this.mAtCommandInstall);
    }

    private void onServiceStateChangedHi110x(ServiceState state) {
        logd("onServiceStateChanged, NetworkType: " + this.mPhone.getNetworkType());
        if (this.mPhone.getNetworkType() == 13) {
            this.mIsLteWork = DEBUG;
        } else {
            this.mIsLteWork = false;
            this.mLteFreqCoex = null;
        }
        processLteWithWlanCoex();
    }

    private int checkFreqCoexParam(int[] freqCoex) {
        if (freqCoex.length != COEX_PRAM_NUM) {
            logd("Lte param length error :" + freqCoex.length);
            return -1;
        } else if (freqCoex[COEX_CONFIG_FREQ] < 0 || freqCoex[COEX_CONFIG_FREQ] > 2) {
            logd("Lte param state must in 0-2 :" + freqCoex[COEX_CONFIG_FREQ]);
            return -1;
        } else if (freqCoex[2] < 0 || freqCoex[2] > 5) {
            logd("Lte param uplink BandWidth must in 0 to 5 :" + freqCoex[2]);
            return -1;
        } else if (freqCoex[4] < 0 || freqCoex[4] > 5) {
            logd("Lte param downlink BandWidth must in 0 to 5 :" + freqCoex[4]);
            return -1;
        } else if (freqCoex[5] <= 0 || freqCoex[5] > 64) {
            logd("Lte param  Band num must in 1 to 64 :" + freqCoex[5]);
            return -1;
        } else {
            logd("Lte param :state" + freqCoex[COEX_CONFIG_FREQ] + ", ulfreq:" + freqCoex[COEX_CONFIG_SIMPLE] + ",ulbw:" + freqCoex[2] + ",dlfreq:" + freqCoex[3] + ", dlbw:" + freqCoex[4] + ", band:" + freqCoex[5]);
            return COEX_CONFIG_FREQ;
        }
    }

    private void processLteWithWlanCoex() {
        boolean coex;
        switch (SystemProperties.getInt(ISM_COEX_CONFIG, COEX_CONFIG_FREQ)) {
            case COEX_CONFIG_SIMPLE /*1*/:
                coex = checkLteWithWlanCoexBySimple();
                break;
            default:
                logd("processLteWithWlanCoex checkLteWithWlanCoexByFreq");
                coex = checkLteWithWlanCoexByFreq();
                break;
        }
        logd("processLteWithWlanCoex coex = " + coex);
        setWlanCoex(coex);
        this.mIsmCoexHandler.sendMessage(Message.obtain(this.mIsmCoexHandler, COEX_CONFIG_FREQ, Boolean.valueOf(coex)));
    }

    private void onLteFreqCoexChanged(AsyncResult aResult) {
        int[] tmpFreqCoex = aResult.result;
        if (checkFreqCoexParam(tmpFreqCoex) == 0) {
            this.mLteFreqCoex = tmpFreqCoex;
            logd("IsmCoexWifiStateTrack.java onLteFreqCoexChanged");
            processLteWithWlanCoex();
        }
    }

    private boolean checkLteWithWlanCoexBySimple() {
        boolean p2pState = false;
        if (this.mWifiP2pManagerHisiExt != null) {
            p2pState = this.mWifiP2pManagerHisiExt.isWifiP2pEnabled();
        }
        logd("Ltestate:" + this.mIsLteWork + ", wifistate:" + this.mWifiState.get() + ", p2pState:" + p2pState);
        if (this.mIsLteWork) {
            return !p2pState ? this.mWifiState.get() : DEBUG;
        } else {
            return false;
        }
    }

    private boolean checkLteWithWlanCoexByFreq() {
        if (this.mLteFreqCoex == null) {
            logd("mLteFreqCoex is NULL");
            return false;
        } else if (this.mLteFreqCoex[COEX_CONFIG_FREQ] != COEX_CONFIG_SIMPLE) {
            logd("mLteFreqCoex[0] is 0");
            return false;
        } else if (this.mIsLteWork) {
            String atComandString = getAtCommandHi110x(this.mChnlIndex);
            logd("chnlid:" + this.mChnlIndex + ", atcommandstring:" + atComandString);
            int[] uplinkFreq = getDetailCoexParamByBand(atComandString, this.mLteFreqCoex[2]);
            int lteUplinkFreq = this.mLteFreqCoex[COEX_CONFIG_SIMPLE];
            logd("LcheckLteWithWlanCoexByFreq: lte-uplink= " + this.mLteFreqCoex[COEX_CONFIG_SIMPLE] + " lte-downlink= " + this.mLteFreqCoex[3]);
            if (uplinkFreq[COEX_CONFIG_FREQ] != COEX_CONFIG_SIMPLE || uplinkFreq[COEX_CONFIG_SIMPLE] * 10 > lteUplinkFreq || lteUplinkFreq > uplinkFreq[2] * 10) {
                int[] downlinkFreq = getDetailCoexParamByBand(atComandString, this.mLteFreqCoex[4]);
                int lteDownlinkFreq = this.mLteFreqCoex[3];
                if (downlinkFreq[COEX_CONFIG_FREQ] != COEX_CONFIG_SIMPLE || downlinkFreq[4] * 10 > lteDownlinkFreq || lteDownlinkFreq > downlinkFreq[5] * 10) {
                    return false;
                }
                logd("Lte downlink exit coex : lte-downlink= " + lteDownlinkFreq);
                return DEBUG;
            }
            logd("Lte uplink exit coex: lte-uplink= " + lteUplinkFreq);
            return DEBUG;
        } else {
            logd("checkLteWithWlanCoexByFreq Lte is Down");
            return false;
        }
    }

    private int[] getDetailCoexParamByBand(String param, int index) {
        String[] array = param.split(",");
        int[] detailCoex = new int[COEX_PRAM_NUM];
        if (array.length > index) {
            String[] tmp = array[index].split(HwCHRWifiCPUUsage.COL_SEP);
            int i = COEX_CONFIG_FREQ;
            while (i < tmp.length && i < COEX_PRAM_NUM) {
                detailCoex[i] = Integer.parseInt(tmp[i]);
                i += COEX_CONFIG_SIMPLE;
            }
        }
        logd("DetailCoex: index=" + index + ", state=" + detailCoex[COEX_CONFIG_FREQ] + ", uplink-min=" + detailCoex[COEX_CONFIG_SIMPLE] + ", uplink-max=" + detailCoex[2] + ", uplink-txpower=" + detailCoex[3] + ", down-min=" + detailCoex[4] + ", down-max=" + detailCoex[5]);
        return detailCoex;
    }

    private void setWlanCoex(boolean isOn) {
        if (this.mWlanCoexIsOn == isOn) {
            logd("the coex settings is not changed ");
            return;
        }
        this.mWlanCoexIsOn = isOn;
        logd("Set Wlan Coex is On? " + this.mWlanCoexIsOn);
        if (this.mWlanCoexIsOn) {
            this.mIsmCoexHandler.sendMessage(Message.obtain(this.mIsmCoexHandler, COEX_CONFIG_SIMPLE, Boolean.valueOf(DEBUG)));
        } else {
            this.mIsmCoexHandler.sendMessage(Message.obtain(this.mIsmCoexHandler, COEX_CONFIG_SIMPLE, Boolean.valueOf(false)));
        }
    }

    private void handleSupplicantStateChangeHi110x(Intent intent) {
        SupplicantState state = (SupplicantState) intent.getParcelableExtra("newState");
        logd("handleSupplicantStateChange: " + state);
        if (state != null) {
            switch (-getandroid-net-wifi-SupplicantStateSwitchesValues()[state.ordinal()]) {
                case COEX_CONFIG_SIMPLE /*1*/:
                case WifiScanGenieDataBaseImpl.DATABASE_VERSION /*2*/:
                case MessageUtil.MSG_WIFI_ENABLED /*3*/:
                case MessageUtil.MSG_WIFI_UPDATE_SCAN_RESULT /*7*/:
                    this.mWifiScaning.set(false);
                    this.mWifiConnect.set(DEBUG);
                    break;
                case MessageUtil.MSG_WIFI_DISABLE /*4*/:
                    this.mWifiConnect.set(DEBUG);
                    WifiInfo connectWifiInfo = this.mWifiStateMachine.syncRequestConnectionInfo();
                    logd("connectWifiInfo:" + connectWifiInfo);
                    for (ScanResult result : this.mWifiStateMachine.syncGetScanResultsList()) {
                        if (result.BSSID.equals(connectWifiInfo.getBSSID())) {
                            this.mWifiFrequence = result.frequency;
                            logd("result:" + result + "\n" + "FrequencyBand:" + this.mWifiStateMachine.getFrequencyBand());
                        }
                    }
                    break;
                case MessageUtil.MSG_WIFI_FIND_TARGET /*5*/:
                    this.mWifiConnect.set(false);
                    this.mWifiFrequence = COEX_CONFIG_FREQ;
                    break;
                case COEX_PRAM_NUM /*6*/:
                case MessageUtil.MSG_WIFI_CONFIG_CHANGED /*8*/:
                    this.mWifiScaning.set(false);
                    break;
                case MessageUtil.MSG_WIFI_INTERNET_DISCONNECTED /*12*/:
                    this.mWifiScaning.set(DEBUG);
                    break;
                default:
                    this.mWifiScaning.set(false);
                    this.mWifiConnect.set(false);
                    break;
            }
        }
    }

    private int getLocalCoexChnlId() {
        int chnlid;
        if (mIsHisiConnectivity) {
            logd(" mWifiState:" + this.mWifiState + "\n mP2pState:" + this.mP2pState + "\n mWifiConnected:" + this.mWifiConnect + "\n mFrequencyWifi:" + this.mWifiFrequence + "\n mP2pConnected:" + this.mP2pConnected + "\n mFrequencyP2p:" + this.mP2pFrequency + "\n mP2pDiscovery:" + this.mP2pDiscovery + "\n mAtCommandInstall:" + this.mAtCommandInstall + "\n mIsLteWork:" + this.mIsLteWork + "\n mChnlIndex:" + this.mChnlIndex);
            if (this.mWifiState.get() || this.mP2pState.get()) {
                if (this.mP2pConnected.get()) {
                    if (!this.mWifiConnect.get() || this.mP2pFrequency == this.mWifiFrequence) {
                        chnlid = getChannelId(this.mP2pFrequency);
                    }
                }
                if (this.mWifiConnect.get() && this.mWifiFrequence != 0) {
                    chnlid = getChannelId(this.mWifiFrequence);
                } else if (this.mWifiState.get()) {
                    chnlid = WIFI_SCAN;
                } else {
                    chnlid = WIFI_NOT_WORK_INDEX;
                    loge("should not be here");
                }
                logd("getLocalCoexChnlId: chnlid=" + chnlid);
                return chnlid;
            }
            logd("getLocalCoexChnlId: wifi & P2P is not work");
            return WIFI_NOT_WORK_INDEX;
        }
        boolean p2pState = false;
        if (this.mWifiP2pManagerHisiExt != null) {
            p2pState = this.mWifiP2pManagerHisiExt.isWifiP2pEnabled();
        }
        logd(" mWifiState:" + this.mWifiState + "\n p2pState:" + p2pState + "\n mWifiConnected:" + this.mWifiConnect + "\n mFrequencyWifi:" + this.mWifiFrequence + "\n mP2pConnected:" + this.mP2pConnected + "\n mFrequencyP2p:" + this.mP2pFrequency + "\n mP2pDiscovery:" + this.mP2pDiscovery + "\n mAtCommandInstall:" + this.mAtCommandInstall + "\n mIsLteWork:" + this.mIsLteWork);
        if (!this.mWifiState.get() && !p2pState) {
            logd("getLocalCoexChnlId: wifi & P2P is not work");
            return WIFI_NOT_WORK_INDEX;
        } else if (p2pState) {
            if (this.mP2pConnected.get()) {
                chnlid = getChannelId(this.mP2pFrequency);
            } else {
                chnlid = WIFI_SCAN;
            }
            logd("getLocalCoexChnlId: P2p chnlid=" + chnlid);
            return chnlid;
        } else {
            if (this.mWifiConnect.get() && this.mWifiFrequence != 0) {
                chnlid = getChannelId(this.mWifiFrequence);
            } else if (this.mWifiState.get()) {
                chnlid = WIFI_SCAN;
            } else {
                chnlid = WIFI_NOT_WORK_INDEX;
                loge("should not be here");
            }
            logd("getLocalCoexChnlId: Wifi chnlid=" + chnlid);
            return chnlid;
        }
    }

    private void sendAtCommandHi110x(boolean coex) {
        int chnlid = this.mChnlIndex;
        if (!coex) {
            chnlid = WIFI_NOT_WORK_INDEX;
        }
        if (this.mAtCommandIndex != chnlid) {
            logd("chnlid=" + chnlid);
            this.mAtCommandIndex = chnlid;
            atCommand(getAtCommandHi110x(chnlid));
        }
    }

    private String getAtCommandHi110x(int channel) {
        if (this.mAtCommandInstall.get()) {
            return this.mAtCommandLoadHi110x[channel];
        }
        return AT_COMMAND_HI110X[channel];
    }
}
