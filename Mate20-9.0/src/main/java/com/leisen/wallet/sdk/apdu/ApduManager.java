package com.leisen.wallet.sdk.apdu;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.leisen.wallet.sdk.AppConfig;
import com.leisen.wallet.sdk.bean.CommonRequestParams;
import com.leisen.wallet.sdk.bean.OperAppletReqParams;
import com.leisen.wallet.sdk.business.ApduBean;
import com.leisen.wallet.sdk.business.ApduResBean;
import com.leisen.wallet.sdk.business.BaseBusinessForResp;
import com.leisen.wallet.sdk.business.BaseResponse;
import com.leisen.wallet.sdk.newhttp.AsyncHttpClientX;
import com.leisen.wallet.sdk.newhttp.SimpleResponseHandlerX;
import com.leisen.wallet.sdk.oma.SmartCard;
import com.leisen.wallet.sdk.tsm.TSMOperatorResponse;
import com.leisen.wallet.sdk.util.AppJsonUtil;
import com.leisen.wallet.sdk.util.LogUtil;
import java.util.ArrayList;
import java.util.List;

public class ApduManager extends SimpleResponseHandlerX {
    private static final String BOUNDARY = "==>";
    private static final int BUSINESS_TYPE_ACTIVATE = 35;
    private static final int BUSINESS_TYPE_APPLETOPER = 30;
    private static final int BUSINESS_TYPE_COMMON_METHOD = 40;
    private static final int BUSINESS_TYPE_GPACOPER = 32;
    private static final int BUSINESS_TYPE_INFOINIT = 34;
    private static final int BUSINESS_TYPE_INFOSYNC = 33;
    private static final int BUSINESS_TYPE_SSDOPER = 31;
    private static final int FLAG_ACTIVATE_APPLET = 9;
    private static final int FLAG_ESEINFOSYNC = 2;
    private static final int FLAG_GETCIN = 7;
    private static final int FLAG_GETCPLC = 6;
    private static final int FLAG_GETIIN = 8;
    private static final int FLAG_INFOINIT = 1;
    private static final int FLAG_OPERAPPLET = 4;
    private static final int FLAG_OPERGPAC = 5;
    private static final int FLAG_OPERSSD = 3;
    public static final int SEND_TYPE_FIRST = 1;
    public static final int SEND_TYPE_NEXT = 2;
    private static final String TAG = "ApduManager";
    private ApduResponseHandler mApduResponseHandler = new ApduResponseHandler() {
        public void onSuccess(String response) {
            if (ApduManager.this.mTsmOperatorResponse != null) {
                ApduManager.this.mTsmOperatorResponse.onOperSuccess(response);
            }
            ApduManager.this.clearData();
        }

        public void onSendNext(int result, int index, String rapdu, String sw) {
            int unused = ApduManager.this.mCurrentTaskIndex = ApduManager.this.mCurrentTaskIndex + 1;
            ApduManager.this.sendNextApdu(result, index, rapdu, sw);
        }

        public void OnSendNextError(int result, int index, String rapdu, String sw, Error e) {
            String unused = ApduManager.this.mErrorMessage = e.getMessage();
            int unused2 = ApduManager.this.mCurrentTaskIndex = ApduManager.this.mCurrentTaskIndex + 1;
            ApduManager.this.sendNextApdu(result, index, rapdu, sw);
        }

        public void onFailure(int result, Error e) {
            if (ApduManager.this.mTsmOperatorResponse != null) {
                ApduManager.this.mTsmOperatorResponse.onOperFailure(result, e);
            }
            ApduManager.this.clearData();
        }
    };
    private ApduSmartCardRequest mApduSmartCardRequest;
    private AsyncHttpClientX mAsyncHttpClient;
    private int mBusinessType = -1;
    private CommonRequestParams mCommonRequestParams;
    private AppConfig mConfig;
    private Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentTaskIndex = 1;
    /* access modifiers changed from: private */
    public String mErrorMessage;
    /* access modifiers changed from: private */
    public TSMOperatorResponse mTsmOperatorResponse;

    public ApduManager(Context context, AppConfig config) {
        init(context, config);
    }

    private void init(Context context, AppConfig config) {
        this.mContext = context;
        this.mConfig = config;
        this.mAsyncHttpClient = new AsyncHttpClientX(true);
        this.mApduSmartCardRequest = new ApduSmartCardRequest(this.mContext, config, this.mApduResponseHandler);
    }

