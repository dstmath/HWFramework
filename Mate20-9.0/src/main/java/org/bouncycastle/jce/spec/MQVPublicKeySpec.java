package org.bouncycastle.jce.spec;

import java.security.PublicKey;
import java.security.spec.KeySpec;
import org.bouncycastle.jce.interfaces.MQVPublicKey;

public class MQVPublicKeySpec implements KeySpec, MQVPublicKey {
    private PublicKey ephemeralKey;
    private PublicKey staticKey;

    public MQVPublicKeySpec(PublicKey publicKey, PublicKey publicKey2) {
        this.staticKey = publicKey;
        this.ephemeralKey = publicKey2;
    }

    public String getAlgorithm() {
        return "ECMQV";
    }

    public byte[] getEncoded() {
        return null;
    }

    public PublicKey getEphemeralKey() {
        return this.ephemeralKey;
    }

    public String getFormat() {
        return null;
    }

    public PublicKey getStaticKey() {
        return this.staticKey;
    }
}
