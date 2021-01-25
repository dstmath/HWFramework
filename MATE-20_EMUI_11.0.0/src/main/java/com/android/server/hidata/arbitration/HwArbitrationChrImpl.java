package com.android.server.hidata.arbitration;

import android.os.Bundle;
import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.histream.HwHiStreamManager;
import java.util.HashMap;

public class HwArbitrationChrImpl {
    public static final int CHR_MPLINK_FAIL_APP_END = 10;
    public static final int CHR_MPLINK_FAIL_BIND_FAIL = 8;
    public static final int CHR_MPLINK_FAIL_COEXISTENCE = 2;
    public static final int CHR_MPLINK_FAIL_ENVIRONMENT = 1;
    public static final int CHR_MPLINK_FAIL_HISTRORYQOE = 4;
    public static final int CHR_MPLINK_FAIL_ISBINDING = 6;
    public static final int CHR_MPLINK_FAIL_OTHERS = 20;
    public static final int CHR_MPLINK_FAIL_PINGPONG = 3;
    public static final int CHR_MPLINK_FAIL_QUERY_TIMEOUT = 7;
    public static final int CHR_MPLINK_FAIL_TARGETNETWORK = 5;
    public static final int CHR_MPLINK_FAIL_UNBIND_FAIL = 9;
    public static final int CHR_MPLINK_SUCCESS = 0;
    private static final int EID_HICURE_EXCEPTION_EVENT = 909002048;
    private static final int EID_WIFI_HICURE_INFO = 909009046;
    public static final int EVENT_CHR_APPEND_MPLINK_TO_WIFI = 6;
    public static final int EVENT_CHR_ERROR_STOP_MPLINK = 7;
    public static final int EVENT_CHR_STALL_BEGIN_MPLINK = 8;
    public static final int EVENT_CHR_STALL_MPLINK_TO_CELLULAR = 5;
    public static final int EVENT_CHR_STALL_MPLINK_TO_WIFI = 3;
    public static final int EVENT_CHR_USER_STOP_MPLINK = 9;
    public static final int EVENT_CHR_WIFI_RECOVER_MPLINK_TO_WIFI = 4;
    private static final int HICURE_FAILED = 0;
    private static final int HICURE_NONEED = 2;
    private static final int HICURE_SUCCESS = 1;
    private static final int INVALID_VALUE = -1;
    public static final int MPLINK_ERROR_CAUSE_BIG_DATA_TRAFFIC = 0;
    private static final String TAG = "HiData_HwArbitrationChrImpl";
    private static HwArbitrationChrImpl mHwArbitrationChrImpl;
    private int mHiCureCnt = 0;
    private HashMap<Integer, HiCureEvent> mHiCureEventMap;
    private int mHiCureFailedCnt = 0;
    private int mHiCureNoNeedCnt = 0;
    private HashMap<String, Integer> mHiCureResultMap;
    private int mHiCureSuccessCnt = 0;
    private IHiDataCHRCallBack mHiDataCHRCallBack;
    private int mRequestHiCureCnt = 0;
    private int mplinkCnt = 0;

    public static HwArbitrationChrImpl createInstance() {
        if (mHwArbitrationChrImpl == null) {
            mHwArbitrationChrImpl = new HwArbitrationChrImpl();
        }
        return mHwArbitrationChrImpl;
    }

    private HwArbitrationChrImpl() {
        HwArbitrationCommonUtils.logD(TAG, false, "init  HwArbitrationChrImpl", new Object[0]);
        this.mHiCureResultMap = new HashMap<>();
        this.mHiCureEventMap = new HashMap<>();
    }

    public void registArbitationChrCallBack(IHiDataCHRCallBack callBack) {
        this.mHiDataCHRCallBack = callBack;
    }

    /* access modifiers changed from: package-private */
    public static class HiCureEvent {
        String mApkName;
        int mBlockType;
        int mCurNetwork;
        int mCureMethod = -1;
        int mCureSrc;
        int mDataLink;
        int mDataRoamingSwitch;
        int mDataSwitch;
        int mDiagnoseResult = -1;
        int mHiCureResult = -1;
        int mHiDataResult = -1;
        int mWifiLink;
        int mWifiSwitch;

