package tmsdkobf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Pair;
import com.qq.taf.jce.JceStruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.i;

public final class ny {
    private static ny FS = null;
    private final Object DA = new Object();
    private nl Dz;
    private tmsdkobf.ns.a FL;
    private int FM = Process.myPid();
    private ExecutorService FN;
    private ArrayList<a> FO = new ArrayList();
    private TreeMap<Integer, a> FP = new TreeMap();
    private TreeMap<Integer, Pair<JceStruct, ka>> FQ = new TreeMap();
    private Handler FR = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 11:
                    Object[] objArr = (Object[]) message.obj;
                    a aVar = (a) objArr[0];
                    if (aVar.Gk != null) {
                        aVar.Gk.onFinish(((Integer) objArr[1]).intValue(), aVar.Gf, ((Integer) objArr[2]).intValue(), ((Integer) objArr[3]).intValue(), aVar.Gi);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private Handler Fa = new Handler(nu.getLooper()) {
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case 0:
                    ny.this.a((a) message.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private Handler vH = new Handler(nu.getLooper()) {
        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    try {
                        ny.this.vH.removeMessages(1);
                        Runnable bVar = new b(ny.this, null);
                        synchronized (ny.this.DA) {
                            Iterator it = ny.this.FO.iterator();
                            while (it.hasNext()) {
                                a aVar = (a) it.next();
                                bVar.a(Integer.valueOf(aVar.Gc), aVar);
                                if ((aVar.Gj & 1073741824) == 0) {
                                    ny.this.FP.put(Integer.valueOf(aVar.Gc), aVar);
                                }
                                mb.d("SharkProcessProxy", ny.this.FM + " sendShark() MSG_SEND_PROXY_TASK task.mIpcSeqNo: " + aVar.Gc);
                            }
                            ny.this.FO.clear();
                        }
                        ny.this.FN.submit(bVar);
                        return;
                    } catch (Exception e) {
                        mb.o("SharkProcessProxy", "exception: " + e);
                        return;
                    }
                default:
                    return;
            }
        }
    };

    public class a {
        public int FM;
        public int Gc;
        public int Gd;
        public long Ge;
        public int Gf;
        public long Gg;
        public JceStruct Gh;
        public JceStruct Gi;
        public int Gj;
        public jy Gk;
        public long Gl = -1;
        public long Gm = System.currentTimeMillis();
        public long mTimeout = -1;

        a(int i, int i2, int i3, long j, long j2, int i4, JceStruct jceStruct, JceStruct jceStruct2, int i5, jy jyVar, long j3, long j4) {
            this.FM = i;
            this.Gc = i2;
            this.Gd = i3;
            this.Ge = j;
            this.Gf = i4;
            this.Gg = j2;
            this.Gh = jceStruct;
            this.Gi = jceStruct2;
            this.Gj = i5;
            this.Gk = jyVar;
            this.mTimeout = j3;
            this.Gl = j4;
        }

        public boolean gp() {
            boolean z = true;
            long abs = Math.abs(System.currentTimeMillis() - this.Gm);
            if (abs < (!((this.mTimeout > 0 ? 1 : (this.mTimeout == 0 ? 0 : -1)) <= 0) ? this.mTimeout : 185000)) {
                z = false;
            }
            if (z) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("cmdId|").append(this.Gf);
                stringBuilder.append("|mIpcSeqNo|").append(this.Gc);
                stringBuilder.append("|mPushSeqNo|").append(this.Gd);
                stringBuilder.append("|mPushId|").append(this.Ge);
                stringBuilder.append("|mCallerIdent|").append(this.Gg);
                stringBuilder.append("|mTimeout|").append(this.mTimeout);
                stringBuilder.append("|time(s)|").append(abs / 1000);
                nv.c("ocean", "[ocean][time_out]SharkProcessProxy.SharkProxyTask.isTimeOut(), " + stringBuilder.toString(), null, null);
            }
            return z;
        }
    }

    private class b implements Runnable {
        private TreeMap<Integer, a> Gn;

        private b() {
            this.Gn = new TreeMap();
        }

        /* synthetic */ b(ny nyVar, AnonymousClass1 anonymousClass1) {
            this();
        }

        public void a(Integer num, a aVar) {
            this.Gn.put(num, aVar);
        }

