package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateParsingException;
import java.util.Enumeration;
import java.util.Map;
import sun.misc.HexDumpEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class X509CertInfo implements CertAttrSet<String> {
    public static final String ALGORITHM_ID = "algorithmID";
    private static final int ATTR_ALGORITHM = 3;
    private static final int ATTR_EXTENSIONS = 10;
    private static final int ATTR_ISSUER = 4;
    private static final int ATTR_ISSUER_ID = 8;
    private static final int ATTR_KEY = 7;
    private static final int ATTR_SERIAL = 2;
    private static final int ATTR_SUBJECT = 6;
    private static final int ATTR_SUBJECT_ID = 9;
    private static final int ATTR_VALIDITY = 5;
    private static final int ATTR_VERSION = 1;
    public static final String DN_NAME = "dname";
    public static final String EXTENSIONS = "extensions";
    public static final String IDENT = "x509.info";
    public static final String ISSUER = "issuer";
    public static final String ISSUER_ID = "issuerID";
    public static final String KEY = "key";
    public static final String NAME = "info";
    public static final String SERIAL_NUMBER = "serialNumber";
    public static final String SUBJECT = "subject";
    public static final String SUBJECT_ID = "subjectID";
    public static final String VALIDITY = "validity";
    public static final String VERSION = "version";
    private static final Map<String, Integer> map = null;
    protected CertificateAlgorithmId algId;
    protected CertificateExtensions extensions;
    protected CertificateValidity interval;
    protected X500Name issuer;
    protected UniqueIdentity issuerUniqueId;
    protected CertificateX509Key pubKey;
    private byte[] rawCertInfo;
    protected CertificateSerialNumber serialNum;
    protected X500Name subject;
    protected UniqueIdentity subjectUniqueId;
    protected CertificateVersion version;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.x509.X509CertInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.x509.X509CertInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.X509CertInfo.<clinit>():void");
    }

    public X509CertInfo() {
        this.version = new CertificateVersion();
        this.serialNum = null;
        this.algId = null;
        this.issuer = null;
        this.subject = null;
        this.interval = null;
        this.pubKey = null;
        this.issuerUniqueId = null;
        this.subjectUniqueId = null;
        this.extensions = null;
        this.rawCertInfo = null;
    }

    public X509CertInfo(byte[] cert) throws CertificateParsingException {
        this.version = new CertificateVersion();
        this.serialNum = null;
        this.algId = null;
        this.issuer = null;
        this.subject = null;
        this.interval = null;
        this.pubKey = null;
        this.issuerUniqueId = null;
        this.subjectUniqueId = null;
        this.extensions = null;
        this.rawCertInfo = null;
        try {
            parse(new DerValue(cert));
        } catch (Throwable e) {
            throw new CertificateParsingException(e);
        }
    }

    public X509CertInfo(DerValue derVal) throws CertificateParsingException {
        this.version = new CertificateVersion();
        this.serialNum = null;
        this.algId = null;
        this.issuer = null;
        this.subject = null;
        this.interval = null;
        this.pubKey = null;
        this.issuerUniqueId = null;
        this.subjectUniqueId = null;
        this.extensions = null;
        this.rawCertInfo = null;
        try {
            parse(derVal);
        } catch (Throwable e) {
            throw new CertificateParsingException(e);
        }
    }

    public void encode(OutputStream out) throws CertificateException, IOException {
        if (this.rawCertInfo == null) {
            DerOutputStream tmp = new DerOutputStream();
            emit(tmp);
            this.rawCertInfo = tmp.toByteArray();
        }
        out.write((byte[]) this.rawCertInfo.clone());
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(VERSION);
        elements.addElement(SERIAL_NUMBER);
        elements.addElement(ALGORITHM_ID);
        elements.addElement(ISSUER);
        elements.addElement(VALIDITY);
        elements.addElement(SUBJECT);
        elements.addElement(KEY);
        elements.addElement(ISSUER_ID);
        elements.addElement(SUBJECT_ID);
        elements.addElement(EXTENSIONS);
        return elements.elements();
    }

    public String getName() {
        return NAME;
    }

    public byte[] getEncodedInfo() throws CertificateEncodingException {
        try {
            if (this.rawCertInfo == null) {
                DerOutputStream tmp = new DerOutputStream();
                emit(tmp);
                this.rawCertInfo = tmp.toByteArray();
            }
            return (byte[]) this.rawCertInfo.clone();
        } catch (IOException e) {
            throw new CertificateEncodingException(e.toString());
        } catch (CertificateException e2) {
            throw new CertificateEncodingException(e2.toString());
        }
    }

    public boolean equals(Object other) {
        if (other instanceof X509CertInfo) {
            return equals((X509CertInfo) other);
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean equals(X509CertInfo other) {
        if (this == other) {
            return true;
        }
        if (this.rawCertInfo == null || other.rawCertInfo == null || this.rawCertInfo.length != other.rawCertInfo.length) {
            return false;
        }
        for (int i = 0; i < this.rawCertInfo.length; i += ATTR_VERSION) {
            if (this.rawCertInfo[i] != other.rawCertInfo[i]) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int retval = 0;
        for (int i = ATTR_VERSION; i < this.rawCertInfo.length; i += ATTR_VERSION) {
            retval += this.rawCertInfo[i] * i;
        }
        return retval;
    }

    public String toString() {
        if (this.subject == null || this.pubKey == null || this.interval == null || this.issuer == null || this.algId == null || this.serialNum == null) {
            throw new NullPointerException("X.509 cert is incomplete");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        sb.append("  ").append(this.version.toString()).append("\n");
        sb.append("  Subject: ").append(this.subject.toString()).append("\n");
        sb.append("  Signature Algorithm: ").append(this.algId.toString()).append("\n");
        sb.append("  Key:  ").append(this.pubKey.toString()).append("\n");
        sb.append("  ").append(this.interval.toString()).append("\n");
        sb.append("  Issuer: ").append(this.issuer.toString()).append("\n");
        sb.append("  ").append(this.serialNum.toString()).append("\n");
        if (this.issuerUniqueId != null) {
            sb.append("  Issuer Id:\n").append(this.issuerUniqueId.toString()).append("\n");
        }
        if (this.subjectUniqueId != null) {
            sb.append("  Subject Id:\n").append(this.subjectUniqueId.toString()).append("\n");
        }
        if (this.extensions != null) {
            int i;
            Extension[] exts = (Extension[]) this.extensions.getAllExtensions().toArray(new Extension[0]);
            sb.append("\nCertificate Extensions: ").append(exts.length);
            for (i = 0; i < exts.length; i += ATTR_VERSION) {
                sb.append("\n[").append(i + ATTR_VERSION).append("]: ");
                Extension ext = exts[i];
                try {
                    if (OIDMap.getClass(ext.getExtensionId()) == null) {
                        sb.append(ext.toString());
                        byte[] extValue = ext.getExtensionValue();
                        if (extValue != null) {
                            DerOutputStream out = new DerOutputStream();
                            out.putOctetString(extValue);
                            extValue = out.toByteArray();
                            sb.append("Extension unknown: DER encoded OCTET string =\n").append(new HexDumpEncoder().encodeBuffer(extValue)).append("\n");
                        }
                    } else {
                        sb.append(ext.toString());
                    }
                } catch (Exception e) {
                    sb.append(", Error parsing this extension");
                }
            }
            Map<String, Extension> invalid = this.extensions.getUnparseableExtensions();
            if (!invalid.isEmpty()) {
                sb.append("\nUnparseable certificate extensions: ").append(invalid.size());
                i = ATTR_VERSION;
                for (Object ext2 : invalid.values()) {
                    int i2 = i + ATTR_VERSION;
                    sb.append("\n[").append(i).append("]: ");
                    sb.append(ext2);
                    i = i2;
                }
            }
        }
        sb.append("\n]");
        return sb.toString();
    }

    public void set(String name, Object val) throws CertificateException, IOException {
        X509AttributeName attrName = new X509AttributeName(name);
        int attr = attributeMap(attrName.getPrefix());
        if (attr == 0) {
            throw new CertificateException("Attribute name not recognized: " + name);
        }
        this.rawCertInfo = null;
        String suffix = attrName.getSuffix();
        switch (attr) {
            case ATTR_VERSION /*1*/:
                if (suffix == null) {
                    setVersion(val);
                } else {
                    this.version.set(suffix, val);
                }
            case ATTR_SERIAL /*2*/:
                if (suffix == null) {
                    setSerialNumber(val);
                } else {
                    this.serialNum.set(suffix, val);
                }
            case ATTR_ALGORITHM /*3*/:
                if (suffix == null) {
                    setAlgorithmId(val);
                } else {
                    this.algId.set(suffix, val);
                }
            case ATTR_ISSUER /*4*/:
                setIssuer(val);
            case ATTR_VALIDITY /*5*/:
                if (suffix == null) {
                    setValidity(val);
                } else {
                    this.interval.set(suffix, val);
                }
            case ATTR_SUBJECT /*6*/:
                setSubject(val);
            case ATTR_KEY /*7*/:
                if (suffix == null) {
                    setKey(val);
                } else {
                    this.pubKey.set(suffix, val);
                }
            case ATTR_ISSUER_ID /*8*/:
                setIssuerUniqueId(val);
            case ATTR_SUBJECT_ID /*9*/:
                setSubjectUniqueId(val);
            case ATTR_EXTENSIONS /*10*/:
                if (suffix == null) {
                    setExtensions(val);
                    return;
                }
                if (this.extensions == null) {
                    this.extensions = new CertificateExtensions();
                }
                this.extensions.set(suffix, val);
            default:
        }
    }

    public void delete(String name) throws CertificateException, IOException {
        X509AttributeName attrName = new X509AttributeName(name);
        int attr = attributeMap(attrName.getPrefix());
        if (attr == 0) {
            throw new CertificateException("Attribute name not recognized: " + name);
        }
        this.rawCertInfo = null;
        String suffix = attrName.getSuffix();
        switch (attr) {
            case ATTR_VERSION /*1*/:
                if (suffix == null) {
                    this.version = null;
                } else {
                    this.version.delete(suffix);
                }
            case ATTR_SERIAL /*2*/:
                if (suffix == null) {
                    this.serialNum = null;
                } else {
                    this.serialNum.delete(suffix);
                }
            case ATTR_ALGORITHM /*3*/:
                if (suffix == null) {
                    this.algId = null;
                } else {
                    this.algId.delete(suffix);
                }
            case ATTR_ISSUER /*4*/:
                this.issuer = null;
            case ATTR_VALIDITY /*5*/:
                if (suffix == null) {
                    this.interval = null;
                } else {
                    this.interval.delete(suffix);
                }
            case ATTR_SUBJECT /*6*/:
                this.subject = null;
            case ATTR_KEY /*7*/:
                if (suffix == null) {
                    this.pubKey = null;
                } else {
                    this.pubKey.delete(suffix);
                }
            case ATTR_ISSUER_ID /*8*/:
                this.issuerUniqueId = null;
            case ATTR_SUBJECT_ID /*9*/:
                this.subjectUniqueId = null;
            case ATTR_EXTENSIONS /*10*/:
                if (suffix == null) {
                    this.extensions = null;
                } else if (this.extensions != null) {
                    this.extensions.delete(suffix);
                }
            default:
        }
    }

    public Object get(String name) throws CertificateException, IOException {
        X509AttributeName attrName = new X509AttributeName(name);
        int attr = attributeMap(attrName.getPrefix());
        if (attr == 0) {
            throw new CertificateParsingException("Attribute name not recognized: " + name);
        }
        String suffix = attrName.getSuffix();
        switch (attr) {
            case ATTR_VERSION /*1*/:
                if (suffix == null) {
                    return this.version;
                }
                return this.version.get(suffix);
            case ATTR_SERIAL /*2*/:
                if (suffix == null) {
                    return this.serialNum;
                }
                return this.serialNum.get(suffix);
            case ATTR_ALGORITHM /*3*/:
                if (suffix == null) {
                    return this.algId;
                }
                return this.algId.get(suffix);
            case ATTR_ISSUER /*4*/:
                if (suffix == null) {
                    return this.issuer;
                }
                return getX500Name(suffix, true);
            case ATTR_VALIDITY /*5*/:
                if (suffix == null) {
                    return this.interval;
                }
                return this.interval.get(suffix);
            case ATTR_SUBJECT /*6*/:
                if (suffix == null) {
                    return this.subject;
                }
                return getX500Name(suffix, false);
            case ATTR_KEY /*7*/:
                if (suffix == null) {
                    return this.pubKey;
                }
                return this.pubKey.get(suffix);
            case ATTR_ISSUER_ID /*8*/:
                return this.issuerUniqueId;
            case ATTR_SUBJECT_ID /*9*/:
                return this.subjectUniqueId;
            case ATTR_EXTENSIONS /*10*/:
                if (suffix == null) {
                    return this.extensions;
                }
                if (this.extensions == null) {
                    return null;
                }
                return this.extensions.get(suffix);
            default:
                return null;
        }
    }

    private Object getX500Name(String name, boolean getIssuer) throws IOException {
        if (name.equalsIgnoreCase(DN_NAME)) {
            return getIssuer ? this.issuer : this.subject;
        } else if (name.equalsIgnoreCase(CertificateSubjectName.DN_PRINCIPAL)) {
            Object asX500Principal;
            if (getIssuer) {
                asX500Principal = this.issuer.asX500Principal();
            } else {
                asX500Principal = this.subject.asX500Principal();
            }
            return asX500Principal;
        } else {
            throw new IOException("Attribute name not recognized.");
        }
    }

    private void parse(DerValue val) throws CertificateParsingException, IOException {
        if (val.tag != 48) {
            throw new CertificateParsingException("signed fields invalid");
        }
        this.rawCertInfo = val.toByteArray();
        DerInputStream in = val.data;
        DerValue tmp = in.getDerValue();
        if (tmp.isContextSpecific((byte) 0)) {
            this.version = new CertificateVersion(tmp);
            tmp = in.getDerValue();
        }
        this.serialNum = new CertificateSerialNumber(tmp);
        this.algId = new CertificateAlgorithmId(in);
        this.issuer = new X500Name(in);
        if (this.issuer.isEmpty()) {
            throw new CertificateParsingException("Empty issuer DN not allowed in X509Certificates");
        }
        this.interval = new CertificateValidity(in);
        this.subject = new X500Name(in);
        if (this.version.compare(0) == 0 && this.subject.isEmpty()) {
            throw new CertificateParsingException("Empty subject DN not allowed in v1 certificate");
        }
        this.pubKey = new CertificateX509Key(in);
        if (in.available() == 0) {
            return;
        }
        if (this.version.compare(0) == 0) {
            throw new CertificateParsingException("no more data allowed for version 1 certificate");
        }
        tmp = in.getDerValue();
        if (tmp.isContextSpecific((byte) 1)) {
            this.issuerUniqueId = new UniqueIdentity(tmp);
            if (in.available() != 0) {
                tmp = in.getDerValue();
            } else {
                return;
            }
        }
        if (tmp.isContextSpecific((byte) 2)) {
            this.subjectUniqueId = new UniqueIdentity(tmp);
            if (in.available() != 0) {
                tmp = in.getDerValue();
            } else {
                return;
            }
        }
        if (this.version.compare(ATTR_SERIAL) != 0) {
            throw new CertificateParsingException("Extensions not allowed in v2 certificate");
        }
        if (tmp.isConstructed() && tmp.isContextSpecific((byte) 3)) {
            this.extensions = new CertificateExtensions(tmp.data);
        }
        verifyCert(this.subject, this.extensions);
    }

    private void verifyCert(X500Name subject, CertificateExtensions extensions) throws CertificateParsingException, IOException {
        if (!subject.isEmpty()) {
            return;
        }
        if (extensions == null) {
            throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and certificate has no extensions");
        }
        try {
            SubjectAlternativeNameExtension subjectAltNameExt = (SubjectAlternativeNameExtension) extensions.get(SubjectAlternativeNameExtension.NAME);
            GeneralNames names = subjectAltNameExt.get(SubjectAlternativeNameExtension.SUBJECT_NAME);
            if (names == null || names.isEmpty()) {
                throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and SubjectAlternativeName extension is empty");
            } else if (!subjectAltNameExt.isCritical()) {
                throw new CertificateParsingException("X.509 Certificate is incomplete: SubjectAlternativeName extension MUST be marked critical when subject field is empty");
            }
        } catch (IOException e) {
            throw new CertificateParsingException("X.509 Certificate is incomplete: subject field is empty, and SubjectAlternativeName extension is absent");
        }
    }

    private void emit(DerOutputStream out) throws CertificateException, IOException {
        DerOutputStream tmp = new DerOutputStream();
        this.version.encode(tmp);
        this.serialNum.encode(tmp);
        this.algId.encode(tmp);
        if (this.version.compare(0) == 0 && this.issuer.toString() == null) {
            throw new CertificateParsingException("Null issuer DN not allowed in v1 certificate");
        }
        this.issuer.encode(tmp);
        this.interval.encode(tmp);
        if (this.version.compare(0) == 0 && this.subject.toString() == null) {
            throw new CertificateParsingException("Null subject DN not allowed in v1 certificate");
        }
        this.subject.encode(tmp);
        this.pubKey.encode(tmp);
        if (this.issuerUniqueId != null) {
            this.issuerUniqueId.encode(tmp, DerValue.createTag(DerValue.TAG_CONTEXT, false, (byte) 1));
        }
        if (this.subjectUniqueId != null) {
            this.subjectUniqueId.encode(tmp, DerValue.createTag(DerValue.TAG_CONTEXT, false, (byte) 2));
        }
        if (this.extensions != null) {
            this.extensions.encode(tmp);
        }
        out.write((byte) DerValue.tag_SequenceOf, tmp);
    }

    private int attributeMap(String name) {
        Integer num = (Integer) map.get(name);
        if (num == null) {
            return 0;
        }
        return num.intValue();
    }

    private void setVersion(Object val) throws CertificateException {
        if (val instanceof CertificateVersion) {
            this.version = (CertificateVersion) val;
            return;
        }
        throw new CertificateException("Version class type invalid.");
    }

    private void setSerialNumber(Object val) throws CertificateException {
        if (val instanceof CertificateSerialNumber) {
            this.serialNum = (CertificateSerialNumber) val;
            return;
        }
        throw new CertificateException("SerialNumber class type invalid.");
    }

    private void setAlgorithmId(Object val) throws CertificateException {
        if (val instanceof CertificateAlgorithmId) {
            this.algId = (CertificateAlgorithmId) val;
            return;
        }
        throw new CertificateException("AlgorithmId class type invalid.");
    }

    private void setIssuer(Object val) throws CertificateException {
        if (val instanceof X500Name) {
            this.issuer = (X500Name) val;
            return;
        }
        throw new CertificateException("Issuer class type invalid.");
    }

    private void setValidity(Object val) throws CertificateException {
        if (val instanceof CertificateValidity) {
            this.interval = (CertificateValidity) val;
            return;
        }
        throw new CertificateException("CertificateValidity class type invalid.");
    }

    private void setSubject(Object val) throws CertificateException {
        if (val instanceof X500Name) {
            this.subject = (X500Name) val;
            return;
        }
        throw new CertificateException("Subject class type invalid.");
    }

    private void setKey(Object val) throws CertificateException {
        if (val instanceof CertificateX509Key) {
            this.pubKey = (CertificateX509Key) val;
            return;
        }
        throw new CertificateException("Key class type invalid.");
    }

    private void setIssuerUniqueId(Object val) throws CertificateException {
        if (this.version.compare(ATTR_VERSION) < 0) {
            throw new CertificateException("Invalid version");
        } else if (val instanceof UniqueIdentity) {
            this.issuerUniqueId = (UniqueIdentity) val;
        } else {
            throw new CertificateException("IssuerUniqueId class type invalid.");
        }
    }

    private void setSubjectUniqueId(Object val) throws CertificateException {
        if (this.version.compare(ATTR_VERSION) < 0) {
            throw new CertificateException("Invalid version");
        } else if (val instanceof UniqueIdentity) {
            this.subjectUniqueId = (UniqueIdentity) val;
        } else {
            throw new CertificateException("SubjectUniqueId class type invalid.");
        }
    }

    private void setExtensions(Object val) throws CertificateException {
        if (this.version.compare(ATTR_SERIAL) < 0) {
            throw new CertificateException("Invalid version");
        } else if (val instanceof CertificateExtensions) {
            this.extensions = (CertificateExtensions) val;
        } else {
            throw new CertificateException("Extensions class type invalid.");
        }
    }
}
