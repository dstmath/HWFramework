package com.android.server.pm.auth;

import android.content.Context;
import android.content.res.XmlResourceParser;
import com.android.server.pm.auth.util.CryptionUtils;
import com.android.server.pm.auth.util.HwAuthLogger;
import com.android.server.pm.auth.util.Utils;
import java.io.IOException;
import java.security.PublicKey;
import org.xmlpull.v1.XmlPullParserException;

public class DevicePublicKeyLoader {
    private static final int HW_PUB_KEY_RES = 34275328;
    public static final String KEY = "key";
    private static final String PUBLIC_KEY = "30820122300d06092a864886f70d01010105000382010f003082010a0282010100a3d269348ac59923f65e8111c337605e29a1d1bc54fa96c1445050dd14d8d63b10f9f0230bb87ef348183660bedcabfdec045e235ed96935799fcdb4af5c97717ff3b0954eaf1b723225b3a00f81cbd67ce6dc5a4c07f7741ad3bf1913a480c6e267ab1740f409edd2dc33c8b718a8e30e56d9a93f321723c1d0c9ea62115f996812ceef186954595e39a19b74245542c407f7dddb1d12e6eedcfc0bd7cd945ef7255ad0fc9e796258e0fb5e52a23013d15033a32b4071b65f3f924ae5c5761e22327b4d2ae60f4158a5eb15565ba079de29b81540f5fbb3be101a95357f367fc661d797074ff3826950029c52223e4594673a24a334cae62d63b838ba3df9770203010001";
    public static final String TAG = "HwCertificationManager";
    public static final String VALUE = "value";
    private static PublicKey mPublicKey;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.pm.auth.DevicePublicKeyLoader.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.pm.auth.DevicePublicKeyLoader.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.auth.DevicePublicKeyLoader.<clinit>():void");
    }

    public static PublicKey getPublicKey(Context context) {
        if (mPublicKey == null) {
            mPublicKey = loadDevicePublicKey(context);
        }
        return mPublicKey;
    }

    private static PublicKey loadDevicePublicKey(Context context) {
        byte[] bArr = null;
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = context.getResources().getXml(HW_PUB_KEY_RES);
            String hwPubKeyString = null;
            for (int eventType = xmlResourceParser.next(); eventType != 1; eventType = xmlResourceParser.next()) {
                if (eventType == 2 && xmlResourceParser.getName().equals(KEY)) {
                    hwPubKeyString = xmlResourceParser.getAttributeValue(null, VALUE);
                    break;
                }
            }
            if (hwPubKeyString != null) {
                bArr = Utils.stringToBytes(hwPubKeyString);
            }
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (XmlPullParserException e) {
            HwAuthLogger.e(TAG, "XmlPullParserException:" + e, e);
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (IOException ex) {
            HwAuthLogger.e(TAG, "IOException:" + ex, ex);
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
        if (bArr != null) {
            return CryptionUtils.getPublicKey(bArr);
        }
        return CryptionUtils.getPublicKey(PUBLIC_KEY);
    }
}
