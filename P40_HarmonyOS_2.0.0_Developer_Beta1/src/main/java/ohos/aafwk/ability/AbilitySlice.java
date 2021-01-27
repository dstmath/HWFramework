package ohos.aafwk.ability;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ohos.aafwk.ability.AbilityForm;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.AbilitySliceLifecycleExecutor;
import ohos.aafwk.ability.FormException;
import ohos.aafwk.ability.IAbilityFormProvider;
import ohos.aafwk.ability.IFormHost;
import ohos.aafwk.ability.Lifecycle;
import ohos.aafwk.ability.startsetting.AbilityStartSetting;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.agp.animation.AnimatorProperty;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.ComponentParent;
import ohos.agp.components.ComponentProvider;
import ohos.agp.components.DirectionalLayout;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.LayoutScatterException;
import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;
import ohos.app.AbilityContext;
import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ElementName;
import ohos.bundle.FormInfo;
import ohos.bundle.IBundleManager;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.global.resource.ResourceManager;
import ohos.multimodalinput.event.KeyEvent;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.tools.Bytrace;
import ohos.utils.Pair;
import ohos.utils.fastjson.JSONObject;

public class AbilitySlice extends AbilityContext implements ILifecycle {
    private static final String ABILITY_NAME_KEY = "abilityName";
    private static final String BUNDLE_NAME_KEY = "bundleName";
    private static final int CODE_ACQUIRE_FORM = 0;
    private static final int CODE_UPDATE_FORM = 1;
    private static final String CONTINUE_ABILITY_FAILED = "continue ability failed.";
    private static final int DELETE_FORM = 3;
    private static final int DISABLE_FORM_UPDATE = 6;
    private static final int ENABLE_FORM_UPDATE = 5;
    private static final int FORM_ORIENTATION_LANDSCAPE = 2;
    private static final int FORM_ORIENTATION_PORTRAIT = 1;
    private static final LogLabel LABEL = LogLabel.create();
    private static final int MESSAGE_EVENT = 101;
    private static final String PARAMS_KEY = "params";
    public static final String PARAM_FORM_DIMENSION_KEY = "ohos.extra.param.key.form_dimension";
    public static final String PARAM_FORM_HEIGHT_KEY = "ohos.extra.param.key.form_height";
    public static final String PARAM_FORM_ID_KEY = "ohos.extra.param.key.form_id";
    public static final String PARAM_FORM_NAME_KEY = "ohos.extra.param.key.form_name";
    private static final String PARAM_FORM_ORIENTATION_KEY = "ohos.extra.param.key.form_orientation";
    public static final String PARAM_FORM_WIDTH_KEY = "ohos.extra.param.key.form_width";
    public static final String PARAM_MODULE_NAME_KEY = "ohos.extra.param.key.module_name";
    private static final int RELEASE_FORM = 8;
    private static final int REQUEST_CODE_MARK = 65535;
    private static final int ROUTER_EVENT = 100;
    private final Object FORM_LOCK = new Object();
    private final Object LOCK = new Object();
    private AbilitySliceManager abilitySliceManager;
    private final Map<AbilityForm, IAbilityConnection> acquiringRecords = new HashMap();
    private final Map<Integer, FormCallback> appCallbacks = new HashMap();
    private volatile FormHostClient client;
    private final Map<Integer, Component> componentMap = new HashMap();
    private volatile AbilitySliceLifecycleExecutor.LifecycleState currentState = AbilitySliceLifecycleExecutor.LifecycleState.INITIAL;
    private final Map<Integer, InstantProvider> instantProviders = new HashMap();
    private boolean isInitialized = false;
    private final Map<Integer, Pair<Integer, DirectionalLayout>> layouts = new HashMap();
    private Lifecycle lifecycle = new Lifecycle();
    private Intent resultData;
    private SliceUIContent uiContent;

    public interface FormCallback {
        public static final int OHOS_FORM_ACQUIRE_SUCCESS = 0;
        public static final int OHOS_FORM_APPLY_FAILURE = 2;
        public static final int OHOS_FORM_PREVIEW_FAILURE = 1;
        public static final int OHOS_FORM_REAPPLY_FAILURE = 3;

        void onAcquired(int i, Form form);

        void onFormUninstalled(int i);
    }

