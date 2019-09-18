package com.huawei.wallet.sdk.common.apdu.response;

import com.huawei.wallet.sdk.common.http.response.BaseResponse;

public class CardServerBaseResponse extends BaseResponse {
    public static final int RESPONSE_CODE_APPLY_TRANSFER_OUT_USERID_ERROR = 7;
    public static final int RESPONSE_CODE_CANNOT_BE_RESOLVED = -98;
    public static final int RESPONSE_CODE_CONNECTION_FAILED = -2;
    public static final int RESPONSE_CODE_DOWN_VERSION = 99;
    public static final int RESPONSE_CODE_EXTERNAL_FAILED = 6;
    public static final int RESPONSE_CODE_INTERNAL_ERROR = 3;
    public static final int RESPONSE_CODE_NO_ACCESS_AUTHORITY = 4;
    public static final int RESPONSE_CODE_NO_NETWORK_FAILED = -1;
    public static final int RESPONSE_CODE_OPERATION_FAILED = 5;
    public static final int RESPONSE_CODE_OTHER_ERRORS = -99;
    public static final int RESPONSE_CODE_PARAMS_ERROR = 1;
    public static final int RESPONSE_CODE_SERVER_OVERLOAD_ERROR = -4;
    public static final int RESPONSE_CODE_SIGNATURE_ERROR = 2;
    public static final int RESPONSE_CODE_SUCCESS = 0;
    public static final String RESPONSE_CONNECTION_FAILED_MESSAGE = "RESPONSE_CONNECTION_FAILED_MESSAGE";
    public static final String RESPONSE_MESSAGE_CONNECTION_FAILED = "RESPONSE_MESSAGE_CONNECTION_FAILED";
    public static final String RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION = "RESPONSE_MESSAGE_CONNECTION_FAILED_KEY_MANAGEMENT_EXCEPTION";
    public static final String RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION = "RESPONSE_MESSAGE_CONNECTION_FAILED_NO_SUCH_ALGORITHM_EXCEPTION";
    public static final String RESPONSE_MESSAGE_NO_NETWORK_FAILED = "RESPONSE_MESSAGE_NO_NETWORK_FAILED";
    public static final String RESPONSE_MESSAGE_PARAMS_ERROR = "RESPONSE_MESSAGE_PARAMS_ERROR";
    public static final String RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION = "RESPONSE_MESSAGE_PARAMS_ERROR_MALFORMED_URL_EXCEPTION";
    public static final String RESPONSE_MESSAGE_SERVER_OVERLOAD_ERROR = "RESPONSE_MESSAGE_SERVER_OVERLOAD_ERROR";
    public String materialName;
    public String openCardLotteryResult;
    public String promotionId;
    public String responseType;
    private String resultDesc = null;
    public int returnCode = -99;
    public String triggerOpencardID;
    public String uploadConfigPath;
    public String uploadObsPath;

    public String getResultDesc() {
        return this.resultDesc;
    }

    public void setResultDesc(String resultDesc2) {
        this.resultDesc = resultDesc2;
    }

    public CardServerBaseResponse() {
    }

    public CardServerBaseResponse(int reCode, String errorMessage) {
        this.returnCode = reCode;
        this.resultDesc = errorMessage;
    }
}
