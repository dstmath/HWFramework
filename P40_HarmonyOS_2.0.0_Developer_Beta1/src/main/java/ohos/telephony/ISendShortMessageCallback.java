package ohos.telephony;

public interface ISendShortMessageCallback {
    public static final int SEND_SMS_FAILURE_RADIO_OFF = 2;
    public static final int SEND_SMS_FAILURE_SERVICE_UNAVAILABLE = 3;
    public static final int SEND_SMS_FAILURE_UNKNOWN = 1;
    public static final int SEND_SMS_SUCCESS = 0;

    void sendShortMessageResult(int i, String str, boolean z);
}
