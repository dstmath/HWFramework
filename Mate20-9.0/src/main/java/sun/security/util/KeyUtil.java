package sun.security.util;

import java.security.Key;
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.security.spec.ECParameterSpec;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHKey;

public final class KeyUtil {
    public static final int getKeySize(Key key) {
        int size = -1;
        if (key instanceof Length) {
            try {
                size = ((Length) key).length();
            } catch (UnsupportedOperationException e) {
            }
            if (size >= 0) {
                return size;
            }
        }
        if (key instanceof SecretKey) {
            SecretKey sk = (SecretKey) key;
            if ("RAW".equals(sk.getFormat()) && sk.getEncoded() != null) {
                size = sk.getEncoded().length * 8;
            }
        } else if (key instanceof RSAKey) {
            size = ((RSAKey) key).getModulus().bitLength();
        } else if (key instanceof ECKey) {
            ECParameterSpec params = ((ECKey) key).getParams();
            if (params != null) {
                size = params.getOrder().bitLength();
            }
        } else if (key instanceof DSAKey) {
            DSAParams params2 = ((DSAKey) key).getParams();
            size = params2 != null ? params2.getP().bitLength() : -1;
        } else if (key instanceof DHKey) {
            size = ((DHKey) key).getParams().getP().bitLength();
        }
        return size;
    }
}
