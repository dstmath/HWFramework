package com.huawei.wallet.sdk.business.idcard.idcard.server.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.idcard.idcard.server.card.NewHttpConnTask;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.ServerAccessApplyAPDUResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.util.SignJsonDataUtil;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessApplyAPDUTask extends NewHttpConnTask<ServerAccessApplyAPDUResponse, ServerAccessApplyAPDURequest> {
    private static final String HEAD_COMMANDER = "get.apdu";
    private static final String TAG = "IDCard:ServerAccessApplyAPDUTask";

    public ServerAccessApplyAPDUTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(ServerAccessApplyAPDURequest request) {
        String str = null;
        if (request == null || !request.valid()) {
            LogX.e("ServerAccessApplyAPDUTask prepareRequestStr, invalid param");
            return null;
        }
        JSONObject dataObject = request.createRequestData(JSONHelper.createHeaderStr(request.getSrcTransactionID(), "get.apdu", request.getIsNeedServiceTokenAuth()));
        if (dataObject != null) {
            str = SignJsonDataUtil.signJsonData(dataObject, this.mContext);
        }
        return str;
    }

    /* access modifiers changed from: protected */
    public ServerAccessApplyAPDUResponse readErrorResponse(int errorCode, String errorMessage) {
        ServerAccessApplyAPDUResponse response = new ServerAccessApplyAPDUResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        return response;
    }

    /* access modifiers changed from: protected */
    public ServerAccessApplyAPDUResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        ServerAccessApplyAPDUResponse response = new ServerAccessApplyAPDUResponse();
        response.returnCode = returnCode;
        response.setResultDesc(returnDesc);
        setErrorInfo(dataObject, response);
        if (returnCode == 0 && dataObject != null) {
            JSONArray apduArray = null;
            try {
                if (dataObject.has("apduList") && dataObject.has("apduCount") && dataObject.getInt("apduCount") > 0) {
                    apduArray = dataObject.getJSONArray("apduList");
                }
                if (dataObject.has(ExecuteApduTask.NEXT_STEP)) {
                    response.setNextStep(JSONHelper.getStringValue(dataObject, ExecuteApduTask.NEXT_STEP));
                }
                if (apduArray != null) {
                    List<ServerAccessAPDU> apduList = new ArrayList<>();
                    int n = apduArray.length();
                    for (int i = 0; i < n; i++) {
                        ServerAccessAPDU apdu = ServerAccessAPDU.buildFromJson(apduArray.getJSONObject(i));
                        if (apdu != null) {
                            apduList.add(apdu);
                        }
                    }
                    response.setApduList(apduList);
                }
            } catch (JSONException e) {
                LogX.e("ServerAccessApplyAPDUTask readSuccessResponse, JSONException");
                response.returnCode = -99;
            }
        }
        return response;
    }
}
