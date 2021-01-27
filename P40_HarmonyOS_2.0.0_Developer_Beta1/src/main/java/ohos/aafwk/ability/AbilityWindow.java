package ohos.aafwk.ability;

import java.util.Optional;
import java.util.function.Consumer;
import ohos.aafwk.content.Intent;
import ohos.aafwk.utils.dfx.hiview.AbilityHiviewWrapper;
import ohos.aafwk.utils.dfx.hiview.EventInfo;
import ohos.aafwk.utils.log.KeyLog;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;
import ohos.agp.window.wmc.AGPWindow;
import ohos.agp.window.wmc.AGPWindowManager;
import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MouseEvent;
import ohos.multimodalinput.event.RotationEvent;
import ohos.multimodalinput.event.TouchEvent;
import ohos.tools.Bytrace;

public class AbilityWindow {
    private static final String DISPATCH_RESULT_LOG = "dispatch result: %{public}b";
    private static final float FLOAT_DELTA = 1.0E-7f;
    private static final LogLabel LABEL = LogLabel.create();
    private static final String SOURCEDEVICE_DEBUG_LOG = "SourceDevice: %{public}d";
    private Ability ability;
    private Context context;
    private Window userWindow;
    private AGPWindow window;
    private AGPWindowManager windowManager = AGPWindowManager.getInstance();

    /* access modifiers changed from: package-private */
    public void initialize(Ability ability2) {
        this.ability = ability2;
        if (this.windowManager != null) {
            this.context = ability2.getContext();
            if (this.context == null) {
                throw new IllegalStateException("no valid context, fatal error");
            } else if (this.window == null) {
                KeyLog.infoBound("[%{public}s][%{public}s][%{public}s]: Ability: %{public}s", LABEL.getTag(), KeyLog.CREATE_WINDOW, KeyLog.LogState.START, ability2);
                this.window = this.windowManager.createWindow(ability2);
                if (this.window == null) {
                    reportHiviewEvent(1);
                    throw new IllegalStateException("create window failed");
                }
            }
        } else {
            throw new IllegalStateException("windowManagerClient can never be null");
        }
    }

    private void reportHiviewEvent(int i) {
        EventInfo eventInfo = new EventInfo();
        eventInfo.setEventId(AbilityHiviewWrapper.EVENT_ID_ZFRAMEWORK_WINDOW_OP_FAILED);
        eventInfo.setErrorType(i);
        Ability ability2 = this.ability;
        if (ability2 == null || ability2.getAbilityInfo() == null) {
            AbilityHiviewWrapper.sendEvent(eventInfo);
            return;
        }
        eventInfo.setBundleName(this.ability.getAbilityInfo().getBundleName());
        eventInfo.setAbilityName(this.ability.getAbilityInfo().getClassName());
        AbilityHiviewWrapper.sendEvent(eventInfo);
    }

    /* access modifiers changed from: package-private */
    public void setUIContent(ComponentContainer componentContainer) {
        if (this.window == null) {
            reportHiviewEvent(1);
            Log.error(LABEL, "window is null, setUIContent with componentContainer failed", new Object[0]);
            return;
        }
        if (Log.isDebuggable()) {
            KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: attach layout componentContainer to window: [%{public}s]", LABEL.getTag(), KeyLog.SET_UI_CONTENT, KeyLog.LogState.START, componentContainer);
        }
        Bytrace.startTrace(2147483648L, "setUIContent");
        this.window.setContentLayout(componentContainer);
        Bytrace.finishTrace(2147483648L, "setUIContent");
    }

    /* access modifiers changed from: package-private */
    public void setUIContent(int i) {
        if (this.window == null) {
            Log.error(LABEL, "window is null, setUIContent with resource ID failed", new Object[0]);
            return;
        }
        if (Log.isDebuggable()) {
            KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: attach layout res to window: [%{public}d]", LABEL.getTag(), KeyLog.SET_UI_CONTENT, KeyLog.LogState.START, Integer.valueOf(i));
        }
        this.window.setContentLayout(i);
    }

    private boolean updateLayoutParamInt(Intent intent, String str, int i, Consumer<Integer> consumer) {
        int intParam = intent.getIntParam(str, i);
        if (intParam == i) {
            return false;
        }
        Log.debug(LABEL, "update layout params %{public}s: %{public}d", str, Integer.valueOf(intParam));
        consumer.accept(Integer.valueOf(intParam));
        return true;
    }

