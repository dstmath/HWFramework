package com.huawei.internal.telephony.smartnet;

import android.content.Context;
import android.os.Handler;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.SubscriptionControllerEx;
import com.huawei.internal.telephony.uicc.UiccControllerExt;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HwSmartNetEstimateStrategy {
    private static final int SUB1 = 0;
    private static final int SUB2 = 1;
    private static final String TAG = "HwSmartNetEstimateStrategy";
    private static HwSmartNetEstimateStrategy sInstance = null;
    private Context mContext = null;
    private CellSensor[] mCurrentEstimateCellDatas;
    private List<ArrayList<MatchInfo>> mMatchCellInfoPoints = new ArrayList();
    private Handler mStateHandler = null;

    private HwSmartNetEstimateStrategy(Context context, Handler stateHandler) {
        this.mContext = context;
        this.mStateHandler = stateHandler;
        for (int index = 0; index < 2; index++) {
            this.mMatchCellInfoPoints.add(new ArrayList<>());
        }
        this.mCurrentEstimateCellDatas = new CellSensor[HwSmartNetConstants.SIM_NUM];
        logi("create HwSmartNetEstimateStrategy");
    }

    public static HwSmartNetEstimateStrategy init(Context context, Handler stateHandler) {
        HwSmartNetEstimateStrategy hwSmartNetEstimateStrategy;
        synchronized (HwSmartNetEstimateStrategy.class) {
            if (sInstance == null) {
                sInstance = new HwSmartNetEstimateStrategy(context, stateHandler);
            } else {
                logi("init() called multiple times!  sInstance = " + sInstance);
            }
            hwSmartNetEstimateStrategy = sInstance;
        }
        return hwSmartNetEstimateStrategy;
    }

    public static HwSmartNetEstimateStrategy getInstance() {
        HwSmartNetEstimateStrategy hwSmartNetEstimateStrategy;
        synchronized (HwSmartNetEstimateStrategy.class) {
            hwSmartNetEstimateStrategy = sInstance;
        }
        return hwSmartNetEstimateStrategy;
    }

    /* access modifiers changed from: private */
    public static void logi(String msg) {
        RlogEx.i(TAG, msg);
    }

    /* access modifiers changed from: private */
    public static void logd(String msg) {
    }

    private List<CellInfoPoint> getMatchCellInfoPoints(int routeId, String iccIdHash, int count) {
        List<CellInfoPoint> cellInfoPoints = HwSmartNetDb.getInstance().queryMatchCellInfoPoints(routeId, iccIdHash);
        if (cellInfoPoints == null || cellInfoPoints.size() == 0) {
            return new ArrayList();
        }
        cellInfoPoints.sort(new Comparator<CellInfoPoint>() {
            /* class com.huawei.internal.telephony.smartnet.HwSmartNetEstimateStrategy.AnonymousClass1 */

            public int compare(CellInfoPoint cellInfoPoint1, CellInfoPoint cellInfoPoint2) {
                return Double.valueOf(cellInfoPoint2.getExceptionProbability()).compareTo(Double.valueOf(cellInfoPoint1.getExceptionProbability()));
            }
        });
        return cellInfoPoints.subList(0, Math.min(cellInfoPoints.size(), count));
    }

    private List<CellInfoPoint> readMatchInfoPointsFromDb(int routeId, int phoneId) {
        UiccControllerExt uiccController = UiccControllerExt.getInstance();
        return getMatchCellInfoPoints(routeId, HwSmartNetCommon.calcHash(uiccController.getUiccCard(phoneId) != null ? uiccController.getUiccCard(phoneId).getIccId() : null), 5);
    }

    public void initMatchInfoPoints() {
        int routeId = HwSmartNetRouteStrategy.getInstance().getCurrentRouteId();
        List<MatchInfo> matchInfosByRoutes = this.mMatchCellInfoPoints.get(routeId - 1);
        matchInfosByRoutes.clear();
        for (int index = 0; index < HwSmartNetConstants.SIM_NUM; index++) {
            List<CellInfoPoint> cellInfoPoints = readMatchInfoPointsFromDb(routeId, index);
            if (cellInfoPoints == null || cellInfoPoints.size() == 0) {
                matchInfosByRoutes.add(new MatchInfo());
                logi("initMatchInfoPoints index " + index + " fail.");
            } else {
                matchInfosByRoutes.add(new MatchInfo(cellInfoPoints));
                logi("initMatchInfoPoints index = " + index + " size = " + cellInfoPoints.size());
            }
        }
    }

    private void processSimHotplugOut() {
        for (int routeId = 0; routeId < 2; routeId++) {
            List<MatchInfo> matchInfosByRoutes = this.mMatchCellInfoPoints.get(routeId);
            for (int index = 0; index < HwSmartNetConstants.SIM_NUM; index++) {
                matchInfosByRoutes.get(index).clearMatchInfo();
            }
        }
    }

    private boolean judgeIfSwitchMainSlot(int routeId, int mainSlotId) {
        int slaveSlotId = mainSlotId == 0 ? 1 : 0;
        MatchInfo matchInfoSlaveSlot = this.mMatchCellInfoPoints.get(routeId - 1).get(slaveSlotId);
        if (!this.mMatchCellInfoPoints.get(routeId - 1).get(mainSlotId).judgeIsMatched(mainSlotId) || matchInfoSlaveSlot.judgeIsMatched(slaveSlotId)) {
            return false;
        }
        logi("switch dds to slave slot : " + slaveSlotId);
        SubscriptionControllerEx.getInstance().setDefaultDataSubId(SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(slaveSlotId));
        return true;
    }

    public boolean executeEstimate(int phoneId, Object object) {
        if (!HwSmartNetCommon.isValidSlotId(phoneId) || !(object instanceof CellSensor)) {
            return false;
        }
        this.mCurrentEstimateCellDatas[phoneId] = (CellSensor) object;
        int routeId = HwSmartNetRouteStrategy.getInstance().getCurrentRouteId();
        logi("matchInfosByRoutes size = " + this.mMatchCellInfoPoints.get(routeId - 1).size());
        for (int index = 0; index < HwSmartNetConstants.SIM_NUM; index++) {
            if (this.mCurrentEstimateCellDatas[index] == null) {
                logi("executeEstimate: CurrentEstimateCellData index : " + index + " is null.");
                return false;
            }
        }
        if (SubscriptionControllerEx.getInstance().getDefaultDataSubId() == SubscriptionControllerEx.getInstance().getSubIdUsingPhoneId(phoneId)) {
            return judgeIfSwitchMainSlot(routeId, phoneId);
        }
        logi("slave slot just update current cell data.");
        return false;
    }

    public boolean isMatchInfoStudied() {
        return !this.mMatchCellInfoPoints.get(HwSmartNetRouteStrategy.getInstance().getCurrentRouteId() - 1).stream().allMatch($$Lambda$HwSmartNetEstimateStrategy$9pKJVjPcAgwwa_w3a65o77b4ZC0.INSTANCE);
    }

    static /* synthetic */ boolean lambda$isMatchInfoStudied$0(MatchInfo matchInfosByRoute) {
        return matchInfosByRoute.getBlackPointsCounts() == 0;
    }

    public void endAndUpdateMatchInfo(int routeId) {
        if (routeId == -1) {
            logi("route id invalid!");
        } else if (!HwSmartNetStateListener.getInstance().isAllSimLoaded() || !HwSmartNetStudyStrategy.getInstance().isAllSimSampleAndStudyDone(routeId, HwSmartNetStateListener.getInstance().getIccIdHashs())) {
            logi("has not start estimate, not update!");
        } else {
            List<MatchInfo> matchInfosByRoutes = this.mMatchCellInfoPoints.get(routeId - 1);
            logi("endAndUpdateMatchInfo routeId = " + routeId + " matchInfo size = " + matchInfosByRoutes.size());
            for (int index = 0; index < HwSmartNetConstants.SIM_NUM; index++) {
                List<CellInfoPoint> cellInfoPoints = readMatchInfoPointsFromDb(routeId, index);
                if (!(cellInfoPoints == null || cellInfoPoints.size() == 0)) {
                    matchInfosByRoutes.set(index, new MatchInfo(cellInfoPoints));
                }
            }
        }
    }

    private class BlackPointPath {
        ArrayList<CellSensor> blackPointPaths;

        private BlackPointPath() {
        }
    }

    /* access modifiers changed from: private */
    public class MatchInfo {
        List<CellInfoPoint> blackPoints;

        MatchInfo() {
        }

        MatchInfo(List<CellInfoPoint> cellInfoPoints) {
            this.blackPoints = cellInfoPoints;
        }

        /* access modifiers changed from: package-private */
        public void clearMatchInfo() {
            this.blackPoints = null;
        }

        /* access modifiers changed from: package-private */
        public int getBlackPointsCounts() {
            List<CellInfoPoint> list = this.blackPoints;
            if (list == null) {
                return 0;
            }
            return list.size();
        }

        /* access modifiers changed from: package-private */
        public boolean judgeIsMatched(int slotId) {
            if (this.blackPoints == null) {
                return false;
            }
            HwSmartNetEstimateStrategy.logi("mCurrentEstimateCellDatas[slotId].getMainCellId() = " + HwSmartNetEstimateStrategy.this.mCurrentEstimateCellDatas[slotId].getMainCellId());
            for (CellInfoPoint cellInfoPoint : this.blackPoints) {
                HwSmartNetEstimateStrategy.logd("match info point : " + cellInfoPoint.toString());
                if (cellInfoPoint.getCellIdentity() == HwSmartNetEstimateStrategy.this.mCurrentEstimateCellDatas[slotId].getMainCellId()) {
                    return true;
                }
            }
            return false;
        }
    }
}
