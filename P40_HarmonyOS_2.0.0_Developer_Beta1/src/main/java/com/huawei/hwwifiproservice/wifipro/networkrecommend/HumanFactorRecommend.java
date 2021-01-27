package com.huawei.hwwifiproservice.wifipro.networkrecommend;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.database.ContentObserver;
import android.net.booster.IHwCommBoosterCallback;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.hwwifiproservice.WifiProStateMachine;

public class HumanFactorRecommend {
    private static final int BOOSTER_DATA_TYPE_HUMAN_FACTOR = 9;
    private static final int HUMAN_FACTOR_DATA_TRAFFIC_ECONOMIC = 3;
    private static final int HUMAN_FACTOR_DATA_TRAFFIC_NORMAL = 2;
    private static final int HUMAN_FACTOR_DATA_TRAFFIC_RICH = 1;
    private static final long INVALID_LAST_TOAST_TIMESTAMP = -1;
    private static final String LAST_TOAST_TIMESTAMPS = "last_toast_timestamps";
    private static final String LOG_TAG = "WiFi_PROHumanFactorRecommend";
    private static final int MESSAGE_REGISTER_HUMAN_FACTOR = 1;
    private static final long MIN_MP_WIFI_PRO_TOAST_INTERVAL = 120000;
    private static final String MY_PACKAGE_NAME = "com.huawei.hwwifiproservice.humanfactor";
    private static final long NORMAL_TRAFFIC_TOAST_INTERVAL = 7200000;
    private static final int REGISTER_HUMAN_FACTOR_DATA_TYPE = 1201;
    private static final int REGISTER_HUMAN_FACTOR_DELAY_TIME = 10000;
    private static final int REGISTER_HUMAN_FACTOR_MAX_TIME = 6;
    private static final int REGISTER_HUMAN_FACTOR_TYPE_OFF = 0;
    private static final int REGISTER_HUMAN_FACTOR_TYPE_ON = 1;
    private static final long RICH_TRAFFIC_TOAST_INTERVAL = 21600000;
    private static HumanFactorRecommend sInstance = null;
    private Context mContext = null;
    private Handler mHandler = null;
    private IHwCommBoosterCallback mHwCommBoosterCallback = new IHwCommBoosterCallback.Stub() {
        /* class com.huawei.hwwifiproservice.wifipro.networkrecommend.HumanFactorRecommend.AnonymousClass1 */

        public void callBack(int type, Bundle bundle) {
            HumanFactorRecommend humanFactorRecommend = HumanFactorRecommend.this;
            humanFactorRecommend.logE("callBack, receive booster callback type " + type);
            if (bundle == null) {
                HumanFactorRecommend.this.logE("callBack, data is null");
            } else if (type != 9) {
                HumanFactorRecommend humanFactorRecommend2 = HumanFactorRecommend.this;
                humanFactorRecommend2.logE("callBack, unexpected event type=" + type);
            } else {
                HumanFactorRecommend.this.handleHumanFactor(bundle);
            }
        }
    };
    private long mMpToastTimestamp = INVALID_LAST_TOAST_TIMESTAMP;
    private volatile int mRegisterHumanFactorTime = 0;
    private int mUserTypeByDataTraffic = 2;
    private long mWlanproToastTimestamp = INVALID_LAST_TOAST_TIMESTAMP;

    private HumanFactorRecommend() {
    }

    public void init(Handler handler, Context context) {
        this.mContext = context;
        if (handler != null) {
            this.mHandler = new LocalHandler(handler.getLooper());
        } else {
            logE("init, handler is null");
        }
        registerForMpToastChange();
        registerForWifiProToastChange();
    }

