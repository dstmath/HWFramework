package android.telephony;

public class SmsMessageUtils {
    public static boolean useCdmaFormatForMoSms(int subscription) {
        return SmsMessage.useCdmaFormatForMoSmsHw(subscription);
    }

    public static boolean isCdmaVoice(int subscription) {
        return SmsMessage.isCdmaVoiceHw(subscription);
    }
}
