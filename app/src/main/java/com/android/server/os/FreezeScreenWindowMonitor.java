package com.android.server.os;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.FreezeScreenScene;
import android.os.FreezeScreenScene.MonitorHelper;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;
import android.view.WindowManagerPolicy.WindowState;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.rms.shrinker.ProcessStopShrinker;
import com.android.server.wm.WindowManagerService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

public class FreezeScreenWindowMonitor implements IFreezeScreenWindowMonitor {
    public static final String TAG = "FreezeScreenWindowMonitor";
    private static FreezeScreenWindowMonitor mWinMonitor;
    private DisplayEventLostScene mDisplayEventLostScene;
    private FocusWindowNullScene mFocusWindowNullScene;
    private HighWindowLayerScene mHighWindowLayerScene;
    private ArrayMap<String, Integer> mSceneMap;

    public static class DisplayEventLostScene extends FreezeScreenScene {
        private String mProcessName;

        public boolean checkParamsValid(ArrayMap<String, Object> params) {
            if (params != null && (params.get("context") instanceof Context) && (params.get("looper") instanceof Looper) && (params.get(ProcessStopShrinker.PID_KEY) instanceof Integer)) {
                return true;
            }
            return false;
        }

        public void checkFreezeScreen(ArrayMap<String, Object> params) {
            if (checkParamsValid(params)) {
                int pid = ((Integer) params.get(ProcessStopShrinker.PID_KEY)).intValue();
                if (isAppAlive((Context) params.get("context"), pid)) {
                    Log.i(FreezeScreenWindowMonitor.TAG, "DisplayEventLostScene find FreezeScreen,mProcessName:" + this.mProcessName);
                    ArrayMap<String, Object> paramsRadar = new ArrayMap();
                    paramsRadar.put("checkType", Integer.valueOf(907400012));
                    paramsRadar.put(ProcessStopShrinker.PID_KEY, Integer.valueOf(pid));
                    paramsRadar.put("processName", this.mProcessName);
                    getFreezeScreenRadar().upload(paramsRadar);
                }
            }
        }

        private boolean isAppAlive(Context context, int pid) {
            List<RunningAppProcessInfo> appProcessList = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
            if (appProcessList == null) {
                return false;
            }
            for (RunningAppProcessInfo appProcess : appProcessList) {
                if (appProcess != null && pid == appProcess.pid) {
                    this.mProcessName = appProcess.processName;
                    return true;
                }
            }
            return false;
        }
    }

    public static class FocusWindowNullScene extends FreezeScreenScene {
        private static final String GET_FOCUSED_WINDOW_METHOD_NAME = "getFocusedWindow";
        private static final String PHONE_WINDOW_MANAGER_FIELD = "mPhoneWindowManager";
        private static final String WINDOW_MANAGER_SERVICE_CLASS = "com.android.server.wm.WindowManagerService";
        private String mFocusedActivity;

        public FocusWindowNullScene() {
            this.mFocusedActivity = null;
        }

        public void checkFreezeScreen(ArrayMap<String, Object> params) {
            if (checkParamsValid(params)) {
                processCurWinAndAppStatus((String) params.get("focusedActivityName"), (WindowManagerService) params.get("windowManager"));
            }
        }

        public synchronized void cancelCheckFreezeScreen(ArrayMap<String, Object> params) {
            if (checkCancelParamsValid(params)) {
                if (this.mHandler != null && ((String) params.get("anrActivityName")).equals(this.mFocusedActivity)) {
                    Log.d(FreezeScreenWindowMonitor.TAG, "FocusWindowNullScene ANR appear and cancelCheckFreezeScreen");
                    this.mHandler.removeMessages(1);
                }
            }
        }

        public boolean checkCancelParamsValid(ArrayMap<String, Object> params) {
            if (params != null && (params.get("anrActivityName") instanceof String)) {
                return true;
            }
            return false;
        }

