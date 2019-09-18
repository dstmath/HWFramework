package com.android.org.bouncycastle.jcajce.provider.asymmetric.util;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.nist.NISTNamedCurves;
import com.android.org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import com.android.org.bouncycastle.asn1.sec.SECNamedCurves;
import com.android.org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import com.android.org.bouncycastle.asn1.x9.ECNamedCurveTable;
import com.android.org.bouncycastle.asn1.x9.X962NamedCurves;
import com.android.org.bouncycastle.asn1.x9.X962Parameters;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.crypto.ec.CustomNamedCurves;
import com.android.org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import com.android.org.bouncycastle.crypto.params.ECDomainParameters;
import com.android.org.bouncycastle.crypto.params.ECNamedDomainParameters;
import com.android.org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import com.android.org.bouncycastle.crypto.params.ECPublicKeyParameters;
import com.android.org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import com.android.org.bouncycastle.jce.interfaces.ECPrivateKey;
import com.android.org.bouncycastle.jce.interfaces.ECPublicKey;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import com.android.org.bouncycastle.jce.spec.ECParameterSpec;
import com.android.org.bouncycastle.math.ec.ECPoint;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Enumeration;

public class ECUtil {
    static int[] convertMidTerms(int[] k) {
        int[] res = new int[3];
        if (k.length == 1) {
            res[0] = k[0];
        } else if (k.length != 3) {
            throw new IllegalArgumentException("Only Trinomials and pentanomials supported");
        } else if (k[0] < k[1] && k[0] < k[2]) {
            res[0] = k[0];
            if (k[1] < k[2]) {
                res[1] = k[1];
                res[2] = k[2];
            } else {
                res[1] = k[2];
                res[2] = k[1];
            }
        } else if (k[1] < k[2]) {
            res[0] = k[1];
            if (k[0] < k[2]) {
                res[1] = k[0];
                res[2] = k[2];
            } else {
                res[1] = k[2];
                res[2] = k[0];
            }
        } else {
            res[0] = k[2];
            if (k[0] < k[1]) {
                res[1] = k[0];
                res[2] = k[1];
            } else {
                res[1] = k[1];
                res[2] = k[0];
            }
        }
        return res;
    }

    public static ECDomainParameters getDomainParameters(ProviderConfiguration configuration, ECParameterSpec params) {
        if (params instanceof ECNamedCurveParameterSpec) {
            ECNamedCurveParameterSpec nParams = (ECNamedCurveParameterSpec) params;
            ECNamedDomainParameters eCNamedDomainParameters = new ECNamedDomainParameters(getNamedCurveOid(nParams.getName()), nParams.getCurve(), nParams.getG(), nParams.getN(), nParams.getH(), nParams.getSeed());
            return eCNamedDomainParameters;
        } else if (params == null) {
            ECParameterSpec iSpec = configuration.getEcImplicitlyCa();
            ECDomainParameters eCDomainParameters = new ECDomainParameters(iSpec.getCurve(), iSpec.getG(), iSpec.getN(), iSpec.getH(), iSpec.getSeed());
            return eCDomainParameters;
        } else {
            ECDomainParameters eCDomainParameters2 = new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH(), params.getSeed());
            return eCDomainParameters2;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v3, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: com.android.org.bouncycastle.asn1.x9.X9ECParameters} */
    /* JADX WARNING: Multi-variable type inference failed */
    public static ECDomainParameters getDomainParameters(ProviderConfiguration configuration, X962Parameters params) {
        if (params.isNamedCurve()) {
            ASN1ObjectIdentifier oid = ASN1ObjectIdentifier.getInstance(params.getParameters());
            X9ECParameters ecP = getNamedCurveByOid(oid);
            if (ecP == null) {
                ecP = configuration.getAdditionalECParameters().get(oid);
            }
            X9ECParameters ecP2 = ecP;
            ECNamedDomainParameters eCNamedDomainParameters = new ECNamedDomainParameters(oid, ecP2.getCurve(), ecP2.getG(), ecP2.getN(), ecP2.getH(), ecP2.getSeed());
            return eCNamedDomainParameters;
        } else if (params.isImplicitlyCA()) {
            ECParameterSpec iSpec = configuration.getEcImplicitlyCa();
            ECDomainParameters eCDomainParameters = new ECDomainParameters(iSpec.getCurve(), iSpec.getG(), iSpec.getN(), iSpec.getH(), iSpec.getSeed());
            return eCDomainParameters;
        } else {
            X9ECParameters ecP3 = X9ECParameters.getInstance(params.getParameters());
            ECDomainParameters domainParameters = new ECDomainParameters(ecP3.getCurve(), ecP3.getG(), ecP3.getN(), ecP3.getH(), ecP3.getSeed());
            return domainParameters;
        }
    }

