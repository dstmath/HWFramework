package android.encrypt;

import android.util.Log;
import huawei.android.provider.HanziToPinyin.Token;
import java.nio.charset.Charset;
import java.util.Locale;

public class HwPasswordUtil implements PasswordUtil {
    private static final int CHAR_OFFSET = 10;
    private static final int LENGTH = 4;
    private static final String SKEY = "skey";
    private static final String TAG = "PasswordUtil";
    private static final char[] mChars = null;
    private static final String mHexStr = "0123456789ABCDEF";
    private static PasswordUtil mPasswordUtil;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.encrypt.HwPasswordUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.encrypt.HwPasswordUtil.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.encrypt.HwPasswordUtil.<clinit>():void");
    }

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
            return PasswordEncrypter.decrypter(flagPswd.substring(LENGTH));
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
            String validationCode = pswd.substring(0, LENGTH);
            if (validationCode2Str(validationCode).equals(pswd.substring(LENGTH, 8))) {
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
        int i = 0;
        while (i < code.length()) {
            try {
                buf.append(String.valueOf((char) (code.charAt(i) + offset)));
                i++;
            } catch (Exception ex) {
                Log.e(TAG, "codeOffset:ex:" + ex.getMessage());
            }
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
        for (int x = 0; x < strOld.length(); x++) {
            data[x] = (byte) (data[x] ^ keyData[keyIndex]);
            keyIndex++;
            if (keyIndex == keyData.length) {
                keyIndex = 0;
            }
        }
        return new String(data, Charset.forName("UTF-8"));
    }

    private static String hexStr2Str(String hexStr) {
        hexStr = hexStr.trim().replace(Token.SEPARATOR, "").toUpperCase(Locale.US);
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[(hexStr.length() / 2)];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (((mHexStr.indexOf(hexs[i * 2]) << LENGTH) | mHexStr.indexOf(hexs[(i * 2) + 1])) & PduHeaders.STORE_STATUS_ERROR_END);
        }
        return new String(bytes, Charset.forName("UTF-8"));
    }
}
