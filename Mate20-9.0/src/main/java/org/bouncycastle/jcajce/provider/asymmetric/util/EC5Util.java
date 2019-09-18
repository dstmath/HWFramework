package org.bouncycastle.jcajce.provider.asymmetric.util;

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
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jcajce.provider.config.ProviderConfiguration;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.field.FiniteField;
import org.bouncycastle.math.field.Polynomial;
import org.bouncycastle.math.field.PolynomialExtensionField;
import org.bouncycastle.util.Arrays;

public class EC5Util {
    private static Map customCurves = new HashMap();

    static {
        Enumeration names = CustomNamedCurves.getNames();
        while (names.hasMoreElements()) {
            String str = (String) names.nextElement();
            X9ECParameters byName = ECNamedCurveTable.getByName(str);
            if (byName != null) {
                customCurves.put(byName.getCurve(), CustomNamedCurves.getByName(str).getCurve());
            }
        }
        X9ECParameters byName2 = CustomNamedCurves.getByName("Curve25519");
        customCurves.put(new ECCurve.Fp(byName2.getCurve().getField().getCharacteristic(), byName2.getCurve().getA().toBigInteger(), byName2.getCurve().getB().toBigInteger()), byName2.getCurve());
    }

    public static EllipticCurve convertCurve(ECCurve eCCurve, byte[] bArr) {
        return new EllipticCurve(convertField(eCCurve.getField()), eCCurve.getA().toBigInteger(), eCCurve.getB().toBigInteger(), null);
    }

    public static ECCurve convertCurve(EllipticCurve ellipticCurve) {
        ECField field = ellipticCurve.getField();
        BigInteger a = ellipticCurve.getA();
        BigInteger b = ellipticCurve.getB();
        if (field instanceof ECFieldFp) {
            ECCurve fp = new ECCurve.Fp(((ECFieldFp) field).getP(), a, b);
            if (customCurves.containsKey(fp)) {
                fp = (ECCurve) customCurves.get(fp);
            }
            return fp;
        }
        ECFieldF2m eCFieldF2m = (ECFieldF2m) field;
        int m = eCFieldF2m.getM();
        int[] convertMidTerms = ECUtil.convertMidTerms(eCFieldF2m.getMidTermsOfReductionPolynomial());
        ECCurve.F2m f2m = new ECCurve.F2m(m, convertMidTerms[0], convertMidTerms[1], convertMidTerms[2], a, b);
        return f2m;
    }

    public static ECField convertField(FiniteField finiteField) {
        if (ECAlgorithms.isFpField(finiteField)) {
            return new ECFieldFp(finiteField.getCharacteristic());
        }
        Polynomial minimalPolynomial = ((PolynomialExtensionField) finiteField).getMinimalPolynomial();
        int[] exponentsPresent = minimalPolynomial.getExponentsPresent();
        return new ECFieldF2m(minimalPolynomial.getDegree(), Arrays.reverse(Arrays.copyOfRange(exponentsPresent, 1, exponentsPresent.length - 1)));
    }

    public static ECPoint convertPoint(org.bouncycastle.math.ec.ECPoint eCPoint) {
        org.bouncycastle.math.ec.ECPoint normalize = eCPoint.normalize();
        return new ECPoint(normalize.getAffineXCoord().toBigInteger(), normalize.getAffineYCoord().toBigInteger());
    }

    public static org.bouncycastle.math.ec.ECPoint convertPoint(ECParameterSpec eCParameterSpec, ECPoint eCPoint, boolean z) {
        return convertPoint(convertCurve(eCParameterSpec.getCurve()), eCPoint, z);
    }

    public static org.bouncycastle.math.ec.ECPoint convertPoint(ECCurve eCCurve, ECPoint eCPoint, boolean z) {
        return eCCurve.createPoint(eCPoint.getAffineX(), eCPoint.getAffineY());
    }

    public static ECParameterSpec convertSpec(EllipticCurve ellipticCurve, org.bouncycastle.jce.spec.ECParameterSpec eCParameterSpec) {
        if (!(eCParameterSpec instanceof ECNamedCurveParameterSpec)) {
            return new ECParameterSpec(ellipticCurve, convertPoint(eCParameterSpec.getG()), eCParameterSpec.getN(), eCParameterSpec.getH().intValue());
        }
        ECNamedCurveSpec eCNamedCurveSpec = new ECNamedCurveSpec(((ECNamedCurveParameterSpec) eCParameterSpec).getName(), ellipticCurve, convertPoint(eCParameterSpec.getG()), eCParameterSpec.getN(), eCParameterSpec.getH());
        return eCNamedCurveSpec;
    }

