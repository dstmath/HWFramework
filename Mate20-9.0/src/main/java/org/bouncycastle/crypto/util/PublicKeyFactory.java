package org.bouncycastle.crypto.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.cryptopro.ECGOST3410NamedCurves;
import org.bouncycastle.asn1.cryptopro.GOST3410PublicKeyAlgParameters;
import org.bouncycastle.asn1.oiw.ElGamalParameter;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.DHParameter;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.rosstandart.RosstandartObjectIdentifiers;
import org.bouncycastle.asn1.ua.DSTU4145BinaryField;
import org.bouncycastle.asn1.ua.DSTU4145ECBinary;
import org.bouncycastle.asn1.ua.DSTU4145NamedCurves;
import org.bouncycastle.asn1.ua.DSTU4145Params;
import org.bouncycastle.asn1.ua.DSTU4145PointEncoder;
import org.bouncycastle.asn1.ua.UAObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.bouncycastle.asn1.x9.DHPublicKey;
import org.bouncycastle.asn1.x9.DomainParameters;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.ValidationParams;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DHParameters;
import org.bouncycastle.crypto.params.DHPublicKeyParameters;
import org.bouncycastle.crypto.params.DHValidationParameters;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ElGamalParameters;
import org.bouncycastle.crypto.params.ElGamalPublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.math.ec.ECCurve;

public class PublicKeyFactory {
    private static Map converters = new HashMap();

    private static class DHAgreementConverter extends SubjectPublicKeyInfoConverter {
        private DHAgreementConverter() {
            super();
        }

