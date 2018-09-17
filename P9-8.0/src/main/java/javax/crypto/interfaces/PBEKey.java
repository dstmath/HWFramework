package javax.crypto.interfaces;

import javax.crypto.SecretKey;

public interface PBEKey extends SecretKey {
    public static final long serialVersionUID = -1430015993304333921L;

    int getIterationCount();

    char[] getPassword();

    byte[] getSalt();
}
