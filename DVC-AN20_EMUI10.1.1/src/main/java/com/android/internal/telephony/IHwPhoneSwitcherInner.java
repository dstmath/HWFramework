package com.android.internal.telephony;

import android.content.Context;
import android.net.NetworkRequest;
import android.os.RegistrantList;

public interface IHwPhoneSwitcherInner {
    RegistrantList getActivePhoneRegistrants(int i);

    Context getPhoneContext();

    void onDualPsStateChanged(boolean z, String str);

    int phoneIdForRequestForEx(NetworkRequest networkRequest, int i);

    void resendDataAllowedForEx(int i);
}
