package tmsdkobf;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;
import tmsdkobf.kp.a;
import tmsdkobf.kp.b;

/* compiled from: Unknown */
public class kr {
    private static Object lock;
    private static kr wb;
    Handler handler;
    private SparseArray<kp> vY;
    HandlerThread vZ;
    Handler wa;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.kr.1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ kr wc;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.kr.1.1 */
        class AnonymousClass1 implements b {
            final /* synthetic */ kp wd;
            final /* synthetic */ AnonymousClass1 we;

            AnonymousClass1(AnonymousClass1 anonymousClass1, kp kpVar) {
                this.we = anonymousClass1;
                this.wd = kpVar;
            }

            public void q(ArrayList<a> arrayList) {
                if (arrayList != null && arrayList.size() > 0) {
                    this.wd.p(arrayList);
                }
                if (!this.wd.dk()) {
                    this.we.wc.b(this.wd);
                }
            }
        }

        AnonymousClass1(kr krVar, Looper looper) {
            this.wc = krVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            int i = message.what;
            this.wc.wa.removeMessages(i);
            kp kpVar = (kp) this.wc.vY.get(i);
            if (kpVar != null) {
                this.wc.a(kpVar, new AnonymousClass1(this, kpVar));
                if (!kpVar.dk()) {
                    this.wc.b(kpVar);
                }
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.kr.2 */
    class AnonymousClass2 extends Handler {
        final /* synthetic */ kr wc;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.kr.2.1 */
        class AnonymousClass1 implements hw {
            final /* synthetic */ kp wd;
            final /* synthetic */ AnonymousClass2 wf;

            AnonymousClass1(AnonymousClass2 anonymousClass2, kp kpVar) {
                this.wf = anonymousClass2;
                this.wd = kpVar;
            }

            public void a(int i, ArrayList<fs> arrayList, int i2, int i3) {
                if (i2 == 0) {
                    this.wd.o(this.wf.wc.a(i, (ArrayList) arrayList));
                    if (i == 0) {
                        this.wd.aZ(i3);
                    }
                }
            }
        }

        AnonymousClass2(kr krVar, Looper looper) {
            this.wc = krVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            kp kpVar;
            switch (message.what) {
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    kq kqVar = (kq) message.obj;
                    ArrayList arrayList = kqVar.vX;
                    kpVar = kqVar.vW;
                    if (!(arrayList == null || arrayList.size() <= 0 || kpVar == null)) {
                        kpVar.p(arrayList);
                        break;
                    }
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    Object obj = message.obj;
                    if (obj != null && (obj instanceof kp)) {
                        kpVar = (kp) obj;
                        this.wc.vY.remove(kpVar.dl());
                        this.wc.vY.append(kpVar.dl(), kpVar);
                        kpVar.a(new AnonymousClass1(this, kpVar));
                        break;
                    }
                    return;
                    break;
                case FileInfo.TYPE_BIGFILE /*3*/:
                    Integer num = (Integer) message.obj;
                    if (num != null) {
                        kpVar = (kp) this.wc.vY.get(num.intValue());
                        if (kpVar != null) {
                            kpVar.dm();
                            break;
                        }
                    }
                case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                    this.wc.b((kp) this.wc.vY.get(((Integer) message.obj).intValue()));
                    break;
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.kr.3 */
    class AnonymousClass3 implements b {
        final /* synthetic */ kr wc;
        final /* synthetic */ kp wd;

        AnonymousClass3(kr krVar, kp kpVar) {
            this.wc = krVar;
            this.wd = kpVar;
        }

        public void q(ArrayList<a> arrayList) {
            if (arrayList != null && arrayList.size() > 0) {
                Message.obtain(this.wc.handler, 1, new kq(this.wd, arrayList)).sendToTarget();
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.kr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.kr.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.kr.<clinit>():void");
    }

    private kr() {
        this.vY = new SparseArray();
        this.vZ = null;
        this.handler = null;
        this.wa = null;
        this.vZ = ((lq) fe.ad(4)).bF("ProfileServiceManager");
        this.vZ.start();
        this.wa = new AnonymousClass1(this, this.vZ.getLooper());
        this.handler = new AnonymousClass2(this, this.vZ.getLooper());
    }

    private void a(kp kpVar, b bVar) {
        if (kpVar != null) {
            hu.h("ProfileServiceManager", "MSG_FULL_CHECK id : " + kpVar.dl());
            kpVar.a(bVar);
        }
    }

    private void b(kp kpVar) {
        if (kpVar != null) {
            kpVar.b(new AnonymousClass3(this, kpVar));
        }
    }

    public static kr do() {
        if (wb == null) {
            synchronized (lock) {
                if (wb == null) {
                    wb = new kr();
                }
            }
        }
        return wb;
    }

    protected ArrayList<a> a(int i, ArrayList<fs> arrayList) {
        ArrayList<a> arrayList2 = new ArrayList();
        if (arrayList == null || arrayList.size() == 0) {
            return arrayList2;
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            fs fsVar = (fs) it.next();
            if (fsVar != null) {
                a aVar = new a();
                aVar.vV = fsVar;
                aVar.action = i;
                arrayList2.add(aVar);
            }
        }
        return arrayList2;
    }

    public void a(kp kpVar) {
        if (kpVar != null && this.handler != null) {
            Message.obtain(this.handler, 2, kpVar).sendToTarget();
        }
    }

    public void ba(int i) {
        Message.obtain(this.wa, i).sendToTarget();
    }

    public void bb(int i) {
        Message.obtain(this.handler, 4, Integer.valueOf(i)).sendToTarget();
    }
}
