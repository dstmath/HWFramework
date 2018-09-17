package com.huawei.android.pushagent.utils.a;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class a {
    private Key ej;
    private SecureRandom ek = new SecureRandom();

    private a() {
    }

    public static a nl(byte[] bArr) {
        a aVar = new a();
        aVar.nn(bArr);
        return aVar;
    }

    public void nn(byte[] bArr) {
        if (bArr.length == 16) {
            this.ej = new SecretKeySpec(bArr, "AES");
        }
    }

    public void nm(byte[] bArr) {
        this.ek.nextBytes(bArr);
    }

    private byte[] nh(byte[] bArr, Key key, int i) {
        int i2 = 16;
        if (bArr == null || key == null) {
            return new byte[0];
        }
        byte[] copyOf;
        Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
        byte[] bArr2 = new byte[16];
        if (i == 1) {
            nm(bArr2);
            i2 = 0;
        } else if (i != 2) {
            return new byte[0];
        } else {
            if (bArr.length <= 16) {
                return new byte[0];
            }
            for (int i3 = 0; i3 < 16; i3++) {
                bArr2[i3] = bArr[i3];
            }
        }
        instance.init(i, key, new IvParameterSpec(bArr2));
        byte[] doFinal = instance.doFinal(bArr, i2, bArr.length - i2);
        if (i == 1) {
            copyOf = Arrays.copyOf(bArr2, bArr2.length + doFinal.length);
            System.arraycopy(doFinal, 0, copyOf, bArr2.length, doFinal.length);
        } else {
            copyOf = doFinal;
        }
        return copyOf;
    }

    public byte[] nj(byte[] bArr) {
        return nh(bArr, this.ej, 1);
    }

    public byte[] ni(byte[] bArr) {
        return nh(bArr, this.ej, 2);
    }

    public byte[] nk(byte[] bArr, byte[] bArr2) {
        if (bArr == null || this.ej == null || bArr2 == null) {
            return new byte[0];
        }
        if (16 != bArr2.length) {
            return new byte[0];
        }
        Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
        instance.init(1, this.ej, new IvParameterSpec(bArr2));
        byte[] doFinal = instance.doFinal(bArr);
        byte[] copyOf = Arrays.copyOf(bArr2, bArr2.length + doFinal.length);
        System.arraycopy(doFinal, 0, copyOf, bArr2.length, doFinal.length);
        return copyOf;
    }
}
