package tmsdk.common;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdkobf.kk;
import tmsdkobf.kn;
import tmsdkobf.lf;
import tmsdkobf.lo;
import tmsdkobf.qc;
import tmsdkobf.qd;

/* compiled from: Unknown */
public final class TMServiceFactory {
    private static IServiceProvider Aj;

    /* compiled from: Unknown */
    public interface IServiceProvider {
        lf getPreferenceService(String str);

        lf getSingleProcessPrefService(String str);

        lo getSysDBService();

        qd getSystemInfoService();

        ITMSPlugin getTMSPlugin();
    }

    public static lf getPreferenceService(String str) {
        return Aj == null ? kk.a(TMSDKContext.getApplicaionContext(), str) : Aj.getPreferenceService(str);
    }

    public static lf getSingleProcessPrefService(String str) {
        return Aj == null ? kk.a(TMSDKContext.getApplicaionContext(), str) : Aj.getSingleProcessPrefService(str);
    }

    public static lo getSysDBService() {
        return Aj == null ? new kn(TMSDKContext.getApplicaionContext(), 0) : Aj.getSysDBService();
    }

    public static qd getSystemInfoService() {
        qd qdVar = null;
        if (Aj != null) {
            qdVar = Aj.getSystemInfoService();
        }
        return qdVar != null ? qdVar : (qd) ManagerCreatorC.getManager(qc.class);
    }

    public static ITMSPlugin getTMSPlugin() {
        return Aj == null ? null : Aj.getTMSPlugin();
    }

    public static void initServices(IServiceProvider iServiceProvider) {
        Aj = iServiceProvider;
    }
}
