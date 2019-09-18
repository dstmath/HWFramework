package com.huawei.wallet.sdk.common.apdu.tsm.requester;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.apdu.task.TsmParamQueryTask;
import com.huawei.wallet.sdk.common.apdu.tsm.commom.TsmOperationConstant;
import com.huawei.wallet.sdk.common.log.LogC;

public class InitEseParamRequester extends TSMOperateParamRequester {
    public InitEseParamRequester(Context context, int mediaType) {
        super(context, "InitEse", mediaType);
    }

    /* access modifiers changed from: protected */
    public TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest paramQueryRequest) {
        TsmParamQueryResponse response = (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, AddressNameMgr.getInstance().getAddress(TsmOperationConstant.TASK_COMMANDER_INFO_INIT, AddressNameMgr.MODULE_NAME_BANKCARD, null, this.mContext), TsmOperationConstant.TASK_COMMANDER_INFO_INIT).processTask(paramQueryRequest);
        LogC.i("InitEseParamRequester", "queryInfoInitTsmParam end.", false);
        return response;
    }
}
