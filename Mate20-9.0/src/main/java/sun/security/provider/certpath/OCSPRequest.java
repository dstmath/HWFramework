package sun.security.provider.certpath;

import java.io.IOException;
import java.security.cert.Extension;
import java.util.Collections;
import java.util.List;
import sun.misc.HexDumpEncoder;
import sun.security.util.Debug;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

class OCSPRequest {
    private static final Debug debug = Debug.getInstance("certpath");
    private static final boolean dump = (debug != null && Debug.isOn("ocsp"));
    private final List<CertId> certIds;
    private final List<Extension> extensions;
    private byte[] nonce;

    OCSPRequest(CertId certId) {
        this((List<CertId>) Collections.singletonList(certId));
    }

    OCSPRequest(List<CertId> certIds2) {
        this.certIds = certIds2;
        this.extensions = Collections.emptyList();
    }

    OCSPRequest(List<CertId> certIds2, List<Extension> extensions2) {
        this.certIds = certIds2;
        this.extensions = extensions2;
    }

    /* access modifiers changed from: package-private */
    public byte[] encodeBytes() throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        DerOutputStream requestsOut = new DerOutputStream();
        for (CertId certId : this.certIds) {
            DerOutputStream certIdOut = new DerOutputStream();
            certId.encode(certIdOut);
            requestsOut.write((byte) 48, certIdOut);
        }
        tmp.write((byte) 48, requestsOut);
        if (!this.extensions.isEmpty()) {
            DerOutputStream extOut = new DerOutputStream();
            for (Extension ext : this.extensions) {
                ext.encode(extOut);
                if (ext.getId().equals(OCSP.NONCE_EXTENSION_OID.toString())) {
                    this.nonce = ext.getValue();
                }
            }
            DerOutputStream extsOut = new DerOutputStream();
            extsOut.write((byte) 48, extOut);
            tmp.write(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 2), extsOut);
        }
        DerOutputStream tbsRequest = new DerOutputStream();
        tbsRequest.write((byte) 48, tmp);
        DerOutputStream ocspRequest = new DerOutputStream();
        ocspRequest.write((byte) 48, tbsRequest);
        byte[] bytes = ocspRequest.toByteArray();
        if (dump) {
            new HexDumpEncoder();
            Debug debug2 = debug;
            debug2.println("OCSPRequest bytes...\n\n" + hexEnc.encode(bytes) + "\n");
        }
        return bytes;
    }

    /* access modifiers changed from: package-private */
    public List<CertId> getCertIds() {
        return this.certIds;
    }

    /* access modifiers changed from: package-private */
    public byte[] getNonce() {
        return this.nonce;
    }
}
