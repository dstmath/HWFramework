package com.huawei.wallet.sdk.common.utils.crypto;

import android.text.TextUtils;
import android.util.Base64;
import com.huawei.wallet.sdk.common.log.LogC;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AES {
    public static final String AES_ALGORITHM_CBC = "AES/CBC/PKCS5Padding";
    public static final String CHAR_ENCODING = "UTF-8";
    public static final int size = 128;

    private AES() {
    }

    public static String getaeskey() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        return byte2HexStr(key);
    }

    public static String getAesIV() {
        byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        return byte2HexStr(key);
    }

    public static String byte2HexStr(byte[] b) {
        String str;
        StringBuilder sb = new StringBuilder("");
        for (byte b2 : b) {
            String stmp = Integer.toHexString(b2 & 255);
            if (stmp.length() == 1) {
                str = "0" + stmp;
            } else {
                str = stmp;
            }
            sb.append(str);
        }
        return sb.toString().toUpperCase(Locale.US).trim();
    }

    private static byte[] encrypt(byte[] data, byte[] key, String algorithm) {
        return encrypt(data, key, key, algorithm);
    }

    private static byte[] encrypt(byte[] data, byte[] key, byte[] ivbyte, String algorithm) {
        Cipher cipher;
        boolean z = true;
        if (data == null || key == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("AES enrypt, invalid params, data: ");
            sb.append(data == null);
            sb.append(" key: ");
            if (key != null) {
                z = false;
            }
            sb.append(z);
            LogC.e(sb.toString(), false);
            return new byte[0];
        } else if (ivbyte.length != 16) {
            LogC.e("AES enrypt, Invalid AES iv length (must be 16 bytes)", false);
            return new byte[0];
        } else {
            try {
                SecretKeySpec seckey = new SecretKeySpec(new SecretKeySpec(key, "AES").getEncoded(), "AES");
                if (!TextUtils.isEmpty(algorithm)) {
                    if (!AES_ALGORITHM_CBC.equals(algorithm)) {
                        cipher = Cipher.getInstance(algorithm);
                        cipher.init(1, seckey);
                        return cipher.doFinal(data);
                    }
                }
                cipher = Cipher.getInstance(AES_ALGORITHM_CBC);
                cipher.init(1, seckey, new IvParameterSpec(ivbyte));
                return cipher.doFinal(data);
            } catch (NoSuchPaddingException e) {
                LogC.e("encrypt NoSuchPaddingException.", false);
                return new byte[0];
            } catch (NoSuchAlgorithmException e2) {
                LogC.e("encrypt NoSuchAlgorithmException.", false);
                return new byte[0];
            } catch (InvalidAlgorithmParameterException e3) {
                LogC.e("encrypt InvalidAlgorithmParameterException.", false);
                return new byte[0];
            } catch (InvalidKeyException e4) {
                LogC.e("encrypt InvalidKeyException.", false);
                return new byte[0];
            } catch (BadPaddingException e5) {
                LogC.e("encrypt BadPaddingException.", false);
                return new byte[0];
            } catch (IllegalBlockSizeException e6) {
                LogC.e("encrypt IllegalBlockSizeException.", false);
                return new byte[0];
            } catch (Exception e7) {
                LogC.i("encrypt Exception::" + e7, true);
                return new byte[0];
            }
        }
    }

    private static byte[] encrypt(byte[] data, byte[] key, byte[] iv) {
        boolean z = true;
        if (data == null || key == null || iv == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("AES enrypt, invalid params, data: ");
            sb.append(data == null);
            sb.append(" key: ");
            sb.append(key == null);
            sb.append(" iv:");
            if (iv != null) {
                z = false;
            }
            sb.append(z);
            LogC.e(sb.toString(), false);
            return new byte[0];
        } else if (key.length != 16) {
            LogC.e("Invalid AES key length (must be 16 bytes)", false);
            return new byte[0];
        } else {
            try {
                SecretKeySpec seckey = new SecretKeySpec(new SecretKeySpec(key, "AES").getEncoded(), "AES");
                Cipher cipher = Cipher.getInstance(AES_ALGORITHM_CBC);
                cipher.init(1, seckey, new IvParameterSpec(iv));
                return cipher.doFinal(data);
            } catch (NoSuchPaddingException e) {
                LogC.e("encrypt NoSuchPaddingException.", false);
                return new byte[0];
            } catch (NoSuchAlgorithmException e2) {
                LogC.e("encrypt NoSuchAlgorithmException.", false);
                return new byte[0];
            } catch (InvalidAlgorithmParameterException e3) {
                LogC.e("encrypt InvalidAlgorithmParameterException.", false);
                return new byte[0];
            } catch (InvalidKeyException e4) {
                LogC.e("encrypt InvalidKeyException.", false);
                return new byte[0];
            } catch (BadPaddingException e5) {
                LogC.e("encrypt BadPaddingException.", false);
                return new byte[0];
            } catch (IllegalBlockSizeException e6) {
                LogC.e("encrypt IllegalBlockSizeException.", false);
                return new byte[0];
            } catch (Exception e7) {
                LogC.i("encrypt Exception::" + e7, true);
                return new byte[0];
            }
        }
    }

    public static byte[] decrypt(byte[] data, byte[] key, byte[] iv) {
        boolean z = true;
        if (data == null || key == null || iv == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("AES decrypt, Invalid params, data: ");
            sb.append(data == null);
            sb.append(" key: ");
            sb.append(key == null);
            sb.append(" iv:");
            if (iv != null) {
                z = false;
            }
            sb.append(z);
            LogC.e(sb.toString(), false);
            return new byte[0];
        } else if (key.length != 16) {
            LogC.e("AES decrypt, Invalid AES key length (must be 16 bytes)", false);
            return new byte[0];
        } else {
            try {
                SecretKeySpec seckey = new SecretKeySpec(new SecretKeySpec(key, "AES").getEncoded(), "AES");
                Cipher cipher = Cipher.getInstance(AES_ALGORITHM_CBC);
                cipher.init(2, seckey, new IvParameterSpec(iv));
                return cipher.doFinal(data);
            } catch (IllegalArgumentException e) {
                LogC.i("decrypt IllegalArgumentException::" + e, true);
                return new byte[0];
            } catch (NoSuchPaddingException e2) {
                LogC.i("decrypt NoSuchPaddingException::" + e2, true);
                return new byte[0];
            } catch (NoSuchAlgorithmException e3) {
                LogC.i("decrypt NoSuchAlgorithmException::" + e3, true);
                return new byte[0];
            } catch (InvalidKeyException e4) {
                LogC.i("decrypt InvalidKeyException::" + e4, true);
                return new byte[0];
            } catch (BadPaddingException e5) {
                LogC.i("decrypt BadPaddingException::" + e5, true);
                return new byte[0];
            } catch (IllegalBlockSizeException e6) {
                LogC.i("decrypt IllegalBlockSizeException::" + e6, true);
                return new byte[0];
            } catch (InvalidAlgorithmParameterException e7) {
                LogC.i("decrypt InvalidAlgorithmParameterException::" + e7, true);
                return new byte[0];
            } catch (Exception e8) {
                LogC.i("decrypt Exception::" + e8, true);
                return new byte[0];
            }
        }
    }

    public static String encryptToBase64(String data, String key) {
        return encryptToBase64(data, key, AES_ALGORITHM_CBC);
    }

    private static String encryptToBase64(String data, String key, String algorithm) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(key)) {
            LogC.e("AES encrypt, Invalid params, data or key is empty", false);
            return null;
        }
        try {
            return Base64.encodeToString(encrypt(data.getBytes(CHAR_ENCODING), asBin(key), algorithm), 2);
        } catch (UnsupportedEncodingException e) {
            LogC.e("encrypt UnsupportedEncodingException.", false);
            return null;
        } catch (Exception e2) {
            LogC.i("encryptToBase64 Exception::" + e2, true);
            return null;
        }
    }

    public static byte[] encryptToByte(byte[] data, String key) {
        if (data == null || data.length == 0) {
            LogC.e("AES encrypt, Invalid params, data or key is empty", false);
            return new byte[0];
        }
        try {
            return encrypt(data, asBin(key), AES_ALGORITHM_CBC);
        } catch (Exception e) {
            LogC.i("encryptToByte Exception::" + e, true);
            return new byte[0];
        }
    }

    public static String encryptToBase64WithIv(String data, String key, String iv) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(key) || TextUtils.isEmpty(iv)) {
            LogC.e("AES encrypt, Invalid params, data, key or iv is empty", false);
            return null;
        }
        try {
            return Base64.encodeToString(encrypt(data.getBytes(CHAR_ENCODING), asBin(key), asBin(iv)), 2);
        } catch (UnsupportedEncodingException e) {
            LogC.e("encrypt UnsupportedEncodingException.", false);
            return null;
        } catch (Exception e2) {
            LogC.i("encrypt Exception::" + e2, true);
            return null;
        }
    }

    public static String encryptToHex(String data, String key, String iv, String algorithm) {
        try {
            return asHex(encrypt(data.getBytes(CHAR_ENCODING), asBin(key), asBin(iv), algorithm));
        } catch (UnsupportedEncodingException e) {
            LogC.e("encrypt UnsupportedEncodingException.", false);
            return null;
        } catch (Exception e2) {
            LogC.i("encrypt Exception::" + e2, true);
            return null;
        }
    }

    public static String decryptFromBase64WithIv(String data, String key, String iv) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(key) || TextUtils.isEmpty(iv)) {
            LogC.e("AES decrypt, Invalid params, data, key or iv is empty", false);
            return null;
        }
        try {
            byte[] valueByte = decrypt(Base64.decode(data, 2), asBin(key), asBin(iv));
            if (valueByte != null) {
                return new String(valueByte, CHAR_ENCODING);
            }
            LogC.e(LogC.LOG_HWSDK_TAG, "decryptFromBase64 decrypt data is null", false);
            return null;
        } catch (UnsupportedEncodingException e) {
            LogC.e("decryptFromBase64 UnsupportedEncodingException", false);
            return null;
        } catch (Exception e2) {
            LogC.i("decryptFromBase64 Exception::" + e2, true);
            return null;
        }
    }

    public static String asHex(byte[] buf) {
        StringBuffer strbuf = new StringBuffer(buf.length * 2);
        for (int i = 0; i < buf.length; i++) {
            if ((buf[i] & 255) < 16) {
                strbuf.append('0');
            }
            strbuf.append(Long.toString((long) (buf[i] & 255), 16));
        }
        return strbuf.toString();
    }

    public static byte[] asBin(String hexStr) {
        if (hexStr.length() < 1) {
            return new byte[0];
        }
        byte[] binByte = new byte[(hexStr.length() / 2)];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, (i * 2) + 1), 16);
            binByte[i] = (byte) ((high * 16) + Integer.parseInt(hexStr.substring((i * 2) + 1, (i * 2) + 2), 16));
        }
        return binByte;
    }
}
