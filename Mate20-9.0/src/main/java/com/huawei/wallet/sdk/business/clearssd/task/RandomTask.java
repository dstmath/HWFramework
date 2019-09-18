package com.huawei.wallet.sdk.business.clearssd.task;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.clearssd.request.RandomRequest;
import com.huawei.wallet.sdk.business.clearssd.response.RandomResponse;
import com.huawei.wallet.sdk.business.diploma.util.DiplomaUtil;
import com.huawei.wallet.sdk.common.http.service.ServerCmdConstant;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class RandomTask extends HttpConnTask<RandomResponse, RandomRequest> {
    private static final String SIGN_CERT = "RSAWithCert";
    private static final String TAG = "RandomTask";

    public RandomTask(Context mContext, String url) {
        super(mContext, url);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(RandomRequest request) {
        if (request == null || TextUtils.isEmpty(request.getSrcTransactionID()) || TextUtils.isEmpty(request.getMerchantID())) {
            LogC.d(TAG, "RandomTask|prepareRequestStr|params invalid.", false);
            return null;
        }
        return JSONHelper.createRequestStr(request.getMerchantID(), request.getRsaKeyIndex(), createDataStr(JSONHelper.createHeaderStr(request.getSrcTransactionID(), ServerCmdConstant.GET_RANDOM_CMD), request), this.mContext);
    }

    private JSONObject createDataStr(JSONObject headerObject, RandomRequest request) {
        if (headerObject == null) {
            return null;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("header", headerObject);
            obj.put("deviceId", request.getDeviceId());
            obj.put("cplc", request.getCplc());
            LogC.d("content:" + content, false);
            obj.put("sign", DiplomaUtil.getSignature(this.mContext, "nfc.get.random|" + request.getCplc() + "|" + request.getDeviceId() + "|" + SIGN_CERT));
            obj.put(SNBConstant.FIELD_RSA_SIGN_TYPE, SIGN_CERT);
        } catch (JSONException e) {
            LogC.e(TAG, "RandomTask|createDataStr|parse json error", true);
            obj = null;
        }
        return obj;
    }

    public RandomResponse readErrorResponse(int errorCode, String errorMessage) {
        RandomResponse response = new RandomResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        return response;
    }

    public RandomResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        RandomResponse response = new RandomResponse();
        response.returnCode = returnCode;
        try {
            String random = JSONHelper.getStringValue(dataObject, "random");
            if (!StringUtil.isEmpty(random, true)) {
                LogC.i("RandomTask|readSuccessResponse|random", false);
                response.setRand(random);
            }
        } catch (JSONException e) {
            LogC.i("RandomTask|readSuccessResponse|JSONException", false);
        }
        return response;
    }
}
