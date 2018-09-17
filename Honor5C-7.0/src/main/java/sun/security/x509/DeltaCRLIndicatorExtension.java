package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import sun.security.util.DerOutputStream;

public class DeltaCRLIndicatorExtension extends CRLNumberExtension {
    private static final String LABEL = "Base CRL Number";
    public static final String NAME = "DeltaCRLIndicator";

    public DeltaCRLIndicatorExtension(int crlNum) throws IOException {
        super(PKIXExtensions.DeltaCRLIndicator_Id, true, BigInteger.valueOf((long) crlNum), NAME, LABEL);
    }

    public DeltaCRLIndicatorExtension(BigInteger crlNum) throws IOException {
        super(PKIXExtensions.DeltaCRLIndicator_Id, true, crlNum, NAME, LABEL);
    }

    public DeltaCRLIndicatorExtension(Boolean critical, Object value) throws IOException {
        super(PKIXExtensions.DeltaCRLIndicator_Id, Boolean.valueOf(critical.booleanValue()), value, NAME, LABEL);
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        super.encode(out, PKIXExtensions.DeltaCRLIndicator_Id, true);
    }
}
