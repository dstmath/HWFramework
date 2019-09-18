package com.huawei.wallet.sdk.business.bankcard.request;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.server.TsmOperationServiceImpl;
import com.huawei.wallet.sdk.common.apdu.request.TsmParamQueryRequest;
import com.huawei.wallet.sdk.common.apdu.response.TsmParamQueryResponse;
import com.huawei.wallet.sdk.common.apdu.tsm.requester.TSMOperateParamRequester;

public class DeleteAppletRequest extends TSMOperateParamRequester {
    private String bankRsaIndex;
    private final String mAid;
    private String verifySign;
    private String verifyTime;

    public DeleteAppletRequest(Context context, String aid, int mediaType) {
        super(context, "DeleteApplet", mediaType);
        this.mAid = aid;
    }

    /* access modifiers changed from: protected */
    public TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest paramQueryRequest) {
        paramQueryRequest.setAid(this.mAid);
        paramQueryRequest.setBankSignResult(this.verifySign);
        paramQueryRequest.setBankSignTime(this.verifyTime);
        paramQueryRequest.setBankRsaIndex(this.bankRsaIndex);
        return new TsmOperationServiceImpl(this.mContext).queryDeleteAppletTsmParam(paramQueryRequest);
    }

    public void setVerifySign(String verifySign2) {
        this.verifySign = verifySign2;
    }

    public void setVerifyTime(String verifyTime2) {
        this.verifyTime = verifyTime2;
    }

    public void setBankRsaIndex(String bankRsaIndex2) {
        this.bankRsaIndex = bankRsaIndex2;
    }
}
