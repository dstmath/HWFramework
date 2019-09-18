package android.rms.iaware;

import android.content.Context;
import android.content.pm.PackageManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class IAwareDecrypt {
    private static final int AES128_KEY_LEN = 16;
    private static final byte[] COMPONENT = {-118, 80, -33, -103, 100, 101, 125, -35, -28, -46, -66, -15, -36, 5, -79, 115};
    private static final byte[] COMPONENT2 = {-91, 55, 87, -46, 64, -10, 24, 58, 50, Byte.MIN_VALUE, -42, -77, -62, 118, 112, 29, -60, 16, -94, -35, 17, 46, 68, -80, 40, -58, -25, -90, -5, 36, -84, 27};
    private static final byte[] COMPONENT3 = {2, 13, 17, 7, -66, -97, 55, -95, 85, -25, 74, 56, 96, 112, -122, 66};
    private static final String COMPONENT_NAME_1 = "iaware_c.dat";
    private static final String COMPONENT_NAME_2 = "iaware_cm.dat";
    private static final String ENCYPTION_SCHEME = "AES";
    private static final String PKG_NAME = "com.huawei.iaware";
    private static final int STREAM_READ_SIZE = 1024;
    private static final String TAG = "IAwareDecrypt";
    private static final byte[] UTF8_BOM_HEAD = {-17, -69, -65};
    private static final int UTF8_BOM_HEAD_LEN = 3;
    private static final String XML_HEAD = "<?xml";
    private static final int XML_HEAD_LEN = 5;

    public static InputStream decryptInputStream(Context context, InputStream ins) {
        InputStream inputStream;
        if (ins == null || context == null) {
            return ins;
        }
        long start = System.currentTimeMillis();
        if (!ins.markSupported()) {
            inputStream = getByteArrayInputStream(ins);
        } else {
            inputStream = ins;
        }
        if (inputStream == null) {
            return null;
        }
        if (isNormalXml(inputStream)) {
            return inputStream;
        }
        InputStream ret = null;
        CipherOutputStream cipherOutputStream = null;
        try {
            if (!isStreamAvailable(inputStream)) {
                closeStream(null);
                closeStream(inputStream);
                return null;
            }
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
                byte[] buffer = new byte[1024];
                while (true) {
                    int read = inputStream.read(buffer);
                    int r = read;
                    if (read == -1) {
                        break;
                    }
                    cipherOutputStream2.write(buffer, 0, r);
                }
                closeStream(cipherOutputStream2);
                cipherOutputStream = null;
                ret = new ByteArrayInputStream(baos.toByteArray());
                closeStream(cipherOutputStream);
                closeStream(inputStream);
                AwareLog.d(TAG, "decryptInputStream decrypt spend " + (System.currentTimeMillis() - start) + "ms!");
                return ret;
            }
        } catch (IOException e) {
            AwareLog.e(TAG, "decryptFile IOException!");
        } catch (Throwable th) {
            closeStream(null);
            closeStream(inputStream);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    private static ByteArrayInputStream getByteArrayInputStream(InputStream inputStream) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while (true) {
            try {
                int read = inputStream.read(buffer);
                int r = read;
                if (read != -1) {
                    bos.write(buffer, 0, r);
                } else {
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bos.toByteArray());
                    closeStream(inputStream);
                    return byteArrayInputStream;
                }
            } catch (IOException e) {
                AwareLog.e(TAG, "getByteArrayInputStream IOException!");
                closeStream(inputStream);
                return null;
            } catch (Throwable th) {
                closeStream(inputStream);
                throw th;
            }
        }
    }

    private static boolean isNormalXml(InputStream inputStream) {
        byte[] head = new byte[5];
        boolean z = true;
        try {
            byte[] bomHead = new byte[3];
            if (inputStream.read(bomHead) != 3 || !Arrays.equals(bomHead, UTF8_BOM_HEAD)) {
                inputStream.reset();
            }
            int r = inputStream.read(head);
            inputStream.reset();
            if (r != 5 || !XML_HEAD.equals(new String(head, CharacterSets.DEFAULT_CHARSET_NAME))) {
                z = false;
            }
            return z;
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
            return new byte[0];
        } catch (BadPaddingException e2) {
            AwareLog.w(TAG, "parseCodeFormate BadPaddingException!");
            return new byte[0];
        }
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
        return dst.length != 16 ? null : dst;
    }

    private static byte[] getComponentFromAssets(Context context, String name) {
        byte[] zeroByte = new byte[0];
        InputStream in = null;
        try {
            in = context.getAssets().open(name);
            if (in == null) {
                closeStream(in);
                return zeroByte;
            }
            byte[] buffer = new byte[16];
            if (in.read(buffer) == 16) {
                closeStream(in);
                return buffer;
            }
            closeStream(in);
            return zeroByte;
        } catch (IOException e) {
            AwareLog.e(TAG, "Assets Exception!");
        } catch (Throwable th) {
            closeStream(in);
            throw th;
        }
    }

    private static Context getContext(Context context) {
        if (PKG_NAME.equals(context.getPackageName())) {
            return context;
        }
        try {
            return context.createPackageContext(PKG_NAME, 4);
        } catch (PackageManager.NameNotFoundException e) {
            AwareLog.e(TAG, "getContex NameNotFoundException!");
            return null;
        }
    }
}
