package com.huawei.coauth.auth;

import java.util.Arrays;

public class PasswordInfo {
    private byte[] mPassword = new byte[0];
    private int mPasswordType;

    public enum LockScreenType {
        NONE,
        MIXED,
        NUMBER,
        PIN_FOUR,
        PIN_SIX,
        PATTERN
    }

    public byte[] getPassword() {
        return (byte[]) this.mPassword.clone();
    }

    public void setPassword(byte[] passwd) {
        this.mPassword = passwd != null ? (byte[]) passwd.clone() : new byte[0];
    }

    public int getPassType() {
        return this.mPasswordType;
    }

    public void setPassType(int passType) {
        this.mPasswordType = passType;
    }

    public void clearPassword() {
        byte[] bArr = this.mPassword;
        if (bArr != null) {
            Arrays.fill(bArr, (byte) 0);
        }
    }
}
