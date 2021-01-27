package com.android.server.hidata.appqoe;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.hidata.HwHiDataAppStateInfo;
import com.android.server.hidata.IHwHiDataCallback;
import com.android.server.hidata.arbitration.HwArbitrationCommonUtils;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.Iterator;

public class HwAppQoeManager {
    private static final int GAMEQOE_CALLBACKLIST_MAX_SIZE = 10;
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwAppQoeManager.class.getSimpleName());
    private static HwAppQoeManager sHwAppQoeManager = null;
    IHwAppQoeCallback mBrainCallback = null;
    private Context mContext;
    private ArrayList<IHwHiDataCallback> mGameQoeCallbackList = new ArrayList<>();
    private HwAppQoeStateMachine mHwAppQoeStateMachine = null;
    private final Object mLock = new Object();
    IHwAppQoeCallback mWaveMappingCallback = null;

    private HwAppQoeManager(Context context) {
        this.mContext = context;
        this.mHwAppQoeStateMachine = HwAppQoeStateMachine.createHwAppQoeStateMachine(context);
    }

    public static HwAppQoeManager createHwAppQoeManager(Context context) {
        if (sHwAppQoeManager == null) {
            sHwAppQoeManager = new HwAppQoeManager(context);
        }
        return sHwAppQoeManager;
    }

    public static HwAppQoeManager getInstance() {
        return sHwAppQoeManager;
    }

    public void registerAppQoeCallback(IHwAppQoeCallback callback, boolean isBrain) {
        if (isBrain) {
            this.mBrainCallback = callback;
        } else {
            this.mWaveMappingCallback = callback;
        }
    }

    public IHwAppQoeCallback getAppQoeCallback(boolean isBrain) {
        if (isBrain) {
            return this.mBrainCallback;
        }
        return this.mWaveMappingCallback;
    }

    public HwAppStateInfo getCurAppStateInfo() {
        HwAppQoeUtils.logD(TAG, false, "enter getCurAppStateInfo.", new Object[0]);
        return this.mHwAppQoeStateMachine.getCurAppStateInfo();
    }

    public boolean registerHiDataMonitor(IHwHiDataCallback callback) {
        synchronized (this.mLock) {
            if (callback == null) {
                HwAppQoeUtils.logE(TAG, false, "Callback null", new Object[0]);
                return false;
            } else if (this.mGameQoeCallbackList.size() >= 10) {
                HwAppQoeUtils.logE(TAG, false, "mGameQoeCallbackList size full", new Object[0]);
                return false;
            } else if (this.mGameQoeCallbackList.contains(callback)) {
                HwAppQoeUtils.logE(TAG, false, "Callback has in list, do not register again", new Object[0]);
                return false;
            } else {
                this.mGameQoeCallbackList.add(callback);
                return true;
            }
        }
    }

    public void notifyGameQoeCallback(HwAppStateInfo appStateInfo, int state) {
        synchronized (this.mLock) {
            if (this.mGameQoeCallbackList.size() == 0) {
                HwAppQoeUtils.logE(TAG, false, "no notifyGameQoeCallback", new Object[0]);
                return;
            }
            HwAppQoeUtils.logD(TAG, false, "notifyGameQoeCallback: %{public}d", Integer.valueOf(state));
            HwHiDataAppStateInfo gameStateInfo = new HwHiDataAppStateInfo();
            gameStateInfo.setAppId(appStateInfo.mAppId);
            gameStateInfo.setCurUid(appStateInfo.mAppUid);
            gameStateInfo.setCurScenes(appStateInfo.mScenesId);
            gameStateInfo.setCurRtt(appStateInfo.mAppRtt);
            gameStateInfo.setCurState(state);
            gameStateInfo.setAction(appStateInfo.mAction);
            Iterator<IHwHiDataCallback> it = this.mGameQoeCallbackList.iterator();
            while (it.hasNext()) {
                it.next().onAppStateChangeCallBack(gameStateInfo);
            }
        }
    }

    public static boolean isAppStartMonitor(HwAppStateInfo appStateInfo, Context context) {
        if (appStateInfo == null || context == null) {
            HwAppQoeUtils.logE(TAG, false, "appStateInfo or context error", new Object[0]);
            return false;
        } else if (WifiProCommonUtils.isWifiProLitePropertyEnabled(context) || !WifiProCommonUtils.isWifiProSwitchOn(context)) {
            HwAppQoeUtils.logD(TAG, false, "lite version or wifi pro switch off", new Object[0]);
            return false;
        } else if (HwArbitrationCommonUtils.MAINLAND_REGION && appStateInfo.getAppRegion() != 1) {
            return true;
        } else {
            if (appStateInfo.getAppRegion() == 1 && !HwArbitrationFunction.isChina() && HwArbitrationCommonUtils.IS_HIDATA2_ENABLED) {
                return isOverSeaAppInAllowMpLinkArea(appStateInfo);
            }
            HwAppQoeUtils.logD(TAG, false, "the app and phone region is not situation", new Object[0]);
            return false;
        }
    }

    private static boolean isOverSeaAppInAllowMpLinkArea(HwAppStateInfo appStateInfo) {
        if (appStateInfo.mScenesId == 100901) {
            return !isNotAllowMpLinkRegion();
        }
        return true;
    }

    private static boolean isNotAllowMpLinkRegion() {
        String country = SystemProperties.get("ro.hw.country", "");
        return country.contains("meaf") || "jp".equalsIgnoreCase(country) || "la".equalsIgnoreCase(country) || "nla".equalsIgnoreCase(country);
    }
}
