package com.android.internal.telephony.cat;

import android.os.Handler;
import com.android.internal.telephony.HwTelephonyFactory;

public abstract class AbstractCatService extends Handler implements AppInterface {
    public static final int DELAY_SEND_TIME = 25000;
    static final int MSG_ID_NO_OPEN_CHANNEL_RECEIVED = 12;
    static final int MSG_ID_OTA_SET_RESULT_NOTIFY = 10;
    public static final int MSG_ID_START_OTA = 11;
    public static final int OTA_TYPE_CHANGE_IMSI = 1;
    public static final int OTA_TYPE_OPEN_SERVICE = 0;
    public static final int OTA_TYPE_UPDATE_COUNTRY_INFO = 2;
    public int OTA_TYPE = 255;
    public int mOtaCmdType = 255;
    CatServiceReference mReference = HwTelephonyFactory.getHwUiccManager().createHwCatServiceReference();

    public interface CatServiceReference {
        String getLanguageNotificationCode();

        void setLanguageNotificationCode(String str);
    }

    public void onCmdResponse(CatResponseMessage resMsg) {
    }

    public String getLanguageNotificationCode() {
        return this.mReference.getLanguageNotificationCode();
    }

    public void setLanguageNotificationCode(String strLanguageNotificationCode) {
        this.mReference.setLanguageNotificationCode(strLanguageNotificationCode);
    }
}
