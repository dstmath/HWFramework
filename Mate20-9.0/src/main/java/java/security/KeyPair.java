package java.security;

import java.io.Serializable;

public final class KeyPair implements Serializable {
    private static final long serialVersionUID = -7565189502268009837L;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public KeyPair(PublicKey publicKey2, PrivateKey privateKey2) {
        this.publicKey = publicKey2;
        this.privateKey = privateKey2;
    }

    public PublicKey getPublic() {
        return this.publicKey;
    }

    public PrivateKey getPrivate() {
        return this.privateKey;
    }
}
