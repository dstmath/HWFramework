package org.bouncycastle.dvcs;

import org.bouncycastle.asn1.dvcs.TargetEtcChain;

public class TargetChain {
    private final TargetEtcChain certs;

    public TargetChain(TargetEtcChain targetEtcChain) {
        this.certs = targetEtcChain;
    }

    public TargetEtcChain toASN1Structure() {
        return this.certs;
    }
}
