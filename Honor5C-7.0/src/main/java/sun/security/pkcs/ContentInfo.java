package sun.security.pkcs;

import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class ContentInfo {
    public static ObjectIdentifier DATA_OID;
    public static ObjectIdentifier DIGESTED_DATA_OID;
    public static ObjectIdentifier ENCRYPTED_DATA_OID;
    public static ObjectIdentifier ENVELOPED_DATA_OID;
    public static ObjectIdentifier NETSCAPE_CERT_SEQUENCE_OID;
    private static final int[] OLD_DATA = null;
    public static ObjectIdentifier OLD_DATA_OID;
    private static final int[] OLD_SDATA = null;
    public static ObjectIdentifier OLD_SIGNED_DATA_OID;
    public static ObjectIdentifier PKCS7_OID;
    public static ObjectIdentifier SIGNED_AND_ENVELOPED_DATA_OID;
    public static ObjectIdentifier SIGNED_DATA_OID;
    public static ObjectIdentifier TIMESTAMP_TOKEN_INFO_OID;
    private static int[] crdata;
    private static int[] data;
    private static int[] ddata;
    private static int[] edata;
    private static int[] nsdata;
    private static int[] pkcs7;
    private static int[] sdata;
    private static int[] sedata;
    private static int[] tstInfo;
    DerValue content;
    ObjectIdentifier contentType;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: sun.security.pkcs.ContentInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: sun.security.pkcs.ContentInfo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: sun.security.pkcs.ContentInfo.<clinit>():void");
    }

    public ContentInfo(ObjectIdentifier contentType, DerValue content) {
        this.contentType = contentType;
        this.content = content;
    }

    public ContentInfo(byte[] bytes) {
        DerValue octetString = new DerValue((byte) 4, bytes);
        this.contentType = DATA_OID;
        this.content = octetString;
    }

    public ContentInfo(DerInputStream derin) throws IOException, ParsingException {
        this(derin, false);
    }

    public ContentInfo(DerInputStream derin, boolean oldStyle) throws IOException, ParsingException {
        DerValue[] typeAndContent = derin.getSequence(2);
        this.contentType = new DerInputStream(typeAndContent[0].toByteArray()).getOID();
        if (oldStyle) {
            this.content = typeAndContent[1];
        } else if (typeAndContent.length > 1) {
            this.content = new DerInputStream(typeAndContent[1].toByteArray()).getSet(1, true)[0];
        }
    }

    public DerValue getContent() {
        return this.content;
    }

    public ObjectIdentifier getContentType() {
        return this.contentType;
    }

    public byte[] getData() throws IOException {
        if (!this.contentType.equals(DATA_OID) && !this.contentType.equals(OLD_DATA_OID) && !this.contentType.equals(TIMESTAMP_TOKEN_INFO_OID)) {
            throw new IOException("content type is not DATA: " + this.contentType);
        } else if (this.content == null) {
            return null;
        } else {
            return this.content.getOctetString();
        }
    }

    public void encode(DerOutputStream out) throws IOException {
        DerOutputStream seq = new DerOutputStream();
        seq.putOID(this.contentType);
        if (this.content != null) {
            DerOutputStream contentDerCode = new DerOutputStream();
            this.content.encode(contentDerCode);
            seq.putDerValue(new DerValue((byte) -96, contentDerCode.toByteArray()));
        }
        out.write((byte) DerValue.tag_SequenceOf, seq);
    }

    public byte[] getContentBytes() throws IOException {
        if (this.content == null) {
            return null;
        }
        return new DerInputStream(this.content.toByteArray()).getOctetString();
    }

    public String toString() {
        return ("" + "Content Info Sequence\n\tContent type: " + this.contentType + "\n") + "\tContent: " + this.content;
    }
}
