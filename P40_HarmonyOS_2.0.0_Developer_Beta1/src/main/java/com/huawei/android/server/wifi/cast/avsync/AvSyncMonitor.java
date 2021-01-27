package com.huawei.android.server.wifi.cast.avsync;

import android.content.ComponentName;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.cast.CastOptManager;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.IHwActivityNotifierEx;
import com.huawei.android.server.wifi.cast.ICastScene;

public class AvSyncMonitor {
    private static final String APP_SWITCH = "appSwitch";
    public static final int EVENT_CAST_SCENE_STARTED = 1;
    public static final int EVENT_CAST_SCENE_STOPPED = 2;
    public static final int EVENT_FRONT_APP_CHANGED = 3;
    private static final int INITIAL_CNT = -1;
    private static final String TAG = "AvSyncMonitor";
    private IHwActivityNotifierEx mActivityNotifierEx = new IHwActivityNotifierEx() {
        /* class com.huawei.android.server.wifi.cast.avsync.AvSyncMonitor.AnonymousClass2 */

        public void call(Bundle extras) {
            if (extras != null) {
                Object tempComp = extras.getParcelable("toActivity");
                ComponentName componentName = null;
                if (tempComp instanceof ComponentName) {
                    componentName = (ComponentName) tempComp;
                }
                int uid = extras.getInt("toUid");
                if (componentName != null && uid != -1) {
                    int oldUid = AvSyncMonitor.this.mCurrentTopUid;
                    String oldPackageName = AvSyncMonitor.this.mCurrentPackageName;
                    synchronized (AvSyncMonitor.this.mCurrentTopUidLock) {
                        AvSyncMonitor.this.mCurrentTopUid = uid;
                        AvSyncMonitor.this.mCurrentPackageName = componentName.getPackageName();
                    }
                    if (!TextUtils.isEmpty(AvSyncMonitor.this.mCurrentPackageName) && !AvSyncMonitor.this.mCurrentPackageName.equals(oldPackageName) && AvSyncMonitor.this.mCurrentTopUid != oldUid) {
                        AvSyncMonitor.this.mAvSyncListener.onAvSyncEvent(AvSyncMonitor.this.mCurrentPackageName, AvSyncMonitor.this.mCastType, 3);
                    }
                }
            }
        }
    };
    private IAvSyncListener mAvSyncListener;
    private ICastScene mCastListener = new ICastScene() {
        /* class com.huawei.android.server.wifi.cast.avsync.AvSyncMonitor.AnonymousClass1 */

        @Override // com.huawei.android.server.wifi.cast.ICastScene
        public void startCastScene(int castType) {
            AvSyncMonitor.this.mCastType = castType;
            AvSyncMonitor.this.mIsCastScene = true;
            if (AvSyncMonitor.this.mAvSyncListener != null) {
                AvSyncMonitor.this.mAvSyncListener.onAvSyncEvent(AvSyncMonitor.this.mCurrentPackageName, AvSyncMonitor.this.mCastType, 1);
            }
        }

        @Override // com.huawei.android.server.wifi.cast.ICastScene
        public void stopCastScene() {
            AvSyncMonitor.this.mCastType = -1;
            AvSyncMonitor.this.mIsCastScene = false;
            AvSyncMonitor.this.mCurrentPackageName = null;
            if (AvSyncMonitor.this.mAvSyncListener != null) {
                AvSyncMonitor.this.mAvSyncListener.onAvSyncEvent(AvSyncMonitor.this.mCurrentPackageName, AvSyncMonitor.this.mCastType, 2);
            }
        }
    };
    private int mCastType = -1;
    private String mCurrentPackageName = null;
    private int mCurrentTopUid = -1;
    private final Object mCurrentTopUidLock = new Object();
    private boolean mIsCastScene = false;

    public interface IAvSyncListener {
        void onAvSyncEvent(String str, int i, int i2);
    }

    public AvSyncMonitor(IAvSyncListener listener) {
        this.mAvSyncListener = listener;
    }

    public void init() {
        registerCastScenesListener();
        registerFrontAppListener();
    }

    private void registerFrontAppListener() {
        ActivityManagerEx.registerHwActivityNotifier(this.mActivityNotifierEx, APP_SWITCH);
    }

    private void registerCastScenesListener() {
        CastOptManager instance = CastOptManager.getInstance();
        if (instance != null) {
            instance.setCastSceneListener(this.mCastListener);
        } else {
            HwHiLog.i(TAG, false, "registerCastScenesListener failed", new Object[0]);
        }
    }

    public String getTopAppPkgName() {
        return this.mCurrentPackageName;
    }

    public boolean isCastScene() {
        return this.mIsCastScene;
    }

    public int getCastType() {
        return this.mCastType;
    }
}
