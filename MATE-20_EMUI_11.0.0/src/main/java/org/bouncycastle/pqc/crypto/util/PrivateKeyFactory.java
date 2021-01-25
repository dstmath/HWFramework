package org.bouncycastle.pqc.crypto.util;

import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.bc.BCObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.pqc.asn1.PQCObjectIdentifiers;
import org.bouncycastle.pqc.asn1.SPHINCS256KeyParams;
import org.bouncycastle.pqc.asn1.XMSSKeyParams;
import org.bouncycastle.pqc.asn1.XMSSMTKeyParams;
import org.bouncycastle.pqc.asn1.XMSSMTPrivateKey;
import org.bouncycastle.pqc.asn1.XMSSPrivateKey;
import org.bouncycastle.pqc.crypto.lms.HSSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.lms.LMSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.newhope.NHPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.qtesla.QTESLAPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.sphincs.SPHINCSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.BDS;
import org.bouncycastle.pqc.crypto.xmss.BDSStateMap;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSMTPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSPrivateKeyParameters;
import org.bouncycastle.pqc.crypto.xmss.XMSSUtil;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;

public class PrivateKeyFactory {
    private static short[] convert(byte[] bArr) {
        short[] sArr = new short[(bArr.length / 2)];
        for (int i = 0; i != sArr.length; i++) {
            sArr[i] = Pack.littleEndianToShort(bArr, i * 2);
        }
        return sArr;
    }

    public static AsymmetricKeyParameter createKey(InputStream inputStream) throws IOException {
        return createKey(PrivateKeyInfo.getInstance(new ASN1InputStream(inputStream).readObject()));
    }

    public static AsymmetricKeyParameter createKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        ASN1ObjectIdentifier algorithm = privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm();
        if (algorithm.on(BCObjectIdentifiers.qTESLA)) {
            return new QTESLAPrivateKeyParameters(Utils.qTeslaLookupSecurityCategory(privateKeyInfo.getPrivateKeyAlgorithm()), ASN1OctetString.getInstance(privateKeyInfo.parsePrivateKey()).getOctets());
        } else if (algorithm.equals((ASN1Primitive) BCObjectIdentifiers.sphincs256)) {
            return new SPHINCSPrivateKeyParameters(ASN1OctetString.getInstance(privateKeyInfo.parsePrivateKey()).getOctets(), Utils.sphincs256LookupTreeAlgName(SPHINCS256KeyParams.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getParameters())));
        } else {
            if (algorithm.equals((ASN1Primitive) BCObjectIdentifiers.newHope)) {
                return new NHPrivateKeyParameters(convert(ASN1OctetString.getInstance(privateKeyInfo.parsePrivateKey()).getOctets()));
            }
            if (algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.id_alg_hss_lms_hashsig)) {
                byte[] octets = ASN1OctetString.getInstance(privateKeyInfo.parsePrivateKey()).getOctets();
                ASN1BitString publicKeyData = privateKeyInfo.getPublicKeyData();
                if (Pack.bigEndianToInt(octets, 0) != 1) {
                    return HSSPrivateKeyParameters.getInstance(Arrays.copyOfRange(octets, 4, octets.length));
                }
                if (publicKeyData == null) {
                    return LMSPrivateKeyParameters.getInstance(Arrays.copyOfRange(octets, 4, octets.length));
                }
                byte[] octets2 = publicKeyData.getOctets();
                return LMSPrivateKeyParameters.getInstance(Arrays.copyOfRange(octets, 4, octets.length), Arrays.copyOfRange(octets2, 4, octets2.length));
            } else if (algorithm.equals((ASN1Primitive) BCObjectIdentifiers.xmss)) {
                XMSSKeyParams instance = XMSSKeyParams.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getParameters());
                ASN1ObjectIdentifier algorithm2 = instance.getTreeDigest().getAlgorithm();
                XMSSPrivateKey instance2 = XMSSPrivateKey.getInstance(privateKeyInfo.parsePrivateKey());
                try {
                    XMSSPrivateKeyParameters.Builder withRoot = new XMSSPrivateKeyParameters.Builder(new XMSSParameters(instance.getHeight(), Utils.getDigest(algorithm2))).withIndex(instance2.getIndex()).withSecretKeySeed(instance2.getSecretKeySeed()).withSecretKeyPRF(instance2.getSecretKeyPRF()).withPublicSeed(instance2.getPublicSeed()).withRoot(instance2.getRoot());
                    if (instance2.getVersion() != 0) {
                        withRoot.withMaxIndex(instance2.getMaxIndex());
                    }
                    if (instance2.getBdsState() != null) {
                        withRoot.withBDSState(((BDS) XMSSUtil.deserialize(instance2.getBdsState(), BDS.class)).withWOTSDigest(algorithm2));
                    }
                    return withRoot.build();
                } catch (ClassNotFoundException e) {
                    throw new IOException("ClassNotFoundException processing BDS state: " + e.getMessage());
                }
            } else if (algorithm.equals((ASN1Primitive) PQCObjectIdentifiers.xmss_mt)) {
                XMSSMTKeyParams instance3 = XMSSMTKeyParams.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getParameters());
                ASN1ObjectIdentifier algorithm3 = instance3.getTreeDigest().getAlgorithm();
                try {
                    XMSSMTPrivateKey instance4 = XMSSMTPrivateKey.getInstance(privateKeyInfo.parsePrivateKey());
                    XMSSMTPrivateKeyParameters.Builder withRoot2 = new XMSSMTPrivateKeyParameters.Builder(new XMSSMTParameters(instance3.getHeight(), instance3.getLayers(), Utils.getDigest(algorithm3))).withIndex(instance4.getIndex()).withSecretKeySeed(instance4.getSecretKeySeed()).withSecretKeyPRF(instance4.getSecretKeyPRF()).withPublicSeed(instance4.getPublicSeed()).withRoot(instance4.getRoot());
                    if (instance4.getVersion() != 0) {
                        withRoot2.withMaxIndex(instance4.getMaxIndex());
                    }
                    if (instance4.getBdsState() != null) {
                        withRoot2.withBDSState(((BDSStateMap) XMSSUtil.deserialize(instance4.getBdsState(), BDSStateMap.class)).withWOTSDigest(algorithm3));
                    }
                    return withRoot2.build();
                } catch (ClassNotFoundException e2) {
                    throw new IOException("ClassNotFoundException processing BDS state: " + e2.getMessage());
                }
            } else {
                throw new RuntimeException("algorithm identifier in private key not recognised");
            }
        }
    }

    public static AsymmetricKeyParameter createKey(byte[] bArr) throws IOException {
        return createKey(PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(bArr)));
    }
}
