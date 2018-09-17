package com.huawei.zxing.client.result;

import java.util.regex.Pattern;

public final class URIParsedResult extends ParsedResult {
    private static final Pattern USER_IN_HOST = null;
    private final String title;
    private final String uri;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.zxing.client.result.URIParsedResult.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.zxing.client.result.URIParsedResult.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.zxing.client.result.URIParsedResult.<clinit>():void");
    }

    public URIParsedResult(String uri, String title) {
        super(ParsedResultType.URI);
        this.uri = massageURI(uri);
        this.title = title;
    }

    public String getURI() {
        return this.uri;
    }

    public String getTitle() {
        return this.title;
    }

    public boolean isPossiblyMaliciousURI() {
        return USER_IN_HOST.matcher(this.uri).find();
    }

    public String getDisplayResult() {
        StringBuilder result = new StringBuilder(30);
        ParsedResult.maybeAppend(this.title, result);
        ParsedResult.maybeAppend(this.uri, result);
        return result.toString();
    }

    private static String massageURI(String uri) {
        uri = uri.trim();
        int protocolEnd = uri.indexOf(58);
        if (protocolEnd < 0) {
            return "http://" + uri;
        }
        if (isColonFollowedByPortNumber(uri, protocolEnd)) {
            return "http://" + uri;
        }
        return uri;
    }

    private static boolean isColonFollowedByPortNumber(String uri, int protocolEnd) {
        int nextSlash = uri.indexOf(47, protocolEnd + 1);
        if (nextSlash < 0) {
            nextSlash = uri.length();
        }
        if (nextSlash <= protocolEnd + 1) {
            return false;
        }
        int x = protocolEnd + 1;
        while (x < nextSlash) {
            if (uri.charAt(x) < '0' || uri.charAt(x) > '9') {
                return false;
            }
            x++;
        }
        return true;
    }
}
