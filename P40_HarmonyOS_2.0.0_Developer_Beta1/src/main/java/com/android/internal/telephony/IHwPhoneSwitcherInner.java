package com.android.internal.telephony;

import android.content.Context;
import android.net.NetworkRequest;

public interface IHwPhoneSwitcherInner {
    Context getPhoneContext();

    void onDualPsStateChanged(boolean z, String str);

    int phoneIdForRequestForEx(NetworkRequest networkRequest, int i);

    void resendDataAllowedForEx(int i);
}