    /* access modifiers changed from: protected */
    public void onAbilityResult(int i, int i2, Intent intent) {
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

    /* access modifiers changed from: private */
    public class JsFormEventHandler extends EventHandler {
        private int formID;

        public JsFormEventHandler(int i) {
            super(EventRunner.current());
            this.formID = i;
        }

        @Override // ohos.eventhandler.EventHandler
        public void processEvent(InnerEvent innerEvent) {
            int i = innerEvent.eventId;
            if (i == 100) {
                JSONObject jSONObject = (JSONObject) innerEvent.object;
                if (jSONObject == null || !jSONObject.containsKey("bundleName") || !jSONObject.containsKey(AbilitySlice.ABILITY_NAME_KEY)) {
                    Log.error(AbilitySlice.LABEL, "param illegale, bundleName or abilityName not exist", new Object[0]);
                    return;
                }
                String string = jSONObject.getString("bundleName");
                String string2 = jSONObject.getString(AbilitySlice.ABILITY_NAME_KEY);
                ElementName elementName = new ElementName("", string, string2);
                Intent intent = new Intent();
                intent.setElement(elementName);
                if (jSONObject.containsKey(AbilitySlice.PARAMS_KEY)) {
                    intent.setParam(AbilitySlice.PARAMS_KEY, jSONObject.getString(AbilitySlice.PARAMS_KEY));
                }
                Log.debug(AbilitySlice.LABEL, "process route event, bundleName: %{public}s, abilityName: %{public}s", string, string2);
                AbilitySlice.this.startAbility(intent);
            } else if (i == 101) {
                Log.debug(AbilitySlice.LABEL, "process message event", new Object[0]);
                if (innerEvent.object instanceof Intent) {
                    try {
                        AbilitySlice.this.requestForm(this.formID, (Intent) innerEvent.object);
                    } catch (FormException unused) {
                        Log.error(AbilitySlice.LABEL, "process request form fail", new Object[0]);
                    }
                }
            } else {
                Log.error(AbilitySlice.LABEL, "err event type: %{public}d", Integer.valueOf(i));
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void init(Context context, AbilitySliceManager abilitySliceManager2) {
        if (this.isInitialized) {
            Log.warn(LABEL, "AbilitySlice is already init", new Object[0]);
        } else if (context == null || abilitySliceManager2 == null) {
            Log.error(LABEL, "illegal initialization: context = %{public}s, asm = %{public}s", context, abilitySliceManager2);
            throw new IllegalArgumentException("illegal initialization: context = " + context + ", asm = " + abilitySliceManager2);
        } else {
            this.isInitialized = true;
            attachBaseContext(context);
            this.abilitySliceManager = abilitySliceManager2;
            this.uiContent = new SliceUIContent(abilitySliceManager2.getWindowProxy());
            Log.debug(LABEL, "AbilitySlice init done", new Object[0]);
        }
    }

    /* access modifiers changed from: protected */
    public void onActive() {
        Set<Integer> keySet;
        synchronized (this.FORM_LOCK) {
            keySet = this.appCallbacks.keySet();
        }
        if (!keySet.isEmpty()) {
            lifecycleUpdate(new ArrayList(keySet), 5);
        }
    }

    /* access modifiers changed from: protected */
    public void onBackground() {
        Set<Integer> keySet;
        synchronized (this.FORM_LOCK) {
            keySet = this.appCallbacks.keySet();
        }
        if (!keySet.isEmpty()) {
            lifecycleUpdate(new ArrayList(keySet), 6);
        }
    }

    /* access modifiers changed from: protected */
    public void onBackPressed() {
        terminate();
    }

    public void postTask(Runnable runnable, long j) {
        getAbility().postTask(runnable, j);
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

        private synchronized AnimatorProperty createCurComponentAnimator(AbilitySliceAnimator abilitySliceAnimator) {
            ComponentParent componentParent = this.curComponentContainer != null ? this.curComponentContainer.getComponentParent() : null;
            Component component = componentParent instanceof Component ? (Component) componentParent : null;
            if (component == null || !component.isBoundToWindow()) {
                return null;
            }
            if (abilitySliceAnimator != null && abilitySliceAnimator.isDefaultAnimator()) {
                abilitySliceAnimator.constructDefaultAnimator((float) component.getRight());
            }
            return component.createAnimatorProperty();
        }

        /* access modifiers changed from: package-private */
        public synchronized void componentEnterAnimator(AbilitySliceAnimator abilitySliceAnimator) {
            AnimatorProperty createCurComponentAnimator = createCurComponentAnimator(abilitySliceAnimator);
            if (createCurComponentAnimator != null) {
                Log.info(AbilitySlice.LABEL, "start enter animator", new Object[0]);
                abilitySliceAnimator.buildEnterAnimator(createCurComponentAnimator).start();
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void componentExitAnimator(AbilitySliceAnimator abilitySliceAnimator) {
            AnimatorProperty createCurComponentAnimator = createCurComponentAnimator(abilitySliceAnimator);
            if (createCurComponentAnimator != null) {
                Log.info(AbilitySlice.LABEL, "start exit animator", new Object[0]);
                abilitySliceAnimator.buildExitAnimator(createCurComponentAnimator).start();
            }
        }

        /* access modifiers changed from: package-private */
        public synchronized void stopComponentAnimator() {
            AnimatorProperty createCurComponentAnimator = createCurComponentAnimator(null);
            if (createCurComponentAnimator != null) {
                createCurComponentAnimator.end();
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
            checkInitialization("setUIContent with componentContainer failed");
            this.uiContent.updateUIContent(componentContainer);
            return;
        }
        Log.error(LABEL, "componentContainer must be valid", new Object[0]);
        throw new AbilitySliceRuntimeException("componentContainer must be valid");
    }

    /* access modifiers changed from: package-private */
    public final ComponentContainer getCurrentUI() {
        SliceUIContent sliceUIContent = this.uiContent;
        if (sliceUIContent != null) {
            return sliceUIContent.curComponentContainer;
        }
        Log.error(LABEL, "ui Content is not inited", new Object[0]);
        return null;
    }

    public Component findComponentById(int i) {
        checkInitialization("find component by ID failed");
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
        } else if (abilitySlice.equals(this)) {
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
        startAbility(intent, AbilityStartSetting.getEmptySetting());
    }

    public void startAbility(Intent intent, AbilityStartSetting abilityStartSetting) {
        if (intent != null) {
            checkInitialization("startAbility failed");
            this.abilitySliceManager.startAbility(intent, abilityStartSetting);
            return;
        }
        Log.error(LABEL, "intent must be assigned for startAbility", new Object[0]);
        throw new IllegalArgumentException("intent must be assigned for startAbility");
    }

    public void startAbilityForResult(Intent intent, int i, AbilityStartSetting abilityStartSetting) {
        if (intent != null) {
            checkValidRequestCode(i);
            checkInitialization("startAbilityForResult failed");
            this.abilitySliceManager.startAbilityForResult(this, intent, i, abilityStartSetting);
            return;
        }
        Log.error(LABEL, "intent must be assigned for startAbilityForResult", new Object[0]);
        throw new IllegalArgumentException("intent must be assigned for startAbilityForResult");
    }

    public void startAbilityForResult(Intent intent, int i) {
        startAbilityForResult(intent, i, AbilityStartSetting.getEmptySetting());
    }

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
        } else if (verifyCallingOrSelfPermission(AbilityForm.PERMISSION_REQUIRE_FORM) != 0) {
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

    private FormHostClient getClient() {
        if (this.client == null) {
            synchronized (this.LOCK) {
                if (this.client == null) {
                    this.client = new FormHostClient();
                }
            }
        }
        return this.client;
    }

    /* access modifiers changed from: package-private */
    public class FormHostClient extends IFormHost.FormHostStub {
        public FormHostClient() {
        }

        @Override // ohos.aafwk.ability.IFormHost
        public void onAcquired(Form form) {
            processFormUpdate(form);
        }

        @Override // ohos.aafwk.ability.IFormHost
        public void onUpdate(Form form) {
            processFormUpdate(form);
        }

        @Override // ohos.aafwk.ability.IFormHost
        public void onFormUninstalled(List<Integer> list) throws RemoteException {
            FormCallback formCallback;
            if (list == null || list.isEmpty()) {
                Log.warn(AbilitySlice.LABEL, "onFormUninstalled, no form id need to be processed", new Object[0]);
                return;
            }
            for (Integer num : list) {
                int intValue = num.intValue();
                synchronized (AbilitySlice.this.FORM_LOCK) {
                    formCallback = (FormCallback) AbilitySlice.this.appCallbacks.get(Integer.valueOf(intValue));
                    AbilitySlice.this.cleanFormResource(intValue);
                }
                if (formCallback != null) {
                    formCallback.onFormUninstalled(intValue);
                }
            }
        }

        private void processFormUpdate(Form form) {
            if (form == null) {
                Log.error(AbilitySlice.LABEL, "on acquired ability form error, form is empty", new Object[0]);
            } else if (AbilitySlice.this.getContext().getUITaskDispatcher() == null) {
                Log.error(AbilitySlice.LABEL, "get ui dispatcher failed", new Object[0]);
            } else {
                Log.debug(AbilitySlice.LABEL, "on acquired ability form %{public}d", Integer.valueOf(form.formId));
                AbilitySlice.this.getContext().getUITaskDispatcher().asyncDispatch(new Runnable(form) {
                    /* class ohos.aafwk.ability.$$Lambda$AbilitySlice$FormHostClient$52_kz6XsWNzf9U8kvpHhKXqyIo */
                    private final /* synthetic */ Form f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        AbilitySlice.FormHostClient.this.lambda$processFormUpdate$0$AbilitySlice$FormHostClient(this.f$1);
                    }
                });
            }
        }

        public /* synthetic */ void lambda$processFormUpdate$0$AbilitySlice$FormHostClient(Form form) {
            AbilitySlice.this.handleFormMessage(1, form);
        }
    }

    public boolean acquireForm(Intent intent, FormCallback formCallback) throws FormException {
        Log.info(LABEL, "acquireForm begin", new Object[0]);
        if (intent == null || formCallback == null) {
            throw new FormException(FormException.FormError.INPUT_PARAM_INVALID, "passing in intent and callback must not be null");
        }
        setDefaultIntentParam(intent);
        if (checkIntentValid(intent)) {
            FormManager instance = FormManager.getInstance();
            if (instance != null) {
                Form addForm = instance.addForm(intent, getClient());
                if (addForm != null) {
                    TaskDispatcher uITaskDispatcher = getUITaskDispatcher();
                    if (uITaskDispatcher != null) {
                        synchronized (this.FORM_LOCK) {
                            if (!this.appCallbacks.containsKey(Integer.valueOf(addForm.formId))) {
                                this.appCallbacks.put(Integer.valueOf(addForm.formId), formCallback);
                                generateComponents(getContext(), addForm, intent.getIntParam(PARAM_FORM_WIDTH_KEY, -2), intent.getIntParam(PARAM_FORM_HEIGHT_KEY, -2), uITaskDispatcher);
                            } else {
                                throw new FormException(FormException.FormError.FORM_DUPLICATE_ADDED);
                            }
                        }
                        return true;
                    }
                    throw new FormException(FormException.FormError.INTERNAL_ERROR.toString(), "ui dispatcher is not found");
                }
                throw new FormException(FormException.FormError.INTERNAL_ERROR.toString(), "fms acquire form failed");
            }
            throw new FormException(FormException.FormError.FMS_BINDER_ERROR);
        }
        throw new FormException(FormException.FormError.INPUT_PARAM_INVALID, "intent param is not correct");
    }

    private void setDefaultIntentParam(Intent intent) {
        ResourceManager resourceManager = getResourceManager();
        if (resourceManager == null || resourceManager.getConfiguration() == null || resourceManager.getConfiguration().direction != 1) {
            intent.setParam(PARAM_FORM_ORIENTATION_KEY, 1);
        } else {
            intent.setParam(PARAM_FORM_ORIENTATION_KEY, 2);
        }
        if (!intent.hasParameter(PARAM_FORM_ID_KEY)) {
            intent.setParam(PARAM_FORM_ID_KEY, 0);
        }
    }

    private boolean checkIntentValid(Intent intent) {
        ElementName element = intent.getElement();
        if (element == null) {
            Log.error(LABEL, "bundleName and abilityName is not set in intent", new Object[0]);
            return false;
        }
        String bundleName = element.getBundleName();
        String abilityName = element.getAbilityName();
        String stringParam = intent.getStringParam(PARAM_MODULE_NAME_KEY);
        if (isEmpty(bundleName) || isEmpty(abilityName) || isEmpty(stringParam)) {
            Log.error(LABEL, "bundleName or abilityName or moduleName is not set in intent", new Object[0]);
            return false;
        }
        int intParam = intent.getIntParam(PARAM_FORM_ID_KEY, -1);
        if (intParam < 0) {
            Log.error(LABEL, "form id should not be negative in intent", new Object[0]);
            return false;
        }
        synchronized (this.FORM_LOCK) {
            if (this.appCallbacks.containsKey(Integer.valueOf(intParam))) {
                Log.error(LABEL, "form has already acquired, do not support acquire twice", new Object[0]);
                return false;
            }
        }
        if (intent.getIntParam(PARAM_FORM_DIMENSION_KEY, 1) <= 0) {
            Log.error(LABEL, "dimension should not be zero or negative in intent", new Object[0]);
            return false;
        }
        int intParam2 = intent.getIntParam(PARAM_FORM_WIDTH_KEY, -2);
        int intParam3 = intent.getIntParam(PARAM_FORM_HEIGHT_KEY, -2);
        if (intParam2 >= -2 && intParam3 >= -2) {
            return true;
        }
        Log.error(LABEL, "width or height is not set correctly in intent", new Object[0]);
        return false;
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFormMessage(int i, Form form) {
        DirectionalLayout directionalLayout;
        int i2;
        boolean z;
        FormCallback formCallback;
        int addNewLayoutComponent;
        int i3;
        boolean z2 = i == 0;
        synchronized (this.FORM_LOCK) {
            Pair<Integer, DirectionalLayout> pair = this.layouts.get(Integer.valueOf(form.formId));
            if (pair == null) {
                Log.error(LABEL, "handleFormMessage, form has no layout, form id: %{public}d ", Integer.valueOf(form.formId));
                return;
            }
            int intValue = ((Integer) pair.f).intValue();
            Component component = this.componentMap.get(Integer.valueOf(form.formId));
            directionalLayout = (DirectionalLayout) pair.s;
            InstantProvider instantProvider = form.getInstantProvider();
            if (instantProvider != null) {
                if (component == null) {
                    i3 = addJsPreviewComponent(form, directionalLayout, instantProvider);
                } else {
                    i3 = applyJsComponentAction(component, instantProvider);
                }
                i2 = i3;
                z = true;
            } else {
                if (form.remoteComponent == null) {
                    addNewLayoutComponent = addPreviewComponent(form, directionalLayout);
                } else if (component == null) {
                    addNewLayoutComponent = addCachedFormComponent(form, directionalLayout);
                } else if (intValue == form.remoteComponent.getLayoutId()) {
                    addNewLayoutComponent = applyComponentAction(form, component);
                } else {
                    addNewLayoutComponent = addNewLayoutComponent(form, component, directionalLayout);
                }
                i2 = addNewLayoutComponent;
                z = false;
            }
            form.setComponent(directionalLayout);
            formCallback = this.appCallbacks.get(Integer.valueOf(form.formId));
        }
        if (z2 && formCallback != null) {
            Log.info(LABEL, "handleMessage, call user implement of form %{public}d", Integer.valueOf(form.formId));
            if (directionalLayout != null && !z) {
                directionalLayout.setClickedListener(new Component.ClickedListener(form) {
                    /* class ohos.aafwk.ability.$$Lambda$AbilitySlice$8P_nuxPUNsDHkVfktVP90_9Y8 */
                    private final /* synthetic */ Form f$1;

                    {
                        this.f$1 = r2;
                    }

                    @Override // ohos.agp.components.Component.ClickedListener
                    public final void onClick(Component component) {
                        AbilitySlice.this.lambda$handleFormMessage$0$AbilitySlice(this.f$1, component);
                    }
                });
            }
            formCallback.onAcquired(i2, form);
        }
    }

    public /* synthetic */ void lambda$handleFormMessage$0$AbilitySlice(Form form, Component component) {
        startFullPage(form);
    }

    private int addPreviewComponent(Form form, DirectionalLayout directionalLayout) {
        Log.debug(LABEL, "generate component using preview ID of form %{public}d", Integer.valueOf(form.formId));
        ComponentContainer previewComponents = getPreviewComponents(getContext(), form.getBundleName(), form.previewID);
        if (previewComponents == null) {
            return 1;
        }
        addComponentToLayout(previewComponents, directionalLayout);
        this.componentMap.put(Integer.valueOf(form.formId), previewComponents);
        return 0;
    }

    private int addJsPreviewComponent(Form form, DirectionalLayout directionalLayout, InstantProvider instantProvider) {
        try {
            Component instantComponent = instantProvider.getInstantComponent(getContext());
            directionalLayout.addComponent(instantComponent);
            this.componentMap.put(Integer.valueOf(form.formId), instantComponent);
            instantProvider.setAbilityHandler(new JsFormEventHandler(form.formId));
            instantProvider.setEventHandler();
            this.instantProviders.put(Integer.valueOf(form.formId), instantProvider);
            return 0;
        } catch (InstantProviderException e) {
            Log.error(LABEL, "get instant component failed, err: %{public}s", e.getMessage());
            return 1;
        }
    }

    private int addCachedFormComponent(Form form, DirectionalLayout directionalLayout) {
        Log.debug(LABEL, "addCachedFormComponent, component not generated, apply one", new Object[0]);
        try {
            form.remoteComponent.inflateLayout(getContext());
            ComponentContainer allComponents = form.remoteComponent.getAllComponents();
            if (allComponents == null) {
                return 2;
            }
            form.remoteComponent.applyAction(allComponents);
            addComponentToLayout(allComponents, directionalLayout);
            this.componentMap.put(Integer.valueOf(form.formId), allComponents);
            return 0;
        } catch (ComponentProvider.ComponentProviderException | LayoutScatterException unused) {
            Log.error(LABEL, "addCachedFormComponent, component provider apply failed", new Object[0]);
            return 2;
        }
    }

    private int applyComponentAction(Form form, Component component) {
        Log.debug(LABEL, "component existed, apply action, formId:%{public}d", Integer.valueOf(form.formId));
        if (component instanceof ComponentContainer) {
            try {
                form.remoteComponent.applyAction((ComponentContainer) component);
            } catch (ComponentProvider.ComponentProviderException unused) {
                Log.error(LABEL, "applyComponentAction, apply action on component container failed", new Object[0]);
                return 3;
            }
        }
        return 0;
    }

    private int applyJsComponentAction(Component component, InstantProvider instantProvider) {
        instantProvider.setComponent(component);
        instantProvider.update();
        return 0;
    }

    private int addNewLayoutComponent(Form form, Component component, DirectionalLayout directionalLayout) {
        Log.debug(LABEL, "layout id changed, generate new component of from %{public}d", Integer.valueOf(form.formId));
        try {
            form.remoteComponent.inflateLayout(getContext());
            ComponentContainer allComponents = form.remoteComponent.getAllComponents();
            if (allComponents == null) {
                return 2;
            }
            form.remoteComponent.applyAction(allComponents);
            directionalLayout.removeComponent(component);
            ComponentContainer.LayoutConfig layoutConfig = directionalLayout.getLayoutConfig();
            layoutConfig.width = allComponents.getLayoutConfig().width;
            layoutConfig.height = allComponents.getLayoutConfig().height;
            directionalLayout.setLayoutConfig(layoutConfig);
            directionalLayout.addComponent(allComponents, -1, -1);
            this.layouts.put(Integer.valueOf(form.formId), new Pair<>(Integer.valueOf(form.remoteComponent.getLayoutId()), directionalLayout));
            this.componentMap.put(Integer.valueOf(form.formId), allComponents);
            return 0;
        } catch (ComponentProvider.ComponentProviderException | LayoutScatterException unused) {
            Log.error(LABEL, "addNewLayoutView, remote component apply failed", new Object[0]);
            return 2;
        }
    }

    private void startFullPage(Form form) {
        if (form == null) {
            Log.error(LABEL, "startFullPage, form is null", new Object[0]);
            return;
        }
        Intent intent = new Intent();
        intent.setElement(new ElementName("", form.getBundleName(), form.getAbilityName()));
        intent.setParam(PARAM_FORM_ID_KEY, form.formId);
        startAbility(intent);
    }

    private void addComponentToLayout(Component component, DirectionalLayout directionalLayout) {
        ComponentContainer.LayoutConfig layoutConfig = directionalLayout.getLayoutConfig();
        if (layoutConfig.width == -2 && layoutConfig.height == -2) {
            layoutConfig.width = component.getLayoutConfig().width;
            layoutConfig.height = component.getLayoutConfig().height;
            directionalLayout.setLayoutConfig(layoutConfig);
            Log.debug(LABEL, "addComponentToLayout current component width %{public}d, height %{public}d", Integer.valueOf(layoutConfig.width), Integer.valueOf(layoutConfig.height));
        }
        directionalLayout.addComponent(component, -1, -1);
    }

    private void generateComponents(Context context, Form form, int i, int i2, TaskDispatcher taskDispatcher) {
        Pair<Integer, DirectionalLayout> pair;
        DirectionalLayout directionalLayout = new DirectionalLayout(context);
        directionalLayout.setLayoutConfig(new ComponentContainer.LayoutConfig(i, i2));
        if (form.getInstantProvider() != null) {
            pair = new Pair<>(-1, directionalLayout);
        } else if (form.remoteComponent != null) {
            pair = new Pair<>(Integer.valueOf(form.remoteComponent.getLayoutId()), directionalLayout);
        } else {
            pair = new Pair<>(Integer.valueOf(form.previewID), directionalLayout);
        }
        this.layouts.put(Integer.valueOf(form.formId), pair);
        this.componentMap.remove(Integer.valueOf(form.formId));
        taskDispatcher.asyncDispatch(new Runnable(form) {
            /* class ohos.aafwk.ability.$$Lambda$AbilitySlice$EHMG1o8talHXQAlkFSyNKUK7iA */
            private final /* synthetic */ Form f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.lang.Runnable
            public final void run() {
                AbilitySlice.this.lambda$generateComponents$1$AbilitySlice(this.f$1);
            }
        });
    }

    public /* synthetic */ void lambda$generateComponents$1$AbilitySlice(Form form) {
        handleFormMessage(0, form);
    }

    private ComponentContainer getPreviewComponents(Context context, String str, int i) {
        Log.debug(LABEL, "getPreviewComponents....", new Object[0]);
        Context createBundleContext = context.createBundleContext(str, 2);
        if (createBundleContext == null) {
            Log.debug(LABEL, "getPreviewComponents context is null", new Object[0]);
            return null;
        }
        ResourceManager resourceManager = createBundleContext.getResourceManager();
        if (resourceManager == null) {
            Log.info(LABEL, "getPreviewComponents resManager is null", new Object[0]);
            return null;
        }
        try {
            Component parse = LayoutScatter.getInstance(this).clone(createBundleContext, resourceManager).parse(i, null, false);
            if (parse == null) {
                Log.info(LABEL, "getPreviewComponents component is null", new Object[0]);
                return null;
            } else if (parse instanceof ComponentContainer) {
                ComponentContainer componentContainer = (ComponentContainer) parse;
                Log.info(LABEL, "getPreviewComponents addComponent success.", new Object[0]);
                return componentContainer;
            } else {
                Log.info(LABEL, "getPreviewComponents addComponent failed", new Object[0]);
                return null;
            }
        } catch (ComponentProvider.ComponentProviderException | LayoutScatterException e) {
            Log.error(LABEL, "getPreviewComponents, parse layout id failed %{public}s", e.getMessage());
            return null;
        }
    }

    private boolean deleteForm(int i, int i2) throws FormException {
        Log.info(LABEL, "deleteForm begin of form %{public}d", Integer.valueOf(i));
        if (this.client != null) {
            FormManager instance = FormManager.getInstance();
            if (instance == null) {
                Log.error(LABEL, "deleteForm, formManager is null", new Object[0]);
                throw new FormException(FormException.FormError.FMS_BINDER_ERROR);
            } else if (instance.deleteForm(i, this.client, i2)) {
                synchronized (this.FORM_LOCK) {
                    cleanFormResource(i);
                }
                return true;
            } else {
                throw new FormException(FormException.FormError.INTERNAL_ERROR);
            }
        } else {
            throw new FormException(FormException.FormError.INTERNAL_ERROR, "form has no client");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanFormResource(int i) {
        this.layouts.remove(Integer.valueOf(i));
        this.componentMap.remove(Integer.valueOf(i));
        this.appCallbacks.remove(Integer.valueOf(i));
        if (this.appCallbacks.isEmpty()) {
            this.client = null;
        }
        InstantProvider instantProvider = this.instantProviders.get(Integer.valueOf(i));
        if (instantProvider != null) {
            instantProvider.destroy();
            this.instantProviders.remove(Integer.valueOf(i));
        }
    }

    public boolean deleteForm(int i) throws FormException {
        if (i > 0) {
            return deleteForm(i, 3);
        }
        throw new FormException(FormException.FormError.INPUT_PARAM_INVALID, "passing in form id can't be negative");
    }

    public boolean releaseForm(int i) throws FormException {
        if (i > 0) {
            return deleteForm(i, 8);
        }
        throw new FormException(FormException.FormError.INPUT_PARAM_INVALID, "passing in form id can't be negative");
    }

    public List<FormInfo> getAllForms() throws FormException {
        new ArrayList(0);
        IBundleManager bundleManager = getBundleManager();
        if (bundleManager != null) {
            try {
                return bundleManager.getAllForms();
            } catch (RemoteException e) {
                Log.error(LABEL, "getAllForms exception ", new Object[0]);
                FormException.FormError formError = FormException.FormError.SEND_BMS_MSG_ERROR;
                throw new FormException(formError, "get forms occurs exception:" + e.getMessage());
            }
        } else {
            Log.error(LABEL, "getBundleManager failed when getAllForms", new Object[0]);
            throw new FormException(FormException.FormError.BMS_BINDER_ERROR);
        }
    }

    private boolean lifecycleUpdate(List<Integer> list, int i) {
        if (this.client == null) {
            Log.error(LABEL, "lifecycleUpdate, client records has no client", new Object[0]);
            return false;
        }
        try {
            FormManager instance = FormManager.getInstance();
            if (instance != null) {
                return instance.lifecycleUpdate(list, this.client, i);
            }
            Log.error(LABEL, "lifecycleUpdate, get form manager instance failed", new Object[0]);
            return false;
        } catch (RemoteException unused) {
            Log.error(LABEL, "lifecycleUpdate, occurs remote exception", new Object[0]);
            return false;
        }
    }

    public List<FormInfo> getFormsByApp(String str) throws FormException {
        new ArrayList(0);
        IBundleManager bundleManager = getBundleManager();
        if (bundleManager != null) {
            try {
                return bundleManager.getFormsByApp(str);
            } catch (RemoteException e) {
                Log.error(LABEL, "getFormsByApp exception ", new Object[0]);
                FormException.FormError formError = FormException.FormError.SEND_BMS_MSG_ERROR;
                throw new FormException(formError, "get forms occurs exception:" + e.getMessage());
            }
        } else {
            Log.error(LABEL, "getBundleManager failed when getFormsByApp", new Object[0]);
            throw new FormException(FormException.FormError.BMS_BINDER_ERROR);
        }
    }

    public List<FormInfo> getFormsByModule(String str, String str2) throws FormException {
        new ArrayList(0);
        IBundleManager bundleManager = getBundleManager();
        if (bundleManager != null) {
            try {
                return bundleManager.getFormsByModule(str, str2);
            } catch (RemoteException e) {
                Log.error(LABEL, "getFormsByModule exception ", new Object[0]);
                FormException.FormError formError = FormException.FormError.SEND_BMS_MSG_ERROR;
                throw new FormException(formError, "get forms occurs exception:" + e.getMessage());
            }
        } else {
            Log.error(LABEL, "getBundleManager failed when getFormsByModule", new Object[0]);
            throw new FormException(FormException.FormError.BMS_BINDER_ERROR);
        }
    }

    public boolean requestForm(int i) throws FormException {
        return requestForm(i, new Intent());
    }

    public boolean requestForm(int i, Intent intent) throws FormException {
        if (i <= 0 || intent == null) {
            Log.error(LABEL, "requestForm, input param intent null", new Object[0]);
            throw new FormException(FormException.FormError.INPUT_PARAM_INVALID, "request form or intent is invalid");
        } else if (this.client != null) {
            try {
                FormManager instance = FormManager.getInstance();
                if (instance != null) {
                    return instance.requestForm(i, this.client, intent);
                }
                Log.error(LABEL, "requestForm, failed to get form manager", new Object[0]);
                return false;
            } catch (RemoteException e) {
                Log.error(LABEL, "requestForm, occurs remote exception", new Object[0]);
                FormException.FormError formError = FormException.FormError.SEND_FMS_MSG_ERROR;
                throw new FormException(formError, "form request occurs exception:" + e.getMessage());
            }
        } else {
            Log.error(LABEL, "requestForm, client records has no client", new Object[0]);
            throw new FormException(FormException.FormError.INPUT_PARAM_INVALID, "client records has no client");
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
            if (getAbility().supportHighPerformanceUI() || this.uiContent.isLatestUIAttached() || this.uiContent.isUiAttachedDisable()) {
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
            if (this.abilitySliceManager.getAbilityState() == Ability.STATE_ACTIVE) {
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
        if (this.currentState == AbilitySliceLifecycleExecutor.LifecycleState.INACTIVE) {
            Log.warn("topslice has been changed to inactive", new Object[0]);
        } else if (this.currentState == AbilitySliceLifecycleExecutor.LifecycleState.BACKGROUND) {
            setUiAttachedAllowed(true);
            Bytrace.startTrace(2147483648L, "sliceOnForeground");
            onForeground(intent);
            Bytrace.finishTrace(2147483648L, "sliceOnForeground");
            dispatchLifecycle(Lifecycle.Event.ON_FOREGROUND, intent);
            this.currentState = AbilitySliceLifecycleExecutor.LifecycleState.INACTIVE;
            this.uiContent.ensureLatestUIAttached();
        } else {
            throw new LifecycleException("Action(\" foreground \") is illegal for current state [" + this.currentState + "]");
        }
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
        printWriter.println(str + "Ability slice state: " + this.currentState);
        printWriter.print(str);
        if (this.resultData != null) {
            printWriter.println("Ability slice result: " + this.resultData.toUri());
        }
        printWriter.println(str + "Ability slice uiContent:");
        SliceUIContent sliceUIContent = this.uiContent;
        if (sliceUIContent == null) {
            printWriter.print(Ability.PREFIX + str);
            printWriter.println("null");
        } else {
            sliceUIContent.dump(Ability.PREFIX + str, printWriter, str2);
        }
        printWriter.println(str + "Ability slice connected service list:");
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
    public void componentEnterAnimator() {
        AbilitySliceAnimator abilitySliceAnimator = this.abilitySliceManager.getAbilitySliceAnimator();
        if (abilitySliceAnimator == null) {
            Log.info(LABEL, "ability slice animator is set to null", new Object[0]);
        } else {
            this.uiContent.componentEnterAnimator(abilitySliceAnimator);
        }
    }

    /* access modifiers changed from: package-private */
    public void componentExitAnimator() {
        AbilitySliceAnimator abilitySliceAnimator = this.abilitySliceManager.getAbilitySliceAnimator();
        if (abilitySliceAnimator == null) {
            Log.info(LABEL, "ability slice animator is set to null", new Object[0]);
        } else {
            this.uiContent.componentExitAnimator(abilitySliceAnimator);
        }
    }

    /* access modifiers changed from: package-private */
    public void stopComponentAnimator() {
        this.uiContent.stopComponentAnimator();
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
