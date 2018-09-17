package android.encrypt;

import android.util.Log;
import java.nio.charset.Charset;
import java.util.Locale;

public class HwPasswordUtil implements PasswordUtil {
    private static final int CHAR_OFFSET = 10;
    private static final int LENGTH = 4;
    private static final String SKEY = "skey";
    private static final String TAG = "PasswordUtil";
    private static final char[] mChars = mHexStr.toCharArray();
    private static final String mHexStr = "0123456789ABCDEF";
    private static PasswordUtil mPasswordUtil;

    public static synchronized PasswordUtil getInstance() {
        PasswordUtil passwordUtil;
        synchronized (HwPasswordUtil.class) {
            if (mPasswordUtil == null) {
                mPasswordUtil = new HwPasswordUtil();
            }
            passwordUtil = mPasswordUtil;
        }
        return passwordUtil;
    }

    public static String flagPswd2PlainText(String flagPswd) {
        String plainText = "";
        if (isFlagPswd(flagPswd)) {
            return PasswordEncrypter.decrypter(flagPswd.substring(4));
        }
        Log.e(TAG, "flagPswd2PlainText: flagPswd is not a flag password!");
        return plainText;
    }

    public String pswd2PlainText(String dbPswd) {
        if (dbPswd == null) {
            return null;
        }
        String plainText = "";
        if (isFlagPswd(dbPswd)) {
            plainText = flagPswd2PlainText(dbPswd);
        } else {
            plainText = dbPswd;
        }
        return plainText;
    }

    public static boolean isFlagPswd(String pswd) {
        boolean rtnFlag = false;
        if (pswd == null || pswd.length() < 8) {
            return false;
        }
        try {
            String validationCode = pswd.substring(0, 4);
            if (validationCode2Str(validationCode).equals(pswd.substring(4, 8))) {
                rtnFlag = true;
            }
        } catch (Exception ex) {
            Log.e(TAG, "FlagPswd:ex:" + ex.getMessage());
        }
        return rtnFlag;
    }

    private static String validationCode2Str(String code) {
        return codeOffset(code, -10);
    }

    private static String codeOffset(String code, int offset) {
        String result = "";
        StringBuffer buf = new StringBuffer();
        if (code == null || code.length() == 0) {
            return result;
        }
        try {
            int len = code.length();
            for (int i = 0; i < len; i++) {
                buf.append(String.valueOf((char) (code.charAt(i) + offset)));
            }
        } catch (Exception ex) {
            Log.e(TAG, "codeOffset:ex:" + ex.getMessage());
        }
        return buf.toString();
    }

    public static String getSecretKey(String originKey) {
        return skey(hexStr2Str(originKey));
    }

    private static String skey(String strOld) {
        byte[] data = strOld.getBytes(Charset.forName("UTF-8"));
        byte[] keyData = SKEY.getBytes(Charset.forName("UTF-8"));
        int keyIndex = 0;
        int len = strOld.length();
        for (int x = 0; x < len; x++) {
            data[x] = (byte) (data[x] ^ keyData[keyIndex]);
            keyIndex++;
            if (keyIndex == keyData.length) {
                keyIndex = 0;
            }
        }
        return new String(data, Charset.forName("UTF-8"));
    }

    private static String hexStr2Str(String hexStr) {
        hexStr = hexStr.trim().replace(" ", "").toUpperCase(Locale.US);
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[(hexStr.length() / 2)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (((mHexStr.indexOf(hexs[i * 2]) << 4) | mHexStr.indexOf(hexs[(i * 2) + 1])) & 255);
        }
        return new String(bytes, Charset.forName("UTF-8"));
    }
}
