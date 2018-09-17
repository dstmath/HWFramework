package tmsdk.common.module.aresengine;

public abstract class MmsTransactionHelper {
    public static final int APN_ALREADY_ACTIVE = 0;
    public static final int APN_REQUEST_STARTED = 1;
    public static final String DEFAULT_NETWORK_FEATURE = "enableMMS";

    @Deprecated
    public abstract int beginMmsConnectivity(String str);

    public abstract int beginMmsConnectivity(String str, int i);

    public abstract int retrieveMmsContent(SmsEntity smsEntity);

    public abstract int sendAcknowledgeInd(SmsEntity smsEntity);

    public abstract int sendNotifyRespInd(int i, SmsEntity smsEntity);

    public abstract void stop();
}
