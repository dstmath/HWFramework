package sun.security.internal.interfaces;

import javax.crypto.SecretKey;

@Deprecated
public interface TlsMasterSecret extends SecretKey {
    public static final long serialVersionUID = -461748105810469773L;

    int getMajorVersion();

    int getMinorVersion();
}
