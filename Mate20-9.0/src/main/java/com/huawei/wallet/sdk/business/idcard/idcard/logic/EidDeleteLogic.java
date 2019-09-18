package com.huawei.wallet.sdk.business.idcard.idcard.logic;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.idcard.EidCardRepository;
import com.huawei.wallet.sdk.business.idcard.idcard.server.EIdCardServerOperator;
import com.huawei.wallet.sdk.business.idcard.idcard.server.EIdCardServerOperatorResult;
import com.huawei.wallet.sdk.business.idcard.idcard.storage.CtidTaManager;
import com.huawei.wallet.sdk.common.log.LogC;

public class EidDeleteLogic {
    private static final String DEFAULT = "default";
    public static final String DEVICE_RESET = "deviceReset";
    private static final String TAG = "IDCard:EidDeleteLogic";

    public static boolean deleteEid(Context context, String issuerId, String aid, String operationType) {
        EIdCardServerOperator mOperator = new EIdCardServerOperator(context, issuerId, aid);
        LogC.i(TAG, "deleteSSD begin", false);
        EIdCardServerOperatorResult result = mOperator.deleteSSD();
        if (DEVICE_RESET.equals(operationType)) {
            LogC.i(TAG, "cancelEID begin", false);
            result = mOperator.cancelEID();
        }
        boolean ret = handleResult(result);
        if (!ret) {
            reportResult(context, ret);
            return false;
        }
        LogC.i(TAG, "delete card info in TA.", false);
        boolean ret2 = CtidTaManager.getInstance(context).deleteEidCardInfo();
        reportResult(context, ret2);
        if (!ret2) {
            return false;
        }
        EidCardRepository.getInstance(context).refreshCardInfo(false);
        return true;
    }

    private static boolean handleResult(EIdCardServerOperatorResult result) {
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