        public boolean checkParamsValid(ArrayMap<String, Object> params) {
            if (params != null && (params.get("looper") instanceof Looper) && (params.get("focusedActivityName") instanceof String) && (params.get("windowManager") instanceof WindowManagerService)) {
                return true;
            }
            return false;
        }

        private synchronized void processCurWinAndAppStatus(String focusedActivity, WindowManagerService windowManager) {
            Method method = MonitorHelperExtend.getReflectPrivateMethod(WINDOW_MANAGER_SERVICE_CLASS, GET_FOCUSED_WINDOW_METHOD_NAME);
            if (method != null) {
                try {
                    if (((WindowState) method.invoke(windowManager, new Object[0])) == null) {
                        this.mFocusedActivity = focusedActivity;
                        Log.i(FreezeScreenWindowMonitor.TAG, "FocusWindowNullScene find FreezeScreen,mFocusedActivity:" + this.mFocusedActivity);
                        ArrayMap<String, Object> paramsRadar = new ArrayMap();
                        paramsRadar.put("checkType", Integer.valueOf(907400013));
                        paramsRadar.put("focusedActivityName", focusedActivity);
                        getFreezeScreenRadar().upload(paramsRadar);
                    }
                } catch (IllegalAccessException e) {
                } catch (InvocationTargetException e2) {
                }
            }
        }
    }

    public static final class HighWindowLayerScene extends FreezeScreenScene {
        private static final long AUTO_CHECK_MIN_INTERVAL_TIME = 60000;
        private static long sLastAutoUploadTime;
        private WindowState mCurWindowState;
        private int mNumber;

        public synchronized void checkFreezeScreen(ArrayMap<String, Object> params) {
            if (!checkParamsValid(params)) {
                return;
            }
            if (checkFreq(params)) {
                checkFreezeScreen((WindowState) params.get("windowState"));
            }
        }

        public boolean checkParamsValid(ArrayMap<String, Object> params) {
            if (params == null || !(params.get("windowState") instanceof WindowState)) {
                return false;
            }
            return true;
        }

        public void checkFreezeScreen(WindowState curWin) {
            this.mCurWindowState = curWin;
            if (isWinMayCauseFreezeScreen(curWin)) {
                String windowState = this.mCurWindowState.toString();
                Log.i(FreezeScreenWindowMonitor.TAG, "HighWindowLayerScene find FreezeScreen,mCurWindowState:" + windowState);
                ArrayMap<String, Object> paramsRadar = new ArrayMap();
                paramsRadar.put("checkType", Integer.valueOf(907400011));
                paramsRadar.put("highLevelWindowName", windowState);
                getFreezeScreenRadar().upload(paramsRadar);
            }
        }

