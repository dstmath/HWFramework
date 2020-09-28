package android.rms.iaware.scenerecog;

import android.content.Context;
import android.os.Process;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.rms.iaware.scenerecog.AwareAppDataBiz;
import android.util.Log;
import java.util.concurrent.atomic.AtomicBoolean;

public class AwareAppSceneBiz implements AwareAppDataBiz.IAwareAppSceneBiz {
    private static final boolean SCENE_RECOG_SWITCH = SystemProperties.getBoolean("persist.sys.iaware.appscenerecog.switch", false);
    private static final String TAG = "AwareAppSceneBiz";
    private AwareAppDataBiz mAppDataBiz;
    private AtomicBoolean mEnable = new AtomicBoolean(false);

    public AwareAppSceneBiz() {
        init();
    }

    private void init() {
        this.mAppDataBiz = new AwareAppDataBiz(this);
    }

    @Override // android.rms.iaware.scenerecog.AwareAppDataBiz.IAwareAppSceneBiz
    public void enable() {
        this.mEnable.set(true);
    }

    @Override // android.rms.iaware.scenerecog.AwareAppDataBiz.IAwareAppSceneBiz
    public void disable() {
        this.mEnable.set(false);
    }

    public void initAppSceneBiz(Context context) {
        AwareAppDataBiz awareAppDataBiz;
        disable();
        if (UserHandle.getAppId(Process.myUid()) < 10000 || !SCENE_RECOG_SWITCH) {
            return;
        }
        if (context == null || (awareAppDataBiz = this.mAppDataBiz) == null) {
            Log.i(TAG, "Application context is null");
        } else {
            awareAppDataBiz.initAppSceneData(context.getPackageName());
        }
    }

    public boolean isMatchSceneId(String pkgName, String activityName, int sceneId, int listVer, int featureBit) {
        AwareAppDataBiz awareAppDataBiz;
        if (!this.mEnable.get() || (awareAppDataBiz = this.mAppDataBiz) == null) {
            return false;
        }
        return awareAppDataBiz.isMatchSceneId(pkgName, activityName, sceneId, listVer, featureBit);
    }
}
