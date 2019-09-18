package com.huawei.wallet.sdk.business.idcard.idcard.logic;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.idcard.server.IdCardServer;
import com.huawei.wallet.sdk.business.idcard.idcard.server.request.CardStatusQueryRequest;
import com.huawei.wallet.sdk.business.idcard.idcard.server.response.CardStatusQueryResponse;
import com.huawei.wallet.sdk.business.idcard.walletbase.carrera.constant.AutoReportErrorCode;
import com.huawei.wallet.sdk.common.log.LogC;
import java.util.HashMap;

public class QueryCardStatusRunnable implements Runnable {
    private static final String TAG = "IDCard:QueryCardStatusRunnable";
    private QueryCardStatusCallback mCallback;
    private Context mContext;
    private String mNeedQrCode;
    private CardStatusQueryRequest mRequest;
    private CardStatusQueryResponse mResponse;

    public QueryCardStatusRunnable(Context context, QueryCardStatusCallback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    public QueryCardStatusRunnable(Context context, String mNeedQrCode2, QueryCardStatusCallback callback) {
        this.mContext = context;
        this.mNeedQrCode = mNeedQrCode2;
        this.mCallback = callback;
    }

    public void run() {
        generateRequest();
        handleResponse();
    }

    private void generateRequest() {
        this.mRequest = new CardStatusQueryRequest();
        this.mRequest.setIsNeedServiceTokenAuth(true);
        this.mRequest.setNeedQrCode(this.mNeedQrCode);
        this.mResponse = new IdCardServer(this.mContext).queryCardStatus(this.mRequest);
    }

    private void handleResponse() {
        if (this.mResponse.returnCode == 0) {
            this.mCallback.onSuccess(this.mResponse.getData());
        } else if (this.mResponse.returnCode == -4) {
            HashMap hashMap = new HashMap();
            hashMap.put("fail_reason", "queryIdCardStatus server overload 503");
            LogC.e(TAG, "queryIdCardStatus server overload 503", null, AutoReportErrorCode.ERROR_EVENT_ID_NFC_SERVER_OVERLOAD_ERR, hashMap, false, false);
            this.mCallback.onFail(this.mResponse.returnCode, "queryIdCardStatus server overload 503");
        } else {
            String errInfo = "queryCardStatus fail, returnCode : " + this.mResponse.returnCode;
            if (this.mResponse.returnCode == -1 || this.mResponse.returnCode == -2) {
                LogC.e(TAG, errInfo, false);
            } else {
                HashMap hashMap2 = new HashMap();
                hashMap2.put("fail_reason", errInfo);
                LogC.e(TAG, errInfo, null, AutoReportErrorCode.ERROR_EVENT_ID_NFC_SERVER_ERR, hashMap2, false, false);
            }
            this.mCallback.onFail(this.mResponse.returnCode, errInfo);
        }
    }

    public void exec() {
        generateRequest();
        handleResponse();
    }
}
