package java.security;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore.Entry.Attribute;
import java.util.Arrays;
import java.util.regex.Pattern;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

public final class PKCS12Attribute implements Attribute {
    private static final Pattern COLON_SEPARATED_HEX_PAIRS = Pattern.compile("^[0-9a-fA-F]{2}(:[0-9a-fA-F]{2})+$");
    private byte[] encoded;
    private int hashValue = -1;
    private String name;
    private String value;

    public PKCS12Attribute(String name, String value) {
        if (name == null || value == null) {
            throw new NullPointerException();
        }
        try {
            ObjectIdentifier type = new ObjectIdentifier(name);
            this.name = name;
            int length = value.length();
            String[] values = (value.charAt(0) == '[' && value.charAt(length - 1) == ']') ? value.substring(1, length - 1).split(", ") : new String[]{value};
            this.value = value;
            try {
                this.encoded = encode(type, values);
            } catch (IOException e) {
                throw new IllegalArgumentException("Incorrect format: value", e);
            }
        } catch (IOException e2) {
            throw new IllegalArgumentException("Incorrect format: name", e2);
        }
    }

    public PKCS12Attribute(byte[] encoded) {
        if (encoded == null) {
            throw new NullPointerException();
        }
        this.encoded = (byte[]) encoded.clone();
        try {
            parse(encoded);
        } catch (IOException e) {
            throw new IllegalArgumentException("Incorrect format: encoded", e);
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
        if (obj instanceof PKCS12Attribute) {
            return Arrays.equals(this.encoded, ((PKCS12Attribute) obj).getEncoded());
        }
        return false;
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
        for (String value : values) {
            if (COLON_SEPARATED_HEX_PAIRS.matcher(value).matches()) {
                byte[] bytes = new BigInteger(value.replace((CharSequence) ":", (CharSequence) ""), 16).toByteArray();
                if (bytes[0] == (byte) 0) {
                    bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
                }
                attrContent.putOctetString(bytes);
            } else {
                attrContent.putUTF8String(value);
            }
        }
        attribute.write((byte) 49, attrContent);
        DerOutputStream attributeValue = new DerOutputStream();
        attributeValue.write((byte) 48, attribute);
        return attributeValue.toByteArray();
    }

    private void parse(byte[] encoded) throws IOException {
        DerValue[] attrSeq = new DerInputStream(encoded).getSequence(2);
        ObjectIdentifier type = attrSeq[0].getOID();
        DerValue[] attrValueSet = new DerInputStream(attrSeq[1].toByteArray()).getSet(1);
        Object[] values = new String[attrValueSet.length];
        for (int i = 0; i < attrValueSet.length; i++) {
            if (attrValueSet[i].tag == (byte) 4) {
                values[i] = Debug.toString(attrValueSet[i].getOctetString());
            } else {
                String printableString = attrValueSet[i].getAsString();
                if (printableString != null) {
                    values[i] = printableString;
                } else if (attrValueSet[i].tag == (byte) 6) {
                    values[i] = attrValueSet[i].getOID().toString();
                } else if (attrValueSet[i].tag == (byte) 24) {
                    values[i] = attrValueSet[i].getGeneralizedTime().toString();
                } else if (attrValueSet[i].tag == (byte) 23) {
                    values[i] = attrValueSet[i].getUTCTime().toString();
                } else if (attrValueSet[i].tag == (byte) 2) {
                    values[i] = attrValueSet[i].getBigInteger().toString();
                } else if (attrValueSet[i].tag == (byte) 1) {
                    values[i] = String.valueOf(attrValueSet[i].getBoolean());
                } else {
                    values[i] = Debug.toString(attrValueSet[i].getDataBytes());
                }
            }
        }
        this.name = type.toString();
        this.value = values.length == 1 ? values[0] : Arrays.toString(values);
    }
}
