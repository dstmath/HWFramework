package java.security;

import java.io.IOException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import sun.security.jca.Providers;

public class AlgorithmParameters {
    private String algorithm;
    private boolean initialized = false;
    private AlgorithmParametersSpi paramSpi;
    private Provider provider;

    protected AlgorithmParameters(AlgorithmParametersSpi paramSpi2, Provider provider2, String algorithm2) {
        this.paramSpi = paramSpi2;
        this.provider = provider2;
        this.algorithm = algorithm2;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static AlgorithmParameters getInstance(String algorithm2) throws NoSuchAlgorithmException {
        try {
            Object[] objs = Security.getImpl(algorithm2, "AlgorithmParameters", (String) null);
            return new AlgorithmParameters((AlgorithmParametersSpi) objs[0], (Provider) objs[1], algorithm2);
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(algorithm2 + " not found");
        }
    }

    public static AlgorithmParameters getInstance(String algorithm2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider2 == null || provider2.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        }
        Providers.checkBouncyCastleDeprecation(provider2, "AlgorithmParameters", algorithm2);
        Object[] objs = Security.getImpl(algorithm2, "AlgorithmParameters", provider2);
        return new AlgorithmParameters((AlgorithmParametersSpi) objs[0], (Provider) objs[1], algorithm2);
    }

    public static AlgorithmParameters getInstance(String algorithm2, Provider provider2) throws NoSuchAlgorithmException {
        if (provider2 != null) {
            Providers.checkBouncyCastleDeprecation(provider2, "AlgorithmParameters", algorithm2);
            Object[] objs = Security.getImpl(algorithm2, "AlgorithmParameters", provider2);
            return new AlgorithmParameters((AlgorithmParametersSpi) objs[0], (Provider) objs[1], algorithm2);
        }
        throw new IllegalArgumentException("missing provider");
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public final void init(AlgorithmParameterSpec paramSpec) throws InvalidParameterSpecException {
        if (!this.initialized) {
            this.paramSpi.engineInit(paramSpec);
            this.initialized = true;
            return;
        }
        throw new InvalidParameterSpecException("already initialized");
    }

    public final void init(byte[] params) throws IOException {
        if (!this.initialized) {
            this.paramSpi.engineInit(params);
            this.initialized = true;
            return;
        }
        throw new IOException("already initialized");
    }

    public final void init(byte[] params, String format) throws IOException {
        if (!this.initialized) {
            this.paramSpi.engineInit(params, format);
            this.initialized = true;
            return;
        }
        throw new IOException("already initialized");
    }

    public final <T extends AlgorithmParameterSpec> T getParameterSpec(Class<T> paramSpec) throws InvalidParameterSpecException {
        if (this.initialized) {
            return this.paramSpi.engineGetParameterSpec(paramSpec);
        }
        throw new InvalidParameterSpecException("not initialized");
    }

    public final byte[] getEncoded() throws IOException {
        if (this.initialized) {
            return this.paramSpi.engineGetEncoded();
        }
        throw new IOException("not initialized");
    }

    public final byte[] getEncoded(String format) throws IOException {
        if (this.initialized) {
            return this.paramSpi.engineGetEncoded(format);
        }
        throw new IOException("not initialized");
    }

    public final String toString() {
        if (!this.initialized) {
            return null;
        }
        return this.paramSpi.engineToString();
    }
}
