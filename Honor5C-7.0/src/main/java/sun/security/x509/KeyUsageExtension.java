package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import sun.security.util.BitArray;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KeyUsageExtension extends Extension implements CertAttrSet<String> {
    public static final String CRL_SIGN = "crl_sign";
    public static final String DATA_ENCIPHERMENT = "data_encipherment";
    public static final String DECIPHER_ONLY = "decipher_only";
    public static final String DIGITAL_SIGNATURE = "digital_signature";
    public static final String ENCIPHER_ONLY = "encipher_only";
    public static final String IDENT = "x509.info.extensions.KeyUsage";
    public static final String KEY_AGREEMENT = "key_agreement";
    public static final String KEY_CERTSIGN = "key_certsign";
    public static final String KEY_ENCIPHERMENT = "key_encipherment";
    public static final String NAME = "KeyUsage";
    public static final String NON_REPUDIATION = "non_repudiation";
    private boolean[] bitString;

    private void encodeThis() throws IOException {
        DerOutputStream os = new DerOutputStream();
        os.putTruncatedUnalignedBitString(new BitArray(this.bitString));
        this.extensionValue = os.toByteArray();
    }

    private boolean isSet(int position) {
        return this.bitString[position];
    }

    private void set(int position, boolean val) {
        if (position >= this.bitString.length) {
            boolean[] tmp = new boolean[(position + 1)];
            System.arraycopy(this.bitString, 0, tmp, 0, this.bitString.length);
            this.bitString = tmp;
        }
        this.bitString[position] = val;
    }

    public KeyUsageExtension(byte[] bitString) throws IOException {
        this.bitString = new BitArray(bitString.length * 8, bitString).toBooleanArray();
        this.extensionId = PKIXExtensions.KeyUsage_Id;
        this.critical = true;
        encodeThis();
    }

    public KeyUsageExtension(boolean[] bitString) throws IOException {
        this.bitString = bitString;
        this.extensionId = PKIXExtensions.KeyUsage_Id;
        this.critical = true;
        encodeThis();
    }

    public KeyUsageExtension(BitArray bitString) throws IOException {
        this.bitString = bitString.toBooleanArray();
        this.extensionId = PKIXExtensions.KeyUsage_Id;
        this.critical = true;
        encodeThis();
    }

    public KeyUsageExtension(Boolean critical, Object value) throws IOException {
        this.extensionId = PKIXExtensions.KeyUsage_Id;
        this.critical = critical.booleanValue();
        byte[] extValue = (byte[]) value;
        if (extValue[0] == 4) {
            this.extensionValue = new DerValue(extValue).getOctetString();
        } else {
            this.extensionValue = extValue;
        }
        this.bitString = new DerValue(this.extensionValue).getUnalignedBitString().toBooleanArray();
    }

    public KeyUsageExtension() {
        this.extensionId = PKIXExtensions.KeyUsage_Id;
        this.critical = true;
        this.bitString = new boolean[0];
    }

    public void set(String name, Object obj) throws IOException {
        if (obj instanceof Boolean) {
            boolean val = ((Boolean) obj).booleanValue();
            if (name.equalsIgnoreCase(DIGITAL_SIGNATURE)) {
                set(0, val);
            } else if (name.equalsIgnoreCase(NON_REPUDIATION)) {
                set(1, val);
            } else if (name.equalsIgnoreCase(KEY_ENCIPHERMENT)) {
                set(2, val);
            } else if (name.equalsIgnoreCase(DATA_ENCIPHERMENT)) {
                set(3, val);
            } else if (name.equalsIgnoreCase(KEY_AGREEMENT)) {
                set(4, val);
            } else if (name.equalsIgnoreCase(KEY_CERTSIGN)) {
                set(5, val);
            } else if (name.equalsIgnoreCase(CRL_SIGN)) {
                set(6, val);
            } else if (name.equalsIgnoreCase(ENCIPHER_ONLY)) {
                set(7, val);
            } else if (name.equalsIgnoreCase(DECIPHER_ONLY)) {
                set(8, val);
            } else {
                throw new IOException("Attribute name not recognized by CertAttrSet:KeyUsage.");
            }
            encodeThis();
            return;
        }
        throw new IOException("Attribute must be of type Boolean.");
    }

    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(DIGITAL_SIGNATURE)) {
            return Boolean.valueOf(isSet(0));
        }
        if (name.equalsIgnoreCase(NON_REPUDIATION)) {
            return Boolean.valueOf(isSet(1));
        }
        if (name.equalsIgnoreCase(KEY_ENCIPHERMENT)) {
            return Boolean.valueOf(isSet(2));
        }
        if (name.equalsIgnoreCase(DATA_ENCIPHERMENT)) {
            return Boolean.valueOf(isSet(3));
        }
        if (name.equalsIgnoreCase(KEY_AGREEMENT)) {
            return Boolean.valueOf(isSet(4));
        }
        if (name.equalsIgnoreCase(KEY_CERTSIGN)) {
            return Boolean.valueOf(isSet(5));
        }
        if (name.equalsIgnoreCase(CRL_SIGN)) {
            return Boolean.valueOf(isSet(6));
        }
        if (name.equalsIgnoreCase(ENCIPHER_ONLY)) {
            return Boolean.valueOf(isSet(7));
        }
        if (name.equalsIgnoreCase(DECIPHER_ONLY)) {
            return Boolean.valueOf(isSet(8));
        }
        throw new IOException("Attribute name not recognized by CertAttrSet:KeyUsage.");
    }

    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(DIGITAL_SIGNATURE)) {
            set(0, false);
        } else if (name.equalsIgnoreCase(NON_REPUDIATION)) {
            set(1, false);
        } else if (name.equalsIgnoreCase(KEY_ENCIPHERMENT)) {
            set(2, false);
        } else if (name.equalsIgnoreCase(DATA_ENCIPHERMENT)) {
            set(3, false);
        } else if (name.equalsIgnoreCase(KEY_AGREEMENT)) {
            set(4, false);
        } else if (name.equalsIgnoreCase(KEY_CERTSIGN)) {
            set(5, false);
        } else if (name.equalsIgnoreCase(CRL_SIGN)) {
            set(6, false);
        } else if (name.equalsIgnoreCase(ENCIPHER_ONLY)) {
            set(7, false);
        } else if (name.equalsIgnoreCase(DECIPHER_ONLY)) {
            set(8, false);
        } else {
            throw new IOException("Attribute name not recognized by CertAttrSet:KeyUsage.");
        }
        encodeThis();
    }

    public String toString() {
        String s = super.toString() + "KeyUsage [\n";
        try {
            if (isSet(0)) {
                s = s + "  DigitalSignature\n";
            }
            if (isSet(1)) {
                s = s + "  Non_repudiation\n";
            }
            if (isSet(2)) {
                s = s + "  Key_Encipherment\n";
            }
            if (isSet(3)) {
                s = s + "  Data_Encipherment\n";
            }
            if (isSet(4)) {
                s = s + "  Key_Agreement\n";
            }
            if (isSet(5)) {
                s = s + "  Key_CertSign\n";
            }
            if (isSet(6)) {
                s = s + "  Crl_Sign\n";
            }
            if (isSet(7)) {
                s = s + "  Encipher_Only\n";
            }
            if (isSet(8)) {
                s = s + "  Decipher_Only\n";
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return s + "]\n";
    }

    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.KeyUsage_Id;
            this.critical = true;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }

    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(DIGITAL_SIGNATURE);
        elements.addElement(NON_REPUDIATION);
        elements.addElement(KEY_ENCIPHERMENT);
        elements.addElement(DATA_ENCIPHERMENT);
        elements.addElement(KEY_AGREEMENT);
        elements.addElement(KEY_CERTSIGN);
        elements.addElement(CRL_SIGN);
        elements.addElement(ENCIPHER_ONLY);
        elements.addElement(DECIPHER_ONLY);
        return elements.elements();
    }

    public boolean[] getBits() {
        return (boolean[]) this.bitString.clone();
    }

    public String getName() {
        return NAME;
    }
}
