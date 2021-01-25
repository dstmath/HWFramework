package org.bouncycastle.est;

public interface TLSUniqueProvider {
    byte[] getTLSUnique();

    boolean isTLSUniqueAvailable();
}
