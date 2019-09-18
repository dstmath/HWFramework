package com.huawei.wallet.sdk.business.buscard.base.traffic.readers;

import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import java.util.List;

public class CardDateInfoReader extends InfoReader<String[]> {
    private static final String TAG = "CardDateInfoReader";

    public CardDateInfoReader(IAPDUService omaService) {
        super(omaService);
    }

    /* access modifiers changed from: protected */
    public String[] handleResult(List<List<String>> results) throws AppletCardException {
        List<String> datas = results.get(0);
        checkData(TAG, datas);
        int sz = datas.size();
        return new String[]{datas.get(sz - 2), datas.get(sz - 1)};
    }
}
