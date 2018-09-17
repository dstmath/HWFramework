package tmsdkobf;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.util.SparseIntArray;
import com.qq.taf.jce.JceStruct;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import tmsdk.common.CallerIdent;
import tmsdk.common.TMSDKContext;
import tmsdkobf.ji.c;

public class gs {
    private static Object lock = new Object();
    private static int[] oJ;
    private static gs oN;
    private HandlerThread mHandlerThread = ((ki) fj.D(4)).newFreeHandlerThread("profile upload task queue");
    private gw oG;
    private gq oH = gq.aZ();
    private HashMap<Integer, gn> oI = new HashMap();
    private Set<Integer> oK = new HashSet();
    private pf oL = new pf("profile4", 43200000, 24);
    private pf oM = new pf("profile2", 43200000, 12);
    private State oO = State.UNKNOWN;
    private State oP = State.UNKNOWN;
    private tmsdkobf.kh.a oQ = new tmsdkobf.kh.a() {
    };
    private ka oR = new ka() {
        public oh<Long, Integer, JceStruct> a(int i, long j, int i2, JceStruct jceStruct) {
            if (i2 == 11052) {
                nv.r("ProfileUpload", "recv profile full upload push");
                at atVar = (at) jceStruct;
                if (atVar != null) {
                    nv.r("ProfileUpload", "profilePush.profileID : " + atVar.bK + " profilePush.profileCmd : " + atVar.bY);
                    switch (atVar.bY) {
                        case 1:
                            gs.this.c(atVar.bK, 0);
                            if (atVar.bK == 2) {
                                gs.this.oH.g(true);
                                jk.cv().ah(atVar.bK);
                                break;
                            }
                            break;
                        case 2:
                            if (atVar.bK == 2) {
                                gs.this.oH.g(false);
                                break;
                            }
                            break;
                    }
                    return null;
                }
                nv.r("ProfileUpload", "profilePush == null");
                return null;
            }
            mb.o("ProfileUpload", "cmdId != ECmd.Cmd_SCProfilePushCmd : " + i2);
            return null;
        }
    };
    private ConcurrentHashMap<Integer, a> oS = new ConcurrentHashMap();
    private SparseIntArray oT = new SparseIntArray();
    private Handler oU;
    private Callback oV = new Callback() {
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    gs.this.Y(message.arg1);
                    break;
                case 2:
                    int i = message.arg1;
                    ArrayList X = gs.this.X(i);
                    if (X != null && X.size() > 0) {
                        byte[] a = gr.a(gr.a(i, 0, X));
                        if (a != null) {
                            gs.this.oT.put(i, a.length);
                            break;
                        }
                    }
                    gr.f("ProfileUpload", "get full upload jce");
                    return true;
                    break;
                case 3:
                    gs.this.oT.delete(message.arg1);
                    break;
                case 4:
                    new Bundle().putInt("profile.id", message.arg1);
                    break;
                case 5:
                    gs.this.a((gu) message.obj);
                    break;
                case 6:
                    gs.this.b((tmsdkobf.gp.a) message.obj);
                    break;
                case 7:
                    gs.this.a((gn) message.obj);
                    break;
                case 8:
                    gs.this.bd();
                    break;
                case 9:
                    gs.this.a((gx) message.obj);
                    break;
                case 10:
                    gs.this.oK.clear();
                    break;
            }
            return true;
        }
    };
    private HashSet<jl> oW = new HashSet();

    static class a {
        public c oY;
        public gt oZ;

        public a(c cVar, gt gtVar) {
            this.oY = cVar;
            this.oZ = gtVar;
        }
    }

    class b implements jy {
        tmsdkobf.gp.a pa;

        public b(tmsdkobf.gp.a aVar) {
            this.pa = aVar;
        }

        public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
            Message.obtain(gs.this.oU, 9, new gx(i, i2, i3, i4, jceStruct, this.pa)).sendToTarget();
        }
    }

    private gs() {
        State state;
        this.mHandlerThread.start();
        this.oU = new Handler(this.mHandlerThread.getLooper(), this.oV);
        this.oG = (gw) fj.D(5);
        this.oG.a(11052, new at(), 2, this.oR);
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) TMSDKContext.getApplicaionContext().getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo == null) {
            state = State.DISCONNECTED;
            this.oO = state;
        } else {
            state = activeNetworkInfo.getState();
            this.oO = state;
        }
        this.oP = state;
        h(30000);
        oJ = new int[6];
        for (int i = 1; i < 5; i++) {
            oJ[i] = 0;
        }
    }

    private void Y(int i) {
        a aVar = (a) this.oS.get(Integer.valueOf(i));
        if (aVar == null || aVar.oY == null) {
            gr.f("ProfileUpload", "profileID " + i + " callback null,can't full upload");
            return;
        }
        ArrayList cu = aVar.oY.cu();
        if (cu != null) {
            a(CallerIdent.getIdent(1, 4294967296L), i, 0, cu);
            return;
        }
        gr.f("ProfileUpload", "get full upload profile null,can't full upload");
    }

    private pf Z(int i) {
        switch (i) {
            case 2:
                return this.oM;
            case 4:
                return this.oL;
            default:
                return null;
        }
    }

    private void a(gn gnVar) {
        if (gnVar != null) {
            int aW = gp.aV().aW();
            nv.r("ProfileUpload", "enqueue force push taskID : " + aW);
            if (gnVar.oy == null || !gnVar.oy.cJ()) {
                this.oI.put(Integer.valueOf(aW), gnVar);
                bd();
            }
        }
    }

    private void a(tmsdkobf.gp.a aVar) {
        if (aVar != null) {
            gr.f("ProfileUpload", "---onSharkUnknown");
            if (aVar.bK > 0 && aVar.bK < 5) {
                pf Z = Z(aVar.bK);
                if (Z == null) {
                    c(aVar.bK, 0);
                } else if (Z.hI()) {
                    Z.hJ();
                    c(aVar.bK, 0);
                    gr.f("ProfileUpload", "unknown err,full upload");
                } else {
                    gr.f("ProfileUpload", "unknown err,full upload,freq ctrl,ignore this");
                }
            }
        }
    }

    private void a(gu guVar) {
        Object obj = null;
        if (guVar != null) {
            int i;
            int i2 = 0;
            JceStruct a = gr.a(guVar.bK, guVar.pb, guVar.pc);
            gr.f("ProfileUpload", "profileEnqueue");
            int i3 = a.bK;
            int i4 = a.bO;
            gp aV = gp.aV();
            if (this.oH.b(a) && i4 != 0) {
                gr.f("ProfileUpload", "ingnore this, the same as the last upload task");
                i = -1;
            } else {
                byte[] a2 = gr.a(a);
                if (a2 == null || a2.length == 0) {
                    i = -2;
                } else {
                    i2 = a2.length;
                    if (i4 == 0) {
                        aV.O(i3);
                        this.oH.T(i3);
                    }
                    long a3 = (long) aV.a(a2, i3);
                    gr.f("ProfileUpload", "profileEnqueue taskID : " + a3);
                    if (a3 >= 0) {
                        obj = 1;
                    }
                    if (obj == null) {
                        gr.f("ProfileUpload", "pushLast fail!!!");
                        i = -3;
                    } else {
                        i = 0;
                        this.oH.h(i3, a2.length);
                        if (i4 == 0) {
                            this.oT.put(i3, a2.length);
                        }
                        this.oH.a(a);
                    }
                }
            }
            a aVar = (a) this.oS.get(Integer.valueOf(i3));
            if (!(aVar == null || aVar.oZ == null)) {
                aVar.oZ.a(guVar.pb, guVar.pc, i, i2);
            }
            bd();
        }
    }

    private void a(gx gxVar) {
        if (gxVar != null) {
            tmsdkobf.gp.a aVar = gxVar.pa;
            int i = gxVar.eC;
            int i2 = gxVar.eB;
            if (aVar != null) {
                HashSet hashSet;
                ArrayList arrayList = new ArrayList();
                ar aY = aVar.aY();
                if (!(aY == null || aY.bN == null)) {
                    Iterator it = aY.bN.iterator();
                    while (it.hasNext()) {
                        byte[] bArr = (byte[]) it.next();
                        if (aY.bK == 4) {
                            arrayList.add(nn.a(bArr, new as(), false));
                        }
                    }
                }
                mb.d("TrafficCorrection", "ProfileUpload-retCode:[" + i2 + "]dataRetCode[" + i + "]");
                int i3 = 0;
                if (!(i2 == 0 && i == 0)) {
                    i3 = -1;
                }
                synchronized (this.oW) {
                    hashSet = new HashSet(this.oW);
                }
                Iterator it2 = hashSet.iterator();
                while (it2.hasNext()) {
                    ((jl) it2.next()).a(arrayList, i3);
                }
                this.oK.remove(Integer.valueOf(aVar.ox));
                if (aVar.aY() == null) {
                    gr.f("ProfileUpload", "recv profile resp retCode : " + i2 + " dataRetCode : " + i);
                } else {
                    gr.f("ProfileUpload", "recv profile resp retCode : " + i2 + " dataRetCode : " + i + " profileID : " + aVar.aY().bK + " actionID : " + aVar.aY().bO + " lastVerifyKey " + aVar.aY().bL + " presentVerifyKey " + aVar.aY().bM + " taskID " + aVar.ox);
                }
                if (i2 == 0) {
                    switch (i) {
                        case -1:
                            c(aVar);
                            break;
                        case 0:
                            b(aVar);
                            break;
                        default:
                            a(aVar);
                            break;
                    }
                } else if (i2 > 0) {
                    d(aVar);
                }
            }
        }
    }

    private void b(tmsdkobf.gp.a aVar) {
        if (aVar != null) {
            gp aV = gp.aV();
            switch (aVar.oD) {
                case 0:
                    if (aVar.aY() != null) {
                        int i = aVar.aY().bK;
                        gr.f("ProfileUpload", "+++onSharkSuccess");
                        oJ[i] = 0;
                        byte[] P = aV.P(aVar.ox);
                        if (P != null) {
                            this.oH.i(i, P.length);
                            gr.f("ProfileUpload", "popFirst success! taskID : " + aVar.ox);
                            break;
                        }
                        String str = "ProfileUpload";
                        gr.f(str, "popFirst fail! queueQuantity : " + this.oH.S(i) + " taskID : " + aVar.ox);
                        break;
                    }
                    return;
                case 1:
                    aV.P(aVar.ox);
                    gr.f("ProfileUpload", "popFirst success! taskID : " + aVar.ox);
                    break;
            }
        }
    }

    public static gs bc() {
        if (oN == null) {
            synchronized (lock) {
                if (oN == null) {
                    oN = new gs();
                }
            }
        }
        return oN;
    }

    private void bd() {
        gr.f("ProfileUpload", "uploadTask");
        List<tmsdkobf.gp.a> aX = gp.aV().aX();
        if (aX == null || aX.size() == 0) {
            mb.s("ProfileUpload", "uploadTask no more task");
            return;
        }
        Iterator it = aX.iterator();
        while (it.hasNext()) {
            if (this.oK.contains(Integer.valueOf(((tmsdkobf.gp.a) it.next()).ox))) {
                it.remove();
            }
        }
        if (aX == null || aX.size() == 0) {
            mb.s("ProfileUpload", "all task is uploading");
            return;
        }
        Object obj = null;
        for (tmsdkobf.gp.a aVar : aX) {
            if (!(aVar == null || aVar.aY() == null)) {
                int i = aVar.aY().bK;
                if (aVar.aY().bO != 0) {
                    int i2 = this.oT.get(i);
                    int S = this.oH.S(i);
                    if (S > i2 && i2 > 0) {
                        gr.f("ProfileUpload", "queue more than full,then full upload. queue : " + S + " quantity : " + i2);
                        c(i, 0);
                        obj = 1;
                    }
                }
            }
        }
        if (obj == null) {
            for (tmsdkobf.gp.a aVar2 : aX) {
                if (aVar2.aY() != null) {
                    this.oK.add(Integer.valueOf(aVar2.ox));
                    JceStruct aY = aVar2.aY();
                    gr.f("ProfileUpload", "send : profileID " + aY.bK + " actionID " + aY.bO + " lastVerifyKey " + aY.bL + " presentVerifyKey " + aY.bM + " taskID " + aVar2.ox);
                    this.oG.a(1051, aY, new au(), 18, new b(aVar2), 90000);
                } else if (aVar2.oD != 1) {
                    gr.f("ProfileUpload", "ProfileQueueTask neither force push nor upload profile");
                } else {
                    gn gnVar = (gn) this.oI.remove(Integer.valueOf(aVar2.ox));
                    if (gnVar != null && (gnVar == null || gnVar.oy == null || !gnVar.oy.cJ())) {
                        this.oK.add(Integer.valueOf(aVar2.ox));
                        gr.f("ProfileUpload", "send : cmdid : " + gnVar.Y + " taskID : " + gnVar.ox);
                        this.oG.c(gnVar.pid, gnVar.on, gnVar.oo, gnVar.ex, gnVar.op, gnVar.Y, gnVar.oq, gnVar.or, gnVar.os, gnVar.eE, gnVar.ot, gnVar.ou, gnVar.ov, gnVar.ow);
                        b(aVar2);
                    } else {
                        b(aVar2);
                    }
                }
            }
        }
    }

    private void c(int i, long j) {
        Message obtain = Message.obtain(this.oU, 1, i, 0);
        this.oU.removeMessages(1);
        this.oU.sendMessageDelayed(obtain, j);
    }

    private void c(tmsdkobf.gp.a aVar) {
        if (aVar != null) {
            gr.f("ProfileUpload", "---onSharkFail");
            int[] iArr = oJ;
            int i = aVar.bK;
            int i2 = iArr[i] + 1;
            iArr[i] = i2;
            if (i2 <= 2) {
                gr.f("ProfileUpload", "resend");
                h((long) (oJ[aVar.bK] * 30000));
                return;
            }
            oJ[aVar.bK] = 0;
            if (aVar.bK > 0 && aVar.bK < 5) {
                pf Z = Z(aVar.bK);
                if (Z == null) {
                    c(aVar.bK, 0);
                } else if (Z.hI()) {
                    Z.hJ();
                    c(aVar.bK, 0);
                    gr.f("ProfileUpload", "err more than 2,full upload");
                } else {
                    gr.f("ProfileUpload", "err more than 2,full upload,freq ctrl,ignore this");
                }
            }
        }
    }

    private void d(tmsdkobf.gp.a aVar) {
        if (aVar != null) {
            gr.f("ProfileUpload", "---onSharkFail");
            int[] iArr = oJ;
            int i = aVar.bK;
            int i2 = iArr[i] + 1;
            iArr[i] = i2;
            if (i2 <= 1) {
                gr.f("ProfileUpload", "resend");
                h((long) (oJ[aVar.bK] * 30000));
                return;
            }
            oJ[aVar.bK] = 0;
            gr.f("ProfileUpload", "err more than 1,wait next upload task");
        }
    }

    private void h(long j) {
        if (this.oP.compareTo(State.CONNECTED) != 0) {
            gr.f("ProfileUpload", "no network");
            return;
        }
        this.oU.removeMessages(8);
        this.oU.sendEmptyMessageDelayed(8, j);
    }

    public ArrayList<JceStruct> X(int i) {
        a aVar = (a) this.oS.get(Integer.valueOf(i));
        return (aVar == null || aVar.oY == null) ? null : aVar.oY.cu();
    }

    public void a(long j, int i, c cVar, gt gtVar, int i2) {
        if (((a) this.oS.put(Integer.valueOf(i), new a(cVar, gtVar))) == null) {
            this.oT.put(i, i2);
        }
    }

    public void a(jl jlVar) {
        synchronized (this.oW) {
            this.oW.add(jlVar);
        }
    }

    /* JADX WARNING: Missing block: B:2:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean a(long j, int i, int i2, ArrayList<JceStruct> arrayList) {
        if (i <= 0 || i >= 5 || this.oS.get(Integer.valueOf(i)) == null) {
            return false;
        }
        gr.f("ProfileUpload", "profileUpload  profileID : " + i + " profileActionID : " + i2);
        Message.obtain(this.oU, 5, new gu(i, i2, arrayList, null)).sendToTarget();
        return true;
    }

    public WeakReference<kd> b(int i, int i2, int i3, long j, long j2, int i4, JceStruct jceStruct, byte[] bArr, JceStruct jceStruct2, int i5, jy jyVar, jz jzVar, long j3, long j4) {
        kd kdVar = new kd();
        Message.obtain(this.oU, 7, new gn(i, i2, i3, j, j2, i4, jceStruct, bArr, jceStruct2, i5, jyVar, jzVar, j3, j4, kdVar)).sendToTarget();
        return new WeakReference(kdVar);
    }

    public void b(jl jlVar) {
        synchronized (this.oW) {
            this.oW.remove(jlVar);
        }
    }

    public void j(int i, int i2) {
        this.oT.put(i, i2);
    }
}
