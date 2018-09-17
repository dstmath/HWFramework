package android.rms.iaware;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class IAwareDecrypt {
    private static final int AES128_KEY_LEN = 16;
    private static final byte[] COMPONENT = new byte[]{(byte) -118, (byte) 80, (byte) -33, (byte) -103, (byte) 100, (byte) 101, (byte) 125, (byte) -35, (byte) -28, (byte) -46, (byte) -66, (byte) -15, (byte) -36, (byte) 5, (byte) -79, (byte) 115};
    private static final byte[] COMPONENT2 = new byte[]{(byte) -91, (byte) 55, (byte) 87, (byte) -46, (byte) 64, (byte) -10, (byte) 24, (byte) 58, (byte) 50, Byte.MIN_VALUE, (byte) -42, (byte) -77, (byte) -62, (byte) 118, (byte) 112, (byte) 29, (byte) -60, (byte) 16, (byte) -94, (byte) -35, (byte) 17, (byte) 46, (byte) 68, (byte) -80, (byte) 40, (byte) -58, (byte) -25, (byte) -90, (byte) -5, (byte) 36, (byte) -84, (byte) 27};
    private static final byte[] COMPONENT3 = new byte[]{(byte) 2, (byte) 13, (byte) 17, (byte) 7, (byte) -66, (byte) -97, (byte) 55, (byte) -95, (byte) 85, (byte) -25, (byte) 74, (byte) 56, (byte) 96, (byte) 112, (byte) -122, (byte) 66};
    private static final String COMPONENT_NAME_1 = "iaware_c.dat";
    private static final String COMPONENT_NAME_2 = "iaware_cm.dat";
    private static final String ENCYPTION_SCHEME = "AES";
    private static final String PKG_NAME = "com.huawei.iaware";
    private static final int STREAM_READ_SIZE = 1024;
    private static final String TAG = "IAwareDecrypt";
    private static final String XML_HEAD = "<?xml";
    private static final int XML_HEAD_LEN = 5;

    public static InputStream decryptInputStream(Context context, InputStream ins) {
        Throwable th;
        if (ins == null || context == null) {
            return ins;
        }
        InputStream inputStream;
        long start = System.currentTimeMillis();
        if (ins.markSupported()) {
            inputStream = ins;
        } else {
            inputStream = getByteArrayInputStream(ins);
        }
        if (inputStream == null) {
            return null;
        }
        if (isNormalXml(inputStream)) {
            return inputStream;
        }
        InputStream ret = null;
        Closeable cipherOutputStream = null;
        try {
            if (isStreamAvailable(inputStream)) {
                Cipher cipher = getCipher(context, inputStream);
                if (cipher == null) {
                    closeStream(null);
                    closeStream(inputStream);
                    return null;
                } else if (inputStream.skip(16) != 16) {
                    closeStream(null);
                    closeStream(inputStream);
                    return null;
                } else {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    CipherOutputStream cipherOutputStream2 = new CipherOutputStream(baos, cipher);
                    try {
                        byte[] buffer = new byte[1024];
                        while (true) {
                            int r = inputStream.read(buffer);
                            if (r == -1) {
                                break;
                            }
                            cipherOutputStream2.write(buffer, 0, r);
                        }
                        closeStream(cipherOutputStream2);
                        cipherOutputStream = null;
                        InputStream ret2 = new ByteArrayInputStream(baos.toByteArray());
                        closeStream(null);
                        closeStream(inputStream);
                        ret = ret2;
                    } catch (IOException e) {
                        cipherOutputStream = cipherOutputStream2;
                        try {
                            AwareLog.e(TAG, "decryptFile IOException!");
                            closeStream(cipherOutputStream);
                            closeStream(inputStream);
                            AwareLog.d(TAG, "decryptInputStream decrypt spend " + (System.currentTimeMillis() - start) + "ms!");
                            return ret;
                        } catch (Throwable th2) {
                            th = th2;
                            closeStream(cipherOutputStream);
                            closeStream(inputStream);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        Object cipherOutputStream3 = cipherOutputStream2;
                        closeStream(cipherOutputStream);
                        closeStream(inputStream);
                        throw th;
                    }
                    AwareLog.d(TAG, "decryptInputStream decrypt spend " + (System.currentTimeMillis() - start) + "ms!");
                    return ret;
                }
            }
            closeStream(null);
            closeStream(inputStream);
            return null;
        } catch (IOException e2) {
        }
    }

    private static ByteArrayInputStream getByteArrayInputStream(InputStream inputStream) {
        ByteArrayInputStream byteArrayInputStream;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            try {
                int r = inputStream.read(buffer);
                if (r == -1) {
                    break;
                }
                bos.write(buffer, 0, r);
            } catch (IOException e) {
                byteArrayInputStream = TAG;
                AwareLog.e(byteArrayInputStream, "getByteArrayInputStream IOException!");
                return null;
            } finally {
                closeStream(inputStream);
            }
        }
        byteArrayInputStream = new ByteArrayInputStream(bos.toByteArray());
        return byteArrayInputStream;
    }

    private static boolean isNormalXml(InputStream inputStream) {
        byte[] head = new byte[5];
        try {
            int r = inputStream.read(head);
            inputStream.reset();
            return r == 5 ? XML_HEAD.equals(new String(head, CharacterSets.DEFAULT_CHARSET_NAME)) : false;
        } catch (IOException e) {
            AwareLog.w(TAG, "isNormalXml IOException!");
            return true;
        }
    }

    private static Cipher initAESCipher(byte[] codeFormate, byte[] iv) {
        if (codeFormate == null || iv == null) {
            return null;
        }
        try {
            SecretKeySpec key = new SecretKeySpec(codeFormate, ENCYPTION_SCHEME);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, key, new IvParameterSpec(iv));
            return cipher;
        } catch (NoSuchAlgorithmException e) {
            AwareLog.e(TAG, "initAESCipher NoSuchAlgorithmException!");
            return null;
        } catch (NoSuchPaddingException e2) {
            AwareLog.e(TAG, "initAESCipher NoSuchPaddingException!");
            return null;
        } catch (InvalidKeyException e3) {
            AwareLog.e(TAG, "initAESCipher InvalidKeyException!");
            return null;
        } catch (InvalidAlgorithmParameterException e4) {
            AwareLog.e(TAG, "initAESCipher InvalidAlgorithmParameterException!");
            return null;
        } catch (IllegalArgumentException e5) {
            AwareLog.e(TAG, "initAESCipher IllegalArgumentException!");
            return null;
        }
    }

    private static Cipher getCipher(Context context, InputStream inputStream) throws IOException {
        return initAESCipher(parseCodeFormate(context), parseComponent(inputStream, 0, 16));
    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                AwareLog.w(TAG, "close inputStream error!");
            }
        }
    }

    private static byte[] parseCodeFormate(Context context) {
        Cipher cipher = initAESCipher(getRootComponent(context), COMPONENT3);
        if (cipher == null) {
            return new byte[0];
        }
        try {
            return cipher.doFinal(COMPONENT2);
        } catch (IllegalBlockSizeException e) {
            AwareLog.w(TAG, "parseCodeFormate IllegalBlockSizeException!");
        } catch (BadPaddingException e2) {
            AwareLog.w(TAG, "parseCodeFormate BadPaddingException!");
        }
        return new byte[0];
    }

    private static byte[] getRootComponent(Context context) {
        Context otherContext = getContext(context);
        if (otherContext == null) {
            return new byte[0];
        }
        return generateKey(getComponentFromAssets(otherContext, COMPONENT_NAME_1), getComponentFromAssets(otherContext, COMPONENT_NAME_2));
    }

    private static boolean isStreamAvailable(InputStream inputStream) throws IOException {
        return inputStream.available() > 16;
    }

    private static byte[] parseComponent(InputStream inputStream, int start, int len) throws IOException {
        byte[] zeroByte = new byte[0];
        inputStream.reset();
        byte[] buffer = new byte[len];
        if (inputStream.skip((long) start) != ((long) start)) {
            inputStream.reset();
            return buffer;
        }
        if (inputStream.read(buffer, 0, len) != len) {
            buffer = zeroByte;
        }
        inputStream.reset();
        return buffer;
    }

    private static byte[] generateKey(byte[] c1, byte[] c2) {
        return cutByteArray(hashCompoent(XORBytes(gression(XORBytes(cutByteArray(hashCompoent(gression(COMPONENT, 16, true, 4))), c1, 16), 16, false, 1), c2, 16)));
    }

    private static byte[] gression(byte[] component, int len, boolean leftShift, int bit) {
        if (component == null || component.length != len) {
            return new byte[0];
        }
        byte[] ret = new byte[len];
        for (int i = 0; i < len; i++) {
            if (leftShift) {
                ret[i] = (byte) (component[i] << bit);
            } else {
                ret[i] = (byte) (component[i] >> bit);
            }
        }
        return ret;
    }

    /* JADX WARNING: Missing block: B:4:0x0008, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static byte[] XORBytes(byte[] c1, byte[] c2, int len) {
        byte[] zeroByte = new byte[0];
        if (c1 == null || c1.length != len || c2 == null || c2.length != len) {
            return zeroByte;
        }
        byte[] ret = new byte[len];
        for (int i = 0; i < len; i++) {
            ret[i] = (byte) (c1[i] ^ c2[i]);
        }
        return ret;
    }

    private static byte[] hashCompoent(byte[] component) {
        byte[] zeroByte = new byte[0];
        if (component == null || component.length <= 0) {
            return zeroByte;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(component);
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            AwareLog.e(TAG, "No SHA-256 algorithm found!");
            return zeroByte;
        }
    }

    private static byte[] cutByteArray(byte[] res) {
        byte[] zeroByte = new byte[0];
        if (res == null || res.length < 16) {
            return zeroByte;
        }
        byte[] dst = new byte[16];
        System.arraycopy(res, 0, dst, 0, 16);
        if (dst.length != 16) {
            dst = null;
        }
        return dst;
    }

    private static byte[] getComponentFromAssets(Context context, String name) {
        byte[] zeroByte = new byte[0];
        Closeable closeable = null;
        try {
            closeable = context.getAssets().open(name);
            if (closeable == null) {
                return zeroByte;
            }
            byte[] buffer = new byte[16];
            if (closeable.read(buffer) == 16) {
                closeStream(closeable);
                return buffer;
            }
            closeStream(closeable);
            return zeroByte;
        } catch (IOException e) {
            AwareLog.e(TAG, "Assets Exception!");
        } finally {
            closeStream(closeable);
        }
    }

    private static Context getContext(Context context) {
        if (PKG_NAME.equals(context.getPackageName())) {
            return context;
        }
        try {
            return context.createPackageContext(PKG_NAME, 4);
        } catch (NameNotFoundException e) {
            AwareLog.e(TAG, "getContex NameNotFoundException!");
            return null;
        }
    }
}
