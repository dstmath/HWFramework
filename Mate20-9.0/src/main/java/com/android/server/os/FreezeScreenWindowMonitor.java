package com.android.server.os;

import android.app.ActivityManager;
import android.content.Context;
import android.os.FreezeScreenScene;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.Log;
import android.util.ZRHung;
import com.android.server.hidata.hinetwork.HwHiNetworkParmStatistics;
import com.android.server.policy.HwPolicyFactory;
import com.android.server.policy.WindowManagerPolicy;
import com.android.server.wm.WindowManagerService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;

public class FreezeScreenWindowMonitor implements IFreezeScreenWindowMonitor {
    public static final String TAG = "FreezeScreenWindowMonitor";
    private static FreezeScreenWindowMonitor mWinMonitor = null;
    private DisplayEventLostScene mDisplayEventLostScene;
    private FocusWindowErrorScene mFocusWindowErrorScene;
    private FocusWindowNullScene mFocusWindowNullScene;
    private HighWindowLayerScene mHighWindowLayerScene;
    private ArrayMap<String, Integer> mSceneMap;

    public static class DisplayEventLostScene extends FreezeScreenScene {
        private String mProcessName;

        public boolean checkParamsValid(ArrayMap<String, Object> params) {
            if (params != null && (params.get("context") instanceof Context) && (params.get("looper") instanceof Looper) && (params.get("pid") instanceof Integer)) {
                return true;
            }
            return false;
        }

        public void checkFreezeScreen(ArrayMap<String, Object> params) {
            if (checkParamsValid(params)) {
                int pid = ((Integer) params.get("pid")).intValue();
                if (isAppAlive((Context) params.get("context"), pid)) {
                    Log.i(FreezeScreenWindowMonitor.TAG, "DisplayEventLostScene find FreezeScreen,mProcessName:" + this.mProcessName);
                    ArrayMap<String, Object> paramsRadar = new ArrayMap<>();
                    paramsRadar.put("checkType", 907400012);
                    paramsRadar.put("pid", Integer.valueOf(pid));
                    paramsRadar.put("processName", this.mProcessName);
                    getFreezeScreenRadar().upload(paramsRadar);
                }
            }
        }

