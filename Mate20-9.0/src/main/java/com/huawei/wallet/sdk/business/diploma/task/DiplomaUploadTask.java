package com.huawei.wallet.sdk.business.diploma.task;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.wallet.sdk.business.buscard.cloudtransferout.snb.SNBConstant;
import com.huawei.wallet.sdk.business.diploma.request.DiplomaUploadRequest;
import com.huawei.wallet.sdk.business.diploma.response.DiplomaUploadResponse;
import com.huawei.wallet.sdk.common.http.service.ServerCmdConstant;
import com.huawei.wallet.sdk.common.http.task.HttpConnTask;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.JSONHelper;
import org.json.JSONException;
import org.json.JSONObject;

public class DiplomaUploadTask extends HttpConnTask<DiplomaUploadResponse, DiplomaUploadRequest> {
    private static final String TAG = "DiplomaUploadTask";

    public DiplomaUploadTask(Context mContext, String mUrl, DiplomaUploadRequest request) {
        super(mContext, mUrl);
    }

    /* access modifiers changed from: protected */
    public String prepareRequestStr(DiplomaUploadRequest diplomaUploadRequest) {
        if (diplomaUploadRequest == null || TextUtils.isEmpty(diplomaUploadRequest.getSrcTransactionID()) || TextUtils.isEmpty(diplomaUploadRequest.getMerchantID())) {
            LogC.d(TAG, "DiplomaUploadTask prepareRequestStr, params invalid.", false);
            return null;
        }
        return JSONHelper.createRequestStr(diplomaUploadRequest.getMerchantID(), diplomaUploadRequest.getRsaKeyIndex(), createDataStr(JSONHelper.createHeaderStr(diplomaUploadRequest.getSrcTransactionID(), ServerCmdConstant.DIPLOMA_UPLOAD), diplomaUploadRequest), this.mContext);
    }

    private JSONObject createDataStr(JSONObject headerObject, DiplomaUploadRequest request) {
        if (headerObject == null) {
            return null;
        }
        JSONObject obj = new JSONObject();
        try {
            obj.put("header", headerObject);
            obj.put("deviceCert", request.getDeviceCert());
            obj.put("businessCert", request.getBusinessCert());
            obj.put("deviceId", request.getDeviceId());
            obj.put("cplcList", request.getCplcList());
            obj.put("sign", request.getSign());
            obj.put(SNBConstant.FIELD_RSA_SIGN_TYPE, request.getSignType());
        } catch (JSONException e) {
            LogC.e(TAG, "DiplomaUploadTask createDataStr parse json error", true);
            obj = null;
        }
        return obj;
    }

    /* access modifiers changed from: protected */
    public DiplomaUploadResponse readErrorResponse(int errorCode, String errorMessage) {
        DiplomaUploadResponse response = new DiplomaUploadResponse();
        response.returnCode = errorCode;
        response.setResultDesc(errorMessage);
        return response;
    }

    /* access modifiers changed from: protected */
    public DiplomaUploadResponse readSuccessResponse(int returnCode, String returnDesc, JSONObject dataObject) {
        LogC.e(TAG, "readSuccessResponse" + returnCode, true);
        DiplomaUploadResponse response = new DiplomaUploadResponse();
        response.setResultCode(returnCode);
        response.setReturnDesc(returnDesc);
        return response;
    }
}
