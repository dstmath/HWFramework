package java.security.spec;

public class PSSParameterSpec implements AlgorithmParameterSpec {
    public static final PSSParameterSpec DEFAULT = new PSSParameterSpec();
    private String mdName = "SHA-1";
    private String mgfName = "MGF1";
    private AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
    private int saltLen = 20;
    private int trailerField = 1;

    private PSSParameterSpec() {
    }

    public PSSParameterSpec(String mdName2, String mgfName2, AlgorithmParameterSpec mgfSpec2, int saltLen2, int trailerField2) {
        if (mdName2 == null) {
            throw new NullPointerException("digest algorithm is null");
        } else if (mgfName2 == null) {
            throw new NullPointerException("mask generation function algorithm is null");
        } else if (saltLen2 < 0) {
            throw new IllegalArgumentException("negative saltLen value: " + saltLen2);
        } else if (trailerField2 >= 0) {
            this.mdName = mdName2;
            this.mgfName = mgfName2;
            this.mgfSpec = mgfSpec2;
            this.saltLen = saltLen2;
            this.trailerField = trailerField2;
        } else {
            throw new IllegalArgumentException("negative trailerField: " + trailerField2);
        }
    }

    public PSSParameterSpec(int saltLen2) {
        if (saltLen2 >= 0) {
            this.saltLen = saltLen2;
            return;
        }
        throw new IllegalArgumentException("negative saltLen value: " + saltLen2);
    }

    public String getDigestAlgorithm() {
        return this.mdName;
    }

    public String getMGFAlgorithm() {
        return this.mgfName;
    }

    public AlgorithmParameterSpec getMGFParameters() {
        return this.mgfSpec;
    }

    public int getSaltLength() {
        return this.saltLen;
    }

    public int getTrailerField() {
        return this.trailerField;
    }
}
