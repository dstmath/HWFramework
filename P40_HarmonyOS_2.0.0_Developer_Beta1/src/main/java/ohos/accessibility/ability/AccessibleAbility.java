package ohos.accessibility.ability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.accessibility.AccessibilityEventInfo;
import ohos.accessibility.adapter.ability.AccessibilityDisableAdapter;
import ohos.accessibility.adapter.ability.AccessibilitySearchAdapter;
import ohos.accessibility.adapter.ability.AccessibilityWindowAdapter;
import ohos.accessibility.adapter.ability.AccessibleControlAdapter;
import ohos.accessibility.adapter.ability.PerformActionAdapter;
import ohos.accessibility.remoteadapter.BarrierfreeManagerSkeleton;
import ohos.agp.utils.Rect;
import ohos.multimodalinput.event.KeyEvent;
import ohos.rpc.IRemoteObject;

public abstract class AccessibleAbility extends Ability {
    private static final int CONNECTION_ID_FLAG = -1;
    private static final String DESCRIPTOR = "android.accessibilityservice.IAccessibilityServiceClient";
    public static final int GLOBAL_ACTION_BACK = 1;
    public static final int GLOBAL_ACTION_HOME = 2;
    public static final int GLOBAL_ACTION_LOCK_SCREEN = 8;
    public static final int GLOBAL_ACTION_NOTIFICATIONS = 4;
    public static final int GLOBAL_ACTION_POWER_DIALOG = 6;
    public static final int GLOBAL_ACTION_QUICK_SETTINGS = 5;
    public static final int GLOBAL_ACTION_RECENTS = 3;
    public static final int GLOBAL_ACTION_TAKE_SCREENSHOT = 9;
    public static final int GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN = 7;
    public static final int SHOW_MODE_AUTO = 0;
    public static final int SHOW_MODE_HIDE = 1;
    public static final int SHOW_MODE_IGNORE_HARD_KEYBOARD = 2;
    private static final String TAG = "AccessibleAbility";
    private int connectionId = -1;
    private Map<Integer, DisplayResizeController> mDisplayResizeControllers = new HashMap();
    private Map<Integer, GestureResultListenerInfo> mGestureListenerInfos = new HashMap();
    private int mGestureSequenceNum;
    private final Object mLock = new Object();
    private BarrierfreeManagerSkeleton mRemote = new BarrierfreeManagerSkeleton(DESCRIPTOR, getAccessibilityCallbackImpl());
    private SoftKeyBoardController mSoftKeyBoardController;

    public interface AccessibilityCallbacks {
        void init(int i);

        void onAbilityConnected();

        void onAccessibilityEvent(AccessibilityEventInfo accessibilityEventInfo);

        void onDisplayResizeChanged(int i, Rect rect, float f, float f2, float f3);

        boolean onGesture(int i);

        void onInterrupt();

        boolean onKeyEvent(KeyEvent keyEvent);

        void onPerformGestureResult(int i, boolean z);

        void onSoftKeyboardShowModeChanged(int i);
    }

    /* access modifiers changed from: protected */
    public void onAbilityConnected() {
    }

    public abstract void onAccessibilityEvent(AccessibilityEventInfo accessibilityEventInfo);

    /* access modifiers changed from: protected */
    public boolean onGesture(int i) {
        return false;
    }

    public abstract void onInterrupt();

    /* access modifiers changed from: protected */
    public boolean onKeyPressEvent(KeyEvent keyEvent) {
        return false;
    }

    public final void disableAbility() {
        AccessibilityDisableAdapter.disableAbility(this.connectionId);
    }

