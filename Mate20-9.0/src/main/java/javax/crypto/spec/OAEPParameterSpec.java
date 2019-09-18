package javax.crypto.spec;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import javax.crypto.spec.PSource;

public class OAEPParameterSpec implements AlgorithmParameterSpec {
    public static final OAEPParameterSpec DEFAULT = new OAEPParameterSpec();
    private String mdName = "SHA-1";
    private String mgfName = "MGF1";
    private AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
    private PSource pSrc = PSource.PSpecified.DEFAULT;

    private OAEPParameterSpec() {
    }

    public OAEPParameterSpec(String mdName2, String mgfName2, AlgorithmParameterSpec mgfSpec2, PSource pSrc2) {
        if (mdName2 == null) {
            throw new NullPointerException("digest algorithm is null");
        } else if (mgfName2 == null) {
            throw new NullPointerException("mask generation function algorithm is null");
        } else if (pSrc2 != null) {
            this.mdName = mdName2;
            this.mgfName = mgfName2;
            this.mgfSpec = mgfSpec2;
            this.pSrc = pSrc2;
        } else {
            throw new NullPointerException("source of the encoding input is null");
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
