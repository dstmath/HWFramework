package java.security;

import java.security.spec.AlgorithmParameterSpec;

public class AlgorithmParameterGenerator {
    private String algorithm;
    private AlgorithmParameterGeneratorSpi paramGenSpi;
    private Provider provider;

    protected AlgorithmParameterGenerator(AlgorithmParameterGeneratorSpi paramGenSpi, Provider provider, String algorithm) {
        this.paramGenSpi = paramGenSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static AlgorithmParameterGenerator getInstance(String algorithm) throws NoSuchAlgorithmException {
        try {
            Object[] objs = Security.getImpl(algorithm, "AlgorithmParameterGenerator", (String) null);
            return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) objs[0], (Provider) objs[1], algorithm);
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(algorithm + " not found");
        }
    }

    public static AlgorithmParameterGenerator getInstance(String algorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider == null || provider.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        }
        Object[] objs = Security.getImpl(algorithm, "AlgorithmParameterGenerator", provider);
        return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) objs[0], (Provider) objs[1], algorithm);
    }

    public static AlgorithmParameterGenerator getInstance(String algorithm, Provider provider) throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException("missing provider");
        }
        Object[] objs = Security.getImpl(algorithm, "AlgorithmParameterGenerator", provider);
        return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) objs[0], (Provider) objs[1], algorithm);
    }

    public final Provider getProvider() {
        return this.provider;
    }

    public final void init(int size) {
        this.paramGenSpi.engineInit(size, new SecureRandom());
    }

    public final void init(int size, SecureRandom random) {
        this.paramGenSpi.engineInit(size, random);
    }

    public final void init(AlgorithmParameterSpec genParamSpec) throws InvalidAlgorithmParameterException {
        this.paramGenSpi.engineInit(genParamSpec, new SecureRandom());
    }

    public final void init(AlgorithmParameterSpec genParamSpec, SecureRandom random) throws InvalidAlgorithmParameterException {
        this.paramGenSpi.engineInit(genParamSpec, random);
    }

    public final AlgorithmParameters generateParameters() {
        return this.paramGenSpi.engineGenerateParameters();
    }
}
