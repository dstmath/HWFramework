package org.bouncycastle.est;

public interface ESTClientProvider {
    boolean isTrusted();

    ESTClient makeClient() throws ESTException;
}
