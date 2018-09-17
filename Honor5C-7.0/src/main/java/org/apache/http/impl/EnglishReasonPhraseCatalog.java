package org.apache.http.impl;

import java.util.Locale;
import org.apache.http.ReasonPhraseCatalog;

@Deprecated
public class EnglishReasonPhraseCatalog implements ReasonPhraseCatalog {
    public static final EnglishReasonPhraseCatalog INSTANCE = null;
    private static final String[][] REASON_PHRASES = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.http.impl.EnglishReasonPhraseCatalog.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.http.impl.EnglishReasonPhraseCatalog.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: org.apache.http.impl.EnglishReasonPhraseCatalog.<clinit>():void");
    }

    protected EnglishReasonPhraseCatalog() {
    }

    public String getReason(int status, Locale loc) {
        if (status < 100 || status >= 600) {
            throw new IllegalArgumentException("Unknown category for status code " + status + ".");
        }
        int category = status / 100;
        int subcode = status - (category * 100);
        if (REASON_PHRASES[category].length > subcode) {
            return REASON_PHRASES[category][subcode];
        }
        return null;
    }

    private static void setReason(int status, String reason) {
        int category = status / 100;
        REASON_PHRASES[category][status - (category * 100)] = reason;
    }
}
