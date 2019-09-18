package com.huawei.wallet.sdk.business.buscard.base.traffic.readers;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import java.util.List;

public class CardNumInfoReader extends InfoReader<String> {
    private static final String TAG = "CardNumInfoReader";

    public CardNumInfoReader(IAPDUService omaService) {
        super(omaService);
    }

    /* access modifiers changed from: protected */
    public String handleResult(List<List<String>> results) throws AppletCardException {
        List<String> datas = results.get(0);
        checkData(TAG, datas);
        return datas.get(datas.size() - 1);
    }
}
