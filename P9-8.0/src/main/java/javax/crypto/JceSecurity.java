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
import java.security.Provider.Service;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;

final class JceSecurity {
    private static final URL NULL_URL;
    private static final Object PROVIDER_VERIFIED = Boolean.TRUE;
    static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<Class<?>, URL> codeBaseCacheRef = new WeakHashMap();
    private static CryptoPermissions defaultPolicy = null;
    private static CryptoPermissions exemptPolicy = null;
    private static boolean isRestricted = true;
    private static final Map<Provider, Object> verificationResults = new IdentityHashMap();
    private static final Map<Provider, Object> verifyingProviders = new IdentityHashMap();

    static {
        try {
            NULL_URL = new URL("http://null.sun.com/");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private JceSecurity() {
    }

    static Instance getInstance(String type, Class<?> clazz, String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Service s = GetInstance.getService(type, algorithm, provider);
        Exception ve = getVerificationResult(s.getProvider());
        if (ve == null) {
            return GetInstance.getInstance(s, clazz);
        }
        throw ((NoSuchProviderException) new NoSuchProviderException("JCE cannot authenticate the provider " + provider).initCause(ve));
    }

    static Instance getInstance(String type, Class<?> clazz, String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Service s = GetInstance.getService(type, algorithm, provider);
        Exception ve = getVerificationResult(provider);
        if (ve == null) {
            return GetInstance.getInstance(s, clazz);
        }
        throw new SecurityException("JCE cannot authenticate the provider " + provider.getName(), ve);
    }

    static Instance getInstance(String type, Class<?> clazz, String algorithm) throws NoSuchAlgorithmException {
        Throwable failure = null;
        for (Service s : GetInstance.getServices(type, algorithm)) {
            if (canUseProvider(s.getProvider())) {
                try {
                    return GetInstance.getInstance(s, clazz);
                } catch (Throwable e) {
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
            } else if (o != null) {
                Exception exception = (Exception) o;
                return exception;
            } else if (verifyingProviders.get(p) != null) {
                Exception noSuchProviderException = new NoSuchProviderException("Recursion during verification");
                return noSuchProviderException;
            } else {
                try {
                    verifyingProviders.put(p, Boolean.FALSE);
                    verifyProviderJar(getCodeBase(p.getClass()));
                    verificationResults.put(p, PROVIDER_VERIFIED);
                    verifyingProviders.remove(p);
                    return null;
                } catch (Exception e) {
                    verificationResults.put(p, e);
                    verifyingProviders.remove(p);
                    return e;
                } catch (Throwable th) {
                    verifyingProviders.remove(p);
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
            url = (URL) codeBaseCacheRef.get(clazz);
            if (url == null) {
                url = (URL) AccessController.doPrivileged(new PrivilegedAction<URL>() {
                    public URL run() {
                        ProtectionDomain pd = clazz.getProtectionDomain();
                        if (pd != null) {
                            CodeSource cs = pd.getCodeSource();
                            if (cs != null) {
                                return cs.getLocation();
                            }
                        }
                        return JceSecurity.NULL_URL;
                    }
                });
                codeBaseCacheRef.put(clazz, url);
            }
            if (url == NULL_URL) {
                url = null;
            }
        }
        return url;
    }

    private static void setupJurisdictionPolicies() throws Exception {
        String javaHomeDir = System.getProperty("java.home");
        String sep = File.separator;
        String pathToPolicyJar = javaHomeDir + sep + "lib" + sep + "security" + sep;
        File exportJar = new File(pathToPolicyJar, "US_export_policy.jar");
        File importJar = new File(pathToPolicyJar, "local_policy.jar");
        if (ClassLoader.getSystemResource("javax/crypto/Cipher.class") != null && (exportJar.exists() ^ 1) == 0 && (importJar.exists() ^ 1) == 0) {
            CryptoPermissions defaultExport = new CryptoPermissions();
            CryptoPermissions exemptExport = new CryptoPermissions();
            loadPolicies(exportJar, defaultExport, exemptExport);
            CryptoPermissions defaultImport = new CryptoPermissions();
            CryptoPermissions exemptImport = new CryptoPermissions();
            loadPolicies(importJar, defaultImport, exemptImport);
            if (defaultExport.isEmpty() || defaultImport.isEmpty()) {
                throw new SecurityException("Missing mandatory jurisdiction policy files");
            }
            defaultPolicy = defaultExport.getMinimum(defaultImport);
            if (exemptExport.isEmpty()) {
                if (exemptImport.isEmpty()) {
                    exemptImport = null;
                }
                exemptPolicy = exemptImport;
                return;
            }
            exemptPolicy = exemptExport.getMinimum(exemptImport);
            return;
        }
        throw new SecurityException("Cannot locate policy or framework files!");
    }

    private static void loadPolicies(File jarPathName, CryptoPermissions defaultPolicy, CryptoPermissions exemptPolicy) throws Exception {
        JarFile jf = new JarFile(jarPathName);
        Enumeration<JarEntry> entries = jf.entries();
        while (entries.hasMoreElements()) {
            JarEntry je = (JarEntry) entries.nextElement();
            InputStream inputStream = null;
            try {
                if (je.getName().startsWith("default_")) {
                    inputStream = jf.getInputStream(je);
                    defaultPolicy.load(inputStream);
                } else if (je.getName().startsWith("exempt_")) {
                    inputStream = jf.getInputStream(je);
                    exemptPolicy.load(inputStream);
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                JarVerifier.verifyPolicySigned(je.getCertificates());
            } catch (Throwable th) {
                if (inputStream != null) {
                    inputStream.close();
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

    static boolean isRestricted() {
        return isRestricted;
    }
}
