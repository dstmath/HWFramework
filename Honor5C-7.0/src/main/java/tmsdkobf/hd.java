package tmsdkobf;

import java.util.List;
import tmsdk.common.TMSDKContext;
import tmsdkobf.ha.a;

/* compiled from: Unknown */
public class hd extends gz {
    hb pT;
    hc pU;
    gp pV;
    boolean qc;
    String[] qq;
    ha qt;
    boolean qu;

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.hd.2 */
    class AnonymousClass2 implements a {
        final /* synthetic */ hd qv;
        final /* synthetic */ int qw;

        AnonymousClass2(hd hdVar, int i) {
            this.qv = hdVar;
            this.qw = i;
        }

        public void a(int i, int i2, String str) {
            if (this.qv.pP != null) {
                this.qv.pP.a(i, i2, str);
            }
        }

        public void bn() {
            if (this.qv.pP != null) {
                this.qv.pP.aC(this.qw);
            }
        }
    }

    public hd(int i) {
        super(i);
        this.qu = false;
        this.qc = false;
        this.pT = new hb();
        this.pU = new hc();
        this.qt = new ha();
        this.pV = new gp();
        this.pU.a(new hc.a() {
            final /* synthetic */ hd qv;

            {
                this.qv = r1;
            }

            public void a(int i, List<String> list, boolean z, long j, String str, String str2, String str3) {
                this.qv.a(i, list, z, j, str, str2, str3);
            }
        });
        this.qt.a(this.pU);
        this.qt.a(this.pT);
        this.qt.a(new AnonymousClass2(this, i));
        this.qt.a(this.pV);
    }

    public void a(String[] strArr) {
        this.qq = strArr;
        this.pT.a(this.qq);
    }

    void bi() {
    }

    void bj() {
        this.qu = true;
    }

    void bk() {
        if (this.qu) {
            this.pT.a(TMSDKContext.getApplicaionContext());
            this.pV.init();
            this.qt.r(this.qc);
            this.qt.bk();
        }
    }

    void bl() {
        this.qt.bm();
    }

    public void onDestroy() {
        if (this.qu) {
            this.qt.bm();
        }
    }

    public void t(boolean z) {
        this.qc = z;
        be();
    }
}
