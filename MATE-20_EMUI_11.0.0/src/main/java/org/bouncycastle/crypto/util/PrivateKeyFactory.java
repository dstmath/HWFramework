package org.bouncycastle.crypto.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.oiw.ElGamalParameter;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSAPrivateKey;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPrivateKeyParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECGOST3410Parameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.Ed448PrivateKeyParameters;
import org.bouncycastle.crypto.params.ElGamalParameters;
import org.bouncycastle.crypto.params.ElGamalPrivateKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.params.X25519PrivateKeyParameters;
import org.bouncycastle.crypto.params.X448PrivateKeyParameters;
import org.bouncycastle.util.Arrays;

public class PrivateKeyFactory {
    public static AsymmetricKeyParameter createKey(InputStream inputStream) throws IOException {
        return createKey(PrivateKeyInfo.getInstance(new ASN1InputStream(inputStream).readObject()));
    }

    public static AsymmetricKeyParameter createKey(PrivateKeyInfo privateKeyInfo) throws IOException {
        BigInteger bigInteger;
        ECGOST3410Parameters eCGOST3410Parameters;
        ECDomainParameters eCDomainParameters;
        AlgorithmIdentifier privateKeyAlgorithm = privateKeyInfo.getPrivateKeyAlgorithm();
        ASN1ObjectIdentifier algorithm = privateKeyAlgorithm.getAlgorithm();
        if (algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.rsaEncryption) || algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.id_RSASSA_PSS) || algorithm.equals((ASN1Primitive) X509ObjectIdentifiers.id_ea_rsa)) {
            RSAPrivateKey instance = RSAPrivateKey.getInstance(privateKeyInfo.parsePrivateKey());
            return new RSAPrivateCrtKeyParameters(instance.getModulus(), instance.getPublicExponent(), instance.getPrivateExponent(), instance.getPrime1(), instance.getPrime2(), instance.getExponent1(), instance.getExponent2(), instance.getCoefficient());
        }
        ECGOST3410Parameters eCGOST3410Parameters2 = null;
        DSAParameters dSAParameters = null;
        int i = 0;
        if (algorithm.equals((ASN1Primitive) PKCSObjectIdentifiers.dhKeyAgreement)) {
            DHParameter instance2 = DHParameter.getInstance(privateKeyAlgorithm.getParameters());
            ASN1Integer aSN1Integer = (ASN1Integer) privateKeyInfo.parsePrivateKey();
            BigInteger l = instance2.getL();
            if (l != null) {
                i = l.intValue();
            }
            return new DHPrivateKeyParameters(aSN1Integer.getValue(), new DHParameters(instance2.getP(), instance2.getG(), null, i));
        } else if (algorithm.equals((ASN1Primitive) OIWObjectIdentifiers.elGamalAlgorithm)) {
            ElGamalParameter instance3 = ElGamalParameter.getInstance(privateKeyAlgorithm.getParameters());
            return new ElGamalPrivateKeyParameters(((ASN1Integer) privateKeyInfo.parsePrivateKey()).getValue(), new ElGamalParameters(instance3.getP(), instance3.getG()));
        } else if (algorithm.equals((ASN1Primitive) X9ObjectIdentifiers.id_dsa)) {
            ASN1Integer aSN1Integer2 = (ASN1Integer) privateKeyInfo.parsePrivateKey();
            ASN1Encodable parameters = privateKeyAlgorithm.getParameters();
            if (parameters != null) {
                DSAParameter instance4 = DSAParameter.getInstance(parameters.toASN1Primitive());
                dSAParameters = new DSAParameters(instance4.getP(), instance4.getQ(), instance4.getG());
            }
            return new DSAPrivateKeyParameters(aSN1Integer2.getValue(), dSAParameters);
        } else if (algorithm.equals((ASN1Primitive) X9ObjectIdentifiers.id_ecPublicKey)) {
            X962Parameters instance5 = X962Parameters.getInstance(privateKeyAlgorithm.getParameters());
            boolean isNamedCurve = instance5.isNamedCurve();
            ASN1Primitive parameters2 = instance5.getParameters();
            if (isNamedCurve) {
                ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) parameters2;
                X9ECParameters byOID = CustomNamedCurves.getByOID(aSN1ObjectIdentifier);
                if (byOID == null) {
                    byOID = ECNamedCurveTable.getByOID(aSN1ObjectIdentifier);
                }
                eCDomainParameters = new ECNamedDomainParameters(aSN1ObjectIdentifier, byOID.getCurve(), byOID.getG(), byOID.getN(), byOID.getH(), byOID.getSeed());
            } else {
                X9ECParameters instance6 = X9ECParameters.getInstance(parameters2);
                eCDomainParameters = new ECDomainParameters(instance6.getCurve(), instance6.getG(), instance6.getN(), instance6.getH(), instance6.getSeed());
            }
            return new ECPrivateKeyParameters(ECPrivateKey.getInstance(privateKeyInfo.parsePrivateKey()).getKey(), eCDomainParameters);
        } else if (algorithm.equals((ASN1Primitive) EdECObjectIdentifiers.id_X25519)) {
            return new X25519PrivateKeyParameters(getRawKey(privateKeyInfo, 32), 0);
        } else {
            if (algorithm.equals((ASN1Primitive) EdECObjectIdentifiers.id_X448)) {
                return new X448PrivateKeyParameters(getRawKey(privateKeyInfo, 56), 0);
            }
            if (algorithm.equals((ASN1Primitive) EdECObjectIdentifiers.id_Ed25519)) {
                return new Ed25519PrivateKeyParameters(getRawKey(privateKeyInfo, 32), 0);
            }
            if (algorithm.equals((ASN1Primitive) EdECObjectIdentifiers.id_Ed448)) {
                return new Ed448PrivateKeyParameters(getRawKey(privateKeyInfo, 57), 0);
            }
            if (algorithm.equals((ASN1Primitive) CryptoProObjectIdentifiers.gostR3410_2001) || algorithm.equals((ASN1Primitive) RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512) || algorithm.equals((ASN1Primitive) RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256)) {
                GOST3410PublicKeyAlgParameters instance7 = GOST3410PublicKeyAlgParameters.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getParameters());
                ASN1Primitive aSN1Primitive = privateKeyInfo.getPrivateKeyAlgorithm().getParameters().toASN1Primitive();
                if (!(aSN1Primitive instanceof ASN1Sequence) || !(ASN1Sequence.getInstance(aSN1Primitive).size() == 2 || ASN1Sequence.getInstance(aSN1Primitive).size() == 3)) {
                    X962Parameters instance8 = X962Parameters.getInstance(privateKeyInfo.getPrivateKeyAlgorithm().getParameters());
                    if (instance8.isNamedCurve()) {
                        ASN1ObjectIdentifier instance9 = ASN1ObjectIdentifier.getInstance(instance8.getParameters());
                        X9ECParameters byOID2 = ECNamedCurveTable.getByOID(instance9);
                        if (byOID2 == null) {
                            ECDomainParameters byOID3 = ECGOST3410NamedCurves.getByOID(instance9);
                            eCGOST3410Parameters = new ECGOST3410Parameters(new ECNamedDomainParameters(instance9, byOID3.getCurve(), byOID3.getG(), byOID3.getN(), byOID3.getH(), byOID3.getSeed()), instance7.getPublicKeyParamSet(), instance7.getDigestParamSet(), instance7.getEncryptionParamSet());
                        } else {
                            eCGOST3410Parameters = new ECGOST3410Parameters(new ECNamedDomainParameters(instance9, byOID2.getCurve(), byOID2.getG(), byOID2.getN(), byOID2.getH(), byOID2.getSeed()), instance7.getPublicKeyParamSet(), instance7.getDigestParamSet(), instance7.getEncryptionParamSet());
                        }
                        eCGOST3410Parameters2 = eCGOST3410Parameters;
                    } else if (!instance8.isImplicitlyCA()) {
                        X9ECParameters instance10 = X9ECParameters.getInstance(instance8.getParameters());
                        eCGOST3410Parameters2 = new ECGOST3410Parameters(new ECNamedDomainParameters(algorithm, instance10.getCurve(), instance10.getG(), instance10.getN(), instance10.getH(), instance10.getSeed()), instance7.getPublicKeyParamSet(), instance7.getDigestParamSet(), instance7.getEncryptionParamSet());
                    }
                    ASN1Encodable parsePrivateKey = privateKeyInfo.parsePrivateKey();
                    bigInteger = parsePrivateKey instanceof ASN1Integer ? ASN1Integer.getInstance(parsePrivateKey).getValue() : ECPrivateKey.getInstance(parsePrivateKey).getKey();
                } else {
                    eCGOST3410Parameters2 = new ECGOST3410Parameters(new ECNamedDomainParameters(instance7.getPublicKeyParamSet(), ECGOST3410NamedCurves.getByOID(instance7.getPublicKeyParamSet())), instance7.getPublicKeyParamSet(), instance7.getDigestParamSet(), instance7.getEncryptionParamSet());
                    ASN1OctetString privateKey = privateKeyInfo.getPrivateKey();
                    if (privateKey.getOctets().length == 32 || privateKey.getOctets().length == 64) {
                        bigInteger = new BigInteger(1, Arrays.reverse(privateKey.getOctets()));
                    } else {
                        ASN1Encodable parsePrivateKey2 = privateKeyInfo.parsePrivateKey();
                        bigInteger = parsePrivateKey2 instanceof ASN1Integer ? ASN1Integer.getInstance(parsePrivateKey2).getPositiveValue() : new BigInteger(1, Arrays.reverse(ASN1OctetString.getInstance(parsePrivateKey2).getOctets()));
                    }
                }
                return new ECPrivateKeyParameters(bigInteger, new ECGOST3410Parameters(eCGOST3410Parameters2, instance7.getPublicKeyParamSet(), instance7.getDigestParamSet(), instance7.getEncryptionParamSet()));
            }
            throw new RuntimeException("algorithm identifier in private key not recognised");
        }
    }

    public static AsymmetricKeyParameter createKey(byte[] bArr) throws IOException {
        return createKey(PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(bArr)));
    }

    private static byte[] getRawKey(PrivateKeyInfo privateKeyInfo, int i) throws IOException {
        byte[] octets = ASN1OctetString.getInstance(privateKeyInfo.parsePrivateKey()).getOctets();
        if (i == octets.length) {
            return octets;
        }
        throw new RuntimeException("private key encoding has incorrect length");
    }
}
