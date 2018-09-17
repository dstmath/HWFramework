package sun.security.ssl;

import java.io.IOException;
import javax.net.ssl.SSLProtocolException;

/* compiled from: HelloExtensions */
final class RenegotiationInfoExtension extends HelloExtension {
    private final byte[] renegotiated_connection;

    RenegotiationInfoExtension(byte[] clientVerifyData, byte[] serverVerifyData) {
        super(ExtensionType.EXT_RENEGOTIATION_INFO);
        if (clientVerifyData.length != 0) {
            this.renegotiated_connection = new byte[(clientVerifyData.length + serverVerifyData.length)];
            System.arraycopy(clientVerifyData, 0, this.renegotiated_connection, 0, clientVerifyData.length);
            if (serverVerifyData.length != 0) {
                System.arraycopy(serverVerifyData, 0, this.renegotiated_connection, clientVerifyData.length, serverVerifyData.length);
                return;
            }
            return;
        }
        this.renegotiated_connection = new byte[0];
    }

    RenegotiationInfoExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_RENEGOTIATION_INFO);
        if (len < 1) {
            throw new SSLProtocolException("Invalid " + this.type + " extension");
        }
        int renegoInfoDataLen = s.getInt8();
        if (renegoInfoDataLen + 1 != len) {
            throw new SSLProtocolException("Invalid " + this.type + " extension");
        }
        this.renegotiated_connection = new byte[renegoInfoDataLen];
        if (renegoInfoDataLen != 0) {
            s.read(this.renegotiated_connection, 0, renegoInfoDataLen);
        }
    }

    int length() {
        return this.renegotiated_connection.length + 5;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(this.type.id);
        s.putInt16(this.renegotiated_connection.length + 1);
        s.putBytes8(this.renegotiated_connection);
    }

    boolean isEmpty() {
        return this.renegotiated_connection.length == 0;
    }

    byte[] getRenegotiatedConnection() {
        return this.renegotiated_connection;
    }

    public String toString() {
        String str;
        StringBuilder append = new StringBuilder().append("Extension ").append(this.type).append(", renegotiated_connection: ");
        if (this.renegotiated_connection.length == 0) {
            str = "<empty>";
        } else {
            str = Debug.toString(this.renegotiated_connection);
        }
        return append.append(str).toString();
    }
}