        private boolean checkFreq(ArrayMap<String, Object> params) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - sLastAutoUploadTime <= AUTO_CHECK_MIN_INTERVAL_TIME || !(params.get("newCirCle") instanceof Boolean) || !(params.get("number") instanceof Integer)) {
                return false;
            }
            if (((Boolean) params.get("newCircle")).booleanValue()) {
                this.mNumber = ((Integer) params.get("number")).intValue();
                Log.i(FreezeScreenWindowMonitor.TAG, "HighWindowLayerScene start to check");
            }
            int i = this.mNumber;
            this.mNumber = i - 1;
            if (i == 0) {
                sLastAutoUploadTime = currentTime;
            }
            return true;
        }

        private boolean isWinMayCauseFreezeScreen(WindowState curWin) {
            if (isWinLayerAboveSystem(curWin) && isFullWindow(curWin) && ((!isTouchModal(curWin) || !isTouchable(curWin)) && isWindowVisible(curWin))) {
                return hasInputChannel(curWin);
            }
            return false;
        }

        private boolean isWinLayerAboveSystem(WindowState curWin) {
            return MonitorHelperExtend.windowTypeToLayerLw(curWin.getAttrs().type) >= MonitorHelperExtend.windowTypeToLayerLw(2003);
        }

        private boolean isFullWindow(WindowState curWin) {
            return MonitorHelperExtend.isFullWindow(curWin);
        }

        private boolean isTouchable(WindowState curWin) {
            return (curWin.getAttrs().flags & 16) == 0;
        }

        private boolean isTouchModal(WindowState curWin) {
            return (curWin.getAttrs().flags & 40) == 0;
        }

        private boolean isWindowVisible(WindowState curWin) {
            return curWin.isVisibleOrBehindKeyguardLw();
        }

        private boolean hasInputChannel(WindowState curWin) {
            return (curWin.getAttrs().inputFeatures & 2) == 0;
        }
    }

    public static class MonitorHelperExtend extends MonitorHelper {

        /* renamed from: com.android.server.os.FreezeScreenWindowMonitor.MonitorHelperExtend.1 */
        static class AnonymousClass1 implements PrivilegedAction {
            final /* synthetic */ Method val$method;

            AnonymousClass1(Method val$method) {
                this.val$method = val$method;
            }

            public Object run() {
                this.val$method.setAccessible(true);
                return null;
            }
        }

        public static int windowTypeToLayerLw(int type) {
            return HwPolicyFactory.getHwPhoneWindowManager().windowTypeToLayerLw(type);
        }

        public static boolean isFullWindow(WindowState win) {
            if (win == null) {
                return false;
            }
            return isFullWindow(win.getAttrs());
        }

        public static Class<?> getWMSClass(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                Log.e(FreezeScreenWindowMonitor.TAG, e.toString());
                return null;
            }
        }

        public static Method getReflectPrivateMethod(String className, String methodName) {
            Class<?> cls = getWMSClass(className);
            if (cls != null) {
                try {
                    Method method = cls.getDeclaredMethod(methodName, new Class[0]);
                    AccessController.doPrivileged(new AnonymousClass1(method));
                    return method;
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    static {
        mWinMonitor = null;
    }

    public static synchronized FreezeScreenWindowMonitor getInstance() {
        FreezeScreenWindowMonitor freezeScreenWindowMonitor;
        synchronized (FreezeScreenWindowMonitor.class) {
            if (mWinMonitor == null) {
                mWinMonitor = new FreezeScreenWindowMonitor();
            }
            freezeScreenWindowMonitor = mWinMonitor;
        }
        return freezeScreenWindowMonitor;
    }

    private FreezeScreenWindowMonitor() {
        initScene();
        initSceneMap();
    }

    private void initScene() {
        this.mHighWindowLayerScene = new HighWindowLayerScene();
        this.mDisplayEventLostScene = new DisplayEventLostScene();
        this.mFocusWindowNullScene = new FocusWindowNullScene();
    }

    private void initSceneMap() {
        this.mSceneMap = new ArrayMap();
        this.mSceneMap.put("HighWindowLayerScene", Integer.valueOf(907400011));
        this.mSceneMap.put("DisplayEventLostScene", Integer.valueOf(907400012));
        this.mSceneMap.put("FocusWindowNullScene", Integer.valueOf(907400013));
    }

    public void checkFreezeScreen(ArrayMap<String, Object> params) {
        if (params != null && (params.get("checkType") instanceof String)) {
            switch (((Integer) this.mSceneMap.get((String) params.get("checkType"))).intValue()) {
                case 907400011:
                    this.mHighWindowLayerScene.checkFreezeScreen((ArrayMap) params);
                    break;
                case 907400012:
                    this.mDisplayEventLostScene.scheduleCheckFreezeScreen(params);
                    break;
                case 907400013:
                    this.mFocusWindowNullScene.scheduleCheckFreezeScreen(params);
                    break;
            }
        }
    }

    public void cancelCheckFreezeScreen(ArrayMap<String, Object> params) {
        switch (((Integer) this.mSceneMap.get((String) params.get("checkType"))).intValue()) {
            case 907400013:
                this.mFocusWindowNullScene.cancelCheckFreezeScreen(params);
            default:
        }
    }
}
