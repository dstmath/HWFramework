package com.android.server.hidata.histream;

import android.content.Context;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;

/* access modifiers changed from: package-private */
public class HwHistreamUserLearning {
    private static final int WECHAT_CHECK_USER_BEHAVIOR_DALYE = 30000;
    private static HwHistreamUserLearning mHwHistreamUserLearning;
    private long lastDataDisabledTime = 0;
    private long lastWifiDisabledTime = 0;
    private HwHiStreamDataBaseManager mHwHiStreamDataBaseManager;
    private HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor;
    private AppInfo mOtherAppInfo = null;
    private AppInfo mWechatInfo = null;

    private HwHistreamUserLearning(Context context) {
        this.mHwHiStreamDataBaseManager = HwHiStreamDataBaseManager.getInstance(context);
        this.mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
    }

    public static HwHistreamUserLearning createInstance(Context context) {
        if (mHwHistreamUserLearning == null) {
            mHwHistreamUserLearning = new HwHistreamUserLearning(context);
        }
        return mHwHistreamUserLearning;
    }

    public static HwHistreamUserLearning getInstance() {
        return mHwHistreamUserLearning;
    }

    public void onAPPStateChange(HwAPPStateInfo stateInfo, int appState) {
        HwHiStreamTraffic mHwHiStreamTraffic = HwHiStreamTraffic.getInstance();
        if (stateInfo != null && mHwHiStreamTraffic != null && this.mHwHiStreamNetworkMonitor != null) {
            if (100 == appState) {
                int curNetworkType = stateInfo.mNetworkType;
                String curBssid = this.mHwHiStreamNetworkMonitor.getCurBSSID();
                long startTime = System.currentTimeMillis();
                if (100105 == stateInfo.mScenceId || 100106 == stateInfo.mScenceId) {
                    this.mWechatInfo = new AppInfo(stateInfo.mScenceId, appState);
                    if (800 == curNetworkType && startTime - this.lastDataDisabledTime < HwArbitrationDEFS.DelayTimeMillisA) {
                        setAPUserType(curBssid, stateInfo.mScenceId, 1);
                    } else if (801 == curNetworkType && startTime - this.lastWifiDisabledTime < HwArbitrationDEFS.DelayTimeMillisA) {
                        setAPUserType(curBssid, stateInfo.mScenceId, 2);
                    }
                } else {
                    this.mOtherAppInfo = new AppInfo(stateInfo.mScenceId, appState);
                }
            } else if (101 == appState) {
                AppInfo appInfo = this.mWechatInfo;
                if (appInfo == null || appInfo.mScenceId != stateInfo.mScenceId) {
                    AppInfo appInfo2 = this.mOtherAppInfo;
                    if (appInfo2 != null && appInfo2.mScenceId == stateInfo.mScenceId) {
                        this.mOtherAppInfo = null;
                        return;
                    }
                    return;
                }
                this.mWechatInfo = null;
            }
        }
    }

    public void onWifiDisabled() {
        AppInfo curAppInfo = null;
        AppInfo appInfo = this.mOtherAppInfo;
        if (appInfo == null || appInfo.mAppState == 101) {
            AppInfo appInfo2 = this.mWechatInfo;
            if (appInfo2 == null || appInfo2.mAppState == 101) {
                this.lastWifiDisabledTime = System.currentTimeMillis();
            } else {
                curAppInfo = this.mWechatInfo;
            }
        } else {
            curAppInfo = this.mOtherAppInfo;
        }
        updateUserType(curAppInfo);
    }

    public void onMobileDataDisabled(boolean isMplink) {
        AppInfo curAppInfo = null;
        AppInfo appInfo = this.mOtherAppInfo;
        if (appInfo == null || appInfo.mAppState == 101) {
            AppInfo appInfo2 = this.mWechatInfo;
            if (appInfo2 == null || appInfo2.mAppState == 101) {
                this.lastDataDisabledTime = System.currentTimeMillis();
            } else {
                curAppInfo = this.mWechatInfo;
            }
        } else {
            curAppInfo = this.mOtherAppInfo;
        }
        degradeUserType(curAppInfo, isMplink);
    }

    public boolean setAPUserType(String ssid, int scenceId, int UserType) {
        HwHiStreamUtils.logD(false, "setAPUserType usertype = %{public}d, scenceId %{public}d", Integer.valueOf(UserType), Integer.valueOf(scenceId));
        if (ssid == null) {
            return false;
        }
        return this.mHwHiStreamDataBaseManager.addOrUpdateApRecordInfo(new HiStreamAPInfo(ssid, scenceId, UserType));
    }

    public int getUserType(HwAPPStateInfo stateInfo) {
        if (stateInfo == null) {
            return 1;
        }
        int appSceneId = stateInfo.mScenceId;
        int userType = 0;
        if (801 == stateInfo.mNetworkType) {
            userType = 2;
        } else {
            HwHiStreamNetworkMonitor hwHiStreamNetworkMonitor = this.mHwHiStreamNetworkMonitor;
            if (hwHiStreamNetworkMonitor != null) {
                userType = getAPUserType(hwHiStreamNetworkMonitor.getCurBSSID(), appSceneId);
            }
        }
        if (userType == 0) {
            return 1;
        }
        return userType;
    }

    private int getAPUserType(String bssid, int scenceId) {
        if (bssid == null) {
            HwHiStreamUtils.logD(false, "getAPUserType:bssid is null", new Object[0]);
            return 0;
        }
        HiStreamAPInfo apInfo = this.mHwHiStreamDataBaseManager.queryApRecordInfo(bssid, scenceId);
        if (apInfo == null) {
            HwHiStreamUtils.logD(false, "getAPUserType:APinfo is null", new Object[0]);
            return 0;
        }
        HwHiStreamUtils.logD(false, "getAPUserType : %{public}d, scenceId = %{public}d", Integer.valueOf(apInfo.APUsrType), Integer.valueOf(scenceId));
        return apInfo.APUsrType;
    }

    private void updateUserType(AppInfo curAppInfo) {
        HwHiStreamNetworkMonitor hwHiStreamNetworkMonitor;
        String curBssid;
        int curUserType;
        if (curAppInfo != null && (hwHiStreamNetworkMonitor = this.mHwHiStreamNetworkMonitor) != null && (curUserType = getAPUserType((curBssid = hwHiStreamNetworkMonitor.getCurBSSID()), curAppInfo.mScenceId)) != 2) {
            if (curUserType == 3) {
                setAPUserType(curBssid, curAppInfo.mScenceId, 1);
            } else {
                setAPUserType(curBssid, curAppInfo.mScenceId, 2);
            }
        }
    }

    private void degradeUserType(AppInfo curAppInfo, boolean isMplink) {
        HwHiStreamNetworkMonitor hwHiStreamNetworkMonitor;
        String curBssid;
        int curUserType;
        if (curAppInfo != null && (hwHiStreamNetworkMonitor = this.mHwHiStreamNetworkMonitor) != null && (curUserType = getAPUserType((curBssid = hwHiStreamNetworkMonitor.getCurBSSID()), curAppInfo.mScenceId)) != 3) {
            if (curUserType != 1 || !isMplink) {
                setAPUserType(curBssid, curAppInfo.mScenceId, 1);
            } else {
                setAPUserType(curBssid, curAppInfo.mScenceId, 3);
            }
        }
    }

    public static class AppInfo {
        int mAppState;
        int mScenceId;

        public AppInfo(int scenceId, int appState) {
            this.mScenceId = scenceId;
            this.mAppState = appState;
        }
    }
}
