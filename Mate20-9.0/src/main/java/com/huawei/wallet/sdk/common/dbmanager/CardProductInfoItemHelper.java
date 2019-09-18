package com.huawei.wallet.sdk.common.dbmanager;

import com.huawei.wallet.sdk.common.utils.StringUtil;
import java.util.ArrayList;
import java.util.List;

public class CardProductInfoItemHelper {
    public static List<String> getReservedNField(CardProductInfoItem cardProductInfoItem) {
        ArrayList<String> reservedN = new ArrayList<>();
        addString(cardProductInfoItem.getReserved1(), reservedN);
        addString(cardProductInfoItem.getReserved2(), reservedN);
        addString(cardProductInfoItem.getReserved3(), reservedN);
        addString(cardProductInfoItem.getReserved4(), reservedN);
        addString(cardProductInfoItem.getReserved5(), reservedN);
        addString(cardProductInfoItem.getReserved6(), reservedN);
        return reservedN;
    }

    private static void addString(String data, ArrayList<String> list) {
        if (!StringUtil.isEmpty(data, true)) {
            list.add(data);
        }
    }
}
