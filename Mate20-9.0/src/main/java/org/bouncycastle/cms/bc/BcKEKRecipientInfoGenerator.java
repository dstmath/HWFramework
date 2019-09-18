package org.bouncycastle.cms.bc;

import org.bouncycastle.asn1.cms.KEKIdentifier;
import org.bouncycastle.cms.KEKRecipientInfoGenerator;
import org.bouncycastle.operator.bc.BcSymmetricKeyWrapper;

public class BcKEKRecipientInfoGenerator extends KEKRecipientInfoGenerator {
    public BcKEKRecipientInfoGenerator(KEKIdentifier kEKIdentifier, BcSymmetricKeyWrapper bcSymmetricKeyWrapper) {
        super(kEKIdentifier, bcSymmetricKeyWrapper);
    }

    public BcKEKRecipientInfoGenerator(byte[] bArr, BcSymmetricKeyWrapper bcSymmetricKeyWrapper) {
        this(new KEKIdentifier(bArr, null, null), bcSymmetricKeyWrapper);
    }
}
