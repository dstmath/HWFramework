package com.huawei.wallet.sdk.common.apdu;

import com.huawei.wallet.sdk.common.apdu.base.WalletProcessTraceBase;
import com.huawei.wallet.sdk.common.apdu.model.ApduCommand;
import com.huawei.wallet.sdk.common.apdu.model.ChannelID;
import java.util.List;

public interface IAPDUService extends WalletProcessTraceBase {
    public static final int CHANNEL_TYPE_BASIC = 1;
    public static final int CHANNEL_TYPE_LOGIC = 0;
    public static final Object CONTACTLESSC_LOCK = new Object();
    public static final int MEDIA_TYPE_ESE = 0;
    public static final int MEDIA_TYPE_INSE = 3;
    public static final int MEDIA_TYPE_SD = 2;
    public static final int MEDIA_TYPE_SIM = 1;
    public static final Object OMA_ACCESS_SYNC_LOCK = new Object();
    public static final int RESULT_SUCCESS = 0;
    public static final int RETURN_APDU_EXCUTE_CLOSE_CHANNEL_EXCEPTION = 5001;
    public static final int RETURN_APDU_EXCUTE_CLOSE_SESERVICE_EXCEPTION = 5002;
    public static final int RETURN_APDU_EXCUTE_CONTACTLESS_BEGIN_IO_EXCEPTION = 6002;
    public static final int RETURN_APDU_EXCUTE_CONTACTLESS_IO_EXCEPTION = 6003;
    public static final int RETURN_APDU_EXCUTE_NOT_EXPECTED_SW = 4002;
    public static final int RETURN_APDU_EXCUTE_NO_READER = 1002;
    public static final int RETURN_APDU_EXCUTE_NO_TAG = 6001;
    public static final int RETURN_APDU_EXCUTE_OPENCHANNEL_EXCEPTION = 2008;
    public static final int RETURN_APDU_EXCUTE_OPENCHANNEL_IOEXCEPTION = 2002;
    public static final int RETURN_APDU_EXCUTE_OPENCHANNEL_MISSRESOURCEEXCEPTION = 2007;
    public static final int RETURN_APDU_EXCUTE_OPENCHANNEL_NOAID = 2005;
    public static final int RETURN_APDU_EXCUTE_OPENCHANNEL_NOSUCHELEMENT = 2004;
    public static final int RETURN_APDU_EXCUTE_OPENCHANNEL_NULLPOINTEREXCEPTION = 2006;
    public static final int RETURN_APDU_EXCUTE_OPENCHANNEL_SECURITYEXCEPTION = 2003;
    public static final int RETURN_APDU_EXCUTE_OPENSESSION_FAILED = 2001;
    public static final int RETURN_APDU_EXCUTE_PARAMS_ILLEGAL = 1001;
    public static final int RETURN_APDU_EXCUTE_RAPDU_IS_SMALL = 4001;
    public static final int RETURN_APDU_EXCUTE_SE_READER_NOT_PRESENT = 1003;
    public static final int RETURN_APDU_EXCUTE_TRANSMIT_CHANNEL_IS_NULL = 3001;
    public static final int RETURN_APDU_EXCUTE_TRANSMIT_EXCEPTION = 3007;
    public static final int RETURN_APDU_EXCUTE_TRANSMIT_IOEXCEPTION = 3002;
    public static final int RETURN_APDU_EXCUTE_TRANSMIT_MISSRESOURCEEXCEPTION = 3006;
    public static final int RETURN_APDU_EXCUTE_TRANSMIT_NOSUCHELEMENT = 3004;
    public static final int RETURN_APDU_EXCUTE_TRANSMIT_NULLPOINTEREXCEPTION = 3005;
    public static final int RETURN_APDU_EXCUTE_TRANSMIT_SECURITYEXCEPTION = 3003;
    public static final int RETURN_APDU_EXCUTE_UNKNOWN_ERROR = 9999;
    public static final int RETURN_SMARTCARD_NO_CAPDULIST = 1004;
    public static final String TAG = "OmaService";

    TaskResult<Integer> closeAllChannel();

    TaskResult<Integer> closeChannel(ChannelID channelID);

    TaskResult<Integer> closeSEService();

    TaskResult<ChannelID> excuteApduList(List<ApduCommand> list, ChannelID channelID);

    TaskResult<Integer> getReaderId(int i);
}
