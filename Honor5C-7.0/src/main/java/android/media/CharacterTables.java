package android.media;

class CharacterTables {
    private static final char[] frequent_cn = null;
    private static final char[] frequent_ja = null;
    private static final char[] frequent_tw = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.media.CharacterTables.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.media.CharacterTables.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.media.CharacterTables.<clinit>():void");
    }

    CharacterTables() {
    }

    private static boolean isFrequent(char[] array, char c) {
        int start = 0;
        int end = array.length - 1;
        int mid = (end + 0) / 2;
        while (start <= end) {
            if (c == array[mid]) {
                return true;
            }
            if (c > array[mid]) {
                start = mid + 1;
            } else {
                end = mid - 1;
            }
            mid = (start + end) / 2;
        }
        return false;
    }

    static boolean isFrequentCn(char c) {
        return isFrequent(frequent_cn, c);
    }

    static boolean isFrequentJa(char c) {
        return isFrequent(frequent_ja, c);
    }

    static boolean isFrequentTw(char c) {
        return isFrequent(frequent_tw, c);
    }

    static boolean isFrequentHan(char c) {
        if (isFrequentCn(c) || isFrequentTw(c)) {
            return true;
        }
        return isFrequentJa(c);
    }
}
