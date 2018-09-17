package tmsdkobf;

import android.os.IBinder;
import android.os.RemoteException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.common.DataEntity;
import tmsdkobf.jl.b;

/* compiled from: Unknown */
public final class jo extends b {
    private static ConcurrentLinkedQueue<jm> ud;
    private static volatile jo ue;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.jo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.jo.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.jo.<clinit>():void");
    }

    private jo() {
    }

    public static boolean a(jm jmVar) {
        return ud.add(jmVar);
    }

    public static jo cp() {
        if (ue == null) {
            synchronized (jo.class) {
                if (ue == null) {
                    ue = new jo();
                }
            }
        }
        return ue;
    }

    public IBinder asBinder() {
        return this;
    }

    public DataEntity sendMessage(DataEntity dataEntity) throws RemoteException {
        int what = dataEntity.what();
        Iterator it = ud.iterator();
        while (it.hasNext()) {
            jm jmVar = (jm) it.next();
            if (jmVar.isMatch(what)) {
                return jmVar.onProcessing(dataEntity);
            }
        }
        return null;
    }
}
