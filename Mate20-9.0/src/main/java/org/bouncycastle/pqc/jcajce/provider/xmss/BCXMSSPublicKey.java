package org.bouncycastle.pqc.jcajce.provider.xmss;

import java.io.IOException;
import java.security.PublicKey;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.asn1.XMSSKeyParams;
import org.bouncycastle.pqc.asn1.XMSSPublicKey;
import org.bouncycastle.pqc.crypto.xmss.XMSSParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSPublicKeyParameters;
import org.bouncycastle.pqc.jcajce.interfaces.XMSSKey;
import org.bouncycastle.util.Arrays;

public class BCXMSSPublicKey implements PublicKey, XMSSKey {
    private final XMSSPublicKeyParameters keyParams;
    private final ASN1ObjectIdentifier treeDigest;

    public BCXMSSPublicKey(ASN1ObjectIdentifier aSN1ObjectIdentifier, XMSSPublicKeyParameters xMSSPublicKeyParameters) {
        this.treeDigest = aSN1ObjectIdentifier;
        this.keyParams = xMSSPublicKeyParameters;
    }

    public BCXMSSPublicKey(SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
        XMSSKeyParams instance = XMSSKeyParams.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
        this.treeDigest = instance.getTreeDigest().getAlgorithm();
        XMSSPublicKey instance2 = XMSSPublicKey.getInstance(subjectPublicKeyInfo.parsePublicKey());
        this.keyParams = new XMSSPublicKeyParameters.Builder(new XMSSParameters(instance.getHeight(), DigestUtil.getDigest(this.treeDigest))).withPublicSeed(instance2.getPublicSeed()).withRoot(instance2.getRoot()).build();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BCXMSSPublicKey)) {
            return false;
        }
        BCXMSSPublicKey bCXMSSPublicKey = (BCXMSSPublicKey) obj;
        return this.treeDigest.equals(bCXMSSPublicKey.treeDigest) && Arrays.areEqual(this.keyParams.toByteArray(), bCXMSSPublicKey.keyParams.toByteArray());
    }

    public final String getAlgorithm() {
        return "XMSS";
    }

    public byte[] getEncoded() {
        try {
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PQCObjectIdentifiers.xmss, new XMSSKeyParams(this.keyParams.getParameters().getHeight(), new AlgorithmIdentifier(this.treeDigest))), (ASN1Encodable) new XMSSPublicKey(this.keyParams.getPublicSeed(), this.keyParams.getRoot())).getEncoded();
        } catch (IOException e) {
            return null;
        }
    }

    public String getFormat() {
        return "X.509";
    }

    public int getHeight() {
        return this.keyParams.getParameters().getHeight();
    }

    /* access modifiers changed from: package-private */
    public CipherParameters getKeyParams() {
        return this.keyParams;
    }

    public String getTreeDigest() {
        return DigestUtil.getXMSSDigestName(this.treeDigest);
    }

    public int hashCode() {
        return this.treeDigest.hashCode() + (37 * Arrays.hashCode(this.keyParams.toByteArray()));
    }
}
