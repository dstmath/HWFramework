package com.android.internal.telephony;

import android.os.SystemProperties;
import java.util.HashMap;
import java.util.Map;

public class HwCustRILReferenceImpl extends HwCustRILReference {
    private static final boolean CUST_APN_AUTH_ON = SystemProperties.getBoolean("ro.config.hw_allow_pdp_auth", false);

    public boolean isCustCorrectApnAuthOn() {
        return CUST_APN_AUTH_ON;
    }

    public Map<String, String> custCorrectApnAuth(String userName, int authType, String password) {
        Map<String, String> map = new HashMap();
        String str = "userName";
        if (userName == null) {
            userName = "";
        }
        map.put(str, userName);
        str = "password";
        if (password == null) {
            password = "";
        }
        map.put(str, password);
        map.put("authType", String.valueOf(authType));
        return map;
    }
}
