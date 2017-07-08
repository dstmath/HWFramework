package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import java.io.UnsupportedEncodingException;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.b;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ls {
    private static String TAG;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ls.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ls.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ls.<clinit>():void");
    }

    public static String b(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        try {
            byte[] encrypt = TccCryptor.encrypt(context, str.getBytes("gbk"), null);
            if (encrypt != null) {
                return b.encodeToString(encrypt, 0);
            }
        } catch (UnsupportedEncodingException e) {
            d.c(TAG, "getEncodeString, UnsupportedEncodingException: " + e);
        } catch (Exception e2) {
            d.c(TAG, "getEncodeString, Exception: " + e2);
        }
        return null;
    }

    public static String c(Context context, String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        try {
            byte[] decrypt = TccCryptor.decrypt(context, b.decode(str, 0), null);
            if (decrypt != null) {
                return new String(decrypt, "gbk");
            }
        } catch (UnsupportedEncodingException e) {
            d.c(TAG, "getDecodeString, UnsupportedEncodingException: " + e);
        } catch (Exception e2) {
            d.c(TAG, "getDecodeString, Exception: " + e2);
        }
        return null;
    }
}
