package org.bouncycastle.pqc.jcajce.provider.xmss;

import java.io.IOException;
import java.security.PrivateKey;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.asn1.XMSSKeyParams;
import org.bouncycastle.pqc.asn1.XMSSPrivateKey;
import org.bouncycastle.pqc.crypto.xmss.BDS;
import org.bouncycastle.pqc.crypto.xmss.XMSSParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSUtil;
import org.bouncycastle.pqc.jcajce.interfaces.XMSSKey;
import org.bouncycastle.util.Arrays;

public class BCXMSSPrivateKey implements PrivateKey, XMSSKey {
    private final XMSSPrivateKeyParameters keyParams;
    private final ASN1ObjectIdentifier treeDigest;

    public BCXMSSPrivateKey(ASN1ObjectIdentifier aSN1ObjectIdentifier, XMSSPrivateKeyParameters xMSSPrivateKeyParameters) {
        this.treeDigest = aSN1ObjectIdentifier;
        this.keyParams = xMSSPrivateKeyParameters;
    }

    public BCXMSSPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        XMSSKeyParams instance = XMSSKeyParams.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getParameters());
        this.treeDigest = instance.getTreeDigest().getAlgorithm();
        XMSSPrivateKey instance2 = XMSSPrivateKey.getInstance(privateKeyInfo.parsePrivateKey());
        try {
            XMSSPrivateKeyParameters.Builder withRoot = new XMSSPrivateKeyParameters.Builder(new XMSSParameters(instance.getHeight(), DigestUtil.getDigest(this.treeDigest))).withIndex(instance2.getIndex()).withSecretKeySeed(instance2.getSecretKeySeed()).withSecretKeyPRF(instance2.getSecretKeyPRF()).withPublicSeed(instance2.getPublicSeed()).withRoot(instance2.getRoot());
            if (instance2.getBdsState() != null) {
                withRoot.withBDSState((BDS) XMSSUtil.deserialize(instance2.getBdsState(), BDS.class));
            }
            this.keyParams = withRoot.build();
        } catch (ClassNotFoundException e) {
            throw new IOException("ClassNotFoundException processing BDS state: " + e.getMessage());
        }
    }

    private XMSSPrivateKey createKeyStructure() {
        byte[] byteArray = this.keyParams.toByteArray();
        int digestSize = this.keyParams.getParameters().getDigestSize();
        int height = this.keyParams.getParameters().getHeight();
        int bytesToXBigEndian = (int) XMSSUtil.bytesToXBigEndian(byteArray, 0, 4);
        if (XMSSUtil.isIndexValid(height, (long) bytesToXBigEndian)) {
            byte[] extractBytesAtOffset = XMSSUtil.extractBytesAtOffset(byteArray, 4, digestSize);
            int i = 4 + digestSize;
            byte[] extractBytesAtOffset2 = XMSSUtil.extractBytesAtOffset(byteArray, i, digestSize);
            int i2 = i + digestSize;
            byte[] extractBytesAtOffset3 = XMSSUtil.extractBytesAtOffset(byteArray, i2, digestSize);
            int i3 = i2 + digestSize;
            byte[] extractBytesAtOffset4 = XMSSUtil.extractBytesAtOffset(byteArray, i3, digestSize);
            int i4 = i3 + digestSize;
            XMSSPrivateKey xMSSPrivateKey = new XMSSPrivateKey(bytesToXBigEndian, extractBytesAtOffset, extractBytesAtOffset2, extractBytesAtOffset3, extractBytesAtOffset4, XMSSUtil.extractBytesAtOffset(byteArray, i4, byteArray.length - i4));
            return xMSSPrivateKey;
        }
        throw new IllegalArgumentException("index out of bounds");
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BCXMSSPrivateKey)) {
            return false;
        }
        BCXMSSPrivateKey bCXMSSPrivateKey = (BCXMSSPrivateKey) obj;
        return this.treeDigest.equals(bCXMSSPrivateKey.treeDigest) && Arrays.areEqual(this.keyParams.toByteArray(), bCXMSSPrivateKey.keyParams.toByteArray());
    }

    public String getAlgorithm() {
        return "XMSS";
    }

    public byte[] getEncoded() {
        try {
            return new PrivateKeyInfo(new AlgorithmIdentifier(PQCObjectIdentifiers.xmss, new XMSSKeyParams(this.keyParams.getParameters().getHeight(), new AlgorithmIdentifier(this.treeDigest))), createKeyStructure()).getEncoded();
        } catch (IOException e) {
            return null;
        }
    }

    public String getFormat() {
        return "PKCS#8";
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

    /* access modifiers changed from: package-private */
    public ASN1ObjectIdentifier getTreeDigestOID() {
        return this.treeDigest;
    }

    public int hashCode() {
        return this.treeDigest.hashCode() + (37 * Arrays.hashCode(this.keyParams.toByteArray()));
    }
}
