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
import org.bouncycastle.util.Properties;
import org.bouncycastle.util.Strings;

public class DRBG {
    private static final String PREFIX = DRBG.class.getName();
    private static final String[][] initialEntropySourceNames = {new String[]{"sun.security.provider.Sun", "sun.security.provider.SecureRandom"}, new String[]{"org.apache.harmony.security.provider.crypto.CryptoProvider", "org.apache.harmony.security.provider.crypto.SHA1PRNG_SecureRandomImpl"}, new String[]{"com.android.org.conscrypt.OpenSSLProvider", "com.android.org.conscrypt.OpenSSLRandom"}, new String[]{"org.conscrypt.OpenSSLProvider", "org.conscrypt.OpenSSLRandom"}};

    /* access modifiers changed from: private */
    public static class CoreSecureRandom extends SecureRandom {
        CoreSecureRandom(Object[] objArr) {
            super((SecureRandomSpi) objArr[1], (Provider) objArr[0]);
        }
    }

    public static class Default extends SecureRandomSpi {
        private static final SecureRandom random = DRBG.createBaseRandom(true);

        /* access modifiers changed from: protected */
        @Override // java.security.SecureRandomSpi
        public byte[] engineGenerateSeed(int i) {
            return random.generateSeed(i);
        }

        /* access modifiers changed from: protected */
        @Override // java.security.SecureRandomSpi
        public void engineNextBytes(byte[] bArr) {
            random.nextBytes(bArr);
        }

        /* access modifiers changed from: protected */
        @Override // java.security.SecureRandomSpi
        public void engineSetSeed(byte[] bArr) {
            random.setSeed(bArr);
        }
    }

    private static class HybridRandomProvider extends Provider {
        protected HybridRandomProvider() {
            super("BCHEP", 1.0d, "Bouncy Castle Hybrid Entropy Provider");
        }
    }

    /* access modifiers changed from: private */
    public static class HybridSecureRandom extends SecureRandom {
        private final SecureRandom baseRandom = DRBG.createInitialEntropySource();
        private final SP800SecureRandom drbg = new SP800SecureRandomBuilder(new EntropySourceProvider() {
            /* class org.bouncycastle.jcajce.provider.drbg.DRBG.HybridSecureRandom.AnonymousClass1 */

            @Override // org.bouncycastle.crypto.prng.EntropySourceProvider
            public EntropySource get(int i) {
                return new SignallingEntropySource(i);
            }
        }).setPersonalizationString(Strings.toByteArray("Bouncy Castle Hybrid Entropy Source")).buildHMAC(new HMac(new SHA512Digest()), this.baseRandom.generateSeed(32), false);
        private final AtomicInteger samples = new AtomicInteger(0);
        private final AtomicBoolean seedAvailable = new AtomicBoolean(false);

        private class SignallingEntropySource implements EntropySource {
            private final int byteLength;
            private final AtomicReference entropy = new AtomicReference();
            private final AtomicBoolean scheduled = new AtomicBoolean(false);

            private class EntropyGatherer implements Runnable {
                private final int numBytes;

                EntropyGatherer(int i) {
                    this.numBytes = i;
                }

                private void sleep(long j) {
                    try {
                        Thread.sleep(j);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                @Override // java.lang.Runnable
                public void run() {
                    String propertyValue = Properties.getPropertyValue("org.bouncycastle.drbg.gather_pause_secs");
                    long j = 5000;
                    if (propertyValue != null) {
                        try {
                            j = Long.parseLong(propertyValue) * 1000;
                        } catch (Exception e) {
                        }
                    }
                    byte[] bArr = new byte[this.numBytes];
                    for (int i = 0; i < SignallingEntropySource.this.byteLength / 8; i++) {
                        sleep(j);
                        byte[] generateSeed = HybridSecureRandom.this.baseRandom.generateSeed(8);
                        System.arraycopy(generateSeed, 0, bArr, i * 8, generateSeed.length);
                    }
                    int i2 = SignallingEntropySource.this.byteLength - ((SignallingEntropySource.this.byteLength / 8) * 8);
                    if (i2 != 0) {
                        sleep(j);
                        byte[] generateSeed2 = HybridSecureRandom.this.baseRandom.generateSeed(i2);
                        System.arraycopy(generateSeed2, 0, bArr, bArr.length - generateSeed2.length, generateSeed2.length);
                    }
                    SignallingEntropySource.this.entropy.set(bArr);
                    HybridSecureRandom.this.seedAvailable.set(true);
                }
            }

            SignallingEntropySource(int i) {
                this.byteLength = (i + 7) / 8;
            }

            @Override // org.bouncycastle.crypto.prng.EntropySource
            public int entropySize() {
                return this.byteLength * 8;
            }

            @Override // org.bouncycastle.crypto.prng.EntropySource
            public byte[] getEntropy() {
                byte[] bArr = (byte[]) this.entropy.getAndSet(null);
                if (bArr == null || bArr.length != this.byteLength) {
                    bArr = HybridSecureRandom.this.baseRandom.generateSeed(this.byteLength);
                } else {
                    this.scheduled.set(false);
                }
                if (!this.scheduled.getAndSet(true)) {
                    Thread thread = new Thread(new EntropyGatherer(this.byteLength));
                    thread.setDaemon(true);
                    thread.start();
                }
                return bArr;
            }

            @Override // org.bouncycastle.crypto.prng.EntropySource
            public boolean isPredictionResistant() {
                return true;
            }
        }

