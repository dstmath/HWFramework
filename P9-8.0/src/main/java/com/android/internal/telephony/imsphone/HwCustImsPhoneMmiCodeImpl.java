package com.android.internal.telephony.imsphone;

import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;

public class HwCustImsPhoneMmiCodeImpl extends HwCustImsPhoneMmiCode {
    private static final boolean IS_SUPPORT_DUAL_NUMBER = SystemProperties.getBoolean("ro.config.hw_dual_number", false);

    public boolean isVirtualNum(String number) {
        if (!IS_SUPPORT_DUAL_NUMBER || !number.endsWith("#")) {
            return false;
        }
        String tempstring = number.substring(0, number.length() - 1).replace(" ", "").replace("+", "").replace("-", "");
        if (tempstring.startsWith("*230#")) {
            tempstring = tempstring.substring(5, tempstring.length());
        } else if (tempstring.startsWith("*23#")) {
            tempstring = tempstring.substring(4, tempstring.length());
        }
        if (tempstring.matches("[0-9]+")) {
            return true;
        }
        return false;
    }

    public boolean ignoreSpecialDialString(ImsPhone phone, String dialString) {
        if (dialString == null || phone == null) {
            return false;
        }
        String data = System.getString(phone.getContext().getContentResolver(), "hw_custom_dialstring_prefix");
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        for (String prefix : data.split(";")) {
            if (dialString.startsWith(prefix) && dialString.substring(prefix.length()).matches("[0-9]+")) {
                return true;
            }
        }
        return false;
    }
}
