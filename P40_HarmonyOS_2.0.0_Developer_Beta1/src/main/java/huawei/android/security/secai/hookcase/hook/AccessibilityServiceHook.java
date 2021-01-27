package huawei.android.security.secai.hookcase.hook;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import huawei.android.security.HwBehaviorCollectManagerImpl;
import huawei.android.security.secai.hookcase.annotations.BackupMethod;
import huawei.android.security.secai.hookcase.annotations.HookMethod;
import huawei.android.security.secai.hookcase.utils.BehaviorIdCast;

class AccessibilityServiceHook {
    private static final String ACCESSIBILITY_SERVICE_CLIENT_WRAPPER_CLASSNAME = "android.accessibilityservice.AccessibilityService$IAccessibilityServiceClientWrapper";
    private static final String TAG = AccessibilityServiceHook.class.getSimpleName();

    AccessibilityServiceHook() {
    }

    @HookMethod(name = "onAccessibilityEvent", params = {AccessibilityEvent.class, boolean.class}, reflectionTargetClass = ACCESSIBILITY_SERVICE_CLIENT_WRAPPER_CLASSNAME)
    static void onAccessibilityEventHook(Object obj, AccessibilityEvent event, boolean isServiceWantsEvent) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.ACCESSIBILITY_ONACCESSIBILITYEVENT.getValue());
        Log.i(TAG, "Call System Hook Method: AccessibilityService onAccessibilityEventHook()");
        onAccessibilityEventBackup(obj, event, isServiceWantsEvent);
    }

    @BackupMethod(name = "onAccessibilityEvent", params = {AccessibilityEvent.class, boolean.class}, reflectionTargetClass = ACCESSIBILITY_SERVICE_CLIENT_WRAPPER_CLASSNAME)
    static void onAccessibilityEventBackup(Object obj, AccessibilityEvent event, boolean isServiceWantsEvent) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: AccessibilityService onAccessibilityEventBackup().");
    }

    @HookMethod(name = "dispatchServiceConnected", params = {}, targetClass = AccessibilityService.class)
    static void dispatchServiceConnectedHook(Object obj) {
        HwBehaviorCollectManagerImpl.getSubClassDefault().sendBehavior(BehaviorIdCast.BehaviorId.ACCESSIBILITY_DISPATCHSERVICECONNECTED.getValue());
        Log.i(TAG, "Call System Hook Method: AccessibilityService dispatchServiceConnected()");
        dispatchServiceConnectedBackup(obj);
    }

    @BackupMethod(name = "dispatchServiceConnected", params = {}, targetClass = AccessibilityService.class)
    static void dispatchServiceConnectedBackup(Object obj) {
        for (int i = 0; i < 100; i++) {
        }
        Log.i(TAG, "Call System Backup Method: AccessibilityService dispatchServiceConnected()");
    }
}
