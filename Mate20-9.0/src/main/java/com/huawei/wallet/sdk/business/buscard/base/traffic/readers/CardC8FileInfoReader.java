package com.huawei.wallet.sdk.business.buscard.base.traffic.readers;

import com.huawei.wallet.sdk.business.buscard.base.appletcardinfo.AppletCardResult;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.business.idcard.walletbase.util.LogX;
import com.huawei.wallet.sdk.common.apdu.IAPDUService;
import java.util.List;

public class CardC8FileInfoReader extends InfoReader<Integer> {
    public static final Integer C8_FILE_STATUS_ABNORMAL = 2;
    public static final Integer C8_FILE_STATUS_NEW_FILE_ABNORMAL = 3;
    public static final Integer C8_FILE_STATUS_NORMAL = 1;
    public static final Integer C8_FILE_STATUS_TO_BE_READ = 0;
    private static final String TAG = "CardC8FileInfoReader";

    public CardC8FileInfoReader(IAPDUService omaService) {
        super(omaService);
    }

    /* access modifiers changed from: protected */
    public Integer handleResult(List<List<String>> results) throws AppletCardException {
        List<String> datas = results.get(0);
        if (datas == null || datas.size() < 3) {
            LogX.i("CardC8FileInfoReader handleResult, the data size error");
            throw new AppletCardException(AppletCardResult.RESULT_FAILED_INNER_EXCEPTION, " the data size error");
        }
        String c8FileStatus = datas.get(2);
        LogX.i("CardC8FileInfoReader handleResult, c8FileStatus = " + c8FileStatus);
        if ("TRUE".equals(c8FileStatus)) {
            return C8_FILE_STATUS_NORMAL;
        }
        return C8_FILE_STATUS_ABNORMAL;
    }
}
