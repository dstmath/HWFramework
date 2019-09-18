package com.huawei.wallet.sdk.common.apdu.tsm.requester;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.commonbase.server.AddressNameMgr;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.apdu.task.TsmParamQueryTask;
import com.huawei.wallet.sdk.common.apdu.tsm.commom.TsmOperationConstant;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;

public class SynESEParamRequester extends TSMOperateParamRequester {
    private String aid;
    private String mSign;
    private String mSpID;
    private String mTimeStamp;

    public SynESEParamRequester(Context context, String aid2, int mediaType) {
        super(context, "SynESE", mediaType);
        this.aid = aid2;
    }

    public SynESEParamRequester(Context context, String spid, String sign, String timeStamp, int mediaType) {
        super(context, "SynESE", mediaType);
        this.mSign = sign;
        this.mSpID = spid;
        this.mTimeStamp = timeStamp;
    }

    /* access modifiers changed from: protected */
    public TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest paramQueryRequest) {
        paramQueryRequest.setAid(this.aid);
        if (!StringUtil.isEmpty(this.mSign, true)) {
            paramQueryRequest.setBankSignResult(this.mSign);
            paramQueryRequest.setSignType(TSMOperateParamRequester.SIGN_TYPE_SHA256);
        }
        if (!StringUtil.isEmpty(this.mTimeStamp, true)) {
            paramQueryRequest.setBankSignTime(this.mTimeStamp);
        }
        if (!StringUtil.isEmpty(this.mSpID, true)) {
            paramQueryRequest.setBankRsaIndex(this.mSpID);
        }
        return queryInfoSynTsmParam(paramQueryRequest);
    }

    public TsmParamQueryResponse queryInfoSynTsmParam(TsmParamQueryRequest request) {
        LogC.i("queryInfoSynTsmParam begin.", false);
        return (TsmParamQueryResponse) new TsmParamQueryTask(this.mContext, AddressNameMgr.getInstance().getAddress(TsmOperationConstant.TASK_COMMANDER_SYNC_INFO, AddressNameMgr.MODULE_NAME_BANKCARD, null, this.mContext), TsmOperationConstant.TASK_COMMANDER_SYNC_INFO).processTask(request);
    }
}
