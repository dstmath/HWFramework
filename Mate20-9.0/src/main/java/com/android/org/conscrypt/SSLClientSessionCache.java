package com.android.org.conscrypt;

import javax.net.ssl.SSLSession;

public interface SSLClientSessionCache {
    byte[] getSessionData(String str, int i);

    void putSessionData(SSLSession sSLSession, byte[] bArr);
}
