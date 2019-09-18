package com.android.server.security.tsmagent.logic.spi.tsm.laser;

import android.content.Context;
import com.leisen.wallet.sdk.bean.CommonRequestParams;
import com.leisen.wallet.sdk.tsm.TSMOperator;

public final class LaserTSMServiceCommonExecute extends LaserTSMService {
    public LaserTSMServiceCommonExecute(Context context, int reader) {
        super(context, reader);
    }

    /* access modifiers changed from: package-private */
    public String getRemoteUrl() {
        return super.getTsmUrl();
    }

    /* access modifiers changed from: package-private */
    public int excuteTSMcommand(TSMOperator operator, CommonRequestParams requestParams) {
        return operator.commonExecute(requestParams);
    }
}
