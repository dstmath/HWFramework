package com.android.server.hidata.appqoe;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.Log;
import com.android.server.hidata.HwHidataAppStateInfo;
import com.android.server.hidata.IHwHidataCallback;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.android.net.hwmplink.MpLinkCommonUtils;
import java.util.ArrayList;
import java.util.Iterator;

public class HwAPPQoEManager {
    private static final int GAMEQOE_CALLBACKLIST_MAX_SIZE = 10;
    private static final String TAG = "HiData_HwAPPQoEManager";
    private static final int USER_HANDOVER_TO_WIFI = 9;
    private static HwAPPQoEManager mHwAPPQoEManager = null;
    IHwAPPQoECallback mBrainCallback = null;
    private Context mContext;
    private ArrayList<IHwHidataCallback> mGameQoeCallbackList = new ArrayList<>();
    private HwAPPQoEStateMachine mHwAPPQoEStateMachine = null;
    private final Object mLock = new Object();
    IHwAPPQoECallback mWaveMappingCallback = null;

    private HwAPPQoEManager(Context context) {
        this.mContext = context;
        this.mHwAPPQoEStateMachine = HwAPPQoEStateMachine.createHwAPPQoEStateMachine(context);
    }

    public static HwAPPQoEManager createHwAPPQoEManager(Context context) {
        if (mHwAPPQoEManager == null) {
            mHwAPPQoEManager = new HwAPPQoEManager(context);
        }
        return mHwAPPQoEManager;
    }

    public static HwAPPQoEManager getInstance() {
        HwAPPQoEManager hwAPPQoEManager = mHwAPPQoEManager;
        if (hwAPPQoEManager != null) {
            return hwAPPQoEManager;
        }
        return null;
    }

    public void registerAppQoECallback(IHwAPPQoECallback callback, boolean isBrain) {
        if (isBrain) {
            this.mBrainCallback = callback;
        } else {
            this.mWaveMappingCallback = callback;
        }
    }

    public IHwAPPQoECallback getAPPQoECallback(boolean isBrain) {
        if (isBrain) {
            return this.mBrainCallback;
        }
        return this.mWaveMappingCallback;
    }

    public HwAPPStateInfo getCurAPPStateInfo() {
        HwAPPQoEUtils.logD(TAG, false, "Enter getCurAPPStateInfo.", new Object[0]);
        return this.mHwAPPQoEStateMachine.getCurAPPStateInfo();
    }

    public void logE(String info) {
        Log.e(TAG, info);
    }

    private static boolean getSettingsSystemBoolean(ContentResolver cr, String name, boolean def) {
        return Settings.System.getInt(cr, name, def ? 1 : 0) == 1;
    }

    public boolean getHidataState() {
        return getSettingsSystemBoolean(this.mContext.getContentResolver(), "smart_network_switching", false);
    }

    public boolean registerHidataMonitor(IHwHidataCallback callback) {
        synchronized (this.mLock) {
            if (callback == null) {
                HwAPPQoEUtils.logE(TAG, false, "Callback null", new Object[0]);
                return false;
            } else if (this.mGameQoeCallbackList.size() >= 10) {
                HwAPPQoEUtils.logE(TAG, false, "mGameQoeCallbackList size full", new Object[0]);
                return false;
            } else if (this.mGameQoeCallbackList.contains(callback)) {
                HwAPPQoEUtils.logE(TAG, false, "Callback has in list, do not register again", new Object[0]);
                return false;
            } else {
                this.mGameQoeCallbackList.add(callback);
                return true;
            }
        }
    }

    public void notifyGameQoeCallback(HwAPPStateInfo appStateInfo, int state) {
        synchronized (this.mLock) {
            if (this.mGameQoeCallbackList.size() == 0) {
                HwAPPQoEUtils.logE(TAG, false, "no notifyGameQoeCallback", new Object[0]);
                return;
            }
            HwAPPQoEUtils.logD(TAG, false, "notifyGameQoeCallback: %{public}d", Integer.valueOf(state));
            HwHidataAppStateInfo gameStateInfo = new HwHidataAppStateInfo();
            gameStateInfo.setAppId(appStateInfo.mAppId);
            gameStateInfo.setCurUid(appStateInfo.mAppUID);
            gameStateInfo.setCurScence(appStateInfo.mScenceId);
            gameStateInfo.setCurRtt(appStateInfo.mAppRTT);
            gameStateInfo.setCurState(state);
            gameStateInfo.setAction(appStateInfo.mAction);
            Iterator<IHwHidataCallback> it = this.mGameQoeCallbackList.iterator();
            while (it.hasNext()) {
                it.next().onAppStateChangeCallBack(gameStateInfo);
            }
        }
    }

    public boolean isNotifyAppQoeMpLinkStateSuccessful(String pkgName, boolean enable) {
        HwAPPStateInfo appStateInfo = getCurAPPStateInfo();
        if (appStateInfo == null) {
            HwAPPQoEUtils.logE(TAG, false, "appStateInfo error", new Object[0]);
            return false;
        }
        int uid = MpLinkCommonUtils.getAppUid(this.mContext, pkgName);
        if (appStateInfo.mAppUID != uid) {
            HwAPPQoEUtils.logE(TAG, false, "not curApp uid : %{public}d", Integer.valueOf(uid));
            return false;
        }
        if (enable) {
            this.mHwAPPQoEStateMachine.sendMessage(112, appStateInfo);
        } else {
            this.mHwAPPQoEStateMachine.sendMessage(111, appStateInfo);
        }
        return true;
    }

    public static boolean isAppStartMonitor(HwAPPStateInfo appStateInfo, Context context) {
        if (appStateInfo == null || context == null) {
            HwAPPQoEUtils.logE(TAG, false, "appStateInfo or context error", new Object[0]);
            return false;
        } else if (WifiProCommonUtils.isWifiProLitePropertyEnabled(context) || !WifiProCommonUtils.isWifiProSwitchOn(context)) {
            HwAPPQoEUtils.logD(TAG, false, "lite version or wifi pro switch off", new Object[0]);
            return false;
        } else if (HwArbitrationCommonUtils.MAINLAND_REGION && appStateInfo.getAppRegion() != 1) {
            return true;
        } else {
            if (appStateInfo.getAppRegion() == 1 && !HwArbitrationFunction.isChina() && HwArbitrationCommonUtils.IS_HIDATA2_ENABLED) {
                return isOverSeaAppInAllowMpLinkArea(appStateInfo);
            }
            HwAPPQoEUtils.logD(TAG, false, "the app and phone region is not situation", new Object[0]);
            return false;
        }
    }

    private static boolean isOverSeaAppInAllowMpLinkArea(HwAPPStateInfo appStateInfo) {
        if (appStateInfo.mScenceId == 100901) {
            return !isNotAllowMplinkRegion();
        }
        return true;
    }

    private static boolean isNotAllowMplinkRegion() {
        String country = SystemProperties.get("ro.hw.country", "");
        return country.contains("meaf") || "jp".equalsIgnoreCase(country) || "la".equalsIgnoreCase(country) || "nla".equalsIgnoreCase(country);
    }
}
