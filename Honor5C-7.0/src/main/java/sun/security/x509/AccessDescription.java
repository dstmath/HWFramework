package sun.security.x509;

import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class AccessDescription {
    public static final ObjectIdentifier Ad_CAISSUERS_Id = null;
    public static final ObjectIdentifier Ad_CAREPOSITORY_Id = null;
    public static final ObjectIdentifier Ad_OCSP_Id = null;
    public static final ObjectIdentifier Ad_TIMESTAMPING_Id = null;
    private GeneralName accessLocation;
    private ObjectIdentifier accessMethod;
    private int myhash;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.x509.AccessDescription.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.x509.AccessDescription.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.AccessDescription.<clinit>():void");
    }

    public AccessDescription(ObjectIdentifier accessMethod, GeneralName accessLocation) {
        this.myhash = -1;
        this.accessMethod = accessMethod;
        this.accessLocation = accessLocation;
    }

    public AccessDescription(DerValue derValue) throws IOException {
        this.myhash = -1;
        DerInputStream derIn = derValue.getData();
        this.accessMethod = derIn.getOID();
        this.accessLocation = new GeneralName(derIn.getDerValue());
    }

    public ObjectIdentifier getAccessMethod() {
        return this.accessMethod;
    }

    public GeneralName getAccessLocation() {
        return this.accessLocation;
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        tmp.putOID(this.accessMethod);
        this.accessLocation.encode(tmp);
        out.write((byte) DerValue.tag_SequenceOf, tmp);
    }

    public int hashCode() {
        if (this.myhash == -1) {
            this.myhash = this.accessMethod.hashCode() + this.accessLocation.hashCode();
        }
        return this.myhash;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof AccessDescription)) {
            return false;
        }
        AccessDescription that = (AccessDescription) obj;
        if (this == that) {
            return true;
        }
        if (this.accessMethod.equals(that.getAccessMethod())) {
            z = this.accessLocation.equals(that.getAccessLocation());
        }
        return z;
    }

    public String toString() {
        String method;
        if (this.accessMethod.equals(Ad_CAISSUERS_Id)) {
            method = "caIssuers";
        } else if (this.accessMethod.equals(Ad_CAREPOSITORY_Id)) {
            method = "caRepository";
        } else if (this.accessMethod.equals(Ad_TIMESTAMPING_Id)) {
            method = "timeStamping";
        } else if (this.accessMethod.equals(Ad_OCSP_Id)) {
            method = "ocsp";
        } else {
            method = this.accessMethod.toString();
        }
        return "\n   accessMethod: " + method + "\n   accessLocation: " + this.accessLocation.toString() + "\n";
    }
}
