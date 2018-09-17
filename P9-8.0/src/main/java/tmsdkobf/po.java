package tmsdkobf;

import android.content.Context;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.ErrorCode;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.exception.NetWorkException;
import tmsdk.common.exception.NetworkOnMainThreadException;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.tcc.TccCryptor;
import tmsdk.common.utils.e;
import tmsdk.common.utils.i;
import tmsdk.common.utils.l;
import tmsdk.common.utils.n;
import tmsdk.common.utils.q;

class po implements pl {
    private static String IV = null;
    private static String TAG = "WupSessionHelperImpl";
    private ek IS;
    private fc IT;
    private final String Kh = "tid";
    private final String Ki = "mdtids";
    private String Kj = null;
    private ep Kk;
    private eq Kl;
    private ec Km;
    private eh Kn;
    volatile boolean Ko;
    private Object Kp = new Object();
    private volatile long Kq = 0;
    private volatile long Kr = 0;
    private boolean Ks = false;
    private Context mContext;
    private String mImei = null;
    private md vu;

    public po(Context context) {
        this.mContext = context;
        this.vu = new md("wup");
        if (IV == null) {
            IV = this.vu.getString("guid", null);
        }
        this.mImei = q.cI(l.L(this.mContext));
        this.Kj = q.cI(l.N(this.mContext));
        lw.eI();
        hM();
    }

    private void F(long j) {
        this.Kq = j;
        this.vu.a("last_ft", this.Kq, true);
    }

    private void G(long j) {
        this.Kr = j;
        this.vu.a("cts_td", this.Kr, true);
    }

    private void a(fo foVar, int i, String str, String str2, HashMap<String, Object> hashMap) {
        foVar.E(i);
        foVar.C(str);
        foVar.D(str2);
        foVar.B("UTF-8");
        if (hashMap != null && hashMap.size() > 0) {
            for (Entry entry : hashMap.entrySet()) {
                foVar.put((String) entry.getKey(), entry.getValue());
            }
        }
    }

    private void hM() {
        this.Kq = this.vu.getLong("last_ft", 0);
        this.Kr = this.vu.getLong("cts_td", 0);
    }

    private ec hQ() {
        if (this.Km != null) {
            this.Km.l(IV);
            this.Km.d(this.mImei);
            this.Km.f(this.Kj);
        } else {
            this.Km = new ec();
            this.Km.d(this.mImei);
            this.Km.e(q.cI(l.M(this.mContext)));
            this.Km.f(this.Kj);
            this.Km.g(q.cI(l.O(this.mContext)));
            this.Km.h(q.cI(l.P(this.mContext)));
            this.Km.e(n.iX());
            this.Km.i(q.cI(l.iL()));
            this.Km.j(q.cI(l.getProductName()));
            this.Km.k(q.cI(e.E(this.mContext)));
            this.Km.l(IV);
        }
        return this.Km;
    }

    private int hR() {
        ec hQ = hQ();
        AtomicReference atomicReference = new AtomicReference();
        int a = ((pq) ManagerCreatorC.getManager(pq.class)).a(hQ, atomicReference);
        if (a != 0) {
            return a;
        }
        eg egVar = (eg) atomicReference.get();
        if (egVar == null) {
            return a;
        }
        IV = egVar.b();
        return (IV == null || IV.equals("")) ? -2001 : a;
    }

    private boolean hS() {
        String cI = q.cI(l.L(this.mContext));
        String cI2 = q.cI(l.N(this.mContext));
        this.mImei = this.vu.getString("imei", cI);
        this.Kj = this.vu.getString("mac", cI2);
        if (cI.equals(this.mImei) && cI2.equals(this.Kj)) {
            return false;
        }
        this.mImei = cI;
        this.Kj = cI2;
        return true;
    }

    public int a(pp ppVar) {
        return a(ppVar, TMSDKContext.getStrFromEnvMap(TMSDKContext.PRE_HTTP_SERVER_URL));
    }

    public int a(pp ppVar, String str) {
        return a(ppVar, false, str);
    }

    public int a(pp ppVar, boolean z) {
        return a(ppVar, z, TMSDKContext.getStrFromEnvMap(TMSDKContext.PRE_HTTP_SERVER_URL));
    }

