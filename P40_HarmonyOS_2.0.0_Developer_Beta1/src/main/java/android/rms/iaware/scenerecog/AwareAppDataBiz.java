package android.rms.iaware.scenerecog;

import android.rms.iaware.scenerecog.AwareAppDataService;
import android.rms.iaware.scenerecog.entity.AwareSceneEntity;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.os.SystemPropertiesEx;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

public class AwareAppDataBiz implements AwareAppDataService.IAwareDataCallBack {
    private static final String ACTIVITY_NAME = "activity";
    private static final int DEFAULT_SWITCH_OPEN = -1;
    private static final boolean LOG_SWITCH = SystemPropertiesEx.getBoolean("persist.sys.iaware.appscenerecog.log.switch", false);
    private static final int MAX_BIT_POSITION = 30;
    private static final int MIN_BIT_POSITION = 1;
    private static final String PACKAGE_NAME = "pkgName";
    private static final String SCENE_ID = "sceneId";
    private static final String SYS_STATUS = "sysStatus";
    private static final String TAG = "AwareAppDataBiz";
    private static final String VERSION_APP_LIST = "verAppList";
    private AwareAppDataService mAppDataService;
    private IAwareAppSceneBiz mAppSceneBiz;
    private final ArrayList<AwareSceneEntity> mAppSceneData = new ArrayList<>();

    public interface IAwareAppSceneBiz {
        void disable();

        void enable();
    }

    public AwareAppDataBiz(IAwareAppSceneBiz sceneBiz) {
        this.mAppSceneBiz = sceneBiz;
        this.mAppDataService = new AwareAppDataService(this);
    }

    public void initAppSceneData(String packageName) {
        if (!TextUtils.isEmpty(packageName)) {
            this.mAppDataService.initAppSceneData(packageName);
        }
    }

    @Override // android.rms.iaware.scenerecog.AwareAppDataService.IAwareDataCallBack
    public void onInitDataSuccess(ArrayList<String> data) {
        if (data == null || data.isEmpty()) {
            Log.i(TAG, "Currrent app scene data is null");
            onInitDataFailure();
            return;
        }
        logDebug("Current application scene data size " + data.size());
        synchronized (this.mAppSceneData) {
            this.mAppSceneData.clear();
            int listSize = data.size();
            for (int i = 0; i < listSize; i++) {
                String sceneString = data.get(i);
                try {
                    JSONObject jsonObject = new JSONObject(sceneString);
                    AwareSceneEntity entity = new AwareSceneEntity();
                    entity.setActivity(jsonObject.optString("activity"));
                    entity.setSceneId(jsonObject.optInt(SCENE_ID));
                    entity.setPkgName(jsonObject.optString("pkgName"));
                    entity.setAppListVersion(jsonObject.optInt(VERSION_APP_LIST));
                    entity.setSysStatus(jsonObject.optInt(SYS_STATUS));
                    this.mAppSceneData.add(entity);
                } catch (JSONException e) {
                    Log.i(TAG, "json Exception : " + sceneString);
                }
            }
            if (this.mAppSceneBiz != null) {
                if (this.mAppSceneData.isEmpty()) {
                    this.mAppSceneBiz.disable();
                } else {
                    this.mAppSceneBiz.enable();
                }
            }
        }
    }

    @Override // android.rms.iaware.scenerecog.AwareAppDataService.IAwareDataCallBack
    public void onInitDataFailure() {
        synchronized (this.mAppSceneData) {
            this.mAppSceneData.clear();
        }
        IAwareAppSceneBiz iAwareAppSceneBiz = this.mAppSceneBiz;
        if (iAwareAppSceneBiz != null) {
            iAwareAppSceneBiz.disable();
        }
    }

    public boolean isMatchSceneId(String pkgName, String activityName, int sceneId, int listVer, int featureBit) {
        if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(activityName)) {
            Log.i(TAG, "Current match package or activity name is null");
            return false;
        }
        synchronized (this.mAppSceneData) {
            if (this.mAppSceneData.isEmpty()) {
                Log.i(TAG, "Current application scene data is empty");
                return false;
            }
            int listSize = this.mAppSceneData.size();
            for (int i = 0; i < listSize; i++) {
                AwareSceneEntity entity = this.mAppSceneData.get(i);
                if (entity != null) {
                    if (sceneId != entity.getSceneId()) {
                        continue;
                    } else if (listVer >= entity.getAppListVersion()) {
                        if (isMatch(entity, pkgName, activityName, featureBit)) {
                            logDebug("Current application package name is " + pkgName + ", activity name is " + activityName + ", list version " + listVer + ", scene id is " + sceneId + ", feature bit " + featureBit + ", match success");
                            return true;
                        }
                    }
                }
            }
            logDebug("Current application package name is " + pkgName + ", activity name is " + activityName + ", list version " + listVer + ", scene id is " + sceneId + ", feature bit " + featureBit + ", match failure");
            return false;
        }
    }

    private boolean isMatch(AwareSceneEntity entity, String pkgName, String activityName, int featureBit) {
        if (entity != null && pkgName.equals(entity.getPkgName()) && activityName.equals(entity.getActivity())) {
            if (featureBit == -1) {
                return true;
            }
            if (featureBit > 30 || featureBit < 1) {
                return false;
            }
            int featureSwitch = 1 << featureBit;
            if ((entity.getSysStatus() & featureSwitch) == featureSwitch) {
                return true;
            }
        }
        return false;
    }

    private void logDebug(String message) {
        if (LOG_SWITCH) {
            Log.d(TAG, message);
        }
    }
}
