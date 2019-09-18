package com.huawei.wallet.sdk.business.buscard.base.operation;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.math.BigInteger;

public class MinusOperation extends Operation {
    public String handleData(String data) throws AppletCardException {
        if (!StringUtil.isEmpty(this.param, true)) {
            return String.valueOf(new BigInteger(data).intValue() - new BigInteger(this.param).intValue());
        }
        throw new AppletCardException(2, " MinusOperation param is null");
    }
}
