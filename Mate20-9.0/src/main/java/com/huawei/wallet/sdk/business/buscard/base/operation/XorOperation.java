package com.huawei.wallet.sdk.business.buscard.base.operation;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;

public class XorOperation extends Operation {
    public String handleData(String data) throws AppletCardException {
        char[] chars = data.toCharArray();
        int num = Character.getNumericValue(chars[0]);
        for (int i = 1; i < chars.length; i++) {
            num ^= Character.getNumericValue(chars[i]);
        }
        return String.valueOf(num);
    }
}
