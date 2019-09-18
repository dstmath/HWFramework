package com.huawei.wallet.sdk.business.buscard.base.util;

import java.text.NumberFormat;

public class UIUtil {
    public static String getFormatInternationlDefault() {
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        return numberFormat.format(Double.parseDouble("0.00"));
    }
}
