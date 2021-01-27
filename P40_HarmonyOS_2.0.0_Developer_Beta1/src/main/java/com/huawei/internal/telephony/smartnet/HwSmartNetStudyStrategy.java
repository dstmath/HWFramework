package com.huawei.internal.telephony.smartnet;

import android.content.Context;
import android.os.Handler;
import com.huawei.android.telephony.RlogEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HwSmartNetStudyStrategy {
    private static final int CALC_AVERAGE_NUMBER_LEN = 2;
    private static final String TAG = "HwSmartNetStudyStrategy";
    private static HwSmartNetStudyStrategy sInstance = null;
    private ArrayList<ArrayList<BlackPointPath>> mBlackPointPath;
    private HashMap<Long, CellInfoPoint> mCellInfoPointHashMap;
    private Context mContext = null;
    private Handler mStateHandler = null;
    private HashMap<String, int[]> mStudyIndexByIccId;

    private HwSmartNetStudyStrategy(Context context, Handler stateHandler) {
        this.mContext = context;
        this.mStateHandler = stateHandler;
        this.mCellInfoPointHashMap = new HashMap<>();
        this.mStudyIndexByIccId = new HashMap<>();
        initStudyIndexFromDataBase();
        logi("create HwSmartNetStudyStrategy");
    }

    public static HwSmartNetStudyStrategy init(Context context, Handler stateHandler) {
        HwSmartNetStudyStrategy hwSmartNetStudyStrategy;
        synchronized (HwSmartNetStudyStrategy.class) {
            if (sInstance == null) {
                sInstance = new HwSmartNetStudyStrategy(context, stateHandler);
            } else {
                logi("init() called multiple times!  sInstance = " + sInstance);
            }
            hwSmartNetStudyStrategy = sInstance;
        }
        return hwSmartNetStudyStrategy;
    }

    public static HwSmartNetStudyStrategy getInstance() {
        HwSmartNetStudyStrategy hwSmartNetStudyStrategy;
        synchronized (HwSmartNetStudyStrategy.class) {
            hwSmartNetStudyStrategy = sInstance;
        }
        return hwSmartNetStudyStrategy;
    }

    private static void loge(String msg) {
        RlogEx.e(TAG, msg);
    }

    private static void logi(String msg) {
        RlogEx.i(TAG, msg);
    }

    private static void logd(String msg) {
    }

    private void initStudyIndexFromDataBase() {
        for (StudyIndexTable studyIndexTable : HwSmartNetDb.getInstance().queryStudyIndex()) {
            String iccidHash = studyIndexTable.getIccidHash();
            int routeId = studyIndexTable.getRouteId();
            this.mStudyIndexByIccId.putIfAbsent(iccidHash, new int[2]);
            if (routeId <= 2) {
                this.mStudyIndexByIccId.get(iccidHash)[routeId - 1] = studyIndexTable.getStudyMaxCount();
                logi("initStudyIndexFromDataBase route id : " + routeId + " iccidHash = " + iccidHash + " index = " + this.mStudyIndexByIccId.get(iccidHash)[routeId - 1]);
            }
        }
    }

    private void updateStudyIndexByIccId(int routeId, String iccIdHash) {
        if (routeId > 2) {
            logi("route id invalid : " + routeId);
            return;
        }
        this.mStudyIndexByIccId.putIfAbsent(iccIdHash, new int[2]);
        int[] studyIndexs = this.mStudyIndexByIccId.get(iccIdHash);
        if (studyIndexs.length >= routeId) {
            int i = routeId - 1;
            studyIndexs[i] = studyIndexs[i] + 1;
            updateOrInsertStudyIndexToDb(new StudyIndexTable(routeId, iccIdHash, studyIndexs[routeId - 1]));
        }
    }

    private void updateOrInsertStudyIndexToDb(StudyIndexTable studyIndexTable) {
        HwSmartNetDb.getInstance().insertOrUpdateStudyIndex(studyIndexTable);
    }

    public boolean isAllSimSampleAndStudyDone(int routeId, String[] iccIdHash) {
        if (iccIdHash == null || iccIdHash.length < HwSmartNetConstants.SIM_NUM || routeId > 2) {
            loge("isAllSimSampleAndStudyDone return false");
            return false;
        }
        logd("isAllSimSampleAndStudyDone route id = " + routeId + " iccidHash[0] = " + iccIdHash[0] + " size = " + iccIdHash.length);
        boolean ret = false;
        for (int index = 0; index < HwSmartNetConstants.SIM_NUM; index++) {
            int[] studyCounts = this.mStudyIndexByIccId.getOrDefault(iccIdHash[index], new int[0]);
            StringBuilder sb = new StringBuilder();
            sb.append("isAllSimSampleAndStudyDone studyCounts length = ");
            sb.append(studyCounts.length);
            sb.append(" iccidhash = ");
            sb.append(iccIdHash[index]);
            sb.append(" study index = ");
            sb.append(studyCounts.length >= routeId ? studyCounts[routeId - 1] : 0);
            logd(sb.toString());
            if (studyCounts.length >= routeId && studyCounts[routeId - 1] >= 1) {
                ret = true;
            }
        }
        return ret;
    }

    private void analyseBlackPoint(CellSensor preSamplingInfo, CellSensor currentSampleInfo, String iccIdHash) {
        CellInfoPoint cellInfoPoint;
        long cellId = preSamplingInfo.getMainCellId();
        if (this.mCellInfoPointHashMap.containsKey(Long.valueOf(cellId))) {
            cellInfoPoint = this.mCellInfoPointHashMap.get(Long.valueOf(cellId));
        } else {
            cellInfoPoint = new CellInfoPoint(preSamplingInfo.getRouteId(), cellId, iccIdHash);
            this.mCellInfoPointHashMap.put(Long.valueOf(cellId), cellInfoPoint);
        }
        logi("before analyseBlackPoint CellInfoPoint = " + cellInfoPoint.toString() + " size = " + this.mCellInfoPointHashMap.size());
        cellInfoPoint.increaseTotalCounts();
        if (currentSampleInfo.getExceptionStartTime() != 0) {
            cellInfoPoint.updateBlackType(currentSampleInfo.getBlackPointType());
            cellInfoPoint.increaseExceptionCounts();
            long exceptionStartTime = currentSampleInfo.getExceptionStartTime();
            cellInfoPoint.setConvertToExceptionDuration(((cellInfoPoint.getConvertToExceptionDuration() + exceptionStartTime) - preSamplingInfo.getSamplingTime()) / 2);
        } else {
            cellInfoPoint.increaseNormalCounts();
        }
        cellInfoPoint.setExceptionProbability(((double) cellInfoPoint.getExceptionCounts()) / ((double) cellInfoPoint.getTotalCounts()));
        logi("after analyseBlackPoint CellInfoPoint = " + cellInfoPoint.toString() + " size = " + this.mCellInfoPointHashMap.size());
    }

    private void outputAnalyseResultToDb() {
        for (Long l : this.mCellInfoPointHashMap.keySet()) {
            HwSmartNetDb.getInstance().insertOrUpdateCellInfoPoint(this.mCellInfoPointHashMap.get(Long.valueOf(l.longValue())));
        }
    }

    public void startAnalyseSampleInfo() {
        int routeId = -1;
        for (int index = 0; index < HwSmartNetConstants.SIM_NUM; index++) {
            logi("startAnalyseSampleInfo phone id = " + index);
            int routeIdTmp = analyseSampleInfo(HwSmartNetSamplingStrategy.getInstance().getCellSensorInfo(index));
            int i = -1;
            if (routeIdTmp != -1) {
                i = routeIdTmp;
            }
            routeId = i;
        }
        analyseOneStudyDone(routeId);
    }

    private void initCellInfoPointHashMapFromDb(int routeId, String iccIdHash) {
        this.mCellInfoPointHashMap.clear();
        for (CellInfoPoint cellInfoPoint : HwSmartNetDb.getInstance().queryCellInfoPoints(routeId, iccIdHash)) {
            this.mCellInfoPointHashMap.put(Long.valueOf(cellInfoPoint.getCellIdentity()), cellInfoPoint);
        }
    }

    private int analyseSampleInfo(List<CellSensor> samplingInfos) {
        if (samplingInfos == null || samplingInfos.size() == 0) {
            logi("no sample info to analyse!");
            return -1;
        }
        String iccIdHash = HwSmartNetCommon.calcHash(samplingInfos.get(0).getIccid());
        initCellInfoPointHashMapFromDb(samplingInfos.get(0).getRouteId(), iccIdHash);
        CellSensor preSamplingInfo = null;
        for (CellSensor samplingInfo : samplingInfos) {
            logi("analyseSampleInfo : " + samplingInfo.toString());
            if (preSamplingInfo == null) {
                preSamplingInfo = samplingInfo;
            } else {
                analyseBlackPoint(preSamplingInfo, samplingInfo, iccIdHash);
                preSamplingInfo = samplingInfo;
            }
        }
        if (this.mCellInfoPointHashMap.keySet().size() > 0) {
            updateStudyIndexByIccId(samplingInfos.get(0).getRouteId(), iccIdHash);
        } else {
            logi("no valid analyse data, not update the study index!");
        }
        outputAnalyseResultToDb();
        recordBlackPointPathToDataBase();
        return samplingInfos.get(0).getRouteId();
    }

    private void analyseOneStudyDone(int routeId) {
        this.mStateHandler.obtainMessage(402, routeId, 0).sendToTarget();
    }

    public void getBlackPointPathFromDataBase(String iccIdHash) {
    }

    private void recordBlackPointPathToDataBase() {
    }

    class BlackPointPath {
        int pathIndex;
        List<CellSensor> paths;

        BlackPointPath() {
        }
    }
}
