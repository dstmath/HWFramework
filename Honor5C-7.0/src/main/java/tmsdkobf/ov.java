package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.numbermarker.NumQueryRet;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class ov extends BaseManagerC {
    public static String TAG;
    private qt CR;
    private qo EV;
    private ExecutorService EW;
    private Handler yB;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.ov.1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ ov EX;

        AnonymousClass1(ov ovVar, Looper looper) {
            this.EX = ovVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            c cVar;
            a aVar;
            switch (message.what) {
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    cVar = (c) message.obj;
                    if (!cVar.EZ.dC()) {
                        cVar.EZ.setState(1);
                        this.EX.EW.submit(new d(this.EX, cVar));
                        break;
                    }
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    aVar = (a) message.obj;
                    if (!aVar.EZ.dC()) {
                        aVar.EZ.setState(1);
                        this.EX.EW.submit(new b(this.EX, aVar));
                        break;
                    }
                case FileInfo.TYPE_BIGFILE /*3*/:
                    cVar = (c) message.obj;
                    if (!(cVar == null || cVar.Fg == null)) {
                        cVar.Fg.a(cVar.EY, cVar.Ff);
                        break;
                    }
                case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                    aVar = (a) message.obj;
                    if (!(aVar == null || aVar.Fb == null)) {
                        aVar.Fb.a(aVar.EY, aVar.Fa);
                        break;
                    }
            }
        }
    }

    /* compiled from: Unknown */
    static class a {
        public int EY;
        public ll EZ;
        public List<qs> Fa;
        public le Fb;
        public int qM;
    }

    /* compiled from: Unknown */
    class b implements Runnable {
        final /* synthetic */ ov EX;
        public a Fc;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.ov.b.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ le Fd;
            final /* synthetic */ b Fe;

            AnonymousClass1(b bVar, le leVar) {
                this.Fe = bVar;
                this.Fd = leVar;
            }

            public void run() {
                this.Fd.a(this.Fe.Fc.EY, this.Fe.Fc.Fa);
            }
        }

        public b(ov ovVar, a aVar) {
            this.EX = ovVar;
            this.Fc = aVar;
        }

        public void run() {
            if (this.Fc != null && this.Fc.Fa != null) {
                le leVar = this.Fc.Fb;
                this.Fc.EY = this.EX.EV.w(this.Fc.Fa);
                if (this.Fc.EZ != null) {
                    this.Fc.EZ.setState(2);
                }
                if (leVar != null) {
                    switch (lk.bf(this.Fc.qM)) {
                        case RubbishType.SCAN_FLAG_APK /*8*/:
                            Message obtain = Message.obtain();
                            obtain.obj = this.Fc;
                            obtain.what = 4;
                            this.EX.yB.sendMessage(obtain);
                            break;
                        case NumQueryRet.USED_FOR_Common /*16*/:
                            leVar.a(this.Fc.EY, this.Fc.Fa);
                            break;
                        default:
                            jq.ct().a(new AnonymousClass1(this, leVar), "run callback");
                            break;
                    }
                }
            }
        }
    }

    /* compiled from: Unknown */
    static class c {
        public int EY;
        public ll EZ;
        public qs Ff;
        public ld Fg;
        public int qM;

        public c(qs qsVar, ld ldVar, int i) {
            this.EZ = new ll();
            this.Ff = qsVar;
            this.Fg = ldVar;
            this.qM = i;
        }
    }

    /* compiled from: Unknown */
    class d implements Runnable {
        final /* synthetic */ ov EX;
        public c Fh;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.ov.d.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ ld Fi;
            final /* synthetic */ d Fj;

            AnonymousClass1(d dVar, ld ldVar) {
                this.Fj = dVar;
                this.Fi = ldVar;
            }

            public void run() {
                this.Fi.a(this.Fj.Fh.EY, this.Fj.Fh.Ff);
            }
        }

        public d(ov ovVar, c cVar) {
            this.EX = ovVar;
            this.Fh = cVar;
        }

        public void run() {
            if (this.Fh != null && this.Fh.Ff != null) {
                ld ldVar = this.Fh.Fg;
                this.Fh.EY = this.EX.EV.a(this.Fh.Ff);
                tmsdk.common.utils.d.g(ov.TAG, "runHttpSession err : " + this.Fh.EY);
                if (this.Fh.EZ != null) {
                    this.Fh.EZ.setState(2);
                }
                if (ldVar != null) {
                    switch (lk.bf(this.Fh.qM)) {
                        case RubbishType.SCAN_FLAG_APK /*8*/:
                            Message obtain = Message.obtain();
                            obtain.obj = this.Fh;
                            obtain.what = 3;
                            this.EX.yB.sendMessage(obtain);
                            break;
                        case NumQueryRet.USED_FOR_Common /*16*/:
                            ldVar.a(this.Fh.EY, this.Fh.Ff);
                            break;
                        default:
                            jq.ct().a(new AnonymousClass1(this, ldVar), "run callback");
                            break;
                    }
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.ov.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.ov.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.ov.<clinit>():void");
    }

    public ov() {
        this.yB = new AnonymousClass1(this, Looper.getMainLooper());
    }

    WeakReference<ll> a(long j, qs qsVar, int i, ld ldVar) {
        tmsdk.common.utils.d.g(TAG, "ident : " + j + " sendOldProtocol ");
        if (qsVar == null) {
            return null;
        }
        c cVar = new c(qsVar, ldVar, i);
        Message obtain = Message.obtain();
        obtain.obj = cVar;
        obtain.what = 1;
        this.yB.sendMessage(obtain);
        return new WeakReference(cVar.EZ);
    }

    WeakReference<ll> a(long j, qs qsVar, ld ldVar) {
        return a(j, qsVar, 0, ldVar);
    }

    public void onCreate(Context context) {
        this.EW = Executors.newSingleThreadExecutor();
        this.CR = (qt) ManagerCreatorC.getManager(qt.class);
        this.EV = this.CR.ic();
    }
}