    private boolean updateLayoutParamFloat(Intent intent, String str, float f, Consumer<Float> consumer) {
        float floatParam = intent.getFloatParam(str, f);
        if (Math.abs(floatParam - f) < FLOAT_DELTA) {
            return false;
        }
        Log.debug(LABEL, "update layout params %{public}s: %{public}f", str, Float.valueOf(floatParam));
        consumer.accept(Float.valueOf(floatParam));
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setWindowAttribute(Intent intent) {
        if (Log.isDebuggable()) {
            KeyLog.debugBound(LABEL, KeyLog.SET_UI_LAYOUT, KeyLog.LogState.START);
        }
        AGPWindow aGPWindow = this.window;
        if (aGPWindow == null) {
            Log.error(LABEL, "window is null, set window attribute failed", new Object[0]);
            return;
        }
        AGPWindow.LayoutParams attributes = aGPWindow.getAttributes();
        if (attributes == null) {
            Log.error(LABEL, "attributes is null, set layout fail.", new Object[0]);
            return;
        }
        boolean updateLayoutParamInt = updateLayoutParamInt(intent, "PageLayoutX", attributes.x, new Consumer() {
            /* class ohos.aafwk.ability.$$Lambda$AbilityWindow$yvG5RrVyrhLtGaOmcxsIcgBCF1U */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AGPWindow.LayoutParams.this.x = ((Integer) obj).intValue();
            }
        });
        if (updateLayoutParamInt(intent, "PageLayoutY", attributes.y, new Consumer() {
            /* class ohos.aafwk.ability.$$Lambda$AbilityWindow$6epGj3TUSYH56Jps11H51Ofawxw */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AGPWindow.LayoutParams.this.y = ((Integer) obj).intValue();
            }
        })) {
            updateLayoutParamInt = true;
        }
        if (updateLayoutParamInt(intent, "PageLayoutWidth", attributes.width, new Consumer() {
            /* class ohos.aafwk.ability.$$Lambda$AbilityWindow$erhIQosGwBvc69BKmJOaGefd0G8 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AGPWindow.LayoutParams.this.width = ((Integer) obj).intValue();
            }
        })) {
            updateLayoutParamInt = true;
        }
        if (updateLayoutParamInt(intent, "PageLayoutHeight", attributes.height, new Consumer() {
            /* class ohos.aafwk.ability.$$Lambda$AbilityWindow$EdK6qQ04xiBvXI7PGMb6dUbA10 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AGPWindow.LayoutParams.this.height = ((Integer) obj).intValue();
            }
        })) {
            updateLayoutParamInt = true;
        }
        if (updateLayoutParamFloat(intent, "PageLayoutAlpha", attributes.alpha, new Consumer() {
            /* class ohos.aafwk.ability.$$Lambda$AbilityWindow$xiZaIaoDvnG8sfEG3Ol0rL5dMss */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AGPWindow.LayoutParams.this.alpha = ((Float) obj).floatValue();
            }
        })) {
            updateLayoutParamInt = true;
        }
        if (updateLayoutParamFloat(intent, "PageLayoutDimAmount", attributes.dimAmount, new Consumer() {
            /* class ohos.aafwk.ability.$$Lambda$AbilityWindow$GoV3Pwkm7YsnBVLLWzSHWYbOQxQ */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AGPWindow.LayoutParams.this.dimAmount = ((Float) obj).floatValue();
            }
        })) {
            updateLayoutParamInt = true;
        }
        if (updateLayoutParamInt(intent, "PageLayoutFlag", attributes.flags, new Consumer() {
            /* class ohos.aafwk.ability.$$Lambda$AbilityWindow$hB4g5ebY9qBlxqCLZNlbcj6gYc */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AGPWindow.LayoutParams.this.flags = ((Integer) obj).intValue();
            }
        })) {
            updateLayoutParamInt = true;
        }
        if (updateLayoutParamInt(intent, "PageLayoutAlignment", attributes.gravity, new Consumer() {
            /* class ohos.aafwk.ability.$$Lambda$AbilityWindow$IrWA_9nPhvW18DNXyzPMUXZfIc */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                AGPWindow.LayoutParams.this.gravity = ((Integer) obj).intValue();
            }
        })) {
            updateLayoutParamInt = true;
        }
        if (updateLayoutParamInt) {
            this.window.setAttributes(attributes);
        }
        int intParam = intent.getIntParam("WindowPaddingLeft", 0);
        int intParam2 = intent.getIntParam("WindowPaddingTop", 0);
        int intParam3 = intent.getIntParam("WindowPaddingRight", 0);
        int intParam4 = intent.getIntParam("WindowPaddingBottom", 0);
        if (intParam >= 0 && intParam2 >= 0 && intParam3 >= 0 && intParam4 >= 0) {
            this.window.setPadding(intParam, intParam2, intParam3, intParam4);
        }
    }

    /* access modifiers changed from: package-private */
    public Component findComponentById(int i) {
        AGPWindow aGPWindow = this.window;
        if (aGPWindow != null) {
            return aGPWindow.findComponentById(i).orElse(null);
        }
        Log.error(LABEL, "window is null, find component failed", new Object[0]);
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setIsAmbientMode(boolean z) {
        AGPWindow aGPWindow = this.window;
        if (aGPWindow == null) {
            Log.error(LABEL, "window is null, set is ambient mode failed", new Object[0]);
        } else {
            aGPWindow.setIsAmbientMode(z);
        }
    }

    /* access modifiers changed from: package-private */
    public ComponentContainer getCurrentAttachedUI() {
        AGPWindow aGPWindow = this.window;
        if (aGPWindow != null) {
            return aGPWindow.getContainerLayout();
        }
        Log.error(LABEL, "window is null, get UI failed", new Object[0]);
        return null;
    }

    /* access modifiers changed from: package-private */
    public Window getWindow() {
        if (this.userWindow == null) {
            this.userWindow = new Window(this.window);
        }
        return this.userWindow;
    }

    /* access modifiers changed from: package-private */
    public WindowManager.LayoutConfig getLayoutParams() {
        AGPWindow aGPWindow = this.window;
        if (aGPWindow == null) {
            Log.error(LABEL, "Window is null, get LayoutParams failed", new Object[0]);
            return null;
        }
        AGPWindow.LayoutParams attributes = aGPWindow.getAttributes();
        if (attributes == null) {
            Log.error(LABEL, "Window getAttributes return null", new Object[0]);
            return null;
        }
        WindowManager.LayoutConfig layoutConfig = new WindowManager.LayoutConfig();
        layoutConfig.alpha = attributes.alpha;
        layoutConfig.dim = attributes.dimAmount;
        layoutConfig.alignment = attributes.gravity;
        layoutConfig.height = attributes.height;
        layoutConfig.width = attributes.width;
        layoutConfig.type = attributes.type;
        layoutConfig.x = attributes.x;
        layoutConfig.y = attributes.y;
        layoutConfig.flags = attributes.flags;
        return layoutConfig;
    }

    /* access modifiers changed from: package-private */
    public void setLayoutParams(WindowManager.LayoutConfig layoutConfig) {
        if (layoutConfig == null) {
            Log.error(LABEL, "Window can not set LayoutParams to null", new Object[0]);
        } else if (this.window == null) {
            Log.error(LABEL, "Window is null, set LayoutParams failed", new Object[0]);
        } else {
            AGPWindow.LayoutParams layoutParams = new AGPWindow.LayoutParams();
            layoutParams.alpha = layoutConfig.alpha;
            layoutParams.dimAmount = layoutConfig.dim;
            layoutParams.gravity = layoutConfig.alignment;
            layoutParams.height = layoutConfig.height;
            layoutParams.width = layoutConfig.width;
            layoutParams.x = layoutConfig.x;
            layoutParams.y = layoutConfig.y;
            layoutParams.flags = layoutConfig.flags;
            this.window.setAttributes(layoutParams);
        }
    }

    /* access modifiers changed from: package-private */
    public void setWindowBackgroundColor(int i, int i2, int i3) {
        AGPWindow aGPWindow = this.window;
        if (aGPWindow == null) {
            Log.error(LABEL, "window is null, setWindowBackgroundColor failed", new Object[0]);
        } else {
            aGPWindow.setBackgroundColor(i, i2, i3);
        }
    }

    /* access modifiers changed from: package-private */
    public void onPreAbilityStart() {
        try {
            if (Log.isDebuggable()) {
                KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: abilityImpl: %{public}s, window: %{public}s", LABEL.getTag(), KeyLog.LOAD_WINDOW, KeyLog.LogState.START, this.ability, this.window);
            }
            this.window.load();
        } catch (IllegalStateException e) {
            reportHiviewEvent(2);
            Log.error(LABEL, "load window failed: %{public}s", e);
            throw e;
        }
    }

    /* access modifiers changed from: package-private */
    public void onPostAbilityStart() {
        try {
            if (Log.isDebuggable()) {
                KeyLog.debugBound(LABEL, KeyLog.SHOW_WINDOW, KeyLog.LogState.START);
            }
            this.window.show();
        } catch (IllegalStateException e) {
            reportHiviewEvent(3);
            Log.error(LABEL, "show window failed: %{public}s", e);
            throw e;
        }
    }

    /* access modifiers changed from: package-private */
    public void onPostAbilityStop() {
        try {
            if (Log.isDebuggable()) {
                KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: window: %{public}s", LABEL.getTag(), KeyLog.DESTROY_WINDOW, KeyLog.LogState.START, this.window);
            }
            this.windowManager.destroyWindow(this.window);
        } catch (IllegalStateException e) {
            Log.error(LABEL, "destroy window failed: [%{public}s], %{public}s", this.window, e);
        } catch (Throwable th) {
            this.window = null;
            throw th;
        }
        this.window = null;
    }

    /* access modifiers changed from: package-private */
    public boolean dispatchTouchEvent(TouchEvent touchEvent) {
        if (this.window == null) {
            Log.error(LABEL, "window is not created yet, ignore the motionEvent", new Object[0]);
            return false;
        }
        if (Log.isDebuggable()) {
            KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: SourceDevice: %{public}d", LABEL.getTag(), KeyLog.DISPATCH_TOUCH_EVENT, KeyLog.LogState.START, Integer.valueOf(touchEvent.getSourceDevice()));
        }
        boolean dispatchTouchEvent = this.window.dispatchTouchEvent(touchEvent);
        if (Log.isDebuggable()) {
            KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: dispatch result: %{public}b", LABEL.getTag(), KeyLog.DISPATCH_TOUCH_EVENT, KeyLog.LogState.END, Boolean.valueOf(dispatchTouchEvent));
        }
        return dispatchTouchEvent;
    }

    /* access modifiers changed from: package-private */
    public boolean dispatchMouseEvent(MouseEvent mouseEvent) {
        if (this.window == null) {
            Log.error(LABEL, "window is not created yet, ignore the motionEvent", new Object[0]);
            return false;
        }
        if (Log.isDebuggable()) {
            KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: SourceDevice: %{public}d", LABEL.getTag(), KeyLog.DISPATCH_MOUSE_EVENT, KeyLog.LogState.START, Integer.valueOf(mouseEvent.getSourceDevice()));
        }
        boolean dispatchMouseEvent = this.window.dispatchMouseEvent(mouseEvent);
        if (Log.isDebuggable()) {
            KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: dispatch result: %{public}b", LABEL.getTag(), KeyLog.DISPATCH_MOUSE_EVENT, KeyLog.LogState.END, Boolean.valueOf(dispatchMouseEvent));
        }
        return dispatchMouseEvent;
    }

    /* access modifiers changed from: package-private */
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (this.window == null) {
            Log.error(LABEL, "window is not created yet, ignore the keyEvent", new Object[0]);
            return false;
        }
        if (Log.isDebuggable()) {
            KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: SourceDevice: %{public}d", LABEL.getTag(), KeyLog.DISPATCH_KEY_EVENT, KeyLog.LogState.START, Integer.valueOf(keyEvent.getSourceDevice()));
        }
        boolean dispatchKeyboardEvent = this.window.dispatchKeyboardEvent(keyEvent);
        if (Log.isDebuggable()) {
            KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: dispatch result: %{public}b", LABEL.getTag(), KeyLog.DISPATCH_KEY_EVENT, KeyLog.LogState.END, Boolean.valueOf(dispatchKeyboardEvent));
        }
        return dispatchKeyboardEvent;
    }

    /* access modifiers changed from: package-private */
    public void setAVController(Object obj) {
        if (obj == null) {
            Log.error(LABEL, "Window can not set AVController to null", new Object[0]);
            return;
        }
        AGPWindow aGPWindow = this.window;
        if (aGPWindow == null) {
            Log.error(LABEL, "Window is null, set AVController failed", new Object[0]);
        } else {
            aGPWindow.setAVController(obj);
        }
    }

    /* access modifiers changed from: package-private */
    public Object getAVController() {
        AGPWindow aGPWindow = this.window;
        if (aGPWindow == null) {
            Log.error(LABEL, "Window is null, get AVController failed", new Object[0]);
            return null;
        }
        Optional<Object> aVController = aGPWindow.getAVController();
        if (aVController.isPresent()) {
            return aVController.get();
        }
        Log.error(LABEL, "Window getAVController return null", new Object[0]);
        return null;
    }

    /* access modifiers changed from: package-private */
    public void setVolumeTypeAdjustedByKey(int i) {
        AGPWindow aGPWindow = this.window;
        if (aGPWindow == null) {
            Log.error(LABEL, "Window is null, set Volume Type failed", new Object[0]);
        } else {
            aGPWindow.setVolumeControlStream(i);
        }
    }

    /* access modifiers changed from: package-private */
    public int getVolumeTypeAdjustedByKey() {
        AGPWindow aGPWindow = this.window;
        if (aGPWindow != null) {
            return aGPWindow.getVolumeControlStream();
        }
        Log.error(LABEL, "Window is null, get Volume Type failed", new Object[0]);
        return -1;
    }

    /* access modifiers changed from: package-private */
    public boolean dispatchRotationEvent(RotationEvent rotationEvent) {
        if (this.window == null) {
            Log.error(LABEL, "window is not created yet, ignore the rotationEvent", new Object[0]);
            return false;
        }
        if (Log.isDebuggable()) {
            KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: SourceDevice: %{public}d", LABEL.getTag(), KeyLog.DISPATCH_ROTATION_EVENT, KeyLog.LogState.START, Integer.valueOf(rotationEvent.getSourceDevice()));
        }
        boolean dispatchRotationEvent = this.window.dispatchRotationEvent(rotationEvent);
        if (Log.isDebuggable()) {
            KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: dispatch result: %{public}b", LABEL.getTag(), KeyLog.DISPATCH_ROTATION_EVENT, KeyLog.LogState.END, Boolean.valueOf(dispatchRotationEvent));
        }
        return dispatchRotationEvent;
    }

    /* access modifiers changed from: package-private */
    public boolean hasWindowFocus() {
        AGPWindow aGPWindow = this.window;
        if (aGPWindow != null) {
            return aGPWindow.hasWindowFocus();
        }
        Log.error(LABEL, "window is null, check window focus failed", new Object[0]);
        return false;
    }

    /* access modifiers changed from: package-private */
    public Component getCurrentFocus() {
        AGPWindow aGPWindow = this.window;
        if (aGPWindow == null) {
            Log.error(LABEL, "window is null, get current window focus failed", new Object[0]);
            return null;
        } else if (aGPWindow.getWindowFocus().isPresent()) {
            return this.window.getWindowFocus().get();
        } else {
            Log.error(LABEL, "Window getCurrentFocus return null", new Object[0]);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean setSwipeToDismiss(boolean z) {
        AGPWindow aGPWindow = this.window;
        if (aGPWindow == null) {
            Log.error(LABEL, "window is null, set swipe to dismiss window failed", new Object[0]);
            return false;
        }
        boolean swipeToDismiss = aGPWindow.setSwipeToDismiss(z);
        if (z) {
            this.window.setOnDismissListener(new AGPWindow.OnDismissListener() {
                /* class ohos.aafwk.ability.$$Lambda$AbilityWindow$rVH_2k8nTLboZSHSFW4Y5XsdoaE */

                @Override // ohos.agp.window.wmc.AGPWindow.OnDismissListener
                public final void onDismissed() {
                    AbilityWindow.this.lambda$setSwipeToDismiss$8$AbilityWindow();
                }
            });
        }
        return swipeToDismiss;
    }

    public /* synthetic */ void lambda$setSwipeToDismiss$8$AbilityWindow() {
        TaskDispatcher mainTaskDispatcher = this.ability.getMainTaskDispatcher();
        if (mainTaskDispatcher != null) {
            mainTaskDispatcher.delayDispatch(new Runnable() {
                /* class ohos.aafwk.ability.AbilityWindow.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    AbilitySlice topAbilitySlice = AbilityWindow.this.ability.getAbilitySliceManager().getTopAbilitySlice();
                    if (topAbilitySlice != null) {
                        topAbilitySlice.onBackPressed();
                        return;
                    }
                    Log.info(AbilityWindow.LABEL, "terminate Ability", new Object[0]);
                    if (!AbilityWindow.this.ability.isTerminating()) {
                        AbilityWindow.this.ability.terminateAbility();
                    }
                }
            }, 0);
        }
    }
}
