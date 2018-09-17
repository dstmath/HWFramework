package tmsdkobf;

import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;

/* compiled from: Unknown */
public final class ig implements ln {
    private static if rM;
    private long lU;

    public ig(long j) {
        this.lU = j;
        if (rM == null) {
            synchronized (ig.class) {
                if (rM == null) {
                    rM = new if();
                    rM.onCreate(TMSDKContext.getApplicaionContext());
                }
            }
        }
        rM = (if) ManagerCreatorC.getManager(if.class);
    }

    public lc bp(String str) {
        return rM.b(str, this.lU);
    }

    public lf getPreferenceService(String str) {
        return rM.c(str, this.lU);
    }
}
