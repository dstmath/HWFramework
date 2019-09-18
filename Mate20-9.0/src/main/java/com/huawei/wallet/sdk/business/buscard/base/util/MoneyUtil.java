package com.huawei.wallet.sdk.business.buscard.base.util;

import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import java.text.DecimalFormat;

public class MoneyUtil {
    public static String formatMoneyByTwoPoint(double payMoney) {
        return new DecimalFormat("0.00").format(payMoney);
    }

    public static String formatMoneyBySixPoint(double payMoney) {
        return new DecimalFormat("0.000000").format(payMoney);
    }

    public static int convertYuanToFen(String amount) {
        try {
            return (int) Math.round(100.0d * Double.parseDouble(amount));
        } catch (NumberFormatException e) {
            LogX.i("MoneyUtil convertYuanToFen NumberFormatException. amount : " + amount);
            return -1;
        }
    }

    public static String convertFenToYuan(long amount) {
        String fenStr;
        long total = amount;
        boolean isPositive = total >= 0;
        if (!isPositive) {
            total *= -1;
        }
        long fen = total % 100;
        long yuan = total / 100;
        if (fen < 10) {
            fenStr = "0" + String.valueOf(fen);
        } else {
            fenStr = String.valueOf(fen);
        }
        if (isPositive) {
            return String.valueOf(yuan) + "." + fenStr;
        }
        return "-" + String.valueOf(yuan) + "." + fenStr;
    }

    public static String convertFenToYuan(String amount) {
        try {
            return convertFenToYuan(Long.parseLong(amount));
        } catch (NumberFormatException e) {
            return "";
        }
    }
}
