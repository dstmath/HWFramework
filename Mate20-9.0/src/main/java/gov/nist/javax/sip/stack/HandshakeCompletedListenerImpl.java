package gov.nist.javax.sip.stack;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;

public class HandshakeCompletedListenerImpl implements HandshakeCompletedListener {
    private HandshakeCompletedEvent handshakeCompletedEvent;
    private TLSMessageChannel tlsMessageChannel;

    public HandshakeCompletedListenerImpl(TLSMessageChannel tlsMessageChannel2) {
        this.tlsMessageChannel = tlsMessageChannel2;
        tlsMessageChannel2.setHandshakeCompletedListener(this);
    }

    public void handshakeCompleted(HandshakeCompletedEvent handshakeCompletedEvent2) {
        this.handshakeCompletedEvent = handshakeCompletedEvent2;
    }

    public HandshakeCompletedEvent getHandshakeCompletedEvent() {
        return this.handshakeCompletedEvent;
    }
}
