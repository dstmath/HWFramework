package sun.security.ssl;

import java.io.IOException;
import java.io.PrintStream;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;

final class ECDHClientKeyExchange extends HandshakeMessage {
    private byte[] encodedPoint;

    int messageType() {
        return 16;
    }

    byte[] getEncodedPoint() {
        return this.encodedPoint;
    }

    ECDHClientKeyExchange(PublicKey publicKey) {
        ECPublicKey ecKey = (ECPublicKey) publicKey;
        this.encodedPoint = JsseJce.encodePoint(ecKey.getW(), ecKey.getParams().getCurve());
    }

    ECDHClientKeyExchange(HandshakeInStream input) throws IOException {
        this.encodedPoint = input.getBytes8();
    }

    int messageLength() {
        return this.encodedPoint.length + 1;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putBytes8(this.encodedPoint);
    }

    void print(PrintStream s) throws IOException {
        s.println("*** ECDHClientKeyExchange");
        if (debug != null && Debug.isOn("verbose")) {
            Debug.println(s, "ECDH Public value", this.encodedPoint);
        }
    }
}