    /* JADX WARNING: Missing block: B:73:0x027e, code:
            r11 = r13;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    int a(pp ppVar, boolean z, String str) {
        lw lwVar;
        NetWorkException e;
        Throwable th;
        kv.d(TAG, " runHttpSession() url:" + str);
        boolean eJ = eJ();
        kv.d(TAG, " runHttpSession() couldNotConnect:" + eJ);
        if (eJ) {
            return -64;
        }
        int i = ErrorCode.ERR_WUP;
        if (ppVar == null) {
            return -6057;
        }
        byte[] bArr;
        if (!z) {
            i = hT();
            if (i != 0) {
                return i - 60;
            }
        }
        fo foVar = ppVar.Kw;
        fo foVar2 = ppVar.Kx;
        a(foVar, ppVar.Kt, ppVar.Ku.Kf, ppVar.Ku.Kg, ppVar.Kv);
        int p = foVar.p();
        fo foVar3 = null;
        if (p != 999 && hN()) {
            pn pnVar = (pn) pm.Ke.get(999);
            HashMap hashMap = new HashMap(3);
            hashMap.put("phonetype", ht());
            hashMap.put("userinfo", hu());
            hashMap.put("reqinfo", hU());
            foVar3 = new fo(true);
            a(foVar3, 999, pnVar.Kf, pnVar.Kg, hashMap);
            kv.n("Chord", "WupSessionHelperImpl runHttpSession() getMainTips");
        }
        lwVar = null;
        if (foVar3 != null) {
            try {
                Object l = foVar.l();
                Object l2 = foVar3.l();
                bArr = new byte[(l.length + l2.length)];
                System.arraycopy(l, 0, bArr, 0, l.length);
                System.arraycopy(l2, 0, bArr, l.length, l2.length);
            } catch (NetWorkException e2) {
                e = e2;
                i = e.getErrCode();
                kv.o(TAG, "NetWorkException:" + e.getMessage());
                e.printStackTrace();
                int i2 = i;
                if (lwVar != null) {
                    lwVar.close();
                }
                if (!this.Ko && i == 0) {
                    TMSDKContext.reportChannelInfo();
                }
                return i2;
            } catch (IllegalArgumentException e3) {
                e = e3;
                i = -6057;
                kv.o(TAG, "wup agrs error:" + e.getMessage());
                e.printStackTrace();
                if (lwVar != null) {
                    lwVar.close();
                }
                if (this.Ko) {
                }
            } catch (NetworkOnMainThreadException e4) {
                e = e4;
                throw e;
            } catch (Throwable th2) {
                th = th2;
                NetWorkException netWorkException = e;
            }
        } else {
            bArr = foVar.l();
        }
        byte[] encrypt = TccCryptor.encrypt(bArr, null);
        lwVar = lw.e(str, ppVar.Ky);
        lwVar.setRequestMethod("POST");
        lwVar.setPostData(encrypt);
        int eK = lwVar.eK();
        AtomicReference atomicReference = new AtomicReference();
        i = lwVar.a(false, atomicReference);
        if (i == 0) {
            byte[] bArr2 = (byte[]) atomicReference.get();
            if (bArr2 != null) {
                if (bArr2.length > 0) {
                    Object decrypt = TccCryptor.decrypt(bArr2, null);
                    int i3 = ((((decrypt[0] & 255) << 24) | ((decrypt[1] & 255) << 16)) | ((decrypt[2] & 255) << 8)) | (decrypt[3] & 255);
                    if (i3 == decrypt.length) {
                        foVar2.b(decrypt);
                    } else if (i3 > 0 && decrypt.length > i3 + 4) {
                        int i4 = ((((decrypt[i3] & 255) << 24) | ((decrypt[i3 + 1] & 255) << 16)) | ((decrypt[i3 + 2] & 255) << 8)) | (decrypt[i3 + 3] & 255);
                        if (i3 + i4 == decrypt.length) {
                            fo foVar4;
                            Object obj = new byte[i3];
                            Object obj2 = new byte[i4];
                            System.arraycopy(decrypt, 0, obj, 0, i3);
                            System.arraycopy(decrypt, i3, obj2, 0, i4);
                            fo foVar5 = new fo(true);
                            foVar5.B("UTF-8");
                            foVar5.b(obj2);
                            foVar2.b(obj);
                            if (foVar5.p() == p) {
                                foVar4 = foVar2;
                                foVar2 = foVar5;
                            }
                            ppVar.Kx = foVar2;
                            foVar5 = foVar4;
                            er erVar = (er) foVar5.a("cmdinfo", (Object) new er());
                            if (erVar != null) {
                                cq(erVar.c());
                            } else {
                                new er().s("");
                            }
                        }
                    }
                }
            }
            i = 0;
            if (lwVar != null) {
                lwVar.close();
            }
            if (!this.Ko) {
                TMSDKContext.reportChannelInfo();
            }
            return i;
        }
        int i5 = i;
        if (lwVar != null) {
            lwVar.close();
        }
        if (!this.Ko && i == 0) {
            TMSDKContext.reportChannelInfo();
        }
        return i5;
        if (lwVar != null) {
            lwVar.close();
        }
        if (!this.Ko && r31 == 0) {
            TMSDKContext.reportChannelInfo();
        }
        throw th;
    }

    public Object a(fo foVar, String str, Object obj) {
        if (str == null) {
            str = "";
        }
        return foVar.a(str, obj);
    }

    public void cq(String str) {
        kv.n("Chord", "setNewTipsId() newTipsId:" + str);
        this.vu.a("tid", str, true);
    }

    public boolean eJ() {
        return lw.eJ();
    }

    /* JADX WARNING: Removed duplicated region for block: B:39:0x00b2  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0093  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean hN() {
        synchronized (this.Kp) {
            if (this.Ks) {
                boolean z;
                long currentTimeMillis = System.currentTimeMillis();
                boolean a = lr.a(new Date(currentTimeMillis), new Date(this.Kq));
                kv.d("Chord", "couldFetchCloud() nowIsTheDayOfLastFetch: " + a);
                if (a) {
                    if ((this.Kr < 72 ? 1 : null) == null) {
                        kv.n("Chord", "couldFetchCloud() couldFetchCloud: false");
                        return false;
                    }
                }
                G(0);
                long j = currentTimeMillis - this.Kq;
                if ((j <= 0 ? 1 : null) == null) {
                    if ((j <= 1200000 ? 1 : null) != null) {
                        z = false;
                        kv.d("Chord", "couldFetchCloud() moreThanBlank: " + z);
                        if (z) {
                            kv.n("Chord", "couldFetchCloud() couldFetchCloud: false");
                            return false;
                        }
                        F(currentTimeMillis);
                        G(this.Kr + 1);
                        kv.n("Chord", "couldFetchCloud() couldFetchCloud: true");
                        return true;
                    }
                }
                z = true;
                kv.d("Chord", "couldFetchCloud() moreThanBlank: " + z);
                if (z) {
                }
            } else {
                kv.n("Chord", "couldFetchCloud() mIsCloudReady: false");
                return false;
            }
        }
    }

    public ep hO() {
        if (this.Kk == null) {
            this.Kk = new ep();
            this.Kk.f(2);
        }
        return this.Kk;
    }

    public eq hP() {
        if (this.Kl != null) {
            this.Kl.d(this.mImei);
        } else {
            this.Kl = new eq();
            this.Kl.e(q.cI(l.M(this.mContext)));
            this.Kl.d(this.mImei);
            this.Kl.q(q.cI("19B7C7417A1AB190"));
            this.Kl.r(q.cI("6.1.0"));
            this.Kl.m(3059);
            this.Kl.n(13);
        }
        return this.Kl;
    }

    /* JADX WARNING: Missing block: B:10:0x002f, code:
            return 0;
     */
    /* JADX WARNING: Missing block: B:15:0x003f, code:
            if (hS() == false) goto L_0x002e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized int hT() {
        if (IV != null) {
            if (!IV.equals("")) {
            }
        }
        int hR = hR();
        if (hR != 0) {
            return hR;
        }
        this.vu.a("imei", this.mImei, false);
        this.vu.a("mac", this.Kj, false);
        this.vu.a("guid", IV, false);
    }

    eh hU() {
        String cI = q.cI(this.vu.getString("tid", null));
        kv.n("Chord", "getMainReqInfo() oldTipsId:" + cI);
        if (this.Kn != null) {
            this.Kn.n(cI);
        } else {
            this.Kn = new eh();
            this.Kn.n(cI);
            this.Kn.a(new ex(this.mContext.getPackageName()));
        }
        return this.Kn;
    }

    public ek ht() {
        if (this.IS == null) {
            this.IS = new ek();
            this.IS.f(2);
            this.IS.g(SmsCheckResult.ESCT_201);
        }
        return this.IS;
    }

    public fc hu() {
        int i = 2;
        int i2 = 1;
        if (this.IT != null) {
            this.IT.I = IV;
            this.IT.dl = this.mImei;
            this.IT.lz = e.iB();
            fc fcVar = this.IT;
            if (i.iG() == eb.iJ) {
                i2 = 2;
            }
            fcVar.lx = i2;
        } else {
            this.IT = new fc();
            this.IT.dl = this.mImei;
            this.IT.dq = q.cI("19B7C7417A1AB190");
            this.IT.dr = q.cI(im.bQ());
            this.IT.dw = q.cI(l.iL());
            this.IT.dp = 13;
            int i3 = 0;
            int i4 = 0;
            int i5 = 0;
            String[] split = "6.1.0".trim().split("[\\.]");
            if (split.length >= 3) {
                i3 = Integer.parseInt(split[0]);
                i4 = Integer.parseInt(split[1]);
                i5 = Integer.parseInt(split[2]);
            }
            this.IT.ly = new el(i3, i4, i5);
            this.IT.I = IV;
            this.IT.imsi = q.cI(l.M(this.mContext));
            fc fcVar2 = this.IT;
            if (i.iG() != eb.iJ) {
                i = 1;
            }
            fcVar2.lx = i;
            fc fcVar3 = this.IT;
            if (!e.F(this.mContext)) {
                i2 = 0;
            }
            fcVar3.ib = i2;
            this.IT.iN = n.iX();
            this.IT.L = 3059;
        }
        nz nzVar = (nz) ManagerCreatorC.getManager(nz.class);
        if (nzVar != null) {
            this.IT.lD = nzVar.b();
        }
        kv.n(TAG, "getUserInfo() product: " + this.IT.dp);
        kv.n(TAG, "getUserInfo() new guid: " + this.IT.lD);
        return this.IT;
    }
}
