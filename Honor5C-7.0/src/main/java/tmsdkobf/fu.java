package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

public class fu {
    private static fu mC;
    private Context mContext;
    private boolean mD;
    private String mE;
    private boolean mF;
    private boolean mG;
    private boolean mH;
    private boolean mI;
    private boolean mJ;
    private boolean mK;
    private boolean mL;
    private boolean mM;
    private String mN;
    private String mO;
    private SparseArray<Integer> mP;
    private boolean mQ;
    private boolean mR;
    private boolean mS;
    private Intent mT;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.fu.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.fu.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.fu.<clinit>():void");
    }

    public static synchronized fu u() {
        fu fuVar;
        synchronized (fu.class) {
            if (mC == null) {
                mC = new fu();
            }
            fuVar = mC;
        }
        return fuVar;
    }

    private fu() {
        this.mD = true;
        this.mE = "TULog";
        this.mF = false;
        this.mG = false;
        this.mH = false;
        this.mI = true;
        this.mJ = true;
        this.mK = false;
        this.mL = false;
        this.mM = false;
        this.mN = "xxx.pService";
        this.mO = "_xxx";
        this.mP = null;
        this.mQ = false;
        this.mR = false;
        this.mS = false;
        this.mT = null;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public void b(boolean z, String str) {
        this.mD = z;
        this.mE = str;
    }

    public void c(boolean z) {
        this.mF = z;
    }

    public void d(boolean z) {
        this.mG = z;
    }

    public void e(boolean z) {
        this.mH = z;
    }

    public void f(boolean z) {
        this.mI = z;
    }

    public void g(boolean z) {
        this.mJ = z;
    }

    public void h(boolean z) {
        this.mK = z;
    }

    public void i(boolean z) {
        this.mL = z;
    }

    public void j(boolean z) {
        this.mM = z;
    }

    public void af(String str) {
        this.mN = str;
    }

    public void ag(String str) {
        this.mO = str;
    }

    public void a(int... iArr) {
        if (iArr != null) {
            if (this.mP == null) {
                this.mP = new SparseArray();
            }
            for (int put : iArr) {
                this.mP.put(put, Integer.valueOf(0));
            }
        }
    }

    public void k(boolean z) {
        this.mQ = z;
    }

    public void l(boolean z) {
        this.mR = z;
    }

    public void m(boolean z) {
        this.mS = z;
    }

    public void a(Intent intent) {
        this.mT = intent;
    }
}
