package com.huawei.wallet.sdk.common.apdu.tsm.requester;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.apdu.task.TsmParamQueryTask;
import com.huawei.wallet.sdk.common.apdu.tsm.commom.TsmOperationConstant;

public class CreateOrDeleteOpenSSDParamRequester extends TSMOperateParamRequester {
    public static final String OPERATOR_TYPE_CREATE_SSD = "createSSD";
    public static final String OPERATOR_TYPE_DELETE_SSD = "deleteSSD";
    private final String mAid;
    private final String mOperatorType;
    private final String mSign;
    private final String mSpID;
    private final String mTimeStamp;

    public CreateOrDeleteOpenSSDParamRequester(Context context, String aid, String spId, String sign, String timeStamp, String operatorType, int mediaType) {
        super(context, "CreateOrDeleteOpenSSD", mediaType);
        this.mAid = aid;
        this.mSign = sign;
        this.mSpID = spId;
        this.mTimeStamp = timeStamp;
        this.mOperatorType = operatorType;
    }

    /* access modifiers changed from: protected */
    public TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest paramQueryRequest) {
        String address = AddressNameMgr.getInstance().getAddress(TsmOperationConstant.TASK_COMMANDER_DEL_SSD, AddressNameMgr.MODULE_NAME_BANKCARD, null, this.mContext);
        paramQueryRequest.setAid(this.mAid);
        paramQueryRequest.setBankSignResult(this.mSign);
        paramQueryRequest.setBankSignTime(this.mTimeStamp);
        paramQueryRequest.setBankRsaIndex(this.mSpID);
        paramQueryRequest.setSignType(TSMOperateParamRequester.SIGN_TYPE_SHA256);
        return (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, address, TsmOperationConstant.TASK_COMMANDER_DEL_SSD).processTask(paramQueryRequest);
    }
}
