package com.huawei.wallet.sdk.business.bankcard.task;

import android.content.Context;
import com.huawei.wallet.sdk.business.bankcard.constant.Constants;
import com.huawei.wallet.sdk.business.bankcard.modle.SwipeCardInfo;
import com.huawei.wallet.sdk.business.bankcard.request.WipeAllBankCardRequest;
import com.huawei.wallet.sdk.business.bankcard.response.CardSwipeResponse;
import com.huawei.wallet.sdk.common.apdu.request.ServerAccessApplyAPDURequest;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WipeAllBankCardTask extends HttpConnTask {
    private static final String TAG = "WipeAllBankCardTask";
    private WipeAllBankCardRequest request;

    public WipeAllBankCardTask(Context context, String url, WipeAllBankCardRequest request2) {
        super(context, url);
        this.request = request2;
    }

    private JSONObject createDataStr(JSONObject headerObject, WipeAllBankCardRequest request2) {
        if (headerObject == null) {
            return null;
        }
        LogC.d(TAG, "createDataStr headerStr : " + headerObject.toString(), true);
        JSONObject dataJson = new JSONObject();
        try {
            dataJson.put("header", headerObject);
            dataJson.put("requestId", "" + System.currentTimeMillis());
            if (!StringUtil.isEmpty(request2.getCplc(), true)) {
                dataJson.put("cplc", request2.getCplc());
            }
            if (!StringUtil.isEmpty(request2.getEvent(), true)) {
                dataJson.put("event", request2.getEvent());
            }
            String brand = String.valueOf(request2.getBrand());
            if (!StringUtil.isEmpty(brand, true)) {
                dataJson.put("cardBrand", brand);
            }
        } catch (JSONException e) {
            LogC.e(TAG, "createDataStr, params invalid.", false);
            dataJson = null;
        }
        return dataJson;
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(Object o) {
        if (this.request == null || StringUtil.isEmpty(this.request.getSrcTransactionID(), true) || StringUtil.isEmpty(this.request.getMerchantID(), true)) {
            LogC.d(TAG, "prepareRequestStr, params invalid.", false);
            return null;
        }
        return JSONHelper.createRequestStr(this.request.getMerchantID(), this.request.getRsaKeyIndex(), createDataStr(JSONHelper.createHeaderStr(this.request.getSrcTransactionID(), "wipe.device"), this.request), this.mContext);
    }

    /* access modifiers changed from: protected */
    public Object readErrorResponse(int errorCode, String errorMessage) {
        return null;
    }

    /* access modifiers changed from: protected */
    public Object readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        CardSwipeResponse response = new CardSwipeResponse();
        response.returnCode = returnCode;
        if (returnCode == 0) {
            JSONArray cardArrays = null;
            try {
                if (dataObject.has("cardInfoList")) {
                    cardArrays = dataObject.getJSONArray("cardInfoList");
                }
                if (cardArrays != null) {
                    response.setSwipeCardInfoList(new ArrayList());
                    for (int i = 0; i < cardArrays.length(); i++) {
                        SwipeCardInfo tempCardItem = createSwipeCardInfoItem(cardArrays.getJSONObject(i));
                        if (tempCardItem != null) {
                            response.getSwipeCardInfoList().add(tempCardItem);
                        }
                    }
                }
            } catch (JSONException e) {
                response.returnCode = -99;
            }
        }
        return response;
    }

    private SwipeCardInfo createSwipeCardInfoItem(JSONObject tempJsonItem) {
        if (tempJsonItem == null) {
            return null;
        }
        SwipeCardInfo item = new SwipeCardInfo();
        try {
            item.setTokenID(JSONHelper.getStringValue(tempJsonItem, "tokenRefID"));
            item.setStatus(JSONHelper.getStringValue(tempJsonItem, Constants.FIELD_APPLET_CONFIG_STATUS));
            item.setIssuerId(JSONHelper.getStringValue(tempJsonItem, ServerAccessApplyAPDURequest.ReqKey.ISSUERID));
            item.setAid(JSONHelper.getStringValue(tempJsonItem, "aid"));
        } catch (JSONException e) {
            item = null;
        }
        return item;
    }
}
