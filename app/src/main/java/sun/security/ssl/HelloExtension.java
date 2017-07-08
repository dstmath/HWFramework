package sun.security.ssl;

import java.io.IOException;

/* compiled from: HelloExtensions */
abstract class HelloExtension {
    final ExtensionType type;

    abstract int length();

    abstract void send(HandshakeOutStream handshakeOutStream) throws IOException;

    public abstract String toString();

    HelloExtension(ExtensionType type) {
        this.type = type;
    }
}
