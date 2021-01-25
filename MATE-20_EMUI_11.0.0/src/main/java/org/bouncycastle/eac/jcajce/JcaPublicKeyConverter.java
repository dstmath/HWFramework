package org.bouncycastle.eac.jcajce;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.eac.EACObjectIdentifiers;
import org.bouncycastle.asn1.eac.ECDSAPublicKey;
import org.bouncycastle.asn1.eac.PublicKeyDataObject;
import org.bouncycastle.asn1.eac.RSAPublicKey;
import org.bouncycastle.eac.EACException;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.math.field.FiniteField;
import org.bouncycastle.math.field.Polynomial;
import org.bouncycastle.math.field.PolynomialExtensionField;
import org.bouncycastle.util.Arrays;

public class JcaPublicKeyConverter {
    private EACHelper helper = new DefaultEACHelper();

    private static EllipticCurve convertCurve(ECCurve eCCurve) {
        return new EllipticCurve(convertField(eCCurve.getField()), eCCurve.getA().toBigInteger(), eCCurve.getB().toBigInteger(), null);
    }

    private static ECCurve convertCurve(EllipticCurve ellipticCurve, BigInteger bigInteger, int i) {
        ECField field = ellipticCurve.getField();
        BigInteger a = ellipticCurve.getA();
        BigInteger b = ellipticCurve.getB();
        if (field instanceof ECFieldFp) {
            return new ECCurve.Fp(((ECFieldFp) field).getP(), a, b, bigInteger, BigInteger.valueOf((long) i));
        }
        throw new IllegalStateException("not implemented yet!!!");
    }

    private static ECField convertField(FiniteField finiteField) {
        if (ECAlgorithms.isFpField(finiteField)) {
            return new ECFieldFp(finiteField.getCharacteristic());
        }
        Polynomial minimalPolynomial = ((PolynomialExtensionField) finiteField).getMinimalPolynomial();
        int[] exponentsPresent = minimalPolynomial.getExponentsPresent();
        return new ECFieldF2m(minimalPolynomial.getDegree(), Arrays.reverse(Arrays.copyOfRange(exponentsPresent, 1, exponentsPresent.length - 1)));
    }

    private static ECPoint convertPoint(ECCurve eCCurve, java.security.spec.ECPoint eCPoint) {
        return eCCurve.createPoint(eCPoint.getAffineX(), eCPoint.getAffineY());
    }

    private PublicKey getECPublicKeyPublicKey(ECDSAPublicKey eCDSAPublicKey) throws EACException, InvalidKeySpecException {
        try {
            return this.helper.createKeyFactory("ECDSA").generatePublic(new ECPublicKeySpec(getPublicPoint(eCDSAPublicKey), getParams(eCDSAPublicKey)));
        } catch (NoSuchProviderException e) {
            throw new EACException("cannot find provider: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e2) {
            throw new EACException("cannot find algorithm ECDSA: " + e2.getMessage(), e2);
        }
    }

    private ECParameterSpec getParams(ECDSAPublicKey eCDSAPublicKey) {
        if (eCDSAPublicKey.hasParameters()) {
            ECCurve.Fp fp = new ECCurve.Fp(eCDSAPublicKey.getPrimeModulusP(), eCDSAPublicKey.getFirstCoefA(), eCDSAPublicKey.getSecondCoefB(), eCDSAPublicKey.getOrderOfBasePointR(), eCDSAPublicKey.getCofactorF());
            ECPoint decodePoint = fp.decodePoint(eCDSAPublicKey.getBasePointG());
            return new ECParameterSpec(convertCurve(fp), new java.security.spec.ECPoint(decodePoint.getAffineXCoord().toBigInteger(), decodePoint.getAffineYCoord().toBigInteger()), eCDSAPublicKey.getOrderOfBasePointR(), eCDSAPublicKey.getCofactorF().intValue());
        }
        throw new IllegalArgumentException("Public key does not contains EC Params");
    }

    private java.security.spec.ECPoint getPublicPoint(ECDSAPublicKey eCDSAPublicKey) {
        if (eCDSAPublicKey.hasParameters()) {
            ECPoint.Fp fp = (ECPoint.Fp) new ECCurve.Fp(eCDSAPublicKey.getPrimeModulusP(), eCDSAPublicKey.getFirstCoefA(), eCDSAPublicKey.getSecondCoefB(), eCDSAPublicKey.getOrderOfBasePointR(), eCDSAPublicKey.getCofactorF()).decodePoint(eCDSAPublicKey.getPublicPointY());
            return new java.security.spec.ECPoint(fp.getAffineXCoord().toBigInteger(), fp.getAffineYCoord().toBigInteger());
        }
        throw new IllegalArgumentException("Public key does not contains EC Params");
    }

    public PublicKey getKey(PublicKeyDataObject publicKeyDataObject) throws EACException, InvalidKeySpecException {
        if (publicKeyDataObject.getUsage().on(EACObjectIdentifiers.id_TA_ECDSA)) {
            return getECPublicKeyPublicKey((ECDSAPublicKey) publicKeyDataObject);
        }
        RSAPublicKey rSAPublicKey = (RSAPublicKey) publicKeyDataObject;
        try {
            return this.helper.createKeyFactory("RSA").generatePublic(new RSAPublicKeySpec(rSAPublicKey.getModulus(), rSAPublicKey.getPublicExponent()));
        } catch (NoSuchProviderException e) {
            throw new EACException("cannot find provider: " + e.getMessage(), e);
        } catch (NoSuchAlgorithmException e2) {
            throw new EACException("cannot find algorithm ECDSA: " + e2.getMessage(), e2);
        }
    }

    public PublicKeyDataObject getPublicKeyDataObject(ASN1ObjectIdentifier aSN1ObjectIdentifier, PublicKey publicKey) {
        if (publicKey instanceof java.security.interfaces.RSAPublicKey) {
            java.security.interfaces.RSAPublicKey rSAPublicKey = (java.security.interfaces.RSAPublicKey) publicKey;
            return new RSAPublicKey(aSN1ObjectIdentifier, rSAPublicKey.getModulus(), rSAPublicKey.getPublicExponent());
        }
        ECPublicKey eCPublicKey = (ECPublicKey) publicKey;
        ECParameterSpec params = eCPublicKey.getParams();
        EllipticCurve curve = params.getCurve();
        ECCurve convertCurve = convertCurve(curve, params.getOrder(), params.getCofactor());
        return new ECDSAPublicKey(aSN1ObjectIdentifier, ((ECFieldFp) curve.getField()).getP(), curve.getA(), curve.getB(), convertPoint(convertCurve, params.getGenerator()).getEncoded(false), params.getOrder(), convertPoint(convertCurve, eCPublicKey.getW()).getEncoded(false), params.getCofactor());
    }

    public JcaPublicKeyConverter setProvider(String str) {
        this.helper = new NamedEACHelper(str);
        return this;
    }

    public JcaPublicKeyConverter setProvider(Provider provider) {
        this.helper = new ProviderEACHelper(provider);
        return this;
    }
}
