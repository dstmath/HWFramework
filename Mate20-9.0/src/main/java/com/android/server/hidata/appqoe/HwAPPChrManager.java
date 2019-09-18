package com.android.server.hidata.appqoe;

import android.os.SystemProperties;
import com.android.server.hidata.channelqoe.HwChannelQoEManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwAPPChrManager {
    public static final int BETA_USER = 3;
    public static final int COMMERCIAL_USER = 1;
    public static final int FANS_USER = 2;
    private static final long ONE_DAY_TIME = 86400000;
    private static final long ONE_WEEK_TIME = 604800000;
    public static final int OVERSEA_COMMERCIAL_USER = 6;
    public static final int OVERSEA_USER = 5;
    public static final int TEST_USER = 4;
    private static HwAPPChrManager mHwAPPChrManager = null;
    private List<HwAPPChrStatisInfo> mInfoList = new ArrayList();
    private long mLastUploadTime = 0;
    private Object mLock = new Object();

    private HwAPPChrManager() {
        this.mInfoList.clear();
    }

    public static synchronized HwAPPChrManager getInstance() {
        HwAPPChrManager hwAPPChrManager;
        synchronized (HwAPPChrManager.class) {
            if (mHwAPPChrManager == null) {
                mHwAPPChrManager = new HwAPPChrManager();
            }
            hwAPPChrManager = mHwAPPChrManager;
        }
        return hwAPPChrManager;
    }

    public void updateStatisInfo(HwAPPStateInfo appStateInfo, int type) {
        if (appStateInfo != null && appStateInfo.mAppType == 1000) {
            updateStatisInfo(appStateInfo.mAppId, appStateInfo.mScenceId, type);
        }
    }

    public void updateStatisInfo(int appid, int scenceid, int type) {
        synchronized (this.mLock) {
            HwAPPChrStatisInfo result = getStatisInfo(appid, scenceid);
            switch (type) {
                case 1:
                    result.wifiStartNum++;
                    break;
                case 2:
                    result.cellStartNum++;
                    break;
                case 3:
                    result.wifiStallNum++;
                    break;
                case 4:
                    result.cellStallNum++;
                    break;
                case 5:
                    result.wifispNum++;
                    break;
                case 6:
                    result.cellspNum++;
                    break;
                case 7:
                    result.rn1Num++;
                    break;
                case 8:
                    result.rn2Num++;
                    break;
                case 9:
                    result.rn3Num++;
                    break;
                case 10:
                    result.rn4Num++;
                    break;
                case 11:
                    result.chfNum++;
                    break;
                case 12:
                    result.mpfNum++;
                    break;
                case 13:
                    result.mpsNum++;
                    break;
                case 14:
                    result.afgNum++;
                    break;
                case 15:
                    result.afbNum++;
                    break;
                case 16:
                    result.inKQINum++;
                    break;
                case 17:
                    result.overKQINum++;
                    break;
                case 18:
                    result.closeCellNum++;
                    break;
                case 19:
                    result.closeWiFiNum++;
                    break;
                case 20:
                    result.startHicNum++;
                    break;
                case 21:
                    result.hicsNum++;
                    break;
            }
            setStatisInfo(result);
        }
    }

    public void updateTraffic(HwAPPStateInfo appStateInfo, long tracffic) {
        if (appStateInfo != null && appStateInfo.mAppType == 1000) {
            synchronized (this.mLock) {
                HwAPPChrStatisInfo result = getStatisInfo(appStateInfo.mAppId, appStateInfo.mScenceId);
                result.trffic = (int) (((long) result.trffic) + tracffic);
                setStatisInfo(result);
            }
        }
    }

    private void setStatisInfo(HwAPPChrStatisInfo info) {
        for (HwAPPChrStatisInfo result : this.mInfoList) {
            if (result.appId == info.appId && result.scenceId == info.scenceId) {
                result.copyInfo(info);
                return;
            }
        }
    }

    public HwAPPChrStatisInfo getStatisInfo(int appid, int scenceid) {
        HwAPPChrStatisInfo result;
        synchronized (this.mLock) {
            result = null;
            Iterator<HwAPPChrStatisInfo> it = this.mInfoList.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                HwAPPChrStatisInfo info = it.next();
                if (info.appId == appid && info.scenceId == scenceid) {
                    result = info;
                    break;
                }
            }
            if (result == null) {
                result = new HwAPPChrStatisInfo();
                result.appId = appid;
                result.scenceId = scenceid;
                this.mInfoList.add(result);
            }
        }
        return result;
    }

    public boolean isCommercialUser() {
        int userType = SystemProperties.getInt("ro.logsystem.usertype", 1);
        if (3 == userType || 4 == userType) {
            return false;
        }
        return true;
    }

    public void uploadAppChrInfo() {
        long uploadTime;
        synchronized (this.mLock) {
            if (isCommercialUser()) {
                uploadTime = 604800000;
                HwAPPQoEUtils.logD("uploadAppChrInfo is a Commercial User ");
            } else {
                uploadTime = 86400000;
                HwAPPQoEUtils.logD("uploadAppChrInfo is a beta User ");
            }
            HwChannelQoEManager.getInstance().uploadChannelQoEParmStatistics(uploadTime);
            if ((this.mLastUploadTime == 0 || System.currentTimeMillis() - this.mLastUploadTime > uploadTime) && this.mInfoList.size() > 0) {
                HwAPPQoEUtils.logD("uploadAppChrInfo mInfoList.size() = " + this.mInfoList.size());
                HwAPPChrExcpReport.getInstance().reportStaticsInfo(this.mInfoList);
                this.mInfoList.clear();
                this.mLastUploadTime = System.currentTimeMillis();
            }
        }
    }
}
