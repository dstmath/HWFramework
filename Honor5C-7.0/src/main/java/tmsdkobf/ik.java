package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ik {
    private static ik rY;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ik.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ik.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ik.<clinit>():void");
    }

    private ik() {
    }

    public static synchronized ik bM() {
        ik ikVar;
        synchronized (ik.class) {
            if (rY == null) {
                rY = new ik();
            }
            ikVar = rY;
        }
        return ikVar;
    }

    public void b(Context context) {
        fu u = fu.u();
        u.setContext(context);
        u.b(d.isEnable(), "TMSLog");
        u.c(d.isEnable());
        u.d(false);
        u.e(false);
        u.f(true);
        u.g(true);
        u.h(false);
        u.i(false);
        u.j(false);
        u.af("tms.pService");
        u.ag("_tms");
        u.k(true);
        u.a(null);
        u.l(true);
        if (VERSION.SDK_INT < 21) {
            u.m(true);
        } else {
            u.m(false);
        }
        u.a(new Intent("com.tencent.tmsecure.ACTION_PKG_MONITOR"));
    }
}
