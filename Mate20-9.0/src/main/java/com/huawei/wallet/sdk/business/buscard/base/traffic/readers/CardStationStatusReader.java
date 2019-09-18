package com.huawei.wallet.sdk.business.buscard.base.traffic.readers;

import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletCardResult;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import java.util.List;

public class CardStationStatusReader extends InfoReader<Integer> {
    public static final Integer STATION_STATUS_IN = 1;
    public static final Integer STATION_STATUS_OUT = 2;
    private static final String TAG = "CardStationStatusReader";

    public CardStationStatusReader(IAPDUService omaService) {
        super(omaService);
    }

    /* access modifiers changed from: protected */
    public Integer handleResult(List<List<String>> results) throws AppletCardException {
        List<String> datas = results.get(0);
        if (datas == null || datas.size() < 4) {
            LogX.i("CardStationStatusReader handleResult, the data size error");
            throw new AppletCardException(AppletCardResult.RESULT_FAILED_INNER_EXCEPTION, " the data size error");
        }
        String inStationStr = datas.get(datas.size() - 2);
        String outStationStr = datas.get(datas.size() - 1);
        LogX.i("CardStationStatusReader handleResult, inStationStr = " + inStationStr + ", outStationStr = " + outStationStr);
        StringBuilder sb = new StringBuilder();
        sb.append("CardStationStatusReader handleResult, rapdu = ");
        sb.append(datas.get(0));
        LogX.i(sb.toString());
        if ("TRUE".equals(inStationStr) && "FALSE".equals(outStationStr)) {
            return STATION_STATUS_IN;
        }
        if ("TRUE".equals(outStationStr) && "FALSE".equals(inStationStr)) {
            return STATION_STATUS_OUT;
        }
        LogX.i("CardStationStatusReader handleResult, the data error");
        return null;
    }
}
