package tmsdkobf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tmsdkobf.jq.a;

/* compiled from: Unknown */
public final class ni implements a {
    private static volatile ni Ce;
    private static final String[] Cf = null;
    private List<mx> Cg;
    private List Ch;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ni.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ni.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ni.<clinit>():void");
    }

    private ni() {
        this.Cg = new ArrayList();
        this.Ch = new ArrayList(2);
        fh();
    }

    public static synchronized ni fg() {
        ni niVar;
        synchronized (ni.class) {
            if (Ce == null) {
                Ce = new ni();
            }
            niVar = Ce;
        }
        return niVar;
    }

    private boolean fh() {
        if (this.Cg.size() == 0) {
            synchronized (this.Cg) {
                if (this.Cg.size() == 0) {
                    for (String myVar : fi()) {
                        this.Cg.add(new my(myVar));
                    }
                }
            }
        }
        return this.Cg.size() > 0;
    }

    public static final List<String> fi() {
        List list = null;
        qz qzVar = jq.uh;
        if (qzVar != null) {
            list = qzVar.ig();
        }
        if (list == null) {
            list = Arrays.asList(Cf);
        }
        List arrayList = new ArrayList();
        for (String str : r0) {
            if (nh.checkService(str) != null) {
                arrayList.add(str);
            }
        }
        return arrayList;
    }

    public boolean endCall() {
        boolean z = false;
        qz qzVar = jq.uh;
        if (!fh()) {
            return false;
        }
        boolean z2;
        if (qzVar != null && qzVar.il()) {
            z2 = false;
            for (mx mxVar : this.Cg) {
                if (mxVar.bF(0)) {
                    z2 = true;
                }
                z2 = !mxVar.bF(1) ? z2 : true;
            }
        } else {
            for (mx mxVar2 : this.Cg) {
                if (mxVar2.endCall()) {
                    z = true;
                }
            }
            z2 = z;
        }
        return z2;
    }
}
