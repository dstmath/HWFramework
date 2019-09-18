package org.bouncycastle.openssl;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

public class PEMKeyPair {
    private final PrivateKeyInfo privateKeyInfo;
    private final SubjectPublicKeyInfo publicKeyInfo;

    public PEMKeyPair(SubjectPublicKeyInfo subjectPublicKeyInfo, PrivateKeyInfo privateKeyInfo2) {
        this.publicKeyInfo = subjectPublicKeyInfo;
        this.privateKeyInfo = privateKeyInfo2;
    }

    public PrivateKeyInfo getPrivateKeyInfo() {
        return this.privateKeyInfo;
    }

    public SubjectPublicKeyInfo getPublicKeyInfo() {
        return this.publicKeyInfo;
    }
}
