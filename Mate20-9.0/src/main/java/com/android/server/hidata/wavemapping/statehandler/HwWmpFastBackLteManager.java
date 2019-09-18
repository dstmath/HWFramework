package com.android.server.hidata.wavemapping.statehandler;

import android.common.HwFrameworkFactory;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import com.android.server.hidata.wavemapping.util.LogUtil;

public class HwWmpFastBackLteManager {
    private static final String TAG = ("WMapping." + HwWmpFastBackLteManager.class.getSimpleName());
    public static final int WAVEMAPPING_DATA_BACK_TO_LTE = 304;
    private static HwWmpFastBackLteManager mHwWmpFastBackLteManager = null;
    private IHwCommBoosterServiceManager mBooster = null;

    public HwWmpFastBackLteManager() {
        LogUtil.i("HwWmpFastBackLteManager");
        try {
            this.mBooster = HwFrameworkFactory.getHwCommBoosterServiceManager();
        } catch (Exception e) {
            LogUtil.e(TAG + " HwWmpFastBackLteManager " + e.getMessage());
            e.printStackTrace();
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

    public void SendDataToBooster(HwWmpFastBackLte mBack) {
        LogUtil.i("SendDataToBooster");
        Bundle data = new Bundle();
        data.putInt("SubId", mBack.mSubId);
        data.putInt("PlmnId", mBack.mPlmnId);
        data.putInt("Rat", mBack.mRat);
        data.putInt("Earfcn", mBack.mEarfcn);
        data.putInt("Lai", mBack.mLai);
        data.putInt("CellId", mBack.mCellId);
        LogUtil.d("Back2Lte: SubID=" + mBack.mSubId + " PlmnId=" + mBack.mPlmnId + " Rat=" + mBack.mRat + " Earfcn=" + mBack.mEarfcn + " Lai=" + mBack.mEarfcn + " CellId=" + mBack.mCellId);
        int ret = this.mBooster.reportBoosterPara("com.android.server.hidata.appqoe", 304, data);
        if (ret != 0) {
            LogUtil.e("reportBoosterPara failed, ret=" + ret);
        }
    }
}
