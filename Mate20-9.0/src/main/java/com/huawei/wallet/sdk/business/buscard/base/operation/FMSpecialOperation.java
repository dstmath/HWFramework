package com.huawei.wallet.sdk.business.buscard.base.operation;

import android.text.TextUtils;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.math.BigInteger;

public class FMSpecialOperation extends Operation {
    /* access modifiers changed from: protected */
    public String handleData(String data) throws AppletCardException {
        if (!StringUtil.isEmpty(this.param, true)) {
            return getCardNum(data);
        }
        throw new AppletCardException(2, " FMSpecialOperation param is null");
    }

    private String getCardNum(String serialNum) {
        if (TextUtils.isEmpty(serialNum) || serialNum.length() <= 7) {
            return null;
        }
        int length = serialNum.length();
        String tempInnerNum = new BigInteger(serialNum.substring(length - 8, length), 16).toString(10);
        return getInnerNum(tempInnerNum).insert(0, getCheckNum(tempInnerNum)).toString();
    }

    private StringBuilder getInnerNum(String original) {
        if (TextUtils.isEmpty(original)) {
            return new StringBuilder("0000000000");
        }
        char[] nums = original.toCharArray();
        int length = nums.length;
        StringBuilder sBuilder = new StringBuilder(10);
        if (length < 10) {
            for (int i = 0; i < 10 - length; i++) {
                sBuilder.append("0");
            }
        }
        for (int i2 = length - 1; i2 >= 1; i2 -= 2) {
            sBuilder.append(nums[i2 - 1]);
            sBuilder.append(nums[i2]);
        }
        if ((length & 1) == 1) {
            sBuilder.append(nums[0]);
        }
        return sBuilder;
    }

    private int getCheckNum(String innerNum) {
        int checkNum;
        int checkNum2 = 0;
        if (innerNum == null || innerNum.length() == 0) {
            return 0;
        }
        int length = innerNum.length();
        char[] nums = innerNum.toCharArray();
        for (int i = length - 1; i >= 0; i--) {
            int num = nums[i] - '0';
            if ((((length - 1) - i) & 1) == 1) {
                checkNum2 += num;
            } else {
                int num2 = num * 2;
                checkNum2 += (num2 / 10) + (num2 % 10);
            }
        }
        if (checkNum2 % 10 == 0) {
            checkNum = 0;
        } else {
            checkNum = (((checkNum2 / 10) + 1) * 10) - checkNum2;
        }
        return checkNum;
    }
}
