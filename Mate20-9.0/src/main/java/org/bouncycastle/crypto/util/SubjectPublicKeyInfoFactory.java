package org.bouncycastle.crypto.util;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Null;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSAPublicKey;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DSAParameter;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.DSAParameters;
import org.bouncycastle.crypto.params.DSAPublicKeyParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.RSAKeyParameters;

public class SubjectPublicKeyInfoFactory {
    private SubjectPublicKeyInfoFactory() {
    }

    public static SubjectPublicKeyInfo createSubjectPublicKeyInfo(AsymmetricKeyParameter asymmetricKeyParameter) throws IOException {
        X962Parameters x962Parameters;
        if (asymmetricKeyParameter instanceof RSAKeyParameters) {
            RSAKeyParameters rSAKeyParameters = (RSAKeyParameters) asymmetricKeyParameter;
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.rsaEncryption, DERNull.INSTANCE), (ASN1Encodable) new RSAPublicKey(rSAKeyParameters.getModulus(), rSAKeyParameters.getExponent()));
        } else if (asymmetricKeyParameter instanceof DSAPublicKeyParameters) {
            DSAPublicKeyParameters dSAPublicKeyParameters = (DSAPublicKeyParameters) asymmetricKeyParameter;
            DSAParameter dSAParameter = null;
            DSAParameters parameters = dSAPublicKeyParameters.getParameters();
            if (parameters != null) {
                dSAParameter = new DSAParameter(parameters.getP(), parameters.getQ(), parameters.getG());
            }
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_dsa, dSAParameter), (ASN1Encodable) new ASN1Integer(dSAPublicKeyParameters.getY()));
        } else if (asymmetricKeyParameter instanceof ECPublicKeyParameters) {
            ECPublicKeyParameters eCPublicKeyParameters = (ECPublicKeyParameters) asymmetricKeyParameter;
            ECDomainParameters parameters2 = eCPublicKeyParameters.getParameters();
            if (parameters2 == null) {
                x962Parameters = new X962Parameters((ASN1Null) DERNull.INSTANCE);
            } else if (parameters2 instanceof ECNamedDomainParameters) {
                x962Parameters = new X962Parameters(((ECNamedDomainParameters) parameters2).getName());
            } else {
                X9ECParameters x9ECParameters = new X9ECParameters(parameters2.getCurve(), parameters2.getG(), parameters2.getN(), parameters2.getH(), parameters2.getSeed());
                x962Parameters = new X962Parameters(x9ECParameters);
            }
            return new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, x962Parameters), ((ASN1OctetString) new X9ECPoint(eCPublicKeyParameters.getQ()).toASN1Primitive()).getOctets());
        } else {
            throw new IOException("key parameters not recognised.");
        }
    }
}