        private boolean isAppAlive(Context context, int pid) {
            List<ActivityManager.RunningAppProcessInfo> appProcessList = ((ActivityManager) context.getSystemService("activity")).getRunningAppProcesses();
            if (appProcessList == null) {
                return false;
            }
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcessList) {
                if (appProcess != null && pid == appProcess.pid) {
                    this.mProcessName = appProcess.processName;
                    return true;
                }
            }
            return false;
        }
    }

    public static class FocusWindowErrorScene extends FreezeScreenScene {
        private static final String GET_FOCUSED_WINDOW_METHOD_NAME = "getFocusedWindow";
        private static final String NULL_STRING = "null";
        private static final String PHONE_WINDOW_MANAGER_FIELD = "mPhoneWindowManager";
        private static final String SEPARATOR_CODE = "/";
        private static final String WINDOW_MANAGER_SERVICE_CLASS = "com.android.server.wm.WindowManagerService";
        private String mFocusedActivity = null;
        private String mFocusedActivityHashCode = null;
        private String mFocusedPackage = null;
        private String mFocusedWindow = null;
        private String mFocusedWindowHashCode = null;

        public void checkFreezeScreen(ArrayMap<String, Object> params) {
            if (checkParamsValid(params)) {
                if ("null".equals(this.mFocusedActivity) || "null".equals(this.mFocusedWindow) || (!this.mFocusedActivityHashCode.equals(this.mFocusedWindowHashCode) && this.mFocusedWindow.contains(SEPARATOR_CODE))) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(this.mFocusedPackage);
                    sb.append("\n");
                    sb.append("FocusWindowErrorScene find freezeScreen, ");
                    sb.append("FOCUS_WINDOW: ");
                    sb.append(this.mFocusedWindow);
                    sb.append(",FOCUS_APP: ");
                    sb.append(this.mFocusedActivity);
                    Log.i(FreezeScreenWindowMonitor.TAG, sb.toString());
                    ZRHung.sendHungEvent(15, null, sb.toString());
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x004d, code lost:
            return;
         */
        public synchronized void cancelCheckFreezeScreen(ArrayMap<String, Object> params) {
            if (checkParamsValid(params)) {
                this.mFocusedWindow = (String) params.get("focusedWindowName");
                this.mFocusedWindowHashCode = (String) params.get("focusedWindowHashCode");
                this.mFocusedPackage = (String) params.get("focusedPackageName");
                this.mFocusedActivity = (String) params.get("focusedActivityName");
                this.mFocusedActivityHashCode = (String) params.get("focusedActivityHashCode");
                if (this.mHandler != null) {
                    Log.d(FreezeScreenWindowMonitor.TAG, "FocusWindowErrorScene cancelCheckFreezeScreen");
                    this.mHandler.removeMessages(2);
                }
            }
        }

        public boolean checkCancelParamsValid(ArrayMap<String, Object> params) {
            if (params != null && (params.get("looper") instanceof Looper)) {
                return true;
            }
            return false;
        }

        public boolean checkParamsValid(ArrayMap<String, Object> params) {
            if (params == null || !(params.get("looper") instanceof Looper) || !(params.get("focusedWindowName") instanceof String) || !(params.get("focusedWindowHashCode") instanceof String) || !(params.get("focusedPackageName") instanceof String) || !(params.get("focusedActivityName") instanceof String) || !(params.get("focusedActivityHashCode") instanceof String)) {
                return false;
            }
            this.mFocusedWindow = (String) params.get("focusedWindowName");
            this.mFocusedWindowHashCode = (String) params.get("focusedWindowHashCode");
            this.mFocusedPackage = (String) params.get("focusedPackageName");
            this.mFocusedActivity = (String) params.get("focusedActivityName");
            this.mFocusedActivityHashCode = (String) params.get("focusedActivityHashCode");
            return true;
        }
    }

    public static class FocusWindowNullScene extends FreezeScreenScene {
        private static final String GET_FOCUSED_WINDOW_METHOD_NAME = "getFocusedWindow";
        private static final String PHONE_WINDOW_MANAGER_FIELD = "mPhoneWindowManager";
        private static final String WINDOW_MANAGER_SERVICE_CLASS = "com.android.server.wm.WindowManagerService";
        private String mFocusedActivity = null;

        public void checkFreezeScreen(ArrayMap<String, Object> params) {
            if (checkParamsValid(params)) {
                processCurWinAndAppStatus((String) params.get("focusedActivityName"), (WindowManagerService) params.get("windowManager"));
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002b, code lost:
            return;
         */
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
                    if (((WindowManagerPolicy.WindowState) method.invoke(windowManager, new Object[0])) == null) {
                        this.mFocusedActivity = focusedActivity;
                        Log.i(FreezeScreenWindowMonitor.TAG, "FocusWindowNullScene find FreezeScreen,mFocusedActivity:" + this.mFocusedActivity);
                        ArrayMap<String, Object> paramsRadar = new ArrayMap<>();
                        paramsRadar.put("checkType", 907400013);
                        paramsRadar.put("focusedActivityName", focusedActivity);
                        getFreezeScreenRadar().upload(paramsRadar);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                }
            }
        }
    }

    public static final class HighWindowLayerScene extends FreezeScreenScene {
        private static final long AUTO_CHECK_MIN_INTERVAL_TIME = 60000;
        private static long sLastAutoUploadTime;
        private WindowManagerPolicy.WindowState mCurWindowState;
        private int mNumber;

        public synchronized void checkFreezeScreen(ArrayMap<String, Object> params) {
            if (checkParamsValid(params)) {
                if (checkFreq(params)) {
                    checkFreezeScreen((WindowManagerPolicy.WindowState) params.get("windowState"));
                }
            }
        }

        public boolean checkParamsValid(ArrayMap<String, Object> params) {
            if (params == null || !(params.get("windowState") instanceof WindowManagerPolicy.WindowState)) {
                return false;
            }
            return true;
        }

        public void checkFreezeScreen(WindowManagerPolicy.WindowState curWin) {
            this.mCurWindowState = curWin;
            if (isWinMayCauseFreezeScreen(curWin)) {
                String windowState = this.mCurWindowState.toString();
                Log.i(FreezeScreenWindowMonitor.TAG, "HighWindowLayerScene find FreezeScreen,mCurWindowState:" + windowState);
                ArrayMap<String, Object> paramsRadar = new ArrayMap<>();
                paramsRadar.put("checkType", 907400011);
                paramsRadar.put("highLevelWindowName", windowState);
                getFreezeScreenRadar().upload(paramsRadar);
            }
        }

        private boolean checkFreq(ArrayMap<String, Object> params) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - sLastAutoUploadTime <= 60000 || !(params.get("newCirCle") instanceof Boolean) || !(params.get(HwHiNetworkParmStatistics.NUMBER) instanceof Integer)) {
                return false;
            }
            if (((Boolean) params.get("newCircle")).booleanValue()) {
                this.mNumber = ((Integer) params.get(HwHiNetworkParmStatistics.NUMBER)).intValue();
                Log.i(FreezeScreenWindowMonitor.TAG, "HighWindowLayerScene start to check");
            }
            int i = this.mNumber;
            this.mNumber = i - 1;
            if (i == 0) {
                sLastAutoUploadTime = currentTime;
            }
            return true;
        }

        private boolean isWinMayCauseFreezeScreen(WindowManagerPolicy.WindowState curWin) {
            return isWinLayerAboveSystem(curWin) && isFullWindow(curWin) && (!isTouchModal(curWin) || !isTouchable(curWin)) && isWindowVisible(curWin) && hasInputChannel(curWin);
        }

        private boolean isWinLayerAboveSystem(WindowManagerPolicy.WindowState curWin) {
            return MonitorHelperExtend.windowTypeToLayerLw(curWin.getAttrs().type) >= MonitorHelperExtend.windowTypeToLayerLw(2003);
        }

        private boolean isFullWindow(WindowManagerPolicy.WindowState curWin) {
            return MonitorHelperExtend.isFullWindow(curWin);
        }

        private boolean isTouchable(WindowManagerPolicy.WindowState curWin) {
            return (curWin.getAttrs().flags & 16) == 0;
        }

        private boolean isTouchModal(WindowManagerPolicy.WindowState curWin) {
            return (curWin.getAttrs().flags & 40) == 0;
        }

        private boolean isWindowVisible(WindowManagerPolicy.WindowState curWin) {
            return false;
        }

        private boolean hasInputChannel(WindowManagerPolicy.WindowState curWin) {
            return (curWin.getAttrs().inputFeatures & 2) == 0;
        }
    }

    public static class MonitorHelperExtend extends FreezeScreenScene.MonitorHelper {
        public static int windowTypeToLayerLw(int type) {
            return HwPolicyFactory.getHwPhoneWindowManager().getWindowLayerFromTypeLw(type);
        }

        public static boolean isFullWindow(WindowManagerPolicy.WindowState win) {
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
                    final Method method = cls.getDeclaredMethod(methodName, new Class[0]);
                    AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            method.setAccessible(true);
                            return null;
                        }
                    });
                    return method;
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
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
        this.mFocusWindowErrorScene = new FocusWindowErrorScene();
    }

    private void initSceneMap() {
        this.mSceneMap = new ArrayMap<>();
        this.mSceneMap.put("HighWindowLayerScene", 907400011);
        this.mSceneMap.put("DisplayEventLostScene", 907400012);
        this.mSceneMap.put("FocusWindowNullScene", 907400013);
        this.mSceneMap.put("FocusWindowErrorScene", 907400014);
    }

    public void checkFreezeScreen(ArrayMap<String, Object> params) {
        if (params != null && (params.get("checkType") instanceof String)) {
            switch (this.mSceneMap.get((String) params.get("checkType")).intValue()) {
                case 907400011:
                    this.mHighWindowLayerScene.checkFreezeScreen(params);
                    break;
                case 907400012:
                    this.mDisplayEventLostScene.scheduleCheckFreezeScreen(params);
                    break;
                case 907400013:
                    this.mFocusWindowNullScene.scheduleCheckFreezeScreen(params);
                    break;
                case 907400014:
                    this.mFocusWindowErrorScene.scheduleCheckFreezeScreen(params);
                    break;
            }
        }
    }

    public void cancelCheckFreezeScreen(ArrayMap<String, Object> params) {
        switch (this.mSceneMap.get((String) params.get("checkType")).intValue()) {
            case 907400013:
                this.mFocusWindowNullScene.cancelCheckFreezeScreen(params);
                return;
            case 907400014:
                this.mFocusWindowErrorScene.cancelCheckFreezeScreen(params);
                return;
            default:
                return;
        }
    }
}
