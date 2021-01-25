package android.os;

import android.app.ActivityThread;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.ArrayMap;
import android.util.IMonitor;
import android.util.Log;
import android.util.ZRHung;
import android.view.WindowManager;
import com.huawei.android.os.storage.StorageManagerExt;
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
    public static final int DOMESTIC_BETA = 3;
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
    private static final int VALUE_LENGTH_LIMIT = 2;
    public static final String WINDOW_MANAGER_PARAM = "windowManager";
    public static final String WINDOW_PARAM = "window";
    public static final String WINDOW_STATE_PARAM = "windowState";
    public FreezeScreenRadar mFreezeScreenRadar = null;
    public Handler mHandler = null;
    private ZRHung.HungConfig mHungConfigFocusWindow = null;
    private ZRHung.HungConfig mHungConfigTransWindow = null;

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    public void checkFreezeScreen(ArrayMap<String, Object> arrayMap) {
    }

    public synchronized void scheduleCheckFreezeScreen(ArrayMap<String, Object> params) {
        if (params != null) {
            int checkFreezeScreenDelayTime = CHECK_FREEZE_SCREEN_DELAY_TIME;
            if (checkParamsValid(params)) {
                if (this.mHandler == null) {
                    Log.i(TAG, "scheduleCheckFreezeScreen new FreezeScreenSceneHandler");
                    this.mHandler = new FreezeScreenSceneHandler((Looper) params.get(LOOPER_PARAM));
                    checkFreezeScreenDelayTime = CHECK_FREEZE_SCREEN_DELAY_TIME;
                }
                String type = (String) params.get(CHECK_TYPE_PARAM);
                Message msg = null;
                boolean equals = FOCUS_WINDOW_ERROR_SCENE_STRING.equals(type);
                int i = CHECK_FREEZE_SCREEN_DELAY_TIME;
                if (equals) {
                    if (this.mHungConfigFocusWindow == null || this.mHungConfigFocusWindow.status > 0) {
                        this.mHungConfigFocusWindow = ZRHung.getHungConfig(15);
                    }
                    if (this.mHungConfigFocusWindow != null) {
                        int status = this.mHungConfigFocusWindow.status;
                        String[] value = this.mHungConfigFocusWindow.value.split(",");
                        if (value != null) {
                            try {
                                if (value.length >= 2) {
                                    i = Integer.parseInt(value[1]);
                                }
                            } catch (NumberFormatException e) {
                                checkFreezeScreenDelayTime = CHECK_FREEZE_SCREEN_DELAY_TIME;
                            }
                        }
                        checkFreezeScreenDelayTime = i;
                        if (status == 0 && value != null && "1".equals(value[0])) {
                            this.mHandler.removeMessages(2);
                            msg = this.mHandler.obtainMessage(2);
                        }
                    }
                } else if (TRANSPARENT_ACTIVITY_SCENE_STRING.equals(type)) {
                    if (this.mHungConfigTransWindow == null || this.mHungConfigTransWindow.status > 0) {
                        this.mHungConfigTransWindow = ZRHung.getHungConfig(14);
                    }
                    if (this.mHungConfigTransWindow != null) {
                        int status2 = this.mHungConfigTransWindow.status;
                        String[] value2 = this.mHungConfigTransWindow.value.split(",");
                        if (value2 != null) {
                            try {
                                if (value2.length >= 2) {
                                    i = Integer.parseInt(value2[1]);
                                }
                            } catch (NumberFormatException e2) {
                                checkFreezeScreenDelayTime = CHECK_FREEZE_SCREEN_DELAY_TIME;
                            }
                        }
                        checkFreezeScreenDelayTime = i;
                        if (status2 == 0 && value2 != null && "1".equals(value2[0])) {
                            this.mHandler.removeMessages(1);
                            msg = this.mHandler.obtainMessage(1);
                        }
                    }
                } else {
                    this.mHandler.removeMessages(1);
                    msg = this.mHandler.obtainMessage(1);
                }
                if (msg != null) {
                    msg.obj = params;
                    this.mHandler.sendMessageDelayed(msg, (long) checkFreezeScreenDelayTime);
                }
                Log.i(TAG, "scheduleCheckFreezeScreen sendMessageDelayed");
            }
        }
    }

    public boolean checkParamsValid(ArrayMap<String, Object> arrayMap) {
        return true;
    }

    private class FreezeScreenSceneHandler extends Handler {
        FreezeScreenSceneHandler(Looper looper) {
            super(looper, null, false);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                Log.d(FreezeScreenScene.TAG, "handleMessage CHECK_FREEZE_SCREEN_MSG");
                FreezeScreenScene.this.checkFreezeScreen((ArrayMap) msg.obj);
            } else if (i == 2) {
                Log.d(FreezeScreenScene.TAG, "handleMessage CHECK_FREEZE_SCREEN_FOCUS_WINDOW_ERROR_MSG");
                FreezeScreenScene.this.checkFreezeScreen((ArrayMap) msg.obj);
            }
        }
    }

    public synchronized FreezeScreenRadar getFreezeScreenRadar() {
        if (this.mFreezeScreenRadar == null) {
            this.mFreezeScreenRadar = new IMonitorRadar();
        }
        return this.mFreezeScreenRadar;
    }

    public static class IMonitorRadar extends FreezeScreenRadar {
        private static final long AUTO_UPLOAD_MIN_INTERVAL_TIME = 43200000;
        private static long sLastAutoUploadTime = 0;

        @Override // android.os.FreezeScreenScene.FreezeScreenRadar
        public void upload(ArrayMap<String, Object> params) {
            Log.i(FreezeScreenScene.TAG, "IMonitorRadar");
            long currentTime = System.currentTimeMillis();
            if (currentTime - sLastAutoUploadTime <= AUTO_UPLOAD_MIN_INTERVAL_TIME) {
                Log.d(FreezeScreenScene.TAG, "Upload too frequently, just discard it.");
                return;
            }
            sLastAutoUploadTime = currentTime;
            if (params != null && (params.get(FreezeScreenScene.CHECK_TYPE_PARAM) instanceof Integer)) {
                int checkType = ((Integer) params.get(FreezeScreenScene.CHECK_TYPE_PARAM)).intValue();
                IMonitor.EventStream eventStream = IMonitor.openEventStream(checkType);
                switch (checkType) {
                    case FreezeScreenScene.TRANSPARENT_ACTIVITY_SCENE /* 907400009 */:
                        eventStream.setParam((short) 0, MonitorHelper.getPackageName()).setParam((short) 1, MonitorHelper.getVersionName()).setParam((short) 2, (String) params.get(FreezeScreenScene.WINDOW_PARAM));
                        break;
                    case FreezeScreenScene.HIGH_WINDOW_LAYER_SCENE /* 907400011 */:
                        eventStream.setParam((short) 0, MonitorHelper.getPackageName()).setParam((short) 1, MonitorHelper.getVersionName()).setParam((short) 2, (String) params.get(FreezeScreenScene.HIGH_LEVEL_WINDOW_NAME_PARAM));
                        break;
                    case FreezeScreenScene.DISPLAY_EVENT_LOST_SCENE /* 907400012 */:
                        eventStream.setParam((short) 0, MonitorHelper.getPackageName()).setParam((short) 1, MonitorHelper.getVersionName()).setParam((short) 2, ((Integer) params.get(FreezeScreenScene.PID_PARAM)).intValue()).setParam((short) 3, (String) params.get(FreezeScreenScene.PROCESS_NAME));
                        break;
                    case FreezeScreenScene.FOCUS_WINDOW_NULL_SCENE /* 907400013 */:
                        eventStream.setParam((short) 0, MonitorHelper.getPackageName()).setParam((short) 1, MonitorHelper.getVersionName()).setParam((short) 2, (String) params.get(FreezeScreenScene.FOCUS_ACTIVITY_PARAM));
                        break;
                }
                IMonitor.sendEvent(eventStream);
                IMonitor.closeEventStream(eventStream);
            }
        }
    }

    public static abstract class FreezeScreenRadar {
        public void upload(ArrayMap<String, Object> arrayMap) {
            Log.i(FreezeScreenScene.TAG, "FreezeScreenRadar");
        }
    }

    public static class MonitorHelper {
        public static boolean isFullWindow(WindowManager.LayoutParams attrs) {
            if (attrs == null) {
                return false;
            }
            boolean hasFullScreenFlag = (attrs.flags & 1024) != 0;
            boolean isMatchParentWidthHeight = isAppWindow(attrs) && attrs.x == 0 && attrs.y == 0 && attrs.width == -1 && attrs.height == -1;
            if (hasFullScreenFlag || isMatchParentWidthHeight) {
                return true;
            }
            return false;
        }

        public static boolean isAppWindow(WindowManager.LayoutParams attrs) {
            return (attrs.type >= 1 && attrs.type < 2000) || attrs.type == 2100 || attrs.type == 2101;
        }

        public static Field getReflectPrivateField(String className, String fieldName) {
            try {
                final Field field = Class.forName(className).getDeclaredField(fieldName);
                AccessController.doPrivileged(new PrivilegedAction() {
                    /* class android.os.FreezeScreenScene.MonitorHelper.AnonymousClass1 */

                    @Override // java.security.PrivilegedAction
                    public Object run() {
                        field.setAccessible(true);
                        return null;
                    }
                });
                return field;
            } catch (ClassNotFoundException e) {
                Log.e(FreezeScreenScene.TAG, "getReflectPrivateField : not found " + className + " catch : ");
                return null;
            } catch (NoSuchFieldException e2) {
                Log.e(FreezeScreenScene.TAG, "getReflectPrivateField catch NoSuchFieldException : ");
                return null;
            }
        }

        public static Class<?> getClass(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                Log.e(FreezeScreenScene.TAG, "Class not found.");
                return null;
            }
        }

        /* access modifiers changed from: private */
        public static String getPackageName() {
            String packageName = ActivityThread.currentPackageName();
            if (packageName == null) {
                return "system_server";
            }
            return packageName;
        }

        private static long getVersionCode() {
            if (ActivityThread.currentApplication() != null) {
                return (long) ActivityThread.currentApplication().getApplicationContext().getApplicationInfo().versionCode;
            }
            return 0;
        }

        /* access modifiers changed from: private */
        public static String getVersionName() {
            Context context;
            PackageInfo info;
            try {
                if (!(ActivityThread.currentApplication() == null || (context = ActivityThread.currentApplication().getApplicationContext()) == null || (info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)) == null || info.versionName == null)) {
                    return info.versionName;
                }
            } catch (Exception e) {
                Log.e(FreezeScreenScene.TAG, "Could not get package info");
            }
            return getVersionCode() + StorageManagerExt.INVALID_KEY_DESC;
        }
    }
}
