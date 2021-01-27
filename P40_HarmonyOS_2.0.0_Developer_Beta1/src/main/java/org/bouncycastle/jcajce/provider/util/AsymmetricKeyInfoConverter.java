package org.bouncycastle.jcajce.provider.util;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

public interface AsymmetricKeyInfoConverter {
    PrivateKey generatePrivate(PrivateKeyInfo privateKeyInfo) throws IOException;

    PublicKey generatePublic(SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException;
}
