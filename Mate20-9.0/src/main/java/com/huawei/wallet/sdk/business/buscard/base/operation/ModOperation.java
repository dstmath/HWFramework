package com.huawei.wallet.sdk.business.buscard.base.operation;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.utils.StringUtil;

public class ModOperation extends Operation {
    public String handleData(String data) throws AppletCardException {
        if (!StringUtil.isEmpty(this.param, true)) {
            return String.valueOf(Integer.parseInt(data) % Integer.parseInt(this.param));
        }
        throw new AppletCardException(2, " ModOperation param is null");
    }
}
