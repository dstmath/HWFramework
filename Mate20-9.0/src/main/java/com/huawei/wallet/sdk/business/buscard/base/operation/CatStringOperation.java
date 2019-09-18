package com.huawei.wallet.sdk.business.buscard.base.operation;

import com.huawei.wallet.sdk.common.utils.StringUtil;

public class CatStringOperation extends Operation {
    public String handleData(String data) {
        if (StringUtil.isEmpty(this.param, true)) {
            return data;
        }
        return data + this.param;
    }
}
