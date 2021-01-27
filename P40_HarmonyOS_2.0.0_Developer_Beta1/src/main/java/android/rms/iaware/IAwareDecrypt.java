package android.rms.iaware;

import android.content.Context;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
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
    private static final byte[] COMPONENT = {-71, -108, 58, -21, 20, -105, 106, 57, -20, -95, -94, -56, 29, -72, -50, -118, 54, 48, 24, -9, -63, -60, 33, -45, 57, 91, 90, 39, -36, 35, -34, 11};
    private static final byte[] COMPONENT2 = {-91, 55, 87, -46, 64, -10, 24, 58, 50, Byte.MIN_VALUE, -42, -77, -62, 118, 112, 29, -60, 16, -94, -35, 17, 46, 68, -80, 40, -58, -25, -90, -5, 36, -84, 27};
    private static final byte[] COMPONENT3 = {2, 13, 17, 7, -66, -97, 55, -95, 85, -25, 74, 56, 96, 112, -122, 66};
    private static final String ENCYPTION_SCHEME = "AES";
    private static final int STREAM_READ_SIZE = 1024;
    private static final String TAG = "IAwareDecrypt";
    private static final byte[] UTF8_BOM_HEAD = {-17, -69, -65};
    private static final int UTF8_BOM_HEAD_LEN = 3;
    private static final String XML_HEAD = "<?xml";
    private static final int XML_HEAD_LEN = 5;

    public static InputStream decryptInputStream(Context context, InputStream ins) {
        if (ins == null || context == null) {
            return ins;
        }
        long start = System.currentTimeMillis();
        InputStream ret = doDecryptInputStream(context, ins);
        AwareLog.d(TAG, "decryptInputStream decrypt spend " + (System.currentTimeMillis() - start) + "ms!");
        return ret;
    }

    private static InputStream doDecryptInputStream(Context context, InputStream ins) {
        InputStream inputStream;
        InputStream ret;
        CipherOutputStream cipherOutputStream;
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
        ret = null;
        cipherOutputStream = null;
        try {
        } catch (IOException e) {
            AwareLog.e(TAG, "decryptFile IOException!");
        } catch (Throwable th) {
            closeStream(null);
            closeStream(inputStream);
            throw th;
        }
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
            byte[] buffer = new byte[STREAM_READ_SIZE];
            while (true) {
                int readNumber = inputStream.read(buffer);
                if (readNumber == -1) {
                    closeStream(cipherOutputStream2);
                    cipherOutputStream = null;
                    ret = new ByteArrayInputStream(baos.toByteArray());
                    break;
                } else if (readNumber <= 0 || readNumber > STREAM_READ_SIZE) {
                    break;
                } else {
                    cipherOutputStream2.write(buffer, 0, readNumber);
                }
            }
            closeStream(cipherOutputStream2);
            closeStream(inputStream);
            return null;
        }
        closeStream(cipherOutputStream);
        closeStream(inputStream);
        return ret;
    }

    private static ByteArrayInputStream getByteArrayInputStream(InputStream inputStream) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[STREAM_READ_SIZE];
        while (true) {
            try {
                int readNumber = inputStream.read(buffer);
                if (readNumber == -1) {
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bos.toByteArray());
                    closeStream(inputStream);
                    return byteArrayInputStream;
                } else if (readNumber <= 0) {
                    break;
                } else if (readNumber > STREAM_READ_SIZE) {
                    break;
                } else {
                    bos.write(buffer, 0, readNumber);
                }
            } catch (IOException e) {
                AwareLog.e(TAG, "getByteArrayInputStream IOException!");
                return null;
            } finally {
                closeStream(inputStream);
            }
        }
        return null;
    }

    private static boolean isNormalXml(InputStream inputStream) {
        byte[] head = new byte[5];
        try {
            byte[] bomHead = new byte[3];
            if (inputStream.read(bomHead) != 3 || !Arrays.equals(bomHead, UTF8_BOM_HEAD)) {
                inputStream.reset();
            }
            int readNumber = inputStream.read(head);
            inputStream.reset();
            if (readNumber != 5 || !XML_HEAD.equals(new String(head, "utf-8"))) {
                return false;
            }
            return true;
        } catch (IOException e) {
            AwareLog.w(TAG, "isNormalXml IOException!");
            return true;
        }
    }

    private static Cipher initAesCipher(byte[] codeFormate, byte[] iv) {
        if (codeFormate == null || iv == null) {
            return null;
        }
        try {
            SecretKeySpec key = new SecretKeySpec(codeFormate, ENCYPTION_SCHEME);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, key, new IvParameterSpec(iv));
            return cipher;
        } catch (NoSuchAlgorithmException e) {
            AwareLog.e(TAG, "initAesCipher NoSuchAlgorithmException!");
            return null;
        } catch (NoSuchPaddingException e2) {
            AwareLog.e(TAG, "initAesCipher NoSuchPaddingException!");
            return null;
        } catch (InvalidKeyException e3) {
            AwareLog.e(TAG, "initAesCipher InvalidKeyException!");
            return null;
        } catch (InvalidAlgorithmParameterException e4) {
            AwareLog.e(TAG, "initAesCipher InvalidAlgorithmParameterException!");
            return null;
        } catch (IllegalArgumentException e5) {
            AwareLog.e(TAG, "initAesCipher IllegalArgumentException!");
            return null;
        }
    }

    private static Cipher getCipher(Context context, InputStream inputStream) throws IOException {
        return initAesCipher(parseCodeFormate(context), parseComponent(inputStream, 0, 16));
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
        Cipher cipher = initAesCipher(IAwareDecryptNative.getNativeComponent(COMPONENT3, COMPONENT), COMPONENT3);
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

    private static boolean isStreamAvailable(InputStream inputStream) throws IOException {
        return inputStream.available() > 16;
    }

    private static byte[] parseComponent(InputStream inputStream, int start, int len) throws IOException {
        inputStream.reset();
        byte[] buffer = new byte[len];
        if (inputStream.skip((long) start) != ((long) start)) {
            inputStream.reset();
            return buffer;
        }
        if (inputStream.read(buffer, 0, len) != len) {
            buffer = new byte[0];
        }
        inputStream.reset();
        return buffer;
    }
}