    public static HumanFactorRecommend getInstance() {
        if (sInstance == null) {
            sInstance = new HumanFactorRecommend();
        }
        return sInstance;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logI(String info) {
        Log.i(LOG_TAG, info);
    }

    private void logD(String info) {
        Log.d(LOG_TAG, info);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logE(String info) {
        Log.e(LOG_TAG, info);
    }

    public boolean isRecommendWiFi2Cell() {
        return false;
    }

    public boolean isRecommendCell2WiFi() {
        return false;
    }

    public boolean isRecommendShowWifiToCellToast() {
        WifiProStateMachine wifiPro = WifiProStateMachine.getWifiProStateMachineImpl();
        if (wifiPro == null) {
            logE("isRecommendShowWifiToCellToast, wifiPro is null");
            return true;
        } else if (!wifiPro.isWiFiProEnabled()) {
            logI("isRecommendShowWifiToCellToast, wifi pro is shut down, don't show toast.");
            return false;
        } else {
            long interval = getToastInterval();
            logI("isRecommendShowWifiToCellToast, showWifiToMobileToast, interval=" + interval);
            if (interval == INVALID_LAST_TOAST_TIMESTAMP) {
                return true;
            }
            if (interval < MIN_MP_WIFI_PRO_TOAST_INTERVAL) {
                logI("isRecommendShowWifiToCellToast, wifi pro or mp show toast in 2 minutes, don't show toast again.");
                return false;
            } else if (this.mUserTypeByDataTraffic == 1 && interval < RICH_TRAFFIC_TOAST_INTERVAL) {
                logI("isRecommendShowWifiToCellToast, rich traffic user, don't show toast.");
                return false;
            } else if (this.mUserTypeByDataTraffic != 2 || interval >= NORMAL_TRAFFIC_TOAST_INTERVAL) {
                return true;
            } else {
                logI("isRecommendShowWifiToCellToast, normal traffic user, don't show toast.");
                return false;
            }
        }
    }

    public void registerBoosterService() {
        registerBoosterCallback();
        if (this.mHandler.hasMessages(1)) {
            logI("registerBoosterHumanFactor, WiFi construct and booster broadcast conflict, ignore");
            return;
        }
        this.mRegisterHumanFactorTime = 0;
        registerBoosterHumanFactor();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerBoosterHumanFactor() {
        if (this.mHandler == null) {
            logE("registerBoosterHumanFactor, mHandler is null");
        } else if (this.mRegisterHumanFactorTime == 0) {
            logI("registerBoosterHumanFactor, first registerBoosterHumanFactor delay 10 second");
            this.mRegisterHumanFactorTime++;
            Handler handler = this.mHandler;
            if (handler != null) {
                this.mHandler.sendMessageDelayed(handler.obtainMessage(1), 10000);
                return;
            }
            logE("registerBoosterHumanFactor, mHandler is null");
        } else {
            IHwCommBoosterServiceManager manager = HwFrameworkFactory.getHwCommBoosterServiceManager();
            if (manager == null) {
                logE("registerBoosterHumanFactor, manager is null");
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putInt("listenType", 1);
            int result = manager.reportBoosterPara(MY_PACKAGE_NAME, (int) REGISTER_HUMAN_FACTOR_DATA_TYPE, bundle);
            if (result == 0) {
                logI("registerBoosterHumanFactor success, mRegisterHumanFactorTime=" + this.mRegisterHumanFactorTime);
                return;
            }
            logI("registerBoosterHumanFactor result=" + result + "mRegisterHumanFactorTime=" + this.mRegisterHumanFactorTime);
            if (this.mRegisterHumanFactorTime >= 6) {
                logE("registerBoosterHumanFactor fail");
                return;
            }
            this.mRegisterHumanFactorTime++;
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), 10000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHumanFactor(Bundle bundle) {
        if (bundle == null) {
            logE("handleHumanFactor, bundle is null");
            return;
        }
        int phoneId = bundle.getInt("phoneId");
        int userType = bundle.getInt("type");
        if (userType == 1 || userType == 2 || userType == 3) {
            int masterSlot = WifiProCommonUtils.getMasterCardSlotId();
            logI("handleHumanFactor, phoneId=" + phoneId + ", masterSlot=" + masterSlot + ", userType=" + userType);
            if (masterSlot == phoneId && this.mUserTypeByDataTraffic != userType) {
                logI("handleHumanFactor, old type=" + this.mUserTypeByDataTraffic + ", new type=" + userType);
                this.mUserTypeByDataTraffic = userType;
                return;
            }
            return;
        }
        logE("handleHumanFactor, phoneId=" + phoneId + ", invalid userType=" + userType);
    }

    private long getToastInterval() {
        long mpInterval = getMpToastInterval();
        long wifiProInterval = getWifiProToastInterval();
        if (mpInterval == INVALID_LAST_TOAST_TIMESTAMP || wifiProInterval == INVALID_LAST_TOAST_TIMESTAMP) {
            if (mpInterval != INVALID_LAST_TOAST_TIMESTAMP) {
                return mpInterval;
            }
            if (wifiProInterval != INVALID_LAST_TOAST_TIMESTAMP) {
                return wifiProInterval;
            }
            logI("getToastInterval, never toast");
            return INVALID_LAST_TOAST_TIMESTAMP;
        } else if (wifiProInterval < mpInterval) {
            return wifiProInterval;
        } else {
            return mpInterval;
        }
    }

    private void registerBoosterCallback() {
        IHwCommBoosterServiceManager manager = HwFrameworkFactory.getHwCommBoosterServiceManager();
        if (manager == null) {
            logE("registerBoosterCallback, HwCommBoosterServiceManager is null");
            return;
        }
        int result = manager.registerCallBack(MY_PACKAGE_NAME, this.mHwCommBoosterCallback);
        if (result != 0) {
            logE("registerBoosterCallback, failed, ret=" + result);
            return;
        }
        logI("registerBoosterCallback, success");
    }

    private long getWifiProToastInterval() {
        if (this.mWlanproToastTimestamp == INVALID_LAST_TOAST_TIMESTAMP) {
            return INVALID_LAST_TOAST_TIMESTAMP;
        }
        return SystemClock.elapsedRealtime() - this.mWlanproToastTimestamp;
    }

    private long getMpToastInterval() {
        if (this.mMpToastTimestamp == INVALID_LAST_TOAST_TIMESTAMP) {
            return INVALID_LAST_TOAST_TIMESTAMP;
        }
        return SystemClock.elapsedRealtime() - this.mMpToastTimestamp;
    }

    private void registerForMpToastChange() {
        if (this.mContext == null) {
            logE("registerForMpToastChange, mContext is null");
            return;
        }
        Handler handler = this.mHandler;
        if (handler == null) {
            logE("registerForMpToastChange, mHandler is null");
            return;
        }
        ContentObserver contentObserver = new ContentObserver(handler) {
            /* class com.huawei.hwwifiproservice.wifipro.networkrecommend.HumanFactorRecommend.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HumanFactorRecommend humanFactorRecommend = HumanFactorRecommend.this;
                humanFactorRecommend.mMpToastTimestamp = Settings.System.getLong(humanFactorRecommend.mContext.getContentResolver(), HumanFactorRecommend.LAST_TOAST_TIMESTAMPS, HumanFactorRecommend.INVALID_LAST_TOAST_TIMESTAMP);
                HumanFactorRecommend humanFactorRecommend2 = HumanFactorRecommend.this;
                humanFactorRecommend2.logI("onChange, mMpToastTimestamp=" + HumanFactorRecommend.this.mMpToastTimestamp);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(LAST_TOAST_TIMESTAMPS), false, contentObserver);
    }

    private void registerForWifiProToastChange() {
        if (this.mContext == null) {
            logE("registerForWifiProToastChange, mContext is null");
            return;
        }
        Handler handler = this.mHandler;
        if (handler == null) {
            logE("registerForWifiProToastChange, mHandler is null");
            return;
        }
        ContentObserver contentObserver = new ContentObserver(handler) {
            /* class com.huawei.hwwifiproservice.wifipro.networkrecommend.HumanFactorRecommend.AnonymousClass3 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HumanFactorRecommend humanFactorRecommend = HumanFactorRecommend.this;
                humanFactorRecommend.mWlanproToastTimestamp = Settings.System.getLong(humanFactorRecommend.mContext.getContentResolver(), "last_wifipro_toast_timestamps", HumanFactorRecommend.INVALID_LAST_TOAST_TIMESTAMP);
                HumanFactorRecommend humanFactorRecommend2 = HumanFactorRecommend.this;
                humanFactorRecommend2.logI("onChange, mWlanproToastTimestamp=" + HumanFactorRecommend.this.mWlanproToastTimestamp);
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("last_wifipro_toast_timestamps"), false, contentObserver);
    }

    /* access modifiers changed from: private */
    public class LocalHandler extends Handler {
        public LocalHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (message == null) {
                Log.e(HumanFactorRecommend.LOG_TAG, "handleMessage, message is null");
                return;
            }
            Log.i(HumanFactorRecommend.LOG_TAG, "handleMessage, message.what=" + message.what);
            if (message.what == 1) {
                HumanFactorRecommend.this.registerBoosterHumanFactor();
            }
        }
    }
}
