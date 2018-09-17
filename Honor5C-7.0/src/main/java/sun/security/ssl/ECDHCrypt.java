package sun.security.ssl;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPublicKeySpec;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;

final class ECDHCrypt {
    private PrivateKey privateKey;
    private ECPublicKey publicKey;

    ECDHCrypt(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = (ECPublicKey) publicKey;
    }

    ECDHCrypt(String curveName, SecureRandom random) {
        try {
            KeyPairGenerator kpg = JsseJce.getKeyPairGenerator("EC");
            kpg.initialize(new ECGenParameterSpec(curveName), random);
            KeyPair kp = kpg.generateKeyPair();
            this.privateKey = kp.getPrivate();
            this.publicKey = (ECPublicKey) kp.getPublic();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate DH keypair", e);
        }
    }

    ECDHCrypt(ECParameterSpec params, SecureRandom random) {
        try {
            KeyPairGenerator kpg = JsseJce.getKeyPairGenerator("EC");
            kpg.initialize((AlgorithmParameterSpec) params, random);
            KeyPair kp = kpg.generateKeyPair();
            this.privateKey = kp.getPrivate();
            this.publicKey = (ECPublicKey) kp.getPublic();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate DH keypair", e);
        }
    }

    PublicKey getPublicKey() {
        return this.publicKey;
    }

    SecretKey getAgreedSecret(PublicKey peerPublicKey) {
        try {
            KeyAgreement ka = JsseJce.getKeyAgreement("ECDH");
            ka.init(this.privateKey);
            ka.doPhase(peerPublicKey, true);
            return ka.generateSecret("TlsPremasterSecret");
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate secret", e);
        }
    }

    SecretKey getAgreedSecret(byte[] encodedPoint) {
        try {
            ECParameterSpec params = this.publicKey.getParams();
            return getAgreedSecret(JsseJce.getKeyFactory("EC").generatePublic(new ECPublicKeySpec(JsseJce.decodePoint(encodedPoint, params.getCurve()), params)));
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Could not generate secret", e);
        } catch (IOException e2) {
            throw new RuntimeException("Could not generate secret", e2);
        }
    }
}
