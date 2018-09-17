package java.security;

import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Locale;
import javax.crypto.spec.SecretKeySpec;

public class KeyRep implements Serializable {
    private static final String PKCS8 = "PKCS#8";
    private static final String RAW = "RAW";
    private static final String X509 = "X.509";
    private static final long serialVersionUID = -4757683898830641853L;
    private String algorithm;
    private byte[] encoded;
    private String format;
    private Type type;

    public enum Type {
        SECRET,
        PUBLIC,
        PRIVATE
    }

    public KeyRep(Type type, String algorithm, String format, byte[] encoded) {
        if (type == null || algorithm == null || format == null || encoded == null) {
            throw new NullPointerException("invalid null input(s)");
        }
        this.type = type;
        this.algorithm = algorithm;
        this.format = format.toUpperCase(Locale.ENGLISH);
        this.encoded = (byte[]) encoded.clone();
    }

    protected Object readResolve() throws ObjectStreamException {
        NotSerializableException nse;
        try {
            if (this.type == Type.SECRET && RAW.equals(this.format)) {
                return new SecretKeySpec(this.encoded, this.algorithm);
            }
            if (this.type == Type.PUBLIC && X509.equals(this.format)) {
                return KeyFactory.getInstance(this.algorithm).generatePublic(new X509EncodedKeySpec(this.encoded));
            }
            if (this.type == Type.PRIVATE && PKCS8.equals(this.format)) {
                return KeyFactory.getInstance(this.algorithm).generatePrivate(new PKCS8EncodedKeySpec(this.encoded));
            }
            throw new NotSerializableException("unrecognized type/format combination: " + this.type + "/" + this.format);
        } catch (NotSerializableException nse2) {
            throw nse2;
        } catch (Exception e) {
            nse2 = new NotSerializableException("java.security.Key: [" + this.type + "] " + "[" + this.algorithm + "] " + "[" + this.format + "]");
            nse2.initCause(e);
            throw nse2;
        }
    }
}
