package com.huawei.nb.security;

import com.huawei.nb.coordinator.helper.BusinessTypeEnum;
import com.huawei.nb.utils.file.FileUtils;
import com.huawei.nb.utils.logger.DSLog;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Utils {
    private static int BUFFERSIZE = 16384;
    private static final String TAG = "SHA256Utils";

    public static String sha256Encrypt(String strTxt) {
        if (strTxt == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(strTxt.getBytes("UTF8"));
            byte[] byteBuffer = digest.digest();
            StringBuilder strHexString = new StringBuilder();
            for (byte key : byteBuffer) {
                String hex = Integer.toHexString(key & 255);
                if (hex.length() == 1) {
                    strHexString.append('0');
                }
                strHexString.append(hex);
            }
            return strHexString.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            DSLog.e("SHA256Utils An error happens when sha256Encrypting", new Object[0]);
            return null;
        }
    }

    public static String getFileSha256(File file) {
        int length;
        InputStream fis = null;
        InputStream bufferedInputStream = null;
        try {
            MessageDigest messagedigest = MessageDigest.getInstance("SHA-256");
            fis = FileUtils.openInputStream(file);
            InputStream bufferedInputStream2 = new BufferedInputStream(fis);
            try {
                byte[] buffer = new byte[BUFFERSIZE];
                int sum = fis.available();
                do {
                    if (sum < BUFFERSIZE) {
                        buffer = new byte[sum];
                        length = bufferedInputStream2.read(buffer, 0, sum);
                    } else {
                        length = bufferedInputStream2.read(buffer, 0, BUFFERSIZE);
                    }
                    messagedigest.update(buffer);
                    sum -= length;
                } while (sum > 0);
                String bytes2Hex = bytes2Hex(messagedigest.digest());
                closeStream(bufferedInputStream2);
                closeStream(fis);
                InputStream inputStream = bufferedInputStream2;
                return bytes2Hex;
            } catch (IOException e) {
                e = e;
                bufferedInputStream = bufferedInputStream2;
            } catch (NoSuchAlgorithmException e2) {
                e = e2;
                bufferedInputStream = bufferedInputStream2;
                NoSuchAlgorithmException noSuchAlgorithmException = e;
                try {
                    DSLog.e("SHA256Utils An error happens when getting FileSha256", new Object[0]);
                    closeStream(bufferedInputStream);
                    closeStream(fis);
                    return null;
                } catch (Throwable th) {
                    th = th;
                    closeStream(bufferedInputStream);
                    closeStream(fis);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                bufferedInputStream = bufferedInputStream2;
                closeStream(bufferedInputStream);
                closeStream(fis);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            IOException iOException = e;
            DSLog.e("SHA256Utils An error happens when getting FileSha256", new Object[0]);
            closeStream(bufferedInputStream);
            closeStream(fis);
            return null;
        } catch (NoSuchAlgorithmException e4) {
            e = e4;
            NoSuchAlgorithmException noSuchAlgorithmException2 = e;
            DSLog.e("SHA256Utils An error happens when getting FileSha256", new Object[0]);
            closeStream(bufferedInputStream);
            closeStream(fis);
            return null;
        }
    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                DSLog.e("SHA256Utils An error happens when closing stream", new Object[0]);
            }
        }
    }

    private static String bytes2Hex(byte[] bts) {
        StringBuilder stb = new StringBuilder();
        if (bts == null) {
            return null;
        }
        for (byte bt : bts) {
            String tmp = Integer.toHexString(bt & 255);
            if (tmp.length() == 1) {
                stb.append(BusinessTypeEnum.BIZ_TYPE_POLICY);
            }
            stb.append(tmp);
        }
        return stb.toString();
    }
}
