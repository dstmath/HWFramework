package com.android.org.bouncycastle.jcajce.util;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class DefaultJcaJceHelperEx {
    private DefaultJcaJceHelper mDefaultJcaJceHelper = new DefaultJcaJceHelper();

    public Cipher createCipher(String var1) throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException {
        return this.mDefaultJcaJceHelper.createCipher(var1);
    }
}