    public static AsymmetricKeyParameter generatePublicKeyParameter(PublicKey key) throws InvalidKeyException {
        if (key instanceof ECPublicKey) {
            ECPublicKey k = (ECPublicKey) key;
            ECParameterSpec s = k.getParameters();
            ECPoint q = k.getQ();
            ECDomainParameters eCDomainParameters = new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed());
            return new ECPublicKeyParameters(q, eCDomainParameters);
        } else if (key instanceof java.security.interfaces.ECPublicKey) {
            java.security.interfaces.ECPublicKey pubKey = (java.security.interfaces.ECPublicKey) key;
            ECParameterSpec s2 = EC5Util.convertSpec(pubKey.getParams(), false);
            ECPoint convertPoint = EC5Util.convertPoint(pubKey.getParams(), pubKey.getW(), false);
            ECDomainParameters eCDomainParameters2 = new ECDomainParameters(s2.getCurve(), s2.getG(), s2.getN(), s2.getH(), s2.getSeed());
            return new ECPublicKeyParameters(convertPoint, eCDomainParameters2);
        } else {
            try {
                byte[] bytes = key.getEncoded();
                if (bytes != null) {
                    PublicKey publicKey = BouncyCastleProvider.getPublicKey(SubjectPublicKeyInfo.getInstance(bytes));
                    if (publicKey instanceof java.security.interfaces.ECPublicKey) {
                        return generatePublicKeyParameter(publicKey);
                    }
                    throw new InvalidKeyException("cannot identify EC public key.");
                }
                throw new InvalidKeyException("no encoding for EC public key");
            } catch (Exception e) {
                throw new InvalidKeyException("cannot identify EC public key: " + e.toString());
            }
        }
    }

    public static AsymmetricKeyParameter generatePrivateKeyParameter(PrivateKey key) throws InvalidKeyException {
        if (key instanceof ECPrivateKey) {
            ECPrivateKey k = (ECPrivateKey) key;
            ECParameterSpec s = k.getParameters();
            if (s == null) {
                s = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();
            }
            BigInteger d = k.getD();
            ECDomainParameters eCDomainParameters = new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed());
            return new ECPrivateKeyParameters(d, eCDomainParameters);
        } else if (key instanceof java.security.interfaces.ECPrivateKey) {
            java.security.interfaces.ECPrivateKey privKey = (java.security.interfaces.ECPrivateKey) key;
            ECParameterSpec s2 = EC5Util.convertSpec(privKey.getParams(), false);
            BigInteger s3 = privKey.getS();
            ECDomainParameters eCDomainParameters2 = new ECDomainParameters(s2.getCurve(), s2.getG(), s2.getN(), s2.getH(), s2.getSeed());
            return new ECPrivateKeyParameters(s3, eCDomainParameters2);
        } else {
            try {
                byte[] bytes = key.getEncoded();
                if (bytes != null) {
                    PrivateKey privateKey = BouncyCastleProvider.getPrivateKey(PrivateKeyInfo.getInstance(bytes));
                    if (privateKey instanceof java.security.interfaces.ECPrivateKey) {
                        return generatePrivateKeyParameter(privateKey);
                    }
                    throw new InvalidKeyException("can't identify EC private key.");
                }
                throw new InvalidKeyException("no encoding for EC private key");
            } catch (Exception e) {
                throw new InvalidKeyException("cannot identify EC private key: " + e.toString());
            }
        }
    }

    public static int getOrderBitLength(ProviderConfiguration configuration, BigInteger order, BigInteger privateValue) {
        if (order != null) {
            return order.bitLength();
        }
        ECParameterSpec implicitCA = configuration.getEcImplicitlyCa();
        if (implicitCA == null) {
            return privateValue.bitLength();
        }
        return implicitCA.getN().bitLength();
    }

    public static ASN1ObjectIdentifier getNamedCurveOid(String curveName) {
        String name;
        if (curveName.indexOf(32) > 0) {
            name = curveName.substring(curveName.indexOf(32) + 1);
        } else {
            name = curveName;
        }
        try {
            if (name.charAt(0) < '0' || name.charAt(0) > '2') {
                return lookupOidByName(name);
            }
            return new ASN1ObjectIdentifier(name);
        } catch (IllegalArgumentException e) {
            return lookupOidByName(name);
        }
    }

    private static ASN1ObjectIdentifier lookupOidByName(String name) {
        ASN1ObjectIdentifier oid = X962NamedCurves.getOID(name);
        if (oid != null) {
            return oid;
        }
        ASN1ObjectIdentifier oid2 = SECNamedCurves.getOID(name);
        if (oid2 == null) {
            return NISTNamedCurves.getOID(name);
        }
        return oid2;
    }

    public static ASN1ObjectIdentifier getNamedCurveOid(ECParameterSpec ecParameterSpec) {
        Enumeration names = ECNamedCurveTable.getNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            X9ECParameters params = ECNamedCurveTable.getByName(name);
            if (params.getN().equals(ecParameterSpec.getN()) && params.getH().equals(ecParameterSpec.getH()) && params.getCurve().equals(ecParameterSpec.getCurve()) && params.getG().equals(ecParameterSpec.getG())) {
                return ECNamedCurveTable.getOID(name);
            }
        }
        return null;
    }

    public static X9ECParameters getNamedCurveByOid(ASN1ObjectIdentifier oid) {
        X9ECParameters params = CustomNamedCurves.getByOID(oid);
        if (params != null) {
            return params;
        }
        X9ECParameters params2 = X962NamedCurves.getByOID(oid);
        if (params2 == null) {
            params2 = SECNamedCurves.getByOID(oid);
        }
        if (params2 == null) {
            return NISTNamedCurves.getByOID(oid);
        }
        return params2;
    }

    public static X9ECParameters getNamedCurveByName(String curveName) {
        X9ECParameters params = CustomNamedCurves.getByName(curveName);
        if (params != null) {
            return params;
        }
        X9ECParameters params2 = X962NamedCurves.getByName(curveName);
        if (params2 == null) {
            params2 = SECNamedCurves.getByName(curveName);
        }
        if (params2 == null) {
            return NISTNamedCurves.getByName(curveName);
        }
        return params2;
    }

    public static String getCurveName(ASN1ObjectIdentifier oid) {
        String name = X962NamedCurves.getName(oid);
        if (name != null) {
            return name;
        }
        String name2 = SECNamedCurves.getName(oid);
        if (name2 == null) {
            return NISTNamedCurves.getName(oid);
        }
        return name2;
    }
}
