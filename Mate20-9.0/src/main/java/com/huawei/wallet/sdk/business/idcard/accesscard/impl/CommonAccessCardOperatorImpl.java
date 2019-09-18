package com.huawei.wallet.sdk.business.idcard.accesscard.impl;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.accesscard.api.AccessCardOperator;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.exception.AccessCardOperatorException;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler.HandleNullifyResultHandler;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.resulthandler.InitAccessCardResultHandler;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.task.DeteleAccessCardOperator;
import com.huawei.wallet.sdk.business.idcard.accesscard.logic.task.InitAccessCardOperator;
import com.huawei.wallet.sdk.common.ta.TACardInfo;
import java.util.List;

public class CommonAccessCardOperatorImpl implements AccessCardOperator {
    private Context mContext;

    public CommonAccessCardOperatorImpl(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void init(List<TACardInfo> list, InitAccessCardResultHandler handler) throws AccessCardOperatorException {
        new InitAccessCardOperator(this.mContext, handler).init(list);
    }

    public void uninstallAccessCard(String issurId, String aid, boolean updateTA, HandleNullifyResultHandler handler) throws AccessCardOperatorException {
        DeteleAccessCardOperator operator = new DeteleAccessCardOperator(this.mContext, issurId, aid, updateTA, handler);
        operator.uninstall();
    }
}
