package org.bouncycastle.pqc.jcajce.provider.xmss;

import java.io.IOException;
import java.security.PrivateKey;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.asn1.XMSSMTKeyParams;
import org.bouncycastle.pqc.asn1.XMSSMTPrivateKey;
import org.bouncycastle.pqc.asn1.XMSSPrivateKey;
import org.bouncycastle.pqc.crypto.xmss.BDSStateMap;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSUtil;
import org.bouncycastle.pqc.jcajce.interfaces.XMSSMTKey;
import org.bouncycastle.util.Arrays;

public class BCXMSSMTPrivateKey implements PrivateKey, XMSSMTKey {
    private final XMSSMTPrivateKeyParameters keyParams;
    private final ASN1ObjectIdentifier treeDigest;

    public BCXMSSMTPrivateKey(ASN1ObjectIdentifier aSN1ObjectIdentifier, XMSSMTPrivateKeyParameters xMSSMTPrivateKeyParameters) {
        this.treeDigest = aSN1ObjectIdentifier;
        this.keyParams = xMSSMTPrivateKeyParameters;
    }

    public BCXMSSMTPrivateKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        XMSSMTKeyParams instance = XMSSMTKeyParams.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getParameters());
        this.treeDigest = instance.getTreeDigest().getAlgorithm();
        XMSSPrivateKey instance2 = XMSSPrivateKey.getInstance(privateKeyInfo.parsePrivateKey());
        try {
            XMSSMTPrivateKeyParameters.Builder withRoot = new XMSSMTPrivateKeyParameters.Builder(new XMSSMTParameters(instance.getHeight(), instance.getLayers(), DigestUtil.getDigest(this.treeDigest))).withIndex((long) instance2.getIndex()).withSecretKeySeed(instance2.getSecretKeySeed()).withSecretKeyPRF(instance2.getSecretKeyPRF()).withPublicSeed(instance2.getPublicSeed()).withRoot(instance2.getRoot());
            if (instance2.getBdsState() != null) {
                withRoot.withBDSState((BDSStateMap) XMSSUtil.deserialize(instance2.getBdsState(), BDSStateMap.class));
            }
            this.keyParams = withRoot.build();
        } catch (ClassNotFoundException e) {
            throw new IOException("ClassNotFoundException processing BDS state: " + e.getMessage());
        }
    }

    private XMSSMTPrivateKey createKeyStructure() {
        byte[] byteArray = this.keyParams.toByteArray();
        int digestSize = this.keyParams.getParameters().getDigestSize();
        int height = this.keyParams.getParameters().getHeight();
        int i = (height + 7) / 8;
        int bytesToXBigEndian = (int) XMSSUtil.bytesToXBigEndian(byteArray, 0, i);
        if (XMSSUtil.isIndexValid(height, (long) bytesToXBigEndian)) {
            int i2 = 0 + i;
            byte[] extractBytesAtOffset = XMSSUtil.extractBytesAtOffset(byteArray, i2, digestSize);
            int i3 = i2 + digestSize;
            byte[] extractBytesAtOffset2 = XMSSUtil.extractBytesAtOffset(byteArray, i3, digestSize);
            int i4 = i3 + digestSize;
            byte[] extractBytesAtOffset3 = XMSSUtil.extractBytesAtOffset(byteArray, i4, digestSize);
            int i5 = i4 + digestSize;
            byte[] extractBytesAtOffset4 = XMSSUtil.extractBytesAtOffset(byteArray, i5, digestSize);
            int i6 = i5 + digestSize;
            XMSSMTPrivateKey xMSSMTPrivateKey = new XMSSMTPrivateKey(bytesToXBigEndian, extractBytesAtOffset, extractBytesAtOffset2, extractBytesAtOffset3, extractBytesAtOffset4, XMSSUtil.extractBytesAtOffset(byteArray, i6, byteArray.length - i6));
            return xMSSMTPrivateKey;
        }
        throw new IllegalArgumentException("index out of bounds");
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BCXMSSMTPrivateKey)) {
            return false;
        }
        BCXMSSMTPrivateKey bCXMSSMTPrivateKey = (BCXMSSMTPrivateKey) obj;
        return this.treeDigest.equals(bCXMSSMTPrivateKey.treeDigest) && Arrays.areEqual(this.keyParams.toByteArray(), bCXMSSMTPrivateKey.keyParams.toByteArray());
    }

    public String getAlgorithm() {
        return "XMSSMT";
    }

    public byte[] getEncoded() {
        try {
            return new PrivateKeyInfo(new AlgorithmIdentifier(PQCObjectIdentifiers.xmss_mt, new XMSSMTKeyParams(this.keyParams.getParameters().getHeight(), this.keyParams.getParameters().getLayers(), new AlgorithmIdentifier(this.treeDigest))), createKeyStructure()).getEncoded();
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

    public int getLayers() {
        return this.keyParams.getParameters().getLayers();
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