        HybridSecureRandom() {
            super(null, new HybridRandomProvider());
        }

        @Override // java.security.SecureRandom
        public byte[] generateSeed(int i) {
            byte[] bArr = new byte[i];
            if (this.samples.getAndIncrement() > 20 && this.seedAvailable.getAndSet(false)) {
                this.samples.set(0);
                this.drbg.reseed(null);
            }
            this.drbg.nextBytes(bArr);
            return bArr;
        }

        @Override // java.security.SecureRandom, java.util.Random
        public void setSeed(long j) {
            SP800SecureRandom sP800SecureRandom = this.drbg;
            if (sP800SecureRandom != null) {
                sP800SecureRandom.setSeed(j);
            }
        }

        @Override // java.security.SecureRandom
        public void setSeed(byte[] bArr) {
            SP800SecureRandom sP800SecureRandom = this.drbg;
            if (sP800SecureRandom != null) {
                sP800SecureRandom.setSeed(bArr);
            }
        }
    }

    public static class Mappings extends AsymmetricAlgorithmProvider {
        @Override // org.bouncycastle.jcajce.provider.util.AlgorithmProvider
        public void configure(ConfigurableProvider configurableProvider) {
            configurableProvider.addAlgorithm("SecureRandom.DEFAULT", DRBG.PREFIX + "$Default");
            configurableProvider.addAlgorithm("SecureRandom.NONCEANDIV", DRBG.PREFIX + "$NonceAndIV");
        }
    }

    public static class NonceAndIV extends SecureRandomSpi {
        private static final SecureRandom random = DRBG.createBaseRandom(false);

        /* access modifiers changed from: protected */
        @Override // java.security.SecureRandomSpi
        public byte[] engineGenerateSeed(int i) {
            return random.generateSeed(i);
        }

        /* access modifiers changed from: protected */
        @Override // java.security.SecureRandomSpi
        public void engineNextBytes(byte[] bArr) {
            random.nextBytes(bArr);
        }

        /* access modifiers changed from: protected */
        @Override // java.security.SecureRandomSpi
        public void engineSetSeed(byte[] bArr) {
            random.setSeed(bArr);
        }
    }

    /* access modifiers changed from: private */
    public static class URLSeededSecureRandom extends SecureRandom {
        private final InputStream seedStream;

        URLSeededSecureRandom(final URL url) {
            super(null, new HybridRandomProvider());
            this.seedStream = (InputStream) AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                /* class org.bouncycastle.jcajce.provider.drbg.DRBG.URLSeededSecureRandom.AnonymousClass1 */

                @Override // java.security.PrivilegedAction
                public InputStream run() {
                    try {
                        return url.openStream();
                    } catch (IOException e) {
                        throw new IllegalStateException("unable to open random source");
                    }
                }
            });
        }

        private int privilegedRead(final byte[] bArr, final int i, final int i2) {
            return ((Integer) AccessController.doPrivileged(new PrivilegedAction<Integer>() {
                /* class org.bouncycastle.jcajce.provider.drbg.DRBG.URLSeededSecureRandom.AnonymousClass2 */

                @Override // java.security.PrivilegedAction
                public Integer run() {
                    try {
                        return Integer.valueOf(URLSeededSecureRandom.this.seedStream.read(bArr, i, i2));
                    } catch (IOException e) {
                        throw new InternalError("unable to read random source");
                    }
                }
            })).intValue();
        }

