package com.huawei.theme.a.a;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class b {
    public static byte[] a(String str, byte[] bArr) {
        try {
            Cipher instance = Cipher.getInstance("AES/CBC/NoPadding");
            int blockSize = instance.getBlockSize();
            int length = bArr.length;
            if (length % blockSize != 0) {
                length += blockSize - (length % blockSize);
            }
            Object obj = new byte[length];
            System.arraycopy(bArr, 0, obj, 0, bArr.length);
            instance.init(1, new SecretKeySpec(str.getBytes("UTF-8"), "AES"), new IvParameterSpec(str.getBytes("UTF-8")));
            return instance.doFinal(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
