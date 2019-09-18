package sun.security.jca;

import dalvik.system.VMRuntime;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class Providers {
    private static final String BACKUP_PROVIDER_CLASSNAME = "sun.security.provider.VerificationProvider";
    public static final int DEFAULT_MAXIMUM_ALLOWABLE_TARGET_API_LEVEL_FOR_BC_DEPRECATION = 27;
    private static final Set<String> DEPRECATED_ALGORITHMS = new HashSet();
    private static volatile Provider SYSTEM_BOUNCY_CASTLE_PROVIDER = providerList.getProvider("BC");
    private static final String[] jarVerificationProviders = {"com.android.org.conscrypt.OpenSSLProvider", "com.android.org.bouncycastle.jce.provider.BouncyCastleProvider", "com.android.org.conscrypt.JSSEProvider", BACKUP_PROVIDER_CLASSNAME};
    private static int maximumAllowableApiLevelForBcDeprecation = 27;
    private static volatile ProviderList providerList;
    private static final ThreadLocal<ProviderList> threadLists = new InheritableThreadLocal();
    private static volatile int threadListsUsed;

    static {
        providerList = ProviderList.EMPTY;
        providerList = ProviderList.fromSecurityProperties();
        int numConfiguredProviders = providerList.size();
        providerList = providerList.removeInvalid();
        if (numConfiguredProviders == providerList.size()) {
            DEPRECATED_ALGORITHMS.addAll(Arrays.asList("ALGORITHMPARAMETERS.1.2.840.113549.3.7", "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.2", "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.22", "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.26", "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.42", "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.46", "ALGORITHMPARAMETERS.2.16.840.1.101.3.4.1.6", "ALGORITHMPARAMETERS.AES", "ALGORITHMPARAMETERS.DESEDE", "ALGORITHMPARAMETERS.EC", "ALGORITHMPARAMETERS.GCM", "ALGORITHMPARAMETERS.OAEP", "ALGORITHMPARAMETERS.TDEA", "CERTIFICATEFACTORY.X.509", "CERTIFICATEFACTORY.X509", "CIPHER.1.2.840.113549.3.4", "CIPHER.2.16.840.1.101.3.4.1.26", "CIPHER.2.16.840.1.101.3.4.1.46", "CIPHER.2.16.840.1.101.3.4.1.6", "CIPHER.AES/GCM/NOPADDING", "CIPHER.ARC4", "CIPHER.ARCFOUR", "CIPHER.OID.1.2.840.113549.3.4", "CIPHER.RC4", "CIPHER.ARC4/ECB/NOPADDING", "CIPHER.ARC4/NONE/NOPADDING", "CIPHER.ARCFOUR/ECB/NOPADDING", "CIPHER.ARCFOUR/NONE/NOPADDING", "CIPHER.RC4/ECB/NOPADDING", "CIPHER.RC4/NONE/NOPADDING", "KEYAGREEMENT.ECDH", "KEYFACTORY.1.2.840.10045.2.1", "KEYFACTORY.1.2.840.113549.1.1.1", "KEYFACTORY.1.2.840.113549.1.1.7", "KEYFACTORY.1.3.133.16.840.63.0.2", "KEYFACTORY.2.5.8.1.1", "KEYFACTORY.EC", "KEYFACTORY.RSA", "KEYGENERATOR.1.2.840.113549.2.10", "KEYGENERATOR.1.2.840.113549.2.11", "KEYGENERATOR.1.2.840.113549.2.7", "KEYGENERATOR.1.2.840.113549.2.8", "KEYGENERATOR.1.2.840.113549.2.9", "KEYGENERATOR.1.3.6.1.5.5.8.1.1", "KEYGENERATOR.1.3.6.1.5.5.8.1.2", "KEYGENERATOR.2.16.840.1.101.3.4.2.1", "KEYGENERATOR.AES", "KEYGENERATOR.DESEDE", "KEYGENERATOR.HMAC-MD5", "KEYGENERATOR.HMAC-SHA1", "KEYGENERATOR.HMAC-SHA224", "KEYGENERATOR.HMAC-SHA256", "KEYGENERATOR.HMAC-SHA384", "KEYGENERATOR.HMAC-SHA512", "KEYGENERATOR.HMAC/MD5", "KEYGENERATOR.HMAC/SHA1", "KEYGENERATOR.HMAC/SHA224", "KEYGENERATOR.HMAC/SHA256", "KEYGENERATOR.HMAC/SHA384", "KEYGENERATOR.HMAC/SHA512", "KEYGENERATOR.HMACMD5", "KEYGENERATOR.HMACSHA1", "KEYGENERATOR.HMACSHA224", "KEYGENERATOR.HMACSHA256", "KEYGENERATOR.HMACSHA384", "KEYGENERATOR.HMACSHA512", "KEYGENERATOR.TDEA", "KEYPAIRGENERATOR.1.2.840.10045.2.1", "KEYPAIRGENERATOR.1.2.840.113549.1.1.1", "KEYPAIRGENERATOR.1.2.840.113549.1.1.7", "KEYPAIRGENERATOR.1.3.133.16.840.63.0.2", "KEYPAIRGENERATOR.2.5.8.1.1", "KEYPAIRGENERATOR.EC", "KEYPAIRGENERATOR.RSA", "MAC.1.2.840.113549.2.10", "MAC.1.2.840.113549.2.11", "MAC.1.2.840.113549.2.7", "MAC.1.2.840.113549.2.8", "MAC.1.2.840.113549.2.9", "MAC.1.3.6.1.5.5.8.1.1", "MAC.1.3.6.1.5.5.8.1.2", "MAC.2.16.840.1.101.3.4.2.1", "MAC.HMAC-MD5", "MAC.HMAC-SHA1", "MAC.HMAC-SHA224", "MAC.HMAC-SHA256", "MAC.HMAC-SHA384", "MAC.HMAC-SHA512", "MAC.HMAC/MD5", "MAC.HMAC/SHA1", "MAC.HMAC/SHA224", "MAC.HMAC/SHA256", "MAC.HMAC/SHA384", "MAC.HMAC/SHA512", "MAC.HMACMD5", "MAC.HMACSHA1", "MAC.HMACSHA224", "MAC.HMACSHA256", "MAC.HMACSHA384", "MAC.HMACSHA512", "MAC.PBEWITHHMACSHA224", "MAC.PBEWITHHMACSHA256", "MAC.PBEWITHHMACSHA384", "MAC.PBEWITHHMACSHA512", "MESSAGEDIGEST.1.2.840.113549.2.5", "MESSAGEDIGEST.1.3.14.3.2.26", "MESSAGEDIGEST.2.16.840.1.101.3.4.2.1", "MESSAGEDIGEST.2.16.840.1.101.3.4.2.2", "MESSAGEDIGEST.2.16.840.1.101.3.4.2.3", "MESSAGEDIGEST.2.16.840.1.101.3.4.2.4", "MESSAGEDIGEST.MD5", "MESSAGEDIGEST.SHA", "MESSAGEDIGEST.SHA-1", "MESSAGEDIGEST.SHA-224", "MESSAGEDIGEST.SHA-256", "MESSAGEDIGEST.SHA-384", "MESSAGEDIGEST.SHA-512", "MESSAGEDIGEST.SHA1", "MESSAGEDIGEST.SHA224", "MESSAGEDIGEST.SHA256", "MESSAGEDIGEST.SHA384", "MESSAGEDIGEST.SHA512", "SECRETKEYFACTORY.DESEDE", "SECRETKEYFACTORY.TDEA", "SIGNATURE.1.2.840.10045.4.1", "SIGNATURE.1.2.840.10045.4.3.1", "SIGNATURE.1.2.840.10045.4.3.2", "SIGNATURE.1.2.840.10045.4.3.3", "SIGNATURE.1.2.840.10045.4.3.4", "SIGNATURE.1.2.840.113549.1.1.11", "SIGNATURE.1.2.840.113549.1.1.12", "SIGNATURE.1.2.840.113549.1.1.13", "SIGNATURE.1.2.840.113549.1.1.14", "SIGNATURE.1.2.840.113549.1.1.4", "SIGNATURE.1.2.840.113549.1.1.5", "SIGNATURE.1.3.14.3.2.29", "SIGNATURE.ECDSA", "SIGNATURE.ECDSAWITHSHA1", "SIGNATURE.MD5/RSA", "SIGNATURE.MD5WITHRSA", "SIGNATURE.MD5WITHRSAENCRYPTION", "SIGNATURE.NONEWITHECDSA", "SIGNATURE.OID.1.2.840.10045.4.3.1", "SIGNATURE.OID.1.2.840.10045.4.3.2", "SIGNATURE.OID.1.2.840.10045.4.3.3", "SIGNATURE.OID.1.2.840.10045.4.3.4", "SIGNATURE.OID.1.2.840.113549.1.1.11", "SIGNATURE.OID.1.2.840.113549.1.1.12", "SIGNATURE.OID.1.2.840.113549.1.1.13", "SIGNATURE.OID.1.2.840.113549.1.1.14", "SIGNATURE.OID.1.2.840.113549.1.1.4", "SIGNATURE.OID.1.2.840.113549.1.1.5", "SIGNATURE.OID.1.3.14.3.2.29", "SIGNATURE.SHA1/RSA", "SIGNATURE.SHA1WITHECDSA", "SIGNATURE.SHA1WITHRSA", "SIGNATURE.SHA1WITHRSAENCRYPTION", "SIGNATURE.SHA224/ECDSA", "SIGNATURE.SHA224/RSA", "SIGNATURE.SHA224WITHECDSA", "SIGNATURE.SHA224WITHRSA", "SIGNATURE.SHA224WITHRSAENCRYPTION", "SIGNATURE.SHA256/ECDSA", "SIGNATURE.SHA256/RSA", "SIGNATURE.SHA256WITHECDSA", "SIGNATURE.SHA256WITHRSA", "SIGNATURE.SHA256WITHRSAENCRYPTION", "SIGNATURE.SHA384/ECDSA", "SIGNATURE.SHA384/RSA", "SIGNATURE.SHA384WITHECDSA", "SIGNATURE.SHA384WITHRSA", "SIGNATURE.SHA384WITHRSAENCRYPTION", "SIGNATURE.SHA512/ECDSA", "SIGNATURE.SHA512/RSA", "SIGNATURE.SHA512WITHECDSA", "SIGNATURE.SHA512WITHRSA", "SIGNATURE.SHA512WITHRSAENCRYPTION"));
            return;
        }
        throw new AssertionError((Object) "Unable to configure default providers");
    }

    private Providers() {
    }

    public static Provider getSunProvider() {
        try {
            return (Provider) Class.forName(jarVerificationProviders[0]).newInstance();
        } catch (Exception e) {
            try {
                return (Provider) Class.forName(BACKUP_PROVIDER_CLASSNAME).newInstance();
            } catch (Exception e2) {
                throw new RuntimeException("Sun provider not found", e);
            }
        }
    }

    public static Object startJarVerification() {
        return beginThreadProviderList(getProviderList().getJarList(jarVerificationProviders));
    }

    public static void stopJarVerification(Object obj) {
        endThreadProviderList((ProviderList) obj);
    }

    public static ProviderList getProviderList() {
        ProviderList list = getThreadProviderList();
        if (list == null) {
            return getSystemProviderList();
        }
        return list;
    }

    public static void setProviderList(ProviderList newList) {
        if (getThreadProviderList() == null) {
            setSystemProviderList(newList);
        } else {
            changeThreadProviderList(newList);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0016, code lost:
        r0 = getSystemProviderList();
        r1 = r0.removeInvalid();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        if (r1 == r0) goto L_0x0024;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0020, code lost:
        setSystemProviderList(r1);
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0024, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0014, code lost:
        return r1;
     */
    public static ProviderList getFullProviderList() {
        synchronized (Providers.class) {
            ProviderList list = getThreadProviderList();
            if (list != null) {
                ProviderList newList = list.removeInvalid();
                if (newList != list) {
                    changeThreadProviderList(newList);
                    list = newList;
                }
            }
        }
    }

    private static ProviderList getSystemProviderList() {
        return providerList;
    }

    private static void setSystemProviderList(ProviderList list) {
        providerList = list;
    }

    public static ProviderList getThreadProviderList() {
        if (threadListsUsed == 0) {
            return null;
        }
        return threadLists.get();
    }

    private static void changeThreadProviderList(ProviderList list) {
        threadLists.set(list);
    }

    public static synchronized ProviderList beginThreadProviderList(ProviderList list) {
        ProviderList oldList;
        synchronized (Providers.class) {
            if (ProviderList.debug != null) {
                ProviderList.debug.println("ThreadLocal providers: " + list);
            }
            oldList = threadLists.get();
            threadListsUsed++;
            threadLists.set(list);
        }
        return oldList;
    }

    public static synchronized void endThreadProviderList(ProviderList list) {
        synchronized (Providers.class) {
            if (list == null) {
                try {
                    if (ProviderList.debug != null) {
                        ProviderList.debug.println("Disabling ThreadLocal providers");
                    }
                    threadLists.remove();
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                if (ProviderList.debug != null) {
                    ProviderList.debug.println("Restoring previous ThreadLocal providers: " + list);
                }
                threadLists.set(list);
            }
            threadListsUsed--;
        }
    }

    public static void setMaximumAllowableApiLevelForBcDeprecation(int targetApiLevel) {
        maximumAllowableApiLevelForBcDeprecation = targetApiLevel;
    }

    public static int getMaximumAllowableApiLevelForBcDeprecation() {
        return maximumAllowableApiLevelForBcDeprecation;
    }

    public static synchronized void checkBouncyCastleDeprecation(String provider, String service, String algorithm) throws NoSuchAlgorithmException {
        synchronized (Providers.class) {
            if ("BC".equals(provider) && providerList.getProvider(provider) == SYSTEM_BOUNCY_CASTLE_PROVIDER) {
                checkBouncyCastleDeprecation(service, algorithm);
            }
        }
    }

    public static synchronized void checkBouncyCastleDeprecation(Provider provider, String service, String algorithm) throws NoSuchAlgorithmException {
        synchronized (Providers.class) {
            if (provider == SYSTEM_BOUNCY_CASTLE_PROVIDER) {
                checkBouncyCastleDeprecation(service, algorithm);
            }
        }
    }

    private static void checkBouncyCastleDeprecation(String service, String algorithm) throws NoSuchAlgorithmException {
        if (!DEPRECATED_ALGORITHMS.contains((service + "." + algorithm).toUpperCase(Locale.US))) {
            return;
        }
        if (VMRuntime.getRuntime().getTargetSdkVersion() <= maximumAllowableApiLevelForBcDeprecation) {
            System.logE(" ******** DEPRECATED FUNCTIONALITY ********");
            System.logE(" * The implementation of the " + key + " algorithm from");
            System.logE(" * the BC provider is deprecated in this version of Android.");
            System.logE(" * It will be removed in a future version of Android and your");
            System.logE(" * application will no longer be able to request it.  Please see");
            System.logE(" * https://android-developers.googleblog.com/2018/03/cryptography-changes-in-android-p.html");
            System.logE(" * for more details.");
            return;
        }
        throw new NoSuchAlgorithmException("The BC provider no longer provides an implementation for " + key + ".  Please see https://android-developers.googleblog.com/2018/03/cryptography-changes-in-android-p.html for more details.");
    }
}
