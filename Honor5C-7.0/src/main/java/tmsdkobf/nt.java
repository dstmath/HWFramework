package tmsdkobf;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import java.util.HashMap;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.creator.ManagerCreatorC;

/* compiled from: Unknown */
final class nt extends BaseManagerC {
    private static int[] Dg;
    private HashMap<ComponentName, ServiceInfo> Dd;
    private byte[] De;
    private np Df;
    private ActivityManager mActivityManager;
    private Context mContext;
    private PackageManager mPackageManager;
    private qa pi;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.nt.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.nt.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.nt.<clinit>():void");
    }

    nt() {
        this.Dd = new HashMap();
        this.De = new byte[0];
    }

    public synchronized np fv() {
        if (this.Df == null) {
            this.Df = new nq(this.mContext);
        }
        return this.Df;
    }

    public int getSingletonType() {
        return 1;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.pi = (qa) ManagerCreatorC.getManager(qa.class);
    }
}
