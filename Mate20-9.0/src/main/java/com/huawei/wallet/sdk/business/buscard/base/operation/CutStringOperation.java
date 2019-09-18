package com.huawei.wallet.sdk.business.buscard.base.operation;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.utils.StringUtil;

public class CutStringOperation extends Operation {
    public String handleData(String data) throws AppletCardException {
        if (!StringUtil.isEmpty(this.param, true)) {
            String[] idxStr = this.param.split("-");
            if (idxStr.length >= 2) {
                int[] idx = {Integer.parseInt(idxStr[0]), Integer.parseInt(idxStr[1])};
                if (idx[0] <= idx[1] && data.length() >= idx[1]) {
                    return data.substring(idx[0], idx[1]);
                }
                throw new AppletCardException(2, " CutStringOperation index param config error. param : " + this.param);
            }
            throw new AppletCardException(2, " CutStringOperation index param config error. param : " + this.param);
        }
        throw new AppletCardException(2, " CutStringOperation param is null");
    }
}
