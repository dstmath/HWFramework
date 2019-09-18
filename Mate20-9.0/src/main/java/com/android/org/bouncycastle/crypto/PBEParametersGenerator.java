package com.android.org.bouncycastle.crypto;

import com.android.org.bouncycastle.util.Strings;

public abstract class PBEParametersGenerator {
    protected int iterationCount;
    protected byte[] password;
    protected byte[] salt;

    public abstract CipherParameters generateDerivedMacParameters(int i);

    public abstract CipherParameters generateDerivedParameters(int i);

    public abstract CipherParameters generateDerivedParameters(int i, int i2);

    protected PBEParametersGenerator() {
    }

    public void init(byte[] password2, byte[] salt2, int iterationCount2) {
        this.password = password2;
        this.salt = salt2;
        this.iterationCount = iterationCount2;
    }

    public byte[] getPassword() {
        return this.password;
    }

    public byte[] getSalt() {
        return this.salt;
    }

    public int getIterationCount() {
        return this.iterationCount;
    }

    public static byte[] PKCS5PasswordToBytes(char[] password2) {
        if (password2 == null) {
            return new byte[0];
        }
        byte[] bytes = new byte[password2.length];
        for (int i = 0; i != bytes.length; i++) {
            bytes[i] = (byte) password2[i];
        }
        return bytes;
    }

    public static byte[] PKCS5PasswordToUTF8Bytes(char[] password2) {
        if (password2 != null) {
            return Strings.toUTF8ByteArray(password2);
        }
        return new byte[0];
    }

    public static byte[] PKCS12PasswordToBytes(char[] password2) {
        if (password2 == null || password2.length <= 0) {
            return new byte[0];
        }
        byte[] bytes = new byte[((password2.length + 1) * 2)];
        for (int i = 0; i != password2.length; i++) {
            bytes[i * 2] = (byte) (password2[i] >>> 8);
            bytes[(i * 2) + 1] = (byte) password2[i];
        }
        return bytes;
    }
}
