package com.huawei.wallet.sdk.business.buscard.base.appletcardinfo;

import com.huawei.wallet.sdk.business.buscard.base.model.HciConfigInfo;
import com.huawei.wallet.sdk.business.buscard.base.util.AppletCardException;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import java.util.List;

public interface ConfigData {
    public static final int TYPE_BANK_CARD_APPLET_VERSION = 8;
    public static final int TYPE_BANK_CARD_LAST_TRANSACTION_INFO = 9;
    public static final int TYPE_BANK_CARD_NUM = 5;
    public static final int TYPE_CARD_AMOUNT = 3;
    public static final int TYPE_CARD_C8_FILE_STATUS = 6;
    public static final int TYPE_CARD_CONSUME_RECORDS = 11;
    public static final int TYPE_CARD_DATE = 2;
    public static final int TYPE_CARD_FM_INFO = 13;
    public static final int TYPE_CARD_IN_OUT_STATION_STATUS = 7;
    public static final int TYPE_CARD_LOGIC_NUM = 12;
    public static final int TYPE_CARD_NUM = 1;
    public static final int TYPE_CARD_RECORDS = 4;
    public static final int TYPE_CARD_RIDE_TIMES = 10;
    public static final int TYPE_CARD_STATUS = 0;

    List<ApduCommand> getApudList(String str, int i) throws AppletCardException;

    List<HciConfigInfo> getHciParseOperation(String str) throws AppletCardException;

    List<ApduCommand> getLocalApudList(String str, int i) throws AppletCardException;

    List<ApduCommand> getLocalApudStatus(String str) throws AppletCardException;

    boolean isSameApduNumAndDate(String str) throws AppletCardException;
}
