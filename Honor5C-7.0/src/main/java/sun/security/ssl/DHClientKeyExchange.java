package sun.security.ssl;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import javax.net.ssl.SSLHandshakeException;

final class DHClientKeyExchange extends HandshakeMessage {
    private byte[] dh_Yc;

    int messageType() {
        return 16;
    }

    BigInteger getClientPublicKey() {
        return this.dh_Yc == null ? null : new BigInteger(1, this.dh_Yc);
    }

    DHClientKeyExchange(BigInteger publicKey) {
        this.dh_Yc = HandshakeMessage.toByteArray(publicKey);
    }

    DHClientKeyExchange() {
        this.dh_Yc = null;
    }

    DHClientKeyExchange(HandshakeInStream input) throws IOException {
        if (input.available() >= 2) {
            this.dh_Yc = input.getBytes16();
            return;
        }
        throw new SSLHandshakeException("Unsupported implicit client DiffieHellman public key");
    }

    int messageLength() {
        if (this.dh_Yc == null) {
            return 0;
        }
        return this.dh_Yc.length + 2;
    }

    void send(HandshakeOutStream s) throws IOException {
        if (this.dh_Yc != null && this.dh_Yc.length != 0) {
            s.putBytes16(this.dh_Yc);
        }
    }

    void print(PrintStream s) throws IOException {
        s.println("*** ClientKeyExchange, DH");
        if (debug != null && Debug.isOn("verbose")) {
            Debug.println(s, "DH Public key", this.dh_Yc);
        }
    }
}
