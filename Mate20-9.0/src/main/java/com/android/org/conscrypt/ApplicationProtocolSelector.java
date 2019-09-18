package com.android.org.conscrypt;

import java.util.List;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;

public abstract class ApplicationProtocolSelector {
    public abstract String selectApplicationProtocol(SSLEngine sSLEngine, List<String> list);

    public abstract String selectApplicationProtocol(SSLSocket sSLSocket, List<String> list);
}
