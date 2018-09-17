package android.telephony;

import com.android.internal.telephony.HwTelephonyFactory;
import java.util.ArrayList;

public class AbstractSmsMessage {
    static final String LOG_TAG = "AbstractSmsMessage";
    protected static final int SUBSCRIPTION_FOR_NONE_VARIABLE_METHODS = -1;

    public static boolean useCdmaFormatForMoSms() {
        return HwTelephonyFactory.getHwInnerSmsManager().useCdmaFormatForMoSms();
    }

    public static boolean handleMSimBySubscrition(int subscription) {
        return -1 != subscription;
    }

    public static boolean isCdmaVoiceBySubscrition(int subscription) {
        return subscription == 0;
    }

    public static ArrayList<String> fragmentText(String text) {
        return HwTelephonyFactory.getHwInnerSmsManager().fragmentText(text);
    }

    public static boolean isCdmaVoice() {
        return HwTelephonyFactory.getHwInnerSmsManager().isCdmaVoice();
    }
}
