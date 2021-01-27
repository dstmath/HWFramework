package android.rms.iaware.scenerecog;

import android.content.Context;

public class HwAppSceneImpl {
    private static final int DEFAULT_LIST_VER = Integer.MAX_VALUE;
    private static final int DEFAULT_SWITCH = -1;
    private static final Object LOCK = new Object();
    private static HwAppSceneImpl sInstance;
    private AwareAppSceneBiz mAppSceneBiz = new AwareAppSceneBiz();

    private HwAppSceneImpl() {
    }

    public static HwAppSceneImpl getDefault() {
        HwAppSceneImpl hwAppSceneImpl;
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new HwAppSceneImpl();
            }
            hwAppSceneImpl = sInstance;
        }
        return hwAppSceneImpl;
    }

    public void reportBindApplicationToAware(Context context) {
        this.mAppSceneBiz.initAppSceneBiz(context);
    }

    public boolean isMatchSceneId(String pkgName, String activityName, int sceneId) {
        return this.mAppSceneBiz.isMatchSceneId(pkgName, activityName, sceneId, DEFAULT_LIST_VER, -1);
    }

    public boolean isMatchSceneId(String pkgName, String activityName, int sceneId, int listVer) {
        return this.mAppSceneBiz.isMatchSceneId(pkgName, activityName, sceneId, listVer, -1);
    }

    public boolean isMatchSceneId(String pkgName, String activityName, int sceneId, int listVer, int featureBit) {
        return this.mAppSceneBiz.isMatchSceneId(pkgName, activityName, sceneId, listVer, featureBit);
    }
}
