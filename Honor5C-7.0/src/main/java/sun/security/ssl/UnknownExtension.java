package sun.security.ssl;

import java.io.IOException;

/* compiled from: HelloExtensions */
final class UnknownExtension extends HelloExtension {
    private final byte[] data;

    UnknownExtension(HandshakeInStream s, int len, ExtensionType type) throws IOException {
        super(type);
        this.data = new byte[len];
        if (len != 0) {
            s.read(this.data);
        }
    }

    int length() {
        return this.data.length + 4;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(this.type.id);
        s.putBytes16(this.data);
    }

    public String toString() {
        return "Unsupported extension " + this.type + ", data: " + Debug.toString(this.data);
    }
}
