package tmsdkobf;

import tmsdk.common.utils.l;

/* compiled from: Unknown */
public class rm {
    private static int oY;
    public String mFileName;
    public String oZ;
    public String pa;
    public String pb;
    public String pc;
    public String pd;
    public String pe;
    public String pf;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.rm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.rm.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.rm.<clinit>():void");
    }

    public rm() {
        StringBuilder append = new StringBuilder().append("");
        int i = oY + 1;
        oY = i;
        this.oZ = append.append(i).toString();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('0');
        stringBuilder.append(this.oZ);
        if (l.dl(this.pa)) {
            stringBuilder.append(':');
            stringBuilder.append('1');
            stringBuilder.append(this.pa);
        }
        if (l.dl(this.mFileName)) {
            stringBuilder.append(':');
            stringBuilder.append('2');
            stringBuilder.append(this.mFileName);
        }
        if (l.dl(this.pb)) {
            stringBuilder.append(':');
            stringBuilder.append('3');
            stringBuilder.append(this.pb);
        }
        if (l.dl(this.pc)) {
            stringBuilder.append(':');
            stringBuilder.append('4');
            stringBuilder.append(this.pc);
        }
        if (l.dl(this.pd)) {
            stringBuilder.append(':');
            stringBuilder.append('5');
            stringBuilder.append(this.pd);
        }
        if (l.dl(this.pe)) {
            stringBuilder.append(':');
            stringBuilder.append('6');
            stringBuilder.append(this.pe);
        }
        stringBuilder.append(':');
        stringBuilder.append('8');
        stringBuilder.append(this.pf);
        return stringBuilder.toString();
    }
}
