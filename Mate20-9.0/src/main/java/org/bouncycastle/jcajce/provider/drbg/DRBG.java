package org.bouncycastle.jcajce.provider.drbg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;
import java.security.Security;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.prng.EntropySource;
import org.bouncycastle.crypto.prng.EntropySourceProvider;
import org.bouncycastle.crypto.prng.SP800SecureRandom;
import org.bouncycastle.crypto.prng.SP800SecureRandomBuilder;
import org.bouncycastle.jcajce.provider.config.ConfigurableProvider;
import org.bouncycastle.jcajce.provider.symmetric.util.ClassUtil;
import org.bouncycastle.jcajce.provider.util.AsymmetricAlgorithmProvider;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.Pack;
import org.bouncycastle.util.Strings;

public class DRBG {
    /* access modifiers changed from: private */
    public static final String PREFIX = DRBG.class.getName();
    /* access modifiers changed from: private */
    public static final Object[] initialEntropySourceAndSpi = findSource();
    private static final String[][] initialEntropySourceNames = {new String[]{"sun.security.provider.Sun", "sun.security.provider.SecureRandom"}, new String[]{"org.apache.harmony.security.provider.crypto.CryptoProvider", "org.apache.harmony.security.provider.crypto.SHA1PRNG_SecureRandomImpl"}, new String[]{"com.android.org.conscrypt.OpenSSLProvider", "com.android.org.conscrypt.OpenSSLRandom"}, new String[]{"org.conscrypt.OpenSSLProvider", "org.conscrypt.OpenSSLRandom"}};

    private static class CoreSecureRandom extends SecureRandom {
        CoreSecureRandom() {
            super((SecureRandomSpi) DRBG.initialEntropySourceAndSpi[1], (Provider) DRBG.initialEntropySourceAndSpi[0]);
        }
    }

    public static class Default extends SecureRandomSpi {
        private static final SecureRandom random = DRBG.createBaseRandom(true);

        /* access modifiers changed from: protected */
        public byte[] engineGenerateSeed(int i) {
            return random.generateSeed(i);
        }

        /* access modifiers changed from: protected */
        public void engineNextBytes(byte[] bArr) {
            random.nextBytes(bArr);
        }

        /* access modifiers changed from: protected */
        public void engineSetSeed(byte[] bArr) {
            random.setSeed(bArr);
        }
    }

    private static class HybridRandomProvider extends Provider {
        protected HybridRandomProvider() {
            super("BCHEP", 1.0d, "Bouncy Castle Hybrid Entropy Provider");
        }
    }

    private static class HybridSecureRandom extends SecureRandom {
        /* access modifiers changed from: private */
        public final SecureRandom baseRandom = DRBG.createInitialEntropySource();
        private final SP800SecureRandom drbg = new SP800SecureRandomBuilder(new EntropySourceProvider() {
            public EntropySource get(int i) {
                return new SignallingEntropySource(i);
            }
        }).setPersonalizationString(Strings.toByteArray("Bouncy Castle Hybrid Entropy Source")).buildHMAC(new HMac(new SHA512Digest()), this.baseRandom.generateSeed(32), false);
        private final AtomicInteger samples = new AtomicInteger(0);
        /* access modifiers changed from: private */
        public final AtomicBoolean seedAvailable = new AtomicBoolean(false);

        private class SignallingEntropySource implements EntropySource {
            private final int byteLength;
            /* access modifiers changed from: private */
            public final AtomicReference entropy = new AtomicReference();
            private final AtomicBoolean scheduled = new AtomicBoolean(false);

            private class EntropyGatherer implements Runnable {
                private final int numBytes;

                EntropyGatherer(int i) {
                    this.numBytes = i;
                }

                public void run() {
                    SignallingEntropySource.this.entropy.set(HybridSecureRandom.this.baseRandom.generateSeed(this.numBytes));
                    HybridSecureRandom.this.seedAvailable.set(true);
                }
            }

            SignallingEntropySource(int i) {
                this.byteLength = (i + 7) / 8;
            }

            public int entropySize() {
                return this.byteLength * 8;
            }

            public byte[] getEntropy() {
                byte[] bArr = (byte[]) this.entropy.getAndSet(null);
                if (bArr == null || bArr.length != this.byteLength) {
                    bArr = HybridSecureRandom.this.baseRandom.generateSeed(this.byteLength);
                } else {
                    this.scheduled.set(false);
                }
                if (!this.scheduled.getAndSet(true)) {
                    new Thread(new EntropyGatherer(this.byteLength)).start();
                }
                return bArr;
            }

            public boolean isPredictionResistant() {
                return true;
            }
        }

        HybridSecureRandom() {
            super(null, new HybridRandomProvider());
        }

        public byte[] generateSeed(int i) {
            byte[] bArr = new byte[i];
            if (this.samples.getAndIncrement() > 20 && this.seedAvailable.getAndSet(false)) {
                this.samples.set(0);
                this.drbg.reseed(null);
            }
            this.drbg.nextBytes(bArr);
            return bArr;
        }

        public void setSeed(long j) {
            if (this.drbg != null) {
                this.drbg.setSeed(j);
            }
        }

        public void setSeed(byte[] bArr) {
            if (this.drbg != null) {
                this.drbg.setSeed(bArr);
            }
        }
    }

    public static class Mappings extends AsymmetricAlgorithmProvider {
        public void configure(ConfigurableProvider configurableProvider) {
            configurableProvider.addAlgorithm("SecureRandom.DEFAULT", DRBG.PREFIX + "$Default");
            configurableProvider.addAlgorithm("SecureRandom.NONCEANDIV", DRBG.PREFIX + "$NonceAndIV");
        }
    }

