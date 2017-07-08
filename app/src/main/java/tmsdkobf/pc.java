package tmsdkobf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Pair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.numbermarker.NumQueryRet;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public final class pc {
    private static pc Gr;
    private oq FC;
    private Handler FT;
    private tmsdkobf.oz.a Gl;
    private int Gm;
    private ExecutorService Gn;
    private ArrayList<a> Go;
    private TreeMap<Integer, a> Gp;
    private TreeMap<Integer, Pair<fs, li>> Gq;
    private Handler yB;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pc.1 */
    class AnonymousClass1 extends Handler {
        final /* synthetic */ pc Gs;

        AnonymousClass1(pc pcVar, Looper looper) {
            this.Gs = pcVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    this.Gs.yB.removeMessages(1);
                    Object bVar = new b(null);
                    synchronized (this.Gs.Go) {
                        Iterator it = this.Gs.Go.iterator();
                        while (it.hasNext()) {
                            a aVar = (a) it.next();
                            bVar.a(Integer.valueOf(aVar.GC), aVar);
                            if ((aVar.GJ & 1073741824) == 0) {
                                this.Gs.Gp.put(Integer.valueOf(aVar.GC), aVar);
                                break;
                            }
                            d.e("SharkProcessProxy", this.Gs.Gm + " sendShark() MSG_SEND_PROXY_TASK task.mIpcSeqNo: " + aVar.GC);
                            break;
                        }
                        d.e("SharkProcessProxy", this.Gs.Gm + " sendShark() MSG_SEND_PROXY_TASK send size: " + this.Gs.Go.size());
                        this.Gs.Go.clear();
                        break;
                    }
                    this.Gs.Gn.submit(bVar);
                    d.e("SharkProcessProxy", "taskrun.mProxyTaskQueue.size() : " + bVar.GM.size());
                    d.e("SharkProcessProxy", this.Gs.Gm + " sendShark() MSG_SEND_PROXY_TASK all cache size: " + this.Gs.Gp.size());
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    Object[] objArr = (Object[]) message.obj;
                    a aVar2 = (a) objArr[0];
                    if (aVar2.GK != null) {
                        aVar2.GK.onFinish(((Integer) objArr[1]).intValue(), aVar2.GF, ((Integer) objArr[2]).intValue(), ((Integer) objArr[3]).intValue(), aVar2.GI);
                    }
                default:
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pc.2 */
    class AnonymousClass2 extends Handler {
        final /* synthetic */ pc Gs;

        AnonymousClass2(pc pcVar, Looper looper) {
            this.Gs = pcVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case SpaceManager.ERROR_CODE_OK /*0*/:
                    this.Gs.a((a) message.obj);
                default:
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pc.3 */
    class AnonymousClass3 implements Runnable {
        final /* synthetic */ pc Gs;
        final /* synthetic */ long Gt;
        final /* synthetic */ int Gu;
        final /* synthetic */ int Gv;

        AnonymousClass3(pc pcVar, long j, int i, int i2) {
            this.Gs = pcVar;
            this.Gt = j;
            this.Gu = i;
            this.Gv = i2;
        }

        public void run() {
            this.Gs.FC.a(this.Gt, this.Gu, this.Gv);
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pc.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ pc Gs;
        final /* synthetic */ int Gu;
        final /* synthetic */ int Gv;

        AnonymousClass4(pc pcVar, int i, int i2) {
            this.Gs = pcVar;
            this.Gu = i;
            this.Gv = i2;
        }

        public void run() {
            this.Gs.FC.t(this.Gu, this.Gv);
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pc.5 */
    class AnonymousClass5 implements Runnable {
        final /* synthetic */ int GA;
        final /* synthetic */ pc Gs;
        final /* synthetic */ int Gu;
        final /* synthetic */ int Gw;
        final /* synthetic */ byte[] Gx;
        final /* synthetic */ int Gy;
        final /* synthetic */ int Gz;

        AnonymousClass5(pc pcVar, int i, byte[] bArr, int i2, int i3, int i4, int i5) {
            this.Gs = pcVar;
            this.Gw = i;
            this.Gx = bArr;
            this.Gu = i2;
            this.Gy = i3;
            this.Gz = i4;
            this.GA = i5;
        }

        public void run() {
            synchronized (this.Gs.Gp) {
                a aVar = (a) this.Gs.Gp.get(Integer.valueOf(this.Gw));
                if (aVar != null) {
                    fs c = ok.c(this.Gx, aVar.GI);
                    if (aVar.GI != c) {
                        aVar.GI = c;
                    }
                    aVar.GF = this.Gu;
                    d.d("SharkProcessProxy", this.Gs.Gm + " callBack() ipcSeqNo: " + this.Gw + " seqNo: " + this.Gy + " cmdId: " + this.Gu + " retCode: " + this.Gz + " dataRetCode: " + this.GA);
                    this.Gs.a(aVar, Integer.valueOf(this.Gy), Integer.valueOf(this.Gz), Integer.valueOf(this.GA));
                    this.Gs.Gp.remove(Integer.valueOf(this.Gw));
                    return;
                }
                d.c("SharkProcessProxy", this.Gs.Gm + " callBack() empty callback by ipcSeqNo: " + this.Gw);
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pc.6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ a GB;
        final /* synthetic */ pc Gs;

        AnonymousClass6(pc pcVar, a aVar) {
            this.Gs = pcVar;
            this.GB = aVar;
        }

        public void run() {
            this.Gs.a(Process.myPid(), this.GB.GC, 0, this.GB.GF, null, oi.bY(-50000), 0);
        }
    }

    /* compiled from: Unknown */
    private class a {
        public int GC;
        public int GD;
        public long GE;
        public int GF;
        public long GG;
        public fs GH;
        public fs GI;
        public int GJ;
        public lg GK;
        public long GL;
        public int Gm;
        final /* synthetic */ pc Gs;
        public long mTimeout;

        a(pc pcVar, int i, int i2, int i3, long j, long j2, int i4, fs fsVar, fs fsVar2, int i5, lg lgVar, long j3, long j4) {
            this.Gs = pcVar;
            this.mTimeout = -1;
            this.GL = -1;
            this.Gm = i;
            this.GC = i2;
            this.GD = i3;
            this.GE = j;
            this.GF = i4;
            this.GG = j2;
            this.GH = fsVar;
            this.GI = fsVar2;
            this.GJ = i5;
            this.GK = lgVar;
            this.mTimeout = j3;
            this.GL = j4;
        }
    }

    /* compiled from: Unknown */
    private class b implements Runnable {
        private TreeMap<Integer, a> GM;
        final /* synthetic */ pc Gs;

        private b(pc pcVar) {
            this.Gs = pcVar;
            this.GM = new TreeMap();
        }

        public void a(Integer num, a aVar) {
            this.GM.put(num, aVar);
        }

        public Set<Entry<Integer, a>> gu() {
            TreeMap treeMap;
            synchronized (this.GM) {
                treeMap = (TreeMap) this.GM.clone();
            }
            return treeMap.entrySet();
        }

        public void run() {
            boolean hv = f.hv();
            for (Entry entry : gu()) {
                if (hv) {
                    d.d("SharkProcessProxy", this.Gs.Gm + " onPostToSendingProcess() mPid: " + ((a) entry.getValue()).Gm + " mCallerIdent: " + ((a) entry.getValue()).GG + " mIpcSeqNo: " + ((a) entry.getValue()).GC + " mPushSeqNo: " + ((a) entry.getValue()).GD + " mPushId: " + ((a) entry.getValue()).GE + " mCmdId: " + ((a) entry.getValue()).GF + " mFlag: " + ((a) entry.getValue()).GJ + " mTimeout: " + ((a) entry.getValue()).mTimeout);
                    this.Gs.FC.a(((a) entry.getValue()).Gm, ((a) entry.getValue()).GG, ((a) entry.getValue()).GC, ((a) entry.getValue()).GD, ((a) entry.getValue()).GE, ((a) entry.getValue()).GF, ok.c(((a) entry.getValue()).GH), ((a) entry.getValue()).GJ, ((a) entry.getValue()).mTimeout, ((a) entry.getValue()).GL);
                    this.Gs.FT.sendMessageDelayed(Message.obtain(this.Gs.FT, 0, entry.getValue()), 185000);
                } else {
                    d.d("SharkProcessProxy", this.Gs.Gm + " run, \u65e0\u7269\u7406\u7f51\u7edc");
                    this.Gs.a(Process.myPid(), ((a) entry.getValue()).GC, 0, ((a) entry.getValue()).GF, null, -2, 0);
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.pc.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.pc.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.pc.<clinit>():void");
    }

    private pc(oq oqVar) {
        this.Gm = Process.myPid();
        this.Go = new ArrayList();
        this.Gp = new TreeMap();
        this.Gq = new TreeMap();
        this.yB = new AnonymousClass1(this, Looper.getMainLooper());
        this.FT = new AnonymousClass2(this, Looper.getMainLooper());
        this.FC = oqVar;
        this.Gl = new tmsdkobf.oz.a();
        this.Gn = Executors.newSingleThreadExecutor();
    }

    private void a(a aVar) {
        d.e("SharkProcessProxy", "runTimeout() sharkProxyTask: " + aVar.GC);
        this.FT.removeMessages(0, aVar);
        if (this.Gp.containsKey(Integer.valueOf(aVar.GC))) {
            jq.ct().a(new AnonymousClass6(this, aVar), "sharkProcessProxyTimeout");
        }
    }

    public static synchronized pc gt() {
        pc pcVar;
        synchronized (pc.class) {
            if (Gr == null) {
                Gr = new pc(((pd) ManagerCreatorC.getManager(pd.class)).gm());
            }
            pcVar = Gr;
        }
        return pcVar;
    }

    public void a(int i, int i2, int i3, int i4, byte[] bArr, int i5, int i6) {
        if (this.Gm == i) {
            jq.ct().a(new AnonymousClass5(this, i2, bArr, i4, i3, i5, i6), "shark callback");
        } else {
            d.f("SharkProcessProxy", this.Gm + " callBack() not my pid's response, its pid is: " + i);
        }
    }

    public void a(int i, long j, int i2, long j2, int i3, fs fsVar, fs fsVar2, int i4, lg lgVar, long j3, long j4) {
        d.e("SharkProcessProxy", this.Gm + " sendShark()");
        a aVar = new a(this, i, this.Gl.fP(), i2, j2, j, i3, fsVar, fsVar2, i4, lgVar, j3, j4);
        synchronized (this.Go) {
            this.Go.add(aVar);
        }
        this.yB.sendEmptyMessage(1);
    }

    public void a(long j, int i, fs fsVar, int i2, li liVar) {
        ClassCastException classCastException;
        synchronized (this.Gq) {
            d.e("SharkProcessProxy", this.Gm + " registerSharkPush() callIdent: " + j + " cmdId: " + i + " flag: " + i2);
            if (this.Gq.containsKey(Integer.valueOf(i))) {
                classCastException = new ClassCastException();
            } else {
                this.Gq.put(Integer.valueOf(i), new Pair(fsVar, liVar));
                jq.ct().a(new AnonymousClass3(this, j, i, i2), "shark regist push");
                classCastException = null;
            }
        }
        if (classCastException != null) {
            throw classCastException;
        }
    }

    protected void a(a aVar, Integer num, Integer num2, Integer num3) {
        if (aVar.GK != null) {
            pa.a("ocean", "[ocean]procallback: ECmd|" + aVar.GF + "|ipcSeqNo|" + aVar.GC + "|seqNo|" + num + "|ret|" + num2 + "|dataRetCode|" + num3 + "|ident|" + aVar.GG, null, null);
            switch (lk.bf(aVar.GJ)) {
                case RubbishType.SCAN_FLAG_APK /*8*/:
                    this.yB.sendMessage(this.yB.obtainMessage(2, new Object[]{aVar, num, num2, num3}));
                    break;
                case NumQueryRet.USED_FOR_Common /*16*/:
                    aVar.GK.onFinish(num.intValue(), aVar.GF, num2.intValue(), num3.intValue(), aVar.GI);
                    break;
                default:
                    aVar.GK.onFinish(num.intValue(), aVar.GF, num2.intValue(), num3.intValue(), aVar.GI);
                    break;
            }
        }
    }

    public li v(int i, int i2) {
        li liVar = null;
        synchronized (this.Gq) {
            d.e("SharkProcessProxy", this.Gm + "unregisterSharkPush() cmdId: " + i + " flag: " + i2);
            if (this.Gq.containsKey(Integer.valueOf(i))) {
                liVar = (li) ((Pair) this.Gq.remove(Integer.valueOf(i))).second;
                jq.ct().a(new AnonymousClass4(this, i, i2), "shark unregist push");
            }
        }
        return liVar;
    }
}
