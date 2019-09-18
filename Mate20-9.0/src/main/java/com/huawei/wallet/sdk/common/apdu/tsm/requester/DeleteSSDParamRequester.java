package com.huawei.wallet.sdk.common.apdu.tsm.requester;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.apdu.task.TsmParamQueryTask;
import com.huawei.wallet.sdk.common.apdu.tsm.commom.TsmOperationConstant;

public class DeleteSSDParamRequester extends TSMOperateParamRequester {
    private final String aid;
    private boolean deleteRelatedObjects = false;
    private String issueId;

    public DeleteSSDParamRequester(Context context, String aid2, int mediaType) {
        super(context, "DeleteSSD", mediaType);
        this.aid = aid2;
    }

    public DeleteSSDParamRequester(Context context, String aid2, boolean deleteRelatedObjects2, int mediaType) {
        super(context, "DeleteSSD", mediaType);
        this.aid = aid2;
        this.deleteRelatedObjects = deleteRelatedObjects2;
    }

    public DeleteSSDParamRequester(Context context, String aid2, String issueId2, boolean deleteRelatedObjects2, int mediaType) {
        super(context, "DeleteSSD", mediaType);
        this.aid = aid2;
        this.issueId = issueId2;
        this.deleteRelatedObjects = deleteRelatedObjects2;
    }

    /* access modifiers changed from: protected */
    public TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest paramQueryRequest) {
        String address = AddressNameMgr.getInstance().getAddress(TsmOperationConstant.TASK_COMMANDER_DEL_SSD, AddressNameMgr.MODULE_NAME_BANKCARD, null, this.mContext);
        paramQueryRequest.setAid(this.aid);
        paramQueryRequest.setIssuerId(this.issueId);
        paramQueryRequest.setDeleteRelatedObjects(this.deleteRelatedObjects);
        return (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, address, TsmOperationConstant.TASK_COMMANDER_DEL_SSD).processTask(paramQueryRequest);
    }
}
