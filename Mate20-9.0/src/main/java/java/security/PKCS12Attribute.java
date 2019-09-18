package java.security;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.regex.Pattern;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class PKCS12Attribute implements KeyStore.Entry.Attribute {
    private static final Pattern COLON_SEPARATED_HEX_PAIRS = Pattern.compile("^[0-9a-fA-F]{2}(:[0-9a-fA-F]{2})+$");
    private byte[] encoded;
    private int hashValue = -1;
    private String name;
    private String value;

    public PKCS12Attribute(String name2, String value2) {
        String[] values;
        if (name2 == null || value2 == null) {
            throw new NullPointerException();
        }
        try {
            ObjectIdentifier type = new ObjectIdentifier(name2);
            this.name = name2;
            int length = value2.length();
            if (value2.charAt(0) == '[' && value2.charAt(length - 1) == ']') {
                values = value2.substring(1, length - 1).split(", ");
            } else {
                values = new String[]{value2};
            }
            this.value = value2;
            try {
                this.encoded = encode(type, values);
            } catch (IOException e) {
                throw new IllegalArgumentException("Incorrect format: value", e);
            }
        } catch (IOException e2) {
            throw new IllegalArgumentException("Incorrect format: name", e2);
        }
    }

    public PKCS12Attribute(byte[] encoded2) {
        if (encoded2 != null) {
            this.encoded = (byte[]) encoded2.clone();
            try {
                parse(encoded2);
            } catch (IOException e) {
                throw new IllegalArgumentException("Incorrect format: encoded", e);
            }
        } else {
            throw new NullPointerException();
        }
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public byte[] getEncoded() {
        return (byte[]) this.encoded.clone();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PKCS12Attribute)) {
            return false;
        }
        return Arrays.equals(this.encoded, ((PKCS12Attribute) obj).getEncoded());
    }

    public int hashCode() {
        if (this.hashValue == -1) {
            Arrays.hashCode(this.encoded);
        }
        return this.hashValue;
    }

    public String toString() {
        return this.name + "=" + this.value;
    }

    private byte[] encode(ObjectIdentifier type, String[] values) throws IOException {
        DerOutputStream attribute = new DerOutputStream();
        attribute.putOID(type);
        DerOutputStream attrContent = new DerOutputStream();
        for (String value2 : values) {
            if (COLON_SEPARATED_HEX_PAIRS.matcher(value2).matches()) {
                byte[] bytes = new BigInteger(value2.replace((CharSequence) ":", (CharSequence) ""), 16).toByteArray();
                if (bytes[0] == 0) {
                    bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
                }
                attrContent.putOctetString(bytes);
            } else {
                attrContent.putUTF8String(value2);
            }
        }
        attribute.write((byte) 49, attrContent);
        DerOutputStream attributeValue = new DerOutputStream();
        attributeValue.write((byte) 48, attribute);
        return attributeValue.toByteArray();
    }

    private void parse(byte[] encoded2) throws IOException {
        DerValue[] attrSeq = new DerInputStream(encoded2).getSequence(2);
        ObjectIdentifier type = attrSeq[0].getOID();
        DerValue[] attrValueSet = new DerInputStream(attrSeq[1].toByteArray()).getSet(1);
        String[] values = new String[attrValueSet.length];
        for (int i = 0; i < attrValueSet.length; i++) {
            if (attrValueSet[i].tag == 4) {
                values[i] = Debug.toString(attrValueSet[i].getOctetString());
            } else {
                String asString = attrValueSet[i].getAsString();
                String printableString = asString;
                if (asString != null) {
                    values[i] = printableString;
                } else if (attrValueSet[i].tag == 6) {
                    values[i] = attrValueSet[i].getOID().toString();
                } else if (attrValueSet[i].tag == 24) {
                    values[i] = attrValueSet[i].getGeneralizedTime().toString();
                } else if (attrValueSet[i].tag == 23) {
                    values[i] = attrValueSet[i].getUTCTime().toString();
                } else if (attrValueSet[i].tag == 2) {
                    values[i] = attrValueSet[i].getBigInteger().toString();
                } else if (attrValueSet[i].tag == 1) {
                    values[i] = String.valueOf(attrValueSet[i].getBoolean());
                } else {
                    values[i] = Debug.toString(attrValueSet[i].getDataBytes());
                }
            }
        }
        this.name = type.toString();
        this.value = values.length == 1 ? values[0] : Arrays.toString((Object[]) values);
    }
}
