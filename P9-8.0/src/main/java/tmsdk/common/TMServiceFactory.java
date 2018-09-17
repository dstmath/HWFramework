package tmsdk.common;

import tmsdk.common.creator.ManagerCreatorC;
import tmsdkobf.jd;
import tmsdkobf.jg;
import tmsdkobf.jx;
import tmsdkobf.kg;
import tmsdkobf.oz;
import tmsdkobf.pa;

public final class TMServiceFactory {
    private static IServiceProvider xv;

    public interface IServiceProvider {
        jx getPreferenceService(String str);

        jx getSingleProcessPrefService(String str);

        kg getSysDBService();

        pa getSystemInfoService();
    }

    public static jx getPreferenceService(String str) {
        return xv == null ? jd.b(TMSDKContext.getApplicaionContext(), str) : xv.getPreferenceService(str);
    }

    public static jx getSingleProcessPrefService(String str) {
        return xv == null ? jd.b(TMSDKContext.getApplicaionContext(), str) : xv.getSingleProcessPrefService(str);
    }

    public static kg getSysDBService() {
        return xv == null ? new jg(TMSDKContext.getApplicaionContext(), 0) : xv.getSysDBService();
    }

    public static pa getSystemInfoService() {
        pa paVar = null;
        if (xv != null) {
            paVar = xv.getSystemInfoService();
        }
        return paVar != null ? paVar : (pa) ManagerCreatorC.getManager(oz.class);
    }

    public static void initServices(IServiceProvider iServiceProvider) {
        xv = iServiceProvider;
    }
}
