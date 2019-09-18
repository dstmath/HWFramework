package com.android.org.conscrypt;

import javax.net.ssl.SSLSession;

interface SSLServerSessionCache {
    byte[] getSessionData(byte[] bArr);

    void putSessionData(SSLSession sSLSession, byte[] bArr);
}
