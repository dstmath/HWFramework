package android.encrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public final class AES128_ECB {
    private static final int AES_128_KEY_LEN = 16;
    static final byte[] C1 = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.encrypt.AES128_ECB.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.encrypt.AES128_ECB.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.encrypt.AES128_ECB.<clinit>():void");
    }

    public static byte[] decode(byte[] btCipher, int iLen, byte[] btKey, int iKeyLen) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        return encode_decode(btCipher, iLen, btKey, iKeyLen, 1);
    }

    private static byte[] encode_decode(byte[] btData, int iLen, byte[] btKey, int iKeyLen, int iFlag) throws BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        if (btData == null || btKey == null) {
            return null;
        }
        int ii;
        int l_iMode;
        if (iLen <= 0 || iLen > btData.length) {
            iLen = btData.length;
        }
        if (iKeyLen <= 0 || iKeyLen > btKey.length) {
            iKeyLen = btKey.length;
        }
        if (iKeyLen > AES_128_KEY_LEN) {
            iKeyLen = AES_128_KEY_LEN;
        }
        byte[] l_btKey = new byte[AES_128_KEY_LEN];
        for (ii = 0; ii < AES_128_KEY_LEN; ii++) {
            l_btKey[ii] = (byte) 0;
        }
        for (ii = 0; ii < iKeyLen; ii++) {
            l_btKey[ii] = btKey[ii];
        }
        Cipher l_oCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        if (iFlag == 0) {
            l_iMode = 1;
        } else {
            l_iMode = 2;
        }
        l_oCipher.init(l_iMode, new SecretKeySpec(l_btKey, 0, AES_128_KEY_LEN, "AES"));
        return l_oCipher.doFinal(btData, 0, iLen);
    }

    private AES128_ECB() {
    }
}
