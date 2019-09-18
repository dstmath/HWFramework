package com.huawei.wallet.sdk.business.buscard.base.traffic.readers;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import java.util.List;

public class CardBalanceInfoReader extends InfoReader<Integer[]> {
    private static final String TAG = "CardBalanceInfoReader";

    public CardBalanceInfoReader(IAPDUService omaService) {
        super(omaService);
    }

    /* access modifiers changed from: protected */
    public Integer[] handleResult(List<List<String>> results) throws AppletCardException {
        Integer[] amounts = new Integer[2];
        int i = 0;
        if (results.size() >= 2) {
            while (true) {
                int i2 = i;
                if (i2 >= 2) {
                    break;
                }
                List<String> datas = results.get(i2);
                checkData(TAG, datas);
                amounts[i2] = Integer.valueOf(Integer.parseInt(datas.get(datas.size() - 1)));
                i = i2 + 1;
            }
        } else {
            amounts[0] = 0;
            List<String> datas2 = results.get(0);
            checkData(TAG, datas2);
            amounts[1] = Integer.valueOf(Integer.parseInt(datas2.get(datas2.size() - 1)));
        }
        return amounts;
    }
}
