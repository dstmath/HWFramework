package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.c;
import tmsdk.common.utils.d;
import tmsdk.common.utils.h;
import tmsdk.common.utils.h.a;
import tmsdk.common.utils.j;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
public class fz extends oq {
    private static ArrayList<Pair<Integer, Long>> ob;
    private static ArrayList<Pair<Integer, Long>> oc;
    private final String TAG;
    private fx nX;
    private boolean nY;
    private volatile String nZ;
    private volatile String oa;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.fz.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.fz.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.fz.<clinit>():void");
    }

    public fz() {
        this.TAG = "SharkOutlet";
        this.nX = fx.O();
        this.nY = true;
    }

    private void a(ArrayList<Pair<Integer, Long>> arrayList, long j, String str, boolean z) {
        Object obj = 1;
        if ((j <= 0 ? 1 : null) == null) {
            if (j < 60000) {
                obj = null;
            }
            if (obj == null && arrayList.size() <= 4) {
                synchronized (arrayList) {
                    arrayList.add(new Pair(Integer.valueOf(ml.Bg), Long.valueOf(j)));
                    if (4 == arrayList.size()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        for (int i = 0; i < arrayList.size(); i++) {
                            Pair pair = (Pair) arrayList.get(i);
                            if (pair != null) {
                                stringBuilder.append(pair.first);
                                stringBuilder.append(",");
                                stringBuilder.append(pair.second);
                                if (arrayList.size() - 1 != i) {
                                    stringBuilder.append("|");
                                }
                            }
                        }
                        String stringBuilder2 = stringBuilder.toString();
                        if (z) {
                            this.nX.aw(stringBuilder2);
                        } else {
                            this.nX.av(stringBuilder2);
                        }
                        d.d("SharkOutlet", "onCtTimeMillis() mConnectTimeMillisAll: " + stringBuilder2);
                    }
                }
            }
        }
    }

    private ArrayList<String> aB(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        d.d("SharkOutlet", "getIpListFromStr() ipListStr: " + str);
        String[] split = str.split("\\|");
        if (split == null || split.length <= 0) {
            return null;
        }
        Object arrayList = new ArrayList();
        Collections.addAll(arrayList, split);
        return arrayList;
    }

    private int getInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

    private String m(ArrayList<String> arrayList) {
        if (arrayList == null) {
            return null;
        }
        if (arrayList.size() <= 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            String str = (String) arrayList.get(i);
            if (!TextUtils.isEmpty(str)) {
                stringBuilder.append(str);
                if (size - 1 != i) {
                    stringBuilder.append("|");
                }
            }
        }
        return stringBuilder.toString();
    }

    private void n(ArrayList<String> arrayList) {
        if (arrayList != null && arrayList.size() > 0) {
            int size = arrayList.size();
            for (int i = 0; i < size; i++) {
                if (arrayList.get(i) != null) {
                    d.d("SharkOutlet", "printIpList() ipList[" + i + "]: " + ((String) arrayList.get(i)));
                }
            }
        }
    }

    public WeakReference<ll> a(int i, int i2, int i3, long j, long j2, int i4, fs fsVar, byte[] bArr, fs fsVar2, int i5, lg lgVar, lh lhVar, long j3, long j4) {
        return hr.by().b(i, i2, i3, j, j2, i4, fsVar, bArr, fsVar2, i5, lgVar, lhVar, j3, j4);
    }

    public void a(long j, ArrayList<String> arrayList, ArrayList<String> arrayList2, ArrayList<String> arrayList3) {
        d.d("SharkOutlet", "onSaveIpListInfo() validperiodLocalMillis: " + j);
        this.nX.g(j);
        String m = m(arrayList);
        if (!(m == null || m.equals(""))) {
            this.nX.as(m);
            d.d("SharkOutlet", "onSaveIpListInfo() cmStr: " + m);
        }
        m = m(arrayList2);
        if (!(m == null || m.equals(""))) {
            this.nX.at(m);
            d.d("SharkOutlet", "onSaveIpListInfo() unStr: " + m);
        }
        m = m(arrayList3);
        if (m != null && !m.equals("")) {
            this.nX.au(m);
            d.d("SharkOutlet", "onSaveIpListInfo() ctStr: " + m);
        }
    }

    public void a(AtomicLong atomicLong, AtomicReference<ArrayList<String>> atomicReference, AtomicReference<ArrayList<String>> atomicReference2, AtomicReference<ArrayList<String>> atomicReference3) {
        long Y = this.nX.Y();
        d.d("SharkOutlet", "onGetIpList() validperiodLocalMillis: " + Y);
        if (atomicLong != null) {
            atomicLong.set(Y);
        }
        Object ah = this.nX.ah();
        Object ai = this.nX.ai();
        Object aj = this.nX.aj();
        if (!TextUtils.isEmpty(ah)) {
            atomicReference.set(aB(ah));
        }
        if (!TextUtils.isEmpty(ai)) {
            atomicReference2.set(aB(ai));
        }
        if (!TextUtils.isEmpty(aj)) {
            atomicReference3.set(aB(aj));
        }
        n((ArrayList) atomicReference.get());
        n((ArrayList) atomicReference2.get());
        n((ArrayList) atomicReference3.get());
    }

    public int aA() {
        d.d("SharkOutlet", "onGetIntervalSecond() intervalSecond: " + this.nX.af());
        return 0;
    }

    public void aA(String str) {
    }

    public ArrayList<Integer> aB() {
        String ag = this.nX.ag();
        if (TextUtils.isEmpty(ag)) {
            d.d("SharkOutlet", "onGetPortList() portList: null");
            return null;
        }
        d.d("SharkOutlet", "onGetPortList() portList: " + ag);
        String[] split = ag.split("\\|");
        if (split.length <= 0) {
            return null;
        }
        ArrayList<Integer> arrayList = new ArrayList();
        for (int i = 0; i < split.length; i++) {
            Object obj = split[i];
            if (!TextUtils.isEmpty(obj)) {
                int i2 = getInt(obj);
                d.d("SharkOutlet", "onGetPortList() port[" + i + "]: " + i2);
                arrayList.add(Integer.valueOf(i2));
            }
        }
        return arrayList;
    }

    public ArrayList<c> aC() {
        String R = this.nX.R();
        if (R == null) {
            return null;
        }
        fq fqVar = new fq(mo.cw(R));
        fqVar.ae("UTF-8");
        ArrayList arrayList = new ArrayList();
        arrayList.add(new c());
        return (ArrayList) fqVar.b(arrayList, 0, false);
    }

    public int aD() {
        return this.nX.Q();
    }

    public long aE() {
        return this.nX.ak();
    }

    public boolean aF() {
        return this.nX.ae();
    }

    public boolean an() {
        return this.nY;
    }

    public int ao() {
        int W = this.nX.W();
        d.d("SharkOutlet", "onGetHash() hash: " + W);
        return W;
    }

    public int ap() {
        int X = this.nX.X();
        d.d("SharkOutlet", "onGetHashSeqNo() hashSeqNo: " + X);
        return X;
    }

    public void ap(int i) {
        d.d("SharkOutlet", "onSaveIntervalSecond() intervalSecond: " + i);
        this.nX.am(i);
    }

    public String aq() {
        String S = this.nX.S();
        d.d("SharkOutlet", "onGetEncodeKey() encodeKey: " + S);
        return S;
    }

    public void aq(int i) {
        this.nX.aj(i);
    }

    public String ar() {
        String T = this.nX.T();
        d.d("SharkOutlet", "onGetSessionId() sessionId: " + T);
        return T;
    }

    public void ar(int i) {
    }

    public String as() {
        String U = this.nX.U();
        d.d("SharkOutlet", "onGetGuidFromPhone() guid: " + U);
        return U;
    }

    public void as(int i) {
    }

    public int at() {
        return this.nX.Z();
    }

    public void at(int i) {
    }

    public int au() {
        return this.nX.aa();
    }

    public void au(int i) {
    }

    public String av() {
        return this.nX.ab();
    }

    public void av(int i) {
    }

    public bj aw() {
        d.d("SharkOutlet", "onGetInfoSavedOfGuid()");
        return this.nX.ac();
    }

    public void aw(int i) {
    }

    public String ax() {
        return h.D(TMSDKContext.getApplicaionContext());
    }

    public void ax(String str) {
        d.d("SharkOutlet", "onSaveEncodeKey() encodeKey: " + str);
        this.nX.ak(str);
    }

    public bj ay() {
        Context applicaionContext = TMSDKContext.getApplicaionContext();
        String[] w = c.w(applicaionContext);
        np fv = ((ns) ManagerCreatorC.getManager(ns.class)).fv();
        long j = 0;
        if (fv != null) {
            j = fv.fu();
        }
        a aVar = new a();
        h.a(aVar);
        long j2 = aVar.Le;
        aVar = new a();
        h.b(aVar);
        long j3 = aVar.Le;
        String E = h.E(applicaionContext);
        int i = 1;
        String str = "";
        if (jq.cx() != null) {
            str = jq.cx().getIMSI(1);
            i = 2;
        }
        String str2 = "";
        str2 = jq.cx() == null ? h.D(applicaionContext) : jq.cx().getIMSI(0);
        d.e("TrafficCorrection", "SharkOutlet::onGetRealInfoOfGuid-imsi:[" + str2 + "]imsi_2:[" + str + "]");
        int H = h.H(applicaionContext);
        int I = h.I(applicaionContext);
        if (H < I) {
            int i2 = H;
            H = I;
            I = i2;
        }
        bj bjVar = new bj();
        bjVar.cC = h.C(applicaionContext);
        bjVar.imsi = str2;
        bjVar.dk = str;
        bjVar.cD = E == null ? "" : E;
        bjVar.cE = "0";
        bjVar.cF = "0";
        bjVar.product = TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_PRODUCT);
        bjVar.cG = l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_LC));
        bjVar.u = TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_BUILD);
        bjVar.cH = l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_CHANNEL));
        bjVar.cI = 2;
        bjVar.cJ = TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_SUB_PLATFORM);
        bjVar.cK = c.y(applicaionContext);
        try {
            bjVar.cL = TMSDKContext.getApplicaionContext().getPackageName();
        } catch (Throwable th) {
            d.c("SharkOutlet", th);
        }
        bjVar.cM = l.dk(h.iC());
        bjVar.cN = j.iM();
        bjVar.cO = l.dk(h.G(applicaionContext));
        bjVar.cP = (short) 2052;
        bjVar.cQ = i;
        bjVar.cR = w[2];
        bjVar.dt = h.dg("ro.product.cpu.abi2");
        bjVar.cS = c.ip();
        bjVar.cT = c.is();
        bjVar.cU = H + "*" + I;
        bjVar.cV = j;
        bjVar.cW = c.it();
        bjVar.cX = j2;
        bjVar.dy = j3;
        bjVar.cY = l.dk(h.iH());
        bjVar.cZ = l.dk(h.iF());
        bjVar.da = l.dk(h.iG());
        bjVar.version = "";
        bjVar.do = 1;
        bjVar.dp = l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_PKGKEY));
        bjVar.dd = h.iL();
        bjVar.dg = 0;
        bjVar.dh = 0;
        bjVar.dq = h.iI();
        bjVar.dr = h.iJ();
        bjVar.ds = h.dg("ro.build.product");
        bjVar.du = h.dg("ro.build.fingerprint");
        bjVar.dv = h.dg("ro.product.locale.language");
        bjVar.dw = h.dg("ro.product.locale.region");
        bjVar.dx = h.getRadioVersion();
        bjVar.de = h.dg("ro.board.platform");
        bjVar.dz = h.dg("ro.mediatek.platform");
        bjVar.df = h.dg("ro.sf.lcd_density");
        bjVar.db = h.dg("ro.product.name");
        bjVar.dc = h.dg("ro.build.version.release");
        bjVar.dA = h.iK();
        bjVar.di = false;
        bjVar.dB = TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_APP_BUILD_TYPE);
        return bjVar;
    }

    public void ay(String str) {
        d.d("SharkOutlet", "onSaveSessionId() sessionId: " + str);
        this.nX.al(str);
    }

    public long az() {
        long V = this.nX.V();
        d.d("SharkOutlet", "onGetGuidUpdateCheckTimeMillis() tm: " + V);
        return V;
    }

    public void az(String str) {
        if (str != null) {
            d.d("SharkOutlet", "onSaveGuidToPhone() guid: " + str);
            this.nX.am(str);
        }
    }

    public void b(int i, int i2) {
        d.d("SharkOutlet", "onSaveHash() hash: " + i + " hashSeqNo: " + i2);
        this.nX.ak(i);
        this.nX.al(i2);
    }

    public void b(bj bjVar) {
        d.d("SharkOutlet", "onSaveInfoOfGuid()");
        this.nX.a(bjVar);
    }

    public void h(long j) {
        d.d("SharkOutlet", "onSaveGuidUpdateCheckTimeMillis() timeMillis: " + j);
        this.nX.f(j);
    }

    public void i(long j) {
        a(ob, j, this.nZ, false);
    }

    public void j(long j) {
        a(oc, j, this.oa, true);
    }

    public void k(ArrayList<Integer> arrayList) {
        if (arrayList != null) {
            int size = arrayList.size();
            if (size > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    stringBuilder.append(arrayList.get(i));
                    if (size - 1 != i) {
                        stringBuilder.append("|");
                    }
                }
                d.d("SharkOutlet", "onSavePortList() portList: " + stringBuilder.toString());
                this.nX.ar(stringBuilder.toString());
            }
        }
    }

    public void l(ArrayList<c> arrayList) {
        if (arrayList != null) {
            fr frVar = new fr();
            frVar.ae("UTF-8");
            frVar.a((Collection) arrayList, 0);
            this.nX.aj(mo.bytesToHexString(frVar.toByteArray()));
        }
    }

    public void o(boolean z) {
        this.nX.n(z);
    }
}
