package com.android.internal.telephony.gsm;

import android.os.AsyncResult;
import android.os.SystemProperties;
import android.telephony.Rlog;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.GsmCdmaPhone;

public class HwCustGsmMmiCodeImpl extends HwCustGsmMmiCode {
    private static final boolean IS_DOCOMO = SystemProperties.get("ro.product.custom", "NULL").contains("docomo");
    private static final String LOG_TAG = "HwCustGsmMmiCodeImpl";
    private static final CharSequence SERVICE_NOT_SUBSCRIBED = "service_not_subscribed";

    public HwCustGsmMmiCodeImpl(GsmCdmaPhone phone) {
        super(phone);
    }

    public CharSequence getErrorMessageEx(AsyncResult ar, CharSequence result) {
        if (!(ar.exception instanceof CommandException)) {
            return result;
        }
        CommandException.Error err = ar.exception.getCommandError();
        if (!IS_DOCOMO || err != CommandException.Error.SERVICE_NOT_SUBSCRIBED) {
            return result;
        }
        Rlog.i(LOG_TAG, "SERVICE_NOT_SUBSCRIBED");
        return SERVICE_NOT_SUBSCRIBED;
    }
}
