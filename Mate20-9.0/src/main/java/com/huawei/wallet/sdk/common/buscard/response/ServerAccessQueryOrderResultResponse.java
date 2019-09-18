package com.huawei.wallet.sdk.common.buscard.response;

import com.huawei.wallet.sdk.common.apdu.response.ServerAccessBaseResponse;

public class ServerAccessQueryOrderResultResponse extends ServerAccessBaseResponse {
    public static final int RESULT_FAILE = 1;
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_WAIT = 2;
    private int result;

    public void setResult(int result2) {
        this.result = result2;
    }

    public int getResult() {
        return this.result;
    }
}
