package tmsdkobf;

import android.content.Context;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.module.intelli_sms.SmsCheckResult;
import tmsdk.common.utils.e;
import tmsdk.common.utils.i;
import tmsdk.common.utils.l;
import tmsdk.common.utils.n;
import tmsdk.common.utils.q;

public class os {
    private static String IV = null;
    private static String TAG = "SharkSessionHelperImpl";
    private ek IS;
    private fc IT;
    private dv IU;
    private Context mContext;
    private String mImei = null;

    public os(Context context) {
        this.mContext = context;
        this.mImei = q.cI(l.L(this.mContext));
        IV = "V2;" + this.mImei + ";" + q.cI(l.N(this.mContext));
    }

    public ek ht() {
        if (this.IS == null) {
            this.IS = new ek();
            this.IS.kn = 2;
            this.IS.dt = SmsCheckResult.ESCT_201;
        }
        return this.IS;
    }

    public fc hu() {
        if (this.IT != null) {
            this.IT.I = IV;
            this.IT.dl = this.mImei;
            this.IT.lx = i.iG().value() != eb.iJ.value() ? eb.iI.value() : eb.iJ.value();
        } else {
            this.IT = new fc();
            this.IT.dl = this.mImei;
            this.IT.dq = q.cI("19B7C7417A1AB190");
            this.IT.dr = q.cI(im.bQ());
            this.IT.dw = q.cI(l.iL());
            this.IT.dp = 13;
            int i = 0;
            int i2 = 0;
            int i3 = 0;
            String[] split = "6.1.0".trim().split("[\\.]");
            if (split.length >= 3) {
                i = Integer.parseInt(split[0]);
                i2 = Integer.parseInt(split[1]);
                i3 = Integer.parseInt(split[2]);
            }
            el elVar = new el();
            elVar.ko = i;
            elVar.kp = i2;
            elVar.kq = i3;
            this.IT.ly = elVar;
            this.IT.I = IV;
            this.IT.imsi = q.cI(l.M(this.mContext));
            this.IT.lx = i.iG().value() != eb.iJ.value() ? eb.iI.value() : eb.iJ.value();
            this.IT.ib = !e.F(this.mContext) ? 0 : 1;
            this.IT.iN = n.iX();
            this.IT.L = 3059;
        }
        this.IT.dp = 13;
        nz nzVar = (nz) ManagerCreatorC.getManager(nz.class);
        if (nzVar != null) {
            this.IT.lD = nzVar.b();
        }
        return this.IT;
    }

    public dv hv() {
        if (this.IU == null) {
            this.IU = new dv();
            this.IU.id = q.cI(im.bQ());
            this.IU.dp = 13;
            this.IU.ib = 0;
        }
        return this.IU;
    }
}