    public static org.bouncycastle.jce.spec.ECParameterSpec convertSpec(ECParameterSpec eCParameterSpec, boolean z) {
        ECCurve convertCurve = convertCurve(eCParameterSpec.getCurve());
        org.bouncycastle.jce.spec.ECParameterSpec eCParameterSpec2 = new org.bouncycastle.jce.spec.ECParameterSpec(convertCurve, convertPoint(convertCurve, eCParameterSpec.getGenerator(), z), eCParameterSpec.getOrder(), BigInteger.valueOf((long) eCParameterSpec.getCofactor()), eCParameterSpec.getCurve().getSeed());
        return eCParameterSpec2;
    }

    public static ECParameterSpec convertToSpec(X962Parameters x962Parameters, ECCurve eCCurve) {
        if (x962Parameters.isNamedCurve()) {
            ASN1ObjectIdentifier aSN1ObjectIdentifier = (ASN1ObjectIdentifier) x962Parameters.getParameters();
            X9ECParameters namedCurveByOid = ECUtil.getNamedCurveByOid(aSN1ObjectIdentifier);
            if (namedCurveByOid == null) {
                Map additionalECParameters = BouncyCastleProvider.CONFIGURATION.getAdditionalECParameters();
                if (!additionalECParameters.isEmpty()) {
                    namedCurveByOid = (X9ECParameters) additionalECParameters.get(aSN1ObjectIdentifier);
                }
            }
            ECNamedCurveSpec eCNamedCurveSpec = new ECNamedCurveSpec(ECUtil.getCurveName(aSN1ObjectIdentifier), convertCurve(eCCurve, namedCurveByOid.getSeed()), convertPoint(namedCurveByOid.getG()), namedCurveByOid.getN(), namedCurveByOid.getH());
            return eCNamedCurveSpec;
        } else if (x962Parameters.isImplicitlyCA()) {
            return null;
        } else {
            X9ECParameters instance = X9ECParameters.getInstance(x962Parameters.getParameters());
            EllipticCurve convertCurve = convertCurve(eCCurve, instance.getSeed());
            return instance.getH() != null ? new ECParameterSpec(convertCurve, convertPoint(instance.getG()), instance.getN(), instance.getH().intValue()) : new ECParameterSpec(convertCurve, convertPoint(instance.getG()), instance.getN(), 1);
        }
    }

    public static ECParameterSpec convertToSpec(X9ECParameters x9ECParameters) {
        return new ECParameterSpec(convertCurve(x9ECParameters.getCurve(), null), convertPoint(x9ECParameters.getG()), x9ECParameters.getN(), x9ECParameters.getH().intValue());
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v10, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: org.bouncycastle.asn1.x9.X9ECParameters} */
    /* JADX WARNING: Multi-variable type inference failed */
    public static ECCurve getCurve(ProviderConfiguration providerConfiguration, X962Parameters x962Parameters) {
        Set acceptableNamedCurves = providerConfiguration.getAcceptableNamedCurves();
        if (x962Parameters.isNamedCurve()) {
            ASN1ObjectIdentifier instance = ASN1ObjectIdentifier.getInstance(x962Parameters.getParameters());
            if (acceptableNamedCurves.isEmpty() || acceptableNamedCurves.contains(instance)) {
                X9ECParameters namedCurveByOid = ECUtil.getNamedCurveByOid(instance);
                if (namedCurveByOid == null) {
                    namedCurveByOid = providerConfiguration.getAdditionalECParameters().get(instance);
                }
                return namedCurveByOid.getCurve();
            }
            throw new IllegalStateException("named curve not acceptable");
        } else if (x962Parameters.isImplicitlyCA()) {
            return providerConfiguration.getEcImplicitlyCa().getCurve();
        } else {
            if (acceptableNamedCurves.isEmpty()) {
                return X9ECParameters.getInstance(x962Parameters.getParameters()).getCurve();
            }
            throw new IllegalStateException("encoded parameters not acceptable");
        }
    }

    public static ECDomainParameters getDomainParameters(ProviderConfiguration providerConfiguration, ECParameterSpec eCParameterSpec) {
        if (eCParameterSpec != null) {
            return ECUtil.getDomainParameters(providerConfiguration, convertSpec(eCParameterSpec, false));
        }
        org.bouncycastle.jce.spec.ECParameterSpec ecImplicitlyCa = providerConfiguration.getEcImplicitlyCa();
        ECDomainParameters eCDomainParameters = new ECDomainParameters(ecImplicitlyCa.getCurve(), ecImplicitlyCa.getG(), ecImplicitlyCa.getN(), ecImplicitlyCa.getH(), ecImplicitlyCa.getSeed());
        return eCDomainParameters;
    }
}
