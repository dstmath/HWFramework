package com.huawei.wallet.sdk.business.idcard.walletbase.tcis.impl;

import android.content.Context;
import android.util.Log;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.idcard.commonbase.util.log.LogErrorConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.tcis.request.TcisRequest;
import com.huawei.wallet.sdk.business.idcard.walletbase.tcis.response.TcisResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.impl.BaseWalletTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.impl.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.response.CardServerBaseResponse;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class GetSessionKeyTcisTask extends BaseWalletTask<TcisResponse, TcisRequest> {
    private static final String COMMANDDER = "get.sessionKey";
    private final String TAG = getClass().getSimpleName();
    private CallBack callBack;

    public GetSessionKeyTcisTask(Context context, String url, CallBack callBack2) {
        super(context, url);
        this.callBack = callBack2;
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(TcisRequest params) {
        if (params == null || StringUtil.isEmpty(params.getSrcTransactionID(), true) || StringUtil.isEmpty(params.getMerchantID(), true)) {
            LogC.e(this.TAG + " prepareRequestStr, params invalid.", false);
            return null;
        }
        return JSONHelper.createRequestStr(params.getMerchantID(), params.getRsaKeyIndex(), createDataStr(JSONHelper.createHeaderStr(params.getSrcTransactionID(), COMMANDDER, params.getIsNeedServiceTokenAuth()), params), this.mContext);
    }

    private JSONObject createDataStr(JSONObject headerObject, TcisRequest request) {
        JSONObject returnDataStr = null;
        if (headerObject == null) {
            LogC.e(this.TAG + " createDataStr, headerObject is null.", false);
            return null;
        }
        try {
            returnDataStr = new JSONObject();
            returnDataStr.put("header", headerObject);
            returnDataStr.put("tcisid", request.getTcisID());
            returnDataStr.put("additionalAuthData", request.getAdditionAuthData());
            returnDataStr.put("taVersion", request.getTA_VERSION());
            returnDataStr.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            return returnDataStr;
        } catch (JSONException e) {
            LogC.e(this.TAG + " createDataStr parse json error:" + e.getMessage(), true);
            return returnDataStr;
        }
    }

    /* access modifiers changed from: protected */
    public TcisResponse readErrorResponse(int errorCode) {
        if (this.callBack != null) {
            this.callBack.onFail(errorCode);
        }
        String str = this.TAG;
        Log.e(str, "err: " + errorCode);
        return new TcisResponse();
    }

    /* access modifiers changed from: protected */
    public TcisResponse readSuccessResponse(String responseStr) {
        LogC.i(this.TAG, "prepareRequestStr: success", false);
        TcisResponse response = new TcisResponse();
        resolveResponse(response, responseStr);
        if (this.callBack != null) {
            this.callBack.onSucess(response);
        }
        return response;
    }

    /* access modifiers changed from: protected */
    public void makeResponseData(CardServerBaseResponse response, JSONObject dataObject) throws JSONException {
        JSONObject data = dataObject;
        if (response instanceof TcisResponse) {
            TcisResponse resp = (TcisResponse) response;
            String[] tempdata = data.optString("encryptedSessionKey").split(":");
            if (tempdata.length == 2) {
                resp.setKaVersion(Integer.parseInt(tempdata[0]));
                resp.setkAInfo(tempdata[1]);
            }
        }
    }

    /* access modifiers changed from: protected */
    public String getTag() {
        return "GetSessionKeyTcisTask";
    }

    /* access modifiers changed from: protected */
    public int getErrorLogConstant() {
        return LogErrorConstant.GET_TCIS_SEESION_KEY_ERROR;
    }
}
