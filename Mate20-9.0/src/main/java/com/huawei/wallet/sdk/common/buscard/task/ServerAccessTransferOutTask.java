package com.huawei.wallet.sdk.common.buscard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.ServerAccessAPDU;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.buscard.request.ServerAccessTransferOutRequest;
import com.huawei.wallet.sdk.common.buscard.response.ServerAccessTransferOutResponse;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ServerAccessTransferOutTask extends HttpConnTask<ServerAccessTransferOutResponse, ServerAccessTransferOutRequest> {
    private static final String HEAD_COMMANDER = "cardmove.out";
    public static final String HEAD_COMMANDER_CLOUD_TRANSFER = "nfc.transcard.backup";
    public static final String HEAD_COMMANDER_CLOUD_TRANSFER_CHECK = "nfc.transcard.remove.check";
    public static final String HEAD_COMMANDER_CLOUD_TRANSFER_INIT = "nfc.transcard.reviveinit";
    private String mHeadCommander = "nfc.transcard.backup";

    public void setHeadCommander(String mHeadCommander2) {
        this.mHeadCommander = mHeadCommander2;
    }

    public ServerAccessTransferOutTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(ServerAccessTransferOutRequest request) {
        if (request == null) {
            LogX.e("ServerAccessTransferOutTask prepareRequestStr, invalid param");
            return null;
        }
        if (request.getTransferVerifyFlag() == null || !request.getTransferVerifyFlag().equalsIgnoreCase("1")) {
            if (checkFullNullParam(request.getEventId(), request.getIssuerId(), request.getCplc(), request.getAppletAid(), request.getSeChipManuFacturer(), request.getDeviceModel(), request.getCardId(), request.getBalance())) {
                LogX.e("ServerAccessTransferOutTask prepareRequestStr, invalid full param check");
                return null;
            }
        } else {
            if (checkNullParam(request.getIssuerId(), request.getCplc(), request.getAppletAid(), request.getSeChipManuFacturer(), request.getDeviceModel(), request.getCardId())) {
                LogX.e("ServerAccessTransferOutTask prepareRequestStr, invalid param check");
                return null;
            }
        }
        JSONObject dataJson = createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), this.mHeadCommander, request.getIsNeedServiceTokenAuth()), request);
        JSONObject reportRequestMessageJson = reportRequestMessage(request);
        if (!isDebugBuild()) {
            LogX.i("ServerAccessTransferOutTask prepareRequestStr, commander= " + this.mHeadCommander + " reportRequestMessageJson= " + reportRequestMessageJson);
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), dataJson, this.mContext);
    }

    private JSONObject reportRequestMessage(ServerAccessTransferOutRequest request) {
        try {
            JSONObject jObj = new JSONObject();
            jObj.put(ExecuteApduTask.SRC_TRANSACTION_ID, request.getSrcTransactionID());
            jObj.put("eventid", request.getEventId());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            jObj.put("seChipManuFacturer", request.getSeChipManuFacturer());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            if (!StringUtil.isEmpty(request.getOrderId(), true)) {
                jObj.put("orderNo", request.getOrderId());
            }
            if (!StringUtil.isEmpty(request.getTransferVerifyFlag(), true)) {
                jObj.put("flag", request.getTransferVerifyFlag());
            }
            if (!StringUtil.isEmpty(request.getPhoneManufacturer(), true)) {
                jObj.put("phoneManufacturer", request.getPhoneManufacturer());
            }
            if (!StringUtil.isEmpty(request.getReserved(), true)) {
                jObj.put("reserved", request.getReserved());
            }
            if (!StringUtil.isEmpty(request.getExtend(), true)) {
                jObj.put("extend", request.getExtend());
            }
            if (!StringUtil.isEmpty(request.getAppCode(), true)) {
                jObj.put("appCode", request.getAppCode());
            }
            if (StringUtil.isEmpty(request.getPartnerId(), true)) {
                return jObj;
            }
            jObj.put(ExecuteApduTask.PARTNER_ID, request.getPartnerId());
            return jObj;
        } catch (JSONException e) {
            LogX.e("ServerAccessTransferOutTask reportRequestMessage, JSONException");
            return null;
        }
    }

    private JSONObject createDataStr(JSONObject headerObject, ServerAccessTransferOutRequest request) {
        JSONObject jObj;
        if (headerObject == null) {
            LogX.e("ServerAccessTransferOutTask createDataStr, invalid param");
            return null;
        }
        try {
            jObj = new JSONObject();
            jObj.put("header", headerObject);
            jObj.put("eventid", request.getEventId());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.ISSUERID, request.getIssuerId());
            jObj.put("cplc", request.getCplc());
            jObj.put(ServerAccessApplyAPDURequest.ReqKey.AID, request.getAppletAid());
            jObj.put("seChipManuFacturer", request.getSeChipManuFacturer());
            jObj.put(ExecuteApduTask.DEVICE_MODEL, request.getDeviceModel());
            jObj.put("cardNo", request.getCardId());
            if (!StringUtil.isEmpty(request.getBalance(), true)) {
                jObj.put(HciConfigInfo.HCI_DATA_TYPE_AFTER_TRANSCTION_BALANCE, request.getBalance());
                jObj.put("cardBalance", request.getBalance());
            }
            if (!StringUtil.isEmpty(request.getOrderId(), true)) {
                jObj.put("orderNo", request.getOrderId());
            }
            if (!StringUtil.isEmpty(request.getTransferVerifyFlag(), true)) {
                jObj.put("flag", request.getTransferVerifyFlag());
            }
            if (!StringUtil.isEmpty(request.getSn(), true)) {
                jObj.put(SNBConstant.FIELD_IMEI, request.getSn());
                jObj.put("sn", request.getSn());
            }
            if (!StringUtil.isEmpty(request.getPhoneNumber(), true)) {
                jObj.put("phoneNumber", request.getPhoneNumber());
            }
            if (!StringUtil.isEmpty(request.getPhoneManufacturer(), true)) {
                jObj.put("phoneManufacturer", request.getPhoneManufacturer());
            }
            if (!StringUtil.isEmpty(request.getReserved(), true)) {
                jObj.put("reserved", request.getReserved());
            }
            if (!StringUtil.isEmpty(request.getExtend(), true)) {
                jObj.put("extend", request.getExtend());
            }
            if (!StringUtil.isEmpty(request.getAppCode(), true)) {
                jObj.put("appCode", request.getAppCode());
            }
            if (!StringUtil.isEmpty(request.getPartnerId(), true)) {
                jObj.put(ExecuteApduTask.PARTNER_ID, request.getPartnerId());
            }
            if (request.getRequestTimes() > 0) {
                jObj.put("times", request.getRequestTimes());
            }
        } catch (JSONException e) {
            LogX.e("ServerAccessTransferOutTask createDataStr, JSONException");
            jObj = null;
        }
        return jObj;
    }

    /* access modifiers changed from: protected */
    public ServerAccessTransferOutResponse readErrorResponse(int errorCode, String errorMessage) {
        ServerAccessTransferOutResponse response = new ServerAccessTransferOutResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        if (!isDebugBuild()) {
            LogX.i("ServerAccessTransferOutTask readErrorResponse, commander= " + this.mHeadCommander + " errorCode= " + errorCode + " errorMessage= " + errorMessage);
        }
        return response;
    }

    /* access modifiers changed from: protected */
    public ServerAccessTransferOutResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        StringBuilder builder = new StringBuilder();
        ServerAccessTransferOutResponse response = new ServerAccessTransferOutResponse();
        response.returnCode = returnCode;
        response.setResultDesc(returnDesc);
        if (dataObject != null) {
            try {
                if (dataObject.has("header")) {
                    JSONObject header = dataObject.getJSONObject("header");
                    if (header != null) {
                        String srcTranId = header.getString("srcTranID");
                        response.setSrcTranID(srcTranId);
                        builder.append("srcTranId=");
                        builder.append(srcTranId);
                    }
                }
            } catch (JSONException e) {
                LogX.e("ServerAccessTransferOutTask readSuccessResponse, JSONException");
                response.returnCode = -99;
            }
        }
        if (returnCode == 0) {
            try {
                response.setTransactionId(JSONHelper.getStringValue(dataObject, "transactionid"));
                builder.append(" transactionid=");
                builder.append(JSONHelper.getStringValue(dataObject, "transactionid"));
                int intervalTime = JSONHelper.getIntValue(dataObject, "invokeIntervalTime");
                if (intervalTime > 0) {
                    builder.append(" invokeIntervalTime=");
                    builder.append(intervalTime);
                    response.setInvokeIntervalTime(intervalTime);
                }
                JSONArray apduArray = null;
                if (dataObject.has("apduList")) {
                    apduArray = dataObject.getJSONArray("apduList");
                }
                if (dataObject.has(ExecuteApduTask.NEXT_STEP)) {
                    response.setNextStep(JSONHelper.getStringValue(dataObject, ExecuteApduTask.NEXT_STEP));
                    builder.append(" nextStep=");
                    builder.append(JSONHelper.getStringValue(dataObject, ExecuteApduTask.NEXT_STEP));
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
                    builder.append(" apduList=");
                    builder.append(apduList);
                }
            } catch (JSONException e2) {
                LogX.e("ServerAccessTransferOutTask readSuccessResponse, JSONException");
                response.returnCode = -99;
            }
        }
        if (!isDebugBuild()) {
            LogX.i("ServerAccessTransferOutTask readSuccessResponse, commander= " + this.mHeadCommander + " returnCode= " + returnCode + " returnDesc= " + returnDesc + " dataObject: " + builder.toString());
        }
        return response;
    }

    private boolean checkNullParam(String issueId, String cplc, String aid, String manuFacturer, String deviceModel, String cardId) {
        if (StringUtil.isEmpty(issueId, true) || StringUtil.isEmpty(cplc, true) || StringUtil.isEmpty(aid, true) || StringUtil.isEmpty(manuFacturer, true) || StringUtil.isEmpty(deviceModel, true) || StringUtil.isEmpty(cardId, true)) {
            return true;
        }
        return false;
    }

    private boolean checkFullNullParam(String eventId, String issueId, String cplc, String aid, String manuFacturer, String deviceModel, String cardId, String balance) {
        if (StringUtil.isEmpty(eventId, true) || StringUtil.isEmpty(issueId, true) || StringUtil.isEmpty(cplc, true) || StringUtil.isEmpty(aid, true) || StringUtil.isEmpty(manuFacturer, true) || StringUtil.isEmpty(deviceModel, true) || StringUtil.isEmpty(cardId, true) || StringUtil.isEmpty(balance, true)) {
            return true;
        }
        return false;
    }
}
