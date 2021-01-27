package com.android.org.bouncycastle.jce.provider;

import java.security.Security;

public class BouncyCastleProviderEx {
    public static void addProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }
}
