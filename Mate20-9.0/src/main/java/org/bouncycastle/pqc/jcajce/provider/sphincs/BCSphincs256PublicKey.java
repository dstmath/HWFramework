package org.bouncycastle.pqc.jcajce.provider.sphincs;

import java.io.IOException;
import java.security.PublicKey;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.asn1.SPHINCS256KeyParams;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCSPublicKeyParameters;
import org.bouncycastle.pqc.jcajce.interfaces.SPHINCSKey;
import org.bouncycastle.util.Arrays;

public class BCSphincs256PublicKey implements PublicKey, SPHINCSKey {
    private static final long serialVersionUID = 1;
    private final SPHINCSPublicKeyParameters params;
    private final ASN1ObjectIdentifier treeDigest;

    public BCSphincs256PublicKey(ASN1ObjectIdentifier aSN1ObjectIdentifier, SPHINCSPublicKeyParameters sPHINCSPublicKeyParameters) {
        this.treeDigest = aSN1ObjectIdentifier;
        this.params = sPHINCSPublicKeyParameters;
    }

    public BCSphincs256PublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) {
        this.treeDigest = SPHINCS256KeyParams.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters()).getTreeDigest().getAlgorithm();
        this.params = new SPHINCSPublicKeyParameters(subjectPublicKeyInfo.getPublicKeyData().getBytes());
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BCSphincs256PublicKey)) {
            return false;
        }
        BCSphincs256PublicKey bCSphincs256PublicKey = (BCSphincs256PublicKey) obj;
        return this.treeDigest.equals(bCSphincs256PublicKey.treeDigest) && Arrays.areEqual(this.params.getKeyData(), bCSphincs256PublicKey.params.getKeyData());
    }

    public final String getAlgorithm() {
        return "SPHINCS-256";
    }

    public byte[] getEncoded() {
        try {
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PQCObjectIdentifiers.sphincs256, new SPHINCS256KeyParams(new AlgorithmIdentifier(this.treeDigest))), this.params.getKeyData()).getEncoded();
        } catch (IOException e) {
            return null;
        }
    }

    public String getFormat() {
        return "X.509";
    }

    public byte[] getKeyData() {
        return this.params.getKeyData();
    }

    /* access modifiers changed from: package-private */
    public CipherParameters getKeyParams() {
        return this.params;
    }

    public int hashCode() {
        return this.treeDigest.hashCode() + (37 * Arrays.hashCode(this.params.getKeyData()));
    }
}
