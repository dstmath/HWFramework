package java.security;

import java.security.spec.AlgorithmParameterSpec;

public class AlgorithmParameterGenerator {
    private String algorithm;
    private AlgorithmParameterGeneratorSpi paramGenSpi;
    private Provider provider;

    protected AlgorithmParameterGenerator(AlgorithmParameterGeneratorSpi paramGenSpi2, Provider provider2, String algorithm2) {
        this.paramGenSpi = paramGenSpi2;
        this.provider = provider2;
        this.algorithm = algorithm2;
    }

    public final String getAlgorithm() {
        return this.algorithm;
    }

    public static AlgorithmParameterGenerator getInstance(String algorithm2) throws NoSuchAlgorithmException {
        try {
            Object[] objs = Security.getImpl(algorithm2, "AlgorithmParameterGenerator", (String) null);
            return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) objs[0], (Provider) objs[1], algorithm2);
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(algorithm2 + " not found");
        }
    }

    public static AlgorithmParameterGenerator getInstance(String algorithm2, String provider2) throws NoSuchAlgorithmException, NoSuchProviderException {
        if (provider2 == null || provider2.length() == 0) {
            throw new IllegalArgumentException("missing provider");
        }
        Object[] objs = Security.getImpl(algorithm2, "AlgorithmParameterGenerator", provider2);
        return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) objs[0], (Provider) objs[1], algorithm2);
    }

    public static AlgorithmParameterGenerator getInstance(String algorithm2, Provider provider2) throws NoSuchAlgorithmException {
        if (provider2 != null) {
            Object[] objs = Security.getImpl(algorithm2, "AlgorithmParameterGenerator", provider2);
            return new AlgorithmParameterGenerator((AlgorithmParameterGeneratorSpi) objs[0], (Provider) objs[1], algorithm2);
        }
        throw new IllegalArgumentException("missing provider");
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
