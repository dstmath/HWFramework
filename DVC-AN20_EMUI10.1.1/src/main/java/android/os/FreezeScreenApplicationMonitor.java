package android.os;

import android.app.Activity;
import android.os.FreezeScreenScene;
import android.util.ArrayMap;
import android.util.Log;
import android.util.ZRHung;
import android.view.WindowManager;
import com.android.internal.policy.PhoneWindow;
import java.lang.reflect.Field;
import java.util.HashSet;

public class FreezeScreenApplicationMonitor implements IFreezeScreenApplicationMonitor {
    public static final String TAG = "FreezeScreenApplicationMonitor";
    private static FreezeScreenApplicationMonitor mAppMonitor = null;
    private ArrayMap<String, Integer> mSceneMap;
    private TransparentActivityScene mTransparentActivityScene;

    public static synchronized FreezeScreenApplicationMonitor getInstance() {
        FreezeScreenApplicationMonitor freezeScreenApplicationMonitor;
        synchronized (FreezeScreenApplicationMonitor.class) {
            if (mAppMonitor == null) {
                mAppMonitor = new FreezeScreenApplicationMonitor();
            }
            freezeScreenApplicationMonitor = mAppMonitor;
        }
        return freezeScreenApplicationMonitor;
    }

    private FreezeScreenApplicationMonitor() {
        initScene();
        initSceneMap();
    }

    private void initScene() {
        this.mTransparentActivityScene = new TransparentActivityScene();
    }

    private void initSceneMap() {
        this.mSceneMap = new ArrayMap<>();
        this.mSceneMap.put(FreezeScreenScene.TRANSPARENT_ACTIVITY_SCENE_STRING, Integer.valueOf((int) FreezeScreenScene.TRANSPARENT_ACTIVITY_SCENE));
    }

    public void checkFreezeScreen(ArrayMap<String, Object> params) {
        if (params != null && (params.get(FreezeScreenScene.CHECK_TYPE_PARAM) instanceof String) && this.mSceneMap.get((String) params.get(FreezeScreenScene.CHECK_TYPE_PARAM)).intValue() == 907400009) {
            this.mTransparentActivityScene.scheduleCheckFreezeScreen(params);
        }
    }

    public void cancelCheckFreezeScreen(ArrayMap<String, Object> params) {
        if (this.mSceneMap.get((String) params.get(FreezeScreenScene.CHECK_TYPE_PARAM)).intValue() == 907400009) {
            this.mTransparentActivityScene.cancelCheckFreezeScreen(params);
        }
    }

    public static final class TransparentActivityScene extends FreezeScreenScene {
        private static final String IS_TRANSPARENT_FIELD = "mIsTransparent";
        private static final String PHONE_WINDOW_CLASS = "com.android.internal.policy.PhoneWindow";
        private Activity mCurCheckActivity = null;
        private HashSet<String> mFreezeScreenAppSet = new HashSet<>();

        @Override // android.os.FreezeScreenScene
        public boolean checkParamsValid(ArrayMap<String, Object> params) {
            if (params == null || !(params.get(FreezeScreenScene.LOOPER_PARAM) instanceof Looper) || !(params.get(FreezeScreenScene.TOKEN_PARAM) instanceof IBinder)) {
                return false;
            }
            if ((params.get("layoutParams") == null || (params.get("layoutParams") instanceof WindowManager.LayoutParams)) && (params.get("activity") instanceof Activity)) {
                return true;
            }
            return false;
        }

        @Override // android.os.FreezeScreenScene
        public synchronized void checkFreezeScreen(ArrayMap<String, Object> params) {
            if (checkParamsValid(params)) {
                this.mCurCheckActivity = (Activity) params.get("activity");
                if (this.mCurCheckActivity != null && this.mCurCheckActivity.getComponentName() != null) {
                    String transPackage = this.mCurCheckActivity.getComponentName().getPackageName();
                    String transActivity = this.mCurCheckActivity.getComponentName().getClassName();
                    if (isWinMayCauseFreezeScreen((WindowManager.LayoutParams) params.get("layoutParams")) && !this.mFreezeScreenAppSet.contains(transActivity)) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(transPackage);
                        sb.append("\n");
                        sb.append("TransparentActivityScene find freezeScreen, ");
                        sb.append("CURR_APP: ");
                        sb.append(transActivity);
                        if (transActivity != null) {
                            this.mFreezeScreenAppSet.add(transActivity);
                        }
                        Log.i(FreezeScreenApplicationMonitor.TAG, sb.toString());
                        ZRHung.sendHungEvent(14, null, sb.toString());
                    }
                }
            }
        }

        public synchronized void cancelCheckFreezeScreen(ArrayMap<String, Object> params) {
            if (checkParamsValid(params)) {
                if (this.mHandler != null) {
                    Log.d(FreezeScreenApplicationMonitor.TAG, "TransparentActivityScene cancelCheckFreezeScreen");
                    this.mHandler.removeMessages(1);
                }
            }
        }

        private final boolean isWinMayCauseFreezeScreen(WindowManager.LayoutParams l) {
            Field isTransparentField;
            if (l == null) {
                return false;
            }
            boolean isTransparent = false;
            if ((this.mCurCheckActivity.getWindow() instanceof PhoneWindow) && (isTransparentField = FreezeScreenScene.MonitorHelper.getReflectPrivateField(PHONE_WINDOW_CLASS, IS_TRANSPARENT_FIELD)) != null) {
                try {
                    isTransparent = ((Boolean) isTransparentField.get(this.mCurCheckActivity.getWindow())).booleanValue();
                } catch (IllegalAccessException e) {
                    Log.w(FreezeScreenApplicationMonitor.TAG, "getTransparentField Fail");
                }
            }
            if (l.alpha < 0.01f || isTransparent) {
                return true;
            }
            return false;
        }
    }
}