        public Set<Entry<Integer, a>> gw() {
            TreeMap treeMap;
            synchronized (this.Gn) {
                treeMap = (TreeMap) this.Gn.clone();
            }
            return treeMap.entrySet();
        }

        public void run() {
            boolean hm = i.hm();
            for (Entry entry : gw()) {
                if (!hm) {
                    mb.n("SharkProcessProxy", ny.this.FM + " run, 无物理网络");
                    ny.this.a(Process.myPid(), ((a) entry.getValue()).Gc, 0, ((a) entry.getValue()).Gf, null, -1000002, 0);
                    mb.s("SharkProcessProxy", "[ocean]SharkProxyTaskRunnable.run(), no network: cmdId: " + ((a) entry.getValue()).Gf + " retCode: " + -1000002);
                    ny.this.Dz.e(((a) entry.getValue()).Gf, -1000002);
                } else if (((a) entry.getValue()).gp()) {
                    ny.this.a(Process.myPid(), ((a) entry.getValue()).Gc, 0, ((a) entry.getValue()).Gf, null, -1000017, 0);
                    mb.s("SharkProcessProxy", "[ocean][time_out]SharkProxyTaskRunnable.run(), send time out, stats by onConnnect(): cmdId: " + ((a) entry.getValue()).Gf + " retCode: " + -1000017);
                    ny.this.Dz.e(((a) entry.getValue()).Gf, -1000017);
                } else {
                    mb.n("SharkProcessProxy", ny.this.FM + " onPostToSendingProcess() mPid: " + ((a) entry.getValue()).FM + " mCallerIdent: " + ((a) entry.getValue()).Gg + " mIpcSeqNo: " + ((a) entry.getValue()).Gc + " mPushSeqNo: " + ((a) entry.getValue()).Gd + " mPushId: " + ((a) entry.getValue()).Ge + " mCmdId: " + ((a) entry.getValue()).Gf + " mFlag: " + ((a) entry.getValue()).Gj + " mTimeout: " + ((a) entry.getValue()).mTimeout);
                    ny.this.Fa.sendMessageDelayed(Message.obtain(ny.this.Fa, 0, entry.getValue()), 185000);
                    ny.this.Dz.a(((a) entry.getValue()).FM, ((a) entry.getValue()).Gg, ((a) entry.getValue()).Gc, ((a) entry.getValue()).Gd, ((a) entry.getValue()).Ge, ((a) entry.getValue()).Gf, nh.b(((a) entry.getValue()).Gh), ((a) entry.getValue()).Gj, ((a) entry.getValue()).mTimeout, ((a) entry.getValue()).Gl, ((a) entry.getValue()).Gm);
                }
            }
        }
    }

    private ny(nl nlVar) {
        this.Dz = nlVar;
        this.FL = new tmsdkobf.ns.a();
        this.FN = Executors.newSingleThreadExecutor();
    }

    private void a(final a aVar) {
        mb.d("SharkProcessProxy", "runTimeout() sharkProxyTask: " + aVar.Gc);
        this.Fa.removeMessages(0, aVar);
        synchronized (this.DA) {
            if (this.FP.containsKey(Integer.valueOf(aVar.Gc))) {
                ((ki) fj.D(4)).addTask(new Runnable() {
                    public void run() {
                        ny.this.a(Process.myPid(), aVar.Gc, 0, aVar.Gf, null, ne.bj(-2050000), 0);
                    }
                }, "sharkProcessProxyTimeout");
                return;
            }
        }
    }

    public static synchronized ny gv() {
        ny nyVar;
        synchronized (ny.class) {
            if (FS == null) {
                FS = new ny(((nz) ManagerCreatorC.getManager(nz.class)).gl());
            }
            nyVar = FS;
        }
        return nyVar;
    }

