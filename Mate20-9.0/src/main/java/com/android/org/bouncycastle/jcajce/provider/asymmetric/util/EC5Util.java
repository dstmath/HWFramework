package com.android.org.bouncycastle.jcajce.provider.asymmetric.util;

import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.x9.ECNamedCurveTable;
import com.android.org.bouncycastle.asn1.x9.X962Parameters;
import com.android.org.bouncycastle.asn1.x9.X9ECParameters;
import com.android.org.bouncycastle.crypto.ec.CustomNamedCurves;
import com.android.org.bouncycastle.crypto.params.ECDomainParameters;
import com.android.org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import com.android.org.bouncycastle.jce.provider.BouncyCastleProvider;
import com.android.org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import com.android.org.bouncycastle.jce.spec.ECNamedCurveSpec;
import com.android.org.bouncycastle.math.ec.ECAlgorithms;
import com.android.org.bouncycastle.math.ec.ECCurve;
import com.android.org.bouncycastle.math.field.FiniteField;
import com.android.org.bouncycastle.math.field.Polynomial;
import com.android.org.bouncycastle.math.field.PolynomialExtensionField;
import com.android.org.bouncycastle.util.Arrays;
import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EC5Util {
    private static Map customCurves = new HashMap();

    static {
        Enumeration e = CustomNamedCurves.getNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            X9ECParameters curveParams = ECNamedCurveTable.getByName(name);
            if (curveParams != null) {
                customCurves.put(curveParams.getCurve(), CustomNamedCurves.getByName(name).getCurve());
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v8, resolved type: com.android.org.bouncycastle.asn1.x9.X9ECParameters} */
    /* JADX WARNING: Multi-variable type inference failed */
    public static ECCurve getCurve(ProviderConfiguration configuration, X962Parameters params) {
        ECCurve curve;
        Set acceptableCurves = configuration.getAcceptableNamedCurves();
        if (params.isNamedCurve()) {
            ASN1ObjectIdentifier oid = ASN1ObjectIdentifier.getInstance(params.getParameters());
            if (acceptableCurves.isEmpty() || acceptableCurves.contains(oid)) {
                X9ECParameters ecP = ECUtil.getNamedCurveByOid(oid);
                if (ecP == null) {
                    ecP = configuration.getAdditionalECParameters().get(oid);
                }
                curve = ecP.getCurve();
            } else {
                throw new IllegalStateException("named curve not acceptable");
            }
        } else if (params.isImplicitlyCA()) {
            curve = configuration.getEcImplicitlyCa().getCurve();
        } else if (acceptableCurves.isEmpty()) {
            curve = X9ECParameters.getInstance(params.getParameters()).getCurve();
        } else {
            throw new IllegalStateException("encoded parameters not acceptable");
        }
        return curve;
    }

    public static ECDomainParameters getDomainParameters(ProviderConfiguration configuration, ECParameterSpec params) {
        if (params != null) {
            return ECUtil.getDomainParameters(configuration, convertSpec(params, false));
        }
        com.android.org.bouncycastle.jce.spec.ECParameterSpec iSpec = configuration.getEcImplicitlyCa();
        ECDomainParameters eCDomainParameters = new ECDomainParameters(iSpec.getCurve(), iSpec.getG(), iSpec.getN(), iSpec.getH(), iSpec.getSeed());
        return eCDomainParameters;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: com.android.org.bouncycastle.asn1.x9.X9ECParameters} */
    /* JADX WARNING: Multi-variable type inference failed */
    public static ECParameterSpec convertToSpec(X962Parameters params, ECCurve curve) {
        ECParameterSpec ecSpec;
        if (params.isNamedCurve()) {
            ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) params.getParameters();
            X9ECParameters ecP = ECUtil.getNamedCurveByOid(oid);
            if (ecP == null) {
                Map additionalECParameters = BouncyCastleProvider.CONFIGURATION.getAdditionalECParameters();
                if (!additionalECParameters.isEmpty()) {
                    ecP = additionalECParameters.get(oid);
                }
            }
            ECNamedCurveSpec ecSpec2 = new ECNamedCurveSpec(ECUtil.getCurveName(oid), convertCurve(curve, ecP.getSeed()), new ECPoint(ecP.getG().getAffineXCoord().toBigInteger(), ecP.getG().getAffineYCoord().toBigInteger()), ecP.getN(), ecP.getH());
            return ecSpec2;
        } else if (params.isImplicitlyCA()) {
            return null;
        } else {
            X9ECParameters ecP2 = X9ECParameters.getInstance(params.getParameters());
            EllipticCurve ellipticCurve = convertCurve(curve, ecP2.getSeed());
            if (ecP2.getH() != null) {
                ecSpec = new ECParameterSpec(ellipticCurve, new ECPoint(ecP2.getG().getAffineXCoord().toBigInteger(), ecP2.getG().getAffineYCoord().toBigInteger()), ecP2.getN(), ecP2.getH().intValue());
            } else {
                ecSpec = new ECParameterSpec(ellipticCurve, new ECPoint(ecP2.getG().getAffineXCoord().toBigInteger(), ecP2.getG().getAffineYCoord().toBigInteger()), ecP2.getN(), 1);
            }
            return ecSpec;
        }
    }

    public static ECParameterSpec convertToSpec(X9ECParameters domainParameters) {
        return new ECParameterSpec(convertCurve(domainParameters.getCurve(), null), new ECPoint(domainParameters.getG().getAffineXCoord().toBigInteger(), domainParameters.getG().getAffineYCoord().toBigInteger()), domainParameters.getN(), domainParameters.getH().intValue());
    }

    public static EllipticCurve convertCurve(ECCurve curve, byte[] seed) {
        return new EllipticCurve(convertField(curve.getField()), curve.getA().toBigInteger(), curve.getB().toBigInteger(), null);
    }

    public static ECCurve convertCurve(EllipticCurve ec) {
        ECField field = ec.getField();
        BigInteger a = ec.getA();
        BigInteger b = ec.getB();
        if (field instanceof ECFieldFp) {
            ECCurve.Fp curve = new ECCurve.Fp(((ECFieldFp) field).getP(), a, b);
            if (customCurves.containsKey(curve)) {
                return (ECCurve) customCurves.get(curve);
            }
            return curve;
        }
        ECFieldF2m fieldF2m = (ECFieldF2m) field;
        int m = fieldF2m.getM();
        int[] ks = ECUtil.convertMidTerms(fieldF2m.getMidTermsOfReductionPolynomial());
        ECCurve.F2m f2m = new ECCurve.F2m(m, ks[0], ks[1], ks[2], a, b);
        return f2m;
    }

    public static ECField convertField(FiniteField field) {
        if (ECAlgorithms.isFpField(field)) {
            return new ECFieldFp(field.getCharacteristic());
        }
        Polynomial poly = ((PolynomialExtensionField) field).getMinimalPolynomial();
        int[] exponents = poly.getExponentsPresent();
        return new ECFieldF2m(poly.getDegree(), Arrays.reverse(Arrays.copyOfRange(exponents, 1, exponents.length - 1)));
    }

    public static ECParameterSpec convertSpec(EllipticCurve ellipticCurve, com.android.org.bouncycastle.jce.spec.ECParameterSpec spec) {
        if (!(spec instanceof ECNamedCurveParameterSpec)) {
            return new ECParameterSpec(ellipticCurve, new ECPoint(spec.getG().getAffineXCoord().toBigInteger(), spec.getG().getAffineYCoord().toBigInteger()), spec.getN(), spec.getH().intValue());
        }
        ECNamedCurveSpec eCNamedCurveSpec = new ECNamedCurveSpec(((ECNamedCurveParameterSpec) spec).getName(), ellipticCurve, new ECPoint(spec.getG().getAffineXCoord().toBigInteger(), spec.getG().getAffineYCoord().toBigInteger()), spec.getN(), spec.getH());
        return eCNamedCurveSpec;
    }

    public static com.android.org.bouncycastle.jce.spec.ECParameterSpec convertSpec(ECParameterSpec ecSpec, boolean withCompression) {
        ECCurve curve = convertCurve(ecSpec.getCurve());
        com.android.org.bouncycastle.jce.spec.ECParameterSpec eCParameterSpec = new com.android.org.bouncycastle.jce.spec.ECParameterSpec(curve, convertPoint(curve, ecSpec.getGenerator(), withCompression), ecSpec.getOrder(), BigInteger.valueOf((long) ecSpec.getCofactor()), ecSpec.getCurve().getSeed());
        return eCParameterSpec;
    }

    public static com.android.org.bouncycastle.math.ec.ECPoint convertPoint(ECParameterSpec ecSpec, ECPoint point, boolean withCompression) {
        return convertPoint(convertCurve(ecSpec.getCurve()), point, withCompression);
    }

    public static com.android.org.bouncycastle.math.ec.ECPoint convertPoint(ECCurve curve, ECPoint point, boolean withCompression) {
        return curve.createPoint(point.getAffineX(), point.getAffineY());
    }
}
