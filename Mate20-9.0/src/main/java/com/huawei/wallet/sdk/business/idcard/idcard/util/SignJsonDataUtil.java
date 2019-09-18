package com.huawei.wallet.sdk.business.idcard.idcard.util;

import android.content.Context;
import com.huawei.wallet.sdk.business.idcard.walletbase.tcis.TCISParameterUtils;
import org.json.JSONObject;

public class SignJsonDataUtil {
    public static String signJsonData(JSONObject jsonData, Context context) {
        if (isNeedSign()) {
            return new TCISParameterUtils().reSignJsonData(jsonData, context, false);
        }
        return jsonData.toString();
    }

    private static boolean isNeedSign() {
        return true;
    }
}
