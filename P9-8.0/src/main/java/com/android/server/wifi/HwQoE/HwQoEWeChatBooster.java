package com.android.server.wifi.HwQoE;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraManager.AvailabilityCallback;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import com.android.server.wifi.HwWifiStatStore;
import com.android.server.wifi.HwWifiStatStoreImpl;

public class HwQoEWeChatBooster {
    public static final int KOG_UDP_TYPE = 17;
    private static final int MSG_APP_STATE_CHANHED = 4;
    private static final int MSG_WIFI_CONNECTED = 2;
    private static final int MSG_WIFI_DISCONNECTED = 3;
    private static final int MSG_WIFI_RSSI_CHANHED = 1;
    public static final int WECHART_DISABLE_FUNC = 0;
    public static final int WECHART_ENABLE_FUNC = 1;
    private static final int WECHART_PW_INIT = 5;
    private static final int WECHART_PW_START = 4;
    private static final int WECHART_PW_STOP = 3;
    private static final String WECHAT_NAME = "com.tencent.mm";
    private static HwQoEWeChatBooster mHwQoEWeChatBooster;
    private AvailabilityCallback mAvailabilityCallback = new AvailabilityCallback() {
        public void onCameraAvailable(String cameraId) {
            HwQoEUtils.logD("onCameraAvailable cameraId = " + cameraId);
            if (cameraId.equals(HwQoEWeChatBooster.this.mCameraId)) {
                HwQoEWeChatBooster.this.mCameraId = "none";
            }
        }

        public void onCameraUnavailable(String cameraId) {
            HwQoEUtils.logD("onCameraUnavailable cameraId = " + cameraId);
            HwQoEWeChatBooster.this.mCameraId = cameraId;
        }
    };
    private BroadcastReceiver mBroadcastReceiver;
    private String mCameraId = "none";
    private CameraManager mCameraManager;
    private Context mContext;
    private HwQoEContentAware mHwQoEContentAware;
    private HwQoEWiFiOptimization mHwQoEWiFiOptimization;
    private Handler mHwWeChatBoosterHandler;
    private HwWifiStatStore mHwWifiStatStore;
    private IntentFilter mIntentFilter;
    private boolean mIsWeCharting = false;
    private NetworkInfo mNetworkInfo;
    private boolean mPhaseTwoIsEnable = false;
    private int mWeChartUID = -1;
    private WifiInfo mWifiInfo;
    private WifiManager mWifiManager;

    public static HwQoEWeChatBooster createInstance(Context context) {
        if (mHwQoEWeChatBooster == null) {
            mHwQoEWeChatBooster = new HwQoEWeChatBooster(context);
        }
        return mHwQoEWeChatBooster;
    }

    private HwQoEWeChatBooster(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        initHwWeChatBoosterHandler();
        registerBroadcastReceiver();
        this.mHwQoEWiFiOptimization = HwQoEWiFiOptimization.getInstance(this.mContext);
        this.mHwWifiStatStore = HwWifiStatStoreImpl.getDefault();
        this.mCameraManager = (CameraManager) this.mContext.getSystemService("camera");
        this.mCameraManager.registerAvailabilityCallback(this.mAvailabilityCallback, null);
        this.mHwQoEContentAware = HwQoEContentAware.getInstance();
    }

