package com.huawei.wallet.sdk.business.idcard.walletbase.pass;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.impl.BaseWalletTask;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.impl.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.response.CardServerBaseResponse;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class PassTypeIdInfoFetchTask extends BaseWalletTask<PassTypeIdInfoResponse, PassTypeIdInfoRequest> {
    private static final String HEAD_COMMANDER = "query.pass.type";
    private static final String TAG = "PassTypeIdInfoFetchTask";

    public PassTypeIdInfoFetchTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    public int getErrorLogConstant() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(PassTypeIdInfoRequest passTypeIdInfoRequest) {
        if (passTypeIdInfoRequest == null || StringUtil.isEmpty(passTypeIdInfoRequest.getPassTypeId(), true)) {
            LogC.e("PassTypeIdInfoFetchTask prepareRequestStr, invalid param", false);
            return null;
        }
        JSONObject headerJson = JSONHelper.createHeaderStr(passTypeIdInfoRequest.getSrcTransactionID(), HEAD_COMMANDER, passTypeIdInfoRequest.getIsNeedServiceTokenAuth());
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("header", headerJson);
            dataJson.put("passTypeId", passTypeIdInfoRequest.getPassTypeId());
        } catch (JSONException e) {
            LogC.e("PassTypeIdInfoFetchTaskprepareRequestStr json error", (Throwable) e, true);
            dataJson = null;
        }
        return JSONHelper.createRequestStr(passTypeIdInfoRequest.getMerchantID(), passTypeIdInfoRequest.getRsaKeyIndex(), dataJson, this.mContext);
    }

    /* access modifiers changed from: protected */
    public PassTypeIdInfoResponse readErrorResponse(int i) {
        LogC.i("PassTypeIdInfoFetchTaskreadErrorResponse errorCode is  " + i, false);
        PassTypeIdInfoResponse response = new PassTypeIdInfoResponse();
        response.returnCode = i;
        return response;
    }

    /* access modifiers changed from: protected */
    public PassTypeIdInfoResponse readSuccessResponse(String s) {
        PassTypeIdInfoResponse response = new PassTypeIdInfoResponse();
        resolveResponse(response, s);
        return response;
    }

    /* access modifiers changed from: protected */
    public void makeResponseData(CardServerBaseResponse response, JSONObject dataObject) throws JSONException {
        if (response instanceof PassTypeIdInfoResponse) {
            PassTypeIdInfoResponse passTypeIdInfoResponse = (PassTypeIdInfoResponse) response;
            if (dataObject.has("passTypeGroup")) {
                passTypeIdInfoResponse.setPassTypeGroup(dataObject.getString("passTypeGroup"));
            }
            if (dataObject.has("enableNFC")) {
                passTypeIdInfoResponse.setEnableNFC(dataObject.getBoolean("enableNFC"));
            }
            if (dataObject.has("reserve")) {
                passTypeIdInfoResponse.setReserve(dataObject.getString("reserve"));
            }
        }
    }
}
