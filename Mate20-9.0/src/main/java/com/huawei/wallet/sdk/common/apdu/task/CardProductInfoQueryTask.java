package com.huawei.wallet.sdk.common.apdu.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.business.bankcard.task.ExecuteApduTask;
import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.json.JSONHelper;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.model.CardProductInfoServerItem;
import com.huawei.wallet.sdk.common.apdu.request.QueryCardProductInfoRequest;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.apdu.response.QueryCardProductInfoResponse;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CardProductInfoQueryTask extends HttpConnTask<QueryCardProductInfoResponse, QueryCardProductInfoRequest> {
    private static final String CARDPRODUCT_INFO_GET_COMMANDER = "nfc.get.products";
    private static final int RESPONSE_CODE_OTHER_ERRORS = -99;

    public CardProductInfoQueryTask(Context context, String url) {
        super(context, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(QueryCardProductInfoRequest request) {
        if (request == null || StringUtil.isEmpty(request.getSrcTransactionID(), true) || StringUtil.isEmpty(request.getMerchantID(), true) || request.getFilters() == null) {
            LogX.d("prepareRequestStr, params invalid.");
            return null;
        }
        JSONObject dataJson = createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), CARDPRODUCT_INFO_GET_COMMANDER, request.getIsNeedServiceTokenAuth()), request);
        JSONObject reportRequestMessageJson = reportRequestMessage(request);
        if (!isDebugBuild()) {
            LogX.i("CardProductInfoQueryTask prepareRequestStr, commander= nfc.get.products reportRequestMessageJson= " + reportRequestMessageJson);
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), dataJson, this.mContext);
    }

    private JSONObject reportRequestMessage(QueryCardProductInfoRequest request) {
        try {
            Set<Map<String, String>> rfilters = request.getFilters();
            if (rfilters == null || rfilters.size() <= 0) {
                return null;
            }
            JSONObject obj = new JSONObject();
            obj.put(ExecuteApduTask.SRC_TRANSACTION_ID, request.getSrcTransactionID());
            obj.put(HciConfigInfo.HCI_DATA_TYPE_VERSION, request.getVersion());
            obj.put("client", request.getClient());
            obj.put("filters", new JSONArray(rfilters));
            obj.put("timestamp", request.getTimeStamp());
            return obj;
        } catch (JSONException e) {
            LogX.e("CardProductInfoQueryTask reportRequestMessage parse json error", true);
            return null;
        }
    }

    private JSONObject createDataStr(JSONObject headerObject, QueryCardProductInfoRequest request) {
        JSONObject obj = null;
        if (headerObject == null) {
            return null;
        }
        try {
            Set<Map<String, String>> rfilters = request.getFilters();
            if (rfilters != null && rfilters.size() > 0) {
                obj = new JSONObject();
                obj.put("header", headerObject);
                obj.put(HciConfigInfo.HCI_DATA_TYPE_VERSION, request.getVersion());
                obj.put("client", request.getClient());
                obj.put("filters", new JSONArray(rfilters));
                obj.put("timestamp", request.getTimeStamp());
            }
        } catch (JSONException e) {
            LogX.e("CardProductInfoQueryTask createDataStr parse json error" + e.getMessage(), true);
            obj = null;
        }
        return obj;
    }

    /* access modifiers changed from: protected */
    public QueryCardProductInfoResponse readErrorResponse(int errorCode, String errorMessage) {
        QueryCardProductInfoResponse response = new QueryCardProductInfoResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        if (!isDebugBuild()) {
            LogX.i("CardProductInfoQueryTask readErrorResponse, commander= nfc.get.products errorCode= " + errorCode + " errorMessage= " + errorMessage);
        }
        return response;
    }

    private CardProductInfoServerItem createCardProductInfoItem(JSONObject tempJsonItem) {
        if (tempJsonItem == null) {
            return null;
        }
        CardProductInfoServerItem item = new CardProductInfoServerItem();
        try {
            item.setProductId(JSONHelper.getStringValue(tempJsonItem, "productid"));
            item.setProductName(JSONHelper.getStringValue(tempJsonItem, "name"));
            item.setPictureUrl(JSONHelper.getStringValue(tempJsonItem, "pictureUrl"));
            item.setDescription(JSONHelper.getStringValue(tempJsonItem, "description"));
            item.setType(JSONHelper.getIntValue(tempJsonItem, Constants.FIELD_HCI_CONFIG_DATA_TYPE));
            item.setTimeStamp(JSONHelper.getLongValue(tempJsonItem, "timestamp"));
            item.setVersion(JSONHelper.getStringValue(tempJsonItem, HciConfigInfo.HCI_DATA_TYPE_VERSION));
            item.setIssuerId(JSONHelper.getStringValue(tempJsonItem, ServerAccessApplyAPDURequest.ReqKey.ISSUERID));
            item.setMktInfo(JSONHelper.getStringValue(tempJsonItem, "mktDesc"));
            item.setReservedInfo(JSONHelper.getStringValue(tempJsonItem, "reserved"));
            item.setFontColor(JSONHelper.getStringValue(tempJsonItem, "frontColor"));
            item.setArea(JSONHelper.getStringValue(tempJsonItem, "area"));
            item.setReserved1(JSONHelper.getStringValue(tempJsonItem, "reserved1"));
            item.setReserved2(JSONHelper.getStringValue(tempJsonItem, "reserved2"));
            item.setReserved3(JSONHelper.getStringValue(tempJsonItem, "reserved3"));
            item.setReserved4(JSONHelper.getStringValue(tempJsonItem, "reserved4"));
            item.setReserved5(JSONHelper.getStringValue(tempJsonItem, "reserved5"));
            item.setReserved6(JSONHelper.getStringValue(tempJsonItem, "reserved6"));
        } catch (JSONException e) {
            LogX.e("CardProductInfoQueryTask createCardProductInfoItem JSONException : " + e.getMessage(), true);
            item = null;
        }
        return item;
    }

    /* access modifiers changed from: protected */
    public QueryCardProductInfoResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        StringBuilder builder = new StringBuilder();
        QueryCardProductInfoResponse response = new QueryCardProductInfoResponse();
        response.returnCode = returnCode;
        if (returnCode == 0) {
            JSONArray cardArrays = null;
            try {
                if (dataObject.has(SNBConstant.FIELD_DATA)) {
                    cardArrays = dataObject.getJSONArray(SNBConstant.FIELD_DATA);
                }
                if (cardArrays != null) {
                    response.items = new ArrayList();
                    for (int i = 0; i < cardArrays.length(); i++) {
                        CardProductInfoServerItem tempCardItem = createCardProductInfoItem(cardArrays.getJSONObject(i));
                        if (tempCardItem != null) {
                            response.items.add(tempCardItem);
                            builder.append("tempCardItem=");
                            builder.append(tempCardItem);
                        }
                    }
                }
            } catch (JSONException e) {
                LogX.e("readSuccessResponse, JSONException : " + e.getMessage(), true);
                response.returnCode = -99;
            }
        }
        if (!isDebugBuild()) {
            LogX.i("CardProductInfoQueryTask readSuccessResponse, commander= nfc.get.products returnCode= " + returnCode + " returnDesc= " + returnDesc + " dataObject: " + builder.toString());
        }
        return response;
    }
}
