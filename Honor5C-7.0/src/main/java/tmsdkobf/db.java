package tmsdkobf;

import java.io.Serializable;

/* compiled from: Unknown */
public final class db implements Serializable {
    static final /* synthetic */ boolean fJ = false;
    private static db[] gM;
    public static final db gN = null;
    public static final db gO = null;
    public static final db gP = null;
    public static final db gQ = null;
    public static final db gR = null;
    public static final db gS = null;
    public static final db gT = null;
    public static final db gU = null;
    public static final db gV = null;
    public static final db gW = null;
    public static final db gX = null;
    public static final db gY = null;
    private String gA;
    private int gz;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.db.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.db.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.db.<clinit>():void");
    }

    private db(int i, int i2, String str) {
        this.gA = new String();
        this.gA = str;
        this.gz = i2;
        gM[i] = this;
    }

    public String toString() {
        return this.gA;
    }

    public int value() {
        return this.gz;
    }
}
