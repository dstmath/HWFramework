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
    private static final Debug debug = null;
    private static final boolean dump = false;
    private final List<CertId> certIds;
    private final List<Extension> extensions;
    private byte[] nonce;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.provider.certpath.OCSPRequest.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.provider.certpath.OCSPRequest.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.OCSPRequest.<clinit>():void");
    }

    OCSPRequest(CertId certId) {
        this(Collections.singletonList(certId));
    }

    OCSPRequest(List<CertId> certIds) {
        this.certIds = certIds;
        this.extensions = Collections.emptyList();
    }

    OCSPRequest(List<CertId> certIds, List<Extension> extensions) {
        this.certIds = certIds;
        this.extensions = extensions;
    }

    byte[] encodeBytes() throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        DerOutputStream requestsOut = new DerOutputStream();
        for (CertId certId : this.certIds) {
            DerOutputStream certIdOut = new DerOutputStream();
            certId.encode(certIdOut);
            requestsOut.write((byte) DerValue.tag_SequenceOf, certIdOut);
        }
        tmp.write((byte) DerValue.tag_SequenceOf, requestsOut);
        if (!this.extensions.isEmpty()) {
            DerOutputStream extOut = new DerOutputStream();
            for (Extension ext : this.extensions) {
                ext.encode(extOut);
                if (ext.getId().equals(OCSP.NONCE_EXTENSION_OID.toString())) {
                    this.nonce = ext.getValue();
                }
            }
            DerOutputStream extsOut = new DerOutputStream();
            extsOut.write((byte) DerValue.tag_SequenceOf, extOut);
            tmp.write(DerValue.createTag(DerValue.TAG_CONTEXT, true, (byte) 2), extsOut);
        }
        DerOutputStream tbsRequest = new DerOutputStream();
        tbsRequest.write((byte) DerValue.tag_SequenceOf, tmp);
        DerOutputStream ocspRequest = new DerOutputStream();
        ocspRequest.write((byte) DerValue.tag_SequenceOf, tbsRequest);
        byte[] bytes = ocspRequest.toByteArray();
        if (dump) {
            debug.println("OCSPRequest bytes...\n\n" + new HexDumpEncoder().encode(bytes) + "\n");
        }
        return bytes;
    }

    List<CertId> getCertIds() {
        return this.certIds;
    }

    byte[] getNonce() {
        return this.nonce;
    }
}
