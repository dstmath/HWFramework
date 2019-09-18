package com.huawei.wallet.sdk.business.buscard.base.util;

import android.text.TextUtils;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtils {
    public static String getFormatRMB(float num) {
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        decimalFormat.applyPattern("0.00");
        return decimalFormat.format((double) num);
    }

    public static String convertYuanToPoint(String amount) {
        return String.valueOf(Math.round(Double.parseDouble(amount) * 100.0d));
    }

    public static String getFormatInternationlMoney(long value) {
        if (value >= 0) {
            double amountValue = ((double) value) / 100.0d;
            try {
                NumberFormat numberFormat = NumberFormat.getInstance();
                numberFormat.setMaximumFractionDigits(2);
                numberFormat.setMinimumFractionDigits(2);
                return numberFormat.format(amountValue);
            } catch (NumberFormatException e) {
            }
        }
        return UIUtil.getFormatInternationlDefault();
    }

    public static String getFormatMoney(long value) {
        if (0 > value) {
            return "0.00";
        }
        DecimalFormat deci = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        deci.applyPattern("0.00");
        return deci.format(((double) value) / 100.0d);
    }

    public static long getFormatMoney(String value) {
        if (TextUtils.isEmpty(value)) {
            return 0;
        }
        return Long.parseLong(value.replaceAll("\\.|\\,", ""));
    }

    public static String getFormatAmount(String amount) {
        try {
            double amountValue = Double.parseDouble(amount);
            DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
            decimalFormat.applyPattern("0.00");
            double amountValue2 = Double.parseDouble(decimalFormat.format(amountValue));
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            numberFormat.setMinimumFractionDigits(2);
            return numberFormat.format(amountValue2);
        } catch (NumberFormatException e) {
            return UIUtil.getFormatInternationlDefault();
        }
    }

    public static String getRechargeFormatAmount(String amount) {
        try {
            return NumberFormat.getInstance().format(Integer.valueOf(Integer.parseInt(amount)));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
