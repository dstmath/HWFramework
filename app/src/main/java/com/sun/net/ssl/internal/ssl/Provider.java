package com.sun.net.ssl.internal.ssl;

import sun.security.ssl.SunJSSE;

public final class Provider extends SunJSSE {
    private static final long serialVersionUID = 3231825739635378733L;

    public Provider(java.security.Provider cryptoProvider) {
        super(cryptoProvider);
    }

    public Provider(String cryptoProvider) {
        super(cryptoProvider);
    }

    public static synchronized boolean isFIPS() {
        boolean isFIPS;
        synchronized (Provider.class) {
            isFIPS = SunJSSE.isFIPS();
        }
        return isFIPS;
    }

    public static synchronized void install() {
        synchronized (Provider.class) {
        }
    }
}