    private void registerBroadcastReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    HwQoEWeChatBooster.this.mNetworkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (HwQoEWeChatBooster.this.mNetworkInfo == null) {
                        return;
                    }
                    if (HwQoEWeChatBooster.this.mNetworkInfo.getState() == State.DISCONNECTED) {
                        HwQoEWeChatBooster.this.mHwWeChatBoosterHandler.sendEmptyMessage(3);
                    } else if (HwQoEWeChatBooster.this.mNetworkInfo.getState() == State.CONNECTED) {
                        HwQoEWeChatBooster.this.mHwWeChatBoosterHandler.sendEmptyMessage(2);
                    } else if (HwQoEWeChatBooster.this.mNetworkInfo.getDetailedState() == DetailedState.VERIFYING_POOR_LINK) {
                        HwQoEWeChatBooster.this.mHwWeChatBoosterHandler.sendEmptyMessage(3);
                    }
                } else if ("android.net.wifi.RSSI_CHANGED".equals(action)) {
                    HwQoEWeChatBooster.this.mHwWeChatBoosterHandler.sendEmptyMessage(1);
                }
            }
        };
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
    }

    private void initHwWeChatBoosterHandler() {
        HandlerThread handlerThread = new HandlerThread("hw_wechatbooster_handler_thread");
        handlerThread.start();
        this.mHwWeChatBoosterHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                int rssiLevel;
                switch (msg.what) {
                    case 1:
                        if (HwQoEWeChatBooster.this.mWifiInfo != null) {
                            rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(HwQoEWeChatBooster.this.mWifiInfo.getRssi());
                            HwQoEUtils.logD("rssi changed,rssiLevel = " + rssiLevel);
                            if (HwQoEWeChatBooster.this.mIsWeCharting) {
                                if (rssiLevel <= 2 && (HwQoEWeChatBooster.this.mPhaseTwoIsEnable ^ 1) != 0) {
                                    HwQoEWeChatBooster.this.setPSAndRetryMode(true);
                                    HwQoEWeChatBooster.this.setTXPower(1);
                                    HwQoEWeChatBooster.this.mHwWifiStatStore.updataWeChartStatic(0, 1, 0, 0, 0);
                                }
                                if (rssiLevel >= 4 && HwQoEWeChatBooster.this.mPhaseTwoIsEnable) {
                                    HwQoEWeChatBooster.this.setPSAndRetryMode(false);
                                    HwQoEWeChatBooster.this.setTXPower(0);
                                    return;
                                }
                                return;
                            }
                            return;
                        }
                        return;
                    case 2:
                        if (HwQoEWeChatBooster.this.mHwQoEContentAware != null) {
                            HwQoEWeChatBooster.this.mHwQoEContentAware.setAppStateMonitorEnabled(true, HwQoEWeChatBooster.WECHAT_NAME, 1);
                            return;
                        }
                        return;
                    case 3:
                        HwQoEWeChatBooster.this.mWifiInfo = null;
                        if (HwQoEWeChatBooster.this.mHwQoEContentAware != null) {
                            HwQoEWeChatBooster.this.mHwQoEContentAware.setAppStateMonitorEnabled(false, HwQoEWeChatBooster.WECHAT_NAME, 1);
                        }
                        if (HwQoEWeChatBooster.this.mIsWeCharting) {
                            HwQoEWeChatBooster.this.setHighPriorityTransmit(HwQoEWeChatBooster.this.mWeChartUID, 0);
                            HwQoEWeChatBooster.this.setLimitedSpeed(0);
                            if (HwQoEWeChatBooster.this.mPhaseTwoIsEnable) {
                                HwQoEWeChatBooster.this.setPSAndRetryMode(false);
                                HwQoEWeChatBooster.this.setTXPower(0);
                            }
                        }
                        HwQoEWeChatBooster.this.mWeChartUID = -1;
                        return;
                    case 4:
                        int uid = msg.arg1;
                        int state = msg.arg2;
                        boolean isBackground = ((Boolean) msg.obj).booleanValue();
                        HwQoEWeChatBooster.this.mWeChartUID = uid;
                        if (1 == state) {
                            HwQoEUtils.logD("wechat video on ");
                            HwQoEWeChatBooster.this.mWifiInfo = HwQoEWeChatBooster.this.mWifiManager.getConnectionInfo();
                            rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(HwQoEWeChatBooster.this.mWifiInfo.getRssi());
                            HwQoEWeChatBooster.this.setHighPriorityTransmit(HwQoEWeChatBooster.this.mWeChartUID, 1);
                            HwQoEWeChatBooster.this.setLimitedSpeed(1);
                            HwQoEWeChatBooster.this.mHwWifiStatStore.updataWeChartStatic(1, 0, 0, 0, 0);
                            if (rssiLevel <= 2) {
                                HwQoEUtils.logD("wechat video on rssiLevel <=2");
                                HwQoEWeChatBooster.this.setPSAndRetryMode(true);
                                HwQoEWeChatBooster.this.setTXPower(1);
                                HwQoEWeChatBooster.this.mHwWifiStatStore.updataWeChartStatic(0, 0, 0, 0, 1);
                            }
                            HwQoEWeChatBooster.this.mIsWeCharting = true;
                            if (HwQoEWeChatBooster.this.isCameraOn()) {
                                HwQoEUtils.logD("wechat video on Camera On");
                                HwQoEWeChatBooster.this.mHwWifiStatStore.updataWeChartStatic(0, 1, 0, 0, 0);
                                HwQoEWeChatBooster.this.mHwWifiStatStore.setWeChatScene(1);
                                return;
                            }
                            HwQoEWeChatBooster.this.mHwWifiStatStore.setWeChatScene(2);
                            return;
                        } else if (state == 0) {
                            HwQoEUtils.logD("wechat video off  ");
                            HwQoEWeChatBooster.this.setHighPriorityTransmit(HwQoEWeChatBooster.this.mWeChartUID, 0);
                            HwQoEWeChatBooster.this.setLimitedSpeed(0);
                            if (HwQoEWeChatBooster.this.mPhaseTwoIsEnable) {
                                HwQoEWeChatBooster.this.setPSAndRetryMode(false);
                                HwQoEWeChatBooster.this.setTXPower(0);
                            }
                            HwQoEWeChatBooster.this.mIsWeCharting = false;
                            if (isBackground) {
                                HwQoEUtils.logD("wechat video off  isBackground");
                                HwQoEWeChatBooster.this.mHwWifiStatStore.updataWeChartStatic(0, 0, 0, 1, 0);
                            }
                            HwQoEWeChatBooster.this.mHwWifiStatStore.setWeChatScene(0);
                            return;
                        } else {
                            return;
                        }
                    default:
                        return;
                }
            }
        };
    }

    public void onSensitiveAppStateChange(int uid, int state, boolean isBackground) {
        this.mHwWeChatBoosterHandler.sendMessage(this.mHwWeChatBoosterHandler.obtainMessage(4, uid, state, Boolean.valueOf(isBackground)));
    }

    public void onPeriodSpeed(long speed) {
        int rssiLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(this.mWifiManager.getConnectionInfo().getRssi());
        int targetSpeed;
        if (isCameraOn()) {
            targetSpeed = 64000;
        } else {
            targetSpeed = 7680;
        }
        if (rssiLevel <= 2 && speed < ((long) targetSpeed) && speed > 0) {
            this.mHwWifiStatStore.updataWeChartStatic(0, 0, 1, 0, 0);
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
            this.mHwQoEWiFiOptimization.hwQoEAdjustSpeed(5);
            this.mHwQoEWiFiOptimization.hwQoEAdjustSpeed(4);
            return;
        }
        this.mPhaseTwoIsEnable = false;
        this.mHwQoEWiFiOptimization.hwQoEAdjustSpeed(3);
    }

    private void setLimitedSpeed(int enable) {
        this.mHwQoEWiFiOptimization.hwQoELimitedSpeed(enable);
    }

    private void setTXPower(int enable) {
        this.mHwQoEWiFiOptimization.setTXPower(enable);
    }

    private boolean isCameraOn() {
        if (this.mCameraId == null || this.mCameraId.equals("none")) {
            return false;
        }
        return true;
    }
}