        @Override // java.security.SecureRandom
        public byte[] generateSeed(int i) {
            byte[] bArr;
            int privilegedRead;
            synchronized (this) {
                bArr = new byte[i];
                int i2 = 0;
                while (i2 != bArr.length && (privilegedRead = privilegedRead(bArr, i2, bArr.length - i2)) > -1) {
                    i2 += privilegedRead;
                }
                if (i2 != bArr.length) {
                    throw new InternalError("unable to fully read random source");
                }
            }
            return bArr;
        }

        @Override // java.security.SecureRandom, java.util.Random
        public void setSeed(long j) {
        }

        @Override // java.security.SecureRandom
        public void setSeed(byte[] bArr) {
        }
    }

    /* access modifiers changed from: private */
    public static SecureRandom createBaseRandom(boolean z) {
        if (Properties.getPropertyValue("org.bouncycastle.drbg.entropysource") != null) {
            EntropySourceProvider createEntropySource = createEntropySource();
            EntropySource entropySource = createEntropySource.get(128);
            byte[] entropy = entropySource.getEntropy();
            return new SP800SecureRandomBuilder(createEntropySource).setPersonalizationString(z ? generateDefaultPersonalizationString(entropy) : generateNonceIVPersonalizationString(entropy)).buildHash(new SHA512Digest(), Arrays.concatenate(entropySource.getEntropy(), entropySource.getEntropy()), z);
        }
        HybridSecureRandom hybridSecureRandom = new HybridSecureRandom();
        byte[] generateSeed = hybridSecureRandom.generateSeed(16);
        return new SP800SecureRandomBuilder(hybridSecureRandom, true).setPersonalizationString(z ? generateDefaultPersonalizationString(generateSeed) : generateNonceIVPersonalizationString(generateSeed)).buildHash(new SHA512Digest(), hybridSecureRandom.generateSeed(32), z);
    }

    /* access modifiers changed from: private */
    public static SecureRandom createCoreSecureRandom() {
        if (Security.getProperty("securerandom.source") == null) {
            return new CoreSecureRandom(findSource());
        }
        try {
            return new URLSeededSecureRandom(new URL(Security.getProperty("securerandom.source")));
        } catch (Exception e) {
            return new CoreSecureRandom(findSource());
        }
    }

    private static EntropySourceProvider createEntropySource() {
        final String propertyValue = Properties.getPropertyValue("org.bouncycastle.drbg.entropysource");
        return (EntropySourceProvider) AccessController.doPrivileged(new PrivilegedAction<EntropySourceProvider>() {
            /* class org.bouncycastle.jcajce.provider.drbg.DRBG.AnonymousClass3 */

            @Override // java.security.PrivilegedAction
            public EntropySourceProvider run() {
                try {
                    return (EntropySourceProvider) ClassUtil.loadClass(DRBG.class, propertyValue).newInstance();
                } catch (Exception e) {
                    throw new IllegalStateException("entropy source " + propertyValue + " not created: " + e.getMessage(), e);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public static SecureRandom createInitialEntropySource() {
        return ((Boolean) AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            /* class org.bouncycastle.jcajce.provider.drbg.DRBG.AnonymousClass1 */

            @Override // java.security.PrivilegedAction
            public Boolean run() {
                try {
                    return Boolean.valueOf(SecureRandom.class.getMethod("getInstanceStrong", new Class[0]) != null);
                } catch (Exception e) {
                    return false;
                }
            }
        })).booleanValue() ? (SecureRandom) AccessController.doPrivileged(new PrivilegedAction<SecureRandom>() {
            /* class org.bouncycastle.jcajce.provider.drbg.DRBG.AnonymousClass2 */

            @Override // java.security.PrivilegedAction
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
        while (true) {
            String[][] strArr = initialEntropySourceNames;
            if (i >= strArr.length) {
                return null;
            }
            String[] strArr2 = strArr[i];
            try {
                return new Object[]{Class.forName(strArr2[0]).newInstance(), Class.forName(strArr2[1]).newInstance()};
            } catch (Throwable th) {
                i++;
            }
        }
    }

    private static byte[] generateDefaultPersonalizationString(byte[] bArr) {
        return Arrays.concatenate(Strings.toByteArray("Default"), bArr, Pack.longToBigEndian(Thread.currentThread().getId()), Pack.longToBigEndian(System.currentTimeMillis()));
    }

    private static byte[] generateNonceIVPersonalizationString(byte[] bArr) {
        return Arrays.concatenate(Strings.toByteArray("Nonce"), bArr, Pack.longToLittleEndian(Thread.currentThread().getId()), Pack.longToLittleEndian(System.currentTimeMillis()));
    }
}
