package javax.net.ssl;

import java.util.Enumeration;

public interface SSLSessionContext {
    Enumeration<byte[]> getIds();

    SSLSession getSession(byte[] bArr);

    int getSessionCacheSize();

    int getSessionTimeout();

    void setSessionCacheSize(int i) throws IllegalArgumentException;

    void setSessionTimeout(int i) throws IllegalArgumentException;
}
