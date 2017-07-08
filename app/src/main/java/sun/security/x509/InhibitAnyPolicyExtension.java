package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.Debug;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.util.logging.PlatformLogger;

public class InhibitAnyPolicyExtension extends Extension implements CertAttrSet<String> {
    public static ObjectIdentifier AnyPolicy_Id = null;
    public static final String IDENT = "x509.info.extensions.InhibitAnyPolicy";
    public static final String NAME = "InhibitAnyPolicy";
    public static final String SKIP_CERTS = "skip_certs";
    private static final Debug debug = null;
    private int skipCerts;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.x509.InhibitAnyPolicyExtension.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.x509.InhibitAnyPolicyExtension.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.InhibitAnyPolicyExtension.<clinit>():void");
    }

    private void encodeThis() throws IOException {
        DerOutputStream out = new DerOutputStream();
        out.putInteger(this.skipCerts);
        this.extensionValue = out.toByteArray();
    }

    public InhibitAnyPolicyExtension(int skipCerts) throws IOException {
        this.skipCerts = PlatformLogger.OFF;
        if (skipCerts < -1) {
            throw new IOException("Invalid value for skipCerts");
        }
        if (skipCerts == -1) {
            this.skipCerts = PlatformLogger.OFF;
        } else {
            this.skipCerts = skipCerts;
        }
        this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
        this.critical = true;
        encodeThis();
    }

    public InhibitAnyPolicyExtension(Boolean critical, Object value) throws IOException {
        this.skipCerts = PlatformLogger.OFF;
        this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
        if (critical.booleanValue()) {
            this.critical = critical.booleanValue();
            this.extensionValue = (byte[]) value;
            DerValue val = new DerValue(this.extensionValue);
            if (val.tag != 2) {
                throw new IOException("Invalid encoding of InhibitAnyPolicy: data not integer");
            } else if (val.data == null) {
                throw new IOException("Invalid encoding of InhibitAnyPolicy: null data");
            } else {
                int skipCertsValue = val.getInteger();
                if (skipCertsValue < -1) {
                    throw new IOException("Invalid value for skipCerts");
                } else if (skipCertsValue == -1) {
                    this.skipCerts = PlatformLogger.OFF;
                    return;
                } else {
                    this.skipCerts = skipCertsValue;
                    return;
                }
            }
        }
        throw new IOException("Criticality cannot be false for InhibitAnyPolicy");
    }

    public String toString() {
        return super.toString() + "InhibitAnyPolicy: " + this.skipCerts + "\n";
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.InhibitAnyPolicy_Id;
            this.critical = true;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (!name.equalsIgnoreCase(SKIP_CERTS)) {
            throw new IOException("Attribute name not recognized by CertAttrSet:InhibitAnyPolicy.");
        } else if (obj instanceof Integer) {
            int skipCertsValue = ((Integer) obj).intValue();
            if (skipCertsValue < -1) {
                throw new IOException("Invalid value for skipCerts");
            }
            if (skipCertsValue == -1) {
                this.skipCerts = PlatformLogger.OFF;
            } else {
                this.skipCerts = skipCertsValue;
            }
            encodeThis();
        } else {
            throw new IOException("Attribute value should be of type Integer.");
        }
    }

    public Integer get(String name) throws IOException {
        if (name.equalsIgnoreCase(SKIP_CERTS)) {
            return new Integer(this.skipCerts);
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:InhibitAnyPolicy.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(SKIP_CERTS)) {
            throw new IOException("Attribute skip_certs may not be deleted.");
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:InhibitAnyPolicy.");
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(SKIP_CERTS);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }
}
