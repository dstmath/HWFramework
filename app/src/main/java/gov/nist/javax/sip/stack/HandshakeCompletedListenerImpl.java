package gov.nist.javax.sip.stack;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;

public class HandshakeCompletedListenerImpl implements HandshakeCompletedListener {
    private HandshakeCompletedEvent handshakeCompletedEvent;
    private TLSMessageChannel tlsMessageChannel;

    public HandshakeCompletedListenerImpl(TLSMessageChannel tlsMessageChannel) {
        this.tlsMessageChannel = tlsMessageChannel;
        tlsMessageChannel.setHandshakeCompletedListener(this);
    }

    public void handshakeCompleted(HandshakeCompletedEvent handshakeCompletedEvent) {
        this.handshakeCompletedEvent = handshakeCompletedEvent;
    }

    public HandshakeCompletedEvent getHandshakeCompletedEvent() {
        return this.handshakeCompletedEvent;
    }
}
