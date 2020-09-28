package com.huawei.android.telephony;

import com.huawei.android.util.NoExtAPIException;

public final class IIccPhoneBookManagerChipsEx {
    private static IIccPhoneBookManagerChipsEx sInstance = new IIccPhoneBookManagerChipsEx();

    private IIccPhoneBookManagerChipsEx() {
    }

    public static IIccPhoneBookManagerChipsEx getDefault() {
        return sInstance;
    }

    public String getSecretCodeSubString(String input) {
        throw new NoExtAPIException("method not supported.");
    }
}
