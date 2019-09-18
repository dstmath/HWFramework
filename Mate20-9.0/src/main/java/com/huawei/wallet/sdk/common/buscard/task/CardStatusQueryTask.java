package com.huawei.wallet.sdk.common.buscard.task;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.business.bankcard.modle.CardStatusItem;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.request.CardStatusQueryRequest;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.response.CardStatusQueryResponse;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.unionpay.tsmservice.data.Constant;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CardStatusQueryTask extends HttpConnTask<CardStatusQueryResponse, CardStatusQueryRequest> {
    private static final String CARD_STATUS_GET_COMMANDER = "nfc.get.list.card";
    public static final String QUERY_CARD_AND_DEVICE = "2";
    private static final String QUERY_CARD_OPERATE_AND_DEVICE = "21";

    public CardStatusQueryTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(CardStatusQueryRequest request) {
        if (request == null || StringUtil.isEmpty(request.getSrcTransactionID(), true) || StringUtil.isEmpty(request.getMerchantID(), true) || StringUtil.isEmpty(request.cplc, true)) {
            LogX.d("prepareRequestStr, params invalid.");
            return null;
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), CARD_STATUS_GET_COMMANDER, request.getIsNeedServiceTokenAuth()), request), this.mContext);
    }

    private JSONObject createDataStr(JSONObject headerObject, CardStatusQueryRequest request) {
        JSONObject dataJson;
        if (headerObject == null) {
            return null;
        }
        LogX.d("createDataStr headerStr : " + headerObject.toString(), true);
        try {
            dataJson = new JSONObject();
            dataJson.put("header", headerObject);
            if (!StringUtil.isEmpty(request.cplc, true)) {
                dataJson.put("cplc", request.cplc);
            }
            if (TextUtils.isEmpty(request.getFlag())) {
                dataJson.put("flag", QUERY_CARD_OPERATE_AND_DEVICE);
            } else {
                dataJson.put("flag", request.getFlag());
            }
            if (!StringUtil.isEmpty(request.getQueryFlag(), true)) {
                dataJson.put("queryFlag", request.getQueryFlag());
            }
        } catch (JSONException e) {
            LogX.e("createDataStr, params invalid.");
            dataJson = null;
        }
        return dataJson;
    }

    /* access modifiers changed from: protected */
    public CardStatusQueryResponse readErrorResponse(int errorCode, String errorMessage) {
        CardStatusQueryResponse response = new CardStatusQueryResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        return response;
    }

    private CardStatusItem createCardStatusItem(JSONObject cardStatusJsonItem) {
        try {
            CardStatusItem statusItem = new CardStatusItem();
            statusItem.setUserId(JSONHelper.getStringValue(cardStatusJsonItem, "userID"));
            statusItem.setCplc(JSONHelper.getStringValue(cardStatusJsonItem, "cplc"));
            statusItem.setAid(JSONHelper.getStringValue(cardStatusJsonItem, "aid"));
            statusItem.setStatus(JSONHelper.getStringValue(cardStatusJsonItem, Constants.FIELD_APPLET_CONFIG_STATUS));
            statusItem.setDpanid(JSONHelper.getStringValue(cardStatusJsonItem, "dpanid"));
            statusItem.setSource(JSONHelper.getStringValue(cardStatusJsonItem, "source"));
            statusItem.setCardNum(JSONHelper.getStringValue(cardStatusJsonItem, Constant.KEY_CARD_NUMBER));
            statusItem.setCardName(JSONHelper.getStringValue(cardStatusJsonItem, "cardName"));
            statusItem.setType(JSONHelper.getIntValue(cardStatusJsonItem, Constant.KEY_CARD_TYPE));
            statusItem.setEidCode(JSONHelper.getStringValue(cardStatusJsonItem, "eidCode"));
            statusItem.setPanEnrollmentId(JSONHelper.getStringValue(cardStatusJsonItem, "panEnrollmentId"));
            statusItem.setvProvisionedTokenid(JSONHelper.getStringValue(cardStatusJsonItem, "provisionedTokenid"));
            statusItem.setMetaDataModTime(JSONHelper.getStringValue(cardStatusJsonItem, "metaDataModTime"));
            statusItem.setmLastModified(JSONHelper.getStringValue(cardStatusJsonItem, "lastModified"));
            statusItem.setTerminal(JSONHelper.getStringValue(cardStatusJsonItem, HciConfigInfo.HCI_DATA_TYPE_AFTER_TERMINAL_ID));
            statusItem.setIssuerId(JSONHelper.getStringValue(cardStatusJsonItem, ServerAccessApplyAPDURequest.ReqKey.ISSUERID));
            statusItem.setTsp(JSONHelper.getIntValue(cardStatusJsonItem, "tsp"));
            if (cardStatusJsonItem.has("reserved")) {
                try {
                    JSONObject reserved = new JSONObject(JSONHelper.getStringValue(cardStatusJsonItem, "reserved"));
                    statusItem.setDpanid(JSONHelper.getStringValue(reserved, "dpanid"));
                    statusItem.setProductId(JSONHelper.getStringValue(reserved, "productId"));
                    statusItem.setRefId(JSONHelper.getStringValue(reserved, ServerAccessApplyAPDURequest.ReqKey.TOKENREFID));
                    statusItem.setAppletVersion(JSONHelper.getStringValue(reserved, "appletVersion"));
                } catch (JSONException e) {
                    LogX.e("createCardItemFromJson JSONException : reserved fail ", true);
                }
            }
            String balance = JSONHelper.getStringValue(cardStatusJsonItem, HciConfigInfo.HCI_DATA_TYPE_AFTER_TRANSCTION_BALANCE);
            if (TextUtils.isEmpty(balance)) {
                return statusItem;
            }
            try {
                statusItem.setBalance(String.valueOf(Float.parseFloat(balance) / 100.0f));
                return statusItem;
            } catch (NumberFormatException e2) {
                LogX.e("createCardItemFromJson JSONException : NumberFormatException", true);
                return statusItem;
            }
        } catch (JSONException ex) {
            LogX.e("createCardItemFromJson JSONException : " + ex.getMessage(), true);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public CardStatusQueryResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        CardStatusQueryResponse response = new CardStatusQueryResponse();
        response.returnCode = returnCode;
        if (returnCode == 0) {
            try {
                response.setDevStatus(JSONHelper.getStringValue(dataObject, "devStatus"));
                response.setCardCount((long) JSONHelper.getIntValue(dataObject, SNBConstant.FIELD_COUNT));
                response.setCreatetime(JSONHelper.getStringValue(dataObject, "createtime"));
                if (dataObject.has("inCloudCount")) {
                    response.setInCloudCount((long) JSONHelper.getIntValue(dataObject, "inCloudCount"));
                }
                JSONArray cardCloudArrays = null;
                if (dataObject.has("inCloudData")) {
                    cardCloudArrays = dataObject.getJSONArray("inCloudData");
                }
                if (cardCloudArrays != null) {
                    response.setCloudItems(new ArrayList());
                    for (int i = 0; i < cardCloudArrays.length(); i++) {
                        CardStatusItem tempCardItem = createCardStatusItem(cardCloudArrays.getJSONObject(i));
                        if (tempCardItem != null) {
                            response.getCloudItems().add(tempCardItem);
                        }
                    }
                }
                JSONArray cardArrays = null;
                if (dataObject.has(SNBConstant.FIELD_DATA)) {
                    cardArrays = dataObject.getJSONArray(SNBConstant.FIELD_DATA);
                }
                if (cardArrays != null) {
                    response.setItems(new ArrayList());
                    CardStatusItem tempCardItem2 = null;
                    for (int i2 = 0; i2 < cardArrays.length(); i2++) {
                        JSONObject tempJsonItem = cardArrays.getJSONObject(i2);
                        StringBuilder sb = new StringBuilder();
                        sb.append(" nfc.get.list.card ");
                        sb.append(tempJsonItem != null ? tempJsonItem.toString() : "");
                        LogX.d(sb.toString());
                        if (tempJsonItem != null) {
                            tempCardItem2 = createCardStatusItem(tempJsonItem);
                        }
                        if (tempCardItem2 != null) {
                            response.getItems().add(tempCardItem2);
                        }
                    }
                }
            } catch (JSONException e) {
                LogX.e("readSuccessResponse, JSONException : " + e.getMessage(), true);
                response.returnCode = -99;
            }
        }
        return response;
    }
}