        HiCureEvent(String apkName, int blockType, int currentNetwork, boolean cellSwitch, boolean dataRoamingSwitch, boolean cellLink, boolean wifiSwitch, boolean wifiLink, int cureSrc) {
            this.mApkName = apkName;
            this.mBlockType = blockType;
            this.mCurNetwork = currentNetwork;
            this.mDataSwitch = cellSwitch ? 1 : 0;
            this.mDataRoamingSwitch = dataRoamingSwitch ? 1 : 0;
            this.mDataLink = cellLink ? 1 : 0;
            this.mWifiSwitch = wifiSwitch ? 1 : 0;
            this.mWifiLink = wifiLink ? 1 : 0;
            this.mCureSrc = cureSrc;
        }

        /* access modifiers changed from: package-private */
        public void setHiCureResult(int result, int diagnoseResult, int cureMethod) {
            this.mHiCureResult = result;
            this.mDiagnoseResult = diagnoseResult;
            this.mCureMethod = cureMethod;
        }

        /* access modifiers changed from: package-private */
        public int getHiCureResult() {
            return this.mHiCureResult;
        }

        /* access modifiers changed from: package-private */
        public void setHiDataResult(int isStall) {
            this.mHiDataResult = isStall;
        }

        /* access modifiers changed from: package-private */
        public int getHiDataResult() {
            return this.mHiDataResult;
        }

        /* access modifiers changed from: package-private */
        public Bundle buildHiCureFaultBundle() {
            Bundle data = new Bundle();
            data.putString("apk", this.mApkName);
            data.putInt("blkType", this.mBlockType);
            data.putInt("curNW", this.mCurNetwork);
            data.putInt("dataSwitch", this.mDataSwitch);
            data.putInt("dataRoamingSwitch", this.mDataRoamingSwitch);
            data.putInt("dataLink", this.mDataLink);
            data.putInt("wifiSwitch", this.mWifiSwitch);
            data.putInt("wifiLink", this.mWifiLink);
            data.putInt("cureSrc", this.mCureSrc);
            data.putInt("diagnoseRst", this.mDiagnoseResult);
            data.putInt("cureMethod", this.mCureMethod);
            data.putInt("cureRst", this.mHiCureResult);
            data.putInt("hiDataRst", this.mHiDataResult);
            return data;
        }

        public String toString() {
            return "apkName = " + this.mApkName + ", blockingType = " + this.mBlockType + ", currentNetwork = " + this.mCurNetwork + ", dataSwitch = " + this.mDataSwitch + ", dataRoamingSwitch = " + this.mDataRoamingSwitch + ", DataLink = " + this.mDataLink + ", wifiSwtich = " + this.mWifiSwitch + ", wifiLink = " + this.mWifiLink + ", cureSource = " + this.mCureSrc + ", diagnoseResult = " + this.mDiagnoseResult + ", cureMethod = " + this.mCureMethod + ", hiCureResult = " + this.mHiCureResult + ", hiDataResult = " + this.mHiDataResult;
        }
    }

    private void resetHiCureCnt() {
        this.mHiCureCnt = 0;
        this.mHiCureFailedCnt = 0;
        this.mHiCureNoNeedCnt = 0;
        this.mHiCureSuccessCnt = 0;
    }

    public void updateHiCureRequestChr(String apkName, int blockType, int currentNetwork, boolean DataSwitch, boolean DataRoamingSwitch, boolean DataLink, boolean wifiSwitch, boolean wifiLink, int cureSrc) {
        HwArbitrationCommonUtils.logD(TAG, false, "updateHiCureRequestChr", new Object[0]);
        this.mRequestHiCureCnt++;
        HiCureEvent hiCureEvent = new HiCureEvent(apkName, blockType, currentNetwork, DataSwitch, DataRoamingSwitch, DataLink, wifiSwitch, wifiLink, cureSrc);
        if (this.mHiCureEventMap == null) {
            this.mHiCureEventMap = new HashMap<>();
        }
        HashMap<Integer, HiCureEvent> hashMap = this.mHiCureEventMap;
        if (hashMap != null) {
            hashMap.put(Integer.valueOf(this.mRequestHiCureCnt), hiCureEvent);
        }
    }

