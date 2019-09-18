package com.huawei.wallet.sdk.business.buscard.base.traffic.readers;

import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletCardResult;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import java.util.List;

public class CardStatusInfoReader extends InfoReader<Boolean> {
    private static final String TAG = "CardStatusInfoReader";

    public CardStatusInfoReader(IAPDUService omaService) {
        super(omaService);
    }

    /* access modifiers changed from: protected */
    public Boolean handleResult(List<List<String>> results) throws AppletCardException {
        for (List<String> datas : results) {
            checkData(TAG, datas);
            if (!Boolean.parseBoolean(datas.get(datas.size() - 1))) {
                throw new AppletCardException(AppletCardResult.RESULT_FAILED_TRAFFIC_CARD_INFO_STATUS_ABNORMAL, "card status is abnormal. data : " + datas.get(0));
            }
        }
        return true;
    }
}
