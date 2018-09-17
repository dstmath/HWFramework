package tmsdkobf;

import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdkobf.ps.a;

/* compiled from: Unknown */
public class jn {
    private static volatile boolean tZ;
    private static a ua;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.jn.1 */
    static class AnonymousClass1 implements Runnable {
        final /* synthetic */ nc ub;

        AnonymousClass1(nc ncVar) {
            this.ub = ncVar;
        }

        public void run() {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (((qt) ManagerCreatorC.getManager(qt.class)).ib() == 0) {
                if (jn.ua != null) {
                    ps.t(TMSDKContext.getApplicaionContext()).c(jn.ua);
                }
                this.ub.a("reportlc", true, true);
            } else if (jn.ua == null) {
                jn.ua = new a() {
                    final /* synthetic */ AnonymousClass1 uc;

                    {
                        this.uc = r1;
                    }

                    public void cn() {
                    }

                    public void co() {
                        jn.reportChannelInfo();
                    }
                };
                ps.t(TMSDKContext.getApplicaionContext()).b(jn.ua);
            }
            jn.tZ = false;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.jn.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.jn.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.jn.<clinit>():void");
    }

    public static void reportChannelInfo() {
        if (!tZ) {
            tZ = true;
            nc ncVar = new nc("tms");
            if (!ncVar.getBoolean("reportlc", false)) {
                jq.ct().a(new AnonymousClass1(ncVar), "reportChannelInfoThread");
            }
        }
    }
}
