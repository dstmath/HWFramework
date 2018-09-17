package tmsdkobf;

import android.util.SparseArray;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.ErrorCode;
import tmsdk.common.utils.q;
import tmsdkobf.ju.a;
import tmsdkobf.ju.b;

public class fh {
    private static fh mf;
    private byte[] mg;
    private SparseArray<b> mh;
    private List<r> mi;
    private long mj;
    private ob mk;
    private ka ml;

    private fh() {
        this.mg = new byte[0];
        this.mi = new ArrayList();
        this.mj = -1;
        this.ml = new ka() {
            public oh<Long, Integer, JceStruct> a(int i, long j, int i2, JceStruct jceStruct) {
                kv.d("WakeupUtil", "[" + Thread.currentThread().getId() + "][shark]onRecvPush-pushId:[" + j + "]cmdId:[" + i2 + "]");
                if (i2 == 10010 && jceStruct != null) {
                    List<s> list = ((x) jceStruct).at;
                    if (list == null || list.size() == 0) {
                        kv.n("WakeupUtil", "onRecvPush|scPushConchs is not null but conchTasks is null!");
                        return null;
                    }
                    l lVar = new l();
                    lVar.V = new ArrayList();
                    StringBuilder stringBuilder = new StringBuilder();
                    fh.this.mj = ((s) list.get(0)).al;
                    for (s sVar : list) {
                        kv.d("WakeupUtil", "conchTask-taskId:" + sVar.al + "|taskSeqno:" + sVar.am);
                        if (sVar.ap == null || sVar.ap.size() == 0) {
                            kv.n("WakeupUtil", "onRecvPush|(conchTask.conchList == null) || (conchTask.conchList.size() == 0)|conchTask.taskId:" + sVar.al);
                            lVar.V.add(fh.this.a(sVar.al, sVar.am, null, 3));
                        } else {
                            Iterator it = sVar.ap.iterator();
                            while (it.hasNext()) {
                                p pVar = (p) it.next();
                                kv.d("WakeupUtil", "conch-cmdId:" + pVar.Y);
                                a aVar = new a(sVar.al, sVar.am, pVar);
                                stringBuilder.append(pVar.Y + ";");
                                if (((b) fh.this.mh.get(aVar.tB != null ? aVar.tB.tD : pVar.Y)) != null) {
                                    kv.d("WakeupUtil", "cmdId:[" + pVar.Y + "]mLocalConchPushListener is not null");
                                    fh.this.a(aVar);
                                    lVar.V.add(fh.this.a(sVar.al, sVar.am, pVar, 1));
                                }
                            }
                        }
                    }
                    if (q.cJ(stringBuilder.toString())) {
                        kt.e(1320064, stringBuilder.toString());
                    }
                    return new oh(Long.valueOf(j), Integer.valueOf(i2), lVar);
                }
                kv.n("WakeupUtil", "onRecvPush|cmdId != ECmd.Cmd_SCPushConch : " + i2);
                return null;
            }
        };
        this.mj = -1;
        this.mh = new SparseArray();
        this.mk = im.bK();
        if (!gf.S().aj().booleanValue()) {
            h();
        }
    }

    private q a(long j, long j2, p pVar, int i) {
        kv.n("WakeupUtil", "createConchPushResult :taskId" + j);
        q qVar = new q();
        qVar.al = j;
        qVar.am = j2;
        if (pVar != null) {
            kv.n("WakeupUtil", "createConchPushResult :taskId" + j + " conch:" + pVar.Y);
            qVar.Y = pVar.Y;
            qVar.af = pVar.af;
        }
        qVar.result = i;
        return qVar;
    }

    private synchronized void a(final a aVar) {
        im.bJ().addTask(new Runnable() {
            public void run() {
                a aVar = aVar;
                int i = aVar.tA.Y;
                SparseArray b = fh.this.mh;
                if (!(aVar == null || aVar.tB == null)) {
                    i = aVar.tB.tD;
                }
                b bVar = (b) b.get(i);
                if (bVar != null) {
                    try {
                        bVar.b(aVar);
                    } catch (Throwable th) {
                    }
                }
            }
        }, "conchP");
    }

    public static fh g() {
        if (mf == null) {
            Class cls = fh.class;
            synchronized (fh.class) {
                if (mf == null) {
                    mf = new fh();
                }
            }
        }
        return mf;
    }

