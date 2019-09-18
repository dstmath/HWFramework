package org.bouncycastle.x509;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.cryptopro.CryptoProObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import org.bouncycastle.asn1.teletrust.TeleTrusTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.jce.X509Principal;
import org.bouncycastle.util.Strings;

class X509Util {
    private static Hashtable algorithms = new Hashtable();
    private static Set noParams = new HashSet();
    private static Hashtable params = new Hashtable();

    static class Implementation {
        Object engine;
        Provider provider;

        Implementation(Object obj, Provider provider2) {
            this.engine = obj;
            this.provider = provider2;
        }

        /* access modifiers changed from: package-private */
        public Object getEngine() {
            return this.engine;
        }

        /* access modifiers changed from: package-private */
        public Provider getProvider() {
            return this.provider;
        }
    }

    static {
        algorithms.put("MD2WITHRSAENCRYPTION", PKCSObjectIdentifiers.md2WithRSAEncryption);
        algorithms.put("MD2WITHRSA", PKCSObjectIdentifiers.md2WithRSAEncryption);
        algorithms.put("MD5WITHRSAENCRYPTION", PKCSObjectIdentifiers.md5WithRSAEncryption);
        algorithms.put("MD5WITHRSA", PKCSObjectIdentifiers.md5WithRSAEncryption);
        algorithms.put("SHA1WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha1WithRSAEncryption);
        algorithms.put("SHA1WITHRSA", PKCSObjectIdentifiers.sha1WithRSAEncryption);
        algorithms.put("SHA224WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha224WithRSAEncryption);
        algorithms.put("SHA224WITHRSA", PKCSObjectIdentifiers.sha224WithRSAEncryption);
        algorithms.put("SHA256WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha256WithRSAEncryption);
        algorithms.put("SHA256WITHRSA", PKCSObjectIdentifiers.sha256WithRSAEncryption);
        algorithms.put("SHA384WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha384WithRSAEncryption);
        algorithms.put("SHA384WITHRSA", PKCSObjectIdentifiers.sha384WithRSAEncryption);
        algorithms.put("SHA512WITHRSAENCRYPTION", PKCSObjectIdentifiers.sha512WithRSAEncryption);
        algorithms.put("SHA512WITHRSA", PKCSObjectIdentifiers.sha512WithRSAEncryption);
        algorithms.put("SHA1WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA224WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA256WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA384WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("SHA512WITHRSAANDMGF1", PKCSObjectIdentifiers.id_RSASSA_PSS);
        algorithms.put("RIPEMD160WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd160);
        algorithms.put("RIPEMD160WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd160);
        algorithms.put("RIPEMD128WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd128);
        algorithms.put("RIPEMD128WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd128);
        algorithms.put("RIPEMD256WITHRSAENCRYPTION", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd256);
        algorithms.put("RIPEMD256WITHRSA", TeleTrusTObjectIdentifiers.rsaSignatureWithripemd256);
        algorithms.put("SHA1WITHDSA", X9ObjectIdentifiers.id_dsa_with_sha1);
        algorithms.put("DSAWITHSHA1", X9ObjectIdentifiers.id_dsa_with_sha1);
        algorithms.put("SHA224WITHDSA", NISTObjectIdentifiers.dsa_with_sha224);
        algorithms.put("SHA256WITHDSA", NISTObjectIdentifiers.dsa_with_sha256);
        algorithms.put("SHA384WITHDSA", NISTObjectIdentifiers.dsa_with_sha384);
        algorithms.put("SHA512WITHDSA", NISTObjectIdentifiers.dsa_with_sha512);
        algorithms.put("SHA1WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA1);
        algorithms.put("ECDSAWITHSHA1", X9ObjectIdentifiers.ecdsa_with_SHA1);
        algorithms.put("SHA224WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA224);
        algorithms.put("SHA256WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA256);
        algorithms.put("SHA384WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA384);
        algorithms.put("SHA512WITHECDSA", X9ObjectIdentifiers.ecdsa_with_SHA512);
        algorithms.put("GOST3411WITHGOST3410", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        algorithms.put("GOST3411WITHGOST3410-94", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        algorithms.put("GOST3411WITHECGOST3410", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        algorithms.put("GOST3411WITHECGOST3410-2001", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        algorithms.put("GOST3411WITHGOST3410-2001", CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA1);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA224);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA256);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA384);
        noParams.add(X9ObjectIdentifiers.ecdsa_with_SHA512);
        noParams.add(X9ObjectIdentifiers.id_dsa_with_sha1);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha224);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha256);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha384);
        noParams.add(NISTObjectIdentifiers.dsa_with_sha512);
        noParams.add(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_94);
        noParams.add(CryptoProObjectIdentifiers.gostR3411_94_with_gostR3410_2001);
        params.put("SHA1WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1, DERNull.INSTANCE), 20));
        params.put("SHA224WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha224, DERNull.INSTANCE), 28));
        params.put("SHA256WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256, DERNull.INSTANCE), 32));
        params.put("SHA384WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha384, DERNull.INSTANCE), 48));
        params.put("SHA512WITHRSAANDMGF1", creatPSSParams(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha512, DERNull.INSTANCE), 64));
    }

    X509Util() {
    }

    static byte[] calculateSignature(ASN1ObjectIdentifier aSN1ObjectIdentifier, String str, String str2, PrivateKey privateKey, SecureRandom secureRandom, ASN1Encodable aSN1Encodable) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (aSN1ObjectIdentifier != null) {
            Signature signatureInstance = getSignatureInstance(str, str2);
            if (secureRandom != null) {
                signatureInstance.initSign(privateKey, secureRandom);
            } else {
                signatureInstance.initSign(privateKey);
            }
            signatureInstance.update(aSN1Encodable.toASN1Primitive().getEncoded(ASN1Encoding.DER));
            return signatureInstance.sign();
        }
        throw new IllegalStateException("no signature algorithm specified");
    }

    static byte[] calculateSignature(ASN1ObjectIdentifier aSN1ObjectIdentifier, String str, PrivateKey privateKey, SecureRandom secureRandom, ASN1Encodable aSN1Encodable) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (aSN1ObjectIdentifier != null) {
            Signature signatureInstance = getSignatureInstance(str);
            if (secureRandom != null) {
                signatureInstance.initSign(privateKey, secureRandom);
            } else {
                signatureInstance.initSign(privateKey);
            }
            signatureInstance.update(aSN1Encodable.toASN1Primitive().getEncoded(ASN1Encoding.DER));
            return signatureInstance.sign();
        }
        throw new IllegalStateException("no signature algorithm specified");
    }

    static X509Principal convertPrincipal(X500Principal x500Principal) {
        try {
            return new X509Principal(x500Principal.getEncoded());
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot convert principal");
        }
    }

    private static RSASSAPSSparams creatPSSParams(AlgorithmIdentifier algorithmIdentifier, int i) {
        return new RSASSAPSSparams(algorithmIdentifier, new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, algorithmIdentifier), new ASN1Integer((long) i), new ASN1Integer(1));
    }

    static Iterator getAlgNames() {
        Enumeration keys = algorithms.keys();
        ArrayList arrayList = new ArrayList();
        while (keys.hasMoreElements()) {
            arrayList.add(keys.nextElement());
        }
        return arrayList.iterator();
    }

    static ASN1ObjectIdentifier getAlgorithmOID(String str) {
        String upperCase = Strings.toUpperCase(str);
        return algorithms.containsKey(upperCase) ? (ASN1ObjectIdentifier) algorithms.get(upperCase) : new ASN1ObjectIdentifier(upperCase);
    }

    static Implementation getImplementation(String str, String str2) throws NoSuchAlgorithmException {
        Provider[] providers = Security.getProviders();
        for (int i = 0; i != providers.length; i++) {
            Implementation implementation = getImplementation(str, Strings.toUpperCase(str2), providers[i]);
            if (implementation != null) {
                return implementation;
            }
            try {
                getImplementation(str, str2, providers[i]);
            } catch (NoSuchAlgorithmException e) {
            }
        }
        throw new NoSuchAlgorithmException("cannot find implementation " + str2);
    }

    static Implementation getImplementation(String str, String str2, Provider provider) throws NoSuchAlgorithmException {
        String upperCase = Strings.toUpperCase(str2);
        while (true) {
            String property = provider.getProperty("Alg.Alias." + str + "." + upperCase);
            if (property == null) {
                break;
            }
            upperCase = property;
        }
        String property2 = provider.getProperty(str + "." + upperCase);
        if (property2 != null) {
            try {
                ClassLoader classLoader = provider.getClass().getClassLoader();
                return new Implementation((classLoader != null ? classLoader.loadClass(property2) : Class.forName(property2)).newInstance(), provider);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("algorithm " + upperCase + " in provider " + provider.getName() + " but no class \"" + property2 + "\" found!");
            } catch (Exception e2) {
                throw new IllegalStateException("algorithm " + upperCase + " in provider " + provider.getName() + " but class \"" + property2 + "\" inaccessible!");
            }
        } else {
            throw new NoSuchAlgorithmException("cannot find implementation " + upperCase + " for provider " + provider.getName());
        }
    }

    static Provider getProvider(String str) throws NoSuchProviderException {
        Provider provider = Security.getProvider(str);
        if (provider != null) {
            return provider;
        }
        throw new NoSuchProviderException("Provider " + str + " not found");
    }

    static AlgorithmIdentifier getSigAlgID(ASN1ObjectIdentifier aSN1ObjectIdentifier, String str) {
        if (noParams.contains(aSN1ObjectIdentifier)) {
            return new AlgorithmIdentifier(aSN1ObjectIdentifier);
        }
        String upperCase = Strings.toUpperCase(str);
        return params.containsKey(upperCase) ? new AlgorithmIdentifier(aSN1ObjectIdentifier, (ASN1Encodable) params.get(upperCase)) : new AlgorithmIdentifier(aSN1ObjectIdentifier, DERNull.INSTANCE);
    }

    static Signature getSignatureInstance(String str) throws NoSuchAlgorithmException {
        return Signature.getInstance(str);
    }

    static Signature getSignatureInstance(String str, String str2) throws NoSuchProviderException, NoSuchAlgorithmException {
        return str2 != null ? Signature.getInstance(str, str2) : Signature.getInstance(str);
    }
}
