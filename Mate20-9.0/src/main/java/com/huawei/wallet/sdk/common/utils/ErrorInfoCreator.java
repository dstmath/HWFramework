package com.huawei.wallet.sdk.common.utils;

import com.huawei.wallet.sdk.common.http.errorcode.ErrorInfo;
import com.unionpay.tsmservice.data.Constant;

public class ErrorInfoCreator {
    public static ErrorInfo buildSimpleErrorInfo(int code) {
        ErrorInfo info = new ErrorInfo();
        info.setCodeMsg(String.valueOf(code));
        String desc = Constant.CASH_LOAD_FAIL;
        if (code == -401) {
            desc = "delete pass fail";
        } else if (code != -4) {
            switch (code) {
                case 8:
                    desc = "passtype对应的group为空";
                    break;
                case 9:
                    desc = "passsd 申领白卡失败";
                    break;
            }
        } else {
            desc = "update TA fail";
        }
        info.setCodeMsg(desc);
        info.setDisplayDetail(desc);
        info.setSuggestion(desc);
        return info;
    }
}