    public void requestInfoInit(CommonRequestParams params) {
        this.mBusinessType = 34;
        this.mApduSmartCardRequest.setFlag(1);
        this.mCommonRequestParams = params;
        sendFirstApdu(AppJsonUtil.getBaseReqJsonResult(params, this.mBusinessType, this.mCurrentTaskIndex));
    }

    public void requestEseInfoSync(CommonRequestParams params) {
        this.mBusinessType = 33;
        this.mApduSmartCardRequest.setFlag(2);
        this.mCommonRequestParams = params;
        sendFirstApdu(AppJsonUtil.getBaseReqJsonResult(params, this.mBusinessType, this.mCurrentTaskIndex));
    }

    public void requestOperSSD(int operType, CommonRequestParams params, String ssdAid) {
        this.mBusinessType = 31;
        this.mApduSmartCardRequest.setFlag(3);
        this.mCommonRequestParams = params;
        sendFirstApdu(AppJsonUtil.getOperSSDJsonResult(params, this.mBusinessType, operType, ssdAid, this.mCurrentTaskIndex));
    }

    public void requestOperApplet(int operType, CommonRequestParams params, OperAppletReqParams reqParams) {
        this.mBusinessType = 30;
        this.mApduSmartCardRequest.setFlag(4);
        this.mCommonRequestParams = params;
        sendFirstApdu(AppJsonUtil.getOperAppletJsonResult(params, this.mBusinessType, operType, reqParams, this.mCurrentTaskIndex));
    }

    public void requestactivateApplet(CommonRequestParams params, String appletAid) {
        this.mBusinessType = 35;
        this.mApduSmartCardRequest.setFlag(9);
        this.mCommonRequestParams = params;
        sendFirstApdu(AppJsonUtil.getActivateAppletJsonResult(params, this.mBusinessType, appletAid, this.mCurrentTaskIndex));
    }

    public void requestCommonMethod(CommonRequestParams params) {
        this.mBusinessType = 40;
        this.mApduSmartCardRequest.setFlag(-1);
        this.mCommonRequestParams = params;
        sendFirstApdu(AppJsonUtil.getBaseReqJsonResult(params, this.mBusinessType, this.mCurrentTaskIndex));
    }

    public void requestOperGPAC(int operType, CommonRequestParams params, String appletAid) {
        this.mBusinessType = 32;
        this.mApduSmartCardRequest.setFlag(5);
        this.mCommonRequestParams = params;
        sendFirstApdu(AppJsonUtil.getOperGPACJsonResult(params, this.mBusinessType, operType, appletAid, this.mCurrentTaskIndex));
    }

    public void requestGetCPLC(String aid) {
        this.mApduSmartCardRequest.setFlag(6);
        this.mApduSmartCardRequest.isGetLocalData(true);
        this.mApduSmartCardRequest.setGetLocalDataApdu(AppConfig.APDU_GETCPLC, aid);
        sendRequestToSmartCard();
    }

    public void requestGetCIN(String aid) {
        this.mApduSmartCardRequest.setFlag(7);
        this.mApduSmartCardRequest.isGetLocalData(true);
        this.mApduSmartCardRequest.setGetLocalDataApdu(AppConfig.APDU_GETCIN, aid);
        sendRequestToSmartCard();
    }

    public void requestGetIIN(String aid) {
        this.mApduSmartCardRequest.setFlag(8);
        this.mApduSmartCardRequest.isGetLocalData(true);
        this.mApduSmartCardRequest.setGetLocalDataApdu(AppConfig.APDU_GETIIN, aid);
        sendRequestToSmartCard();
    }

    public void requestSelectSSD(String aid) {
        this.mApduSmartCardRequest.isGetLocalData(true);
        StringBuilder sb = new StringBuilder();
        sb.append("00A40400");
        int i = 0;
        sb.append(String.format("%02X", new Object[]{Integer.valueOf(aid.length() / 2)}));
        sb.append(aid);
        String[] capduArr = {sb.toString(), ""};
        List<ApduBean> capdus = new ArrayList<>();
        while (true) {
            int i2 = i;
            if (i2 < capduArr.length) {
                capdus.add(new ApduBean(capduArr[i2]));
                i = i2 + 1;
            } else {
                this.mApduSmartCardRequest.setCapduList(capdus);
                sendRequestToSmartCard();
                return;
            }
        }
    }

