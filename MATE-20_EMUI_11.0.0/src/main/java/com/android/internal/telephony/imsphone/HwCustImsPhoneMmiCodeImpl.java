package com.android.internal.telephony.imsphone;

import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.text.TextUtils;

public class HwCustImsPhoneMmiCodeImpl extends HwCustImsPhoneMmiCode {
    private static final boolean IS_SUPPORT_DUAL_NUMBER = SystemProperties.getBoolean("ro.config.hw_dual_number", false);
    private static final String TAG = "HwCustImsPhoneMmiCodeImpl";
    private static final String VIRTUAL_NUM_23 = "*23#";
    private static final String VIRTUAL_NUM_230 = "*230#";

    public boolean isVirtualNum(String number) {
        if (!IS_SUPPORT_DUAL_NUMBER || !number.endsWith("#")) {
            return false;
        }
        String tempstring = number.substring(0, number.length() - 1).replace(" ", "").replace("+", "").replace("-", "");
        if (tempstring.startsWith(VIRTUAL_NUM_230)) {
            tempstring = tempstring.substring(VIRTUAL_NUM_230.length(), tempstring.length());
        } else if (tempstring.startsWith(VIRTUAL_NUM_23)) {
            tempstring = tempstring.substring(VIRTUAL_NUM_23.length(), tempstring.length());
        } else {
            Rlog.d(TAG, "isVirtualNum: do nothing");
        }
        if (!tempstring.matches("[0-9]+")) {
            return false;
        }
        return true;
    }

    public boolean ignoreSpecialDialString(ImsPhone phone, String dialString) {
        if (dialString == null || phone == null) {
            return false;
        }
        String data = Settings.System.getString(phone.getContext().getContentResolver(), "hw_custom_dialstring_prefix");
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        String[] prefixes = data.split(";");
        for (String prefix : prefixes) {
            if (dialString.startsWith(prefix) && dialString.substring(prefix.length()).matches("[0-9]+")) {
                return true;
            }
        }
        return false;
    }
}
