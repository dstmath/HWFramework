package com.android.org.bouncycastle.crypto.util;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1InputStream;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.ASN1Primitive;
import com.android.org.bouncycastle.asn1.DEROctetString;
import com.android.org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.DHParameter;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.RSAPublicKey;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.asn1.x509.DSAParameter;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import com.android.org.bouncycastle.asn1.x9.DHPublicKey;
import com.android.org.bouncycastle.asn1.x9.DomainParameters;
import com.android.org.bouncycastle.asn1.x9.ECNamedCurveTable;
import com.android.org.bouncycastle.asn1.x9.ValidationParams;
import com.android.org.bouncycastle.asn1.x9.X962Parameters;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.asn1.x9.X9ECPoint;
import com.android.org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import com.android.org.bouncycastle.crypto.ec.CustomNamedCurves;
import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import com.android.org.bouncycastle.crypto.params.DHParameters;
import com.android.org.bouncycastle.crypto.params.DHPublicKeyParameters;
import com.android.org.bouncycastle.crypto.params.DHValidationParameters;
import com.android.org.bouncycastle.crypto.params.DSAParameters;
import com.android.org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import com.android.org.bouncycastle.crypto.params.ECDomainParameters;
import com.android.org.bouncycastle.crypto.params.ECNamedDomainParameters;
import com.android.org.bouncycastle.crypto.params.ECPublicKeyParameters;
import com.android.org.bouncycastle.crypto.params.RSAKeyParameters;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

public class PublicKeyFactory {
    public static AsymmetricKeyParameter createKey(byte[] keyInfoData) throws IOException {
        return createKey(SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(keyInfoData)));
    }

    public static AsymmetricKeyParameter createKey(InputStream inStr) throws IOException {
        return createKey(SubjectPublicKeyInfo.getInstance(new ASN1InputStream(inStr).readObject()));
    }

    public static AsymmetricKeyParameter createKey(SubjectPublicKeyInfo keyInfo) throws IOException {
        AlgorithmIdentifier algId = keyInfo.getAlgorithm();
        ASN1Integer derY;
        if (algId.getAlgorithm().equals(PKCSObjectIdentifiers.rsaEncryption) || algId.getAlgorithm().equals(X509ObjectIdentifiers.id_ea_rsa)) {
            RSAPublicKey pubKey = RSAPublicKey.getInstance(keyInfo.parsePublicKey());
            return new RSAKeyParameters(false, pubKey.getModulus(), pubKey.getPublicExponent());
        } else if (algId.getAlgorithm().equals(X9ObjectIdentifiers.dhpublicnumber)) {
            BigInteger y = DHPublicKey.getInstance(keyInfo.parsePublicKey()).getY();
            DomainParameters dhParams = DomainParameters.getInstance(algId.getParameters());
            BigInteger p = dhParams.getP();
            BigInteger g = dhParams.getG();
            BigInteger q = dhParams.getQ();
            BigInteger j = null;
            if (dhParams.getJ() != null) {
                j = dhParams.getJ();
            }
            DHValidationParameters dHValidationParameters = null;
            ValidationParams dhValidationParms = dhParams.getValidationParams();
            if (dhValidationParms != null) {
                dHValidationParameters = new DHValidationParameters(dhValidationParms.getSeed(), dhValidationParms.getPgenCounter().intValue());
            }
            return new DHPublicKeyParameters(y, new DHParameters(p, g, q, j, dHValidationParameters));
        } else if (algId.getAlgorithm().equals(PKCSObjectIdentifiers.dhKeyAgreement)) {
            DHParameter params = DHParameter.getInstance(algId.getParameters());
            derY = (ASN1Integer) keyInfo.parsePublicKey();
            BigInteger lVal = params.getL();
            return new DHPublicKeyParameters(derY.getValue(), new DHParameters(params.getP(), params.getG(), null, lVal == null ? 0 : lVal.intValue()));
        } else if (algId.getAlgorithm().equals(X9ObjectIdentifiers.id_dsa) || algId.getAlgorithm().equals(OIWObjectIdentifiers.dsaWithSHA1)) {
            derY = (ASN1Integer) keyInfo.parsePublicKey();
            ASN1Encodable de = algId.getParameters();
            DSAParameters dSAParameters = null;
            if (de != null) {
                DSAParameter params2 = DSAParameter.getInstance(de.toASN1Primitive());
                DSAParameters dSAParameters2 = new DSAParameters(params2.getP(), params2.getQ(), params2.getG());
            }
            return new DSAPublicKeyParameters(derY.getValue(), dSAParameters);
        } else if (algId.getAlgorithm().equals(X9ObjectIdentifiers.id_ecPublicKey)) {
            X9ECParameters x9;
            ECDomainParameters dParams;
            X962Parameters params3 = X962Parameters.getInstance(algId.getParameters());
            if (params3.isNamedCurve()) {
                ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) params3.getParameters();
                x9 = CustomNamedCurves.getByOID(oid);
                if (x9 == null) {
                    x9 = ECNamedCurveTable.getByOID(oid);
                }
                dParams = new ECNamedDomainParameters(oid, x9.getCurve(), x9.getG(), x9.getN(), x9.getH(), x9.getSeed());
            } else {
                x9 = X9ECParameters.getInstance(params3.getParameters());
                ECDomainParameters eCDomainParameters = new ECDomainParameters(x9.getCurve(), x9.getG(), x9.getN(), x9.getH(), x9.getSeed());
            }
            return new ECPublicKeyParameters(new X9ECPoint(x9.getCurve(), new DEROctetString(keyInfo.getPublicKeyData().getBytes())).getPoint(), dParams);
        } else {
            throw new RuntimeException("algorithm identifier in key not recognised");
        }
    }
}
