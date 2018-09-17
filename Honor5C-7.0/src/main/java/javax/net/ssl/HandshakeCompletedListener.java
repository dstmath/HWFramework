package javax.net.ssl;

import java.util.EventListener;

public interface HandshakeCompletedListener extends EventListener {
    void handshakeCompleted(HandshakeCompletedEvent handshakeCompletedEvent);
}
