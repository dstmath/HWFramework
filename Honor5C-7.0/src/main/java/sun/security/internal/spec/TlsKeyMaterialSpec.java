package sun.security.internal.spec;

import java.security.spec.KeySpec;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

@Deprecated
public class TlsKeyMaterialSpec implements KeySpec, SecretKey {
    static final long serialVersionUID = 812912859129525028L;
    private final SecretKey clientCipherKey;
    private final IvParameterSpec clientIv;
    private final SecretKey clientMacKey;
    private final SecretKey serverCipherKey;
    private final IvParameterSpec serverIv;
    private final SecretKey serverMacKey;

    public TlsKeyMaterialSpec(SecretKey clientMacKey, SecretKey serverMacKey) {
        this(clientMacKey, serverMacKey, null, null, null, null);
    }

    public TlsKeyMaterialSpec(SecretKey clientMacKey, SecretKey serverMacKey, SecretKey clientCipherKey, SecretKey serverCipherKey) {
        this(clientMacKey, serverMacKey, clientCipherKey, null, serverCipherKey, null);
    }

    public TlsKeyMaterialSpec(SecretKey clientMacKey, SecretKey serverMacKey, SecretKey clientCipherKey, IvParameterSpec clientIv, SecretKey serverCipherKey, IvParameterSpec serverIv) {
        if (clientMacKey == null || serverMacKey == null) {
            throw new NullPointerException("MAC keys must not be null");
        }
        this.clientMacKey = clientMacKey;
        this.serverMacKey = serverMacKey;
        this.clientCipherKey = clientCipherKey;
        this.serverCipherKey = serverCipherKey;
        this.clientIv = clientIv;
        this.serverIv = serverIv;
    }

    public String getAlgorithm() {
        return "TlsKeyMaterial";
    }

    public String getFormat() {
        return null;
    }

    public byte[] getEncoded() {
        return null;
    }

    public SecretKey getClientMacKey() {
        return this.clientMacKey;
    }

    public SecretKey getServerMacKey() {
        return this.serverMacKey;
    }

    public SecretKey getClientCipherKey() {
        return this.clientCipherKey;
    }

    public IvParameterSpec getClientIv() {
        return this.clientIv;
    }

    public SecretKey getServerCipherKey() {
        return this.serverCipherKey;
    }

    public IvParameterSpec getServerIv() {
        return this.serverIv;
    }
}
