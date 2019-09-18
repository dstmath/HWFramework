package com.huawei.wallet.sdk.business.idcard.idcard.server.response;

import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.huawei.wallet.sdk.common.log.LogC;

public class ServerAccessCancelEidResponse extends ServerAccessBaseResponse {
    private static final String ERROR_INFO_NOT_EXIST_EID = "P93101";
    private static final String TAG = "IDCard:CancelEid";

    public boolean noEid() {
        if (this.returnCode == 6) {
            ErrorInfo errorInfo = getErrorCodeInfo();
            if (errorInfo != null && errorInfo.getOriginalCode().equals(ERROR_INFO_NOT_EXIST_EID)) {
                LogC.i(TAG, "reutrn code is  P93101, it is ok.", false);
                return true;
            }
        }
        return false;
    }
}