    public final SoftKeyBoardController getSoftKeyBoardController() {
        SoftKeyBoardController softKeyBoardController;
        synchronized (this.mLock) {
            if (this.mSoftKeyBoardController == null) {
                this.mSoftKeyBoardController = new SoftKeyBoardController(this.connectionId, this.mLock);
            }
            softKeyBoardController = this.mSoftKeyBoardController;
        }
        return softKeyBoardController;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onSoftKeyboardShowModeChanged(int i) {
        synchronized (this.mLock) {
            if (this.mSoftKeyBoardController != null) {
                this.mSoftKeyBoardController.dispatchSoftKeyBoardListeners(i);
            }
        }
    }

    public final DisplayResizeController getDisplayResizeController() {
        return getDisplayResizeController(0);
    }

    public final DisplayResizeController getDisplayResizeController(int i) {
        DisplayResizeController displayResizeController;
        synchronized (this.mLock) {
            displayResizeController = this.mDisplayResizeControllers.get(Integer.valueOf(i));
            if (displayResizeController == null) {
                displayResizeController = new DisplayResizeController(this.connectionId, i, this.mLock);
                this.mDisplayResizeControllers.put(Integer.valueOf(i), displayResizeController);
            }
        }
        return displayResizeController;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDisplayResizeChanged(int i, Rect rect, float f, float f2, float f3) {
        synchronized (this.mLock) {
            DisplayResizeController displayResizeController = this.mDisplayResizeControllers.get(Integer.valueOf(i));
            if (displayResizeController != null) {
                displayResizeController.dispatchMagnificationChanged(rect, f, f2, f3);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchAbilityConnected() {
        synchronized (this.mLock) {
            for (DisplayResizeController displayResizeController : this.mDisplayResizeControllers.values()) {
                displayResizeController.onServiceConnectedLocked();
            }
            if (this.mSoftKeyBoardController != null) {
                this.mSoftKeyBoardController.onServiceConnected();
            }
        }
        onAbilityConnected();
    }

    /* access modifiers changed from: package-private */
    public void onPerformGestureResult(int i, boolean z) {
        GestureResultListenerInfo gestureResultListenerInfo;
        if (!this.mGestureListenerInfos.isEmpty()) {
            synchronized (this.mLock) {
                gestureResultListenerInfo = this.mGestureListenerInfos.get(Integer.valueOf(i));
            }
            if (gestureResultListenerInfo != null && gestureResultListenerInfo.getGestureResultListener() != null) {
                if (z) {
                    gestureResultListenerInfo.getGestureResultListener().onCompleted();
                } else {
                    gestureResultListenerInfo.getGestureResultListener().onCancelled();
                }
            }
        }
    }

    public final boolean gestureSimulate(List<GesturePathDefine> list, GestureResultListener gestureResultListener) {
        this.mGestureSequenceNum++;
        if (gestureResultListener != null) {
            this.mGestureListenerInfos.put(Integer.valueOf(this.mGestureSequenceNum), new GestureResultListenerInfo(gestureResultListener));
        }
        return AccessibleControlAdapter.dispatchGesture(this.connectionId, list, this.mGestureSequenceNum);
    }

    public List<AccessibilityWindow> getWindows() {
        return AccessibilityWindowAdapter.getAccessibilityWindows(this.connectionId);
    }

    public Optional<AccessibilityInfo> getRootAccessibilityInfo() {
        return AccessibilitySearchAdapter.getRootAccessibilityInfo(this.connectionId);
    }

    public Optional<AccessibilityInfo> gainFocus(int i) {
        return AccessibilitySearchAdapter.findFocusedAccessibilityInfo(this.connectionId, i);
    }

    public final boolean performCommonAction(int i) {
        return PerformActionAdapter.performAction(i, this.connectionId);
    }

    @Override // ohos.aafwk.ability.Ability
    public final IRemoteObject onConnect(Intent intent) {
        super.onConnect(intent);
        return this.mRemote.asObject();
    }

    private AccessibilityCallbacks getAccessibilityCallbackImpl() {
        return new AccessibilityCallbacks() {
            /* class ohos.accessibility.ability.AccessibleAbility.AnonymousClass1 */

            @Override // ohos.accessibility.ability.AccessibleAbility.AccessibilityCallbacks
            public void onAccessibilityEvent(AccessibilityEventInfo accessibilityEventInfo) {
                AccessibleAbility.this.onAccessibilityEvent(accessibilityEventInfo);
            }

            @Override // ohos.accessibility.ability.AccessibleAbility.AccessibilityCallbacks
            public void onInterrupt() {
                AccessibleAbility.this.onInterrupt();
            }

            @Override // ohos.accessibility.ability.AccessibleAbility.AccessibilityCallbacks
            public void onAbilityConnected() {
                AccessibleAbility.this.dispatchAbilityConnected();
            }

            @Override // ohos.accessibility.ability.AccessibleAbility.AccessibilityCallbacks
            public void init(int i) {
                AccessibleAbility.this.connectionId = i;
            }

            @Override // ohos.accessibility.ability.AccessibleAbility.AccessibilityCallbacks
            public boolean onKeyEvent(KeyEvent keyEvent) {
                return AccessibleAbility.this.onKeyPressEvent(keyEvent);
            }

            @Override // ohos.accessibility.ability.AccessibleAbility.AccessibilityCallbacks
            public void onSoftKeyboardShowModeChanged(int i) {
                AccessibleAbility.this.onSoftKeyboardShowModeChanged(i);
            }

            @Override // ohos.accessibility.ability.AccessibleAbility.AccessibilityCallbacks
            public void onDisplayResizeChanged(int i, Rect rect, float f, float f2, float f3) {
                AccessibleAbility.this.onDisplayResizeChanged(i, rect, f, f2, f3);
            }

            @Override // ohos.accessibility.ability.AccessibleAbility.AccessibilityCallbacks
            public boolean onGesture(int i) {
                return AccessibleAbility.this.onGesture(i);
            }

            @Override // ohos.accessibility.ability.AccessibleAbility.AccessibilityCallbacks
            public void onPerformGestureResult(int i, boolean z) {
                AccessibleAbility.this.onPerformGestureResult(i, z);
            }
        };
    }
}
