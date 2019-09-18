package com.android.org.conscrypt;

import java.util.Collections;
import java.util.List;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;

class Java8ExtendedSSLSession extends Java7ExtendedSSLSession {
    public Java8ExtendedSSLSession(ConscryptSession delegate) {
        super(delegate);
    }

    public final List<SNIServerName> getRequestedServerNames() {
        String requestedServerName = getDelegate().getRequestedServerName();
        if (requestedServerName == null) {
            return null;
        }
        return Collections.singletonList(new SNIHostName(requestedServerName));
    }
}
