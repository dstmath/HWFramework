package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.spec.PSource.PSpecified;

public class OAEPParameterSpec implements AlgorithmParameterSpec {
    public static final OAEPParameterSpec DEFAULT = new OAEPParameterSpec();
    private String mdName = "SHA-1";
    private String mgfName = "MGF1";
    private AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
    private PSource pSrc = PSpecified.DEFAULT;

    private OAEPParameterSpec() {
    }

    public OAEPParameterSpec(String mdName, String mgfName, AlgorithmParameterSpec mgfSpec, PSource pSrc) {
        if (mdName == null) {
            throw new NullPointerException("digest algorithm is null");
        } else if (mgfName == null) {
            throw new NullPointerException("mask generation function algorithm is null");
        } else if (pSrc == null) {
            throw new NullPointerException("source of the encoding input is null");
        } else {
            this.mdName = mdName;
            this.mgfName = mgfName;
            this.mgfSpec = mgfSpec;
            this.pSrc = pSrc;
        }
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

    public PSource getPSource() {
        return this.pSrc;
    }
}
