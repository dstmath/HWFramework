package tmsdkobf;

import android.text.TextUtils;

/* compiled from: Unknown */
public class gm {
    private static int oY;
    public String mDescription;
    public String mFileName;
    public String oZ;
    public String pa;
    public String pb;
    public String pc;
    public String pd;
    public String pe;
    public String pf;
    public String pg;
    public boolean ph;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.gm.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.gm.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.gm.<clinit>():void");
    }

    public gm() {
        StringBuilder append = new StringBuilder().append("");
        int i = oY + 1;
        oY = i;
        this.oZ = append.append(i).toString();
    }

    public static void a(StringBuilder stringBuilder, gm gmVar) {
        stringBuilder.append('0');
        stringBuilder.append(gmVar.oZ);
        if (!TextUtils.isEmpty(gmVar.pa)) {
            stringBuilder.append(':');
            stringBuilder.append('1');
            stringBuilder.append(gmVar.pa);
        }
        if (!TextUtils.isEmpty(gmVar.mFileName)) {
            stringBuilder.append(':');
            stringBuilder.append('2');
            stringBuilder.append(gmVar.mFileName);
        }
        if (!TextUtils.isEmpty(gmVar.pb)) {
            stringBuilder.append(':');
            stringBuilder.append('3');
            stringBuilder.append(gmVar.pb);
        }
        if (!TextUtils.isEmpty(gmVar.pc)) {
            stringBuilder.append(':');
            stringBuilder.append('4');
            stringBuilder.append(gmVar.pc);
        }
        if (!TextUtils.isEmpty(gmVar.pd)) {
            stringBuilder.append(':');
            stringBuilder.append('5');
            stringBuilder.append(gmVar.pd);
        }
        if (!TextUtils.isEmpty(gmVar.pe)) {
            stringBuilder.append(':');
            stringBuilder.append('6');
            stringBuilder.append(gmVar.pe);
        }
        stringBuilder.append(':');
        stringBuilder.append('8');
        stringBuilder.append(gmVar.pf);
    }
}