        /* access modifiers changed from: package-private */
        public AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object obj) throws IOException {
            DHParameter instance = DHParameter.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            ASN1Integer aSN1Integer = (ASN1Integer) subjectPublicKeyInfo.parsePublicKey();
            BigInteger l = instance.getL();
            return new DHPublicKeyParameters(aSN1Integer.getValue(), new DHParameters(instance.getP(), instance.getG(), null, l == null ? 0 : l.intValue()));
        }
    }

    private static class DHPublicNumberConverter extends SubjectPublicKeyInfoConverter {
        private DHPublicNumberConverter() {
            super();
        }

        /* access modifiers changed from: package-private */
        public AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object obj) throws IOException {
            BigInteger y = DHPublicKey.getInstance(subjectPublicKeyInfo.parsePublicKey()).getY();
            DomainParameters instance = DomainParameters.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            BigInteger p = instance.getP();
            BigInteger g = instance.getG();
            BigInteger q = instance.getQ();
            DHValidationParameters dHValidationParameters = null;
            BigInteger j = instance.getJ() != null ? instance.getJ() : null;
            ValidationParams validationParams = instance.getValidationParams();
            if (validationParams != null) {
                dHValidationParameters = new DHValidationParameters(validationParams.getSeed(), validationParams.getPgenCounter().intValue());
            }
            DHParameters dHParameters = new DHParameters(p, g, q, j, dHValidationParameters);
            return new DHPublicKeyParameters(y, dHParameters);
        }
    }

    private static class DSAConverter extends SubjectPublicKeyInfoConverter {
        private DSAConverter() {
            super();
        }

        /* access modifiers changed from: package-private */
        public AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object obj) throws IOException {
            DSAParameters dSAParameters;
            ASN1Integer aSN1Integer = (ASN1Integer) subjectPublicKeyInfo.parsePublicKey();
            ASN1Encodable parameters = subjectPublicKeyInfo.getAlgorithm().getParameters();
            if (parameters != null) {
                DSAParameter instance = DSAParameter.getInstance(parameters.toASN1Primitive());
                dSAParameters = new DSAParameters(instance.getP(), instance.getQ(), instance.getG());
            } else {
                dSAParameters = null;
            }
            return new DSAPublicKeyParameters(aSN1Integer.getValue(), dSAParameters);
        }
    }

    private static class DSTUConverter extends SubjectPublicKeyInfoConverter {
        private DSTUConverter() {
            super();
        }

        private void reverseBytes(byte[] bArr) {
            for (int i = 0; i < bArr.length / 2; i++) {
                byte b = bArr[i];
                bArr[i] = bArr[(bArr.length - 1) - i];
                bArr[(bArr.length - 1) - i] = b;
            }
        }

        /* access modifiers changed from: package-private */
        public AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object obj) throws IOException {
            ECDomainParameters eCDomainParameters;
            try {
                byte[] octets = ((ASN1OctetString) ASN1Primitive.fromByteArray(subjectPublicKeyInfo.getPublicKeyData().getBytes())).getOctets();
                if (subjectPublicKeyInfo.getAlgorithm().getAlgorithm().equals(UAObjectIdentifiers.dstu4145le)) {
                    reverseBytes(octets);
                }
                DSTU4145Params instance = DSTU4145Params.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
                if (instance.isNamedCurve()) {
                    eCDomainParameters = DSTU4145NamedCurves.getByOID(instance.getNamedCurve());
                } else {
                    DSTU4145ECBinary eCBinary = instance.getECBinary();
                    byte[] b = eCBinary.getB();
                    if (subjectPublicKeyInfo.getAlgorithm().getAlgorithm().equals(UAObjectIdentifiers.dstu4145le)) {
                        reverseBytes(b);
                    }
                    DSTU4145BinaryField field = eCBinary.getField();
                    ECCurve.F2m f2m = new ECCurve.F2m(field.getM(), field.getK1(), field.getK2(), field.getK3(), eCBinary.getA(), new BigInteger(1, b));
                    byte[] g = eCBinary.getG();
                    if (subjectPublicKeyInfo.getAlgorithm().getAlgorithm().equals(UAObjectIdentifiers.dstu4145le)) {
                        reverseBytes(g);
                    }
                    eCDomainParameters = new ECDomainParameters(f2m, DSTU4145PointEncoder.decodePoint(f2m, g), eCBinary.getN());
                }
                return new ECPublicKeyParameters(DSTU4145PointEncoder.decodePoint(eCDomainParameters.getCurve(), octets), eCDomainParameters);
            } catch (IOException e) {
                throw new IllegalArgumentException("error recovering public key");
            }
        }
    }

    private static class ECConverter extends SubjectPublicKeyInfoConverter {
        private ECConverter() {
            super();
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v0, resolved type: org.bouncycastle.crypto.params.ECNamedDomainParameters} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v12, resolved type: org.bouncycastle.crypto.params.ECDomainParameters} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v1, resolved type: org.bouncycastle.crypto.params.ECNamedDomainParameters} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v3, resolved type: org.bouncycastle.crypto.params.ECDomainParameters} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v5, resolved type: org.bouncycastle.crypto.params.ECNamedDomainParameters} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v14, resolved type: org.bouncycastle.crypto.params.ECNamedDomainParameters} */
        /* JADX WARNING: type inference failed for: r9v7, types: [org.bouncycastle.asn1.ASN1Primitive] */
        /* access modifiers changed from: package-private */
        /* JADX WARNING: Multi-variable type inference failed */
        public AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object obj) {
            ECNamedDomainParameters eCNamedDomainParameters;
            X962Parameters instance = X962Parameters.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            if (instance.isNamedCurve()) {
                ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) instance.getParameters();
                X9ECParameters byOID = CustomNamedCurves.getByOID(aSN1ObjectIdentifier);
                if (byOID == null) {
                    byOID = ECNamedCurveTable.getByOID(aSN1ObjectIdentifier);
                }
                ECNamedDomainParameters eCNamedDomainParameters2 = new ECNamedDomainParameters(aSN1ObjectIdentifier, byOID.getCurve(), byOID.getG(), byOID.getN(), byOID.getH(), byOID.getSeed());
                eCNamedDomainParameters = eCNamedDomainParameters2;
            } else if (instance.isImplicitlyCA()) {
                eCNamedDomainParameters = (ECDomainParameters) obj;
            } else {
                X9ECParameters instance2 = X9ECParameters.getInstance(instance.getParameters());
                ECDomainParameters eCDomainParameters = new ECDomainParameters(instance2.getCurve(), instance2.getG(), instance2.getN(), instance2.getH(), instance2.getSeed());
                eCNamedDomainParameters = eCDomainParameters;
            }
            byte[] bytes = subjectPublicKeyInfo.getPublicKeyData().getBytes();
            ASN1OctetString dEROctetString = new DEROctetString(bytes);
            if (bytes[0] == 4 && bytes[1] == bytes.length - 2 && ((bytes[2] == 2 || bytes[2] == 3) && new X9IntegerConverter().getByteLength(eCNamedDomainParameters.getCurve()) >= bytes.length - 3)) {
                try {
                    dEROctetString = ASN1Primitive.fromByteArray(bytes);
                } catch (IOException e) {
                    throw new IllegalArgumentException("error recovering public key");
                }
            }
            return new ECPublicKeyParameters(new X9ECPoint(eCNamedDomainParameters.getCurve(), dEROctetString).getPoint(), eCNamedDomainParameters);
        }
    }

    private static class ElGamalConverter extends SubjectPublicKeyInfoConverter {
        private ElGamalConverter() {
            super();
        }

        /* access modifiers changed from: package-private */
        public AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object obj) throws IOException {
            ElGamalParameter instance = ElGamalParameter.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters());
            return new ElGamalPublicKeyParameters(((ASN1Integer) subjectPublicKeyInfo.parsePublicKey()).getValue(), new ElGamalParameters(instance.getP(), instance.getG()));
        }
    }

    private static class GOST3410_2001Converter extends SubjectPublicKeyInfoConverter {
        private GOST3410_2001Converter() {
            super();
        }

        /* access modifiers changed from: package-private */
        public AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object obj) {
            try {
                byte[] octets = ((ASN1OctetString) ASN1Primitive.fromByteArray(subjectPublicKeyInfo.getPublicKeyData().getBytes())).getOctets();
                byte[] bArr = new byte[65];
                bArr[0] = 4;
                for (int i = 1; i <= 32; i++) {
                    bArr[i] = octets[32 - i];
                    bArr[i + 32] = octets[64 - i];
                }
                ECDomainParameters byOID = ECGOST3410NamedCurves.getByOID(subjectPublicKeyInfo.getAlgorithm().getParameters() instanceof ASN1ObjectIdentifier ? ASN1ObjectIdentifier.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters()) : GOST3410PublicKeyAlgParameters.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters()).getPublicKeyParamSet());
                return new ECPublicKeyParameters(byOID.getCurve().decodePoint(bArr), byOID);
            } catch (IOException e) {
                throw new IllegalArgumentException("error recovering public key");
            }
        }
    }

    private static class GOST3410_2012Converter extends SubjectPublicKeyInfoConverter {
        private GOST3410_2012Converter() {
            super();
        }

        /* access modifiers changed from: package-private */
        public AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object obj) {
            ASN1ObjectIdentifier algorithm = subjectPublicKeyInfo.getAlgorithm().getAlgorithm();
            try {
                byte[] octets = ((ASN1OctetString) ASN1Primitive.fromByteArray(subjectPublicKeyInfo.getPublicKeyData().getBytes())).getOctets();
                int i = 32;
                if (algorithm.equals(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512)) {
                    i = 64;
                }
                int i2 = 2 * i;
                byte[] bArr = new byte[(1 + i2)];
                bArr[0] = 4;
                for (int i3 = 1; i3 <= i; i3++) {
                    bArr[i3] = octets[i - i3];
                    bArr[i3 + i] = octets[i2 - i3];
                }
                ECDomainParameters byOID = ECGOST3410NamedCurves.getByOID(GOST3410PublicKeyAlgParameters.getInstance(subjectPublicKeyInfo.getAlgorithm().getParameters()).getPublicKeyParamSet());
                return new ECPublicKeyParameters(byOID.getCurve().decodePoint(bArr), byOID);
            } catch (IOException e) {
                throw new IllegalArgumentException("error recovering public key");
            }
        }
    }

    private static class RSAConverter extends SubjectPublicKeyInfoConverter {
        private RSAConverter() {
            super();
        }

        /* access modifiers changed from: package-private */
        public AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object obj) throws IOException {
            RSAPublicKey instance = RSAPublicKey.getInstance(subjectPublicKeyInfo.parsePublicKey());
            return new RSAKeyParameters(false, instance.getModulus(), instance.getPublicExponent());
        }
    }

    private static abstract class SubjectPublicKeyInfoConverter {
        private SubjectPublicKeyInfoConverter() {
        }

        /* access modifiers changed from: package-private */
        public abstract AsymmetricKeyParameter getPublicKeyParameters(SubjectPublicKeyInfo subjectPublicKeyInfo, Object obj) throws IOException;
    }

    static {
        converters.put(PKCSObjectIdentifiers.rsaEncryption, new RSAConverter());
        converters.put(X509ObjectIdentifiers.id_ea_rsa, new RSAConverter());
        converters.put(X9ObjectIdentifiers.dhpublicnumber, new DHPublicNumberConverter());
        converters.put(PKCSObjectIdentifiers.dhKeyAgreement, new DHAgreementConverter());
        converters.put(X9ObjectIdentifiers.id_dsa, new DSAConverter());
        converters.put(OIWObjectIdentifiers.dsaWithSHA1, new DSAConverter());
        converters.put(OIWObjectIdentifiers.elGamalAlgorithm, new ElGamalConverter());
        converters.put(X9ObjectIdentifiers.id_ecPublicKey, new ECConverter());
        converters.put(CryptoProObjectIdentifiers.gostR3410_2001, new GOST3410_2001Converter());
        converters.put(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_256, new GOST3410_2012Converter());
        converters.put(RosstandartObjectIdentifiers.id_tc26_gost_3410_12_512, new GOST3410_2012Converter());
        converters.put(UAObjectIdentifiers.dstu4145be, new DSTUConverter());
        converters.put(UAObjectIdentifiers.dstu4145le, new DSTUConverter());
    }

    public static AsymmetricKeyParameter createKey(InputStream inputStream) throws IOException {
        return createKey(SubjectPublicKeyInfo.getInstance(new ASN1InputStream(inputStream).readObject()));
    }

    public static AsymmetricKeyParameter createKey(SubjectPublicKeyInfo subjectPublicKeyInfo) throws IOException {
        return createKey(subjectPublicKeyInfo, null);
    }

    public static AsymmetricKeyParameter createKey(SubjectPublicKeyInfo subjectPublicKeyInfo, Object obj) throws IOException {
        AlgorithmIdentifier algorithm = subjectPublicKeyInfo.getAlgorithm();
        SubjectPublicKeyInfoConverter subjectPublicKeyInfoConverter = (SubjectPublicKeyInfoConverter) converters.get(algorithm.getAlgorithm());
        if (subjectPublicKeyInfoConverter != null) {
            return subjectPublicKeyInfoConverter.getPublicKeyParameters(subjectPublicKeyInfo, obj);
        }
        throw new IOException("algorithm identifier in key not recognised: " + algorithm.getAlgorithm());
    }

    public static AsymmetricKeyParameter createKey(byte[] bArr) throws IOException {
        return createKey(SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(bArr)));
    }
}
