package android.text;

import java.util.Arrays;

public class Emoji {
    public static int COMBINING_ENCLOSING_KEYCAP;
    private static int[] EMOJI_LIST;
    private static int[] EMOJI_MODIFIER_BASE;
    public static int VARIATION_SELECTOR_16;
    public static int ZERO_WIDTH_JOINER;
    private static int[] ZWJ_EMOJI;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.Emoji.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.Emoji.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.text.Emoji.<clinit>():void");
    }

    public static boolean isRegionalIndicatorSymbol(int codepoint) {
        return 127462 <= codepoint && codepoint <= 127487;
    }

    public static boolean isEmojiModifier(int codepoint) {
        return 127995 <= codepoint && codepoint <= 127999;
    }

    public static boolean isEmojiModifierBase(int codePoint) {
        return Arrays.binarySearch(EMOJI_MODIFIER_BASE, codePoint) >= 0;
    }

    public static boolean isEmoji(int codePoint) {
        return Arrays.binarySearch(EMOJI_LIST, codePoint) >= 0;
    }

    public static boolean isKeycapBase(int codePoint) {
        return (48 <= codePoint && codePoint <= 57) || codePoint == 35 || codePoint == 42;
    }
}
