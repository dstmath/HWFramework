package com.huawei.wallet.sdk.business.idcard.idcard.logic;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.idcard.server.IdCardServerOperator;
import com.huawei.wallet.sdk.business.idcard.idcard.server.IdCardServerOperatorResult;
import com.huawei.wallet.sdk.business.idcard.idcard.storage.CtidTaManager;
import com.huawei.wallet.sdk.common.log.LogC;

public class CtidDeleteLogic {
    private static final String TAG = "IDCard:CtidDeleteLogic";

    public static boolean deleteCtid(Context context, String issuerId, String aid) {
        IdCardServerOperator mOperator = new IdCardServerOperator(context, issuerId, aid);
        LogC.i(TAG, "deleteSSD begin", false);
        boolean ret = handleResult(mOperator.deleteSSD());
        if (!ret) {
            reportResult(context, ret);
            return false;
        }
        LogC.i(TAG, "delete card info in TA.", false);
        boolean ret2 = CtidTaManager.getInstance(context).deleteCtidCardInfo();
        reportResult(context, ret2);
        if (!ret2) {
            return false;
        }
        return true;
    }

    private static boolean handleResult(IdCardServerOperatorResult result) {
        int resultCode = result.getResultCode();
        if (resultCode != 0) {
            LogC.e(TAG, "deleteSSD fail, result = " + resultCode + ", msg = " + result.getResultMsg(), false);
            return false;
        }
        LogC.i(TAG, "deleteSSD success", false);
        return true;
    }

    private static void reportResult(Context context, boolean ret) {
        if (!ret) {
            LogC.e(TAG, "reportResult fail, ret = " + ret, false);
            return;
        }
        LogC.i(TAG, "reportResult success", false);
    }
}
