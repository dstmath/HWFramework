package huawei.android.security.secai.hookcase.hook;

import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class WindowManagerGlobalHook {
    private static final String TAG = WindowManagerGlobalHook.class.getSimpleName();
    private static final String WINDOWMANAGERGLOBAL_CLASSNAME = "android.view.WindowManagerGlobal";

    WindowManagerGlobalHook() {
    }

    @HookMethod(name = "addView", params = {View.class, ViewGroup.LayoutParams.class, Display.class, Window.class}, reflectionTargetClass = WINDOWMANAGERGLOBAL_CLASSNAME)
    static void addViewHook(Object obj, View view, ViewGroup.LayoutParams params, Display display, Window parentWindow) {
        Log.i(TAG, "Call System Hook Method: WindowManagerGlobal addViewHook()");
        addViewBackup(obj, view, params, display, parentWindow);
    }

    @BackupMethod(name = "addView", params = {View.class, ViewGroup.LayoutParams.class, Display.class, Window.class}, reflectionTargetClass = WINDOWMANAGERGLOBAL_CLASSNAME)
    static void addViewBackup(Object obj, View view, ViewGroup.LayoutParams params, Display display, Window parentWindow) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: WindowManagerGlobal addViewBackup().");
    }

    @HookMethod(name = "removeView", params = {View.class, boolean.class}, reflectionTargetClass = WINDOWMANAGERGLOBAL_CLASSNAME)
    static void removeViewHook(Object obj, View view, boolean isImmediate) {
        Log.i(TAG, "Call System Hook Method:WindowManagerGlobal removeViewHook()");
        removeViewBackup(obj, view, isImmediate);
    }

    @BackupMethod(name = "removeView", params = {View.class, boolean.class}, reflectionTargetClass = WINDOWMANAGERGLOBAL_CLASSNAME)
    static void removeViewBackup(Object obj, View view, boolean isImmediate) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method:WindowManagerGlobal removeViewBackup()");
    }

    @HookMethod(name = "updateViewLayout", params = {View.class, ViewGroup.LayoutParams.class}, reflectionTargetClass = WINDOWMANAGERGLOBAL_CLASSNAME)
    static void updateViewLayoutHook(Object obj, View view, ViewGroup.LayoutParams params) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.WINDOWNMANAGER_UPDATEVIEWLAYOUT.getValue());
        Log.i(TAG, "Call System Hook Method: WindowManagerGlobal updateViewLayoutHook()");
        updateViewLayoutBackup(obj, view, params);
    }

    @BackupMethod(name = "updateViewLayout", params = {View.class, ViewGroup.LayoutParams.class}, reflectionTargetClass = WINDOWMANAGERGLOBAL_CLASSNAME)
    static void updateViewLayoutBackup(Object obj, View view, ViewGroup.LayoutParams params) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: WindowManagerGlobal updateViewLayoutBackup()");
    }
}
