package com.huawei.wallet.sdk.business.idcard.idcard.server.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.idcard.server.card.NewHttpConnTask;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.CardStatusQueryRequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.CardStatusQueryResponse;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.IdCardStatusItem;
import com.huawei.wallet.sdk.business.idcard.idcard.util.SignJsonDataUtil;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.unionpay.tsmservice.data.Constant;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CardStatusQueryTask extends NewHttpConnTask<CardStatusQueryResponse, CardStatusQueryRequest> {
    private static final String CARD_STATUS_GET_COMMANDER = "nfc.get.list.card";

    public CardStatusQueryTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(CardStatusQueryRequest request) {
        String str = null;
        if (request == null || !request.valid()) {
            LogX.d("prepareRequestStr, params invalid.");
            return null;
        }
        JSONObject dataObject = request.createRequestData(JSONHelper.createHeaderStr(request.getSrcTransactionID(), CARD_STATUS_GET_COMMANDER, request.getIsNeedServiceTokenAuth()));
        if (dataObject != null) {
            str = SignJsonDataUtil.signJsonData(dataObject, this.mContext);
        }
        return str;
    }

    /* access modifiers changed from: protected */
    public CardStatusQueryResponse readErrorResponse(int errorCode, String errorMessage) {
        CardStatusQueryResponse response = new CardStatusQueryResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        return response;
    }

    private IdCardStatusItem createCardStatusItem(JSONObject cardStatusJsonItem) {
        try {
            IdCardStatusItem statusItem = new IdCardStatusItem();
            statusItem.setUserId(JSONHelper.getStringValue(cardStatusJsonItem, "userid"));
            statusItem.setCplc(JSONHelper.getStringValue(cardStatusJsonItem, "cplc"));
            statusItem.setAid(JSONHelper.getStringValue(cardStatusJsonItem, "aid"));
            statusItem.setStatus(JSONHelper.getStringValue(cardStatusJsonItem, Constants.FIELD_APPLET_CONFIG_STATUS));
            statusItem.setLastModified(JSONHelper.getStringValue(cardStatusJsonItem, "lastModified"));
            statusItem.setCardName(JSONHelper.getStringValue(cardStatusJsonItem, "cardName"));
            statusItem.setIssuerId(JSONHelper.getStringValue(cardStatusJsonItem, ServerAccessApplyAPDURequest.ReqKey.ISSUERID));
            statusItem.setCardType(JSONHelper.getIntValue(cardStatusJsonItem, Constant.KEY_CARD_TYPE));
            statusItem.setCountry(JSONHelper.getStringValue(cardStatusJsonItem, "country"));
            statusItem.setEidCode(JSONHelper.getStringValue(cardStatusJsonItem, "eidCode"));
            statusItem.setQRCode(JSONHelper.getStringValue(cardStatusJsonItem, "QRCode"));
            return statusItem;
        } catch (JSONException ex) {
            LogX.e("createCardItemFromJson JSONException : " + ex.getMessage(), true);
            return null;
        }
    }

    /* access modifiers changed from: protected */
    public CardStatusQueryResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        CardStatusQueryResponse response = new CardStatusQueryResponse();
        response.returnCode = returnCode;
        if (returnCode == 0 && dataObject != null) {
            try {
                response.setCount((long) JSONHelper.getIntValue(dataObject, SNBConstant.FIELD_COUNT));
                JSONArray cardArrays = null;
                if (dataObject.has(SNBConstant.FIELD_DATA)) {
                    cardArrays = dataObject.getJSONArray(SNBConstant.FIELD_DATA);
                }
                if (cardArrays != null) {
                    response.setData(new ArrayList());
                    for (int i = 0; i < cardArrays.length(); i++) {
                        IdCardStatusItem tempCardItem = createCardStatusItem(cardArrays.getJSONObject(i));
                        if (tempCardItem != null) {
                            response.getData().add(tempCardItem);
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
