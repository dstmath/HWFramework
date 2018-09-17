package android.os;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.ArrayMap;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import android.util.ZRHung;
import android.util.ZRHung.HungConfig;
import android.view.WindowManager.LayoutParams;
import huawei.android.provider.HwSettings.System;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class FreezeScreenScene {
    public static final String ACTIVITY_PARAM = "activity";
    public static final String ANR_ACTIVITY_NAME = "anrActivityName";
    public static final String BLOCK_IME_PACKAGE_NAME = "blockPackageName";
    private static final int CHECK_FREEZE_SCREEN_DELAY_TIME = 20000;
    public static final int CHECK_FREEZE_SCREEN_FOCUS_WINDOW_ERROR_MSG = 2;
    public static final int CHECK_FREEZE_SCREEN_MSG = 1;
    public static final String CHECK_TYPE_PARAM = "checkType";
    public static final String CONTEXT_PARAM = "context";
    public static final int DISPLAY_EVENT_LOST_SCENE = 907400012;
    public static final String DISPLAY_EVENT_LOST_SCENE_STRING = "DisplayEventLostScene";
    public static final String FOCUS_ACTIVITY_HASH_CODE_PARAM = "focusedActivityHashCode";
    public static final String FOCUS_ACTIVITY_PARAM = "focusedActivityName";
    public static final String FOCUS_PACKAGE_PARAM = "focusedPackageName";
    public static final int FOCUS_WINDOW_ERROR_SCENE = 907400014;
    public static final String FOCUS_WINDOW_ERROR_SCENE_STRING = "FocusWindowErrorScene";
    public static final String FOCUS_WINDOW_HASH_CODE_PARAM = "focusedWindowHashCode";
    public static final int FOCUS_WINDOW_NULL_SCENE = 907400013;
    public static final String FOCUS_WINDOW_NULL_SCENE_STRING = "FocusWindowNullScene";
    public static final String FOCUS_WINDOW_PARAM = "focusedWindowName";
    public static final String HIGH_LEVEL_WINDOW_NAME_PARAM = "highLevelWindowName";
    public static final int HIGH_WINDOW_LAYER_SCENE = 907400011;
    public static final String HIGH_WINDOW_LAYER_SCENE_STRING = "HighWindowLayerScene";
    public static final String HUNG_CONFIG_ENABLE = "1";
    public static final int IMOINITOR_RADAR_TYPE = 0;
    public static final boolean IS_DEBUG_VERSION;
    public static final String LAYOUT_PARAM = "layoutParams";
    public static final int LOG_EXCEPTION_RADAR_TYPE = 1;
    public static final String LOOPER_PARAM = "looper";
    public static final String PID_PARAM = "pid";
    public static final String PROCESS_NAME = "processName";
    private static final String TAG = "FreezeScreenScene";
    public static final String TOKEN_PARAM = "token";
    public static final int TRANSPARENT_ACTIVITY_SCENE = 907400009;
    public static final String TRANSPARENT_ACTIVITY_SCENE_STRING = "TransparentActivityScene";
    public static final String WINDOW_MANAGER_PARAM = "windowManager";
    public static final String WINDOW_PARAM = "window";
    public static final String WINDOW_STATE_PARAM = "windowState";
    private static int mCheckFreezeScreenDelayTime = CHECK_FREEZE_SCREEN_DELAY_TIME;
    public FreezeScreenRadar mFreezeScreenRadar = null;
    public Handler mHandler = null;
    private HungConfig mHungConfigFocusWindow = null;
    private HungConfig mHungConfigTransWindow = null;

    public static abstract class FreezeScreenRadar {
        public void upload(ArrayMap<String, Object> arrayMap) {
            Log.i(FreezeScreenScene.TAG, "FreezeScreenRadar");
        }
    }

    private class FreezeScreenSceneHandler extends Handler {
        public FreezeScreenSceneHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Log.d(FreezeScreenScene.TAG, "handleMessage CHECK_FREEZE_SCREEN_MSG");
                    FreezeScreenScene.this.checkFreezeScreen((ArrayMap) msg.obj);
                    return;
                case 2:
                    Log.d(FreezeScreenScene.TAG, "handleMessage CHECK_FREEZE_SCREEN_FOCUS_WINDOW_ERROR_MSG");
                    FreezeScreenScene.this.checkFreezeScreen((ArrayMap) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public static class IMonitorRadar extends FreezeScreenRadar {
        private static final long AUTO_UPLOAD_MIN_INTERVAL_TIME = 43200000;
        private static long sLastAutoUploadTime = 0;

        public void upload(ArrayMap<String, Object> params) {
            Log.i(FreezeScreenScene.TAG, "IMonitorRadar");
            long currentTime = System.currentTimeMillis();
            if (currentTime - sLastAutoUploadTime <= AUTO_UPLOAD_MIN_INTERVAL_TIME) {
                Log.d(FreezeScreenScene.TAG, "Upload too frequently, just discard it.");
                return;
            }
            sLastAutoUploadTime = currentTime;
            if (params != null && ((params.get(FreezeScreenScene.CHECK_TYPE_PARAM) instanceof Integer) ^ 1) == 0) {
                int checkType = ((Integer) params.get(FreezeScreenScene.CHECK_TYPE_PARAM)).intValue();
                EventStream eStream = IMonitor.openEventStream(checkType);
                switch (checkType) {
                    case FreezeScreenScene.TRANSPARENT_ACTIVITY_SCENE /*907400009*/:
                        eStream.setParam((short) 0, MonitorHelper.getPackageName()).setParam((short) 1, MonitorHelper.getVersionName()).setParam((short) 2, (String) params.get(FreezeScreenScene.WINDOW_PARAM));
                        break;
                    case FreezeScreenScene.HIGH_WINDOW_LAYER_SCENE /*907400011*/:
                        eStream.setParam((short) 0, MonitorHelper.getPackageName()).setParam((short) 1, MonitorHelper.getVersionName()).setParam((short) 2, (String) params.get(FreezeScreenScene.HIGH_LEVEL_WINDOW_NAME_PARAM));
                        break;
                    case FreezeScreenScene.DISPLAY_EVENT_LOST_SCENE /*907400012*/:
                        eStream.setParam((short) 0, MonitorHelper.getPackageName()).setParam((short) 1, MonitorHelper.getVersionName()).setParam((short) 2, ((Integer) params.get(FreezeScreenScene.PID_PARAM)).intValue()).setParam((short) 3, (String) params.get(FreezeScreenScene.PROCESS_NAME));
                        break;
                    case FreezeScreenScene.FOCUS_WINDOW_NULL_SCENE /*907400013*/:
                        eStream.setParam((short) 0, MonitorHelper.getPackageName()).setParam((short) 1, MonitorHelper.getVersionName()).setParam((short) 2, (String) params.get(FreezeScreenScene.FOCUS_ACTIVITY_PARAM));
                        break;
                }
                IMonitor.sendEvent(eStream);
                IMonitor.closeEventStream(eStream);
            }
        }
    }

    public static class MonitorHelper {
        public static boolean isFullWindow(LayoutParams attrs) {
            if (attrs == null) {
                return false;
            }
            boolean hasFullScreenFlag = (attrs.flags & 1024) != 0;
            boolean isMatchParentWH = (isAppWindow(attrs) && attrs.x == 0 && attrs.y == 0 && -1 == attrs.width) ? -1 == attrs.height : false;
            if (hasFullScreenFlag) {
                isMatchParentWH = true;
            }
            return isMatchParentWH;
        }

        public static boolean isAppWindow(LayoutParams attrs) {
            if ((attrs.type >= 1 && attrs.type < System.AUTO_HIDE_NAVIGATIONBAR_TIMEOUT_DEFAULT) || attrs.type == 2100 || attrs.type == 2101) {
                return true;
            }
            return false;
        }

        public static Field getReflectPrivateField(String className, String fieldName) {
            try {
                final Field field = Class.forName(className).getDeclaredField(fieldName);
                AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        field.setAccessible(true);
                        return null;
                    }
                });
                return field;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchFieldException e2) {
                e2.printStackTrace();
                return null;
            }
        }

        public static Class<?> getClass(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                Log.e(FreezeScreenScene.TAG, e.toString());
                return null;
            }
        }

        private static String getPackageName() {
            String packageName = ActivityThread.currentPackageName();
            if (packageName == null) {
                return "system_server";
            }
            return packageName;
        }

        private static int getVersionCode() {
            if (ActivityThread.currentApplication() != null) {
                return ActivityThread.currentApplication().getApplicationContext().getApplicationInfo().versionCode;
            }
            return 0;
        }

        private static String getVersionName() {
            try {
                if (ActivityThread.currentApplication() != null) {
                    Context context = ActivityThread.currentApplication().getApplicationContext();
                    if (context != null) {
                        PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                        if (!(info == null || info.versionName == null)) {
                            return info.versionName;
                        }
                    }
                }
            } catch (Exception e) {
                Log.w(FreezeScreenScene.TAG, "Could not get package info", e);
            }
            return getVersionCode() + "";
        }
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    public void checkFreezeScreen(ArrayMap<String, Object> arrayMap) {
    }

    /* JADX WARNING: Removed duplicated region for block: B:69:0x00f7 A:{Catch:{ NumberFormatException -> 0x00a8 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void scheduleCheckFreezeScreen(ArrayMap<String, Object> params) {
        int i = CHECK_FREEZE_SCREEN_DELAY_TIME;
        synchronized (this) {
            if (checkParamsValid(params)) {
                if (this.mHandler == null) {
                    Log.i(TAG, "scheduleCheckFreezeScreen new FreezeScreenSceneHandler");
                    this.mHandler = new FreezeScreenSceneHandler((Looper) params.get(LOOPER_PARAM));
                    mCheckFreezeScreenDelayTime = CHECK_FREEZE_SCREEN_DELAY_TIME;
                }
                String type = (String) params.get(CHECK_TYPE_PARAM);
                Message msg = null;
                int status;
                String[] value;
                if (FOCUS_WINDOW_ERROR_SCENE_STRING.equals(type)) {
                    if (this.mHungConfigFocusWindow == null || this.mHungConfigFocusWindow.status > 0) {
                        this.mHungConfigFocusWindow = ZRHung.getHungConfig((short) 15);
                    }
                    if (this.mHungConfigFocusWindow != null) {
                        status = this.mHungConfigFocusWindow.status;
                        value = this.mHungConfigFocusWindow.value.split(",");
                        if (value != null) {
                            try {
                                if (value.length >= 2) {
                                    i = Integer.parseInt(value[1]);
                                }
                            } catch (NumberFormatException e) {
                                mCheckFreezeScreenDelayTime = CHECK_FREEZE_SCREEN_DELAY_TIME;
                            }
                        }
                        mCheckFreezeScreenDelayTime = i;
                        if (status == 0 && value != null) {
                            if (HUNG_CONFIG_ENABLE.equals(value[0])) {
                                this.mHandler.removeMessages(2);
                                msg = this.mHandler.obtainMessage(2);
                            }
                        }
                    }
                } else if (TRANSPARENT_ACTIVITY_SCENE_STRING.equals(type)) {
                    if (this.mHungConfigTransWindow == null || this.mHungConfigTransWindow.status > 0) {
                        this.mHungConfigTransWindow = ZRHung.getHungConfig((short) 14);
                    }
                    if (this.mHungConfigTransWindow != null) {
                        int parseInt;
                        status = this.mHungConfigTransWindow.status;
                        value = this.mHungConfigTransWindow.value.split(",");
                        if (value != null) {
                            try {
                                if (value.length >= 2) {
                                    parseInt = Integer.parseInt(value[1]);
                                    mCheckFreezeScreenDelayTime = parseInt;
                                    if (status == 0 && value != null) {
                                        if (HUNG_CONFIG_ENABLE.equals(value[0])) {
                                            this.mHandler.removeMessages(1);
                                            msg = this.mHandler.obtainMessage(1);
                                        }
                                    }
                                }
                            } catch (NumberFormatException e2) {
                                mCheckFreezeScreenDelayTime = CHECK_FREEZE_SCREEN_DELAY_TIME;
                            }
                        }
                        parseInt = CHECK_FREEZE_SCREEN_DELAY_TIME;
                        mCheckFreezeScreenDelayTime = parseInt;
                        if (HUNG_CONFIG_ENABLE.equals(value[0])) {
                        }
                    }
                } else {
                    this.mHandler.removeMessages(1);
                    msg = this.mHandler.obtainMessage(1);
                }
                if (msg != null) {
                    msg.obj = params;
                    this.mHandler.sendMessageDelayed(msg, (long) mCheckFreezeScreenDelayTime);
                }
                Log.i(TAG, "scheduleCheckFreezeScreen sendMessageDelayed");
                return;
            }
            return;
        }
    }

    public boolean checkParamsValid(ArrayMap<String, Object> arrayMap) {
        return true;
    }

    public synchronized FreezeScreenRadar getFreezeScreenRadar() {
        if (this.mFreezeScreenRadar == null) {
            this.mFreezeScreenRadar = new IMonitorRadar();
        }
        return this.mFreezeScreenRadar;
    }
}
