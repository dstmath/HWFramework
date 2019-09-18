package com.huawei.wallet.sdk.business.idcard.walletbase.uniwallet.util;

import com.huawei.wallet.sdk.common.utils.TimeUtil;
import java.security.SecureRandom;
import java.util.Date;

public final class Generator {
    private Generator() {
    }

    public static String generateSrcId() {
        int randomNumber = (new SecureRandom().nextInt(10000000) + 10000000) % 10000000;
        if (randomNumber < 1000000) {
            randomNumber += 1000000;
        }
        return TimeUtil.formatDate2String(new Date(System.currentTimeMillis()), TimeUtil.YEAR_TO_MSEL_NO_LINE) + randomNumber;
    }
}
