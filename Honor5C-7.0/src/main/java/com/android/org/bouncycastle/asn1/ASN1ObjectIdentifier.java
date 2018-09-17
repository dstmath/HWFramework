package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.asn1.x509.ReasonFlags;
import com.android.org.bouncycastle.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

public class ASN1ObjectIdentifier extends ASN1Primitive {
    private static final long LONG_LIMIT = 72057594037927808L;
    private static final Map pool = null;
    private byte[] body;
    private final String identifier;

    private static class OidHandle {
        private final byte[] enc;
        private int key;

        OidHandle(byte[] enc) {
            this.key = Arrays.hashCode(enc);
            this.enc = enc;
        }

        public int hashCode() {
            return this.key;
        }

        public boolean equals(Object o) {
            if (o instanceof OidHandle) {
                return Arrays.areEqual(this.enc, ((OidHandle) o).enc);
            }
            return false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.bouncycastle.asn1.ASN1ObjectIdentifier.<clinit>():void");
    }

    public static ASN1ObjectIdentifier getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1ObjectIdentifier)) {
            return (ASN1ObjectIdentifier) obj;
        }
        if ((obj instanceof ASN1Encodable) && (((ASN1Encodable) obj).toASN1Primitive() instanceof ASN1ObjectIdentifier)) {
            return (ASN1ObjectIdentifier) ((ASN1Encodable) obj).toASN1Primitive();
        }
        if (obj instanceof byte[]) {
            try {
                return (ASN1ObjectIdentifier) ASN1Primitive.fromByteArray((byte[]) obj);
            } catch (IOException e) {
                throw new IllegalArgumentException("failed to construct object identifier from byte[]: " + e.getMessage());
            }
        }
        throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
    }

    public static ASN1ObjectIdentifier getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof ASN1ObjectIdentifier)) {
            return getInstance(o);
        }
        return fromOctetString(ASN1OctetString.getInstance(obj.getObject()).getOctets());
    }

    ASN1ObjectIdentifier(byte[] bytes) {
        StringBuffer objId = new StringBuffer();
        long value = 0;
        BigInteger bigValue = null;
        boolean first = true;
        for (int i = 0; i != bytes.length; i++) {
            int b = bytes[i] & 255;
            if (value <= LONG_LIMIT) {
                value += (long) (b & 127);
                if ((b & ReasonFlags.unused) == 0) {
                    if (first) {
                        if (value < 40) {
                            objId.append('0');
                        } else if (value < 80) {
                            objId.append('1');
                            value -= 40;
                        } else {
                            objId.append('2');
                            value -= 80;
                        }
                        first = false;
                    }
                    objId.append('.');
                    objId.append(value);
                    value = 0;
                } else {
                    value <<= 7;
                }
            } else {
                if (bigValue == null) {
                    bigValue = BigInteger.valueOf(value);
                }
                bigValue = bigValue.or(BigInteger.valueOf((long) (b & 127)));
                if ((b & ReasonFlags.unused) == 0) {
                    if (first) {
                        objId.append('2');
                        bigValue = bigValue.subtract(BigInteger.valueOf(80));
                        first = false;
                    }
                    objId.append('.');
                    objId.append(bigValue);
                    bigValue = null;
                    value = 0;
                } else {
                    bigValue = bigValue.shiftLeft(7);
                }
            }
        }
        this.identifier = objId.toString().intern();
        this.body = Arrays.clone(bytes);
    }

    public ASN1ObjectIdentifier(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("'identifier' cannot be null");
        } else if (isValidIdentifier(identifier)) {
            this.identifier = identifier.intern();
        } else {
            throw new IllegalArgumentException("string " + identifier + " not an OID");
        }
    }

    ASN1ObjectIdentifier(ASN1ObjectIdentifier oid, String branchID) {
        if (isValidBranchID(branchID, 0)) {
            this.identifier = oid.getId() + "." + branchID;
            return;
        }
        throw new IllegalArgumentException("string " + branchID + " not a valid OID branch");
    }

    public String getId() {
        return this.identifier;
    }

    public ASN1ObjectIdentifier branch(String branchID) {
        return new ASN1ObjectIdentifier(this, branchID);
    }

    public boolean on(ASN1ObjectIdentifier stem) {
        String id = getId();
        String stemId = stem.getId();
        return (id.length() <= stemId.length() || id.charAt(stemId.length()) != '.') ? false : id.startsWith(stemId);
    }

    private void writeField(ByteArrayOutputStream out, long fieldValue) {
        byte[] result = new byte[9];
        int pos = 8;
        result[8] = (byte) (((int) fieldValue) & 127);
        while (fieldValue >= 128) {
            fieldValue >>= 7;
            pos--;
            result[pos] = (byte) ((((int) fieldValue) & 127) | ReasonFlags.unused);
        }
        out.write(result, pos, 9 - pos);
    }

    private void writeField(ByteArrayOutputStream out, BigInteger fieldValue) {
        int byteCount = (fieldValue.bitLength() + 6) / 7;
        if (byteCount == 0) {
            out.write(0);
            return;
        }
        BigInteger tmpValue = fieldValue;
        byte[] tmp = new byte[byteCount];
        for (int i = byteCount - 1; i >= 0; i--) {
            tmp[i] = (byte) ((tmpValue.intValue() & 127) | ReasonFlags.unused);
            tmpValue = tmpValue.shiftRight(7);
        }
        int i2 = byteCount - 1;
        tmp[i2] = (byte) (tmp[i2] & 127);
        out.write(tmp, 0, tmp.length);
    }

    private void doOutput(ByteArrayOutputStream aOut) {
        OIDTokenizer tok = new OIDTokenizer(this.identifier);
        int first = Integer.parseInt(tok.nextToken()) * 40;
        String secondToken = tok.nextToken();
        if (secondToken.length() <= 18) {
            writeField(aOut, ((long) first) + Long.parseLong(secondToken));
        } else {
            writeField(aOut, new BigInteger(secondToken).add(BigInteger.valueOf((long) first)));
        }
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            if (token.length() <= 18) {
                writeField(aOut, Long.parseLong(token));
            } else {
                writeField(aOut, new BigInteger(token));
            }
        }
    }

    private synchronized byte[] getBody() {
        if (this.body == null) {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            doOutput(bOut);
            this.body = bOut.toByteArray();
        }
        return this.body;
    }

    boolean isConstructed() {
        return false;
    }

    int encodedLength() throws IOException {
        int length = getBody().length;
        return (StreamUtil.calculateBodyLength(length) + 1) + length;
    }

    void encode(ASN1OutputStream out) throws IOException {
        byte[] enc = getBody();
        out.write(6);
        out.writeLength(enc.length);
        out.write(enc);
    }

    public int hashCode() {
        return this.identifier.hashCode();
    }

    boolean asn1Equals(ASN1Primitive o) {
        if (o == this) {
            return true;
        }
        if (o instanceof ASN1ObjectIdentifier) {
            return this.identifier.equals(((ASN1ObjectIdentifier) o).identifier);
        }
        return false;
    }

    public String toString() {
        return getId();
    }

    private static boolean isValidBranchID(String branchID, int start) {
        boolean periodAllowed = false;
        int pos = branchID.length();
        while (true) {
            pos--;
            if (pos < start) {
                return periodAllowed;
            }
            char ch = branchID.charAt(pos);
            if ('0' <= ch && ch <= '9') {
                periodAllowed = true;
            } else if (ch != '.' || !periodAllowed) {
                return false;
            } else {
                periodAllowed = false;
            }
        }
    }

    private static boolean isValidIdentifier(String identifier) {
        if (identifier.length() < 3 || identifier.charAt(1) != '.') {
            return false;
        }
        char first = identifier.charAt(0);
        if (first < '0' || first > '2') {
            return false;
        }
        return isValidBranchID(identifier, 2);
    }

    public ASN1ObjectIdentifier intern() {
        synchronized (pool) {
            OidHandle hdl = new OidHandle(getBody());
            ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) pool.get(hdl);
            if (oid != null) {
                return oid;
            }
            pool.put(hdl, this);
            return this;
        }
    }

    static ASN1ObjectIdentifier fromOctetString(byte[] enc) {
        OidHandle hdl = new OidHandle(enc);
        synchronized (pool) {
            ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) pool.get(hdl);
            if (oid != null) {
                return oid;
            }
            return new ASN1ObjectIdentifier(enc);
        }
    }
}
