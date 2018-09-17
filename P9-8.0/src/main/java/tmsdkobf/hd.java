package tmsdkobf;

import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;

public final class hd implements kf {
    private static hc pp;
    private long mr;

    public hd(long j) {
        this.mr = j;
        if (pp == null) {
            Class cls = hd.class;
            synchronized (hd.class) {
                if (pp == null) {
                    pp = new hc();
                    pp.onCreate(TMSDKContext.getApplicaionContext());
                }
            }
        }
        pp = (hc) ManagerCreatorC.getManager(hc.class);
    }

    public jv ap(String str) {
        return pp.a(str, this.mr);
    }

    public jx getPreferenceService(String str) {
        return pp.b(str, this.mr);
    }
}
