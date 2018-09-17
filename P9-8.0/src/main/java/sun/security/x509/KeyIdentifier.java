package sun.security.x509;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import sun.misc.HexDumpEncoder;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;

public class KeyIdentifier {
    private byte[] octetString;

    public KeyIdentifier(byte[] octetString) {
        this.octetString = (byte[]) octetString.clone();
    }

    public KeyIdentifier(DerValue val) throws IOException {
        this.octetString = val.getOctetString();
    }

    public KeyIdentifier(PublicKey pubKey) throws IOException {
        DerValue algAndKey = new DerValue(pubKey.getEncoded());
        if (algAndKey.tag != (byte) 48) {
            throw new IOException("PublicKey value is not a valid X.509 public key");
        }
        AlgorithmId algid = AlgorithmId.parse(algAndKey.data.getDerValue());
        byte[] key = algAndKey.data.getUnalignedBitString().toByteArray();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.update(key);
            this.octetString = md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA1 not supported");
        }
    }

    public byte[] getIdentifier() {
        return (byte[]) this.octetString.clone();
    }

    public String toString() {
        HexDumpEncoder encoder = new HexDumpEncoder();
        return ("KeyIdentifier [\n" + encoder.encodeBuffer(this.octetString)) + "]\n";
    }

    void encode(DerOutputStream out) throws IOException {
        out.putOctetString(this.octetString);
    }

    public int hashCode() {
        int retval = 0;
        for (int i = 0; i < this.octetString.length; i++) {
            retval += this.octetString[i] * i;
        }
        return retval;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof KeyIdentifier) {
            return Arrays.equals(this.octetString, ((KeyIdentifier) other).getIdentifier());
        }
        return false;
    }
}
