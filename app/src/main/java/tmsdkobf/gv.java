package tmsdkobf;

import android.text.TextUtils;
import java.util.List;

/* compiled from: Unknown */
public class gv {
    private static int pv;
    public String mDescription;
    public String mFileName;
    public int mID;
    public String om;
    public List<String> op;
    public String pb;
    public String pc;
    public String pd;
    public String pe;
    public int pw;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.gv.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.gv.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.gv.<clinit>():void");
    }

    public gv() {
        int i = pv + 1;
        pv = i;
        this.mID = i;
    }

    public static void a(StringBuilder stringBuilder, gv gvVar, boolean z, boolean z2) {
        for (String str : gvVar.op) {
            stringBuilder.append('0');
            stringBuilder.append(gvVar.mID);
            stringBuilder.append(':');
            stringBuilder.append('7');
            stringBuilder.append(!z2 ? '0' : '1');
            if (!TextUtils.isEmpty(str)) {
                stringBuilder.append(':');
                stringBuilder.append('1');
                stringBuilder.append(str);
            }
            if (!TextUtils.isEmpty(gvVar.mFileName)) {
                stringBuilder.append(':');
                stringBuilder.append('2');
                stringBuilder.append(gvVar.mFileName);
            }
            if (!TextUtils.isEmpty(gvVar.pb)) {
                stringBuilder.append(':');
                stringBuilder.append('3');
                stringBuilder.append(gvVar.pb);
            }
            if (!TextUtils.isEmpty(gvVar.pc)) {
                stringBuilder.append(':');
                stringBuilder.append('4');
                stringBuilder.append(gvVar.pc);
            }
            if (!TextUtils.isEmpty(gvVar.pd)) {
                stringBuilder.append(':');
                stringBuilder.append('5');
                stringBuilder.append(gvVar.pd);
            }
            if (!TextUtils.isEmpty(gvVar.pe)) {
                stringBuilder.append(':');
                stringBuilder.append('6');
                stringBuilder.append(gvVar.pe);
            }
            stringBuilder.append(';');
        }
    }

    public static void bb() {
        pv = 0;
    }
}
