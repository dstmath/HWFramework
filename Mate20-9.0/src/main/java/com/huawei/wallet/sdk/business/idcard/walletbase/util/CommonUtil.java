package com.huawei.wallet.sdk.business.idcard.walletbase.util;

import com.google.gson.Gson;
import java.util.List;
import java.util.Map;

public class CommonUtil {
    public static boolean isNull(Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isNull(List<?> list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        if (json == null) {
            return null;
        }
        return new Gson().fromJson(json, classOfT);
    }
}
