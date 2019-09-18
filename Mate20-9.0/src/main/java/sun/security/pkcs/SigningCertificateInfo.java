package sun.security.pkcs;

import java.io.IOException;
import sun.security.util.DerValue;

public class SigningCertificateInfo {
    private byte[] ber = null;
    private ESSCertId[] certId = null;

    public SigningCertificateInfo(byte[] ber2) throws IOException {
        parse(ber2);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[\n");
        for (ESSCertId eSSCertId : this.certId) {
            buffer.append(eSSCertId.toString());
        }
        buffer.append("\n]");
        return buffer.toString();
    }

    public void parse(byte[] bytes) throws IOException {
        DerValue derValue = new DerValue(bytes);
        if (derValue.tag == 48) {
            DerValue[] certs = derValue.data.getSequence(1);
            this.certId = new ESSCertId[certs.length];
            for (int i = 0; i < certs.length; i++) {
                this.certId[i] = new ESSCertId(certs[i]);
            }
            if (derValue.data.available() > 0) {
                for (int i2 = 0; i2 < derValue.data.getSequence(1).length; i2++) {
                }
                return;
            }
            return;
        }
        throw new IOException("Bad encoding for signingCertificate");
    }
}
