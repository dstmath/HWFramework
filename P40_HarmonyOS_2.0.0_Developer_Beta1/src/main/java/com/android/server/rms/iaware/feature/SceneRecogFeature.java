package com.android.server.rms.iaware.feature;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.text.TextUtils;
import com.android.server.rms.iaware.AwareCallback;
import com.android.server.rms.iaware.IRDataRegister;
import com.android.server.rms.iaware.appmng.ActivityEventManager;
import com.huawei.android.app.IHwActivityNotifierEx;

public class SceneRecogFeature extends RFeature {
    private static final int BASE_VERSION = 5;
    public static final String DATA_ACTIVITY_NAME = "activityName";
    public static final String DATA_COMP = "comp";
    public static final String DATA_PID = "pid";
    public static final String DATA_PKG_NAME = "pkgName";
    public static final String DATA_STATE = "state";
    public static final String DATA_UID = "uid";
    public static final String EVENT_PAUSE = "onPause";
    public static final String EVENT_RESUME = "onResume";
    public static final String REASON_INFO = "activityLifeState";
    private static final String TAG = "SceneRecogFeature";
    private static boolean sIsEnable = false;
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        /* class com.android.server.rms.iaware.feature.SceneRecogFeature.AnonymousClass1 */

        public void call(Bundle extras) {
            if (SceneRecogFeature.sIsEnable && extras != null) {
                String state = extras.getString(SceneRecogFeature.DATA_STATE);
                if (!TextUtils.isEmpty(state)) {
                    Parcelable comp = extras.getParcelable(SceneRecogFeature.DATA_COMP);
                    if (comp instanceof ComponentName) {
                        char c = 65535;
                        int hashCode = state.hashCode();
                        if (hashCode != -1340212393) {
                            if (hashCode == 1463983852 && state.equals(SceneRecogFeature.EVENT_RESUME)) {
                                c = 0;
                            }
                        } else if (state.equals(SceneRecogFeature.EVENT_PAUSE)) {
                            c = 1;
                        }
                        if (c == 0) {
                            SceneRecogFeature.this.activityStateChanged(extras.getInt(SceneRecogFeature.DATA_PID), extras.getInt("uid"), (ComponentName) comp, 1);
                        } else if (c == 1) {
                            SceneRecogFeature.this.activityStateChanged(extras.getInt(SceneRecogFeature.DATA_PID), extras.getInt("uid"), (ComponentName) comp, 2);
                        }
                    }
                }
            }
        }
    };

    public SceneRecogFeature(Context context, AwareConstant.FeatureType featureType, IRDataRegister dataRegister) {
        super(context, featureType, dataRegister);
        AwareLog.d(TAG, "create SceneRecogFeature success");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void activityStateChanged(int pid, int uid, ComponentName componentName, int event) {
        if (componentName != null) {
            Bundle bundle = new Bundle();
            bundle.putInt(DATA_PID, pid);
            bundle.putInt("uid", uid);
            bundle.putString(DATA_PKG_NAME, componentName.getPackageName());
            bundle.putString("activityName", componentName.flattenToShortString());
            ActivityEventManager.getInstance().reportAppEvent(event, bundle);
        }
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean disable() {
        if (!sIsEnable) {
            return false;
        }
        AwareCallback.getInstance().unregisterActivityNotifier(this.mActivityNotifierEx, REASON_INFO);
        sIsEnable = false;
        AwareLog.i(TAG, "SceneRecogFeature disable");
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enable() {
        return false;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean enableFeatureEx(int realVersion) {
        AwareLog.i(TAG, TAG + realVersion);
        if (realVersion < 5 || sIsEnable) {
            return false;
        }
        AwareCallback.getInstance().registerActivityNotifier(this.mActivityNotifierEx, REASON_INFO);
        sIsEnable = true;
        AwareLog.i(TAG, "SceneRecogFeature enableFeatureEx");
        return true;
    }

    @Override // com.android.server.rms.iaware.feature.RFeature
    public boolean reportData(CollectData data) {
        return false;
    }
}
