package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.BasicCloudField;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.ErrorCode;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.exception.NetWorkException;
import tmsdk.common.exception.NetworkOnMainThreadException;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.c;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;
import tmsdk.common.utils.h;
import tmsdk.common.utils.j;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
class qr implements qo {
    private static final String Kf = null;
    private static String Kg;
    private static String TAG;
    private final String Kh;
    private final String Ki;
    private String Kj;
    private dn Kk;
    private dw Kl;
    private ev Km;
    private dz Kn;
    private da Ko;
    private dk Kp;
    private dy Kq;
    volatile boolean Kr;
    private Object Ks;
    private volatile long Kt;
    private volatile long Ku;
    private boolean Kv;
    private Context mContext;
    private String mImei;
    private nc yq;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.qr.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.qr.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.qr.<clinit>():void");
    }

    public qr(Context context) {
        this.Kh = "tid";
        this.Ki = "mdtids";
        this.mImei = null;
        this.Kj = null;
        this.Ks = new Object();
        this.Kt = 0;
        this.Ku = 0;
        this.Kv = false;
        this.mContext = context;
        this.yq = new nc("wup");
        if (Kg == null) {
            Kg = this.yq.getString("guid", null);
        }
        this.mImei = l.dk(h.C(this.mContext));
        this.Kj = l.dk(h.E(this.mContext));
        mu.fa();
        hQ();
    }

    private void a(ec ecVar) {
        d.d("CMD", "tryDirectHandleTips: " + ecVar.toString());
        d.d("CMD", "XX run http session, handle cloud instr");
        Intent intent = new Intent();
        intent.setAction("action_direct_verify");
        intent.putExtra("data", ecVar);
        this.mContext.sendBroadcast(intent);
    }

    private void a(fj fjVar, int i, String str, String str2, HashMap<String, Object> hashMap) {
        fjVar.ae(i);
        fjVar.aa(str);
        fjVar.ab(str2);
        fjVar.Z("UTF-8");
        if (hashMap != null && hashMap.size() > 0) {
            for (Entry entry : hashMap.entrySet()) {
                fjVar.put((String) entry.getKey(), entry.getValue());
            }
        }
    }

    private void hQ() {
        this.Kt = this.yq.getLong("last_ft", 0);
        this.Ku = this.yq.getLong("cts_td", 0);
    }

    private da hW() {
        if (this.Ko != null) {
            this.Ko.m(Kg);
            this.Ko.e(this.mImei);
            this.Ko.g(this.Kj);
        } else {
            this.Ko = new da();
            this.Ko.e(this.mImei);
            this.Ko.f(l.dk(h.D(this.mContext)));
            this.Ko.g(this.Kj);
            this.Ko.h(l.dk(h.F(this.mContext)));
            this.Ko.i(l.dk(h.iB()));
            this.Ko.j(j.iM());
            this.Ko.j(l.dk(h.iC()));
            this.Ko.k(l.dk(h.iD()));
            this.Ko.l(l.dk(c.x(this.mContext)));
            this.Ko.m(Kg);
        }
        return this.Ko;
    }

    private int hX() {
        da hW = hW();
        AtomicReference atomicReference = new AtomicReference();
        int a = ((qt) ManagerCreatorC.getManager(qt.class)).a(hW, atomicReference);
        if (a == 0) {
            dh dhVar = (dh) atomicReference.get();
            if (dhVar != null) {
                Kg = dhVar.c();
                if (Kg == null || Kg.equals("")) {
                    return -2001;
                }
            }
        }
        return a;
    }

    private boolean hY() {
        String dk = l.dk(h.C(this.mContext));
        String dk2 = l.dk(h.E(this.mContext));
        this.mImei = this.yq.getString(BasicCloudField.PHONE_IMEI, dk);
        this.Kj = this.yq.getString("mac", dk2);
        if (dk.equals(this.mImei) && dk2.equals(this.Kj)) {
            return false;
        }
        this.mImei = dk;
        this.Kj = dk2;
        return true;
    }

    private void y(long j) {
        this.Kt = j;
        this.yq.a("last_ft", this.Kt, true);
    }

    private void z(long j) {
        this.Ku = j;
        this.yq.a("cts_td", this.Ku, true);
    }

    int a(List<qs> list, boolean z, String str) {
        NetWorkException e;
        IllegalArgumentException e2;
        NetworkOnMainThreadException e3;
        Throwable th;
        boolean fb = fb();
        d.e(TAG, " runHttpSession() couldNotConnect:" + fb);
        if (fb) {
            return -64;
        }
        int i = ErrorCode.ERR_WUP;
        if (list == null) {
            return -6057;
        }
        int size = list.size();
        if (size == 0) {
            return -6057;
        }
        if (!z) {
            i = hZ();
            if (i != 0) {
                return i - 60;
            }
        }
        int i2 = i;
        List<byte[]> arrayList = new ArrayList(size);
        int i3 = 0;
        for (qs qsVar : list) {
            fj fjVar = qsVar.Kz;
            fj fjVar2 = qsVar.KA;
            a(fjVar, qsVar.Kw, qsVar.Kx.Kd, qsVar.Kx.Ke, qsVar.Ky);
            Object m = fjVar.m();
            arrayList.add(m);
            i3 = m.length + i3;
        }
        byte[] bArr = new byte[i3];
        int i4 = 0;
        for (byte[] bArr2 : arrayList) {
            byte[] bArr22;
            System.arraycopy(bArr22, 0, bArr, i4, bArr22.length);
            i4 = bArr22.length + i4;
        }
        mu muVar = null;
        mu cA;
        try {
            bArr22 = TccCryptor.encrypt(bArr, null);
            cA = mu.cA(str);
            try {
                cA.setRequestMethod("POST");
                cA.setPostData(bArr22);
                cA.fc();
                AtomicReference atomicReference = new AtomicReference();
                i2 = cA.a(false, atomicReference);
                if (i2 == 0) {
                    List<fj> arrayList2 = new ArrayList(size + 1);
                    bArr22 = (byte[]) atomicReference.get();
                    if (bArr22 != null) {
                        if (bArr22.length > 0) {
                            Object decrypt = TccCryptor.decrypt(bArr22, null);
                            int length = decrypt.length;
                            i = 0;
                            while (i + 4 < length) {
                                int i5 = ((((decrypt[i] & 255) << 24) | ((decrypt[i + 1] & 255) << 16)) | ((decrypt[i + 2] & 255) << 8)) | (decrypt[i + 3] & 255);
                                Object obj = new byte[i5];
                                System.arraycopy(decrypt, i, obj, 0, i5);
                                fj fjVar3 = new fj(true);
                                fjVar3.Z("UTF-8");
                                fjVar3.b(obj);
                                arrayList2.add(fjVar3);
                                i += i5;
                            }
                            if (arrayList2.size() > 0) {
                                Collection arrayList3 = new ArrayList(size + 1);
                                for (fj fjVar4 : arrayList2) {
                                    i3 = fjVar4.q();
                                    for (qs qsVar2 : list) {
                                        if (qsVar2.Kz.q() == i3) {
                                            qsVar2.KA = fjVar4;
                                            arrayList3.add(fjVar4);
                                            break;
                                        }
                                    }
                                }
                                arrayList2.removeAll(arrayList3);
                                d.d(TAG, " responseList.size: " + arrayList2.size());
                            }
                        }
                    }
                    i2 = 0;
                    if (cA != null) {
                        cA.close();
                    }
                    if (!this.Kr) {
                        TMSDKContext.reportChannelInfo();
                    }
                    return i2;
                }
                if (cA != null) {
                    cA.close();
                }
                if (!this.Kr && i2 == 0) {
                    TMSDKContext.reportChannelInfo();
                }
                return i2;
            } catch (NetWorkException e4) {
                e = e4;
                muVar = cA;
            } catch (IllegalArgumentException e5) {
                e2 = e5;
            } catch (NetworkOnMainThreadException e6) {
                e3 = e6;
            } catch (Throwable th2) {
                th = th2;
            }
        } catch (NetWorkException e7) {
            e = e7;
            try {
                i2 = e.getErrCode();
                d.c(TAG, "NetWorkException:" + e.getMessage());
                e.printStackTrace();
                if (muVar != null) {
                    muVar.close();
                }
                if (!this.Kr && i2 == 0) {
                    TMSDKContext.reportChannelInfo();
                }
                return i2;
            } catch (Throwable th3) {
                th = th3;
                cA = muVar;
                if (cA != null) {
                    cA.close();
                }
                if (!this.Kr && r6 == 0) {
                    TMSDKContext.reportChannelInfo();
                }
                throw th;
            }
        } catch (IllegalArgumentException e8) {
            e2 = e8;
            cA = null;
            i2 = -6057;
            try {
                d.c(TAG, "wup agrs error:" + e2.getMessage());
                e2.printStackTrace();
                if (cA != null) {
                    cA.close();
                }
                if (this.Kr) {
                }
                return i2;
            } catch (Throwable th4) {
                th = th4;
            }
        } catch (NetworkOnMainThreadException e9) {
            e3 = e9;
            cA = null;
            throw e3;
        } catch (Throwable th5) {
            th = th5;
            cA = null;
            if (cA != null) {
                cA.close();
            }
            TMSDKContext.reportChannelInfo();
            throw th;
        }
    }

    public int a(qs qsVar) {
        return a(qsVar, Kf);
    }

    public int a(qs qsVar, String str) {
        return a(qsVar, false, str);
    }

    public int a(qs qsVar, boolean z) {
        return a(qsVar, z, Kf);
    }

    int a(qs qsVar, boolean z, String str) {
        mu muVar;
        Throwable th;
        d.e(TAG, " runHttpSession() url:" + str);
        boolean fb = fb();
        d.e(TAG, " runHttpSession() couldNotConnect:" + fb);
        if (fb) {
            return -64;
        }
        int i = ErrorCode.ERR_WUP;
        if (qsVar == null) {
            return -6057;
        }
        byte[] bArr;
        if (!z) {
            i = hZ();
            if (i != 0) {
                return i - 60;
            }
        }
        int i2 = i;
        fj fjVar = qsVar.Kz;
        fj fjVar2 = qsVar.KA;
        a(fjVar, qsVar.Kw, qsVar.Kx.Kd, qsVar.Kx.Ke, qsVar.Ky);
        if (qsVar.Kx != null && qsVar.Kx.Ke == null) {
        }
        int q = fjVar.q();
        fj fjVar3 = null;
        if (q != 999 && hR()) {
            qq qqVar = (qq) qp.Kc.get(999);
            HashMap hashMap = new HashMap(3);
            hashMap.put("phonetype", hS());
            hashMap.put("userinfo", hU());
            hashMap.put("reqinfo", ia());
            fjVar3 = new fj(true);
            a(fjVar3, 999, qqVar.Kd, qqVar.Ke, hashMap);
            d.d("Chord", "WupSessionHelperImpl runHttpSession() getMainTips");
        }
        if (fjVar3 != null) {
            try {
                Object m = fjVar.m();
                Object m2 = fjVar3.m();
                bArr = new byte[(m.length + m2.length)];
                System.arraycopy(m, 0, bArr, 0, m.length);
                System.arraycopy(m2, 0, bArr, m.length, m2.length);
            } catch (NetWorkException e) {
                e = e;
                muVar = null;
                try {
                    NetWorkException e2;
                    i2 = e2.getErrCode();
                    d.c(TAG, "NetWorkException:" + e2.getMessage());
                    e2.printStackTrace();
                    if (muVar != null) {
                        muVar.close();
                    }
                    if (!this.Kr && i2 == 0) {
                        TMSDKContext.reportChannelInfo();
                    }
                    return i2;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (IllegalArgumentException e3) {
                IllegalArgumentException e4 = e3;
                muVar = null;
                i2 = -6057;
                d.c(TAG, "wup agrs error:" + e4.getMessage());
                e4.printStackTrace();
                if (muVar != null) {
                    muVar.close();
                }
                if (this.Kr) {
                }
                return i2;
            } catch (NetworkOnMainThreadException e5) {
                NetworkOnMainThreadException e6 = e5;
                muVar = null;
                throw e6;
            } catch (Throwable th3) {
                th = th3;
                muVar = null;
                if (muVar != null) {
                    muVar.close();
                }
                if (!this.Kr && r8 == 0) {
                    TMSDKContext.reportChannelInfo();
                }
                throw th;
            }
        }
        bArr = fjVar.m();
        bArr = TccCryptor.encrypt(bArr, null);
        muVar = mu.e(str, qsVar.KB);
        try {
            muVar.setRequestMethod("POST");
            muVar.setPostData(bArr);
            muVar.fc();
            AtomicReference atomicReference = new AtomicReference();
            i2 = muVar.a(false, atomicReference);
            if (i2 == 0) {
                bArr = (byte[]) atomicReference.get();
                if (bArr != null) {
                    if (bArr.length > 0) {
                        Object decrypt = TccCryptor.decrypt(bArr, null);
                        int i3 = ((((decrypt[0] & 255) << 24) | ((decrypt[1] & 255) << 16)) | ((decrypt[2] & 255) << 8)) | (decrypt[3] & 255);
                        if (i3 == decrypt.length) {
                            fjVar2.b(decrypt);
                        } else if (i3 > 0 && decrypt.length > i3 + 4) {
                            int i4 = ((((decrypt[i3] & 255) << 24) | ((decrypt[i3 + 1] & 255) << 16)) | ((decrypt[i3 + 2] & 255) << 8)) | (decrypt[i3 + 3] & 255);
                            if (i3 + i4 == decrypt.length) {
                                Object obj = new byte[i3];
                                Object obj2 = new byte[i4];
                                System.arraycopy(decrypt, 0, obj, 0, i3);
                                System.arraycopy(decrypt, i3, obj2, 0, i4);
                                fj fjVar4 = new fj(true);
                                fjVar4.Z("UTF-8");
                                fjVar4.b(obj2);
                                fjVar2.b(obj);
                                if (fjVar4.q() == q) {
                                    fj fjVar5 = fjVar2;
                                    fjVar2 = fjVar4;
                                    fjVar4 = fjVar5;
                                }
                                qsVar.KA = fjVar2;
                                ec ecVar = (ec) fjVar4.a("cmdinfo", (Object) new ec());
                                if (ecVar != null) {
                                    cX(ecVar.f());
                                } else {
                                    ecVar = new ec();
                                    ecVar.N("");
                                }
                                a(ecVar);
                            }
                        }
                    }
                }
                i2 = 0;
                if (muVar != null) {
                    muVar.close();
                }
                if (!this.Kr) {
                    TMSDKContext.reportChannelInfo();
                }
                return i2;
            }
            if (muVar != null) {
                muVar.close();
            }
            if (!this.Kr && i2 == 0) {
                TMSDKContext.reportChannelInfo();
            }
            return i2;
        } catch (NetWorkException e7) {
            e2 = e7;
            i2 = e2.getErrCode();
            d.c(TAG, "NetWorkException:" + e2.getMessage());
            e2.printStackTrace();
            if (muVar != null) {
                muVar.close();
            }
            TMSDKContext.reportChannelInfo();
            return i2;
        } catch (IllegalArgumentException e8) {
            e4 = e8;
            i2 = -6057;
            d.c(TAG, "wup agrs error:" + e4.getMessage());
            e4.printStackTrace();
            if (muVar != null) {
                muVar.close();
            }
            if (this.Kr) {
            }
            return i2;
        } catch (NetworkOnMainThreadException e9) {
            e6 = e9;
            throw e6;
        } catch (Throwable th4) {
            th = th4;
            i2 = ErrorCode.ERR_WUP;
            d.c(TAG, "wup error:" + th.getMessage());
            th.printStackTrace();
            if (muVar != null) {
                muVar.close();
            }
            if (this.Kr) {
            }
            return i2;
        }
    }

    public Object a(fj fjVar, String str, Object obj) {
        if (str == null) {
            str = "";
        }
        return fjVar.a(str, obj);
    }

    public int b(List<qs> list, String str) {
        return a((List) list, false, str);
    }

    public void cX(String str) {
        d.d("Chord", "setNewTipsId() newTipsId:" + str);
        this.yq.a("tid", str, true);
    }

    public ev cy(int i) {
        d.e("QScannerManagerV2", "getUserInfo-language:[" + i + "]");
        ev hU = hU();
        if (i == 1) {
            d.e("QScannerManagerV2", "QQPIM.ELanguage.ELANG_CHS");
            hU.language = 1;
        } else if (i == 2) {
            d.e("QScannerManagerV2", "QQPIM.ELanguage.ELANG_ENG");
            hU.language = 2;
        }
        return hU;
    }

    public boolean fb() {
        return mu.fb();
    }

    public dy hP() {
        int i = 0;
        if (this.Kq != null) {
            this.Kq.e(this.mImei);
        } else {
            this.Kq = new dy();
            this.Kq.t(Kg);
            this.Kq.C(l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_LC)));
            this.Kq.setName(dc.k(TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_PRODUCT)).toString());
            this.Kq.r(l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_SOFTVERSION)));
            this.Kq.e(this.mImei);
            this.Kq.f(l.dk(h.D(this.mContext)));
            this.Kq.setType(2);
            this.Kq.F(l.dk(j.iN()));
            this.Kq.G(l.dk(h.iC()));
            int H = h.H(this.mContext);
            this.Kq.H("screen=" + H + "*" + h.I(this.mContext));
            this.Kq.o(TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_SUB_PLATFORM));
            this.Kq.I(l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_CHANNEL)));
            dy dyVar = this.Kq;
            if (c.y(this.mContext)) {
                i = 1;
            }
            dyVar.f(i);
        }
        pd pdVar = (pd) ManagerCreatorC.getManager(pd.class);
        if (pdVar != null) {
            this.Kq.M(pdVar.c());
        }
        d.d(TAG, "getSUserInfo() new guid: " + this.Kq.jh);
        return this.Kq;
    }

    public boolean hR() {
        synchronized (this.Ks) {
            if (this.Kv) {
                long currentTimeMillis = System.currentTimeMillis();
                boolean a = mp.a(new Date(currentTimeMillis), new Date(this.Kt));
                d.e("Chord", "couldFetchCloud() nowIsTheDayOfLastFetch: " + a);
                if (a) {
                    if (!(this.Ku < 72)) {
                        d.d("Chord", "couldFetchCloud() couldFetchCloud: false");
                        return false;
                    }
                }
                z(0);
                long j = currentTimeMillis - this.Kt;
                if (!(j <= 0)) {
                    if (j <= 1200000) {
                        a = false;
                        d.e("Chord", "couldFetchCloud() moreThanBlank: " + a);
                        if (a) {
                            d.d("Chord", "couldFetchCloud() couldFetchCloud: false");
                            return false;
                        }
                        y(currentTimeMillis);
                        z(this.Ku + 1);
                        d.d("Chord", "couldFetchCloud() couldFetchCloud: true");
                        return true;
                    }
                }
                a = true;
                d.e("Chord", "couldFetchCloud() moreThanBlank: " + a);
                if (a) {
                    d.d("Chord", "couldFetchCloud() couldFetchCloud: false");
                    return false;
                }
                y(currentTimeMillis);
                z(this.Ku + 1);
                d.d("Chord", "couldFetchCloud() couldFetchCloud: true");
                return true;
            }
            d.d("Chord", "couldFetchCloud() mIsCloudReady: false");
            return false;
        }
    }

    public dn hS() {
        if (this.Kk == null) {
            this.Kk = new dn();
            this.Kk.n(2);
            this.Kk.o(TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_SUB_PLATFORM));
        }
        return this.Kk;
    }

    public dw hT() {
        if (this.Kl == null) {
            this.Kl = new dw();
            this.Kl.n(2);
        }
        return this.Kl;
    }

    public ev hU() {
        int i = 1;
        ev evVar;
        if (this.Km != null) {
            this.Km.r = Kg;
            this.Km.cC = this.mImei;
            this.Km.lg = c.ir();
            evVar = this.Km;
            if (f.iw() == cz.gD) {
                i = 2;
            }
            evVar.le = i;
        } else {
            this.Km = new ev();
            this.Km.cC = this.mImei;
            this.Km.cG = l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_LC));
            this.Km.cH = l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_CHANNEL));
            this.Km.cM = l.dk(h.iC());
            this.Km.product = TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_PRODUCT);
            int intFromEnvMap = TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_PVERSION);
            int intFromEnvMap2 = TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_CVERSION);
            int intFromEnvMap3 = TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_HOTFIX);
            if (intFromEnvMap == 0) {
                String[] split = TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_SOFTVERSION).trim().split("[\\.]");
                if (split.length >= 3) {
                    intFromEnvMap = Integer.parseInt(split[0]);
                    intFromEnvMap2 = Integer.parseInt(split[1]);
                    intFromEnvMap3 = Integer.parseInt(split[2]);
                }
            }
            this.Km.lf = new dq(intFromEnvMap, intFromEnvMap2, intFromEnvMap3);
            this.Km.r = Kg;
            this.Km.imsi = l.dk(h.D(this.mContext));
            this.Km.le = f.iw() != cz.gD ? 1 : 2;
            evVar = this.Km;
            if (!c.y(this.mContext)) {
                i = 0;
            }
            evVar.fL = i;
            this.Km.gH = j.iM();
            this.Km.u = TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_BUILD);
        }
        pd pdVar = (pd) ManagerCreatorC.getManager(pd.class);
        if (pdVar != null) {
            this.Km.jh = pdVar.c();
        }
        d.d(TAG, "getUserInfo() product: " + this.Km.product);
        d.d(TAG, "getUserInfo() new guid: " + this.Km.jh);
        return this.Km;
    }

    public dz hV() {
        if (this.Kn != null) {
            this.Kn.e(this.mImei);
        } else {
            this.Kn = new dz();
            this.Kn.f(l.dk(h.D(this.mContext)));
            this.Kn.e(this.mImei);
            this.Kn.C(l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_LC)));
            this.Kn.r(l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_SOFTVERSION)));
            this.Kn.y(TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_BUILD));
            this.Kn.z(TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_PRODUCT));
        }
        return this.Kn;
    }

    synchronized int hZ() {
        if (Kg != null) {
            if (!(Kg.equals("") || hY())) {
                return 0;
            }
        }
        int hX = hX();
        if (hX != 0) {
            return hX;
        }
        this.yq.a(BasicCloudField.PHONE_IMEI, this.mImei, false);
        this.yq.a("mac", this.Kj, false);
        this.yq.a("guid", Kg, false);
        return 0;
    }

    dk ia() {
        String dk = l.dk(this.yq.getString("tid", null));
        d.d("Chord", "getMainReqInfo() oldTipsId:" + dk);
        if (this.Kp != null) {
            this.Kp.w(dk);
        } else {
            this.Kp = new dk();
            this.Kp.w(dk);
            this.Kp.a(new ep(this.mContext.getPackageName()));
        }
        return this.Kp;
    }

    public int w(List<qs> list) {
        return b(list, Kf);
    }
}
