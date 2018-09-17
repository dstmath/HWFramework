package tmsdkobf;

import tmsdk.common.TMSDKContext;

public class gw extends ob implements ke {
    public gw(long j) {
        super(j);
    }

    public static void a(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, String str) {
        mb.n("SharkNetService", "[shark_init]initSharkSync()");
        nl aA = gh.aA();
        aA.a(z, z2, z3);
        ob.a(aA, z4, z5, str);
        nu.a((gw) fj.D(5));
        mb.n("SharkNetService", "[shark_init]initSharkSync() end");
    }

    public static void be() {
        a(true, false, false, true, TMSDKContext.getStrFromEnvMap(TMSDKContext.PRE_IS_TEST).equals("true"), TMSDKContext.getStrFromEnvMap(TMSDKContext.PRE_TCP_SERVER_ADDRESS));
    }
}
