package ohos.aafwk.ability;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import ohos.aafwk.ability.AbilityForm;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.AbilitySliceLifecycleExecutor;
import ohos.aafwk.ability.IAbilityFormProvider;
import ohos.aafwk.ability.Lifecycle;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.agp.animation.AnimatorProperty;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.ComponentParent;
import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;
import ohos.app.AbilityContext;
import ohos.app.Context;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ElementName;
import ohos.multimodalinput.event.KeyEvent;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.tools.Bytrace;

public class AbilitySlice extends AbilityContext implements ILifecycle {
    private static final String CONTINUE_ABILITY_FAILED = "continue ability failed.";
    private static final LogLabel LABEL = LogLabel.create();
    private static final int REQUEST_CODE_MARK = 65535;
    private AbilitySliceManager abilitySliceManager;
    private final Map<AbilityForm, IAbilityConnection> acquiringRecords = new HashMap();
    private volatile AbilitySliceLifecycleExecutor.LifecycleState currentState = AbilitySliceLifecycleExecutor.LifecycleState.INITIAL;
    private boolean isInitialized = false;
    private Lifecycle lifecycle = new Lifecycle();
    private Intent resultData;
    private SliceUIContent uiContent;

    /* access modifiers changed from: protected */
    public void onAbilityResult(int i, int i2, Intent intent) {
    }

    /* access modifiers changed from: protected */
    public void onActive() {
    }

    /* access modifiers changed from: protected */
    public void onBackground() {
    }

    /* access modifiers changed from: protected */
    public void onForeground(Intent intent) {
    }

