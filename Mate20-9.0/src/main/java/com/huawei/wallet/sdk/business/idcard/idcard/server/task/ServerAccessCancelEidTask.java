package com.huawei.wallet.sdk.business.idcard.idcard.server.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.idcard.server.card.NewHttpConnTask;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessCancelEidRequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessCancelEidResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.util.SignJsonDataUtil;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.common.log.LogC;
import org.json.JSONObject;

public class ServerAccessCancelEidTask extends NewHttpConnTask<ServerAccessCancelEidResponse, ServerAccessCancelEidRequest> {
    private static final String CANCEL_EID_COMMANDER = "eid.cancel";
    private static final String TAG = "IDCard:ServerAccessCancelEidTask";

    public ServerAccessCancelEidTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(ServerAccessCancelEidRequest params) {
        String str = null;
        if (params == null || !params.valid()) {
            LogC.e("IDCard:ServerAccessCancelEidTask prepareRequestStr params error.", false);
            return null;
        }
        JSONObject dataObject = params.createRequestData(JSONHelper.createHeaderStr(params.getSrcTransactionID(), CANCEL_EID_COMMANDER, params.getIsNeedServiceTokenAuth()));
        if (dataObject != null) {
            str = SignJsonDataUtil.signJsonData(dataObject, this.mContext);
        }
        return str;
    }

    /* access modifiers changed from: protected */
    public ServerAccessCancelEidResponse readErrorResponse(int errorCode, String errorMessage) {
        ServerAccessCancelEidResponse response = new ServerAccessCancelEidResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        return response;
    }

    /* access modifiers changed from: protected */
    public ServerAccessCancelEidResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        ServerAccessCancelEidResponse response = new ServerAccessCancelEidResponse();
        response.returnCode = returnCode;
        setErrorInfo(dataObject, response);
        return response;
    }
}
