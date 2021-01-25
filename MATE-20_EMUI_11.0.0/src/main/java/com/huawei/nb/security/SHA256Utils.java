package com.huawei.nb.security;

import com.huawei.nb.coordinator.helper.BusinessTypeEnum;
import com.huawei.nb.utils.file.FileUtils;
import com.huawei.nb.utils.logger.DSLog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Utils {
    private static final int BUFFER_SIZE = 16384;
    private static final String TAG = "SHA256Utils";

    public static String sha256Encrypt(String str) {
        if (str != null) {
            try {
                MessageDigest instance = MessageDigest.getInstance("SHA-256");
                instance.update(str.getBytes("UTF8"));
                byte[] digest = instance.digest();
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    String hexString = Integer.toHexString(b & 255);
                    if (hexString.length() == 1) {
                        sb.append('0');
                    }
                    sb.append(hexString);
                }
                return sb.toString();
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException unused) {
                DSLog.e("SHA256Utils An error happens when sha256Encrypting", new Object[0]);
            }
        }
        return null;
    }

    public static String getFileSha256(File file) {
        FileInputStream fileInputStream;
        BufferedInputStream bufferedInputStream;
        Throwable th;
        int i;
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            fileInputStream = FileUtils.openInputStream(file);
            try {
                bufferedInputStream = new BufferedInputStream(fileInputStream);
            } catch (IOException | NoSuchAlgorithmException unused) {
                bufferedInputStream = null;
                try {
                    DSLog.e("SHA256Utils An error happens when getting FileSha256", new Object[0]);
                    FileUtils.closeCloseable(bufferedInputStream);
                    FileUtils.closeCloseable(fileInputStream);
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    FileUtils.closeCloseable(bufferedInputStream);
                    FileUtils.closeCloseable(fileInputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bufferedInputStream = null;
                FileUtils.closeCloseable(bufferedInputStream);
                FileUtils.closeCloseable(fileInputStream);
                throw th;
            }
            try {
                byte[] bArr = new byte[BUFFER_SIZE];
                int available = fileInputStream.available();
                do {
                    if (available < BUFFER_SIZE) {
                        bArr = new byte[available];
                        i = bufferedInputStream.read(bArr, 0, available);
                    } else {
                        i = bufferedInputStream.read(bArr, 0, BUFFER_SIZE);
                    }
                    instance.update(bArr);
                    available -= i;
                } while (available > 0);
                String bytes2Hex = bytes2Hex(instance.digest());
                FileUtils.closeCloseable(bufferedInputStream);
                FileUtils.closeCloseable(fileInputStream);
                return bytes2Hex;
            } catch (IOException | NoSuchAlgorithmException unused2) {
                DSLog.e("SHA256Utils An error happens when getting FileSha256", new Object[0]);
                FileUtils.closeCloseable(bufferedInputStream);
                FileUtils.closeCloseable(fileInputStream);
                return null;
            }
        } catch (IOException | NoSuchAlgorithmException unused3) {
            fileInputStream = null;
            bufferedInputStream = null;
            DSLog.e("SHA256Utils An error happens when getting FileSha256", new Object[0]);
            FileUtils.closeCloseable(bufferedInputStream);
            FileUtils.closeCloseable(fileInputStream);
            return null;
        } catch (Throwable th4) {
            th = th4;
            fileInputStream = null;
            bufferedInputStream = null;
            FileUtils.closeCloseable(bufferedInputStream);
            FileUtils.closeCloseable(fileInputStream);
            throw th;
        }
    }

    private static String bytes2Hex(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        if (bArr == null) {
            return null;
        }
        for (byte b : bArr) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() == 1) {
                sb.append(BusinessTypeEnum.BIZ_TYPE_POLICY);
            }
            sb.append(hexString);
        }
        return sb.toString();
    }
}
