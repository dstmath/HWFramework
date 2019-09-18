package com.huawei.wallet.sdk.business.idcard.idcard.api;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.idcard.logic.CtidDeleteLogic;
import com.huawei.wallet.sdk.business.idcard.idcard.storage.CtidCache;
import com.huawei.wallet.sdk.business.idcard.idcard.storage.EidCache;

public class CtidApi {
    public static boolean isCtid(int groupType, String issuerId, Context context) {
        return 3 == groupType && EidCache.getInstance(context).getCtidIssuerId().equals(issuerId);
    }

    public static boolean handlerDeviceReset(Context context) {
        return CtidDeleteLogic.deleteCtid(context, EidCache.getInstance(context).getCtidIssuerId(), CtidCache.getInstance().getAid());
    }
}
