package org.bouncycastle.pqc.jcajce.provider.newhope;

import java.io.IOException;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.crypto.newhope.NHPublicKeyParameters;
import org.bouncycastle.pqc.jcajce.interfaces.NHPublicKey;
import org.bouncycastle.util.Arrays;

public class BCNHPublicKey implements NHPublicKey {
    private static final long serialVersionUID = 1;
    private final NHPublicKeyParameters params;

    public BCNHPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this.params = new NHPublicKeyParameters(subjectPublicKeyInfo.getPublicKeyData().getBytes());
    }

    public BCNHPublicKey(NHPublicKeyParameters nHPublicKeyParameters) {
        this.params = nHPublicKeyParameters;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof BCNHPublicKey)) {
            return false;
        }
        return Arrays.areEqual(this.params.getPubData(), ((BCNHPublicKey) obj).params.getPubData());
    }

    public final String getAlgorithm() {
        return "NH";
    }

    public byte[] getEncoded() {
        try {
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PQCObjectIdentifiers.newHope), this.params.getPubData()).getEncoded();
        } catch (IOException e) {
            return null;
        }
    }

    public String getFormat() {
        return "X.509";
    }

    /* access modifiers changed from: package-private */
    public CipherParameters getKeyParams() {
        return this.params;
    }

    public byte[] getPublicData() {
        return this.params.getPubData();
    }

    public int hashCode() {
        return Arrays.hashCode(this.params.getPubData());
    }
}
