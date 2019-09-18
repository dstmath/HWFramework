package com.huawei.wallet.sdk.business.idcard.idcard.server.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.idcard.idcard.server.card.NewHttpConnTask;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessDeleteAppletRequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessDeleteAppletResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.util.SignJsonDataUtil;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessDeleteAppletTask extends NewHttpConnTask<ServerAccessDeleteAppletResponse, ServerAccessDeleteAppletRequest> {
    private static final String HEAD_COMMANDER = "delete.app";
    private static final String TAG = "IDCard:ServerAccessDeleteAppletTask";

    public ServerAccessDeleteAppletTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(ServerAccessDeleteAppletRequest request) {
        String str = null;
        if (request == null || !request.valid()) {
            LogX.e("ServerAccessDeleteAppletTask prepareRequestStr, invalid param");
            return null;
        }
        JSONObject dataObject = request.createRequestData(JSONHelper.createHeaderStr(request.getSrcTransactionID(), "delete.app", request.getIsNeedServiceTokenAuth()));
        if (dataObject != null) {
            str = SignJsonDataUtil.signJsonData(dataObject, this.mContext);
        }
        return str;
    }

    /* access modifiers changed from: protected */
    public ServerAccessDeleteAppletResponse readErrorResponse(int errorCode, String errorMessage) {
        ServerAccessDeleteAppletResponse response = new ServerAccessDeleteAppletResponse();
        response.setResultDesc(errorMessage);
        response.returnCode = errorCode;
        return response;
    }

    /* access modifiers changed from: protected */
    public ServerAccessDeleteAppletResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        ServerAccessDeleteAppletResponse response = new ServerAccessDeleteAppletResponse();
        response.returnCode = returnCode;
        response.setResultDesc(returnDesc);
        setErrorInfo(dataObject, response);
        if (returnCode == 0 && dataObject != null) {
            JSONArray apduArr = null;
            try {
                response.setTransactionId(JSONHelper.getStringValue(dataObject, "transactionid"));
                if (dataObject.has(ExecuteApduTask.NEXT_STEP)) {
                    response.setNextStep(JSONHelper.getStringValue(dataObject, ExecuteApduTask.NEXT_STEP));
                }
                if (dataObject.has("apduCount") && dataObject.has("apduList") && dataObject.getInt("apduCount") > 0) {
                    apduArr = dataObject.getJSONArray("apduList");
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
                LogX.e("ServerAccessDeleteAppletTask readSuccessResponse, JSONException");
                response.returnCode = -99;
            }
        }
        return response;
    }
}
