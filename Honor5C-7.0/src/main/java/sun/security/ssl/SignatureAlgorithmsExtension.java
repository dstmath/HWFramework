package sun.security.ssl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.net.ssl.SSLProtocolException;

/* compiled from: HelloExtensions */
final class SignatureAlgorithmsExtension extends HelloExtension {
    private Collection<SignatureAndHashAlgorithm> algorithms;
    private int algorithmsLen;

    SignatureAlgorithmsExtension(Collection<SignatureAndHashAlgorithm> signAlgs) {
        super(ExtensionType.EXT_SIGNATURE_ALGORITHMS);
        this.algorithms = new ArrayList((Collection) signAlgs);
        this.algorithmsLen = SignatureAndHashAlgorithm.sizeInRecord() * this.algorithms.size();
    }

    SignatureAlgorithmsExtension(HandshakeInStream s, int len) throws IOException {
        super(ExtensionType.EXT_SIGNATURE_ALGORITHMS);
        this.algorithmsLen = s.getInt16();
        if (this.algorithmsLen == 0 || this.algorithmsLen + 2 != len) {
            throw new SSLProtocolException("Invalid " + this.type + " extension");
        }
        this.algorithms = new ArrayList();
        int remains = this.algorithmsLen;
        int sequence = 0;
        while (remains > 1) {
            sequence++;
            this.algorithms.add(SignatureAndHashAlgorithm.valueOf(s.getInt8(), s.getInt8(), sequence));
            remains -= 2;
        }
        if (remains != 0) {
            throw new SSLProtocolException("Invalid server_name extension");
        }
    }

    Collection<SignatureAndHashAlgorithm> getSignAlgorithms() {
        return this.algorithms;
    }

    int length() {
        return this.algorithmsLen + 6;
    }

    void send(HandshakeOutStream s) throws IOException {
        s.putInt16(this.type.id);
        s.putInt16(this.algorithmsLen + 2);
        s.putInt16(this.algorithmsLen);
        for (SignatureAndHashAlgorithm algorithm : this.algorithms) {
            s.putInt8(algorithm.getHashValue());
            s.putInt8(algorithm.getSignatureValue());
        }
    }

    public String toString() {
        Object buffer = new StringBuffer();
        boolean opened = false;
        for (SignatureAndHashAlgorithm signAlg : this.algorithms) {
            if (opened) {
                buffer.append(", " + signAlg.getAlgorithmName());
            } else {
                buffer.append(signAlg.getAlgorithmName());
                opened = true;
            }
        }
        return "Extension " + this.type + ", signature_algorithms: " + buffer;
    }
}
