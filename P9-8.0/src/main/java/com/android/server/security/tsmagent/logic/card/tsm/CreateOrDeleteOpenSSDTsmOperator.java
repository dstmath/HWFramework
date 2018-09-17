package com.android.server.security.tsmagent.logic.card.tsm;

import android.content.Context;
import com.android.server.security.tsmagent.openapi.ITSMOperator;
import com.android.server.security.tsmagent.server.card.impl.CardServer;
import com.android.server.security.tsmagent.server.card.request.TsmParamQueryRequest;
import com.android.server.security.tsmagent.server.card.response.TsmParamQueryResponse;

public class CreateOrDeleteOpenSSDTsmOperator extends TsmBaseOperator {
    private final String mAid;
    private final String mOperatorType;
    private final String mSign;
    private final String mSpID;
    private final String mTimeStamp;

    public CreateOrDeleteOpenSSDTsmOperator(Context context, String aid, String spId, String sign, String timeStamp, String operatorType) {
        super(context, "CreateOrDeleteOpenSSD");
        this.mAid = aid;
        this.mSign = sign;
        this.mSpID = spId;
        this.mTimeStamp = timeStamp;
        this.mOperatorType = operatorType;
    }

    TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest paramQueryRequest) {
        paramQueryRequest.setAid(this.mAid);
        paramQueryRequest.setBankSignResult(this.mSign);
        paramQueryRequest.setBankSignTime(this.mTimeStamp);
        paramQueryRequest.setBankRsaIndex(this.mSpID);
        paramQueryRequest.setSignType(TsmBaseOperator.SIGN_TYPE_SHA256);
        if (ITSMOperator.OPERATOR_TYPE_CREATE_SSD.equals(this.mOperatorType)) {
            return CardServer.getInstance(this.mContext).queryCreateSSDTsmParam(paramQueryRequest);
        }
        if (ITSMOperator.OPERATOR_TYPE_DELETE_SSD.equals(this.mOperatorType)) {
            return CardServer.getInstance(this.mContext).queryDeleteSSDTsmParam(paramQueryRequest);
        }
        return null;
    }
}
