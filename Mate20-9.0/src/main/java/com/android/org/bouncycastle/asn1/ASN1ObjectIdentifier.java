package com.android.org.bouncycastle.asn1;

import com.android.org.bouncycastle.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ASN1ObjectIdentifier extends ASN1Primitive {
    private static final long LONG_LIMIT = 72057594037927808L;
    private static final ConcurrentMap<OidHandle, ASN1ObjectIdentifier> pool = new ConcurrentHashMap();
    private byte[] body;
    private final String identifier;

    private static class OidHandle {
        private final byte[] enc;
        private final int key;

        OidHandle(byte[] enc2) {
            this.key = Arrays.hashCode(enc2);
            this.enc = enc2;
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

    public static ASN1ObjectIdentifier getInstance(Object obj) {
        if (obj == null || (obj instanceof ASN1ObjectIdentifier)) {
            return (ASN1ObjectIdentifier) obj;
        }
        if ((obj instanceof ASN1Encodable) && (((ASN1Encodable) obj).toASN1Primitive() instanceof ASN1ObjectIdentifier)) {
            return (ASN1ObjectIdentifier) ((ASN1Encodable) obj).toASN1Primitive();
        }
        if (obj instanceof byte[]) {
            try {
                return (ASN1ObjectIdentifier) fromByteArray((byte[]) obj);
            } catch (IOException e) {
                throw new IllegalArgumentException("failed to construct object identifier from byte[]: " + e.getMessage());
            }
        } else {
            throw new IllegalArgumentException("illegal object in getInstance: " + obj.getClass().getName());
        }
    }

    public static ASN1ObjectIdentifier getInstance(ASN1TaggedObject obj, boolean explicit) {
        ASN1Primitive o = obj.getObject();
        if (explicit || (o instanceof ASN1ObjectIdentifier)) {
            return getInstance(o);
        }
        return fromOctetString(ASN1OctetString.getInstance(obj.getObject()).getOctets());
    }

    ASN1ObjectIdentifier(byte[] bytes) {
        byte[] bArr = bytes;
        StringBuffer objId = new StringBuffer();
        long value = 0;
        BigInteger bigValue = null;
        boolean first = true;
        for (int i = 0; i != bArr.length; i++) {
            int b = bArr[i] & 255;
            if (value <= LONG_LIMIT) {
                long value2 = value + ((long) (b & 127));
                if ((b & 128) == 0) {
                    if (first) {
                        if (value2 < 40) {
                            objId.append('0');
                        } else if (value2 < 80) {
                            objId.append('1');
                            value2 -= 40;
                        } else {
                            objId.append('2');
                            value2 -= 80;
                        }
                        first = false;
                    }
                    objId.append('.');
                    objId.append(value2);
                    value = 0;
                } else {
                    value = value2 << 7;
                }
            } else {
                BigInteger bigValue2 = (bigValue == null ? BigInteger.valueOf(value) : bigValue).or(BigInteger.valueOf((long) (b & 127)));
                if ((b & 128) == 0) {
                    if (first) {
                        objId.append('2');
                        bigValue2 = bigValue2.subtract(BigInteger.valueOf(80));
                        first = false;
                    }
                    objId.append('.');
                    objId.append(bigValue2);
                    bigValue = null;
                    value = 0;
                } else {
                    bigValue = bigValue2.shiftLeft(7);
                }
            }
        }
        this.identifier = objId.toString().intern();
        this.body = Arrays.clone(bytes);
    }

    public ASN1ObjectIdentifier(String identifier2) {
        if (identifier2 == null) {
            throw new IllegalArgumentException("'identifier' cannot be null");
        } else if (isValidIdentifier(identifier2)) {
            this.identifier = identifier2.intern();
        } else {
            throw new IllegalArgumentException("string " + identifier2 + " not an OID");
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
        return id.length() > stemId.length() && id.charAt(stemId.length()) == '.' && id.startsWith(stemId);
    }

    private void writeField(ByteArrayOutputStream out, long fieldValue) {
        byte[] result = new byte[9];
        int pos = 8;
        result[8] = (byte) (((int) fieldValue) & 127);
        while (fieldValue >= 128) {
            fieldValue >>= 7;
            pos--;
            result[pos] = (byte) ((((int) fieldValue) & 127) | 128);
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
            tmp[i] = (byte) ((tmpValue.intValue() & 127) | 128);
            tmpValue = tmpValue.shiftRight(7);
        }
        int i2 = byteCount - 1;
        tmp[i2] = (byte) (tmp[i2] & Byte.MAX_VALUE);
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

    /* access modifiers changed from: package-private */
    public boolean isConstructed() {
        return false;
    }

    /* access modifiers changed from: package-private */
    public int encodedLength() throws IOException {
        int length = getBody().length;
        return 1 + StreamUtil.calculateBodyLength(length) + length;
    }

    /* access modifiers changed from: package-private */
    public void encode(ASN1OutputStream out) throws IOException {
        byte[] enc = getBody();
        out.write(6);
        out.writeLength(enc.length);
        out.write(enc);
    }

    public int hashCode() {
        return this.identifier.hashCode();
    }

    /* access modifiers changed from: package-private */
    public boolean asn1Equals(ASN1Primitive o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ASN1ObjectIdentifier)) {
            return false;
        }
        return this.identifier.equals(((ASN1ObjectIdentifier) o).identifier);
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

    private static boolean isValidIdentifier(String identifier2) {
        if (identifier2.length() < 3 || identifier2.charAt(1) != '.') {
            return false;
        }
        char first = identifier2.charAt(0);
        if (first < '0' || first > '2') {
            return false;
        }
        return isValidBranchID(identifier2, 2);
    }

    public ASN1ObjectIdentifier intern() {
        OidHandle hdl = new OidHandle(getBody());
        ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) pool.get(hdl);
        if (oid != null) {
            return oid;
        }
        ASN1ObjectIdentifier oid2 = pool.putIfAbsent(hdl, this);
        if (oid2 == null) {
            return this;
        }
        return oid2;
    }

    static ASN1ObjectIdentifier fromOctetString(byte[] enc) {
        ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) pool.get(new OidHandle(enc));
        if (oid == null) {
            return new ASN1ObjectIdentifier(enc);
        }
        return oid;
    }
}
