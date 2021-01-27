package org.bouncycastle.cert.cmp;

import java.math.BigInteger;
import org.bouncycastle.asn1.cmp.RevDetails;
import org.bouncycastle.asn1.x500.X500Name;

public class RevocationDetails {
    private RevDetails revDetails;

    public RevocationDetails(RevDetails revDetails2) {
        this.revDetails = revDetails2;
    }

    public X500Name getIssuer() {
        return this.revDetails.getCertDetails().getIssuer();
    }

    public BigInteger getSerialNumber() {
        return this.revDetails.getCertDetails().getSerialNumber().getValue();
    }

    public X500Name getSubject() {
        return this.revDetails.getCertDetails().getSubject();
    }

    public RevDetails toASN1Structure() {
        return this.revDetails;
    }
}
