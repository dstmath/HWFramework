package java.security;

import java.security.Provider;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import sun.security.jca.GetInstance;
import sun.security.jca.Providers;

public class SecureRandom extends Random {
    private static volatile SecureRandom seedGenerator = null;
    static final long serialVersionUID = 4940670005562187L;
    private String algorithm;
    private long counter;
    private MessageDigest digest;
    private Provider provider;
    private byte[] randomBytes;
    private int randomBytesUsed;
    private SecureRandomSpi secureRandomSpi;
    private byte[] state;

    private static final class StrongPatternHolder {
        /* access modifiers changed from: private */
        public static Pattern pattern = Pattern.compile("\\s*([\\S&&[^:,]]*)(\\:([\\S&&[^,]]*))?\\s*(\\,(.*))?");

        private StrongPatternHolder() {
        }
    }

    public SecureRandom() {
        super(0);
        this.provider = null;
        this.secureRandomSpi = null;
        this.digest = null;
        getDefaultPRNG(false, null);
    }

    public SecureRandom(byte[] seed) {
        super(0);
        this.provider = null;
        this.secureRandomSpi = null;
        this.digest = null;
        getDefaultPRNG(true, seed);
    }

    private void getDefaultPRNG(boolean setSeed, byte[] seed) {
        String prng = getPrngAlgorithm();
        if (prng != null) {
            try {
                SecureRandom random = getInstance(prng);
                this.secureRandomSpi = random.getSecureRandomSpi();
                this.provider = random.getProvider();
                if (setSeed) {
                    this.secureRandomSpi.engineSetSeed(seed);
                }
                if (getClass() == SecureRandom.class) {
                    this.algorithm = prng;
                }
            } catch (NoSuchAlgorithmException nsae) {
                throw new RuntimeException((Throwable) nsae);
            }
        } else {
            throw new IllegalStateException("No SecureRandom implementation!");
        }
    }

    protected SecureRandom(SecureRandomSpi secureRandomSpi2, Provider provider2) {
        this(secureRandomSpi2, provider2, null);
    }

    private SecureRandom(SecureRandomSpi secureRandomSpi2, Provider provider2, String algorithm2) {
        super(0);
        this.provider = null;
        this.secureRandomSpi = null;
        this.digest = null;
        this.secureRandomSpi = secureRandomSpi2;
        this.provider = provider2;
        this.algorithm = algorithm2;
    }

    public static SecureRandom getInstance(String algorithm2) throws NoSuchAlgorithmException {
        GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", (Class<?>) SecureRandomSpi.class, algorithm2);
        return new SecureRandom((SecureRandomSpi) instance.impl, instance.provider, algorithm2);
    }

    public static SecureRandom getInstance(String algorithm2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", (Class<?>) SecureRandomSpi.class, algorithm2, provider2);
        return new SecureRandom((SecureRandomSpi) instance.impl, instance.provider, algorithm2);
    }

    public static SecureRandom getInstance(String algorithm2, Provider provider2) throws NoSuchAlgorithmException {
        GetInstance.Instance instance = GetInstance.getInstance("SecureRandom", (Class<?>) SecureRandomSpi.class, algorithm2, provider2);
        return new SecureRandom((SecureRandomSpi) instance.impl, instance.provider, algorithm2);
    }

    /* access modifiers changed from: package-private */
    public SecureRandomSpi getSecureRandomSpi() {
        return this.secureRandomSpi;
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public String getAlgorithm() {
        return this.algorithm != null ? this.algorithm : "unknown";
    }

    public synchronized void setSeed(byte[] seed) {
        this.secureRandomSpi.engineSetSeed(seed);
    }

    public void setSeed(long seed) {
        if (seed != 0) {
            this.secureRandomSpi.engineSetSeed(longToByteArray(seed));
        }
    }

    public synchronized void nextBytes(byte[] bytes) {
        this.secureRandomSpi.engineNextBytes(bytes);
    }

    /* access modifiers changed from: protected */
    public final int next(int numBits) {
        int numBytes = (numBits + 7) / 8;
        byte[] b = new byte[numBytes];
        int next = 0;
        nextBytes(b);
        for (int i = 0; i < numBytes; i++) {
            next = (next << 8) + (b[i] & Character.DIRECTIONALITY_UNDEFINED);
        }
        return next >>> ((numBytes * 8) - numBits);
    }

    public static byte[] getSeed(int numBytes) {
        if (seedGenerator == null) {
            seedGenerator = new SecureRandom();
        }
        return seedGenerator.generateSeed(numBytes);
    }

    public byte[] generateSeed(int numBytes) {
        return this.secureRandomSpi.engineGenerateSeed(numBytes);
    }

    private static byte[] longToByteArray(long l) {
        byte[] retVal = new byte[8];
        for (int i = 0; i < 8; i++) {
            retVal[i] = (byte) ((int) l);
            l >>= 8;
        }
        return retVal;
    }

    private static String getPrngAlgorithm() {
        for (Provider p : Providers.getProviderList().providers()) {
            Iterator<Provider.Service> it = p.getServices().iterator();
            while (true) {
                if (it.hasNext()) {
                    Provider.Service s = it.next();
                    if (s.getType().equals("SecureRandom")) {
                        return s.getAlgorithm();
                    }
                }
            }
        }
        return null;
    }

    public static SecureRandom getInstanceStrong() throws NoSuchAlgorithmException {
        String property = (String) AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty("securerandom.strongAlgorithms");
            }
        });
        if (property == null || property.length() == 0) {
            throw new NoSuchAlgorithmException("Null/empty securerandom.strongAlgorithms Security Property");
        }
        String remainder = property;
        while (remainder != null) {
            Matcher matcher = StrongPatternHolder.pattern.matcher(remainder);
            Matcher m = matcher;
            if (matcher.matches()) {
                String alg = m.group(1);
                String prov = m.group(3);
                if (prov != null) {
                    return getInstance(alg, prov);
                }
                try {
                    return getInstance(alg);
                } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                    remainder = m.group(5);
                }
            } else {
                remainder = null;
            }
        }
        throw new NoSuchAlgorithmException("No strong SecureRandom impls available: " + property);
    }
}
