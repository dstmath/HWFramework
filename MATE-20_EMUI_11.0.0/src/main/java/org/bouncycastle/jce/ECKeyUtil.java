package org.bouncycastle.jce;

import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X962Parameters;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9ECPoint;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class ECKeyUtil {

    /* access modifiers changed from: private */
    public static class UnexpectedException extends RuntimeException {
        private Throwable cause;

        UnexpectedException(Throwable th) {
            super(th.toString());
            this.cause = th;
        }

        @Override // java.lang.Throwable
        public Throwable getCause() {
            return this.cause;
        }
    }

    public static PrivateKey privateToExplicitParameters(PrivateKey privateKey, String str) throws IllegalArgumentException, NoSuchAlgorithmException, NoSuchProviderException {
        Provider provider = Security.getProvider(str);
        if (provider != null) {
            return privateToExplicitParameters(privateKey, provider);
        }
        throw new NoSuchProviderException("cannot find provider: " + str);
    }

    public static PrivateKey privateToExplicitParameters(PrivateKey privateKey, Provider provider) throws IllegalArgumentException, NoSuchAlgorithmException {
        X9ECParameters x9ECParameters;
        try {
            PrivateKeyInfo instance = PrivateKeyInfo.getInstance(ASN1Primitive.fromByteArray(privateKey.getEncoded()));
            if (!instance.getPrivateKeyAlgorithm().getAlgorithm().equals((ASN1Primitive) CryptoProObjectIdentifiers.gostR3410_2001)) {
                X962Parameters instance2 = X962Parameters.getInstance(instance.getPrivateKeyAlgorithm().getParameters());
                if (instance2.isNamedCurve()) {
                    x9ECParameters = ECUtil.getNamedCurveByOid(ASN1ObjectIdentifier.getInstance(instance2.getParameters()));
                    if (x9ECParameters.hasSeed()) {
                        x9ECParameters = new X9ECParameters(x9ECParameters.getCurve(), x9ECParameters.getBaseEntry(), x9ECParameters.getN(), x9ECParameters.getH());
                    }
                } else if (!instance2.isImplicitlyCA()) {
                    return privateKey;
                } else {
                    x9ECParameters = new X9ECParameters(BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa().getCurve(), new X9ECPoint(BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa().getG(), false), BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa().getN(), BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa().getH());
                }
                return KeyFactory.getInstance(privateKey.getAlgorithm(), provider).generatePrivate(new PKCS8EncodedKeySpec(new PrivateKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, new X962Parameters(x9ECParameters)), instance.parsePrivateKey()).getEncoded()));
            }
            throw new UnsupportedEncodingException("cannot convert GOST key to explicit parameters.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (NoSuchAlgorithmException e2) {
            throw e2;
        } catch (Exception e3) {
            throw new UnexpectedException(e3);
        }
    }

    public static PublicKey publicToExplicitParameters(PublicKey publicKey, String str) throws IllegalArgumentException, NoSuchAlgorithmException, NoSuchProviderException {
        Provider provider = Security.getProvider(str);
        if (provider != null) {
            return publicToExplicitParameters(publicKey, provider);
        }
        throw new NoSuchProviderException("cannot find provider: " + str);
    }

    public static PublicKey publicToExplicitParameters(PublicKey publicKey, Provider provider) throws IllegalArgumentException, NoSuchAlgorithmException {
        X9ECParameters x9ECParameters;
        try {
            SubjectPublicKeyInfo instance = SubjectPublicKeyInfo.getInstance(ASN1Primitive.fromByteArray(publicKey.getEncoded()));
            if (!instance.getAlgorithm().getAlgorithm().equals((ASN1Primitive) CryptoProObjectIdentifiers.gostR3410_2001)) {
                X962Parameters instance2 = X962Parameters.getInstance(instance.getAlgorithm().getParameters());
                if (instance2.isNamedCurve()) {
                    x9ECParameters = ECUtil.getNamedCurveByOid(ASN1ObjectIdentifier.getInstance(instance2.getParameters()));
                    if (x9ECParameters.hasSeed()) {
                        x9ECParameters = new X9ECParameters(x9ECParameters.getCurve(), x9ECParameters.getBaseEntry(), x9ECParameters.getN(), x9ECParameters.getH());
                    }
                } else if (!instance2.isImplicitlyCA()) {
                    return publicKey;
                } else {
                    x9ECParameters = new X9ECParameters(BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa().getCurve(), new X9ECPoint(BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa().getG(), false), BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa().getN(), BouncyCastleProvider.CONFIGURATION.getEcImplicitlyCa().getH());
                }
                return KeyFactory.getInstance(publicKey.getAlgorithm(), provider).generatePublic(new X509EncodedKeySpec(new SubjectPublicKeyInfo(new AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, new X962Parameters(x9ECParameters)), instance.getPublicKeyData().getBytes()).getEncoded()));
            }
            throw new IllegalArgumentException("cannot convert GOST key to explicit parameters.");
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (NoSuchAlgorithmException e2) {
            throw e2;
        } catch (Exception e3) {
            throw new UnexpectedException(e3);
        }
    }
}
