package sun.security.pkcs;

import java.io.IOException;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import sun.misc.HexDumpEncoder;
import sun.security.util.Debug;
import sun.security.util.DerEncoder;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.CertificateExtensions;
import sun.security.x509.GeneralNameInterface;
import sun.util.calendar.BaseCalendar;

public class PKCS9Attribute implements DerEncoder {
    private static final Class<?> BYTE_ARRAY_CLASS = null;
    public static final ObjectIdentifier CHALLENGE_PASSWORD_OID = null;
    public static final String CHALLENGE_PASSWORD_STR = "ChallengePassword";
    public static final ObjectIdentifier CONTENT_TYPE_OID = null;
    public static final String CONTENT_TYPE_STR = "ContentType";
    public static final ObjectIdentifier COUNTERSIGNATURE_OID = null;
    public static final String COUNTERSIGNATURE_STR = "Countersignature";
    public static final ObjectIdentifier EMAIL_ADDRESS_OID = null;
    public static final String EMAIL_ADDRESS_STR = "EmailAddress";
    public static final ObjectIdentifier EXTENDED_CERTIFICATE_ATTRIBUTES_OID = null;
    public static final String EXTENDED_CERTIFICATE_ATTRIBUTES_STR = "ExtendedCertificateAttributes";
    public static final ObjectIdentifier EXTENSION_REQUEST_OID = null;
    public static final String EXTENSION_REQUEST_STR = "ExtensionRequest";
    public static final ObjectIdentifier ISSUER_SERIALNUMBER_OID = null;
    public static final String ISSUER_SERIALNUMBER_STR = "IssuerAndSerialNumber";
    public static final ObjectIdentifier MESSAGE_DIGEST_OID = null;
    public static final String MESSAGE_DIGEST_STR = "MessageDigest";
    private static final Hashtable<String, ObjectIdentifier> NAME_OID_TABLE = null;
    private static final Hashtable<ObjectIdentifier, String> OID_NAME_TABLE = null;
    static final ObjectIdentifier[] PKCS9_OIDS = null;
    private static final Byte[][] PKCS9_VALUE_TAGS = null;
    private static final String RSA_PROPRIETARY_STR = "RSAProprietary";
    public static final ObjectIdentifier SIGNATURE_TIMESTAMP_TOKEN_OID = null;
    public static final String SIGNATURE_TIMESTAMP_TOKEN_STR = "SignatureTimestampToken";
    public static final ObjectIdentifier SIGNING_CERTIFICATE_OID = null;
    public static final String SIGNING_CERTIFICATE_STR = "SigningCertificate";
    public static final ObjectIdentifier SIGNING_TIME_OID = null;
    public static final String SIGNING_TIME_STR = "SigningTime";
    private static final boolean[] SINGLE_VALUED = null;
    public static final ObjectIdentifier SMIME_CAPABILITY_OID = null;
    public static final String SMIME_CAPABILITY_STR = "SMIMECapability";
    private static final String SMIME_SIGNING_DESC_STR = "SMIMESigningDesc";
    public static final ObjectIdentifier UNSTRUCTURED_ADDRESS_OID = null;
    public static final String UNSTRUCTURED_ADDRESS_STR = "UnstructuredAddress";
    public static final ObjectIdentifier UNSTRUCTURED_NAME_OID = null;
    public static final String UNSTRUCTURED_NAME_STR = "UnstructuredName";
    private static final Class[] VALUE_CLASSES = null;
    private static final Debug debug = null;
    private int index;
    private ObjectIdentifier oid;
    private Object value;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.pkcs.PKCS9Attribute.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.pkcs.PKCS9Attribute.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.pkcs.PKCS9Attribute.<clinit>():void");
    }

    public PKCS9Attribute(ObjectIdentifier oid, Object value) throws IllegalArgumentException {
        init(oid, value);
    }

    public PKCS9Attribute(String name, Object value) throws IllegalArgumentException {
        ObjectIdentifier oid = getOID(name);
        if (oid == null) {
            throw new IllegalArgumentException("Unrecognized attribute name " + name + " constructing PKCS9Attribute.");
        }
        init(oid, value);
    }

    private void init(ObjectIdentifier oid, Object value) throws IllegalArgumentException {
        this.oid = oid;
        this.index = indexOf(oid, PKCS9_OIDS, 1);
        Class<?> clazz = this.index == -1 ? BYTE_ARRAY_CLASS : VALUE_CLASSES[this.index];
        if (clazz.isInstance(value)) {
            this.value = value;
            return;
        }
        throw new IllegalArgumentException("Wrong value class  for attribute " + oid + " constructing PKCS9Attribute; was " + value.getClass().toString() + ", should be " + clazz.toString());
    }

    public PKCS9Attribute(DerValue derVal) throws IOException {
        DerInputStream derIn = new DerInputStream(derVal.toByteArray());
        DerValue[] val = derIn.getSequence(2);
        if (derIn.available() != 0) {
            throw new IOException("Excess data parsing PKCS9Attribute");
        } else if (val.length != 2) {
            throw new IOException("PKCS9Attribute doesn't have two components");
        } else {
            this.oid = val[0].getOID();
            byte[] content = val[1].toByteArray();
            DerValue[] elems = new DerInputStream(content).getSet(1);
            this.index = indexOf(this.oid, PKCS9_OIDS, 1);
            if (this.index == -1) {
                if (debug != null) {
                    debug.println("Unsupported signer attribute: " + this.oid);
                }
                this.value = content;
                return;
            }
            int i;
            if (SINGLE_VALUED[this.index] && elems.length > 1) {
                throwSingleValuedException();
            }
            for (DerValue derValue : elems) {
                Byte tag = new Byte(derValue.tag);
                if (indexOf(tag, PKCS9_VALUE_TAGS[this.index], 0) == -1) {
                    throwTagException(tag);
                }
            }
            switch (this.index) {
                case BaseCalendar.SUNDAY /*1*/:
                case BaseCalendar.MONDAY /*2*/:
                case BaseCalendar.AUGUST /*8*/:
                    String[] values = new String[elems.length];
                    for (i = 0; i < elems.length; i++) {
                        values[i] = elems[i].getAsString();
                    }
                    this.value = values;
                    break;
                case BaseCalendar.TUESDAY /*3*/:
                    this.value = elems[0].getOID();
                    break;
                case BaseCalendar.WEDNESDAY /*4*/:
                    this.value = elems[0].getOctetString();
                    break;
                case BaseCalendar.THURSDAY /*5*/:
                    this.value = new DerInputStream(elems[0].toByteArray()).getUTCTime();
                    break;
                case BaseCalendar.JUNE /*6*/:
                    SignerInfo[] values2 = new SignerInfo[elems.length];
                    for (i = 0; i < elems.length; i++) {
                        values2[i] = new SignerInfo(elems[i].toDerInputStream());
                    }
                    this.value = values2;
                    break;
                case BaseCalendar.SATURDAY /*7*/:
                    this.value = elems[0].getAsString();
                    break;
                case BaseCalendar.SEPTEMBER /*9*/:
                    throw new IOException("PKCS9 extended-certificate attribute not supported.");
                case BaseCalendar.OCTOBER /*10*/:
                    throw new IOException("PKCS9 IssuerAndSerialNumberattribute not supported.");
                case BaseCalendar.NOVEMBER /*11*/:
                case BaseCalendar.DECEMBER /*12*/:
                    throw new IOException("PKCS9 RSA DSI attributes11 and 12, not supported.");
                case Calendar.SECOND /*13*/:
                    throw new IOException("PKCS9 attribute #13 not supported.");
                case ZipConstants.LOCCRC /*14*/:
                    this.value = new CertificateExtensions(new DerInputStream(elems[0].toByteArray()));
                    break;
                case Calendar.ZONE_OFFSET /*15*/:
                    throw new IOException("PKCS9 SMIMECapability attribute not supported.");
                case AbstractSpinedBuffer.MIN_CHUNK_SIZE /*16*/:
                    this.value = new SigningCertificateInfo(elems[0].toByteArray());
                    break;
                case Calendar.FIELD_COUNT /*17*/:
                    this.value = elems[0].toByteArray();
                    break;
            }
        }
    }

    public void derEncode(OutputStream out) throws IOException {
        DerOutputStream temp = new DerOutputStream();
        temp.putOID(this.oid);
        String[] values;
        DerOutputStream[] temps;
        int i;
        DerOutputStream temp2;
        switch (this.index) {
            case GeneralNameInterface.NAME_DIFF_TYPE /*-1*/:
                temp.write((byte[]) this.value);
                break;
            case BaseCalendar.SUNDAY /*1*/:
            case BaseCalendar.MONDAY /*2*/:
                values = this.value;
                temps = new DerOutputStream[values.length];
                for (i = 0; i < values.length; i++) {
                    temps[i] = new DerOutputStream();
                    temps[i].putIA5String(values[i]);
                }
                temp.putOrderedSetOf(DerValue.tag_SetOf, temps);
                break;
            case BaseCalendar.TUESDAY /*3*/:
                temp2 = new DerOutputStream();
                temp2.putOID((ObjectIdentifier) this.value);
                temp.write((byte) DerValue.tag_SetOf, temp2.toByteArray());
                break;
            case BaseCalendar.WEDNESDAY /*4*/:
                temp2 = new DerOutputStream();
                temp2.putOctetString((byte[]) this.value);
                temp.write((byte) DerValue.tag_SetOf, temp2.toByteArray());
                break;
            case BaseCalendar.THURSDAY /*5*/:
                temp2 = new DerOutputStream();
                temp2.putUTCTime((Date) this.value);
                temp.write((byte) DerValue.tag_SetOf, temp2.toByteArray());
                break;
            case BaseCalendar.JUNE /*6*/:
                temp.putOrderedSetOf(DerValue.tag_SetOf, (DerEncoder[]) this.value);
                break;
            case BaseCalendar.SATURDAY /*7*/:
                temp2 = new DerOutputStream();
                temp2.putPrintableString((String) this.value);
                temp.write((byte) DerValue.tag_SetOf, temp2.toByteArray());
                break;
            case BaseCalendar.AUGUST /*8*/:
                values = (String[]) this.value;
                temps = new DerOutputStream[values.length];
                for (i = 0; i < values.length; i++) {
                    temps[i] = new DerOutputStream();
                    temps[i].putPrintableString(values[i]);
                }
                temp.putOrderedSetOf(DerValue.tag_SetOf, temps);
                break;
            case BaseCalendar.SEPTEMBER /*9*/:
                throw new IOException("PKCS9 extended-certificate attribute not supported.");
            case BaseCalendar.OCTOBER /*10*/:
                throw new IOException("PKCS9 IssuerAndSerialNumberattribute not supported.");
            case BaseCalendar.NOVEMBER /*11*/:
            case BaseCalendar.DECEMBER /*12*/:
                throw new IOException("PKCS9 RSA DSI attributes11 and 12, not supported.");
            case Calendar.SECOND /*13*/:
                throw new IOException("PKCS9 attribute #13 not supported.");
            case ZipConstants.LOCCRC /*14*/:
                temp2 = new DerOutputStream();
                try {
                    this.value.encode(temp2, true);
                    temp.write((byte) DerValue.tag_SetOf, temp2.toByteArray());
                    break;
                } catch (CertificateException ex) {
                    throw new IOException(ex.toString());
                }
            case Calendar.ZONE_OFFSET /*15*/:
                throw new IOException("PKCS9 attribute #15 not supported.");
            case AbstractSpinedBuffer.MIN_CHUNK_SIZE /*16*/:
                throw new IOException("PKCS9 SigningCertificate attribute not supported.");
            case Calendar.FIELD_COUNT /*17*/:
                temp.write((byte) DerValue.tag_SetOf, (byte[]) this.value);
                break;
        }
        DerOutputStream derOut = new DerOutputStream();
        derOut.write((byte) DerValue.tag_SequenceOf, temp.toByteArray());
        out.write(derOut.toByteArray());
    }

    public boolean isKnown() {
        return this.index != -1;
    }

    public Object getValue() {
        return this.value;
    }

    public boolean isSingleValued() {
        return this.index != -1 ? SINGLE_VALUED[this.index] : true;
    }

    public ObjectIdentifier getOID() {
        return this.oid;
    }

    public String getName() {
        if (this.index == -1) {
            return this.oid.toString();
        }
        return (String) OID_NAME_TABLE.get(PKCS9_OIDS[this.index]);
    }

    public static ObjectIdentifier getOID(String name) {
        return (ObjectIdentifier) NAME_OID_TABLE.get(name.toLowerCase(Locale.ENGLISH));
    }

    public static String getName(ObjectIdentifier oid) {
        return (String) OID_NAME_TABLE.get(oid);
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(100);
        buf.append("[");
        if (this.index == -1) {
            buf.append(this.oid.toString());
        } else {
            buf.append((String) OID_NAME_TABLE.get(PKCS9_OIDS[this.index]));
        }
        buf.append(": ");
        if (this.index == -1 || SINGLE_VALUED[this.index]) {
            if (this.value instanceof byte[]) {
                buf.append(new HexDumpEncoder().encodeBuffer((byte[]) this.value));
            } else {
                buf.append(this.value.toString());
            }
            buf.append("]");
            return buf.toString();
        }
        boolean first = true;
        Object[] values = this.value;
        for (Object obj : values) {
            if (first) {
                first = false;
            } else {
                buf.append(", ");
            }
            buf.append(obj.toString());
        }
        return buf.toString();
    }

    static int indexOf(Object obj, Object[] a, int start) {
        for (int i = start; i < a.length; i++) {
            if (obj.equals(a[i])) {
                return i;
            }
        }
        return -1;
    }

    private void throwSingleValuedException() throws IOException {
        throw new IOException("Single-value attribute " + this.oid + " (" + getName() + ")" + " has multiple values.");
    }

    private void throwTagException(Byte tag) throws IOException {
        Byte[] expectedTags = PKCS9_VALUE_TAGS[this.index];
        StringBuffer msg = new StringBuffer(100);
        msg.append("Value of attribute ");
        msg.append(this.oid.toString());
        msg.append(" (");
        msg.append(getName());
        msg.append(") has wrong tag: ");
        msg.append(tag.toString());
        msg.append(".  Expected tags: ");
        msg.append(expectedTags[0].toString());
        for (int i = 1; i < expectedTags.length; i++) {
            msg.append(", ");
            msg.append(expectedTags[i].toString());
        }
        msg.append(".");
        throw new IOException(msg.toString());
    }
}
