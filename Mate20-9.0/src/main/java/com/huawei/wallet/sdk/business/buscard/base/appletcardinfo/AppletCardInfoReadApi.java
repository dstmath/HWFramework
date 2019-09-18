package com.huawei.wallet.sdk.business.buscard.base.appletcardinfo;

import android.nfc.tech.IsoDep;
import com.huawei.wallet.sdk.business.buscard.base.model.CardInfo;
import com.huawei.wallet.sdk.business.buscard.base.model.TransactionRecord;
import java.util.List;

public interface AppletCardInfoReadApi {
    public static final int READ_CARD_BALANCE = 2;
    public static final int READ_CARD_C8_FILE_STATUS = 8;
    public static final int READ_CARD_IN_OUT_STATION_STATUS = 16;
    public static final int READ_CARD_LOGIC_NUM = 64;
    public static final int READ_CARD_NUM = 1;
    public static final int READ_CARD_RIDE_TIMES = 32;
    public static final int READ_CARD_VALIDITY_DATE = 4;
    public static final int READ_FM_CARD_INFO = 128;

    AppletCardResult<String> readAppletVersion(String str);

    AppletCardResult<String> readBankCardNum(String str);

    AppletCardResult<String> readFMCardInfo(String str, String str2);

    AppletCardResult<String> readLastTransactionInfo(String str);

    AppletCardResult<CardInfo> readTrafficCardInfo(String str, String str2, int i);

    AppletCardResult<CardInfo> readTrafficCardInfoContactless(String str, int i, IsoDep isoDep);

    AppletCardResult<List<TransactionRecord>> readTrafficCardTransactionRecord(String str, String str2);
}
