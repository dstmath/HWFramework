package sun.security.pkcs;

import java.io.IOException;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public class ContentInfo {
    public static ObjectIdentifier DATA_OID = ObjectIdentifier.newInternal(data);
    public static ObjectIdentifier DIGESTED_DATA_OID = ObjectIdentifier.newInternal(ddata);
    public static ObjectIdentifier ENCRYPTED_DATA_OID = ObjectIdentifier.newInternal(crdata);
    public static ObjectIdentifier ENVELOPED_DATA_OID = ObjectIdentifier.newInternal(edata);
    public static ObjectIdentifier NETSCAPE_CERT_SEQUENCE_OID = ObjectIdentifier.newInternal(nsdata);
    private static final int[] OLD_DATA = {1, 2, 840, 1113549, 1, 7, 1};
    public static ObjectIdentifier OLD_DATA_OID = ObjectIdentifier.newInternal(OLD_DATA);
    private static final int[] OLD_SDATA = {1, 2, 840, 1113549, 1, 7, 2};
    public static ObjectIdentifier OLD_SIGNED_DATA_OID = ObjectIdentifier.newInternal(OLD_SDATA);
    public static ObjectIdentifier PKCS7_OID = ObjectIdentifier.newInternal(pkcs7);
    public static ObjectIdentifier SIGNED_AND_ENVELOPED_DATA_OID = ObjectIdentifier.newInternal(sedata);
    public static ObjectIdentifier SIGNED_DATA_OID = ObjectIdentifier.newInternal(sdata);
    public static ObjectIdentifier TIMESTAMP_TOKEN_INFO_OID = ObjectIdentifier.newInternal(tstInfo);
    private static int[] crdata = {1, 2, 840, 113549, 1, 7, 6};
    private static int[] data = {1, 2, 840, 113549, 1, 7, 1};
    private static int[] ddata = {1, 2, 840, 113549, 1, 7, 5};
    private static int[] edata = {1, 2, 840, 113549, 1, 7, 3};
    private static int[] nsdata = {2, 16, 840, 1, 113730, 2, 5};
    private static int[] pkcs7 = {1, 2, 840, 113549, 1, 7};
    private static int[] sdata = {1, 2, 840, 113549, 1, 7, 2};
    private static int[] sedata = {1, 2, 840, 113549, 1, 7, 4};
    private static int[] tstInfo = {1, 2, 840, 113549, 1, 9, 16, 1, 4};
    DerValue content;
    ObjectIdentifier contentType;

    public ContentInfo(ObjectIdentifier contentType2, DerValue content2) {
        this.contentType = contentType2;
        this.content = content2;
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
        if (!this.contentType.equals((Object) DATA_OID) && !this.contentType.equals((Object) OLD_DATA_OID) && !this.contentType.equals((Object) TIMESTAMP_TOKEN_INFO_OID)) {
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
        out.write((byte) 48, seq);
    }

    public byte[] getContentBytes() throws IOException {
        if (this.content == null) {
            return null;
        }
        return new DerInputStream(this.content.toByteArray()).getOctetString();
    }

    public String toString() {
        String out = "" + "Content Info Sequence\n\tContent type: " + this.contentType + "\n";
        return out + "\tContent: " + this.content;
    }
}