    public void a(int i, int i2, int i3, int i4, byte[] bArr, int i5, int i6) {
        if (this.FM == i) {
            final int i7 = i2;
            final byte[] bArr2 = bArr;
            final int i8 = i4;
            final int i9 = i3;
            final int i10 = i5;
            final int i11 = i6;
            ((ki) fj.D(4)).addTask(new Runnable() {
                public void run() {
                    try {
                        a aVar;
                        synchronized (ny.this.DA) {
                            aVar = (a) ny.this.FP.remove(Integer.valueOf(i7));
                        }
                        if (aVar != null) {
                            ny.this.Fa.removeMessages(0, aVar);
                            JceStruct b = nh.b(bArr2, aVar.Gi);
                            if (aVar.Gi != b) {
                                aVar.Gi = b;
                            }
                            aVar.Gf = i8;
                            mb.n("SharkProcessProxy", ny.this.FM + " callBack() ipcSeqNo: " + i7 + " seqNo: " + i9 + " cmdId: " + i8 + " retCode: " + i10 + " dataRetCode: " + i11);
                            ny.this.a(aVar, Integer.valueOf(i9), Integer.valueOf(i10), Integer.valueOf(i11));
                            return;
                        }
                        mb.o("SharkProcessProxy", ny.this.FM + " callBack(), no callback for ipcSeqNo: " + i7);
                    } catch (Exception e) {
                        mb.o("SharkProcessProxy", "exception: " + e);
                        Exception exception = e;
                    }
                }
            }, "shark callback");
            return;
        }
        mb.s("SharkProcessProxy", this.FM + " callBack() not my pid's response, its pid is: " + i);
    }

    public void a(int i, long j, int i2, long j2, int i3, JceStruct jceStruct, JceStruct jceStruct2, int i4, jy jyVar, long j3, long j4) {
        mb.d("SharkProcessProxy", this.FM + " sendShark()");
        a aVar = new a(i, this.FL.fP(), i2, j2, j, i3, jceStruct, jceStruct2, i4, jyVar, j3, j4);
        synchronized (this.DA) {
            this.FO.add(aVar);
        }
        this.vH.sendEmptyMessage(1);
    }

    public void a(long j, int i, JceStruct jceStruct, int i2, ka kaVar) {
        synchronized (this.FQ) {
            mb.d("SharkProcessProxy", this.FM + " registerSharkPush() callIdent: " + j + " cmdId: " + i + " flag: " + i2);
            if (this.FQ.containsKey(Integer.valueOf(i))) {
                String str = "[shark_push]registerSharkPush(), only one listener is allowed for current version! callIdent: " + j + " cmdId: " + i + " flag: " + i2;
                if (nu.ge()) {
                    throw new RuntimeException(str);
                }
                mb.o("SharkProcessProxy", str);
            } else {
                this.FQ.put(Integer.valueOf(i), new Pair(jceStruct, kaVar));
                final long j2 = j;
                final int i3 = i;
                final int i4 = i2;
                ((ki) fj.D(4)).addTask(new Runnable() {
                    public void run() {
                        if (ny.this.Dz == null) {
                            mb.o("SharkProcessProxy", "shark register push failed");
                        } else {
                            ny.this.Dz.a(j2, i3, i4);
                        }
                    }
                }, "shark register push");
            }
        }
    }

    protected void a(a aVar, Integer num, Integer num2, Integer num3) {
        if (aVar.Gk != null) {
            nv.a("ocean", "[ocean]procallback: ECmd|" + aVar.Gf + "|ipcSeqNo|" + aVar.Gc + "|seqNo|" + num + "|ret|" + num2 + "|dataRetCode|" + num3 + "|ident|" + aVar.Gg, null, null);
            switch (kc.al(aVar.Gj)) {
                case 8:
                    this.FR.sendMessage(this.FR.obtainMessage(11, new Object[]{aVar, num, num2, num3}));
                    break;
                case 16:
                    aVar.Gk.onFinish(num.intValue(), aVar.Gf, num2.intValue(), num3.intValue(), aVar.Gi);
                    break;
                default:
                    aVar.Gk.onFinish(num.intValue(), aVar.Gf, num2.intValue(), num3.intValue(), aVar.Gi);
                    break;
            }
        }
    }

    public ka v(final int i, final int i2) {
        ka kaVar = null;
        synchronized (this.FQ) {
            mb.d("SharkProcessProxy", this.FM + "unregisterSharkPush() cmdId: " + i + " flag: " + i2);
            if (this.FQ.containsKey(Integer.valueOf(i))) {
                kaVar = (ka) ((Pair) this.FQ.remove(Integer.valueOf(i))).second;
                ((ki) fj.D(4)).addTask(new Runnable() {
                    public void run() {
                        ny.this.Dz.b(i, i2);
                    }
                }, "shark unregist push");
            }
        }
        return kaVar;
    }
}
