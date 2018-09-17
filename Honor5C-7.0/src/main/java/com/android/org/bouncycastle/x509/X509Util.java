package com.android.org.bouncycastle.x509;

import com.android.org.bouncycastle.asn1.ASN1Encodable;
import com.android.org.bouncycastle.asn1.ASN1Encoding;
import com.android.org.bouncycastle.asn1.ASN1Integer;
import com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier;
import com.android.org.bouncycastle.asn1.DERNull;
import com.android.org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import com.android.org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import com.android.org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import com.android.org.bouncycastle.jce.X509Principal;
import com.android.org.bouncycastle.util.Strings;
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.security.auth.x500.X500Principal;

class X509Util {
    private static Hashtable algorithms;
    private static Set noParams;
    private static Hashtable params;

    static class Implementation {
        Object engine;
        Provider provider;

        Implementation(Object engine, Provider provider) {
            this.engine = engine;
            this.provider = provider;
        }

        Object getEngine() {
            return this.engine;
        }

        Provider getProvider() {
            return this.provider;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.x509.X509Util.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.x509.X509Util.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.x509.X509Util.<clinit>():void");
    }

    X509Util() {
    }

    private static RSASSAPSSparams creatPSSParams(AlgorithmIdentifier hashAlgId, int saltSize) {
        return new RSASSAPSSparams(hashAlgId, new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, hashAlgId), new ASN1Integer((long) saltSize), new ASN1Integer(1));
    }

    static ASN1ObjectIdentifier getAlgorithmOID(String algorithmName) {
        algorithmName = Strings.toUpperCase(algorithmName);
        if (algorithms.containsKey(algorithmName)) {
            return (ASN1ObjectIdentifier) algorithms.get(algorithmName);
        }
        return new ASN1ObjectIdentifier(algorithmName);
    }

    static AlgorithmIdentifier getSigAlgID(ASN1ObjectIdentifier sigOid, String algorithmName) {
        if (noParams.contains(sigOid)) {
            return new AlgorithmIdentifier(sigOid);
        }
        algorithmName = Strings.toUpperCase(algorithmName);
        if (params.containsKey(algorithmName)) {
            return new AlgorithmIdentifier(sigOid, (ASN1Encodable) params.get(algorithmName));
        }
        return new AlgorithmIdentifier(sigOid, DERNull.INSTANCE);
    }

    static Iterator getAlgNames() {
        Enumeration e = algorithms.keys();
        List l = new ArrayList();
        while (e.hasMoreElements()) {
            l.add(e.nextElement());
        }
        return l.iterator();
    }

    static Signature getSignatureInstance(String algorithm) throws NoSuchAlgorithmException {
        return Signature.getInstance(algorithm);
    }

    static Signature getSignatureInstance(String algorithm, String provider) throws NoSuchProviderException, NoSuchAlgorithmException {
        if (provider != null) {
            return Signature.getInstance(algorithm, provider);
        }
        return Signature.getInstance(algorithm);
    }

    static byte[] calculateSignature(ASN1ObjectIdentifier sigOid, String sigName, PrivateKey key, SecureRandom random, ASN1Encodable object) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (sigOid == null) {
            throw new IllegalStateException("no signature algorithm specified");
        }
        Signature sig = getSignatureInstance(sigName);
        if (random != null) {
            sig.initSign(key, random);
        } else {
            sig.initSign(key);
        }
        sig.update(object.toASN1Primitive().getEncoded(ASN1Encoding.DER));
        return sig.sign();
    }

    static byte[] calculateSignature(ASN1ObjectIdentifier sigOid, String sigName, String provider, PrivateKey key, SecureRandom random, ASN1Encodable object) throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        if (sigOid == null) {
            throw new IllegalStateException("no signature algorithm specified");
        }
        Signature sig = getSignatureInstance(sigName, provider);
        if (random != null) {
            sig.initSign(key, random);
        } else {
            sig.initSign(key);
        }
        sig.update(object.toASN1Primitive().getEncoded(ASN1Encoding.DER));
        return sig.sign();
    }

    static X509Principal convertPrincipal(X500Principal principal) {
        try {
            return new X509Principal(principal.getEncoded());
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot convert principal");
        }
    }

    static Implementation getImplementation(String baseName, String algorithm, Provider prov) throws NoSuchAlgorithmException {
        algorithm = Strings.toUpperCase(algorithm);
        while (true) {
            String alias = prov.getProperty("Alg.Alias." + baseName + "." + algorithm);
            if (alias == null) {
                break;
            }
            algorithm = alias;
        }
        String className = prov.getProperty(baseName + "." + algorithm);
        if (className != null) {
            try {
                Class cls;
                ClassLoader clsLoader = prov.getClass().getClassLoader();
                if (clsLoader != null) {
                    cls = clsLoader.loadClass(className);
                } else {
                    cls = Class.forName(className);
                }
                return new Implementation(cls.newInstance(), prov);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("algorithm " + algorithm + " in provider " + prov.getName() + " but no class \"" + className + "\" found!");
            } catch (Exception e2) {
                throw new IllegalStateException("algorithm " + algorithm + " in provider " + prov.getName() + " but class \"" + className + "\" inaccessible!");
            }
        }
        throw new NoSuchAlgorithmException("cannot find implementation " + algorithm + " for provider " + prov.getName());
    }

    static Implementation getImplementation(String baseName, String algorithm) throws NoSuchAlgorithmException {
        Provider[] prov = Security.getProviders();
        for (int i = 0; i != prov.length; i++) {
            Implementation imp = getImplementation(baseName, Strings.toUpperCase(algorithm), prov[i]);
            if (imp != null) {
                return imp;
            }
            try {
                imp = getImplementation(baseName, algorithm, prov[i]);
            } catch (NoSuchAlgorithmException e) {
            }
        }
        throw new NoSuchAlgorithmException("cannot find implementation " + algorithm);
    }

    static Provider getProvider(String provider) throws NoSuchProviderException {
        Provider prov = Security.getProvider(provider);
        if (prov != null) {
            return prov;
        }
        throw new NoSuchProviderException("Provider " + provider + " not found");
    }
}
