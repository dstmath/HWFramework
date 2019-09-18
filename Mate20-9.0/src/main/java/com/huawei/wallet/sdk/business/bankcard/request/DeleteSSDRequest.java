package com.huawei.wallet.sdk.business.bankcard.request;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.server.TsmOperationServiceImpl;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.TSMOperateParamRequester;

public class DeleteSSDRequest extends TSMOperateParamRequester {
    private final String aid;
    private boolean deleteRelatedObjects = false;

    public DeleteSSDRequest(Context context, String aid2, boolean deleteRelatedObjects2, int mediaType) {
        super(context, "DeleteSSD", mediaType);
        this.aid = aid2;
        this.deleteRelatedObjects = deleteRelatedObjects2;
    }

    /* access modifiers changed from: protected */
    public TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest paramQueryRequest) {
        paramQueryRequest.setAid(this.aid);
        paramQueryRequest.setDeleteRelatedObjects(this.deleteRelatedObjects);
        return new TsmOperationServiceImpl(this.mContext).queryDeleteSSDTsmParam(paramQueryRequest);
    }
}