    public void updateHiCureResultChr(int reason, int diagnoseResult, int method) {
        HwArbitrationCommonUtils.logD(TAG, false, "updateHiCureResultChr", new Object[0]);
        HashMap<Integer, HiCureEvent> hashMap = this.mHiCureEventMap;
        if (hashMap == null || hashMap.get(Integer.valueOf(this.mRequestHiCureCnt)) == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "no request record in HiCureEventMap", new Object[0]);
        } else if (-1 != this.mHiCureEventMap.get(Integer.valueOf(this.mRequestHiCureCnt)).getHiCureResult()) {
            HwArbitrationCommonUtils.logD(TAG, false, "hiCure receive duplicate result,drop it", new Object[0]);
        } else {
            this.mHiCureCnt++;
            if (1 == reason) {
                HwArbitrationCommonUtils.logD(TAG, false, "updateHiCureResult:SUCCESS", new Object[0]);
                this.mHiCureSuccessCnt++;
            } else if (2 == reason) {
                HwArbitrationCommonUtils.logD(TAG, false, "updateHiCureResult:NO NEED", new Object[0]);
                this.mHiCureNoNeedCnt++;
            } else if (reason == 0) {
                HwArbitrationCommonUtils.logD(TAG, false, "updateHiCureResult:FAILED", new Object[0]);
                this.mHiCureFailedCnt++;
            } else if (-1 == reason) {
                HwArbitrationCommonUtils.logD(TAG, false, "updateHiCureResult: not Received", new Object[0]);
                HiCureEvent hiCureEvent = this.mHiCureEventMap.get(Integer.valueOf(this.mRequestHiCureCnt));
                if (hiCureEvent != null) {
                    hiCureEvent.setHiCureResult(reason, diagnoseResult, method);
                    uploadDFTEvent(EID_HICURE_EXCEPTION_EVENT, hiCureEvent.buildHiCureFaultBundle());
                    uploadHiCureStatisticsInfo();
                    resetHiCureCnt();
                    return;
                }
                return;
            }
            if (this.mHiCureCnt != this.mHiCureSuccessCnt + this.mHiCureNoNeedCnt + this.mHiCureFailedCnt) {
                HwArbitrationCommonUtils.logD(TAG, false, "HiCure statistics:not received HiCure result", new Object[0]);
            }
            updateHashMap(String.valueOf(reason), this.mHiCureResultMap);
            uploadHiCureStatisticsInfo();
            resetHiCureCnt();
            updateHiCureEventResult(reason, diagnoseResult, method);
        }
    }

    private void updateHiCureEventResult(int result, int diagnoseResult, int method) {
        HwArbitrationCommonUtils.logD(TAG, false, "updateHiCureEventResult", new Object[0]);
        HashMap<Integer, HiCureEvent> hashMap = this.mHiCureEventMap;
        if (hashMap != null) {
            HiCureEvent hiCureEvent = hashMap.get(Integer.valueOf(this.mRequestHiCureCnt));
            if (hiCureEvent != null) {
                hiCureEvent.setHiCureResult(result, diagnoseResult, method);
            } else {
                HwArbitrationCommonUtils.logD(TAG, false, "HiCureEvent not exist", new Object[0]);
            }
        }
    }

    public void updateIsStallAfterCure(int hidataRst) {
        HwArbitrationCommonUtils.logD(TAG, false, "updateIsStallAfterCure", new Object[0]);
        HashMap<Integer, HiCureEvent> hashMap = this.mHiCureEventMap;
        if (hashMap == null || hashMap.get(Integer.valueOf(this.mRequestHiCureCnt)) == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "HiCureEvent not exist", new Object[0]);
        } else if (-1 != this.mHiCureEventMap.get(Integer.valueOf(this.mRequestHiCureCnt)).getHiDataResult()) {
            HwArbitrationCommonUtils.logD(TAG, false, "hidata result is already set", new Object[0]);
        } else {
            HiCureEvent hiCureEvent = this.mHiCureEventMap.get(Integer.valueOf(this.mRequestHiCureCnt));
            hiCureEvent.setHiDataResult(hidataRst);
            uploadDFTEvent(EID_HICURE_EXCEPTION_EVENT, hiCureEvent.buildHiCureFaultBundle());
            HwArbitrationCommonUtils.logD(TAG, false, "HiCure EXCEPTION CHR %{public}s", hiCureEvent.toString());
            this.mHiCureEventMap.put(Integer.valueOf(this.mRequestHiCureCnt), hiCureEvent);
        }
    }

    public void uploadDFTEvent(int eventId, Bundle bundle) {
        HwArbitrationCommonUtils.logD(TAG, false, "uploadHiCureCHREvent,eventId =%{public}d", Integer.valueOf(eventId));
        IHiDataCHRCallBack iHiDataCHRCallBack = this.mHiDataCHRCallBack;
        if (iHiDataCHRCallBack != null) {
            iHiDataCHRCallBack.uploadHiDataDFTEvent(eventId, bundle);
        }
    }

    private void uploadHiCureStatisticsInfo() {
        HwArbitrationCommonUtils.logD(TAG, false, "uploadHiCureStatisticsInfo", new Object[0]);
        Bundle data = new Bundle();
        data.putInt("cureTimes", this.mHiCureCnt);
        data.putInt("failTimes", this.mHiCureFailedCnt);
        data.putInt("succTimes", this.mHiCureSuccessCnt);
        data.putInt("noCureTimes", this.mHiCureNoNeedCnt);
        HwArbitrationCommonUtils.logD(TAG, false, "CureCnt:%{public}d, FailCnt: %{public}d, SuccCnt: %{public}d, noCureCnt: %{public}d", Integer.valueOf(this.mHiCureCnt), Integer.valueOf(this.mHiCureFailedCnt), Integer.valueOf(this.mHiCureSuccessCnt), Integer.valueOf(this.mHiCureNoNeedCnt));
        uploadDFTEvent(EID_WIFI_HICURE_INFO, data);
    }

    private void updateHashMap(String name, HashMap<String, Integer> map) {
        if (map != null && name != null) {
            Integer cnt = map.get(name);
            if (cnt == null) {
                map.put(name, 1);
            } else {
                map.put(name, Integer.valueOf(cnt.intValue() + 1));
            }
        }
    }

    public void updateRequestMplinkChr(HwAPPStateInfo appInfo, int failReason) {
        updateMplinkActionChr(appInfo, 5, failReason);
    }

    public void updateMplinkActionChr(HwAPPStateInfo appInfo, int mplinkEvent, int failReason) {
        HwArbitrationCommonUtils.logD(TAG, false, "onUpdateMplinkActionChr", new Object[0]);
        if (appInfo == null) {
            HwArbitrationCommonUtils.logD(TAG, false, "appInfo is null", new Object[0]);
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, false, "appUid:%{public}d, sceneId:%{public}d, mplinkEvent:%{public}d, failReason:%{public}d", Integer.valueOf(appInfo.mAppUID), Integer.valueOf(appInfo.mScenceId), Integer.valueOf(mplinkEvent), Integer.valueOf(failReason));
        if (-1 != failReason) {
            this.mplinkCnt++;
            HwArbitrationCommonUtils.logD(TAG, false, "mplinkEventCnt:%{public}d", Integer.valueOf(this.mplinkCnt));
            onMplinkStateChange(appInfo.mScenceId, mplinkEvent, failReason);
        }
    }

    public void onMplinkStateChange(int sceneId, int mplinkEvent, int failReason) {
        HwArbitrationCommonUtils.logD(TAG, false, "onMplinkStateChange, sceneId:%{public}d, mplinkEvent:%{public}d,failReason:%{public}d", Integer.valueOf(sceneId), Integer.valueOf(mplinkEvent), Integer.valueOf(failReason));
        HwHiStreamManager mHwHiStreamManager = HwHiStreamManager.getInstance();
        if (mHwHiStreamManager != null) {
            mHwHiStreamManager.onMplinkStateChange(sceneId, mplinkEvent, failReason);
        }
    }
}
