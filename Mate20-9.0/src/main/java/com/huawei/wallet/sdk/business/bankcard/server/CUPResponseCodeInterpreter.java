package com.huawei.wallet.sdk.business.bankcard.server;

import android.content.Context;
import java.util.LinkedHashMap;

class CUPResponseCodeInterpreter {
    static final int CUP_RESULT_CODE_ALLREADY_DELETED = 1001311318;
    static final int CUP_RESULT_CODE_ALLREADY_DOWNLOADED = 1001311312;
    static final int CUP_RESULT_CODE_APPLET_INSTALL_FAIL = 1001311502;
    static final int CUP_RESULT_CODE_APPLET_UNEXISTS = 1001311307;
    static final int CUP_RESULT_CODE_CARD_STATE_ERR = 1001311343;
    static final int CUP_RESULT_CODE_CMD_ERR = 1001311599;
    static final int CUP_RESULT_CODE_INIT_ERR = 1000100011;
    static final int CUP_RESULT_CODE_NFC_ERR = 1000100009;
    static final int CUP_RESULT_CODE_PERSONLIZED_ERR = 1001311323;
    static final int CUP_RESULT_CODE_PERSONLIZING_DATA_UNEXISTS = 1001311326;
    static final int CUP_RESULT_CODE_SUCCESS = 10000;
    static final int CUP_RESULT_CODE_UPTSM_ERR = 1001311102;
    static final int CUP_RESULT_SECURITY_PERSONLIZED_ERR = 1001311505;
    static final int CUP_RESULT_TSM_SERVICE_KILLED_EXCEPTION = 99999;
    static final int CUP_UNIONPAY_INIT_FAILED_ERR = 1000110097;
    private final Context mContext;

    public CUPResponseCodeInterpreter(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: package-private */
    public int translateReponseCode(int responseCodeStr, String action, String describtion) {
        int responseCode = -99;
        boolean isNetworkErr = true;
        if (responseCodeStr % 10000 != 1) {
            isNetworkErr = false;
        }
        if (isNetworkErr) {
            return -3;
        }
        switch (responseCodeStr) {
            case 10000:
            case CUP_RESULT_CODE_ALLREADY_DOWNLOADED /*1001311312*/:
            case CUP_RESULT_CODE_ALLREADY_DELETED /*1001311318*/:
                responseCode = 0;
                break;
            case CUP_RESULT_TSM_SERVICE_KILLED_EXCEPTION /*99999*/:
                responseCode = -4;
                break;
            case CUP_RESULT_CODE_NFC_ERR /*1000100009*/:
                responseCode = -9;
                break;
            case CUP_RESULT_CODE_INIT_ERR /*1000100011*/:
                responseCode = -8;
                break;
            case CUP_UNIONPAY_INIT_FAILED_ERR /*1000110097*/:
                LinkedHashMap<String, String> params = new LinkedHashMap<>();
                params.put("fail_code", "" + responseCodeStr);
                params.put("fail_reason", "CUP TSM: unionPay plugin init failed.");
                break;
            case CUP_RESULT_CODE_UPTSM_ERR /*1001311102*/:
            case CUP_RESULT_CODE_APPLET_UNEXISTS /*1001311307*/:
            case CUP_RESULT_CODE_PERSONLIZING_DATA_UNEXISTS /*1001311326*/:
            case CUP_RESULT_CODE_CARD_STATE_ERR /*1001311343*/:
                responseCode = -7;
                break;
            case CUP_RESULT_CODE_PERSONLIZED_ERR /*1001311323*/:
            case CUP_RESULT_CODE_APPLET_INSTALL_FAIL /*1001311502*/:
            case CUP_RESULT_CODE_CMD_ERR /*1001311599*/:
                responseCode = -6;
                break;
            case CUP_RESULT_SECURITY_PERSONLIZED_ERR /*1001311505*/:
                responseCode = -10;
                break;
        }
        return responseCode;
    }
}