    public static class NonceAndIV extends SecureRandomSpi {
        private static final SecureRandom random = DRBG.createBaseRandom(false);

        /* access modifiers changed from: protected */
        public byte[] engineGenerateSeed(int i) {
            return random.generateSeed(i);
        }

        /* access modifiers changed from: protected */
        public void engineNextBytes(byte[] bArr) {
            random.nextBytes(bArr);
        }

        /* access modifiers changed from: protected */
        public void engineSetSeed(byte[] bArr) {
            random.setSeed(bArr);
        }
    }

    private static class URLSeededSecureRandom extends SecureRandom {
        /* access modifiers changed from: private */
        public final InputStream seedStream;

        URLSeededSecureRandom(final URL url) {
            super(null, new HybridRandomProvider());
            this.seedStream = (InputStream) AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                public InputStream run() {
                    try {
                        return url.openStream();
                    } catch (IOException e) {
                        throw new InternalError("unable to open random source");
                    }
                }
            });
        }

        private int privilegedRead(final byte[] bArr, final int i, final int i2) {
            return ((Integer) AccessController.doPrivileged(new PrivilegedAction<Integer>() {
                public Integer run() {
                    try {
                        return Integer.valueOf(URLSeededSecureRandom.this.seedStream.read(bArr, i, i2));
                    } catch (IOException e) {
                        throw new InternalError("unable to read random source");
                    }
                }
            })).intValue();
        }

        public byte[] generateSeed(int i) {
            byte[] bArr;
            synchronized (this) {
                bArr = new byte[i];
                int i2 = 0;
                while (i2 != bArr.length) {
                    int privilegedRead = privilegedRead(bArr, i2, bArr.length - i2);
                    if (privilegedRead <= -1) {
                        break;
                    }
                    i2 += privilegedRead;
                }
                if (i2 != bArr.length) {
                    throw new InternalError("unable to fully read random source");
                }
            }
            return bArr;
        }

        public void setSeed(long j) {
        }

        public void setSeed(byte[] bArr) {
        }
    }

    /* access modifiers changed from: private */
    public static SecureRandom createBaseRandom(boolean z) {
        if (System.getProperty("org.bouncycastle.drbg.entropysource") != null) {
            EntropySourceProvider createEntropySource = createEntropySource();
            EntropySource entropySource = createEntropySource.get(128);
            return new SP800SecureRandomBuilder(createEntropySource).setPersonalizationString(z ? generateDefaultPersonalizationString(entropySource.getEntropy()) : generateNonceIVPersonalizationString(entropySource.getEntropy())).buildHash(new SHA512Digest(), Arrays.concatenate(entropySource.getEntropy(), entropySource.getEntropy()), z);
        }
        HybridSecureRandom hybridSecureRandom = new HybridSecureRandom();
        return new SP800SecureRandomBuilder(hybridSecureRandom, true).setPersonalizationString(z ? generateDefaultPersonalizationString(hybridSecureRandom.generateSeed(16)) : generateNonceIVPersonalizationString(hybridSecureRandom.generateSeed(16))).buildHash(new SHA512Digest(), hybridSecureRandom.generateSeed(32), z);
    }

    /* access modifiers changed from: private */
    public static SecureRandom createCoreSecureRandom() {
        if (initialEntropySourceAndSpi != null) {
            return new CoreSecureRandom();
        }
        try {
            return new URLSeededSecureRandom(new URL(Security.getProperty("securerandom.source")));
        } catch (Exception e) {
            return new SecureRandom();
        }
    }

    private static EntropySourceProvider createEntropySource() {
        final String property = System.getProperty("org.bouncycastle.drbg.entropysource");
        return (EntropySourceProvider) AccessController.doPrivileged(new PrivilegedAction<EntropySourceProvider>() {
            public EntropySourceProvider run() {
                try {
                    return (EntropySourceProvider) ClassUtil.loadClass(DRBG.class, property).newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException("entropy source " + property + " not created: " + e.getMessage(), e);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public static SecureRandom createInitialEntropySource() {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                try {
                    return Boolean.valueOf(SecureRandom.class.getMethod("getInstanceStrong", new Class[0]) != null);
                } catch (Exception e) {
                    return false;
                }
            }
        })).booleanValue() ? (SecureRandom) AccessController.doPrivileged(new PrivilegedAction<SecureRandom>() {
            public SecureRandom run() {
                try {
                    return (SecureRandom) SecureRandom.class.getMethod("getInstanceStrong", new Class[0]).invoke(null, new Object[0]);
                } catch (Exception e) {
                    return DRBG.createCoreSecureRandom();
                }
            }
        }) : createCoreSecureRandom();
    }

    private static final Object[] findSource() {
        int i = 0;
        while (i < initialEntropySourceNames.length) {
            String[] strArr = initialEntropySourceNames[i];
            try {
                return new Object[]{Class.forName(strArr[0]).newInstance(), Class.forName(strArr[1]).newInstance()};
            } catch (Throwable th) {
                i++;
            }
        }
        return null;
    }

    private static byte[] generateDefaultPersonalizationString(byte[] bArr) {
        return Arrays.concatenate(Strings.toByteArray("Default"), bArr, Pack.longToBigEndian(Thread.currentThread().getId()), Pack.longToBigEndian(System.currentTimeMillis()));
    }

    private static byte[] generateNonceIVPersonalizationString(byte[] bArr) {
        return Arrays.concatenate(Strings.toByteArray("Nonce"), bArr, Pack.longToLittleEndian(Thread.currentThread().getId()), Pack.longToLittleEndian(System.currentTimeMillis()));
    }
}
