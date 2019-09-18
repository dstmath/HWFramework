package com.android.internal.telephony;

import android.net.NetworkRequest;
import android.os.RegistrantList;

public interface IHwPhoneSwitcherInner {
    RegistrantList getActivePhoneRegistrants(int i);

    int phoneIdForRequestForEx(NetworkRequest networkRequest, int i);

    void resendDataAllowedForEx(int i);
}
