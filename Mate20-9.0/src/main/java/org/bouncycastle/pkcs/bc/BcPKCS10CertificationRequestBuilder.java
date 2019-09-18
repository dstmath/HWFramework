package org.bouncycastle.pkcs.bc;

import java.io.IOException;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.SubjectPublicKeyInfoFactory;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;

public class BcPKCS10CertificationRequestBuilder extends PKCS10CertificationRequestBuilder {
    public BcPKCS10CertificationRequestBuilder(X500Name x500Name, AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        super(x500Name, SubjectPublicKeyInfoFactory.createSubjectPublicKeyInfo(asymmetricKeyParameter));
    }
}
