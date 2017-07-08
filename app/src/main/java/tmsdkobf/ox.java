package tmsdkobf;

import android.content.Context;
import android.text.TextUtils;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Random;
import javax.crypto.Cipher;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class ox {
    private pb Ew;
    private b Fo;
    private volatile long Fp;
    private final String TAG;
    private Object mLock;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.ox.1 */
    class AnonymousClass1 extends oy {
        final /* synthetic */ a Fq;
        final /* synthetic */ ox Fr;

        AnonymousClass1(ox oxVar, String str, a aVar) {
            this.Fr = oxVar;
            this.Fq = aVar;
            super(str);
        }

        public void a(boolean z, int i, int i2, ArrayList<bq> arrayList) {
            if (i != 0) {
                d.c("RsaKeyCertifier", "updataRsaKey() ESharkCode.ERR_NONE != retCode, retCode: " + i);
                this.Fq.I(false);
            } else if (arrayList == null) {
                this.Fq.I(false);
            } else if (arrayList.size() > 0) {
                bq bqVar = (bq) arrayList.get(0);
                if (bqVar != null) {
                    d.e("RsaKeyCertifier", "updataRsaKey() rs.seqNo: " + bqVar.dG + " rs.cmd: " + bqVar.aZ + " rs.retCode: " + bqVar.dJ + " rs.dataRetCode: " + bqVar.dK);
                    if (1 == bqVar.dJ) {
                        this.Fq.I(false);
                        return;
                    } else if (bqVar.dK == 0) {
                        byte[] bArr = bqVar.data;
                        if (bArr != null) {
                            d.e("RsaKeyCertifier", "updataRsaKey() rs.data.length: " + bqVar.data.length);
                            try {
                                fs d = ok.d(bArr, new aq());
                                if (d != null) {
                                    aq aqVar = (aq) d;
                                    boolean z2 = TextUtils.isEmpty(this.Fr.S()) || TextUtils.isEmpty(aqVar.t);
                                    if (z2) {
                                        d.c("RsaKeyCertifier", "updataRsaKey() unNnormal");
                                        this.Fq.I(false);
                                        return;
                                    }
                                    pa.a("ocean", "[ocean]info: Chgd befor: sessionId|" + this.Fr.T() + "|encodeKey|" + this.Fr.S(), null, null);
                                    synchronized (this.Fr.mLock) {
                                        this.Fr.cP(this.Fu);
                                        this.Fr.cQ(aqVar.t);
                                    }
                                    d.d("RsaKeyCertifier", "updataRsaKey() mEncodeKey: " + this.Fr.S() + " mSessionId: " + aqVar.t);
                                    pa.a("ocean", "[ocean]info: Chgd after: sessionId|" + aqVar.t + "|encodeKey|" + this.Fr.S(), null, null);
                                    this.Fq.I(true);
                                    return;
                                }
                                d.c("RsaKeyCertifier", "updataRsaKey() null == js");
                                this.Fq.I(false);
                                return;
                            } catch (Exception e) {
                                d.c("RsaKeyCertifier", "updataRsaKey() convert failed");
                                this.Fq.I(false);
                                return;
                            }
                        }
                        this.Fq.I(false);
                        return;
                    } else {
                        d.c("RsaKeyCertifier", "updataRsaKey() rs.dataRetCode: " + bqVar.dK);
                        this.Fq.I(false);
                        return;
                    }
                }
                this.Fq.I(false);
            } else {
                this.Fq.I(false);
            }
        }
    }

    /* compiled from: Unknown */
    public interface a {
        void I(boolean z);
    }

    /* compiled from: Unknown */
    public static class b {
        public volatile String Fs;
        public volatile String Ft;

        public b() {
            this.Fs = "";
            this.Ft = "";
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mSessionId: ");
            stringBuilder.append(this.Fs);
            stringBuilder.append(" mEncodeKey: ");
            stringBuilder.append(this.Ft);
            return stringBuilder.toString();
        }
    }

    public ox(Context context, pb pbVar) {
        this.TAG = "RsaKeyCertifier";
        this.mLock = new Object();
        this.Ew = pbVar;
        this.Fo = new b();
        load();
    }

    private String S() {
        synchronized (this.mLock) {
            if (this.Fo.Ft != null) {
                String str = this.Fo.Ft;
                return str;
            }
            str = "";
            return str;
        }
    }

    private String T() {
        synchronized (this.mLock) {
            if (this.Fo.Fs != null) {
                String str = this.Fo.Fs;
                return str;
            }
            str = "";
            return str;
        }
    }

    private void ak(String str) {
        if (!TextUtils.isEmpty(str)) {
            synchronized (this.mLock) {
                this.Fo.Ft = str;
            }
        }
    }

    private void al(String str) {
        if (!TextUtils.isEmpty(str)) {
            synchronized (this.mLock) {
                this.Fo.Fs = str;
            }
        }
    }

    private void cP(String str) {
        if (!TextUtils.isEmpty(str)) {
            ak(str);
            this.Ew.gm().ax(S());
        }
    }

    private void cQ(String str) {
        if (!TextUtils.isEmpty(str)) {
            al(str);
            this.Ew.gm().ay(T());
        }
    }

    private byte[] cR(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        byte[] doFinal;
        try {
            Key generatePublic = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(tmsdk.common.utils.b.decode("MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDb49jFnNqMDLdl87UtY5jOMqqdMuvQg65Zuva3Qm1tORQGBuM04u7fqygA64XbOx9e/KPNkDNDmqS8SlsAPL1fV2lqM/phgV0NY62TJqSR+PLngwJd2rhYR8wQ1N0JE+R59a5c08EGsd6axStjHsVu2+evCf/SWU9Y/oQpEtOjGwIDAQAB", 0)));
            Cipher instance = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            instance.init(1, generatePublic);
            doFinal = instance.doFinal(str.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            doFinal = null;
        }
        return doFinal;
    }

    private String cc(int i) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer stringBuffer = new StringBuffer();
        for (int i2 = 0; i2 < i; i2++) {
            stringBuffer.append(str.charAt(random.nextInt(62)));
        }
        return stringBuffer.toString();
    }

    private void gh() {
        ak(cc(16));
    }

    private void load() {
        synchronized (this.mLock) {
            ak(this.Ew.gm().aq());
            al(this.Ew.gm().ar());
            this.Fp = this.Ew.gm().aE();
        }
        d.d("RsaKeyCertifier", "load() mEncodeKey: " + S() + " mSessionId: " + T());
        pa.a("ocean", "[ocean]info: Refh: sessionId|" + T() + "|encodeKey|" + S(), null, null);
    }

    public void a(a aVar) {
        d.e("RsaKeyCertifier", "updataRsaKey()");
        gh();
        fs apVar = new ap();
        byte[] cR = cR(S());
        if (cR != null) {
            apVar.by = cR;
            d.d("RsaKeyCertifier", "updataRsaKey() reqRSA.enc: " + mo.bytesToHexString(apVar.by));
            bm bmVar = new bm();
            bmVar.dG = oz.gi().fP();
            bmVar.aZ = 152;
            bmVar.data = ok.c(apVar);
            ArrayList arrayList = new ArrayList();
            arrayList.add(bmVar);
            this.Ew.a(arrayList, new AnonymousClass1(this, S(), aVar));
            return;
        }
        d.c("RsaKeyCertifier", "updataRsaKey() null == dyKey");
        throw new RuntimeException("dyKey is null");
    }

    public b gg() {
        b bVar = new b();
        bVar.Fs = this.Fo.Fs;
        bVar.Ft = this.Fo.Ft;
        return bVar;
    }
}
