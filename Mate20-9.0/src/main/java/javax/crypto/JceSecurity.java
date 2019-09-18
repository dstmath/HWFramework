package javax.crypto;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import sun.security.jca.GetInstance;

final class JceSecurity {
    /* access modifiers changed from: private */
    public static final URL NULL_URL;
    private static final Object PROVIDER_VERIFIED = Boolean.TRUE;
    static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<Class<?>, URL> codeBaseCacheRef = new WeakHashMap();
    private static CryptoPermissions defaultPolicy = null;
    private static CryptoPermissions exemptPolicy = null;
    private static final Map<Provider, Object> verificationResults = new IdentityHashMap();
    private static final Map<Provider, Object> verifyingProviders = new IdentityHashMap();

    static {
        try {
            NULL_URL = new URL("http://null.oracle.com/");
        } catch (Exception e) {
            throw new RuntimeException((Throwable) e);
        }
    }

    private JceSecurity() {
    }

    static GetInstance.Instance getInstance(String type, Class<?> clazz, String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Provider.Service s = GetInstance.getService(type, algorithm, provider);
        Exception ve = getVerificationResult(s.getProvider());
        if (ve == null) {
            return GetInstance.getInstance(s, clazz);
        }
        throw ((NoSuchProviderException) new NoSuchProviderException("JCE cannot authenticate the provider " + provider).initCause(ve));
    }

    static GetInstance.Instance getInstance(String type, Class<?> clazz, String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Provider.Service s = GetInstance.getService(type, algorithm, provider);
        Exception ve = getVerificationResult(provider);
        if (ve == null) {
            return GetInstance.getInstance(s, clazz);
        }
        throw new SecurityException("JCE cannot authenticate the provider " + provider.getName(), ve);
    }

    static GetInstance.Instance getInstance(String type, Class<?> clazz, String algorithm) throws NoSuchAlgorithmException {
        NoSuchAlgorithmException failure = null;
        for (Provider.Service s : GetInstance.getServices(type, algorithm)) {
            if (canUseProvider(s.getProvider())) {
                try {
                    return GetInstance.getInstance(s, clazz);
                } catch (NoSuchAlgorithmException e) {
                    failure = e;
                }
            }
        }
        throw new NoSuchAlgorithmException("Algorithm " + algorithm + " not available", failure);
    }

    static CryptoPermissions verifyExemptJar(URL codeBase) throws Exception {
        JarVerifier jv = new JarVerifier(codeBase, true);
        jv.verify();
        return jv.getPermissions();
    }

    static void verifyProviderJar(URL codeBase) throws Exception {
        new JarVerifier(codeBase, false).verify();
    }

    static synchronized Exception getVerificationResult(Provider p) {
        synchronized (JceSecurity.class) {
            Object o = verificationResults.get(p);
            if (o == PROVIDER_VERIFIED) {
                return null;
            }
            if (o != null) {
                Exception exc = (Exception) o;
                return exc;
            } else if (verifyingProviders.get(p) != null) {
                NoSuchProviderException noSuchProviderException = new NoSuchProviderException("Recursion during verification");
                return noSuchProviderException;
            } else {
                try {
                    verifyingProviders.put(p, Boolean.FALSE);
                    verifyProviderJar(getCodeBase(p.getClass()));
                    verificationResults.put(p, PROVIDER_VERIFIED);
                    verifyingProviders.remove(p);
                    return null;
                } catch (Exception e) {
                    try {
                        verificationResults.put(p, e);
                        return e;
                    } finally {
                        verifyingProviders.remove(p);
                    }
                }
            }
        }
    }

    static boolean canUseProvider(Provider p) {
        return true;
    }

    static URL getCodeBase(final Class<?> clazz) {
        URL url;
        synchronized (codeBaseCacheRef) {
            URL url2 = codeBaseCacheRef.get(clazz);
            if (url2 == null) {
                url2 = (URL) AccessController.doPrivileged(new PrivilegedAction<URL>() {
                    public URL run() {
                        ProtectionDomain pd = Class.this.getProtectionDomain();
                        if (pd != null) {
                            CodeSource cs = pd.getCodeSource();
                            if (cs != null) {
                                return cs.getLocation();
                            }
                        }
                        return JceSecurity.NULL_URL;
                    }
                });
                codeBaseCacheRef.put(clazz, url2);
            }
            url = url2 == NULL_URL ? null : url2;
        }
        return url;
    }

    private static void loadPolicies(File jarPathName, CryptoPermissions defaultPolicy2, CryptoPermissions exemptPolicy2) throws Exception {
        InputStream is;
        JarFile jf = new JarFile(jarPathName);
        Enumeration<JarEntry> entries = jf.entries();
        while (entries.hasMoreElements()) {
            JarEntry je = entries.nextElement();
            InputStream is2 = null;
            try {
                if (je.getName().startsWith("default_")) {
                    is = jf.getInputStream(je);
                    defaultPolicy2.load(is);
                } else if (je.getName().startsWith("exempt_")) {
                    is = jf.getInputStream(je);
                    exemptPolicy2.load(is);
                } else if (is2 != null) {
                    is2.close();
                }
                JarVerifier.verifyPolicySigned(je.getCertificates());
            } finally {
                if (is2 != null) {
                    is2.close();
                }
            }
        }
        jf.close();
    }

    static CryptoPermissions getDefaultPolicy() {
        return defaultPolicy;
    }

    static CryptoPermissions getExemptPolicy() {
        return exemptPolicy;
    }
}
