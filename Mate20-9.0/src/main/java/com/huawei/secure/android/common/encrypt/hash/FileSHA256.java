package com.huawei.secure.android.common.encrypt.hash;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.secure.android.common.util.HexUtil;
import com.huawei.secure.android.common.util.IOUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileSHA256 {
    private static final int BUFFERSIZE = 8192;
    private static final String DEFAULTALGORITHIM = "SHA-256";
    private static final String EMPTY = "";
    private static final String[] SAFE_ALGORITHM = {"SHA-256", "SHA-384", "SHA-512"};
    private static final String TAG = "FileSHA256";

    public static String fileSHA256Encrypt(File file) {
        return fileSHAEncrypt(file, "SHA-256");
    }

    public static String fileSHAEncrypt(File file, String algorithm) {
        if (TextUtils.isEmpty(algorithm) || !isLegalAlgorithm(algorithm)) {
            Log.e(TAG, "algorithm is empty or not safe");
            return "";
        } else if (!isValidFile(file)) {
            Log.e(TAG, "file is not valid");
            return "";
        } else {
            String hashValue = null;
            FileInputStream fis = null;
            try {
                MessageDigest md = MessageDigest.getInstance(algorithm);
                fis = new FileInputStream(file);
                byte[] buffer = new byte[BUFFERSIZE];
                boolean hasUpdate = false;
                while (true) {
                    int read = fis.read(buffer);
                    int length = read;
                    if (read <= 0) {
                        break;
                    }
                    md.update(buffer, 0, length);
                    hasUpdate = true;
                }
                if (hasUpdate) {
                    hashValue = HexUtil.byteArray2HexStr(md.digest());
                }
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "NoSuchAlgorithmException" + e.getMessage());
            } catch (IOException e2) {
                Log.e(TAG, "IOException" + e2.getMessage());
            } catch (Throwable th) {
                IOUtil.closeSecure((InputStream) null);
                throw th;
            }
            IOUtil.closeSecure((InputStream) fis);
            return hashValue;
        }
    }

    public static String inputStreamSHA256Encrypt(InputStream is) {
        if (is == null) {
            return "";
        }
        return inputStreamSHAEncrypt(is, "SHA-256");
    }

    /* JADX INFO: finally extract failed */
    public static String inputStreamSHAEncrypt(InputStream is, String algorithm) {
        if (is == null) {
            return "";
        }
        byte[] buffer = new byte[BUFFERSIZE];
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            while (true) {
                int read = is.read(buffer);
                int length = read;
                if (read < 0) {
                    String byteArray2HexStr = HexUtil.byteArray2HexStr(md.digest());
                    IOUtil.closeSecure(is);
                    return byteArray2HexStr;
                } else if (length > 0) {
                    md.update(buffer, 0, length);
                }
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            Log.e(TAG, "inputstraem exception");
            IOUtil.closeSecure(is);
            return "";
        } catch (Throwable th) {
            IOUtil.closeSecure(is);
            throw th;
        }
    }

    public static boolean validateFileSHA256(File file, String hashValue) {
        if (TextUtils.isEmpty(hashValue)) {
            return false;
        }
        return hashValue.equals(fileSHA256Encrypt(file));
    }

    public static boolean validateFileSHA(File file, String hashValue, String algorithm) {
        if (!TextUtils.isEmpty(hashValue) && isLegalAlgorithm(algorithm)) {
            return hashValue.equals(fileSHAEncrypt(file, algorithm));
        }
        Log.e(TAG, "hash value is null || algorithm is illegal");
        return false;
    }

    public static boolean validateInputStreamSHA256(InputStream is, String hashValue) {
        if (TextUtils.isEmpty(hashValue)) {
            return false;
        }
        return hashValue.equals(inputStreamSHA256Encrypt(is));
    }

    public static boolean validateInputStreamSHA(InputStream is, String hashValue, String algorithm) {
        if (!TextUtils.isEmpty(hashValue) && isLegalAlgorithm(algorithm)) {
            return hashValue.equals(inputStreamSHAEncrypt(is, algorithm));
        }
        Log.e(TAG, "hash value is null || algorithm is illegal");
        return false;
    }

    private static boolean isValidFile(File file) {
        return file != null && file.exists() && file.length() > 0;
    }

    private static boolean isLegalAlgorithm(String algorithm) {
        for (String alg : SAFE_ALGORITHM) {
            if (alg.equals(algorithm)) {
                return true;
            }
        }
        return false;
    }
}
