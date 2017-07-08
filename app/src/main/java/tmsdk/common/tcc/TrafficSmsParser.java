package tmsdk.common.tcc;

import java.util.concurrent.atomic.AtomicInteger;

/* compiled from: Unknown */
public class TrafficSmsParser {

    /* compiled from: Unknown */
    public static class MatchRule {
        public String postfix;
        public String prefix;
        public int type;
        public int unit;

        public MatchRule(int i, int i2, String str, String str2) {
            this.unit = i;
            this.type = i2;
            this.prefix = str;
            this.postfix = str2;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdk.common.tcc.TrafficSmsParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdk.common.tcc.TrafficSmsParser.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.tcc.TrafficSmsParser.<clinit>():void");
    }

    public static int getNumberEntrance(String str, String str2, MatchRule matchRule, AtomicInteger atomicInteger) {
        return nativeGetNumberEntrance(str, str2, matchRule, atomicInteger);
    }

    public static int getWrongSmsType(String str, String str2) {
        return nativeGetWrongSmsType(str, str2);
    }

    private static native int nativeGetNumberEntrance(String str, String str2, MatchRule matchRule, AtomicInteger atomicInteger);

    private static native int nativeGetWrongSmsType(String str, String str2);
}
