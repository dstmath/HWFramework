package com.android.org.conscrypt;

import javax.net.ssl.SSLException;

public interface HandshakeListener {
    void onHandshakeFinished() throws SSLException;
}
