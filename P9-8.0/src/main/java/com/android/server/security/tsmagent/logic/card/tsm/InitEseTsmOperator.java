package com.android.server.security.tsmagent.logic.card.tsm;

import android.content.Context;
import com.android.server.security.tsmagent.server.card.impl.CardServer;
import com.android.server.security.tsmagent.server.card.request.TsmParamQueryRequest;
import com.android.server.security.tsmagent.server.card.response.TsmParamQueryResponse;

public class InitEseTsmOperator extends TsmBaseOperator {
    private final String mSign;
    private final String mSpID;
    private final String mTimeStamp;

    public InitEseTsmOperator(Context context, String spId, String sign, String timeStamp) {
        super(context, "InitEse");
        this.mSpID = spId;
        this.mSign = sign;
        this.mTimeStamp = timeStamp;
    }

    TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest paramQueryRequest) {
        paramQueryRequest.setBankSignResult(this.mSign);
        paramQueryRequest.setBankSignTime(this.mTimeStamp);
        paramQueryRequest.setBankRsaIndex(this.mSpID);
        paramQueryRequest.setSignType(TsmBaseOperator.SIGN_TYPE_SHA256);
        return CardServer.getInstance(this.mContext).queryInfoInitTsmParam(paramQueryRequest);
    }
}