    private synchronized void j() {
        im.bJ().addTask(new Runnable() {
            /* JADX WARNING: Missing block: B:6:0x0015, code:
            if (r2 != null) goto L_0x0038;
     */
            /* JADX WARNING: Missing block: B:16:0x003a, code:
            if (r2.V == null) goto L_?;
     */
            /* JADX WARNING: Missing block: B:17:0x003c, code:
            tmsdkobf.fh.c(r9.mo).a(21, r2, new tmsdkobf.v(), 2, new tmsdkobf.fh.4.AnonymousClass1(r9));
     */
            /* JADX WARNING: Missing block: B:20:?, code:
            return;
     */
            /* JADX WARNING: Missing block: B:21:?, code:
            return;
     */
            /* JADX WARNING: Missing block: B:22:?, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                Throwable th;
                JceStruct jceStruct = null;
                synchronized (fh.this.mg) {
                    try {
                        if (fh.this.mi.size() > 0) {
                            JceStruct mVar = new m();
                            try {
                                mVar.V = new ArrayList(fh.this.mi);
                                fh.this.mi.clear();
                                jceStruct = mVar;
                            } catch (Throwable th2) {
                                th = th2;
                                jceStruct = mVar;
                                throw th;
                            }
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            }
        }, "conchRet");
    }

    public void a(long j, final int i) {
        kv.n("WakeupUtil", "pullConch : mIdent:" + j + " conchCmdId:" + i);
        final b bVar = (b) this.mh.get(i);
        if (bVar != null) {
            JceStruct nVar = new n();
            nVar.Y = i;
            kv.d("WakeupUtil", "ECmd.Cmd_CSPullConch");
            kt.aE(1320060);
            this.mk.a(11, nVar, new w(), 2, new jy() {
                public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
                    kv.d("WakeupUtil", "Cmd_CSPullConch-onFinish, retCode:[" + i3 + "]dataRetCode:[" + i4 + "]");
                    if (i3 != 0) {
                        bVar.tC = i3 - 65;
                    } else if (i4 != 0) {
                        bVar.tC = i4 - 65;
                    } else if (i2 != 10011) {
                        bVar.tC = ErrorCode.ERR_RESPONSE;
                    }
                    if (bVar.tC != 0) {
                        kt.e(1320061, "" + bVar.tC);
                    } else if (jceStruct != null && ((w) jceStruct).at != null && ((w) jceStruct).at.size() != 0) {
                        JceStruct lVar = new l();
                        lVar.V = new ArrayList();
                        StringBuilder stringBuilder = new StringBuilder();
                        if (fh.this.mj == ((s) ((w) jceStruct).at.get(0)).al) {
                            kv.n("WakeupUtil", "Pull receiveCmding conchTaskIDTemp");
                            return;
                        }
                        Iterator it = ((w) jceStruct).at.iterator();
                        while (it.hasNext()) {
                            s sVar = (s) it.next();
                            kv.n("WakeupUtil", "pullConch conchTask.taskId:" + sVar.al + "|taskSeqno:" + sVar.am);
                            if (sVar.ap == null || sVar.ap.size() == 0) {
                                kv.d("WakeupUtil", "ER_Invalid");
                                stringBuilder.append("0-;");
                                lVar.V.add(fh.this.a(sVar.al, sVar.am, null, 3));
                            } else {
                                Iterator it2 = sVar.ap.iterator();
                                while (it2.hasNext()) {
                                    p pVar = (p) it2.next();
                                    if (pVar.Y != i && fh.this.mh.get(pVar.Y) == null) {
                                        stringBuilder.append("2-" + pVar.Y + ";");
                                        lVar.V.add(fh.this.a(sVar.al, sVar.am, pVar, 5));
                                    } else {
                                        fh.this.a(new a(sVar.al, sVar.am, pVar));
                                        lVar.V.add(fh.this.a(sVar.al, sVar.am, pVar, 1));
                                        stringBuilder.append("1-" + pVar.Y + ";");
                                    }
                                }
                            }
                        }
                        if (lVar.V.size() > 0) {
                            kv.d("WakeupUtil", "Cmd_CSConchPushResult");
                            kt.e(1320061, stringBuilder.toString());
                            fh.this.mk.a(13, lVar, new u(), 2, null);
                        }
                    }
                }
            });
        }
    }

    public void a(long j, int i, b bVar) {
        if (bVar != null) {
            synchronized (this.mg) {
                if (this.mh.get(i) == null) {
                    this.mh.put(i, bVar);
                }
            }
        }
    }

    public void a(long j, long j2, long j3, int i, int i2, int i3, int i4) {
        r rVar = new r();
        rVar.al = j2;
        rVar.am = j3;
        rVar.Y = i;
        rVar.af = i2;
        rVar.an = i3;
        switch (i3) {
            case 1:
                rVar.action = i4;
                break;
            case 2:
                rVar.ao = i4;
                break;
            default:
                rVar.result = i4;
                break;
        }
        synchronized (this.mg) {
            this.mi.add(rVar);
        }
        j();
    }

    public void h() {
        kv.d("WakeupUtil", "registerSharkPush, ECmd.Cmd_SCPushConch");
        this.mk.a(10010, new x(), 2, this.ml);
    }

    public void i() {
        kv.d("WakeupUtil", "unRegisterSharkPush, ECmd.Cmd_SCPushConch");
        this.mk.v(10010, 2);
    }
}
