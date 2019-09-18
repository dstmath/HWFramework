package com.android.org.conscrypt;

import java.util.Arrays;
import java.util.List;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocket;

final class ApplicationProtocolSelectorAdapter {
    private static final int NO_PROTOCOL_SELECTED = -1;
    private final SSLEngine engine;
    private final ApplicationProtocolSelector selector;
    private final SSLSocket socket;

    ApplicationProtocolSelectorAdapter(SSLEngine engine2, ApplicationProtocolSelector selector2) {
        this.engine = (SSLEngine) Preconditions.checkNotNull(engine2, "engine");
        this.socket = null;
        this.selector = (ApplicationProtocolSelector) Preconditions.checkNotNull(selector2, "selector");
    }

    ApplicationProtocolSelectorAdapter(SSLSocket socket2, ApplicationProtocolSelector selector2) {
        this.engine = null;
        this.socket = (SSLSocket) Preconditions.checkNotNull(socket2, "socket");
        this.selector = (ApplicationProtocolSelector) Preconditions.checkNotNull(selector2, "selector");
    }

    /* access modifiers changed from: package-private */
    public int selectApplicationProtocol(byte[] encodedProtocols) {
        String selected;
        if (encodedProtocols == null || encodedProtocols.length == 0) {
            return NO_PROTOCOL_SELECTED;
        }
        List<String> protocols = Arrays.asList(SSLUtils.decodeProtocols(encodedProtocols));
        if (this.engine != null) {
            selected = this.selector.selectApplicationProtocol(this.engine, protocols);
        } else {
            selected = this.selector.selectApplicationProtocol(this.socket, protocols);
        }
        if (selected == null || selected.isEmpty()) {
            return NO_PROTOCOL_SELECTED;
        }
        int offset = 0;
        for (String protocol : protocols) {
            if (selected.equals(protocol)) {
                return offset;
            }
            offset += 1 + protocol.length();
        }
        return NO_PROTOCOL_SELECTED;
    }
}
