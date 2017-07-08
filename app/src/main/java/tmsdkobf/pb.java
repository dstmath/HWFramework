package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.SparseArray;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tmsdk.common.TMSDKContext;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.common.utils.f;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public class pb implements tmsdkobf.pk.a {
    private final int FA;
    private final int FB;
    private oq FC;
    private ph FD;
    private ox FE;
    private ol FF;
    private String FG;
    private f FH;
    private SparseArray<qi> FI;
    private int FJ;
    private d FK;
    private d FL;
    private ArrayList<d> FM;
    private LinkedHashMap<Integer, d> FN;
    private ExecutorService FO;
    private boolean FP;
    private c FQ;
    private li FR;
    private tmsdkobf.ph.b FS;
    private Handler FT;
    private final int Fz;
    private final String TAG;
    private Context mContext;
    private Handler yB;

    /* compiled from: Unknown */
    public interface b {
        void a(boolean z, int i, int i2, ArrayList<bq> arrayList);
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pb.2 */
    class AnonymousClass2 extends Handler {
        final /* synthetic */ pb FU;

        /* compiled from: Unknown */
        /* renamed from: tmsdkobf.pb.2.1 */
        class AnonymousClass1 implements Runnable {
            final /* synthetic */ d FV;
            final /* synthetic */ boolean FW;
            final /* synthetic */ AnonymousClass2 FX;

            AnonymousClass1(AnonymousClass2 anonymousClass2, d dVar, boolean z) {
                this.FX = anonymousClass2;
                this.FV = dVar;
                this.FW = z;
            }

            public void run() {
                if (this.FX.FU.FI != null) {
                    ArrayList arrayList = this.FV.Gj;
                    if (arrayList != null && arrayList.size() > 0) {
                        Iterator it = arrayList.iterator();
                        while (it.hasNext()) {
                            bm bmVar = (bm) it.next();
                            if (bmVar != null) {
                                qi qiVar;
                                synchronized (this.FX.FU.FI) {
                                    qiVar = (qi) this.FX.FU.FI.get(bmVar.aZ);
                                }
                                if (qiVar != null) {
                                    if (qiVar.hK()) {
                                        qiVar.hL();
                                    } else {
                                        it.remove();
                                        pa.b("SharkNetwork", "network ctrl cmdid : " + bmVar.aZ, null, null);
                                        bq bqVar = new bq();
                                        bqVar.aZ = bmVar.aZ;
                                        bqVar.dJ = -7;
                                        this.FX.FU.a(true, false, this.FV, -7, 0, bqVar);
                                    }
                                }
                            }
                        }
                    }
                    if (arrayList != null && arrayList.size() > 0) {
                        synchronized (this.FX.FU.FI) {
                            qi qiVar2 = (qi) this.FX.FU.FI.get(997);
                            if (qiVar2 != null) {
                                if (!qiVar2.hK()) {
                                    this.FV.Ge = true;
                                }
                            }
                        }
                    } else {
                        return;
                    }
                }
                try {
                    this.FX.FU.a(this.FW, this.FV);
                } catch (Exception e) {
                    tmsdk.common.utils.d.c("SharkNetwork", e);
                }
            }
        }

        AnonymousClass2(pb pbVar, Looper looper) {
            this.FU = pbVar;
            super(looper);
        }

        private void b(boolean z, d dVar) {
            this.FU.FO.submit(new AnonymousClass1(this, dVar, z));
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case SpaceManager.ERROR_CODE_OK /*0*/:
                    tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND_VIP");
                    this.FU.yB.removeMessages(0);
                    if (this.FU.FK != null || this.FU.FL != null) {
                        if (this.FU.FK == null) {
                            if (this.FU.FL != null) {
                                tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND_VIP mSharkSendGuid");
                                b(true, this.FU.FL);
                                break;
                            }
                        }
                        tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND_VIP mSharkSendRsa");
                        b(false, this.FU.FK);
                        break;
                    }
                    tmsdk.common.utils.d.c("SharkNetwork", "MSG_SHARK_SEND_VIP null");
                    return;
                    break;
                case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                    tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND");
                    this.FU.yB.removeMessages(1);
                    tmsdkobf.ox.b gg = this.FU.FE.gg();
                    if (TextUtils.isEmpty(gg.Fs) || TextUtils.isEmpty(gg.Ft)) {
                        if (2 != this.FU.FJ) {
                            this.FU.FJ = 1;
                            this.FU.yB.sendEmptyMessage(2);
                            return;
                        }
                        tmsdk.common.utils.d.e("SharkNetwork", "MSG_SHARK_SEND \u6b63\u5728\u4ea4\u6362\u5bc6\u94a5");
                    } else if (this.FU.FP) {
                        tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND \u5bc6\u94a5\u8fc7\u671f");
                        this.FU.yB.sendEmptyMessage(2);
                    } else if (this.FU.FF.fL()) {
                        if (5 != this.FU.FJ) {
                            this.FU.FJ = 4;
                            this.FU.yB.sendEmptyMessage(3);
                            return;
                        }
                        tmsdk.common.utils.d.e("SharkNetwork", "MSG_SHARK_SEND \u6b63\u5728\u6ce8\u518cguid");
                    } else if (this.FU.FM.size() > 0) {
                        ArrayList arrayList;
                        synchronized (this.FU.FM) {
                            arrayList = (ArrayList) this.FU.FM.clone();
                            this.FU.FM.clear();
                            break;
                        }
                        if (this.FU.FK != null || this.FU.FL != null) {
                            tmsdk.common.utils.d.f("SharkNetwork", "MSG_SHARK_SEND  mSharkSendRsa: " + this.FU.FK + " mSharkSendGuid: " + this.FU.FL);
                        }
                        Iterator it = arrayList.iterator();
                        while (it.hasNext()) {
                            d dVar = (d) it.next();
                            if (dVar != null) {
                                if (this.FU.FP) {
                                    tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND sending \u5bc6\u94a5\u8fc7\u671f");
                                    this.FU.yB.sendEmptyMessage(2);
                                    return;
                                }
                                b(true, dVar);
                            }
                        }
                        break;
                    }
                case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                    tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_UPDATE_RSAKEY");
                    this.FU.yB.removeMessages(2);
                    this.FU.FO.submit(new Runnable() {
                        final /* synthetic */ AnonymousClass2 FX;

                        {
                            this.FX = r1;
                        }

                        public void run() {
                            try {
                                if (this.FX.FU.FJ != 2) {
                                    this.FX.FU.FJ = 2;
                                    this.FX.FU.FE.a(new tmsdkobf.ox.a() {
                                        final /* synthetic */ AnonymousClass2 FY;

                                        {
                                            this.FY = r1;
                                        }

                                        public void I(boolean z) {
                                            tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_UPDATE_RSAKEY " + (!z ? "\u5931\u8d25" : "\u6210\u529f"));
                                            this.FY.FX.FU.FJ = 3;
                                            if (z) {
                                                if (this.FY.FX.FU.FN.size() > 0) {
                                                    synchronized (this.FY.FX.FU.FM) {
                                                        synchronized (this.FY.FX.FU.FN) {
                                                            if (this.FY.FX.FU.FN.values().size() > 0) {
                                                                this.FY.FX.FU.FM.addAll(0, this.FY.FX.FU.FN.values());
                                                                this.FY.FX.FU.FN.clear();
                                                            }
                                                        }
                                                    }
                                                }
                                                this.FY.FX.FU.FP = false;
                                                this.FY.FX.FU.yB.sendEmptyMessage(1);
                                                return;
                                            }
                                            this.FY.FX.FU.a(true, false, oi.bY(-100));
                                        }
                                    });
                                    return;
                                }
                                pa.x("SharkNetwork", "is updating rsa_key.ingore it.");
                            } catch (Exception e) {
                                tmsdk.common.utils.d.c("SharkNetwork", "MSG_SHARK_UPDATE_RSAKEY e: " + e.toString());
                            }
                        }
                    });
                    break;
                case FileInfo.TYPE_BIGFILE /*3*/:
                    tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_GET_GUID");
                    this.FU.yB.removeMessages(3);
                    this.FU.FO.submit(new Runnable() {
                        final /* synthetic */ AnonymousClass2 FX;

                        {
                            this.FX = r1;
                        }

                        public void run() {
                            try {
                                if (this.FX.FU.FJ != 5) {
                                    this.FX.FU.FJ = 5;
                                    this.FX.FU.FF.a(new tmsdkobf.ol.a() {
                                        final /* synthetic */ AnonymousClass3 FZ;

                                        {
                                            this.FZ = r1;
                                        }

                                        public void c(boolean z, String str) {
                                            tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_GET_GUID " + (!z ? "\u5931\u8d25" : "\u6210\u529f"));
                                            this.FZ.FX.FU.FJ = 6;
                                            if (z) {
                                                this.FZ.FX.FU.yB.sendEmptyMessage(1);
                                            } else {
                                                this.FZ.FX.FU.a(true, false, oi.bY(-200));
                                            }
                                        }
                                    });
                                    return;
                                }
                                pa.x("SharkNetwork", "is geting guid.ingore it.");
                            } catch (Exception e) {
                                tmsdk.common.utils.d.c("SharkNetwork", "MSG_SHARK_GET_GUID e: " + e.toString());
                                this.FX.FU.a(true, false, oi.bY(-200));
                            }
                        }
                    });
                    break;
                case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                    this.FU.FO.submit(new Runnable() {
                        final /* synthetic */ AnonymousClass2 FX;

                        {
                            this.FX = r1;
                        }

                        public void run() {
                            if (this.FX.FU.FF != null) {
                                this.FX.FU.FF.G(true);
                            }
                        }
                    });
                    break;
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pb.3 */
    class AnonymousClass3 extends Handler {
        final /* synthetic */ pb FU;

        AnonymousClass3(pb pbVar, Looper looper) {
            this.FU = pbVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case SpaceManager.ERROR_CODE_OK /*0*/:
                    this.FU.a((d) message.obj);
                default:
            }
        }
    }

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.pb.6 */
    class AnonymousClass6 implements Runnable {
        final /* synthetic */ pb FU;
        final /* synthetic */ d FV;

        AnonymousClass6(pb pbVar, d dVar) {
            this.FU = pbVar;
            this.FV = dVar;
        }

        public void run() {
            if (this.FU.cd(this.FV.Gg) != null) {
                tmsdk.common.utils.d.d("SharkNetwork", "runTimeout() really timeout, sharkSend.seqNoTag: " + this.FV.Gg);
                this.FU.a(this.FV.Gd, this.FV, oi.bY(-50000), 0, null);
            }
        }
    }

    /* compiled from: Unknown */
    public interface a {
        void a(long j, int i, fs fsVar, int i2, li liVar, boolean z);
    }

    /* compiled from: Unknown */
    public interface c {
        long a(boolean z, int i, bq bqVar);
    }

    /* compiled from: Unknown */
    public static class d {
        public boolean Ga;
        public boolean Gb;
        public boolean Gc;
        public boolean Gd;
        public boolean Ge;
        public boolean Gf;
        public int Gg;
        public tmsdkobf.ox.b Gh;
        public long Gi;
        public ArrayList<bm> Gj;
        public b Gk;

        public d(boolean z, boolean z2, boolean z3, long j, ArrayList<bm> arrayList, b bVar) {
            this.Ga = false;
            this.Gb = false;
            this.Gc = false;
            this.Gd = false;
            this.Ge = false;
            this.Gf = false;
            this.Ga = z;
            this.Gb = z2;
            this.Gc = z3;
            this.Gi = j;
            this.Gj = arrayList;
            this.Gk = bVar;
        }

        public boolean gr() {
            return this.Gb || this.Gc;
        }

        public boolean gs() {
            return this.Ga;
        }
    }

    public pb(Context context, oq oqVar, c cVar, a aVar, boolean z) {
        this.TAG = "SharkNetwork";
        this.Fz = 0;
        this.FA = 1;
        this.FB = 2;
        this.FG = "";
        this.FI = null;
        this.FJ = 0;
        this.FK = null;
        this.FL = null;
        this.FM = new ArrayList();
        this.FN = new LinkedHashMap();
        this.FS = new tmsdkobf.ph.b() {
            final /* synthetic */ pb FU;

            {
                this.FU = r1;
            }

            public void a(boolean z, int i, byte[] bArr) {
                int bY;
                if (-160000 == i) {
                    bY = oi.bY(i);
                    tmsdk.common.utils.d.c("SharkNetwork", "spSend() ESharkCode.ERR_NEED_WIFIAPPROVEMENT == ret, doneRet: " + bY);
                    this.FU.a(false, z, bY);
                } else if (i != 0) {
                    bY = oi.bY(i);
                    tmsdk.common.utils.d.c("SharkNetwork", "spSend() ESharkCode.ERR_NONE != ret, doneRet: " + bY);
                    this.FU.a(false, z, bY);
                } else if (bArr != null) {
                    tmsdk.common.utils.d.e("SharkNetwork", "spSend() retData.length: " + bArr.length);
                    try {
                        br s = ot.s(bArr);
                        if (s != null) {
                            d a;
                            br brVar = s;
                            ArrayList arrayList = brVar.dV;
                            tmsdk.common.utils.d.e("SharkNetwork", "spSend() respSashimiList.size(): " + arrayList.size());
                            int i2 = brVar.dH;
                            if (this.FU.FK != null && this.FU.FK.Gg == i2) {
                                a = this.FU.FK;
                                this.FU.FK = null;
                            } else if (this.FU.FL != null && this.FU.FL.Gg == i2) {
                                a = this.FU.FL;
                                this.FU.FL = null;
                            } else {
                                a = (d) this.FU.FN.get(Integer.valueOf(i2));
                            }
                            tmsdk.common.utils.d.e("SharkNetwork", "spSend() seqNoTag: " + i2 + " ssTag: " + a);
                            boolean a2 = this.FU.A(arrayList);
                            if (arrayList != null) {
                                tmsdk.common.utils.d.d("SharkNetwork", "spSend() \u6536\u5230shark\u56de\u5305\uff0c\u5bc6\u94a5\u662f\u5426\u8fc7\u671f\uff1a" + (!a2 ? "\u5426" : "\u662f"));
                                tmsdk.common.utils.d.e("SharkNetwork", "spSend() retShark.seqNo: " + brVar.dG + " respSashimiList.size(): " + arrayList.size());
                                if (a2) {
                                    tmsdk.common.utils.d.e("SharkNetwork", "spSend() \u5bc6\u94a5\u8fc7\u671f");
                                    pa.b("ocean", "[ocean]\u5bc6\u94a5\u8fc7\u671f\uff0c\u81ea\u52a8\u4ea4\u6362\u5bc6\u94a5\u91cd\u53d1", null, null);
                                    this.FU.FP = true;
                                    if (a != null) {
                                        this.FU.b(a);
                                    }
                                    this.FU.yB.sendEmptyMessage(1);
                                    return;
                                }
                                tmsdk.common.utils.d.e("SharkNetwork", "spSend() \u6536\u5230shark\u56de\u5305");
                                this.FU.a(z, a, 0, brVar.dG, this.FU.a(a, z, brVar, arrayList));
                                return;
                            }
                            tmsdk.common.utils.d.c("SharkNetwork", "spSend() null == respSashimiList");
                            this.FU.a(z, a, -5, brVar.dG, null);
                            return;
                        }
                        tmsdk.common.utils.d.c("SharkNetwork", "spSend() null == obj");
                    } catch (Exception e) {
                        tmsdk.common.utils.d.c("SharkNetwork", "spSend() e: " + e.toString());
                    }
                } else {
                    tmsdk.common.utils.d.c("SharkNetwork", "spSend() null == retData");
                }
            }
        };
        this.yB = new AnonymousClass2(this, Looper.getMainLooper());
        this.FT = new AnonymousClass3(this, Looper.getMainLooper());
        tmsdk.common.utils.d.d("SharkNetwork", "SharkNetwork() isTest: " + z);
        this.mContext = context;
        this.FC = oqVar;
        this.FQ = cVar;
        this.FE = new ox(context, this);
        this.FF = new ol(context, this);
        this.FO = Executors.newSingleThreadExecutor();
        this.FD = new ph(this.FC.an(), context, oqVar, z, this.FS, this, aVar);
        a(aVar);
        int myPid = Process.myPid();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(!this.FC.an() ? "f" : "b");
        stringBuilder.append(myPid);
        this.FG = stringBuilder.toString();
        pa.b("ocean", "ext: " + this.FG, null, null);
    }

    private boolean A(ArrayList<bq> arrayList) {
        if (arrayList == null || arrayList.size() != 1) {
            return false;
        }
        bq bqVar = (bq) arrayList.get(0);
        if (bqVar == null) {
            return false;
        }
        if (!(2 == bqVar.dJ)) {
            return false;
        }
        tmsdk.common.utils.d.d("SharkNetwork", "checkRsa() ERC_EXPIRE retCode: " + bqVar.dJ + " dataRetCode: " + bqVar.dK);
        return true;
    }

    private final ArrayList<bq> a(d dVar, boolean z, br brVar, ArrayList<bq> arrayList) {
        if (arrayList == null) {
            return null;
        }
        ArrayList<bq> arrayList2 = new ArrayList();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            bq bqVar = (bq) arrayList.get(i);
            if (bqVar != null) {
                tmsdk.common.utils.d.d("SharkNetwork", "checkFilteList() rs.seqNo: " + bqVar.dG + " rs.cmd: " + bqVar.aZ + " rs.retCode: " + bqVar.dJ + " rs.dataRetCode: " + bqVar.dK);
                if (bqVar.data != null) {
                    tmsdk.common.utils.d.d("SharkNetwork", "checkFilteList() rs.data.length: " + bqVar.data.length);
                }
                if (!a(z, brVar, bqVar)) {
                    arrayList2.add(bqVar);
                }
            }
        }
        return arrayList2;
    }

    private bn a(d dVar, boolean z, boolean z2, tmsdkobf.ox.b bVar, ArrayList<bm> arrayList) {
        bn gf = ot.gf();
        int fP = oz.gj().fP();
        gf.dG = fP;
        dVar.Gg = fP;
        dVar.Gh = bVar;
        gf.dO = 2;
        gf.dP = a(z, bVar);
        gf.dQ = arrayList;
        return gf;
    }

    private f a(boolean z, tmsdkobf.ox.b bVar) {
        if (this.FH == null) {
            this.FH = new f();
        }
        String str = bVar == null ? this.FH.t : bVar.Fs;
        f fVar = this.FH;
        if (z) {
            str = "";
        }
        fVar.t = str;
        this.FH.u = TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_BUILD);
        this.FH.q = go();
        this.FH.v = f.A(this.mContext);
        this.FH.authType = gp();
        this.FH.r = this.FF.c();
        this.FH.s = this.FG;
        if (bVar != null) {
            tmsdk.common.utils.d.d("SharkNetwork", "checkSharkfin() rsaKeyCode: " + bVar.hashCode() + " rsaKey: " + bVar + " mSharkfin.buildno: " + this.FH.u + " mSharkfin.apn: " + this.FH.q + " mSharkfin.netType: " + this.FH.v + " mSharkfin.authType: " + this.FH.authType + " mSharkfin.guid: " + this.FH.r + " mSharkfin.ext1: " + this.FH.s);
            tmsdk.common.utils.d.g("ocean", "[ocean]info: guid|" + this.FH.r);
            pa.a("ocean", "[ocean]info: SdKy: sessionId|" + this.FH.t + "|encodeKey|" + bVar.Ft, null, null);
            tmsdk.common.utils.d.e("ocean", "checkSharkfin() rsaKeyCode: " + bVar.hashCode() + " rsaKey: " + bVar + " mSharkfin.buildno: " + this.FH.u + " mSharkfin.apn: " + this.FH.q + " mSharkfin.netType: " + this.FH.v + " mSharkfin.authType: " + this.FH.authType + " mSharkfin.guid: " + this.FH.r + " mSharkfin.ext1: " + this.FH.s);
        }
        return this.FH;
    }

    private pl<Long, Integer, fs> a(long j, int i, d dVar) {
        os gI = this.FD.gI();
        if (gI != null) {
            gI.a(dVar.hash, i, dVar.c, dVar.d, dVar.e, dVar.f);
        }
        int W = this.FD.gI().W();
        tmsdk.common.utils.d.d("SharkNetwork", "handleIpList() \u6536\u5230ip\u5217\u8868\uff0chash: " + W + " hashSeqNo: " + this.FD.gI().X() + " pushId: " + j);
        if (W == 0) {
            return null;
        }
        this.FD.gI().u(0, 0);
        a aVar = new a();
        aVar.hash = W;
        tmsdk.common.utils.d.d("SharkNetwork", "handleIpList() \u5904\u7406ip\u5217\u8868\u6210\u529f");
        return new pl(Long.valueOf(j), Integer.valueOf(151), aVar);
    }

    private void a(a aVar) {
        this.FR = new li() {
            final /* synthetic */ pb FU;

            {
                this.FU = r1;
            }

            public pl<Long, Integer, fs> a(int i, long j, int i2, fs fsVar) {
                if (fsVar != null) {
                    switch (i2) {
                        case 10150:
                            return this.FU.a(j, i, (d) fsVar);
                        default:
                            return null;
                    }
                }
                tmsdk.common.utils.d.c("SharkNetwork", "onRecvPush() null == push");
                return null;
            }
        };
        aVar.a(0, 10150, new d(), 0, this.FR, false);
    }

    private void a(d dVar) {
        tmsdk.common.utils.d.e("SharkNetwork", "runTimeout() sharkSend.seqNoTag: " + dVar.Gg);
        this.FT.removeMessages(0, dVar);
        if (this.FN.containsKey(Integer.valueOf(dVar.Gg))) {
            jq.ct().a(new AnonymousClass6(this, dVar), "runTimeout");
        }
    }

    private void a(boolean z, d dVar, int i, int i2, ArrayList<bq> arrayList) {
        a(false, z, dVar, i, i2, (ArrayList) arrayList);
    }

    private void a(boolean z, boolean z2, int i) {
        tmsdk.common.utils.d.e("SharkNetwork", "runError() mSharkQueueSendingBySeqNo.size(): " + this.FN.size());
        ArrayList arrayList = new ArrayList();
        synchronized (this.FN) {
            if (z) {
                arrayList.addAll(this.FN.values());
                this.FN.clear();
            } else {
                for (d dVar : this.FN.values()) {
                    if (z2 == dVar.Gd || dVar.gr()) {
                        arrayList.add(dVar);
                    }
                }
                this.FN.values().removeAll(arrayList);
            }
        }
        synchronized (this.FM) {
            arrayList.addAll(this.FM);
            this.FM.clear();
        }
        tmsdk.common.utils.d.e("SharkNetwork", "runError() values.size(): " + arrayList.size());
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            a(z2, (d) it.next(), i, 0, null);
        }
    }

    private void a(boolean z, boolean z2, d dVar, int i, int i2, ArrayList<bq> arrayList) {
        try {
            a(false, false, false, dVar, i, (ArrayList) arrayList);
            if (dVar != null) {
                if (z) {
                    if (dVar.Gj != null && dVar.Gj.size() > 0) {
                        dVar.Gk.a(z2, i, i2, arrayList);
                    }
                }
                cd(dVar.Gg);
                dVar.Gk.a(z2, i, i2, arrayList);
            }
        } catch (Throwable e) {
            tmsdk.common.utils.d.a("SharkNetwork", "runError() callback crash", e);
        }
        this.FC.ar(i);
    }

    private void a(boolean z, boolean z2, d dVar, int i, int i2, bq bqVar) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(bqVar);
        a(z, z2, dVar, i, i2, arrayList);
    }

    private void a(boolean z, boolean z2, boolean z3, d dVar, int i, ArrayList<bq> arrayList) {
        if (dVar != null) {
            int size;
            int i2;
            bm bmVar;
            if (z2 && dVar.Gj != null) {
                size = dVar.Gj.size();
                for (i2 = 0; i2 < size; i2++) {
                    bmVar = (bm) dVar.Gj.get(i2);
                    if (bmVar != null) {
                        pa.a("ocean", "[ocean]info: Used: sessionId|" + (dVar.Gh == null ? "" : dVar.Gh.Fs) + "|encodeKey|" + (dVar.Gh == null ? "" : dVar.Gh.Ft), bmVar, null);
                        if (z) {
                            pa.a("ocean", "[ocean]guid|" + this.FF.c() + "|send|" + (!z3 ? "" : "\u8f6c") + "\u901a\u9053|" + (!dVar.Gd ? "http|" : "tcp|") + "sharkSeqNo|" + dVar.Gg + "|ECmd|" + bmVar.aZ + "|seqNo|" + bmVar.dG + "|refSeqNo|" + bmVar.dH + "|ret|" + i + "|ident|" + bmVar.dI + (bmVar.dL == null ? "" : "|pushId|" + bmVar.dL.dF), bmVar, null);
                        } else {
                            pa.b("ocean", "[ocean]guid|" + this.FF.c() + "|send|" + (!z3 ? "" : "\u8f6c") + "\u901a\u9053|" + (!dVar.Gd ? "http|" : "tcp|") + "sharkSeqNo|" + dVar.Gg + "|ECmd|" + bmVar.aZ + "|seqNo|" + bmVar.dG + "|refSeqNo|" + bmVar.dH + "|ret|" + i + "|ident|" + bmVar.dI + "|size|" + (bmVar.data == null ? 0 : bmVar.data.length) + (bmVar.dL == null ? "" : "|pushId|" + bmVar.dL.dF), bmVar, null);
                        }
                    }
                }
            }
            if (!z2 && arrayList != null && arrayList.size() > 0) {
                size = arrayList.size();
                for (i2 = 0; i2 < size; i2++) {
                    bq bqVar = (bq) arrayList.get(i2);
                    if (bqVar != null) {
                        if (i == 0 && bqVar.dJ == 0 && bqVar.dK == 0) {
                            pa.b("ocean", "[ocean]guid|" + this.FF.c() + "|recv|" + "\u901a\u9053|" + (!dVar.Gd ? "http|" : "tcp|") + "sharkSeqNo|" + dVar.Gg + "|ECmd|" + bqVar.aZ + "|seqNo|" + bqVar.dG + "|refSeqNo|" + bqVar.dH + "|ret|" + i + "|retCode|" + bqVar.dJ + "|dataRetCode|" + bqVar.dK + "|size|" + (bqVar.data == null ? 0 : bqVar.data.length), null, bqVar);
                        } else {
                            pa.c("ocean", "[ocean]guid|" + this.FF.c() + "|recv|" + "\u901a\u9053|" + (!dVar.Gd ? "http|" : "tcp|") + "sharkSeqNo|" + dVar.Gg + "|ECmd|" + bqVar.aZ + "|seqNo|" + bqVar.dG + "|refSeqNo|" + bqVar.dH + "|ret|" + i + "|retCode|" + bqVar.dJ + "|dataRetCode|" + bqVar.dK, null, bqVar);
                        }
                    }
                }
            } else if (!(z2 || dVar == null || dVar.Gj == null || i == 0)) {
                size = dVar.Gj.size();
                for (i2 = 0; i2 < size; i2++) {
                    bmVar = (bm) dVar.Gj.get(i2);
                    if (bmVar != null && bmVar.aZ < 10000) {
                        pa.c("ocean", "[ocean]guid|" + this.FF.c() + "|recv|" + "\u901a\u9053|" + (!dVar.Gd ? "http|" : "tcp|") + "sharkSeqNo|" + dVar.Gg + "|ECmd|" + (bmVar.aZ >= 10000 ? bmVar.aZ : bmVar.aZ + 10000) + "|ret|" + i, bmVar, null);
                    }
                }
            }
        }
    }

    private boolean a(boolean z, br brVar, bq bqVar) {
        if (bqVar == null) {
            return false;
        }
        boolean z2 = bqVar.dH == 0;
        if (z2) {
            pa.b("ocean", "[ocean]guid|" + this.FF.c() + "|push|" + "\u901a\u9053|" + (!z ? "http|" : "tcp|") + "sharkSeqNo|" + brVar.dG + "|ECmd|" + bqVar.aZ + "|seqNo|" + bqVar.dG + "|refSeqNo|" + bqVar.dH + "|ret|" + 0 + "|ident|" + this.FQ.a(z, brVar.dG, bqVar) + (bqVar.dT == null ? "" : "|pushId|" + bqVar.dT.dF), null, bqVar);
        }
        return z2;
    }

    private void b(d dVar) {
        if (dVar == null || dVar.Gj == null || dVar.Gh == null || dVar.Gh.Ft == null) {
            tmsdk.common.utils.d.c("SharkNetwork", "revertClientSashimiData() something null");
            return;
        }
        Iterator it = dVar.Gj.iterator();
        while (it.hasNext()) {
            bm bmVar = (bm) it.next();
            if (!(bmVar == null || bmVar.data == null)) {
                bmVar.data = ok.decrypt(bmVar.data, dVar.Gh.Ft.getBytes());
            }
        }
    }

    private d cd(int i) {
        d dVar;
        tmsdk.common.utils.d.e("SharkNetwork", "removeSendingBySeqNoTag() seqNoTag: " + i);
        synchronized (this.FN) {
            dVar = (d) this.FN.remove(Integer.valueOf(i));
        }
        return dVar;
    }

    private int go() {
        if (!ml.Bc) {
            ml.Bc = false;
            ml.n(this.mContext);
        }
        if ((byte) 3 == ml.Bd) {
            return 1;
        }
        switch (ml.Bf) {
            case SpaceManager.ERROR_CODE_OK /*0*/:
                return 2;
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                return 3;
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                return 4;
            case FileInfo.TYPE_BIGFILE /*3*/:
                return 5;
            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                return 6;
            case UrlCheckType.STEAL_ACCOUNT /*5*/:
                return 7;
            case UrlCheckType.TIPS_CHEAT /*6*/:
                return 8;
            case UrlCheckType.TIPS_DEFAULT /*7*/:
                return 9;
            case RubbishType.SCAN_FLAG_APK /*8*/:
                return 10;
            default:
                return 0;
        }
    }

    private int gp() {
        switch (tmsdk.common.utils.c.ir()) {
            case SpaceManager.ERROR_CODE_OK /*0*/:
                return 0;
            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                return 1;
            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                return 2;
            default:
                return 0;
        }
    }

    protected tmsdkobf.ox.b a(boolean z, d dVar) {
        tmsdkobf.ox.b bVar = null;
        tmsdk.common.utils.d.e("SharkNetwork", "spSend() \u5f00\u59cb\u53d1shark\u5305");
        if (dVar == null) {
            return null;
        }
        bm bmVar;
        if (z) {
            bVar = this.FE.gg();
            if (bVar == null) {
                tmsdk.common.utils.d.c("SharkNetwork", "spSend() RsaKey is null");
            }
            Iterator it = dVar.Gj.iterator();
            while (it.hasNext()) {
                bmVar = (bm) it.next();
                if (!(bmVar == null || bmVar.data == null)) {
                    bmVar.data = ok.encrypt(bmVar.data, bVar.Ft.getBytes());
                }
            }
        }
        fs a = a(dVar, !z, true, bVar, dVar.Gj);
        if (!(dVar == null || dVar.Gj == null || dVar.Gj.size() <= 0)) {
            Iterator it2 = dVar.Gj.iterator();
            while (it2.hasNext()) {
                bmVar = (bm) it2.next();
                if (bmVar != null && bmVar.dH == 0) {
                    dVar.Gf = true;
                }
            }
        }
        byte[] d = ot.d(a);
        tmsdk.common.utils.d.e("SharkNetwork", "spSend() sendData.length: " + d.length);
        synchronized (this.FN) {
            tmsdk.common.utils.d.e("SharkNetwork", "spSend() sharkSend.seqNoTag: " + dVar.Gg);
            this.FN.put(Integer.valueOf(dVar.Gg), dVar);
        }
        this.FT.sendMessageDelayed(Message.obtain(this.FT, 0, dVar), 180000);
        this.FD.a(dVar, d, new tmsdkobf.ph.a() {
            final /* synthetic */ pb FU;

            {
                this.FU = r1;
            }

            public void a(boolean z, int i, d dVar) {
                this.FU.a(false, true, z, dVar, i, null);
            }
        });
        return bVar;
    }

    public void a(int i, int i2, int i3) {
        if (i2 > 0) {
            if (this.FI == null) {
                synchronized (pb.class) {
                    if (this.FI == null) {
                        this.FI = new SparseArray();
                    }
                }
            }
            qi qiVar = new qi("network_control_" + i, (long) (i2 * CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY), i3);
            synchronized (this.FI) {
                this.FI.append(i, qiVar);
                tmsdk.common.utils.d.e("SharkNetwork", "handleNetworkControl : cmdid|" + i + "|timeSpan|" + i2 + "|maxTimes|" + i3);
            }
        }
    }

    public void a(long j, boolean z, ArrayList<bm> arrayList, b bVar) {
        a(false, false, j, (ArrayList) arrayList, bVar);
        if (z) {
            this.FF.G(false);
        }
    }

    protected void a(ArrayList<bm> arrayList, b bVar) {
        if (bVar == null) {
            throw new NullPointerException();
        } else if (arrayList.size() > 0) {
            this.FK = new d(true, false, false, 0, arrayList, bVar);
            this.yB.sendEmptyMessage(0);
        } else {
            tmsdk.common.utils.d.c("SharkNetwork", "asyncSendSharkRsa() empty list");
        }
    }

    public void a(boolean z, boolean z2, long j, ArrayList<bm> arrayList, b bVar) {
        if (bVar == null) {
            throw new NullPointerException();
        } else if (arrayList.size() > 0) {
            d dVar = new d(false, z, z2, j, arrayList, bVar);
            a(true, true, false, dVar, 0, null);
            synchronized (this.FM) {
                this.FM.add(dVar);
                tmsdk.common.utils.d.d("SharkNetwork", "asyncSendShark() mSharkQueueWaiting.size(): " + this.FM.size());
            }
            this.yB.sendEmptyMessage(1);
        } else {
            tmsdk.common.utils.d.c("SharkNetwork", "asyncSendShark() empty list");
        }
    }

    protected void b(ArrayList<bm> arrayList, b bVar) {
        if (bVar == null) {
            throw new NullPointerException();
        } else if (arrayList.size() > 0) {
            this.FL = new d(true, false, false, 0, arrayList, bVar);
            this.yB.sendEmptyMessage(0);
        } else {
            tmsdk.common.utils.d.c("SharkNetwork", "asyncSendSharkGuid() empty list");
        }
    }

    public String c() {
        return this.FF.c();
    }

    public void fX() {
        os gI = this.FD.gI();
        if (gI != null) {
            gI.fX();
        }
    }

    protected tmsdkobf.ox.b gg() {
        return this.FE.gg();
    }

    public pk gk() {
        return this.FD.gk();
    }

    public void gl() {
        this.FF.G(true);
    }

    protected oq gm() {
        return this.FC;
    }

    public void gn() {
        boolean z = false;
        String av = this.FC.av();
        String ax = this.FC.ax();
        if (ax != null) {
            if (TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_BUILD) == this.FC.at() && TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_PRODUCT) == this.FC.au() && ax.equals(av)) {
                this.FF.G(z);
            }
        } else if (av != null) {
            this.FF.G(z);
        }
        z = true;
        this.FF.G(z);
    }

    public void gq() {
        if (this.yB != null) {
            tmsdk.common.utils.d.e("SharkNetwork", "onGuidInfoChange()");
            this.yB.removeMessages(4);
            this.yB.sendEmptyMessageDelayed(4, 15000);
        }
    }

    public void n(boolean z) {
        this.FD.n(z);
    }

    public void refresh() {
        tmsdk.common.utils.d.d("SharkNetwork", "refresh()");
        this.FF.fM();
    }
}
