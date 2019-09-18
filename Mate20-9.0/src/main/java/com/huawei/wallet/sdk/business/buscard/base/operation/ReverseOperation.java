package com.huawei.wallet.sdk.business.buscard.base.operation;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.utils.StringUtil;

public class ReverseOperation extends Operation {
    public String handleData(String data) throws AppletCardException {
        if (!StringUtil.isEmpty(this.param, true)) {
            int step = Integer.parseInt(this.param);
            int totleLen = data.length();
            if (step <= 0 || totleLen % step != 0) {
                throw new AppletCardException(2, " ReverseOperation the data length can not divide setp . len : " + totleLen + " step : " + step);
            }
            char[] newChars = new char[totleLen];
            if (step != 1) {
                return rervse(split(data, step));
            }
            for (int i = 0; i < totleLen; i++) {
                newChars[i] = data.charAt((totleLen - i) - 1);
            }
            return String.copyValueOf(newChars);
        }
        throw new AppletCardException(2, " ReverseOperation param is null");
    }

    private String[] split(String data, int step) {
        int len = data.length() / step;
        String[] datas = new String[len];
        for (int i = 0; i < len; i++) {
            datas[i] = data.substring(i * step, (i + 1) * step);
        }
        return datas;
    }

    private String rervse(String[] datas) {
        StringBuilder sBuild = new StringBuilder();
        for (int i = datas.length - 1; i >= 0; i--) {
            sBuild.append(datas[i]);
        }
        return sBuild.toString();
    }
}
