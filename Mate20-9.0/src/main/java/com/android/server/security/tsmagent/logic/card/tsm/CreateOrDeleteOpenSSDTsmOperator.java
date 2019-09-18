package com.android.server.security.tsmagent.logic.card.tsm;

import android.content.Context;
import com.android.server.security.tsmagent.logic.spi.tsm.laser.LaserTSMServiceCommonExecute;
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

    /* access modifiers changed from: package-private */
    public TsmParamQueryResponse queryOperateParams(TsmParamQueryRequest paramQueryRequest) {
        paramQueryRequest.setAid(this.mAid);
        paramQueryRequest.setBankSignResult(this.mSign);
        paramQueryRequest.setBankSignTime(this.mTimeStamp);
        paramQueryRequest.setBankRsaIndex(this.mSpID);
        paramQueryRequest.setSignType(TsmBaseOperator.SIGN_TYPE_SHA256);
        if (needEnhancedSigned()) {
            paramQueryRequest.setSignCommandEx(true);
        }
        if ("createSSD".equals(this.mOperatorType)) {
            return CardServer.getInstance(this.mContext).queryCreateSSDTsmParam(paramQueryRequest);
        }
        if (!"deleteSSD".equals(this.mOperatorType)) {
            return null;
        }
        paramQueryRequest.setDeleteRelatedObjects(true);
        return CardServer.getInstance(this.mContext).queryDeleteSSDTsmParam(paramQueryRequest);
    }

    private boolean needEnhancedSigned() {
        return this.mSpID.startsWith("NFCDK_");
    }

    public int excute(int reader) {
        return super.excute_withAction(reader, new LaserTSMServiceCommonExecute(this.mContext, reader));
    }
}
