package com.huawei.wallet.sdk.business.idcard.accesscard.server.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.idcard.accesscard.server.request.OpenAccessCardRequest;
import com.huawei.wallet.sdk.business.idcard.accesscard.server.response.OpenAccessCardResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OpenAccessCardTask extends HttpConnTask<OpenAccessCardResponse, OpenAccessCardRequest> {
    private static final String HEAD_COMMANDER = "get.accesscard.apply";

    public OpenAccessCardTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(OpenAccessCardRequest request) {
        if (request == null || StringUtil.isEmpty(request.getSrcTransactionID(), true) || StringUtil.isEmpty(request.getMerchantID(), true) || StringUtil.isEmpty(request.getIssuerid(), true) || StringUtil.isEmpty(request.getCplc(), true) || StringUtil.isEmpty(request.getDeviceModel(), true)) {
            LogX.e("OpenAccessCardTask prepareRequestStr, invalid param");
            return null;
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), request.createRequestData(JSONHelper.createHeaderStr(request.getSrcTransactionID(), HEAD_COMMANDER, request.getIsNeedServiceTokenAuth())), this.mContext);
    }

    /* access modifiers changed from: protected */
    public OpenAccessCardResponse readErrorResponse(int i, String s) {
        OpenAccessCardResponse response = new OpenAccessCardResponse();
        response.setReturnCode(i);
        response.setResultDesc(s);
        return response;
    }

    /* access modifiers changed from: protected */
    public OpenAccessCardResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject jsonObject) {
        OpenAccessCardResponse response = new OpenAccessCardResponse();
        response.setReturnCode(returnCode);
        response.setResultDesc(returnDesc);
        if (returnCode == 0) {
            try {
                response.setApduCount(JSONHelper.getIntValue(jsonObject, "apduCount"));
                response.setTransactionId(JSONHelper.getStringValue(jsonObject, "transactionid"));
                if (jsonObject.has(ExecuteApduTask.NEXT_STEP)) {
                    response.setNextStep(JSONHelper.getStringValue(jsonObject, ExecuteApduTask.NEXT_STEP));
                }
                JSONArray apduArr = null;
                if (jsonObject.has("apduList")) {
                    apduArr = jsonObject.getJSONArray("apduList");
                }
                if (apduArr != null) {
                    List<ServerAccessAPDU> apduList = new ArrayList<>();
                    int n = apduArr.length();
                    for (int i = 0; i < n; i++) {
                        ServerAccessAPDU apdu = ServerAccessAPDU.buildFromJson(apduArr.getJSONObject(i));
                        if (apdu != null) {
                            apduList.add(apdu);
                        }
                    }
                    response.setApduList(apduList);
                }
            } catch (JSONException e) {
                LogX.e("OpenAccessCardTask readSuccessResponse, JSONException");
                response.setReturnCode(-99);
            }
        } else {
            LogX.i("OpenAccessCardTask set errorInfo");
            setErrorInfo(jsonObject, response);
        }
        return response;
    }
}
