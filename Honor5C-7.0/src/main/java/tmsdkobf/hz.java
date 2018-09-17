package tmsdkobf;

import android.os.Handler;
import android.os.Looper;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.update.UpdateConfig;

/* compiled from: Unknown */
public class hz extends pf implements lm {

    /* compiled from: Unknown */
    /* renamed from: tmsdkobf.hz.1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ hz rA;
        final /* synthetic */ boolean ry;
        final /* synthetic */ boolean rz;

        AnonymousClass1(hz hzVar, boolean z, boolean z2) {
            this.rA = hzVar;
            this.ry = z;
            this.rz = z2;
        }

        public void run() {
            boolean v = new fv().v();
            pd pdVar = (pd) ManagerCreatorC.getManager(pd.class);
            pdVar.J(this.ry);
            if (v) {
                pdVar.onImsiChanged();
                ku.dq().onImsiChanged();
            }
            pg.gB().a(pdVar.gm());
            if (this.rz) {
                pg.gB().start();
            }
        }
    }

    public hz(long j) {
        super(j);
    }

    public hz(oq oqVar, boolean z) {
        super(bI());
        ((pd) ManagerCreatorC.getManager(pd.class)).a(oqVar);
        jq.a((pf) this);
        boolean equals = TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_IS_TEST).equals("true");
        super.init(true);
        lk.wC = true;
        kr.do().a(ku.dq());
        kr.do().a(kz.dx());
        new Thread(new AnonymousClass1(this, equals, z)).start();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            final /* synthetic */ hz rA;

            {
                this.rA = r1;
            }

            public void run() {
                kr.do().bb(ku.dq().dl());
            }
        }, 30000);
    }

    private static long bI() {
        return jk.getIdent(0, UpdateConfig.UPDATE_FLAG_PAY_LIST);
    }

    public static void bJ() {
        if (jq.cu() == null) {
            hz hzVar = new hz(new fz(), fw.w().K().booleanValue());
        }
    }
}
