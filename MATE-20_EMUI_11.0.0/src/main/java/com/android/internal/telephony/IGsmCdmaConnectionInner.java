package com.android.internal.telephony;

public interface IGsmCdmaConnectionInner {
    String getAddress();

    IHwGsmCdmaConnectionEx getHwGsmCdmaConnectionEx();

    String getOrigDialString();

    boolean isIncoming();
}
