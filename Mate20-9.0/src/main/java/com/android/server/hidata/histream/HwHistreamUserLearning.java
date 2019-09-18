package com.android.server.hidata.histream;

import android.content.Context;
import android.os.SystemProperties;
import com.android.server.hidata.appqoe.HwAPPQoEUserAction;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;

class HwHistreamUserLearning {
    private static final int WECHAT_CHECK_USER_BEHAVIOR_DALYE = 30000;
    private static HwHistreamUserLearning mHwHistreamUserLearning;
    private long lastDataDisabledTime = 0;
    private long lastWifiDisabledTime = 0;
    private String mCurBssid = null;
    private HwHiStreamDataBaseManager mHwHiStreamDataBaseManager;
    private AppInfo mOtherAppInfo = null;
    private AppInfo mWechatInfo = null;

    public static class AppInfo {
        int mAppState;
        int mScenceId;

        public AppInfo(int scenceId, int appState) {
            this.mScenceId = scenceId;
            this.mAppState = appState;
        }
    }

    private HwHistreamUserLearning(Context context) {
        this.mHwHiStreamDataBaseManager = HwHiStreamDataBaseManager.getInstance(context);
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
        HwHiStreamNetworkMonitor mHwHiStreamNetworkMonitor = HwHiStreamNetworkMonitor.getInstance();
        if (stateInfo != null && mHwHiStreamTraffic != null && mHwHiStreamNetworkMonitor != null) {
            if (100 == appState) {
                int curNetworkType = stateInfo.mNetworkType;
                this.mCurBssid = mHwHiStreamNetworkMonitor.getCurBSSID();
                long startTime = System.currentTimeMillis();
                if (100105 == stateInfo.mScenceId || 100106 == stateInfo.mScenceId) {
                    this.mWechatInfo = new AppInfo(stateInfo.mScenceId, appState);
                    if (800 == curNetworkType && startTime - this.lastDataDisabledTime < HwArbitrationDEFS.DelayTimeMillisA) {
                        setAPUserType(this.mCurBssid, stateInfo.mScenceId, 1);
                    } else if (801 == curNetworkType && startTime - this.lastWifiDisabledTime < HwArbitrationDEFS.DelayTimeMillisA) {
                        setAPUserType(this.mCurBssid, stateInfo.mScenceId, 2);
                    }
                } else {
                    this.mOtherAppInfo = new AppInfo(stateInfo.mScenceId, appState);
                }
            } else if (101 == appState) {
                if (this.mWechatInfo != null && this.mWechatInfo.mScenceId == stateInfo.mScenceId) {
                    this.mWechatInfo = null;
                } else if (this.mOtherAppInfo != null && this.mOtherAppInfo.mScenceId == stateInfo.mScenceId) {
                    this.mOtherAppInfo = null;
                }
            }
        }
    }

    public void onWifiDisabled() {
        AppInfo curAppInfo = null;
        if (this.mOtherAppInfo != null && 101 != this.mOtherAppInfo.mAppState) {
            curAppInfo = this.mOtherAppInfo;
        } else if (this.mWechatInfo == null || 101 == this.mWechatInfo.mAppState) {
            this.lastWifiDisabledTime = System.currentTimeMillis();
        } else {
            curAppInfo = this.mWechatInfo;
        }
        if (curAppInfo != null) {
            int curUserType = getAPUserType(curAppInfo.mScenceId);
            if (3 == curUserType) {
                setAPUserType(this.mCurBssid, curAppInfo.mScenceId, 1);
            } else if (1 == curUserType) {
                setAPUserType(this.mCurBssid, curAppInfo.mScenceId, 2);
            }
        }
    }

    public void onMobileDataDisabled(boolean isMplink) {
        AppInfo curAppInfo = null;
        if (this.mOtherAppInfo != null && 101 != this.mOtherAppInfo.mAppState) {
            curAppInfo = this.mOtherAppInfo;
        } else if (this.mWechatInfo == null || 101 == this.mWechatInfo.mAppState) {
            this.lastDataDisabledTime = System.currentTimeMillis();
        } else {
            curAppInfo = this.mWechatInfo;
        }
        if (curAppInfo != null) {
            int curUserType = getAPUserType(curAppInfo.mScenceId);
            if (true == isMplink && 1 == curUserType) {
                setAPUserType(this.mCurBssid, curAppInfo.mScenceId, 3);
            } else if (2 == curUserType) {
                setAPUserType(this.mCurBssid, curAppInfo.mScenceId, 1);
            }
        }
    }

    public boolean setAPUserType(String ssid, int scenceId, int UserType) {
        HwHiStreamUtils.logD("setAPUserType usertype = " + UserType + ",scenceId" + scenceId);
        if (ssid == null) {
            return false;
        }
        return this.mHwHiStreamDataBaseManager.addOrUpdateApRecordInfo(new HiStreamAPInfo(ssid, scenceId, UserType));
    }

    public int getUserType(HwAPPStateInfo stateInfo) {
        int userType;
        if (stateInfo == null) {
            return 1;
        }
        int appSceneId = stateInfo.mScenceId;
        if (801 == stateInfo.mNetworkType) {
            userType = 2;
        } else {
            userType = getAPUserType(appSceneId);
        }
        if (userType == 0) {
            if (true == isDefaultRadicalUser()) {
                userType = 2;
            } else {
                userType = 1;
            }
        }
        return userType;
    }

    public int getAPUserType(int scenceId) {
        if (this.mCurBssid == null) {
            HwHiStreamUtils.logD("getAPUserType:mCurBssid is null");
            return 0;
        }
        HiStreamAPInfo apInfo = this.mHwHiStreamDataBaseManager.queryApRecordInfo(this.mCurBssid, scenceId);
        if (apInfo == null) {
            HwHiStreamUtils.logD("getAPUserType:APinfo is null");
            return 0;
        }
        HwHiStreamUtils.logD("getAPUserType : " + apInfo.APUsrType + ",scenceId=" + scenceId);
        return apInfo.APUsrType;
    }

    private boolean isDefaultRadicalUser() {
        boolean ret;
        String chipset = SystemProperties.get(HwAPPQoEUserAction.CHIPSET_TYPE_PROP, "none");
        if (chipset == null || (!chipset.contains("4345") && !chipset.contains("4359") && !chipset.contains("1103"))) {
            ret = false;
        } else {
            ret = true;
        }
        HwHiStreamUtils.logD("isDefaultRadicalUser:" + ret);
        return ret;
    }
}
