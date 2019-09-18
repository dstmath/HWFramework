package com.android.server.wifi.HwQoE;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.android.server.wifi.wifipro.WifiHandover;
import com.android.server.wifi.wifipro.WifiProStateMachine;
import java.util.List;

public class HwQoEWeChatBooster {
    public static final int KOG_UDP_TYPE = 17;
    private static final int MSG_APP_STATE_CHANHED = 4;
    private static final int MSG_CONNECTIVITY_ACTION = 6;
    private static final int MSG_WIFI_CONNECTED = 2;
    private static final int MSG_WIFI_DELAY_DISCONNECT = 5;
    public static final int MSG_WIFI_DELAY_DISCONNECT_TIMER = 4000;
    private static final int MSG_WIFI_DISCONNECTED = 3;
    private static final int MSG_WIFI_RSSI_CHANHED = 1;
    private static final int MSG_WIFI_STATE_DISABLED = 7;
    private static final int MSG_WIFI_STATE_ENABLED = 8;
    private static final String TAG = "HiDATA_WeChatBooster";
    public static final int WECHART_AUDIO_THRESHOLD = 30;
    public static final int WECHART_DISABLE_FUNC = 0;
    public static final int WECHART_ENABLE_FUNC = 1;
    public static final int WECHART_NETWORK_CELLULAR = 0;
    public static final int WECHART_NETWORK_RTT_THRESHOLD = 300;
    public static final int WECHART_NETWORK_UNKNOW = -1;
    public static final int WECHART_NETWORK_WIFI = 1;
    public static final int WECHART_POOR_RSSI_LEVEL = 2;
    private static final int WECHART_PW_INIT = 5;
    private static final int WECHART_PW_START = 4;
    private static final int WECHART_PW_STOP = 3;
    public static final int WECHART_VIDEO_THRESHOLD = 120;
    private static final String WECHAT_NAME = "com.tencent.mm";
    private static HwQoEWeChatBooster mHwQoEWeChatBooster;
    /* access modifiers changed from: private */
    public boolean isNetworkChecking = false;
    /* access modifiers changed from: private */
    public boolean isNetworkHaveInternet = false;
    private boolean isPoorNetwork = false;
    private int isPoorNetworkNum = 0;
    private NetworkInfo mActiveNetworkInfo;
    /* access modifiers changed from: private */
    public int mApAuthType;
    private int mApBlackType;
    private CameraManager.AvailabilityCallback mAvailabilityCallback = new CameraManager.AvailabilityCallback() {
        public void onCameraAvailable(String cameraId) {
            HwQoEUtils.logD("onCameraAvailable cameraId = " + cameraId);
            if (cameraId.equals(HwQoEWeChatBooster.this.mCameraId)) {
                String unused = HwQoEWeChatBooster.this.mCameraId = "none";
            }
        }

        public void onCameraUnavailable(String cameraId) {
            HwQoEUtils.logD("onCameraUnavailable cameraId = " + cameraId);
            String unused = HwQoEWeChatBooster.this.mCameraId = cameraId;
        }
    };
    private BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public String mCameraId = "none";
    private CameraManager mCameraManager;
    private long mCheckStartTime = 0;
    /* access modifiers changed from: private */
    public List<HwQoEQualityInfo> mConnectedRecordList;
    private ConnectivityManager mConnectivityManager;
    private Context mContext;
    /* access modifiers changed from: private */
    public int mCurrRssiLevel = -1;
    /* access modifiers changed from: private */
    public String mCurrSSID;
    /* access modifiers changed from: private */
    public int mCurrWeChatType = 0;
    /* access modifiers changed from: private */
    public HiDataApBlackWhiteListManager mHiDataApBlackWhiteListManager;
    private HiDataUtilsManager mHiDataUtilsManager;
    /* access modifiers changed from: private */
    public HidataWechatTraffic mHidataWechatTraffic;
    private HwQoEContentAware mHwQoEContentAware;
    private HwQoEQualityManager mHwQoEQualityManager;
    private HwQoEWiFiOptimization mHwQoEWiFiOptimization;
    /* access modifiers changed from: private */
    public Handler mHwWeChatBoosterHandler;
    /* access modifiers changed from: private */
    public HwWifiCHRService mHwWifiCHRService;
    private IntentFilter mIntentFilter;
    /* access modifiers changed from: private */
    public boolean mIsCheckingRtt = false;
    /* access modifiers changed from: private */
    public int mIsHandoverRSSI = 0;
    /* access modifiers changed from: private */
    public boolean mIsHandoverToMobile = false;
    private boolean mIsUserManualConnectSuccess;
    /* access modifiers changed from: private */
    public boolean mIsUserManualOpenSuccess;
    /* access modifiers changed from: private */
    public boolean mIsVerifyState = false;
    /* access modifiers changed from: private */
    public boolean mIsWeCharting = false;
    /* access modifiers changed from: private */
    public NetworkInfo mNetworkInfo;
    private boolean mPhaseTwoIsEnable = false;
    private int mUserType;
    /* access modifiers changed from: private */
    public int mWeChartUID = -1;
    /* access modifiers changed from: private */
    public boolean mWeChatHoldWifi;
    /* access modifiers changed from: private */
    public long mWeChatStartTime;
    /* access modifiers changed from: private */
    public int mWechatNetwork = -1;
    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;

