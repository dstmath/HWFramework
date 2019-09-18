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

    public KeyRep(Type type2, String algorithm2, String format2, byte[] encoded2) {
        if (type2 == null || algorithm2 == null || format2 == null || encoded2 == null) {
            throw new NullPointerException("invalid null input(s)");
        }
        this.type = type2;
        this.algorithm = algorithm2;
        this.format = format2.toUpperCase(Locale.ENGLISH);
        this.encoded = (byte[]) encoded2.clone();
    }

    /* access modifiers changed from: protected */
    public Object readResolve() throws ObjectStreamException {
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
        } catch (NotSerializableException nse) {
            throw nse;
        } catch (Exception e) {
            NotSerializableException nse2 = new NotSerializableException("java.security.Key: [" + this.type + "] [" + this.algorithm + "] [" + this.format + "]");
            nse2.initCause(e);
            throw nse2;
        }
    }
}
