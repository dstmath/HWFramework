package com.android.server.hidata.wavemapping.statehandler;

import android.common.HwFrameworkFactory;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class HwWmpFastBackLteManager {
    private static final String KEY_CELL_ID = "CellId";
    private static final String KEY_EARFCN = "Earfcn";
    private static final String KEY_LAI = "Lai";
    private static final String KEY_PLMN_ID = "PlmnId";
    private static final String KEY_RAT = "Rat";
    private static final String KEY_SUB_ID = "SubId";
    private static final String REPORT_PACKAGE_NAME = "com.android.server.hidata.appqoe";
    private static final String TAG = ("WMapping." + HwWmpFastBackLteManager.class.getSimpleName());
    public static final int WAVEMAPPING_DATA_BACK_TO_LTE = 304;
    private static HwWmpFastBackLteManager mHwWmpFastBackLteManager = null;
    private IHwCommBoosterServiceManager mBooster = null;

    public HwWmpFastBackLteManager() {
        LogUtil.i(false, "HwWmpFastBackLteManager", new Object[0]);
        try {
            this.mBooster = HwFrameworkFactory.getHwCommBoosterServiceManager();
        } catch (Exception e) {
            LogUtil.e(false, "getHwCommBoosterServiceManager failed by Exception", new Object[0]);
        }
    }

    public static synchronized HwWmpFastBackLteManager getInstance() {
        HwWmpFastBackLteManager hwWmpFastBackLteManager;
        synchronized (HwWmpFastBackLteManager.class) {
            if (mHwWmpFastBackLteManager == null) {
                mHwWmpFastBackLteManager = new HwWmpFastBackLteManager();
            }
            hwWmpFastBackLteManager = mHwWmpFastBackLteManager;
        }
        return hwWmpFastBackLteManager;
    }

    public void sendDataToBooster(HwWmpFastBackLte mBack) {
        LogUtil.i(false, "sendDataToBooster", new Object[0]);
        Bundle data = new Bundle();
        data.putInt(KEY_SUB_ID, mBack.mSubId);
        data.putInt(KEY_PLMN_ID, mBack.mPlmnId);
        data.putInt(KEY_RAT, mBack.mRat);
        data.putInt(KEY_EARFCN, mBack.mEarfcn);
        data.putInt(KEY_LAI, mBack.mLai);
        data.putInt(KEY_CELL_ID, mBack.mCellId);
        LogUtil.d(false, "Back2Lte: SubID=%{public}d PlmnId=%{private}d Rat=%{public}d Earfcn=%{public}d Lai=%{public}d CellId=%{private}d", Integer.valueOf(mBack.mSubId), Integer.valueOf(mBack.mPlmnId), Integer.valueOf(mBack.mRat), Integer.valueOf(mBack.mEarfcn), Integer.valueOf(mBack.mEarfcn), Integer.valueOf(mBack.mCellId));
        int ret = this.mBooster.reportBoosterPara(REPORT_PACKAGE_NAME, (int) WAVEMAPPING_DATA_BACK_TO_LTE, data);
        if (ret != 0) {
            LogUtil.e(false, "reportBoosterPara failed, ret=%{public}d", Integer.valueOf(ret));
        }
    }
}
