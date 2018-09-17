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
            return new ECNamedDomainParameters(getNamedCurveOid(nParams.getName()), nParams.getCurve(), nParams.getG(), nParams.getN(), nParams.getH(), nParams.getSeed());
        } else if (params != null) {
            return new ECDomainParameters(params.getCurve(), params.getG(), params.getN(), params.getH(), params.getSeed());
        } else {
            ECParameterSpec iSpec = configuration.getEcImplicitlyCa();
            return new ECDomainParameters(iSpec.getCurve(), iSpec.getG(), iSpec.getN(), iSpec.getH(), iSpec.getSeed());
        }
    }

    public static ECDomainParameters getDomainParameters(ProviderConfiguration configuration, X962Parameters params) {
        X9ECParameters ecP;
        if (params.isNamedCurve()) {
            ASN1ObjectIdentifier oid = ASN1ObjectIdentifier.getInstance(params.getParameters());
            ecP = getNamedCurveByOid(oid);
            if (ecP == null) {
                ecP = (X9ECParameters) configuration.getAdditionalECParameters().get(oid);
            }
            return new ECNamedDomainParameters(oid, ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH(), ecP.getSeed());
        } else if (params.isImplicitlyCA()) {
            ECParameterSpec iSpec = configuration.getEcImplicitlyCa();
            return new ECDomainParameters(iSpec.getCurve(), iSpec.getG(), iSpec.getN(), iSpec.getH(), iSpec.getSeed());
        } else {
            ecP = X9ECParameters.getInstance(params.getParameters());
            return new ECDomainParameters(ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH(), ecP.getSeed());
        }
    }

    public static AsymmetricKeyParameter generatePublicKeyParameter(PublicKey key) throws InvalidKeyException {
        ECParameterSpec s;
        if (key instanceof ECPublicKey) {
            ECPublicKey k = (ECPublicKey) key;
            s = k.getParameters();
            return new ECPublicKeyParameters(k.getQ(), new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
        } else if (key instanceof java.security.interfaces.ECPublicKey) {
            java.security.interfaces.ECPublicKey pubKey = (java.security.interfaces.ECPublicKey) key;
            s = EC5Util.convertSpec(pubKey.getParams(), false);
            return new ECPublicKeyParameters(EC5Util.convertPoint(pubKey.getParams(), pubKey.getW(), false), new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
        } else {
            try {
                byte[] bytes = key.getEncoded();
                if (bytes == null) {
                    throw new InvalidKeyException("no encoding for EC public key");
                }
                PublicKey publicKey = BouncyCastleProvider.getPublicKey(SubjectPublicKeyInfo.getInstance(bytes));
                if (publicKey instanceof java.security.interfaces.ECPublicKey) {
                    return generatePublicKeyParameter(publicKey);
                }
                throw new InvalidKeyException("cannot identify EC public key.");
            } catch (Exception e) {
                throw new InvalidKeyException("cannot identify EC public key: " + e.toString());
            }
        }
    }

    public static AsymmetricKeyParameter generatePrivateKeyParameter(PrivateKey key) throws InvalidKeyException {
        ECParameterSpec s;
        if (key instanceof ECPrivateKey) {
            ECPrivateKey k = (ECPrivateKey) key;
            s = k.getParameters();
            if (s == null) {
                s = BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa();
            }
            return new ECPrivateKeyParameters(k.getD(), new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
        } else if (key instanceof java.security.interfaces.ECPrivateKey) {
            java.security.interfaces.ECPrivateKey privKey = (java.security.interfaces.ECPrivateKey) key;
            s = EC5Util.convertSpec(privKey.getParams(), false);
            return new ECPrivateKeyParameters(privKey.getS(), new ECDomainParameters(s.getCurve(), s.getG(), s.getN(), s.getH(), s.getSeed()));
        } else {
            try {
                byte[] bytes = key.getEncoded();
                if (bytes == null) {
                    throw new InvalidKeyException("no encoding for EC private key");
                }
                PrivateKey privateKey = BouncyCastleProvider.getPrivateKey(PrivateKeyInfo.getInstance(bytes));
                if (privateKey instanceof java.security.interfaces.ECPrivateKey) {
                    return generatePrivateKeyParameter(privateKey);
                }
                throw new InvalidKeyException("can't identify EC private key.");
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
        oid = SECNamedCurves.getOID(name);
        if (oid == null) {
            return NISTNamedCurves.getOID(name);
        }
        return oid;
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
        params = X962NamedCurves.getByOID(oid);
        if (params == null) {
            params = SECNamedCurves.getByOID(oid);
        }
        if (params == null) {
            return NISTNamedCurves.getByOID(oid);
        }
        return params;
    }

    public static X9ECParameters getNamedCurveByName(String curveName) {
        X9ECParameters params = CustomNamedCurves.getByName(curveName);
        if (params != null) {
            return params;
        }
        params = X962NamedCurves.getByName(curveName);
        if (params == null) {
            params = SECNamedCurves.getByName(curveName);
        }
        if (params == null) {
            return NISTNamedCurves.getByName(curveName);
        }
        return params;
    }

    public static String getCurveName(ASN1ObjectIdentifier oid) {
        String name = X962NamedCurves.getName(oid);
        if (name != null) {
            return name;
        }
        name = SECNamedCurves.getName(oid);
        if (name == null) {
            return NISTNamedCurves.getName(oid);
        }
        return name;
    }
}
