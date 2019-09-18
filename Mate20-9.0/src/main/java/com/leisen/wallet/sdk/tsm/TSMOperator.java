package com.leisen.wallet.sdk.tsm;

import android.content.Context;
import com.leisen.wallet.sdk.AppConfig;
import com.leisen.wallet.sdk.apdu.ApduManager;
import com.leisen.wallet.sdk.bean.CommonRequestParams;
import com.leisen.wallet.sdk.bean.OperAppletReqParams;

public class TSMOperator implements ITSMOperator, TSMOperatorResponse {
    private static final int RETURN_APPLETAID_IS_NULL = 100006;
    private static final int RETURN_APPLETVERSION_IS_NULL = 100007;
    private static final int RETURN_COMMOMREQUESTPARAMS_IS_NULL = 100001;
    private static final int RETURN_CPLC_IS_NULL = 100008;
    private static final int RETURN_FUNCALLID_IS_NULL = 100004;
    public static final int RETURN_NETWORK_ERROR = 100010;
    private static final int RETURN_OPERAPPLETREQPARAMS_IS_NULL = 100002;
    public static final int RETURN_RESPONSE_PARSE_ERROR = 100012;
    public static final int RETURN_SERVER_ERROR = 100013;
    private static final int RETURN_SERVICEID_IS_NULL = 100003;
    public static final int RETURN_SMARTCARD_OPER_FAILURE = 100009;
    private static final int RETURN_SSDAID_IS_NULL = 100005;
    private static final int RETURN_SUCCESS = 100000;
    public static final int RETURN_UNKNOW_ERROR = 100011;
    private ApduManager mApduManager;
    private int mOperatorResult = 100000;
    private TSMOperatorResponse mTsmOperatorResponse;

    private TSMOperator() {
    }

    private TSMOperator(Context context, AppConfig config) {
        this.mApduManager = new ApduManager(context, config);
        this.mApduManager.setTsmOperatorResponse(this);
    }

    public static synchronized TSMOperator getInstance(Context context, String url, int reader) {
        TSMOperator tSMOperator;
        synchronized (TSMOperator.class) {
            tSMOperator = new TSMOperator(context, new AppConfig(url, reader));
        }
        return tSMOperator;
    }

    public int notifyEseInfoSync(CommonRequestParams request) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        this.mApduManager.requestEseInfoSync(request);
        return this.mOperatorResult;
    }

    public int notifyInfoInit(CommonRequestParams request) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        this.mApduManager.requestInfoInit(request);
        return this.mOperatorResult;
    }

    public int addGPAC(CommonRequestParams request, String appletAid) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        if (appletAid == null) {
            return 100006;
        }
        this.mApduManager.requestOperGPAC(1, request, appletAid);
        return this.mOperatorResult;
    }

    public int deleteGPAC(CommonRequestParams request, String appletAid) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        if (appletAid == null) {
            return 100006;
        }
        this.mApduManager.requestOperGPAC(2, request, appletAid);
        return this.mOperatorResult;
    }

    public int createSSD(CommonRequestParams request, String ssdAid) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        if (ssdAid == null) {
            return 100005;
        }
        this.mApduManager.requestOperSSD(1, request, ssdAid);
        return this.mOperatorResult;
    }

    public int deleteSSD(CommonRequestParams request, String ssdAid) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        if (ssdAid == null) {
            return 100005;
        }
        this.mApduManager.requestOperSSD(2, request, ssdAid);
        return this.mOperatorResult;
    }

    public int lockSSD(CommonRequestParams request, String ssdAid) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        if (ssdAid == null) {
            return 100005;
        }
        this.mApduManager.requestOperSSD(3, request, ssdAid);
        return this.mOperatorResult;
    }

    public int unlockSSD(CommonRequestParams request, String ssdAid) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        if (ssdAid == null) {
            return 100005;
        }
        this.mApduManager.requestOperSSD(4, request, ssdAid);
        return this.mOperatorResult;
    }

    public int installApplet(CommonRequestParams request, OperAppletReqParams params) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        int operResult = checkOperAppetReqParams(params);
        if (operResult != -1) {
            return operResult;
        }
        this.mApduManager.requestOperApplet(1, request, params);
        return this.mOperatorResult;
    }

    public int deleteApplet(CommonRequestParams request, OperAppletReqParams params) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        int operResult = checkOperAppetReqParams(params);
        if (operResult != -1) {
            return operResult;
        }
        this.mApduManager.requestOperApplet(2, request, params);
        return this.mOperatorResult;
    }

    public int lockApplet(CommonRequestParams request, OperAppletReqParams params) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        int operResult = checkOperAppetReqParams(params);
        if (operResult != -1) {
            return operResult;
        }
        this.mApduManager.requestOperApplet(3, request, params);
        return this.mOperatorResult;
    }

    public int unlockApplet(CommonRequestParams request, OperAppletReqParams params) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        int operResult = checkOperAppetReqParams(params);
        if (operResult != -1) {
            return operResult;
        }
        this.mApduManager.requestOperApplet(4, request, params);
        return this.mOperatorResult;
    }

    public int activateApplet(CommonRequestParams request, String appletAid) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        if (appletAid == null) {
            return 100006;
        }
        this.mApduManager.requestactivateApplet(request, appletAid);
        return this.mOperatorResult;
    }

    public TSMOperator setTsmOperatorResponse(TSMOperatorResponse tsmOperatorResponse) {
        this.mTsmOperatorResponse = tsmOperatorResponse;
        return this;
    }

    private int checkCommonRequestParams(CommonRequestParams request) {
        if (request == null) {
            return 100001;
        }
        if (request.getCplc() == null) {
            return 100008;
        }
        if (request.getServiceId() == null) {
            return 100003;
        }
        if (request.getFunCallId() == null) {
            return 100004;
        }
        return -1;
    }

    private int checkOperAppetReqParams(OperAppletReqParams params) {
        if (params == null) {
            return 100002;
        }
        if (params.getAppletAid() == null) {
            return 100006;
        }
        return -1;
    }

    public void getCPLC(String aid) {
        this.mApduManager.requestGetCPLC(aid);
    }

    public void getCIN(String aid) {
        this.mApduManager.requestGetCIN(aid);
    }

    public void getIIN(String aid) {
        this.mApduManager.requestGetIIN(aid);
    }

    public void activateApplet(String apdu) {
        this.mApduManager.requestActivateApplet(apdu);
    }

    public void deactivateApplet(String apdu) {
        this.mApduManager.requestdeactivateApplet(apdu);
    }

    public void selectSSD(String aid) {
        this.mApduManager.requestSelectSSD(aid);
    }

    public void onOperSuccess(String response) {
        this.mOperatorResult = 100000;
        if (this.mTsmOperatorResponse != null) {
            this.mTsmOperatorResponse.onOperSuccess(response);
        }
    }

    public void onOperFailure(int result, Error e) {
        this.mOperatorResult = result;
        if (this.mTsmOperatorResponse != null) {
            this.mTsmOperatorResponse.onOperFailure(result, e);
        }
    }

    public int commonExecute(CommonRequestParams request) {
        int result = checkCommonRequestParams(request);
        if (result != -1) {
            return result;
        }
        this.mApduManager.requestCommonMethod(request);
        return this.mOperatorResult;
    }
}
