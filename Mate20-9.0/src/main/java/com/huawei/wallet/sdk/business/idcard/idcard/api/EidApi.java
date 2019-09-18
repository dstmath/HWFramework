package com.huawei.wallet.sdk.business.idcard.idcard.api;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.idcard.logic.EidDeleteLogic;
import com.huawei.wallet.sdk.business.idcard.idcard.storage.EidCache;

public class EidApi {
    public static boolean isEid(int groupType, String issuerId, Context context) {
        return 3 == groupType && EidCache.getInstance(context).getEidIssuerId().equals(issuerId);
    }

    public static boolean handlerDeviceReset(Context context) {
        return EidDeleteLogic.deleteEid(context, EidCache.getInstance(context).getEidIssuerId(), EidCache.getInstance(context).getAid(), EidDeleteLogic.DEVICE_RESET);
    }
}
