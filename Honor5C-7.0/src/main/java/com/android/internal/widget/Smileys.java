package com.android.internal.widget;

public class Smileys {
    public static int ANGEL;
    public static int COOL;
    public static int CRYING;
    public static int EMBARRASSED;
    public static int FOOT_IN_MOUTH;
    public static int HAPPY;
    public static int KISSING;
    public static int LAUGHING;
    public static int LIPS_ARE_SEALED;
    public static int MONEY_MOUTH;
    public static int SAD;
    public static int SURPRISED;
    public static int TONGUE_STICKING_OUT;
    public static int UNDECIDED;
    public static int WINKING;
    public static int WTF;
    public static int YELLING;
    private static final int[] sIconIds = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.widget.Smileys.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.widget.Smileys.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.Smileys.<clinit>():void");
    }

    public static int getSmileyResource(int which) {
        return sIconIds[which];
    }
}
