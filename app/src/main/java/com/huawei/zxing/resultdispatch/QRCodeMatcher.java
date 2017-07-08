package com.huawei.zxing.resultdispatch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QRCodeMatcher {
    private static final Pattern WEB_URL = null;
    private static final Pattern WEB_URL_QQ = null;
    private static final Pattern WEB_URL_WEIBO = null;
    private static final Pattern WECHAT = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.resultdispatch.QRCodeMatcher.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.resultdispatch.QRCodeMatcher.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.resultdispatch.QRCodeMatcher.<clinit>():void");
    }

    public static QRCodeType match(String paramString) {
        String str1 = paramString.trim();
        String str2 = str1.toLowerCase();
        QRCodeType localQRCodeType = QRCodeType.TEXT;
        Matcher localMatcher1 = WECHAT.matcher(str2);
        if (localMatcher1.find() && localMatcher1.start() == 0) {
            return QRCodeType.WECHAT;
        }
        Matcher localMatcher2 = WEB_URL_QQ.matcher(str2);
        if (localMatcher2.find() && localMatcher2.start() == 0) {
            return QRCodeType.WEB_URL_QQ;
        }
        Matcher localMatcher3 = WEB_URL_WEIBO.matcher(str2);
        if (localMatcher3.find() && localMatcher3.start() == 0) {
            return QRCodeType.WEB_URL_WEIBO;
        }
        if (str2.contains("hicloud.com")) {
            return QRCodeType.MARKET;
        }
        if (!WEB_URL.matcher(str2).matches()) {
            return localQRCodeType;
        }
        if (str1.endsWith(".apk")) {
            return QRCodeType.WEB_URL_APK;
        }
        if (str1.startsWith("http://qm.qq.com")) {
            return QRCodeType.WEB_URL_QQ;
        }
        return QRCodeType.WEB_URL;
    }
}
