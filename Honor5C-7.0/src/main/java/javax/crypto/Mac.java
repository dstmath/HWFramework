package javax.crypto;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Provider.Service;
import java.security.ProviderException;
import java.security.spec.AlgorithmParameterSpec;
import sun.security.jca.GetInstance;
import sun.security.jca.GetInstance.Instance;
import sun.security.util.Debug;

public class Mac implements Cloneable {
    private static final Debug debug = null;
    private static int warnCount;
    private final String algorithm;
    private boolean initialized;
    private final Object lock;
    private Provider provider;
    private MacSpi spi;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: javax.crypto.Mac.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: javax.crypto.Mac.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.crypto.Mac.<clinit>():void");
    }

    protected Mac(MacSpi macSpi, Provider provider, String algorithm) {
        this.initialized = false;
        this.spi = macSpi;
        this.provider = provider;
        this.algorithm = algorithm;
        this.lock = null;
    }

    private Mac(String algorithm) {
        this.initialized = false;
        this.algorithm = algorithm;
        this.lock = new Object();
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static final Mac getInstance(String algorithm) throws NoSuchAlgorithmException {
        for (Service s : GetInstance.getServices("Mac", algorithm)) {
            if (JceSecurity.canUseProvider(s.getProvider())) {
                return new Mac(algorithm);
            }
        }
        throw new NoSuchAlgorithmException("Algorithm " + algorithm + " not available");
    }

    public static final Mac getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = JceSecurity.getInstance("Mac", MacSpi.class, algorithm, provider);
        return new Mac((MacSpi) instance.impl, instance.provider, algorithm);
    }

    public static final Mac getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        Instance instance = JceSecurity.getInstance("Mac", MacSpi.class, algorithm, provider);
        return new Mac((MacSpi) instance.impl, instance.provider, algorithm);
    }

    void chooseFirstProvider() {
        if (this.spi == null && this.lock != null) {
            synchronized (this.lock) {
                if (this.spi != null) {
                    return;
                }
                if (debug != null) {
                    int w = warnCount - 1;
                    warnCount = w;
                    if (w >= 0) {
                        debug.println("Mac.init() not first method called, disabling delayed provider selection");
                        if (w == 0) {
                            debug.println("Further warnings of this type will be suppressed");
                        }
                        new Exception("Call trace").printStackTrace();
                    }
                }
                Throwable lastException = null;
                for (Service s : GetInstance.getServices("Mac", this.algorithm)) {
                    if (JceSecurity.canUseProvider(s.getProvider())) {
                        try {
                            Object obj = s.newInstance(null);
                            if (obj instanceof MacSpi) {
                                this.spi = (MacSpi) obj;
                                this.provider = s.getProvider();
                                return;
                            }
                        } catch (Throwable e) {
                            lastException = e;
                        }
                    }
                }
                ProviderException e2 = new ProviderException("Could not construct MacSpi instance");
                if (lastException != null) {
                    e2.initCause(lastException);
                }
                throw e2;
            }
        }
    }

    private void chooseProvider(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        synchronized (this.lock) {
            if (this.spi == null || !(key == null || this.lock == null)) {
                Throwable lastException = null;
                for (Service s : GetInstance.getServices("Mac", this.algorithm)) {
                    if (s.supportsParameter(key) && JceSecurity.canUseProvider(s.getProvider())) {
                        try {
                            MacSpi spi = (MacSpi) s.newInstance(null);
                            spi.engineInit(key, params);
                            this.provider = s.getProvider();
                            this.spi = spi;
                            return;
                        } catch (Throwable e) {
                            if (lastException == null) {
                                lastException = e;
                            }
                        }
                    }
                }
                if (lastException instanceof InvalidKeyException) {
                    throw ((InvalidKeyException) lastException);
                } else if (lastException instanceof InvalidAlgorithmParameterException) {
                    throw ((InvalidAlgorithmParameterException) lastException);
                } else if (lastException instanceof RuntimeException) {
                    throw ((RuntimeException) lastException);
                } else {
                    throw new InvalidKeyException("No installed provider supports this key: " + (key != null ? key.getClass().getName() : "(null)"), lastException);
                }
            }
            this.spi.engineInit(key, params);
        }
    }

    public final Provider getProvider() {
        chooseFirstProvider();
        return this.provider;
    }

    public final int getMacLength() {
        chooseFirstProvider();
        return this.spi.engineGetMacLength();
    }

    public final void init(Key key) throws InvalidKeyException {
        try {
            if (this.spi == null || !(key == null || this.lock == null)) {
                chooseProvider(key, null);
            } else {
                this.spi.engineInit(key, null);
            }
            this.initialized = true;
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException("init() failed", e);
        }
    }

    public final void init(Key key, AlgorithmParameterSpec params) throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (this.spi == null || !(key == null || this.lock == null)) {
            chooseProvider(key, params);
        } else {
            this.spi.engineInit(key, params);
        }
        this.initialized = true;
    }

    public final void update(byte input) throws IllegalStateException {
        chooseFirstProvider();
        if (this.initialized) {
            this.spi.engineUpdate(input);
            return;
        }
        throw new IllegalStateException("MAC not initialized");
    }

    public final void update(byte[] input) throws IllegalStateException {
        chooseFirstProvider();
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        } else if (input != null) {
            this.spi.engineUpdate(input, 0, input.length);
        }
    }

    public final void update(byte[] input, int offset, int len) throws IllegalStateException {
        chooseFirstProvider();
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        } else if (input == null) {
        } else {
            if (offset < 0 || len > input.length - offset || len < 0) {
                throw new IllegalArgumentException("Bad arguments");
            }
            this.spi.engineUpdate(input, offset, len);
        }
    }

    public final void update(ByteBuffer input) {
        chooseFirstProvider();
        if (!this.initialized) {
            throw new IllegalStateException("MAC not initialized");
        } else if (input == null) {
            throw new IllegalArgumentException("Buffer must not be null");
        } else {
            this.spi.engineUpdate(input);
        }
    }

    public final byte[] doFinal() throws IllegalStateException {
        chooseFirstProvider();
        if (this.initialized) {
            byte[] mac = this.spi.engineDoFinal();
            this.spi.engineReset();
            return mac;
        }
        throw new IllegalStateException("MAC not initialized");
    }

    public final void doFinal(byte[] output, int outOffset) throws ShortBufferException, IllegalStateException {
        chooseFirstProvider();
        if (this.initialized) {
            int macLen = getMacLength();
            if (output == null || output.length - outOffset < macLen) {
                throw new ShortBufferException("Cannot store MAC in output buffer");
            }
            System.arraycopy(doFinal(), 0, output, outOffset, macLen);
            return;
        }
        throw new IllegalStateException("MAC not initialized");
    }

    public final byte[] doFinal(byte[] input) throws IllegalStateException {
        chooseFirstProvider();
        if (this.initialized) {
            update(input);
            return doFinal();
        }
        throw new IllegalStateException("MAC not initialized");
    }

    public final void reset() {
        chooseFirstProvider();
        this.spi.engineReset();
    }

    public final Object clone() throws CloneNotSupportedException {
        chooseFirstProvider();
        Mac that = (Mac) super.clone();
        that.spi = (MacSpi) this.spi.clone();
        return that;
    }

    public MacSpi getCurrentSpi() {
        return this.spi;
    }
}
