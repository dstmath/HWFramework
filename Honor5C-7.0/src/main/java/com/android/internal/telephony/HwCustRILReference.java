package com.android.internal.telephony;

import java.util.HashMap;
import java.util.Map;

public class HwCustRILReference {
    public boolean isCustCorrectApnAuthOn() {
        return false;
    }

    public Map<String, String> custCorrectApnAuth(String userName, int authType, String password) {
        return new HashMap();
    }
}
