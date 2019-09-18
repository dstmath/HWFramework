package com.huawei.wallet.sdk.common.apdu.tsm.requester;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.apdu.task.TsmParamQueryTask;
import com.huawei.wallet.sdk.common.apdu.tsm.commom.TsmOperationConstant;

public class DeleteAppletParamRequester extends TSMOperateParamRequester {
    private String bankRsaIndex;
    private final String mAid;
    private String verifySign;
    private String verifyTime;

    public DeleteAppletParamRequester(Context context, String aid, int mediaType) {
        super(context, "DeleteApplet", mediaType);
        this.mAid = aid;
    }

    public DeleteAppletParamRequester(Context context, String aid, String sign, String timeStamp, String rsaIndex, int mediaType) {
        super(context, "DeleteApplet", mediaType);
        this.mAid = aid;
        this.verifySign = sign;
        this.verifyTime = timeStamp;
        this.bankRsaIndex = rsaIndex;
    }

    /* access modifiers changed from: protected */
    public TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest paramQueryRequest) {
        String address = AddressNameMgr.getInstance().getAddress(TsmOperationConstant.TASK_COMMANDER_DEL_SSD, AddressNameMgr.MODULE_NAME_BANKCARD, null, this.mContext);
        paramQueryRequest.setAid(this.mAid);
        paramQueryRequest.setBankSignResult(this.verifySign);
        paramQueryRequest.setBankSignTime(this.verifyTime);
        paramQueryRequest.setBankRsaIndex(this.bankRsaIndex);
        return (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, address, TsmOperationConstant.TASK_COMMANDER_DEL_SSD).processTask(paramQueryRequest);
    }
}