    /* access modifiers changed from: protected */
    public void onInactive() {
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        return false;
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onOrientationChanged(AbilityInfo.DisplayOrientation displayOrientation) {
    }

    /* access modifiers changed from: protected */
    public void onResult(int i, Intent intent) {
    }

    /* access modifiers changed from: protected */
    public void onStart(Intent intent) {
    }

    /* access modifiers changed from: protected */
    public void onStop() {
    }

    /* access modifiers changed from: package-private */
    public final void init(Context context, AbilitySliceManager abilitySliceManager2) {
        if (this.isInitialized) {
            Log.warn(LABEL, "AbilitySlice is already init", new Object[0]);
        } else if (context == null || abilitySliceManager2 == null) {
            String str = "illegal initialization: context = " + context + ", asm = " + abilitySliceManager2;
            Log.error(LABEL, "illegal initialization: context = %{public}s, asm = %{public}s", context, abilitySliceManager2);
            throw new IllegalArgumentException(str);
        } else {
            this.isInitialized = true;
            attachBaseContext(context);
            this.abilitySliceManager = abilitySliceManager2;
            this.uiContent = new SliceUIContent(abilitySliceManager2.getWindowProxy());
            Log.debug(LABEL, "AbilitySlice init done", new Object[0]);
        }
    }

    /* access modifiers changed from: protected */
    public void onBackPressed() {
        terminate();
    }

    /* access modifiers changed from: private */
    public class AbilityFormAcquireConnection implements IAbilityConnection {
        private AbilityForm abilityForm;
        private AbilityForm.OnAcquiredCallback acquiredCallback;
        private Intent intent;

        AbilityFormAcquireConnection(Intent intent2, AbilityForm.OnAcquiredCallback onAcquiredCallback) {
            this.acquiredCallback = onAcquiredCallback;
            this.intent = new Intent(intent2);
        }

        @Override // ohos.aafwk.ability.IAbilityConnection
        public void onAbilityConnectDone(ElementName elementName, IRemoteObject iRemoteObject, int i) {
            if (iRemoteObject != null) {
                try {
                    this.abilityForm = IAbilityFormProvider.FormProviderStub.asProxy(iRemoteObject).acquireAbilityForm();
                    if (AbilitySlice.this.getUITaskDispatcher() == null) {
                        Log.error(AbilitySlice.LABEL, "ability slice is not init, acquire ability form failed", new Object[0]);
                        return;
                    }
                    if (!(this.abilityForm == null || AbilitySlice.this.abilitySliceManager == null)) {
                        Log.info(AbilitySlice.LABEL, "set intent / shell and dispatcher for form", new Object[0]);
                        this.abilityForm.setUITaskDispatcher(AbilitySlice.this.getUITaskDispatcher());
                        this.abilityForm.setFullPageIntentElement(this.intent.getElement());
                        if (!this.abilityForm.asClient(AbilitySlice.this.abilitySliceManager.getContext())) {
                            Log.warn(AbilitySlice.LABEL, "ability form asClient failed", new Object[0]);
                            this.abilityForm = null;
                        } else {
                            synchronized (AbilitySlice.this.acquiringRecords) {
                                AbilitySlice.this.acquiringRecords.put(this.abilityForm, this);
                            }
                        }
                    }
                    AbilitySlice.this.getUITaskDispatcher().asyncDispatch(new Runnable() {
                        /* class ohos.aafwk.ability.$$Lambda$AbilitySlice$AbilityFormAcquireConnection$Da1vHuJcewOrurHdibuRRcHUtiA */

                        @Override // java.lang.Runnable
                        public final void run() {
                            AbilitySlice.AbilityFormAcquireConnection.this.lambda$onAbilityConnectDone$0$AbilitySlice$AbilityFormAcquireConnection();
                        }
                    });
                    Log.info(AbilitySlice.LABEL, "acquire ability form done", new Object[0]);
                } catch (RemoteException unused) {
                    Log.error(AbilitySlice.LABEL, "acquire ability form failed through RPC", new Object[0]);
                }
            }
        }

        public /* synthetic */ void lambda$onAbilityConnectDone$0$AbilitySlice$AbilityFormAcquireConnection() {
            this.acquiredCallback.onAcquired(this.abilityForm);
        }

        @Override // ohos.aafwk.ability.IAbilityConnection
        public void onAbilityDisconnectDone(ElementName elementName, int i) {
            if (AbilitySlice.this.getUITaskDispatcher() == null) {
                Log.error(AbilitySlice.LABEL, "ability slice is not init, no need post destroy event to user", new Object[0]);
                return;
            }
            AbilitySlice.this.getUITaskDispatcher().asyncDispatch(new Runnable() {
                /* class ohos.aafwk.ability.$$Lambda$AbilitySlice$AbilityFormAcquireConnection$Ty3rRCH5YvdgW_XkYIR5SX_Iu0 */

                @Override // java.lang.Runnable
                public final void run() {
                    AbilitySlice.AbilityFormAcquireConnection.this.lambda$onAbilityDisconnectDone$1$AbilitySlice$AbilityFormAcquireConnection();
                }
            });
            synchronized (AbilitySlice.this.acquiringRecords) {
                AbilitySlice.this.acquiringRecords.remove(this.abilityForm);
            }
            Log.info(AbilitySlice.LABEL, "release ability form done: %{public}d", Integer.valueOf(i));
        }

        public /* synthetic */ void lambda$onAbilityDisconnectDone$1$AbilitySlice$AbilityFormAcquireConnection() {
            this.acquiredCallback.onDestroyed(this.abilityForm);
        }
    }

    /* access modifiers changed from: private */
    public static class SliceUIContent extends UIContent {
        SliceUIContent(AbilityWindow abilityWindow) {
            super(abilityWindow);
        }

        private synchronized AnimatorProperty createCurViewAnimator(AbilitySliceAnimator abilitySliceAnimator) {
            ComponentParent componentParent = this.curViewGroup != null ? this.curViewGroup.getComponentParent() : null;
            Component component = componentParent instanceof Component ? (Component) componentParent : null;
            if (component == null || !component.isAttachedToWindow()) {
                return null;
            }
            if (abilitySliceAnimator != null && abilitySliceAnimator.isDefaultAnimator()) {
                abilitySliceAnimator.constructDefaultAnimator((float) component.getRight());
            }
            return component.createAnimatorProperty();
        }

        /* access modifiers changed from: package-private */
        public synchronized void viewEnterAnimator(AbilitySliceAnimator abilitySliceAnimator) {
            AnimatorProperty createCurViewAnimator = createCurViewAnimator(abilitySliceAnimator);
            if (createCurViewAnimator != null) {
                Log.info(AbilitySlice.LABEL, "start enter animator", new Object[0]);
                abilitySliceAnimator.buildEnterAnimator(createCurViewAnimator).start();
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void viewExitAnimator(AbilitySliceAnimator abilitySliceAnimator) {
            AnimatorProperty createCurViewAnimator = createCurViewAnimator(abilitySliceAnimator);
            if (createCurViewAnimator != null) {
                Log.info(AbilitySlice.LABEL, "start exit animator", new Object[0]);
                abilitySliceAnimator.buildExitAnimator(createCurViewAnimator).start();
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void stopViewAnimator() {
            AnimatorProperty createCurViewAnimator = createCurViewAnimator(null);
            if (createCurViewAnimator != null) {
                createCurViewAnimator.end();
            }
        }
    }

    public String toString() {
        return "[Slice@" + Integer.toHexString(hashCode()) + ", " + getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + "]";
    }

    public Window getWindow() {
        checkInitialization("getWindow failed");
        AbilityWindow windowProxy = this.abilitySliceManager.getWindowProxy();
        if (windowProxy != null) {
            return windowProxy.getWindow();
        }
        Log.error(LABEL, "windowProxy is null, getLayoutParams failed", new Object[0]);
        return null;
    }

    public WindowManager.LayoutConfig getLayoutParams() {
        checkInitialization("getLayoutParams failed");
        AbilityWindow windowProxy = this.abilitySliceManager.getWindowProxy();
        if (windowProxy != null) {
            return windowProxy.getLayoutParams();
        }
        Log.error(LABEL, "windowProxy is null, getLayoutParams failed", new Object[0]);
        return null;
    }

    public void setLayoutParams(WindowManager.LayoutConfig layoutConfig) {
        checkInitialization("setLayoutParams failed");
        AbilityWindow windowProxy = this.abilitySliceManager.getWindowProxy();
        if (windowProxy == null) {
            Log.error(LABEL, "windowProxy is null, setLayoutParams failed", new Object[0]);
        } else {
            windowProxy.setLayoutParams(layoutConfig);
        }
    }

    public void setIsAmbientMode(boolean z) {
        checkInitialization("setIsAmbientMode failed");
        AbilityWindow windowProxy = this.abilitySliceManager.getWindowProxy();
        if (windowProxy == null) {
            Log.error(LABEL, "windowProxy is null, setIsAmbientMode failed", new Object[0]);
        } else {
            windowProxy.setIsAmbientMode(z);
        }
    }

    public final void setUIContent(int i) {
        if (i >= 0) {
            checkInitialization("setUIContent failed");
            this.uiContent.updateUIContent(i);
            return;
        }
        Log.error(LABEL, "ui layout resource must be valid [%{public}d]", Integer.valueOf(i));
        throw new AbilitySliceRuntimeException("UI layout resource must be valid");
    }

    public void setUIContent(ComponentContainer componentContainer) {
        if (componentContainer != null) {
            checkInitialization("setUIContent with viewGroup failed");
            this.uiContent.updateUIContent(componentContainer);
            return;
        }
        Log.error(LABEL, "viewGroup must be valid", new Object[0]);
        throw new AbilitySliceRuntimeException("viewGroup must be valid");
    }

    /* access modifiers changed from: package-private */
    public final ComponentContainer getCurrentUI() {
        SliceUIContent sliceUIContent = this.uiContent;
        if (sliceUIContent != null) {
            return sliceUIContent.curViewGroup;
        }
        Log.error(LABEL, "ui Content is not inited", new Object[0]);
        return null;
    }

    public Component findComponentById(int i) {
        checkInitialization("find view by ID failed");
        return this.uiContent.findComponentById(i);
    }

    public final void present(AbilitySlice abilitySlice, Intent intent) {
        if (abilitySlice != null) {
            checkInitialization("present failed");
            this.abilitySliceManager.present(this, abilitySlice, intent);
            return;
        }
        Log.error(LABEL, "present failed, can not present a null target", new Object[0]);
        throw new IllegalArgumentException("can not present a null target");
    }

    public final void presentForResult(AbilitySlice abilitySlice, Intent intent, int i) {
        if (abilitySlice == null) {
            Log.error(LABEL, "present failed, can not present a null target", new Object[0]);
            throw new IllegalArgumentException("can not present a null target");
        } else if (abilitySlice == this) {
            Log.error(LABEL, "present failed, can not present self for result", new Object[0]);
            throw new IllegalArgumentException("can not present self for result");
        } else if (i >= 0) {
            checkInitialization("presentForResult failed");
            this.abilitySliceManager.presentForResult(this, abilitySlice, intent, i);
        } else {
            Log.error(LABEL, "present failed, requestCode must not be negative", new Object[0]);
            throw new IllegalArgumentException("requestCode must not be negative");
        }
    }

    public final void setResult(Intent intent) {
        this.resultData = intent;
    }

    public final void terminate() {
        checkInitialization("terminate failed");
        this.abilitySliceManager.terminate(this, this.resultData);
    }

    public void startAbility(Intent intent) {
        if (intent != null) {
            checkInitialization("startAbility failed");
            this.abilitySliceManager.startAbility(intent);
            return;
        }
        Log.error(LABEL, "intent must be assigned for startAbility", new Object[0]);
        throw new IllegalArgumentException("intent must be assigned for startAbility");
    }

    public void startAbilityForResult(Intent intent, int i) {
        if (intent != null) {
            checkValidRequestCode(i);
            checkInitialization("startAbilityForResult failed");
            this.abilitySliceManager.startAbilityForResult(this, intent, i);
            return;
        }
        Log.error(LABEL, "intent must be assigned for startAbilityForResult", new Object[0]);
        throw new IllegalArgumentException("intent must be assigned for startAbilityForResult");
    }

    /* access modifiers changed from: package-private */
    public final AbilitySliceLifecycleExecutor.LifecycleState getState() {
        return this.currentState;
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public final boolean stopAbility(Intent intent) throws IllegalStateException, IllegalArgumentException {
        if (intent != null) {
            checkInitialization("stopAbility failed");
            return this.abilitySliceManager.stopAbility(intent);
        }
        Log.error(LABEL, "intent must be assigned for stopAbility", new Object[0]);
        throw new IllegalArgumentException("intent must be assigned for stopAbility");
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public void terminateAbility() {
        checkInitialization("terminateAbility failed");
        this.abilitySliceManager.terminateAbility();
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public final boolean connectAbility(Intent intent, IAbilityConnection iAbilityConnection) throws IllegalStateException, IllegalArgumentException {
        checkInitialization("connect ability failed.");
        return this.abilitySliceManager.connectAbility(this, intent, iAbilityConnection);
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public final void disconnectAbility(IAbilityConnection iAbilityConnection) throws IllegalStateException, IllegalArgumentException {
        Log.info(LABEL, "disconnect ability from slice", new Object[0]);
        if (iAbilityConnection != null) {
            checkInitialization("disconnect ability failed.");
            this.abilitySliceManager.disconnectAbility(this, iAbilityConnection);
            return;
        }
        throw new IllegalArgumentException("disconnect Ability failed. conn is null.");
    }

    public void continueAbility() throws IllegalStateException, UnsupportedOperationException {
        checkInitialization(CONTINUE_ABILITY_FAILED);
        this.abilitySliceManager.continueAbility(null);
    }

    public void continueAbility(String str) throws IllegalStateException, UnsupportedOperationException {
        checkInitialization(CONTINUE_ABILITY_FAILED);
        this.abilitySliceManager.continueAbility(str);
    }

    public void continueAbilityReversibly() throws IllegalStateException, UnsupportedOperationException {
        checkInitialization(CONTINUE_ABILITY_FAILED);
        this.abilitySliceManager.continueAbilityReversibly(null);
    }

    public void continueAbilityReversibly(String str) throws IllegalStateException, UnsupportedOperationException {
        checkInitialization(CONTINUE_ABILITY_FAILED);
        this.abilitySliceManager.continueAbilityReversibly(str);
    }

    public boolean reverseContinueAbility() throws IllegalStateException, UnsupportedOperationException {
        checkInitialization("reverse continue ability failed.");
        return this.abilitySliceManager.reverseContinueAbility();
    }

    public final ContinuationState getContinuationState() throws UnsupportedOperationException {
        checkInitialization("get continuation state failed.");
        return this.abilitySliceManager.getContinuationState();
    }

    public final String getOriginalDeviceId() throws UnsupportedOperationException {
        checkInitialization("get original device id failed.");
        return this.abilitySliceManager.getOriginalDeviceId();
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public void setDisplayOrientation(AbilityInfo.DisplayOrientation displayOrientation) {
        if (displayOrientation != null) {
            checkInitialization("set new orientation failed");
            if (this.abilitySliceManager.getWindowProxy() == null) {
                Log.error(LABEL, "window proxy is not initialized yet, set new orientation failed", new Object[0]);
            } else {
                super.setDisplayOrientation(displayOrientation);
            }
        } else {
            throw new IllegalArgumentException("invalid requested display orientation");
        }
    }

    public boolean acquireAbilityFormAsync(Intent intent, AbilityForm.OnAcquiredCallback onAcquiredCallback) {
        Log.info(LABEL, "acquire ability form", new Object[0]);
        if (intent == null || onAcquiredCallback == null) {
            throw new IllegalArgumentException("passing in intent and acquiredCallback must not be null");
        } else if (verifySelfPermission(AbilityForm.PERMISSION_REQUIRE_FORM) != 0) {
            Log.warn(LABEL, "acquireAbilityFormAsync permission denied", new Object[0]);
            return false;
        } else {
            AbilityFormAcquireConnection abilityFormAcquireConnection = new AbilityFormAcquireConnection(intent, onAcquiredCallback);
            intent.setFlags(32);
            boolean connectAbility = connectAbility(intent, abilityFormAcquireConnection);
            Log.info(LABEL, "acquire ability form done: %{public}b", Boolean.valueOf(connectAbility));
            return connectAbility;
        }
    }

    public void releaseAbilityForm(AbilityForm abilityForm) {
        Log.info(LABEL, "release ability form", new Object[0]);
        if (abilityForm != null) {
            synchronized (this.acquiringRecords) {
                IAbilityConnection iAbilityConnection = this.acquiringRecords.get(abilityForm);
                if (iAbilityConnection != null) {
                    disconnectAbility(iAbilityConnection);
                    Log.info(LABEL, "release ability form done", new Object[0]);
                } else {
                    throw new IllegalArgumentException("passing in abilityForm is not acquired");
                }
            }
            return;
        }
        throw new IllegalArgumentException("passing in abilityForm must not be null");
    }

    @Override // ohos.aafwk.ability.ILifecycle
    public final Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    public final Ability getAbility() {
        checkInitialization("get ability failed");
        return this.abilitySliceManager.getAbility();
    }

    private void dispatchLifecycle(Lifecycle.Event event, Intent intent) throws IllegalStateException {
        this.lifecycle.dispatchLifecycle(event, intent);
    }

    /* access modifiers changed from: package-private */
    public final void start(Intent intent) throws LifecycleException {
        if (this.currentState == AbilitySliceLifecycleExecutor.LifecycleState.INITIAL) {
            setUiAttachedAllowed(true);
            Bytrace.startTrace(2147483648L, "sliceOnStart");
            onStart(intent);
            Bytrace.finishTrace(2147483648L, "sliceOnStart");
            dispatchLifecycle(Lifecycle.Event.ON_START, intent);
            if (this.uiContent.isLatestUIAttached() || this.uiContent.isUiAttachedDisable()) {
                this.currentState = AbilitySliceLifecycleExecutor.LifecycleState.INACTIVE;
            } else {
                Log.error(LABEL, "UI must be setup correctly in onStart()", new Object[0]);
                throw new AbilitySliceRuntimeException("UI must be setup correctly in onStart()");
            }
        } else {
            throw new LifecycleException("Action(\" start \") is illegal for current state [" + this.currentState + "]");
        }
    }

    /* access modifiers changed from: package-private */
    public final void active() throws LifecycleException {
        if (this.currentState == AbilitySliceLifecycleExecutor.LifecycleState.INACTIVE) {
            setUiAttachedAllowed(true);
            Bytrace.startTrace(2147483648L, "sliceOnActive");
            onActive();
            Bytrace.finishTrace(2147483648L, "sliceOnActive");
            dispatchLifecycle(Lifecycle.Event.ON_ACTIVE, null);
            this.currentState = AbilitySliceLifecycleExecutor.LifecycleState.ACTIVE;
            synchronized (this.acquiringRecords) {
                for (AbilityForm abilityForm : this.acquiringRecords.keySet()) {
                    if (abilityForm != null) {
                        abilityForm.enableUpdatePush();
                    }
                }
            }
            return;
        }
        throw new LifecycleException("Action(\" Active \") is illegal for current state [" + this.currentState + "]");
    }

    /* access modifiers changed from: package-private */
    public final void inactive() throws LifecycleException {
        if (this.currentState == AbilitySliceLifecycleExecutor.LifecycleState.ACTIVE) {
            setUiAttachedAllowed(true);
            Bytrace.startTrace(2147483648L, "sliceOnInactive");
            onInactive();
            Bytrace.finishTrace(2147483648L, "sliceOnInactive");
            dispatchLifecycle(Lifecycle.Event.ON_INACTIVE, null);
            this.currentState = AbilitySliceLifecycleExecutor.LifecycleState.INACTIVE;
            return;
        }
        throw new LifecycleException("Action(\" inactive \") is illegal for current state [" + this.currentState + "]");
    }

    /* access modifiers changed from: package-private */
    public final void background() throws LifecycleException {
        if (this.currentState == AbilitySliceLifecycleExecutor.LifecycleState.BACKGROUND) {
            Log.warn("topslice has been changed to background", new Object[0]);
        } else if (this.currentState == AbilitySliceLifecycleExecutor.LifecycleState.INACTIVE) {
            setUiAttachedAllowed(false);
            Bytrace.startTrace(2147483648L, "sliceOnBackground");
            onBackground();
            Bytrace.finishTrace(2147483648L, "sliceOnBackground");
            dispatchLifecycle(Lifecycle.Event.ON_BACKGROUND, null);
            this.currentState = AbilitySliceLifecycleExecutor.LifecycleState.BACKGROUND;
            if (this.abilitySliceManager.getAbilityState() == 2) {
                this.uiContent.setLatestUIAttachedFlag(false);
            } else if (Log.isDebuggable()) {
                Log.debug(LABEL, "the entire ability is moving to background, no need to reset UIAttached flag", new Object[0]);
            }
            synchronized (this.acquiringRecords) {
                for (AbilityForm abilityForm : this.acquiringRecords.keySet()) {
                    if (abilityForm != null) {
                        abilityForm.disableUpdatePush();
                    }
                }
            }
        } else {
            throw new LifecycleException("Action(\" background \") is illegal for current state [" + this.currentState + "]");
        }
    }

    /* access modifiers changed from: package-private */
    public final void foreground(Intent intent) throws LifecycleException {
        if (this.currentState == AbilitySliceLifecycleExecutor.LifecycleState.BACKGROUND) {
            setUiAttachedAllowed(true);
            Bytrace.startTrace(2147483648L, "sliceOnForeground");
            onForeground(intent);
            Bytrace.finishTrace(2147483648L, "sliceOnForeground");
            dispatchLifecycle(Lifecycle.Event.ON_FOREGROUND, intent);
            this.currentState = AbilitySliceLifecycleExecutor.LifecycleState.INACTIVE;
            this.uiContent.ensureLatestUIAttached();
            return;
        }
        throw new LifecycleException("Action(\" foreground \") is illegal for current state [" + this.currentState + "]");
    }

    /* access modifiers changed from: package-private */
    public final void stop() throws LifecycleException {
        if (this.currentState == AbilitySliceLifecycleExecutor.LifecycleState.BACKGROUND) {
            setUiAttachedAllowed(false);
            Bytrace.startTrace(2147483648L, "sliceOnStop");
            onStop();
            Bytrace.finishTrace(2147483648L, "sliceOnStop");
            dispatchLifecycle(Lifecycle.Event.ON_STOP, null);
            this.currentState = AbilitySliceLifecycleExecutor.LifecycleState.INITIAL;
            try {
                this.abilitySliceManager.disconnectAbility(this, null);
            } catch (IllegalStateException unused) {
                Log.info(LABEL, "conn is busy while slice disconnectAbility", new Object[0]);
            }
            this.uiContent.reset();
            this.uiContent = null;
            this.isInitialized = false;
            return;
        }
        throw new LifecycleException("Action(\" stop \") is illegal for current state [" + this.currentState + "]");
    }

    /* access modifiers changed from: package-private */
    public final void dumpAbilitySlice(String str, PrintWriter printWriter, String str2) {
        printWriter.print(str);
        printWriter.println("Ability slice state: " + this.currentState);
        printWriter.print(str);
        if (this.resultData != null) {
            printWriter.println("Ability slice result: " + this.resultData.toUri());
        }
        printWriter.print(str);
        printWriter.println("Ability slice uiContent:");
        SliceUIContent sliceUIContent = this.uiContent;
        if (sliceUIContent == null) {
            printWriter.print(Ability.PREFIX + str);
            printWriter.println("null");
        } else {
            sliceUIContent.dump(Ability.PREFIX + str, printWriter, str2);
        }
        printWriter.print(str);
        printWriter.println("Ability slice connected service list:");
        AbilitySliceManager abilitySliceManager2 = this.abilitySliceManager;
        abilitySliceManager2.dumpServiceList(Ability.PREFIX + str, printWriter, this);
    }

    /* access modifiers changed from: package-private */
    public boolean scheduleStartContinuation() {
        if (!(this instanceof IAbilityContinuation)) {
            return true;
        }
        Bytrace.startTrace(2147483648L, "sliceOnStartContinuation");
        boolean onStartContinuation = ((IAbilityContinuation) this).onStartContinuation();
        Bytrace.finishTrace(2147483648L, "sliceOnStartContinuation");
        return onStartContinuation;
    }

    /* access modifiers changed from: package-private */
    public boolean scheduleSaveData(IntentParams intentParams) {
        if (!(this instanceof IAbilityContinuation)) {
            return true;
        }
        Bytrace.startTrace(2147483648L, "sliceOnSaveData");
        boolean onSaveData = ((IAbilityContinuation) this).onSaveData(intentParams);
        Bytrace.finishTrace(2147483648L, "sliceOnSaveData");
        return onSaveData;
    }

    /* access modifiers changed from: package-private */
    public boolean scheduleRestoreData(IntentParams intentParams) {
        if (!(this instanceof IAbilityContinuation)) {
            return true;
        }
        Bytrace.startTrace(2147483648L, "sliceOnRestoreData");
        boolean onRestoreData = ((IAbilityContinuation) this).onRestoreData(intentParams);
        Bytrace.finishTrace(2147483648L, "sliceOnRestoreData");
        return onRestoreData;
    }

    /* access modifiers changed from: package-private */
    public void scheduleCompleteContinuation(int i) {
        if (this instanceof IAbilityContinuation) {
            Bytrace.startTrace(2147483648L, "sliceOnCompleteContinuation");
            ((IAbilityContinuation) this).onCompleteContinuation(i);
            Bytrace.finishTrace(2147483648L, "sliceOnCompleteContinuation");
        }
    }

    /* access modifiers changed from: package-private */
    public void notifyRemoteTerminated() {
        if (this instanceof IAbilityContinuation) {
            Bytrace.startTrace(2147483648L, "notifyRemoteTerminated");
            ((IAbilityContinuation) this).onRemoteTerminated();
            Bytrace.finishTrace(2147483648L, "notifyRemoteTerminated");
        }
    }

    /* access modifiers changed from: package-private */
    public void viewEnterAnimator() {
        AbilitySliceAnimator abilitySliceAnimator = this.abilitySliceManager.getAbilitySliceAnimator();
        if (abilitySliceAnimator == null) {
            Log.info(LABEL, "ability slice animator is set to null", new Object[0]);
        } else {
            this.uiContent.viewEnterAnimator(abilitySliceAnimator);
        }
    }

    /* access modifiers changed from: package-private */
    public void viewExitAnimator() {
        AbilitySliceAnimator abilitySliceAnimator = this.abilitySliceManager.getAbilitySliceAnimator();
        if (abilitySliceAnimator == null) {
            Log.info(LABEL, "ability slice animator is set to null", new Object[0]);
        } else {
            this.uiContent.viewExitAnimator(abilitySliceAnimator);
        }
    }

    /* access modifiers changed from: package-private */
    public void stopViewAnimator() {
        this.uiContent.stopViewAnimator();
    }

    /* access modifiers changed from: package-private */
    public void setUiAttachedAllowed(boolean z) {
        this.uiContent.setUiAttachedAllowed(z);
    }

    /* access modifiers changed from: package-private */
    public void setLatestUIAttachedFlag(boolean z) {
        this.uiContent.setLatestUIAttachedFlag(z);
    }

    /* access modifiers changed from: package-private */
    public void setUiAttachedDisable(boolean z) {
        this.uiContent.setUiAttachedDisable(z);
    }

    private void checkInitialization(String str) {
        if (this.abilitySliceManager == null) {
            Log.error(LABEL, "abilitySliceManager is null, %{public}s", str);
            throw new IllegalStateException("abilitySliceManager is null, " + str);
        }
    }

    private void checkValidRequestCode(int i) {
        if ((-65536 & i) != 0) {
            throw new IllegalArgumentException("Can only use lower 16 bits for requestCode");
        }
    }
}
