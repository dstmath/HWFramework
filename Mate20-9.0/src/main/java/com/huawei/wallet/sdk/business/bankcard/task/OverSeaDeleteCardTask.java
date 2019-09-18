package com.huawei.wallet.sdk.business.bankcard.task;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.request.DeleteOverSeaCardRequest;
import com.huawei.wallet.sdk.business.bankcard.response.NullifyCardResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import com.huawei.wallet.sdk.common.utils.ProductConfigUtil;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OverSeaDeleteCardTask extends HttpConnTask<NullifyCardResponse, DeleteOverSeaCardRequest> {
    public static final String DELETE_CARD_COMMANDER = "delete.app";
    private static final String TAG = OverSeaDeleteCardTask.class.getSimpleName();

    public OverSeaDeleteCardTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(DeleteOverSeaCardRequest request) {
        if (request == null || StringUtil.isEmpty(request.getCplc(), true) || StringUtil.isEmpty(request.getAppletAid(), true) || StringUtil.isEmpty(request.getIssuerId(), true)) {
            LogX.d(getSubProcessPrefix() + "prepareRequestStr, params invalid.");
            return null;
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), "delete.app"), request), this.mContext);
    }

    private JSONObject createDataStr(JSONObject headerObject, DeleteOverSeaCardRequest request) {
        JSONObject dataJson;
        if (headerObject == null) {
            return null;
        }
        LogX.d(getSubProcessPrefix() + "createDataStr headerStr : " + headerObject.toString(), true);
        try {
            dataJson = new JSONObject();
            dataJson.put("header", headerObject);
            dataJson.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            dataJson.put("cplc", request.getCplc());
            dataJson.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            dataJson.put("userid ", request.getUserId());
            dataJson.put("seChipManuFacturer", ProductConfigUtil.geteSEManufacturer());
            dataJson.put(ExecuteApduTask.DEVICE_MODEL, Build.MODEL);
            if (TextUtils.isEmpty(request.getSource())) {
                dataJson.put("source", "HandSet");
            } else {
                dataJson.put("source", request.getSource());
            }
            if (TextUtils.isEmpty(request.getReason())) {
                dataJson.put("reason", "4");
            } else {
                dataJson.put("source", request.getReason());
            }
        } catch (JSONException e) {
            LogX.e(getSubProcessPrefix() + "createDataStr, params invalid.");
            dataJson = null;
        }
        return dataJson;
    }

    /* access modifiers changed from: protected */
    public NullifyCardResponse readErrorResponse(int errorCode, String errorMessage) {
        NullifyCardResponse response = new NullifyCardResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        return response;
    }

    /* access modifiers changed from: protected */
    public NullifyCardResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        NullifyCardResponse response = new NullifyCardResponse();
        if (returnCode == -98) {
            response.returnCode = -99;
            LogX.e(getSubProcessPrefix() + "nullify card err, returnCode : " + returnDesc);
        } else {
            response.returnCode = returnCode;
            JSONArray apduArr = null;
            try {
                if (dataObject.has("apduCount")) {
                    response.setApduCount(JSONHelper.getIntValue(dataObject, "apduCount"));
                }
                if (dataObject.has("apduList")) {
                    apduArr = dataObject.getJSONArray("apduList");
                }
                if (dataObject.has(ExecuteApduTask.NEXT_STEP)) {
                    response.setNextStep(JSONHelper.getStringValue(dataObject, ExecuteApduTask.NEXT_STEP));
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
                if (dataObject.has("transactionid")) {
                    response.setTransactionId(JSONHelper.getStringValue(dataObject, "transactionid"));
                }
                if (dataObject.has("noNeedCommandResp")) {
                    response.setNoNeedCommandResp(JSONHelper.getStringValue(dataObject, "noNeedCommandResp"));
                }
                if (dataObject.has("appletaid")) {
                    response.setNullifyAid(JSONHelper.getStringValue(dataObject, "appletaid"));
                }
                response.setCommandId("delete.app");
            } catch (JSONException e) {
                LogX.e(getSubProcessPrefix() + "ServerAccessApplyAPDUTask readSuccessResponse, JSONException");
                response.returnCode = -99;
            }
        }
        return response;
    }

    public void setProcessPrefix(String processPrefix, String tag) {
        super.setProcessPrefix(processPrefix, TAG + "|");
    }
}
