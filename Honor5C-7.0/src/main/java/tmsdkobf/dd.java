package tmsdkobf;

import java.io.Serializable;

/* compiled from: Unknown */
public final class dd implements Serializable {
    static final /* synthetic */ boolean fJ = false;
    private static dd[] hK;
    public static final dd hL = null;
    public static final dd hM = null;
    public static final dd hN = null;
    public static final dd hO = null;
    public static final dd hP = null;
    public static final dd hQ = null;
    public static final dd hR = null;
    public static final dd hS = null;
    public static final dd hT = null;
    public static final dd hU = null;
    public static final dd hV = null;
    public static final dd hW = null;
    public static final dd hX = null;
    public static final dd hY = null;
    public static final dd hZ = null;
    public static final dd ia = null;
    public static final dd ib = null;
    public static final dd ic = null;
    public static final dd ie = null;
    public static final dd if = null;
    private String gA;
    private int gz;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.dd.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.dd.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.dd.<clinit>():void");
    }

    private dd(int i, int i2, String str) {
        this.gA = new String();
        this.gA = str;
        this.gz = i2;
        hK[i] = this;
    }

    public String toString() {
        return this.gA;
    }

    public int value() {
        return this.gz;
    }
}
