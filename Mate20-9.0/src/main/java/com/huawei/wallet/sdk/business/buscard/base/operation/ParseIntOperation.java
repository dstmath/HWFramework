package com.huawei.wallet.sdk.business.buscard.base.operation;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.math.BigInteger;

public class ParseIntOperation extends Operation {
    public String handleData(String data) throws AppletCardException {
        if (!StringUtil.isEmpty(this.param, true)) {
            return String.valueOf(new BigInteger(data, Integer.parseInt(this.param)).intValue());
        }
        throw new AppletCardException(2, " ParseIntOperation param is null");
    }
}