    public void requestActivateApplet(String apdu) {
        this.mApduSmartCardRequest.isGetLocalData(true);
        int i = 0;
        String[] capduArr = {"00A4040008A00000015143525300", apdu};
        List<ApduBean> capdus = new ArrayList<>();
        while (true) {
            int i2 = i;
            if (i2 < capduArr.length) {
                capdus.add(new ApduBean(capduArr[i2]));
                i = i2 + 1;
            } else {
                this.mApduSmartCardRequest.setCapduList(capdus);
                sendRequestToSmartCard();
                return;
            }
        }
    }

    public void requestdeactivateApplet(String apdu) {
        this.mApduSmartCardRequest.isGetLocalData(true);
        int i = 0;
        String[] capduArr = {"00A4040008A00000015143525300", apdu};
        List<ApduBean> capdus = new ArrayList<>();
        while (true) {
            int i2 = i;
            if (i2 < capduArr.length) {
                capdus.add(new ApduBean(capduArr[i2]));
                i = i2 + 1;
            } else {
                this.mApduSmartCardRequest.setCapduList(capdus);
                sendRequestToSmartCard();
                return;
            }
        }
    }

    private void sendFirstApdu(String request) {
        sendApduToServer(request);
    }

    /* access modifiers changed from: private */
    public void sendNextApdu(int result, int index, String rapdu, String sw) {
        ApduResBean rapduList = new ApduResBean();
        rapduList.setIndex(index);
        rapduList.setApdu(rapdu);
        rapduList.setSw(sw);
        sendApduToServer(AppJsonUtil.getReqNextJsonResult(this.mCommonRequestParams, this.mBusinessType, rapduList, result, this.mCurrentTaskIndex));
    }

    private void sendApduToServer(String request) {
        LogUtil.i(TAG, "==>request url:" + this.mConfig.STREAMURL);
        LogUtil.i(TAG, "==>request:" + AppJsonUtil.removeSensitiveInfo(request));
        if (request != null) {
            try {
                this.mAsyncHttpClient.post(this.mContext, this.mConfig.STREAMURL, request, this);
            } catch (Exception e) {
                this.mApduResponseHandler.sendFailureMessage(100011, new Error(e.getMessage()));
            }
        }
    }

    public void setTsmOperatorResponse(TSMOperatorResponse tsmOperatorResponse) {
        this.mTsmOperatorResponse = tsmOperatorResponse;
    }

    private void sendRequestToSmartCard() {
        if (this.mApduSmartCardRequest != null) {
            this.mApduSmartCardRequest.run();
        }
    }

    /* access modifiers changed from: private */
    public void clearData() {
        this.mCurrentTaskIndex = 1;
        this.mBusinessType = -1;
        this.mCommonRequestParams = null;
        SmartCard.getInstance().closeService();
    }

    public void onSuccess(String responseString) {
        LogUtil.i(TAG, "==>response:" + responseString);
        BaseResponse<BaseBusinessForResp> response = null;
        try {
            response = (BaseResponse) new Gson().fromJson(responseString, new TypeToken<BaseResponse<BaseBusinessForResp>>() {
            }.getType());
        } catch (JsonSyntaxException e) {
            this.mApduResponseHandler.sendFailureMessage(100012, new Error("response data parse failure"));
        }
        if (response == null) {
            this.mApduResponseHandler.sendFailureMessage(100012, new Error("response data is empty"));
        } else if (response.getBusiness().getOperationResult() != 100000) {
            String operationDes = response.getBusiness().getOperationDes();
            if (this.mErrorMessage != null && !"".equals(this.mErrorMessage)) {
                operationDes = operationDes + ":" + this.mErrorMessage;
                this.mErrorMessage = null;
            }
            this.mApduResponseHandler.sendFailureMessage(100013, new Error(operationDes));
        } else {
            if (response.getBusiness().getFinishFlag() == 0) {
                this.mApduResponseHandler.sendSuccessMessage(null);
            } else {
                this.mApduSmartCardRequest.setCapduList(response.getBusiness().getCapduList());
                sendRequestToSmartCard();
            }
        }
    }

    public void OnFailure(String responseString, Throwable error) {
        LogUtil.e(TAG, "==>response:" + responseString);
        this.mApduResponseHandler.sendFailureMessage(100010, new Error(error.getMessage()));
    }
}