    public static HwQoEWeChatBooster createInstance(Context context) {
        if (mHwQoEWeChatBooster == null) {
            mHwQoEWeChatBooster = new HwQoEWeChatBooster(context);
        }
        return mHwQoEWeChatBooster;
    }

    public static HwQoEWeChatBooster getInstance() {
        return mHwQoEWeChatBooster;
    }

    private HwQoEWeChatBooster(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        initHwWeChatBoosterHandler();
        registerBroadcastReceiver();
        this.mHwQoEWiFiOptimization = HwQoEWiFiOptimization.getInstance(this.mContext);
        this.mHwQoEContentAware = HwQoEContentAware.getInstance();
        this.mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
        this.mConnectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        this.mCameraManager.registerAvailabilityCallback(this.mAvailabilityCallback, null);
        this.mHwQoEQualityManager = HwQoEQualityManager.getInstance(this.mContext);
        this.mHidataWechatTraffic = new HidataWechatTraffic(this.mContext);
        this.mHiDataApBlackWhiteListManager = HiDataApBlackWhiteListManager.createInstance(this.mContext);
        this.mHiDataUtilsManager = HiDataUtilsManager.getInstance(this.mContext);
    }

    private void registerBroadcastReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo unused = HwQoEWeChatBooster.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (HwQoEWeChatBooster.this.mNetworkInfo == null) {
                        return;
                    }
                    if (HwQoEWeChatBooster.this.mNetworkInfo.getState() == NetworkInfo.State.DISCONNECTED) {
                        HwQoEWeChatBooster.this.mHwWeChatBoosterHandler.sendEmptyMessage(3);
                        boolean unused2 = HwQoEWeChatBooster.this.mIsVerifyState = false;
                    } else if (HwQoEWeChatBooster.this.mNetworkInfo.getState() == NetworkInfo.State.CONNECTED) {
                        HwQoEUtils.logD("DetailedState: " + HwQoEWeChatBooster.this.mNetworkInfo.getDetailedState());
                        if (HwQoEWeChatBooster.this.mNetworkInfo.getDetailedState() == NetworkInfo.DetailedState.VERIFYING_POOR_LINK) {
                            boolean unused3 = HwQoEWeChatBooster.this.mIsVerifyState = true;
                            return;
                        }
                        HwQoEWeChatBooster.this.mHwWeChatBoosterHandler.sendEmptyMessage(2);
                        boolean unused4 = HwQoEWeChatBooster.this.mIsVerifyState = false;
                    }
                } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                    HwQoEWeChatBooster.this.mHwWeChatBoosterHandler.sendEmptyMessage(1);
                } else if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action)) {
                    HwQoEWeChatBooster.this.mHwWeChatBoosterHandler.sendEmptyMessage(6);
                } else if ("android.net.wifi.WIFI_STATE_CHANGED".equals(action)) {
                    int wifistatue = intent.getIntExtra("wifi_state", 4);
                    if (1 == wifistatue) {
                        HwQoEWeChatBooster.this.mHwWeChatBoosterHandler.sendEmptyMessage(7);
                    } else if (3 == wifistatue) {
                        HwQoEWeChatBooster.this.mHwWeChatBoosterHandler.sendEmptyMessage(8);
                    }
                }
            }
        };
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
    }

    private void initHwWeChatBoosterHandler() {
        HandlerThread handlerThread = new HandlerThread("hw_wechatbooster_handler_thread");
        handlerThread.start();
        this.mHwWeChatBoosterHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i != 112) {
                    if (i != 122) {
                        switch (i) {
                            case 1:
                                HwQoEWeChatBooster.this.handleWifiRssiChanged();
                                break;
                            case 2:
                                HwQoEWeChatBooster.this.handleWifiConnected();
                                break;
                            case 3:
                                HwQoEWeChatBooster.this.handleWifiDisConnected();
                                break;
                            case 4:
                                int uid = msg.arg1;
                                int state = msg.arg2;
                                boolean isBackground = ((Boolean) msg.obj).booleanValue();
                                int unused = HwQoEWeChatBooster.this.mWeChartUID = uid;
                                if (1 != state) {
                                    if (state != 0) {
                                        if (2 == state) {
                                            HwQoEWeChatBooster hwQoEWeChatBooster = HwQoEWeChatBooster.this;
                                            hwQoEWeChatBooster.logD("WECHAT_BACK_GROUND_CHANG  mIsWeCharting = " + HwQoEWeChatBooster.this.mIsWeCharting + " isBackground = " + isBackground);
                                            if (HwQoEWeChatBooster.this.mIsWeCharting) {
                                                if (!isBackground) {
                                                    HwQoEWeChatBooster.this.startWiFiOptimization();
                                                    break;
                                                } else {
                                                    HwQoEWeChatBooster.this.stopWiFiOptimization();
                                                    break;
                                                }
                                            } else {
                                                return;
                                            }
                                        }
                                    } else {
                                        boolean unused2 = HwQoEWeChatBooster.this.mIsUserManualOpenSuccess = false;
                                        boolean unused3 = HwQoEWeChatBooster.this.mIsWeCharting = false;
                                        if (HwQoEWeChatBooster.this.mWechatNetwork == 1) {
                                            HwQoEWeChatBooster hwQoEWeChatBooster2 = HwQoEWeChatBooster.this;
                                            hwQoEWeChatBooster2.logD("WeChat phone trun off in wifi,holdWifi :" + HwQoEWeChatBooster.this.mWeChatHoldWifi);
                                            if (HwQoEWeChatBooster.this.mWeChatHoldWifi && System.currentTimeMillis() - HwQoEWeChatBooster.this.mWeChatStartTime > HidataWechatTraffic.MIN_VALID_TIME) {
                                                HwQoEWeChatBooster.this.mHiDataApBlackWhiteListManager.updateHoldWiFiCounter(HwQoEWeChatBooster.this.mCurrSSID, HwQoEWeChatBooster.this.mApAuthType, HwQoEWeChatBooster.this.mCurrWeChatType);
                                            }
                                            HwQoEWeChatBooster.this.stopWiFiOptimization();
                                            HwQoEWeChatBooster.this.updateAppQualityRecords(HwQoEWeChatBooster.this.mConnectedRecordList);
                                        } else if (HwQoEWeChatBooster.this.mWechatNetwork == 0) {
                                            HwQoEWeChatBooster.this.logD("WeChat phone trun off in celluar");
                                            HwQoEWeChatBooster.this.mHidataWechatTraffic.updateMobileWechatStateChanged(0, 2, HwQoEWeChatBooster.this.mWeChartUID);
                                        } else if (-1 == HwQoEWeChatBooster.this.mWechatNetwork) {
                                            HwQoEWeChatBooster.this.logD("WeChat phone trun off ");
                                        }
                                        int unused4 = HwQoEWeChatBooster.this.mCurrWeChatType = 0;
                                        boolean unused5 = HwQoEWeChatBooster.this.mWeChatHoldWifi = false;
                                        if (HwQoEWeChatBooster.this.mIsHandoverToMobile) {
                                            WifiProStateMachine.getWifiProStateMachineImpl().notifyWifiLinkPoor(false);
                                            boolean unused6 = HwQoEWeChatBooster.this.mIsHandoverToMobile = false;
                                            int unused7 = HwQoEWeChatBooster.this.mIsHandoverRSSI = 0;
                                            break;
                                        }
                                    }
                                } else {
                                    if (HwQoEWeChatBooster.this.mWechatNetwork == 1) {
                                        HwQoEWeChatBooster.this.logD("WeChat phone trun on in wifi");
                                        boolean unused8 = HwQoEWeChatBooster.this.mWeChatHoldWifi = true;
                                        long unused9 = HwQoEWeChatBooster.this.mWeChatStartTime = System.currentTimeMillis();
                                        HwQoEWeChatBooster.this.startWiFiOptimization();
                                    } else if (HwQoEWeChatBooster.this.mWechatNetwork == 0) {
                                        boolean unused10 = HwQoEWeChatBooster.this.mWeChatHoldWifi = false;
                                        HwQoEWeChatBooster.this.logD("WeChat phone trun on in celluar");
                                    }
                                    boolean unused11 = HwQoEWeChatBooster.this.mIsWeCharting = true;
                                    if (HwQoEWeChatBooster.this.isCameraOn()) {
                                        HwQoEWeChatBooster.this.logD("WeChat video call");
                                        int unused12 = HwQoEWeChatBooster.this.mCurrWeChatType = 1;
                                        HwQoEWeChatBooster.this.mHwWifiCHRService.setWeChatScene(1);
                                        if (HwQoEWeChatBooster.this.mWechatNetwork == 0) {
                                            HwQoEWeChatBooster.this.mHidataWechatTraffic.updateMobileWechatStateChanged(1, 1, HwQoEWeChatBooster.this.mWeChartUID);
                                        }
                                    } else {
                                        HwQoEWeChatBooster.this.logD("WeChat audio call");
                                        int unused13 = HwQoEWeChatBooster.this.mCurrWeChatType = 2;
                                        HwQoEWeChatBooster.this.mHwWifiCHRService.setWeChatScene(2);
                                        if (HwQoEWeChatBooster.this.mWechatNetwork == 0) {
                                            HwQoEWeChatBooster.this.mHidataWechatTraffic.updateMobileWechatStateChanged(1, 2, HwQoEWeChatBooster.this.mWeChartUID);
                                        }
                                    }
                                    HwQoEWeChatBooster.this.handleWechatTurnOn(HwQoEWeChatBooster.this.mWechatNetwork);
                                    break;
                                }
                                break;
                            case 5:
                                HwQoEUtils.logD("wechat MSG_WIFI_DELAY_DISCONNECT");
                                HwQoEService.getInstance().disconWiFiNetwork();
                                break;
                            case 6:
                                HwQoEWeChatBooster.this.handleConnectivityNetworkChange();
                                break;
                            case 7:
                                boolean unused14 = HwQoEWeChatBooster.this.mIsUserManualOpenSuccess = false;
                                HwQoEWeChatBooster hwQoEWeChatBooster3 = HwQoEWeChatBooster.this;
                                hwQoEWeChatBooster3.logD("WIFI_STATE_DISABLED: CurrRssiLevel= " + HwQoEWeChatBooster.this.mCurrRssiLevel + " , WeCharting: " + HwQoEWeChatBooster.this.mIsWeCharting);
                                if (HwQoEWeChatBooster.this.mCurrRssiLevel > 2 && HwQoEWeChatBooster.this.mIsWeCharting) {
                                    HwQoEWeChatBooster.this.mHiDataApBlackWhiteListManager.addHandoverBlackList(HwQoEWeChatBooster.this.mCurrSSID, HwQoEWeChatBooster.this.mApAuthType, HwQoEWeChatBooster.this.mCurrWeChatType);
                                    break;
                                }
                            case 8:
                                boolean unused15 = HwQoEWeChatBooster.this.mIsUserManualOpenSuccess = HwQoEWeChatBooster.this.mIsWeCharting;
                                break;
                            default:
                                switch (i) {
                                    case 103:
                                        HwQoEUtils.logD("wechat QOE_MSG_MONITOR_HAVE_INTERNET");
                                        if (HwQoEWeChatBooster.this.isNetworkChecking) {
                                            boolean unused16 = HwQoEWeChatBooster.this.isNetworkChecking = false;
                                            removeMessages(HwQoEUtils.QOE_MSG_WIFI_CHECK_TIMEOUT);
                                        }
                                        if (HwQoEWeChatBooster.this.mIsCheckingRtt) {
                                            HwQoEWeChatBooster.this.handlerCheckResult();
                                            break;
                                        }
                                        break;
                                    case 104:
                                        break;
                                }
                        }
                    } else {
                        HwQoEWeChatBooster.this.onUpdateQualityInfo((long) msg.arg1, (long) msg.arg2);
                    }
                }
                HwQoEUtils.logD("wechat QOE_MSG_MONITOR_NO_INTERNET isNetworkChecking = " + HwQoEWeChatBooster.this.isNetworkChecking + " mIsCheckingRtt = " + HwQoEWeChatBooster.this.mIsCheckingRtt);
                if (HwQoEWeChatBooster.this.isNetworkChecking) {
                    boolean unused17 = HwQoEWeChatBooster.this.isNetworkHaveInternet = false;
                    boolean unused18 = HwQoEWeChatBooster.this.isNetworkChecking = false;
                    HwQoEWeChatBooster.this.handlerNoInternet();
                }
                if (HwQoEWeChatBooster.this.mIsCheckingRtt) {
                    HwQoEWeChatBooster.this.handlerCheckResult();
                }
            }
        };
    }

    public void updateWifiConnectionMode(boolean isUserManualConnect, boolean isUserHandoverWiFi) {
        logD("updateWifiConnectionMode, manualConnect : " + isUserManualConnect + " , handoverWiFi: " + isUserHandoverWiFi);
        this.mIsUserManualConnectSuccess = isUserManualConnect || isUserHandoverWiFi;
    }

    /* access modifiers changed from: private */
    public void onUpdateQualityInfo(long outSpeed, long inSpeed) {
        int targetSpeed;
        int poorTargetNum;
        int type;
        if (this.mIsWeCharting && this.mWechatNetwork == 1) {
            WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
            int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(wifiInfo.getFrequency(), wifiInfo.getRssi());
            HwQoEUtils.logD("HwQoEService: onUpdateQualityInfo  rssiLevel = " + rssiLevel + " wifiInfo.getRssi()" + wifiInfo.getRssi() + " outSpeed = " + outSpeed + " inSpeed = " + inSpeed);
            if (isCameraOn()) {
                targetSpeed = 120;
                type = 1;
                poorTargetNum = 2;
            } else {
                targetSpeed = 30;
                type = 2;
                poorTargetNum = 4;
            }
            setWechatCallType(type);
            if (rssiLevel <= 2 && inSpeed > 0 && inSpeed < ((long) targetSpeed)) {
                HwQoEUtils.logD("HwQoEService: onUpdateQualityInfo  isPoorNetworkNum = " + this.isPoorNetworkNum + " poorTargetNum = " + poorTargetNum);
                this.isPoorNetworkNum = this.isPoorNetworkNum + 1;
                if (this.isPoorNetworkNum >= poorTargetNum) {
                    this.isPoorNetwork = true;
                    startNetworkCheckRtt();
                }
                this.isNetworkHaveInternet = true;
            } else if (inSpeed > ((long) targetSpeed)) {
                this.isPoorNetwork = false;
                this.isNetworkHaveInternet = true;
                this.isPoorNetworkNum = 0;
            } else if (inSpeed == 0) {
                startNetworkChecking();
            }
            if (this.isNetworkHaveInternet && rssiLevel == 2) {
                updateAPPQuality(wifiInfo, inSpeed, type);
            }
        }
    }

    private void setHighPriorityTransmit(int uid, int enable) {
        HwQoEUtils.logD("HwQoEService: setGameKOGHighPriorityTransmit uid: " + uid + " enable: " + enable);
        this.mHwQoEWiFiOptimization.hwQoEHighPriorityTransmit(uid, 17, enable);
    }

    private void setPSAndRetryMode(boolean enable) {
        HwQoEUtils.logD("HwQoEService: setPSAndRetryMode  enable: " + enable);
        if (enable) {
            this.mPhaseTwoIsEnable = true;
            this.mHwQoEWiFiOptimization.hwQoESetMode(5);
            this.mHwQoEWiFiOptimization.hwQoESetMode(4);
            return;
        }
        this.mPhaseTwoIsEnable = false;
        this.mHwQoEWiFiOptimization.hwQoESetMode(3);
    }

    private void setLimitedSpeed(int enable, int mode) {
        this.mHwQoEWiFiOptimization.hwQoELimitedSpeed(enable, mode);
    }

    private void setTXPower(int enable) {
        this.mHwQoEWiFiOptimization.setTXPower(enable);
    }

    /* access modifiers changed from: private */
    public boolean isCameraOn() {
        if (this.mCameraId == null || this.mCameraId.equals("none")) {
            return false;
        }
        return true;
    }

    private boolean getMoblieDateSettings() {
        return getSettingsGlobalBoolean(this.mContext.getContentResolver(), "mobile_data", false);
    }

    private boolean getSettingsGlobalBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.Global.getInt(cr, name, def) == 1;
    }

    private void setAppStateMonitor(int network) {
        if (this.mHwQoEContentAware != null) {
            this.mHwQoEContentAware.setAppStateMonitorEnabled(true, WECHAT_NAME, network);
        }
        if (!this.mHwWeChatBoosterHandler.hasMessages(5)) {
            this.mHwWeChatBoosterHandler.removeMessages(5);
        }
    }

    private void updateAPPQuality(WifiInfo wifiInfo, long inSpeed, int type) {
        HwQoEQualityInfo curRecord = null;
        if (this.mConnectedRecordList == null || inSpeed == 0) {
            HwQoEUtils.logE("updateAPPQuality error");
            return;
        }
        for (HwQoEQualityInfo record : this.mConnectedRecordList) {
            if (record.mRSSI == wifiInfo.getRssi() && record.mAPPType == type) {
                curRecord = record;
            }
        }
        if (curRecord != null) {
            HwQoEUtils.logD("curRecord.mThoughtput = " + curRecord.mThoughtput + " inSpeed = " + inSpeed);
            curRecord.mThoughtput = ((curRecord.mThoughtput * 8) / 10) + ((2 * inSpeed) / 10);
            StringBuilder sb = new StringBuilder();
            sb.append("curRecord.mThoughtput = ");
            sb.append(curRecord.mThoughtput);
            HwQoEUtils.logD(sb.toString());
        } else {
            HwQoEUtils.logD("updateAPPQuality add record");
            HwQoEQualityInfo curRecord2 = new HwQoEQualityInfo();
            curRecord2.mBSSID = wifiInfo.getBSSID();
            curRecord2.mRSSI = wifiInfo.getRssi();
            curRecord2.mAPPType = type;
            curRecord2.mThoughtput = inSpeed;
            this.mConnectedRecordList.add(curRecord2);
        }
    }

    /* access modifiers changed from: private */
    public void updateAppQualityRecords(List<HwQoEQualityInfo> records) {
        if (records != null && records.size() != 0) {
            for (HwQoEQualityInfo record : records) {
                this.mHwQoEQualityManager.addOrUpdateAppQualityRcd(record);
            }
        }
    }

    private void startNetworkChecking() {
        HwQoEUtils.logD("startNetworkChecking  isNetworkChecking = " + this.isNetworkChecking + " isNetworkHaveInternet= " + this.isNetworkHaveInternet);
        if (!this.isNetworkChecking && this.isNetworkHaveInternet) {
            this.isNetworkHaveInternet = false;
            this.isNetworkChecking = true;
            new HwQoENetworkChecker(this.mContext, this.mHwWeChatBoosterHandler).start();
            this.mHwWeChatBoosterHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_CHECK_TIMEOUT, WifiHandover.HANDOVER_WAIT_SCAN_TIME_OUT);
        }
    }

    /* access modifiers changed from: private */
    public void handlerNoInternet() {
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(wifiInfo.getFrequency(), wifiInfo.getRssi());
        HwQoEUtils.logD("handlerNoInternet rssiLevel = " + rssiLevel);
        this.mIsHandoverToMobile = true;
        WifiProStateMachine.getWifiProStateMachineImpl().notifyWifiLinkPoor(true);
    }

    /* access modifiers changed from: private */
    public void handleConnectivityNetworkChange() {
        this.mActiveNetworkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        if (this.mActiveNetworkInfo != null && this.mActiveNetworkInfo.getType() == 0 && this.mActiveNetworkInfo.isConnected()) {
            this.mWechatNetwork = 0;
            logD("TYPE_MOBILE is Connected ,WeCharting:" + this.mIsWeCharting);
            setAppStateMonitor(this.mWechatNetwork);
            if (this.mIsWeCharting) {
                this.mHidataWechatTraffic.updateMobileWechatStateChanged(1, this.mCurrWeChatType, this.mWeChartUID);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleWifiRssiChanged() {
        this.mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (this.mWifiInfo != null) {
            this.mCurrRssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.mWifiInfo.getFrequency(), this.mWifiInfo.getRssi());
            HwQoEUtils.logD("rssi changed,rssiLevel = " + this.mCurrRssiLevel);
            if (this.mIsWeCharting && this.mWechatNetwork == 1) {
                if (this.mCurrRssiLevel <= 2 && !this.mPhaseTwoIsEnable) {
                    setPSAndRetryMode(true);
                    setTXPower(1);
                }
                if (this.mCurrRssiLevel >= 4 && this.mPhaseTwoIsEnable) {
                    setPSAndRetryMode(false);
                    setTXPower(0);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleWifiConnected() {
        logD("handleWifiConnected, ManualConnect:" + this.mIsUserManualConnectSuccess + ",WeCharting:" + this.mIsWeCharting + ", ManualOpen: " + this.mIsUserManualOpenSuccess);
        this.mWechatNetwork = 1;
        setAppStateMonitor(this.mWechatNetwork);
        this.mApAuthType = this.mHiDataUtilsManager.isPublicAP() ? 1 : 0;
        this.mWifiInfo = this.mWifiManager.getConnectionInfo();
        if (this.mWifiInfo != null) {
            this.mCurrRssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.mWifiInfo.getFrequency(), this.mWifiInfo.getRssi());
            this.mCurrSSID = this.mWifiInfo.getSSID();
            getAllWeChatData();
        }
        this.isPoorNetwork = false;
        if (this.mIsWeCharting && (this.mIsUserManualConnectSuccess || this.mIsUserManualOpenSuccess)) {
            this.mHiDataApBlackWhiteListManager.addHandoverWhiteList(this.mCurrSSID, this.mApAuthType, this.mCurrWeChatType);
        }
        if (this.mIsWeCharting) {
            this.mHidataWechatTraffic.updateMobileWechatStateChanged(0, this.mCurrWeChatType, this.mWeChartUID);
        }
    }

    /* access modifiers changed from: private */
    public void handleWechatTurnOn(int network) {
        int i = 1;
        if (network == 1) {
            this.mApAuthType = this.mHiDataUtilsManager.isPublicAP() ? 1 : 0;
            if (!this.mHidataWechatTraffic.wechatTrafficWealthy(this.mCurrWeChatType)) {
                i = 0;
            }
            this.mUserType = i;
            this.mApBlackType = this.mHiDataApBlackWhiteListManager.getApBlackType(this.mCurrSSID, this.mApAuthType, this.mCurrWeChatType, this.mUserType);
            logD("handleWechatTurnOn, mApAuthType = " + this.mApAuthType + ", mUserType = " + this.mUserType + ", mApBlackType = " + this.mApBlackType);
        }
    }

    /* access modifiers changed from: private */
    public void handleWifiDisConnected() {
        this.mWifiInfo = null;
        this.isPoorNetwork = false;
        logD("handleWifiDisConnected  mIsWeCharting = " + this.mIsWeCharting + " mWechatNetwork = " + this.mWechatNetwork);
        this.mWeChatHoldWifi = false;
        if (this.mIsWeCharting && this.mWechatNetwork == 1) {
            setHighPriorityTransmit(this.mWeChartUID, 0);
            setLimitedSpeed(0, 0);
            if (this.mPhaseTwoIsEnable) {
                setPSAndRetryMode(false);
                setTXPower(0);
            }
            this.mWechatNetwork = -1;
            updateAppQualityRecords(this.mConnectedRecordList);
            this.mConnectedRecordList = null;
        }
        if (getMoblieDateSettings()) {
            this.mWechatNetwork = 0;
        } else {
            this.mHwQoEContentAware.setAppStateMonitorEnabled(false, WECHAT_NAME, -1);
        }
    }

    private void getAllWeChatData() {
        List<HwQoEQualityInfo> videoData = this.mHwQoEQualityManager.getAppQualityAllRcd(this.mWifiInfo.getBSSID(), 1);
        List<HwQoEQualityInfo> audioData = this.mHwQoEQualityManager.getAppQualityAllRcd(this.mWifiInfo.getBSSID(), 2);
        this.mConnectedRecordList = videoData;
        this.mConnectedRecordList.addAll(audioData);
    }

    private void setWechatCallType(int type) {
        if (!(this.mCurrWeChatType == type || type == 0)) {
            HwQoEUtils.logD("setWechatCallType mCurrWeChatType = " + this.mCurrWeChatType);
        }
        this.mCurrWeChatType = type;
    }

    private void startNetworkCheckRtt() {
        HwQoEUtils.logD("startNetworkChecking  isNetworkChecking = " + this.isNetworkChecking + " isNetworkHaveInternet= " + this.isNetworkHaveInternet);
        this.mCheckStartTime = System.currentTimeMillis();
        HwQoENetworkChecker mHwQoENetworkChecker = new HwQoENetworkChecker(this.mContext, this.mHwWeChatBoosterHandler);
        this.mIsCheckingRtt = true;
        mHwQoENetworkChecker.start();
        this.mHwWeChatBoosterHandler.sendEmptyMessageDelayed(HwQoEUtils.QOE_MSG_WIFI_CHECK_TIMEOUT, 1000);
    }

    /* access modifiers changed from: private */
    public void handlerCheckResult() {
        long time = System.currentTimeMillis() - this.mCheckStartTime;
        long arpTime = this.mHiDataUtilsManager.checkARPRTT();
        if (this.isPoorNetwork) {
            if (time > 300) {
                HwQoEUtils.logD("network is bad mIsVerifyState = " + this.mIsVerifyState);
                if (this.mHiDataUtilsManager.isMobileNetworkReady(this.mCurrWeChatType) && !this.mIsVerifyState) {
                    this.mWifiInfo = this.mWifiManager.getConnectionInfo();
                    if (this.mWifiInfo != null) {
                        this.mIsHandoverRSSI = this.mWifiInfo.getRssi();
                    }
                    this.mIsHandoverToMobile = true;
                    WifiProStateMachine.getWifiProStateMachineImpl().notifyWifiLinkPoor(true);
                }
            }
            this.isPoorNetwork = false;
            this.isPoorNetworkNum = 0;
        }
        this.mIsCheckingRtt = false;
        this.mCheckStartTime = 0;
        HwQoEUtils.logD("handlerCheckResult time = " + time + " arpTime = " + arpTime);
    }

    public void onSensitiveAppStateChange(int uid, int state, boolean isBackground) {
        this.mHwWeChatBoosterHandler.sendMessage(this.mHwWeChatBoosterHandler.obtainMessage(4, uid, state, Boolean.valueOf(isBackground)));
    }

    public void onPeriodSpeed(long outSpeed, long inSpeed) {
        this.mHwWeChatBoosterHandler.sendMessage(this.mHwWeChatBoosterHandler.obtainMessage(HwQoEUtils.QOE_MSG_UPDATE_QUALITY_INFO, (int) outSpeed, (int) inSpeed, null));
    }

    public boolean isConnectWhenWeChating(ScanResult scanResult) {
        int type;
        int targetSpeed;
        int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(scanResult.frequency, scanResult.level);
        HwQoEUtils.logD("isWeChating mIsWeCharting = " + this.mIsWeCharting + " scanResult.level = " + scanResult.level + "  mIsHandoverRSSI = " + this.mIsHandoverRSSI);
        if (!this.mIsWeCharting) {
            return true;
        }
        if (rssiLevel <= 1) {
            return false;
        }
        if (!this.mIsHandoverToMobile || this.mIsHandoverRSSI == 0) {
            if (rssiLevel >= 3) {
                return true;
            }
            if (isCameraOn()) {
                targetSpeed = 120;
                type = 1;
            } else {
                targetSpeed = 30;
                type = 2;
            }
            for (HwQoEQualityInfo record : this.mHwQoEQualityManager.getAppQualityAllRcd(scanResult.BSSID, type)) {
                HwQoEUtils.logD("record.mRSSI = " + record.mRSSI + " record.mThoughtput = " + record.mThoughtput);
                if (record.mRSSI <= scanResult.level && record.mThoughtput > ((long) (targetSpeed + 20))) {
                    return true;
                }
            }
            return false;
        } else if (scanResult.level >= this.mIsHandoverRSSI + 10) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isWeChating() {
        boolean result = this.mIsWeCharting;
        HwQoEUtils.logD("isWeChating result = " + result + " mIsWeCharting = " + this.mIsWeCharting + " mIsHandoverToMobile = " + this.mIsHandoverToMobile);
        return result;
    }

    public boolean isHandoverToMobile() {
        return this.mIsHandoverToMobile;
    }

    /* access modifiers changed from: private */
    public void startWiFiOptimization() {
        WifiInfo info = this.mWifiManager.getConnectionInfo();
        int currRssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(info.getFrequency(), info.getRssi());
        setHighPriorityTransmit(this.mWeChartUID, 1);
        setLimitedSpeed(1, 6);
        if (currRssiLevel <= 2) {
            HwQoEUtils.logD("wechat video on mCurrRssiLevel <=2");
            setPSAndRetryMode(true);
            setTXPower(1);
        }
    }

    /* access modifiers changed from: private */
    public void stopWiFiOptimization() {
        setHighPriorityTransmit(this.mWeChartUID, 0);
        setLimitedSpeed(0, 0);
        if (this.mPhaseTwoIsEnable) {
            setPSAndRetryMode(false);
            setTXPower(0);
        }
    }

    /* access modifiers changed from: private */
    public void logD(String info) {
        Log.d(TAG, info);
    }
}
