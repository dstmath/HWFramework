package com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.common;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.walletbase.logic.whitecard.DeleteWhiteCardOperator;
import com.huawei.wallet.sdk.business.idcard.walletbase.whitecard.BaseResultHandler;

public class WhiteCardOperatorImpl implements WhiteCardOperatorApi {
    private Context mContext;

    public WhiteCardOperatorImpl(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void deleteWhiteCard(String passTypeId, String passId, String aid, BaseResultHandler resultHandler) {
        new DeleteWhiteCardOperator(this.mContext, resultHandler).deleteWhiteCard(passTypeId, passId, aid);
    }
}
