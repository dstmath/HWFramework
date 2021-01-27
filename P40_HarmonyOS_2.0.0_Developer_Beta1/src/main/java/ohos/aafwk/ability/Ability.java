package ohos.aafwk.ability;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.RemoteException;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import ohos.aafwk.ability.AbilityLifecycleExecutor;
import ohos.aafwk.ability.IAbilityFormProvider;
import ohos.aafwk.ability.Lifecycle;
import ohos.aafwk.ability.delegation.AbilityDelegation;
import ohos.aafwk.ability.startsetting.AbilityStartSetting;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.aafwk.utils.dfx.hiview.AbilityHiviewWrapper;
import ohos.aafwk.utils.dfx.hiview.EventInfo;
import ohos.aafwk.utils.loader.ClassLoaderFactory;
import ohos.aafwk.utils.log.KeyLog;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.accessibility.AccessibilityEventInfo;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.ComponentProvider;
import ohos.agp.window.service.Window;
import ohos.agp.window.service.WindowManager;
import ohos.annotation.SystemApi;
import ohos.app.AbilityContext;
import ohos.app.Context;
import ohos.app.dispatcher.TaskDispatcher;
import ohos.app.dispatcher.task.TaskPriority;
import ohos.bundle.AbilityInfo;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.data.dataability.DataAbilityPredicates;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;
import ohos.devtools.JLog;
import ohos.devtools.JLogConstants;
import ohos.event.notification.NotificationRequest;
import ohos.global.configuration.Configuration;
import ohos.global.resource.RawFileDescriptor;
import ohos.multimodalinput.event.KeyEvent;
import ohos.multimodalinput.event.MouseEvent;
import ohos.multimodalinput.event.RotationEvent;
import ohos.multimodalinput.event.TouchEvent;
import ohos.rpc.IRemoteObject;
import ohos.rpc.ReliableFileDescriptor;
import ohos.system.OsHelper;
import ohos.system.OsHelperErrnoException;
import ohos.tools.Bytrace;
import ohos.utils.PacMap;
import ohos.utils.net.Uri;

public class Ability extends AbilityContext implements ILifecycle, IAbilityScheduler {
    private static final String DEFAULT_ARG = "-ability";
    private static final String DELEGATION = "ohos.aafwk.ability.delegation.AbilityDelegationImpl";
    private static final String DUMP_ABILITY = "-a";
    private static final String DUMP_HELP = "-h";
    private static final String DUMP_SLICE = "-s";
    static final int ILLEGAL_REQUEST_CODE = -1;
    private static final LogLabel LABEL = LogLabel.create();
    private static final String LIFECYCLE_CHANGE_ERROR_LOG = "Lifecycle state change error, currentState:";
    private static final String NEEDINIT_ABILITY_LOG = "Should init Ability first";
    private static final String NEEDINIT_SLICEMANAGER_LOG = "Should init AbilitySliceManager first";
    private static final String PIPE_WRITE_TASK = "com.huawei.ability.pipeWriteTask";
    public static final String PREFIX = "    ";
    static final int REQUEST_CODE_MARK = 65535;
    static final int STATE_ACTIVE = AbilityLifecycleExecutor.LifecycleState.ACTIVE.getValue();
    static final int STATE_BACKGROUND = AbilityLifecycleExecutor.LifecycleState.BACKGROUND.getValue();
    static final int STATE_INACTIVE = AbilityLifecycleExecutor.LifecycleState.INACTIVE.getValue();
    static final int STATE_INITIAL = AbilityLifecycleExecutor.LifecycleState.INITIAL.getValue();
    static final int STATE_UNINITIALIZED = AbilityLifecycleExecutor.LifecycleState.UNINITIALIZED.getValue();
    private static final String UNINIT_ABILITY_ERROR_LOG = "Ability is uninitialized, state: %{public}d";
    private static final String XML_PATH = "-x";
    private AbilityDelegation abilityDelegation;
    private AbilityForm abilityForm;
    private IRemoteObject abilityFormProvider;
    private HandlerThread abilityHandleThread;
    private AbilityInfo abilityInfo;
    private AbilitySliceManager abilitySliceManager;
    private AbilityWindow abilityWindow;
    private boolean basicInitializationDone;
    private IAbilityLifecycleCallback callback = null;
    private ConnectionScheduler connectionScheduler;
    private ContinuationManager continuationManager;
    private Intent intent = null;
    private boolean isTerminating;
    private Lifecycle lifecycle = new Lifecycle();
    private int lifecycleState = STATE_UNINITIALIZED;
    private ComponentContainer rootComponent;
    private volatile boolean sliceManagerInitializationDone = false;
    private Handler taskHandler;
    private UIContent uiContent;

    public PacMap call(String str, String str2, PacMap pacMap) {
        return null;
    }

    public int delete(Uri uri, DataAbilityPredicates dataAbilityPredicates) {
        return 0;
    }

    public Uri denormalizeUri(Uri uri) {
        return uri;
    }

    public void dispatchAccessibilityEventInfo(AccessibilityEventInfo accessibilityEventInfo) {
    }

    public String[] getFileTypes(Uri uri, String str) {
        return null;
    }

    public String getType(Uri uri) {
        return null;
    }

    public int insert(Uri uri, ValuesBucket valuesBucket) {
        return 0;
    }

    public boolean isTemporary() {
        return false;
    }

    public Uri normalizeUri(Uri uri) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void onAbilityResult(int i, int i2, Intent intent2) {
    }

    /* access modifiers changed from: protected */
    @Deprecated
    public void onCommand(Intent intent2, boolean z) {
    }

    @Deprecated
    public void onConfigurationChanged(Configuration configuration) {
    }

    public void onConfigurationUpdated(Configuration configuration) {
    }

    /* access modifiers changed from: protected */
    public IRemoteObject onConnect(Intent intent2) {
        return null;
    }

    /* access modifiers changed from: protected */
    public AbilityForm onCreateForm() {
        return null;
    }

    /* access modifiers changed from: protected */
    public ProviderFormInfo onCreateForm(Intent intent2) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void onDeleteForm(int i) {
    }

    /* access modifiers changed from: protected */
    public void onDisconnect(Intent intent2) {
    }

    public void onEventDispatch() {
    }

    public void onLeaveForeground() {
    }

    public void onMemoryLevel(int i) {
    }

    public void onMissionDeleted(Intent intent2) {
    }

    public CharSequence onNewDescription() {
        return null;
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent2) {
    }

    /* access modifiers changed from: protected */
    public void onOrientationChanged(AbilityInfo.DisplayOrientation displayOrientation) {
    }

    /* access modifiers changed from: protected */
    public void onPostActive() {
    }

    /* access modifiers changed from: protected */
    public void onPostStart(PacMap pacMap) {
    }

    public void onReconnect(Intent intent2) {
    }

    public void onRequestPermissionsFromUserResult(int i, String[] strArr, int[] iArr) {
    }

    public void onRestoreAbilityState(PacMap pacMap) {
    }

    public Uri onSetCaller() {
        return null;
    }

    public Object onStoreDataWhenConfigChange() {
        return null;
    }

    public void onTopActiveAbilityChanged(boolean z) {
    }

    /* access modifiers changed from: protected */
    public boolean onTouchEvent(TouchEvent touchEvent) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void onTriggerFormEvent(int i, String str) {
    }

    /* access modifiers changed from: protected */
    public void onUpdateForm(int i) {
    }

    public void onWindowFocusChanged(boolean z) {
    }

    public ResultSet query(Uri uri, String[] strArr, DataAbilityPredicates dataAbilityPredicates) {
        return null;
    }

    public boolean reload(Uri uri, PacMap pacMap) {
        return false;
    }

    public final void scheduleCancelContinuation() {
    }

    public boolean supportHighPerformanceUI() {
        return false;
    }

    public int update(Uri uri, ValuesBucket valuesBucket, DataAbilityPredicates dataAbilityPredicates) {
        return 0;
    }

    public final void init(Context context, AbilityInfo abilityInfo2) {
        Bytrace.startTrace(2147483648L, "ability init");
        if (abilityInfo2 == null || context == null) {
            throw new IllegalArgumentException("Ability init failed, input argument is null, should never happened");
        }
        attachBaseContext(context);
        initBasicEnv(abilityInfo2);
        this.connectionScheduler = new ConnectionScheduler(context);
        if (abilityInfo2.getType() == AbilityInfo.AbilityType.PAGE) {
            if (supportHighPerformanceUI()) {
                initTaskThread();
            }
            if (!this.sliceManagerInitializationDone) {
                initSliceManagerForPage();
            }
            Bytrace.startTrace(2147483648L, "init window");
            if (this.abilityWindow == null) {
                this.abilityWindow = new AbilityWindow();
            }
            this.abilityWindow.initialize(this);
            Bytrace.finishTrace(2147483648L, "init window");
            this.uiContent = new UIContent(this.abilityWindow);
            if (this instanceof IAbilityContinuation) {
                this.continuationManager = new ContinuationManager(this);
            }
            accelerateTaskThread();
        }
        this.lifecycleState = STATE_INITIAL;
        this.isTerminating = false;
        Bytrace.finishTrace(2147483648L, "ability init");
    }

    private void initSliceManagerForPage() {
        if (this.abilitySliceManager == null) {
            this.abilitySliceManager = new AbilitySliceManager();
        } else {
            Log.warn(LABEL, "AbilitySliceManager already exist", new Object[0]);
        }
        this.abilitySliceManager.init(this);
        this.sliceManagerInitializationDone = true;
    }

    public final void initAsForm(Context context, AbilityInfo abilityInfo2) {
        if (abilityInfo2 == null || context == null) {
            throw new IllegalArgumentException("Ability init failed, input argument is null, should never happened");
        }
        attachBaseContext(context);
        initBasicEnv(abilityInfo2);
        this.abilityFormProvider = new AbilityFormProvider(this, null);
    }

    private void initTaskThread() {
        if (this.abilityHandleThread == null) {
            this.abilityHandleThread = new HandlerThread("PrepareUI-Thread", 10);
            this.abilityHandleThread.start();
        }
    }

    private void accelerateTaskThread() {
        HandlerThread handlerThread = this.abilityHandleThread;
        if (handlerThread != null) {
            int threadId = handlerThread.getThreadId();
            try {
                IActivityManager service = ActivityManager.getService();
                service.setHmThreadToRtg("mode:set;tids:" + threadId);
            } catch (RemoteException unused) {
                Log.error(LABEL, "setHmThreadToRtg %{public}d failed", Integer.valueOf(threadId));
            }
        }
    }

    public void postTask(Runnable runnable, long j) {
        if (runnable != null) {
            HandlerThread handlerThread = this.abilityHandleThread;
            if (handlerThread == null) {
                Log.error(LABEL, "postTask : abilityHandleThread is null.", new Object[0]);
                return;
            }
            if (this.taskHandler == null) {
                this.taskHandler = new Handler(handlerThread.getLooper());
            }
            this.taskHandler.postDelayed(runnable, j);
        }
    }

    @SystemApi
    public HarmonyosApplication getHarmonyosApplication() {
        Object harmonyosApp = getHarmonyosApp();
        if (harmonyosApp instanceof HarmonyosApplication) {
            return (HarmonyosApplication) harmonyosApp;
        }
        return null;
    }

    public AbilityPackage getAbilityPackage() {
        Object harmonyAbilityPkg = getHarmonyAbilityPkg(this.abilityInfo);
        if (harmonyAbilityPkg instanceof AbilityPackage) {
            return (AbilityPackage) harmonyAbilityPkg;
        }
        return null;
    }

    public Window getWindow() {
        if (isWindowProxyInitialized()) {
            return this.abilityWindow.getWindow();
        }
        Log.error(LABEL, "unable to get Window", new Object[0]);
        return null;
    }

    /* access modifiers changed from: package-private */
    public AbilityWindow getWindowProxy() {
        return this.abilityWindow;
    }

    public final void setUIContent(int i) {
        if (i >= 0) {
            UIContent uIContent = this.uiContent;
            if (uIContent != null) {
                uIContent.updateUIContent(i);
            } else {
                Log.error(LABEL, "ui Content must be inited", new Object[0]);
                throw new AbilitySliceRuntimeException("UI Content must be inited");
            }
        } else {
            Log.error(LABEL, "ui layout resource must be valid [%{public}d]", Integer.valueOf(i));
            throw new AbilitySliceRuntimeException("UI layout resource must be valid");
        }
    }

    public void setUIContent(ComponentContainer componentContainer) {
        if (componentContainer != null) {
            UIContent uIContent = this.uiContent;
            if (uIContent != null) {
                this.rootComponent = componentContainer;
                uIContent.updateUIContent(componentContainer);
                return;
            }
            Log.error(LABEL, "ui Content must be inited", new Object[0]);
            throw new AbilitySliceRuntimeException("UI Content must be inited");
        }
        Log.error(LABEL, "componentContainer must be valid", new Object[0]);
        throw new AbilitySliceRuntimeException("componentContainer must be valid");
    }

    public void setAVController(Object obj) {
        if (!isWindowProxyInitialized()) {
            Log.error(LABEL, "unable to set AVController", new Object[0]);
        } else {
            this.abilityWindow.setAVController(obj);
        }
    }

    public Object getAVController() {
        if (isWindowProxyInitialized()) {
            return this.abilityWindow.getAVController();
        }
        Log.error(LABEL, "unable to get AVController", new Object[0]);
        return null;
    }

    public WindowManager.LayoutConfig getLayoutParams() {
        if (isWindowProxyInitialized()) {
            return this.abilityWindow.getLayoutParams();
        }
        Log.error(LABEL, "unable to get LayoutParams", new Object[0]);
        return null;
    }

    public void setLayoutParams(WindowManager.LayoutConfig layoutConfig) {
        if (!isWindowProxyInitialized()) {
            Log.error(LABEL, "unable to set LayoutParams", new Object[0]);
        } else {
            this.abilityWindow.setLayoutParams(layoutConfig);
        }
    }

    public static class LayoutParamsHelper {
        static final String PAGE_LAYOUT_ALIGNMENT = "PageLayoutAlignment";
        static final String PAGE_LAYOUT_ALPHA = "PageLayoutAlpha";
        static final String PAGE_LAYOUT_DIMAMOUNT = "PageLayoutDimAmount";
        static final String PAGE_LAYOUT_FLAG = "PageLayoutFlag";
        static final String PAGE_LAYOUT_HEIGHT = "PageLayoutHeight";
        static final String PAGE_LAYOUT_WIDTH = "PageLayoutWidth";
        static final String PAGE_LAYOUT_X = "PageLayoutX";
        static final String PAGE_LAYOUT_Y = "PageLayoutY";
        private Intent intentLayout;

        public LayoutParamsHelper(Intent intent) {
            this.intentLayout = intent;
        }

        public LayoutParamsHelper setX(int i) {
            Intent intent = this.intentLayout;
            if (intent != null) {
                intent.setParam(PAGE_LAYOUT_X, i);
            }
            return this;
        }

        public LayoutParamsHelper setY(int i) {
            Intent intent = this.intentLayout;
            if (intent != null) {
                intent.setParam(PAGE_LAYOUT_Y, i);
            }
            return this;
        }

        public LayoutParamsHelper setWidth(int i) {
            Intent intent = this.intentLayout;
            if (intent != null) {
                intent.setParam(PAGE_LAYOUT_WIDTH, i);
            }
            return this;
        }

        public LayoutParamsHelper setHeight(int i) {
            Intent intent = this.intentLayout;
            if (intent != null) {
                intent.setParam(PAGE_LAYOUT_HEIGHT, i);
            }
            return this;
        }

        public LayoutParamsHelper setAlpha(float f) {
            Intent intent = this.intentLayout;
            if (intent != null) {
                intent.setParam(PAGE_LAYOUT_ALPHA, f);
            }
            return this;
        }

        public LayoutParamsHelper setDimAmount(float f) {
            Intent intent = this.intentLayout;
            if (intent != null) {
                intent.setParam(PAGE_LAYOUT_DIMAMOUNT, f);
            }
            return this;
        }

        public LayoutParamsHelper setFlags(int i, int i2) {
            Intent intent = this.intentLayout;
            if (intent != null) {
                int i3 = i & i2;
                this.intentLayout.setParam(PAGE_LAYOUT_FLAG, i3 | (intent.getIntParam(PAGE_LAYOUT_FLAG, 0) & (~i2)));
            }
            return this;
        }

        public LayoutParamsHelper setAlignment(int i) {
            Intent intent = this.intentLayout;
            if (intent != null) {
                intent.setParam(PAGE_LAYOUT_ALIGNMENT, i);
            }
            return this;
        }
    }

    public static class WindowAttributeHelper {
        static final String WINDOW_PADDING_BOTTOM = "WindowPaddingBottom";
        static final String WINDOW_PADDING_LEFT = "WindowPaddingLeft";
        static final String WINDOW_PADDING_RIGHT = "WindowPaddingRight";
        static final String WINDOW_PADDING_TOP = "WindowPaddingTop";
        private Intent intentAttribute;

        public WindowAttributeHelper(Intent intent) {
            this.intentAttribute = intent;
        }

        public WindowAttributeHelper setLeftPadding(int i) {
            Intent intent = this.intentAttribute;
            if (intent != null) {
                intent.setParam(WINDOW_PADDING_LEFT, i);
            }
            return this;
        }

        public WindowAttributeHelper setTopPadding(int i) {
            Intent intent = this.intentAttribute;
            if (intent != null) {
                intent.setParam(WINDOW_PADDING_TOP, i);
            }
            return this;
        }

        public WindowAttributeHelper setRightPadding(int i) {
            Intent intent = this.intentAttribute;
            if (intent != null) {
                intent.setParam(WINDOW_PADDING_RIGHT, i);
            }
            return this;
        }

        public WindowAttributeHelper setBottomPadding(int i) {
            Intent intent = this.intentAttribute;
            if (intent != null) {
                intent.setParam(WINDOW_PADDING_BOTTOM, i);
            }
            return this;
        }
    }

    public Component findComponentById(int i) {
        return this.uiContent.findComponentById(i);
    }

    public void setIsAmbientMode(boolean z) {
        if (!isWindowProxyInitialized()) {
            Log.error(LABEL, "unable to set AmbientMode", new Object[0]);
        } else {
            this.abilityWindow.setIsAmbientMode(z);
        }
    }

    public boolean dispatchTouchEvent(TouchEvent touchEvent) {
        if (touchEvent == null) {
            Log.error(LABEL, "null touch event, ignore", new Object[0]);
            return false;
        } else if (!isEventHandlingCapable()) {
            Log.error(LABEL, "is not able to handle this touch event, will be ignored", new Object[0]);
            return false;
        } else if (this.abilityWindow.dispatchTouchEvent(touchEvent)) {
            return true;
        } else {
            return onTouchEvent(touchEvent);
        }
    }

    public boolean dispatchMouseEvent(MouseEvent mouseEvent) {
        if (mouseEvent == null) {
            Log.error(LABEL, "null touch event, ignore", new Object[0]);
            return false;
        } else if (isEventHandlingCapable()) {
            return this.abilityWindow.dispatchMouseEvent(mouseEvent);
        } else {
            Log.error(LABEL, "is not able to handle this touch event, will be ignored", new Object[0]);
            return false;
        }
    }

    public final void dispatchOrientationChange(AbilityInfo.DisplayOrientation displayOrientation) {
        if (Log.isDebuggable()) {
            KeyLog.debugBound("[%{public}s][%{public}s][%{public}s]: new orientation: %{public}s", LABEL.getTag(), KeyLog.DISPATCH_ORIENTATION_CHANGE, KeyLog.LogState.START, displayOrientation);
        }
        if (displayOrientation == null) {
            Log.error(LABEL, "null display orientation event, ignore", new Object[0]);
        } else if (displayOrientation != AbilityInfo.DisplayOrientation.LANDSCAPE && displayOrientation != AbilityInfo.DisplayOrientation.PORTRAIT && displayOrientation != AbilityInfo.DisplayOrientation.FOLLOWRECENT) {
            Log.warn(LABEL, "display orientation should always be LANDSCAPE or PORTRAIT or FOLLOWRECENT, ignore", new Object[0]);
        } else if (!isEventHandlingCapable()) {
            Log.error(LABEL, "is not able to handle this orientation event, will be ignored", new Object[0]);
        } else {
            this.abilitySliceManager.dispatchOrientationChange(displayOrientation);
            onOrientationChanged(displayOrientation);
        }
    }

    public boolean dispatchKeyBoardEvent(KeyEvent keyEvent) {
        if (keyEvent == null) {
            Log.error(LABEL, "null key event, ignore", new Object[0]);
            return false;
        } else if (isEventHandlingCapable()) {
            return this.abilityWindow.dispatchKeyEvent(keyEvent);
        } else {
            Log.error(LABEL, "is not able to handle this key event, will be ignored", new Object[0]);
            return false;
        }
    }

    public boolean dispatchRotationEvent(RotationEvent rotationEvent) {
        if (rotationEvent == null) {
            Log.error(LABEL, "null rotation event, ignore", new Object[0]);
            return false;
        } else if (isEventHandlingCapable()) {
            return this.abilityWindow.dispatchRotationEvent(rotationEvent);
        } else {
            Log.error(LABEL, "is not able to handle this rotation event, will be ignored", new Object[0]);
            return false;
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        AbilitySlice topAbilitySlice = this.abilitySliceManager.getTopAbilitySlice();
        if (topAbilitySlice == null) {
            return false;
        }
        topAbilitySlice.onKeyDown(i, keyEvent);
        return false;
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        AbilitySlice topAbilitySlice = this.abilitySliceManager.getTopAbilitySlice();
        if (topAbilitySlice == null) {
            return false;
        }
        topAbilitySlice.onKeyUp(i, keyEvent);
        return false;
    }

    /* access modifiers changed from: protected */
    public void onBackPressed() {
        terminateAbility();
    }

    public final void notifyBackKeyPressed() {
        if (!isEventHandlingCapable()) {
            Log.error(LABEL, "is not able to handle this back key notification, will be ignored", new Object[0]);
            return;
        }
        if (Log.isDebuggable()) {
            KeyLog.debugBound(LABEL, KeyLog.DISPATCH_BACKKEY_PRESSED, KeyLog.LogState.START);
        }
        if (!this.abilitySliceManager.notifyBackKeyPressed()) {
            onBackPressed();
        }
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) throws IllegalStateException, IllegalArgumentException {
        AbilitySliceManager abilitySliceManager2;
        if (this.abilityInfo == null) {
            throw new IllegalStateException(NEEDINIT_ABILITY_LOG);
        } else if (strArr == null) {
            Log.error(LABEL, "args is null, dump failed", new Object[0]);
            throw new IllegalArgumentException("args is null, dump failed");
        } else if (str == null) {
            Log.error(LABEL, "prefix is null, dump failed", new Object[0]);
            throw new IllegalArgumentException("prefix is null, dump failed");
        } else if (printWriter == null) {
            Log.error(LABEL, "writer is null, dump failed", new Object[0]);
            throw new IllegalArgumentException("fd or writer is null, dump failed");
        } else if (strArr[0].equals(DEFAULT_ARG)) {
            boolean z = false;
            boolean z2 = false;
            boolean z3 = false;
            String str2 = "";
            for (String str3 : strArr) {
                if (!str3.isEmpty() && !str3.equals(DEFAULT_ARG)) {
                    if (str3.equals(DUMP_ABILITY)) {
                        z = true;
                    } else if (str3.equals(DUMP_SLICE) && this.abilityInfo.getType() == AbilityInfo.AbilityType.PAGE) {
                        z2 = true;
                    } else if (str3.equals(XML_PATH) && this.abilityInfo.getType() == AbilityInfo.AbilityType.PAGE) {
                        z2 = true;
                        z3 = true;
                    } else if (str3.equals(DUMP_HELP)) {
                        dumpHelp(str, printWriter);
                        return;
                    } else if (Log.isKnownDumpCmdOpt(str3)) {
                        Log.dump(str, printWriter, strArr);
                        return;
                    } else if (z3) {
                        str2 = str3;
                        z3 = false;
                    } else {
                        Log.error(LABEL, "arg '%{public}s' is not support, dump failed", str3);
                        printWriter.println(str + "arg: [" + str3 + "] is not support, use " + DUMP_HELP + " for help");
                        return;
                    }
                }
            }
            if (!z && !z2) {
                z = true;
                z2 = true;
            }
            printWriter.println(str + "[Ability bundle: " + this.abilityInfo.getBundleName() + "]");
            printWriter.println(str + "[Ability name: " + this.abilityInfo.getClassName() + "]");
            if (z) {
                dumpAbility(PREFIX + str, printWriter);
            }
            if (z2 && (abilitySliceManager2 = this.abilitySliceManager) != null) {
                abilitySliceManager2.dumpSlice(str, printWriter, new String[]{XML_PATH, str2});
            }
        } else {
            Log.error(LABEL, "args not contain %{public}s, dump failed", DEFAULT_ARG);
            throw new IllegalArgumentException("args not contain -ability, dump failed");
        }
    }

    public final void unloadForm() {
        AbilityForm abilityForm2 = this.abilityForm;
        if (abilityForm2 != null) {
            abilityForm2.release();
            this.abilityForm = null;
        }
        this.abilityFormProvider = null;
    }

    public final IRemoteObject getAbilityFormProvider() {
        return this.abilityFormProvider;
    }

    @Override // ohos.aafwk.ability.IAbilityScheduler
    public final void scheduleCommand(Intent intent2, boolean z, int i) {
        Bytrace.startTrace(2147483648L, "onCommand");
        onCommand(intent2, z, i);
        Bytrace.finishTrace(2147483648L, "onCommand");
    }

    @Override // ohos.aafwk.ability.IAbilityScheduler
    public final void scheduleNewIntent(Intent intent2) {
        this.abilitySliceManager.onAbilityNewIntent(intent2);
        Bytrace.startTrace(2147483648L, "onNewIntent");
        onNewIntent(intent2);
        Bytrace.finishTrace(2147483648L, "onNewIntent");
    }

    public final void schedulePostStart(PacMap pacMap) {
        onPostStart(pacMap);
    }

    public final void schedulePostActive() {
        onPostActive();
    }

    @Override // ohos.aafwk.ability.IAbilityScheduler
    public final void scheduleAbilityLifecycle(Intent intent2, int i) {
        if (i < STATE_INITIAL || i > STATE_BACKGROUND) {
            throw new IllegalArgumentException("Schedule ability state, input argument targetState is invalid");
        } else if (getCurrentState() != i) {
            Bytrace.startTrace(2147483648L, "abilityHandleTransaction, CURRENT_STATE: " + getCurrentState() + " -> TARGET_STATE: " + i);
            try {
                handleLifecycleTransaction(intent2, i);
                Bytrace.finishTrace(2147483648L, "abilityHandleTransaction, CURRENT_STATE: " + getCurrentState() + " -> TARGET_STATE: " + i);
            } catch (IllegalArgumentException | IllegalStateException | LifecycleException e) {
                Log.error(LABEL, "Schedule Ability state error[%{public}s], current state: %{public}d, targetState: %{public}d", e, Integer.valueOf(getCurrentState()), Integer.valueOf(i));
                throw e;
            } catch (Throwable th) {
                Bytrace.finishTrace(2147483648L, "abilityHandleTransaction, CURRENT_STATE: " + getCurrentState() + " -> TARGET_STATE: " + i);
                throw th;
            }
        } else {
            Log.warn(LABEL, "ability is already in state: %{public}d", Integer.valueOf(i));
        }
    }

    @Override // ohos.aafwk.ability.IAbilityScheduler
    public final void scheduleAbilityResult(int i, int i2, Intent intent2) {
        Log.debug(LABEL, "send result to ability developer, request code :%{public}d, result code :%{private}d", Integer.valueOf(i), Integer.valueOf(i2));
        if (this.abilitySliceManager != null) {
            Bytrace.startTrace(2147483648L, "onAbilityResult");
            onAbilityResult(65535 & i, i2, intent2);
            Bytrace.finishTrace(2147483648L, "onAbilityResult");
            this.abilitySliceManager.onAbilityResult(i, i2, intent2);
            return;
        }
        throw new IllegalStateException("Schedule ability result failed，should init first");
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public boolean connectAbility(Intent intent2, IAbilityConnection iAbilityConnection) {
        ConnectionScheduler connectionScheduler2 = this.connectionScheduler;
        if (connectionScheduler2 == null) {
            return false;
        }
        return connectionScheduler2.openServiceConnection(this, intent2, iAbilityConnection);
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public void disconnectAbility(IAbilityConnection iAbilityConnection) {
        Log.info(LABEL, "disconnect ability from ability", new Object[0]);
        ConnectionScheduler connectionScheduler2 = this.connectionScheduler;
        if (connectionScheduler2 != null) {
            connectionScheduler2.closeServiceConnection(this, iAbilityConnection);
        }
    }

    @Override // ohos.aafwk.ability.IAbilityScheduler
    public final IRemoteObject scheduleConnectAbility(Intent intent2) {
        if (intent2 != null) {
            KeyLog.info("[%{public}s][%{public}s][%{public}s]: ability: %{public}s, element: %{public}s", LABEL.getTag(), KeyLog.SCHEDULE_CONNECT, KeyLog.LogState.START, this, Optional.of(intent2).map($$Lambda$5NIH3kVWNfBSHcIt4qMC1aQ8cu0.INSTANCE).map($$Lambda$Gbq1Su8pWvMr5cS7vkDL3qT3QMg.INSTANCE).orElse("null"));
            int currentState = getCurrentState();
            if (currentState > STATE_INITIAL) {
                return connectAbilityService(intent2);
            }
            Log.warn(LABEL, "ability state is %{public}d, not allow connect", Integer.valueOf(currentState));
            return null;
        }
        throw new IllegalArgumentException("connect ability failed, intent is null");
    }

    @Override // ohos.aafwk.ability.IAbilityScheduler
    public final void scheduleDisconnectAbility(Intent intent2) {
        if (intent2 != null) {
            KeyLog.info("[%{public}s][%{public}s][%{public}s]: ability: %{public}s, element: %{public}s", LABEL.getTag(), KeyLog.SCHEDULE_DISCONNECT, KeyLog.LogState.START, this, Optional.ofNullable(intent2).map($$Lambda$5NIH3kVWNfBSHcIt4qMC1aQ8cu0.INSTANCE).map($$Lambda$Gbq1Su8pWvMr5cS7vkDL3qT3QMg.INSTANCE).orElse("null"));
            disconnectAbilityService(intent2);
            return;
        }
        throw new IllegalArgumentException("disconnect ability failed, intent is null");
    }

    @Override // ohos.aafwk.ability.ILifecycle
    public final Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    /* access modifiers changed from: package-private */
    public final void dispatchLifecycle(Lifecycle.Event event, Intent intent2) throws IllegalStateException {
        Lifecycle lifecycle2 = this.lifecycle;
        if (lifecycle2 != null) {
            lifecycle2.dispatchLifecycle(event, intent2);
            return;
        }
        throw new IllegalStateException("lifecycle is null, dispatch lifecycle failed for ability");
    }

    public final int batchInsert(Uri uri, ValuesBucket[] valuesBucketArr) {
        if (uri == null || valuesBucketArr == null) {
            return 0;
        }
        int i = 0;
        for (ValuesBucket valuesBucket : valuesBucketArr) {
            if (insert(uri, valuesBucket) >= 0) {
                i++;
            }
        }
        return i;
    }

    public DataAbilityResult[] executeBatch(ArrayList<DataAbilityOperation> arrayList) throws OperationExecuteException {
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 == null) {
            throw new IllegalStateException(NEEDINIT_ABILITY_LOG);
        } else if (abilityInfo2.getType() == AbilityInfo.AbilityType.DATA) {
            Log.debug(LABEL, "data ability executeBatch. name:%{public}s.", this.abilityInfo.getClassName());
            return executeBatchInner(arrayList);
        } else {
            throw new IllegalArgumentException("data ability update failed, current type is: " + this.abilityInfo.getType());
        }
    }

    public FileDescriptor openFile(Uri uri, String str) throws FileNotFoundException {
        throw new FileNotFoundException("Not found support files at " + uri);
    }

    public RawFileDescriptor openRawFile(Uri uri, String str) throws FileNotFoundException {
        throw new FileNotFoundException("Not found support raw file at " + uri);
    }

    public <T> FileDescriptor createPipeFile(Uri uri, String str, PacMap pacMap, T t, PipeFileWriter<T> pipeFileWriter) throws FileNotFoundException, IllegalStateException {
        try {
            ReliableFileDescriptor[] createPipe2 = ReliableFileDescriptor.createPipe2();
            FileDescriptor fileDescriptor = createPipe2[1].getFileDescriptor();
            TaskDispatcher createParallelTaskDispatcher = createParallelTaskDispatcher(PIPE_WRITE_TASK, TaskPriority.DEFAULT);
            if (createParallelTaskDispatcher != null) {
                createParallelTaskDispatcher.asyncDispatch(new Runnable(fileDescriptor, uri, str, pacMap, t) {
                    /* class ohos.aafwk.ability.$$Lambda$Ability$rinxuj6lYDNCH00GAwQbqp344Xg */
                    private final /* synthetic */ FileDescriptor f$1;
                    private final /* synthetic */ Uri f$2;
                    private final /* synthetic */ String f$3;
                    private final /* synthetic */ PacMap f$4;
                    private final /* synthetic */ Object f$5;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                        this.f$5 = r6;
                    }

                    @Override // java.lang.Runnable
                    public final void run() {
                        Ability.lambda$createPipeFile$0(PipeFileWriter.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
                    }
                });
                return createPipe2[0].getFileDescriptor();
            }
            throw new IllegalStateException(NEEDINIT_ABILITY_LOG);
        } catch (OsHelperErrnoException unused) {
            throw new FileNotFoundException("Open pipe failed.");
        }
    }

    static /* synthetic */ void lambda$createPipeFile$0(PipeFileWriter pipeFileWriter, FileDescriptor fileDescriptor, Uri uri, String str, PacMap pacMap, Object obj) {
        pipeFileWriter.write(fileDescriptor, uri, str, pacMap, obj);
        try {
            OsHelper.closeFile(fileDescriptor);
        } catch (OsHelperErrnoException unused) {
            Log.error(LABEL, "Close FileDescriptor failed in pipe.", new Object[0]);
        }
    }

    @SystemApi
    public void makePersistentUriPermission(Uri uri, int i) throws DataAbilityRemoteException {
        throw new DataAbilityRemoteException("No Persistable Permission to take at " + uri);
    }

    /* access modifiers changed from: protected */
    public void onStart(Intent intent2) {
        this.intent = intent2;
        HarmonyosApplication harmonyosApplication = getHarmonyosApplication();
        if (harmonyosApplication != null) {
            harmonyosApplication.dispatchAbilityStarted(this);
        }
        AbilityPackage abilityPackage = getAbilityPackage();
        if (abilityPackage != null) {
            abilityPackage.dispatchAbilityStarted(this);
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        HarmonyosApplication harmonyosApplication = getHarmonyosApplication();
        if (harmonyosApplication != null) {
            harmonyosApplication.dispatchAbilityStoped(this);
        }
        AbilityPackage abilityPackage = getAbilityPackage();
        if (abilityPackage != null) {
            abilityPackage.dispatchAbilityStoped(this);
        }
    }

    /* access modifiers changed from: protected */
    public void onActive() {
        HarmonyosApplication harmonyosApplication = getHarmonyosApplication();
        if (harmonyosApplication != null) {
            harmonyosApplication.dispatchAbilityActived(this);
        }
        AbilityPackage abilityPackage = getAbilityPackage();
        if (abilityPackage != null) {
            abilityPackage.dispatchAbilityActived(this);
        }
    }

    /* access modifiers changed from: protected */
    public void onInactive() {
        HarmonyosApplication harmonyosApplication = getHarmonyosApplication();
        if (harmonyosApplication != null) {
            harmonyosApplication.dispatchAbilityInactived(this);
        }
        AbilityPackage abilityPackage = getAbilityPackage();
        if (abilityPackage != null) {
            abilityPackage.dispatchAbilityInactived(this);
        }
    }

    /* access modifiers changed from: protected */
    public void onForeground(Intent intent2) {
        HarmonyosApplication harmonyosApplication = getHarmonyosApplication();
        if (harmonyosApplication != null) {
            harmonyosApplication.dispatchAbilityForegrounded(this);
        }
        AbilityPackage abilityPackage = getAbilityPackage();
        if (abilityPackage != null) {
            abilityPackage.dispatchAbilityForegrounded(this);
        }
    }

    /* access modifiers changed from: protected */
    public void onBackground() {
        HarmonyosApplication harmonyosApplication = getHarmonyosApplication();
        if (harmonyosApplication != null) {
            harmonyosApplication.dispatchAbilityBackgrounded(this);
        }
        AbilityPackage abilityPackage = getAbilityPackage();
        if (abilityPackage != null) {
            abilityPackage.dispatchAbilityBackgrounded(this);
        }
    }

    /* access modifiers changed from: protected */
    public void onCommand(Intent intent2, boolean z, int i) {
        onCommand(intent2, z);
    }

    public boolean updateForm(int i, ComponentProvider componentProvider) {
        if (componentProvider == null) {
            Log.error(LABEL, "component is null", new Object[0]);
            return false;
        }
        try {
            if (FormManager.getInstance() == null) {
                Log.error(LABEL, "updateForm component, formManager is null", new Object[0]);
                return false;
            }
            FormManager.getInstance().updateForm(i, getBundleName(), componentProvider);
            return true;
        } catch (ohos.rpc.RemoteException unused) {
            Log.error(LABEL, "updateForm for component RemoteException ", new Object[0]);
            return false;
        } catch (IllegalArgumentException unused2) {
            Log.error(LABEL, "updateForm for component IllegalArgumentException ", new Object[0]);
            return false;
        }
    }

    public boolean updateForm(int i, FormBindingData formBindingData) {
        if (formBindingData == null) {
            Log.error(LABEL, "formBindingData is null", new Object[0]);
            return false;
        }
        try {
            if (FormManager.getInstance() == null) {
                Log.error(LABEL, "updateForm binding data, formManager is null", new Object[0]);
                return false;
            }
            FormManager.getInstance().updateForm(i, getBundleName(), formBindingData);
            return true;
        } catch (ohos.rpc.RemoteException unused) {
            Log.error(LABEL, "updateForm for binding data RemoteException ", new Object[0]);
            return false;
        } catch (IllegalArgumentException unused2) {
            Log.error(LABEL, "updateForm for binding data IllegalArgumentException ", new Object[0]);
            return false;
        }
    }

    public void onSaveAbilityState(PacMap pacMap) {
        HarmonyosApplication harmonyosApplication = getHarmonyosApplication();
        if (harmonyosApplication != null) {
            harmonyosApplication.dispatchAbilitySavedState(pacMap);
        }
        AbilityPackage abilityPackage = getAbilityPackage();
        if (abilityPackage != null) {
            abilityPackage.dispatchAbilitySavedState(pacMap);
        }
    }

    public final void startAbility(Intent intent2) throws IllegalArgumentException, IllegalStateException {
        startAbility(intent2, AbilityStartSetting.getEmptySetting());
    }

    public final void startAbility(Intent intent2, AbilityStartSetting abilityStartSetting) throws IllegalArgumentException, IllegalStateException {
        if (intent2 != null) {
            int i = this.lifecycleState;
            if (i >= STATE_INITIAL) {
                KeyLog.infoBound("[%{public}s][%{public}s][%{public}s]: element: %s", LABEL.getTag(), KeyLog.START_ABILITY, KeyLog.LogState.START, Optional.ofNullable(intent2).map($$Lambda$5NIH3kVWNfBSHcIt4qMC1aQ8cu0.INSTANCE).map($$Lambda$Gbq1Su8pWvMr5cS7vkDL3qT3QMg.INSTANCE).orElse("null"));
                startAbility(intent2, -1, abilityStartSetting);
                return;
            }
            Log.error(LABEL, UNINIT_ABILITY_ERROR_LOG, Integer.valueOf(i));
            throw new IllegalStateException("ability is uninitialized, start ability failed");
        }
        throw new IllegalArgumentException("intent is null, can't start ability");
    }

    public final void startAbilityForResult(Intent intent2, int i, AbilityStartSetting abilityStartSetting) throws IllegalArgumentException, IllegalStateException {
        if (intent2 == null) {
            throw new IllegalArgumentException("intent is null, can't start ability");
        } else if (i != -1) {
            int i2 = this.lifecycleState;
            if (i2 < STATE_INITIAL) {
                Log.error(LABEL, UNINIT_ABILITY_ERROR_LOG, Integer.valueOf(i2));
                throw new IllegalStateException("ability is uninitialized, start ability for result failed");
            } else if (this.abilityInfo.getType() == AbilityInfo.AbilityType.PAGE) {
                KeyLog.infoBound("[%{public}s][%{public}s][%{public}s]: element: %s", LABEL.getTag(), KeyLog.START_ABILITY_FORRESULT, KeyLog.LogState.START, Optional.ofNullable(intent2).map($$Lambda$5NIH3kVWNfBSHcIt4qMC1aQ8cu0.INSTANCE).map($$Lambda$Gbq1Su8pWvMr5cS7vkDL3qT3QMg.INSTANCE).orElse("null"));
                startAbility(intent2, i, abilityStartSetting);
                KeyLog.infoBound(KeyLog.KEYLOG_FMT_ARGS, LABEL.getTag(), KeyLog.START_ABILITY_FORRESULT, KeyLog.LogState.END);
            } else {
                throw new IllegalStateException("only page can start other ability for result");
            }
        } else {
            Log.error(LABEL, "request code is illegal: %{public}d", Integer.valueOf(i));
            throw new IllegalArgumentException("request code is illegal");
        }
    }

    public final void startAbilityForResult(Intent intent2, int i) throws IllegalArgumentException, IllegalStateException {
        startAbilityForResult(intent2, i, AbilityStartSetting.getEmptySetting());
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public final void setResult(int i, Intent intent2) {
        if (this.abilityInfo.getType() == AbilityInfo.AbilityType.PAGE) {
            int i2 = this.lifecycleState;
            if (i2 >= STATE_INITIAL) {
                super.setResult(i, intent2);
            } else {
                Log.error(LABEL, UNINIT_ABILITY_ERROR_LOG, Integer.valueOf(i2));
                throw new IllegalStateException("ability is uninitialized, set result failed");
            }
        } else {
            throw new IllegalStateException("only page can setResult");
        }
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public final void terminateAbility() {
        int i = this.lifecycleState;
        if (i >= STATE_INITIAL) {
            KeyLog.infoBound(LABEL, KeyLog.TERMINATE_ABILITY, KeyLog.LogState.START);
            this.isTerminating = true;
            super.terminateAbility();
            KeyLog.infoBound(KeyLog.KEYLOG_FMT_ARGS, LABEL.getTag(), KeyLog.TERMINATE_ABILITY, KeyLog.LogState.END);
            return;
        }
        Log.error(LABEL, UNINIT_ABILITY_ERROR_LOG, Integer.valueOf(i));
        throw new IllegalStateException("Ability is uninitialized state or instrument is null");
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    @SystemApi
    public final void terminateAndRemoveMission() {
        int i = this.lifecycleState;
        if (i >= STATE_INITIAL) {
            KeyLog.infoBound(LABEL, KeyLog.TERMINATE_ABILITY, KeyLog.LogState.START);
            this.isTerminating = true;
            super.terminateAndRemoveMission();
            KeyLog.infoBound(KeyLog.KEYLOG_FMT_ARGS, LABEL.getTag(), KeyLog.TERMINATE_ABILITY, KeyLog.LogState.END);
            return;
        }
        Log.error(LABEL, UNINIT_ABILITY_ERROR_LOG, Integer.valueOf(i));
        throw new IllegalStateException("Ability is uninitialized state or instrument is null");
    }

    @Override // ohos.app.AbilityContext, ohos.app.Context
    public final boolean stopAbility(Intent intent2) {
        int i = this.lifecycleState;
        if (i >= STATE_INITIAL) {
            KeyLog.infoBound("[%{public}s][%{public}s][%{public}s]: element: %s", LABEL.getTag(), KeyLog.STOP_ABILITY, KeyLog.LogState.START, Optional.ofNullable(intent2).map($$Lambda$5NIH3kVWNfBSHcIt4qMC1aQ8cu0.INSTANCE).map($$Lambda$Gbq1Su8pWvMr5cS7vkDL3qT3QMg.INSTANCE).orElse("null"));
            boolean stopAbility = super.stopAbility(intent2);
            KeyLog.infoBound("[%{public}s][%{public}s][%{public}s]: result: %{public}b", LABEL.getTag(), KeyLog.STOP_ABILITY, KeyLog.LogState.END, Boolean.valueOf(stopAbility));
            return stopAbility;
        }
        Log.error(LABEL, UNINIT_ABILITY_ERROR_LOG, Integer.valueOf(i));
        throw new IllegalStateException("ability is uninitialized, stop ability failed");
    }

    public final void setMainRoute(String str) throws IllegalStateException {
        if (this.abilityInfo.getType() == AbilityInfo.AbilityType.PAGE) {
            AbilitySliceRoute abilitySliceRoute = getAbilitySliceRoute();
            if (abilitySliceRoute != null) {
                abilitySliceRoute.setMainRoute(str);
                return;
            }
            throw new IllegalStateException("Should init abilitySliceRoute first");
        }
        throw new IllegalStateException("only page can set main route");
    }

    public final void addActionRoute(String str, String str2) throws IllegalStateException {
        if (this.abilityInfo.getType() != AbilityInfo.AbilityType.PAGE) {
            throw new IllegalStateException("only page can add action route");
        } else if (getAbilitySliceRoute() != null) {
            getAbilitySliceRoute().addActionRoute(str, str2);
        } else {
            throw new IllegalStateException("Should init abilitySliceRoute first");
        }
    }

    public final void setWindowBackgroundColor(int i, int i2, int i3) throws IllegalStateException {
        if (this.abilityInfo.getType() != AbilityInfo.AbilityType.PAGE) {
            throw new IllegalStateException("only page can set window background color");
        } else if (this.abilitySliceManager == null) {
            throw new IllegalStateException(NEEDINIT_SLICEMANAGER_LOG);
        } else if (i < 0 || i > 255 || i2 < 0 || i2 > 255 || i3 < 0 || i3 > 255) {
            Log.error(LABEL, "should set valid color value", new Object[0]);
            throw new IllegalArgumentException("invalid color value");
        } else {
            AbilityWindow abilityWindow2 = this.abilityWindow;
            if (abilityWindow2 != null) {
                abilityWindow2.setWindowBackgroundColor(i, i2, i3);
            } else {
                Log.error(LABEL, "window has not been created yet, set window back ground color failed", new Object[0]);
            }
        }
    }

    public final void keepBackgroundRunning(int i, NotificationRequest notificationRequest) {
        Log.info(LABEL, "keep ability service in background called", new Object[0]);
        if (notificationRequest != null) {
            AbilityInfo abilityInfo2 = this.abilityInfo;
            if (abilityInfo2 == null) {
                throw new IllegalStateException(NEEDINIT_ABILITY_LOG);
            } else if (abilityInfo2.getType() == AbilityInfo.AbilityType.SERVICE) {
                AbilityShellUtils.keepBackgroundRunning(this, i, notificationRequest);
            } else {
                throw new IllegalStateException("keep background failed, current type is " + this.abilityInfo.getType());
            }
        } else {
            throw new IllegalArgumentException("keep background failed. notificationRequest is null.");
        }
    }

    public final void cancelBackgroundRunning() throws IllegalStateException {
        Log.info(LABEL, "cancel ability service background running called", new Object[0]);
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 == null) {
            throw new IllegalStateException(NEEDINIT_ABILITY_LOG);
        } else if (abilityInfo2.getType() == AbilityInfo.AbilityType.SERVICE) {
            AbilityShellUtils.cancelBackgroundRunning(this);
        } else {
            throw new IllegalStateException("cancel background failed, current type is " + this.abilityInfo.getType());
        }
    }

    private void verifySupportForContinuation() {
        if (this.continuationManager == null) {
            throw new UnsupportedOperationException("This ability do not support continuation.");
        }
    }

    public final void continueAbility() throws IllegalStateException, UnsupportedOperationException {
        verifySupportForContinuation();
        this.continuationManager.continueAbility(false, null);
    }

    public final void continueAbility(String str) throws IllegalStateException, UnsupportedOperationException {
        verifySupportForContinuation();
        this.continuationManager.continueAbility(false, str);
    }

    public final void continueAbilityReversibly() throws IllegalStateException, UnsupportedOperationException {
        verifySupportForContinuation();
        this.continuationManager.continueAbility(true, null);
    }

    public final void continueAbilityReversibly(String str) throws IllegalStateException, UnsupportedOperationException {
        verifySupportForContinuation();
        this.continuationManager.continueAbility(true, str);
    }

    public final boolean reverseContinueAbility() throws IllegalStateException, UnsupportedOperationException {
        verifySupportForContinuation();
        return this.continuationManager.reverseContinueAbility();
    }

    public final ContinuationState getContinuationState() throws UnsupportedOperationException {
        verifySupportForContinuation();
        return this.continuationManager.getContinuationState();
    }

    public final void onSubmitContinuationRequestResult(boolean z) throws UnsupportedOperationException {
        verifySupportForContinuation();
        this.continuationManager.onSubmitContinuationRequestResult(z);
    }

    public final boolean scheduleRestoreFromRemote(IntentParams intentParams) throws UnsupportedOperationException {
        verifySupportForContinuation();
        return this.continuationManager.restoreFromRemote(intentParams);
    }

    public final void notifyRemoteTerminated() throws UnsupportedOperationException {
        verifySupportForContinuation();
        this.continuationManager.notifyRemoteTerminated();
    }

    public final String getOriginalDeviceId() throws UnsupportedOperationException {
        verifySupportForContinuation();
        return this.continuationManager.getOriginalDeviceId();
    }

    public final boolean scheduleStartContinuation() {
        verifySupportForContinuation();
        return this.continuationManager.startContinuation();
    }

    public final boolean scheduleSaveData(IntentParams intentParams) {
        verifySupportForContinuation();
        return this.continuationManager.saveData(intentParams);
    }

    public final boolean scheduleRestoreData(IntentParams intentParams, boolean z, String str) {
        verifySupportForContinuation();
        return this.continuationManager.restoreData(intentParams, z, str);
    }

    public final void scheduleCompleteContinuation(int i) {
        verifySupportForContinuation();
        this.continuationManager.completeContinuation(i);
    }

    /* access modifiers changed from: package-private */
    public AbilitySliceManager getAbilitySliceManager() {
        return this.abilitySliceManager;
    }

    public final void setAbilitySliceAnimator(AbilitySliceAnimator abilitySliceAnimator) throws IllegalStateException {
        AbilitySliceManager abilitySliceManager2 = this.abilitySliceManager;
        if (abilitySliceManager2 != null) {
            abilitySliceManager2.setAbilitySliceAnimator(abilitySliceAnimator);
            return;
        }
        throw new IllegalStateException(NEEDINIT_SLICEMANAGER_LOG);
    }

    public boolean isTerminating() {
        return this.isTerminating;
    }

    public void setIntent(Intent intent2) {
        this.intent = intent2;
    }

    public Intent getIntent() {
        return this.intent;
    }

    public boolean hasWindowFocus() {
        AbilityWindow abilityWindow2 = this.abilityWindow;
        if (abilityWindow2 != null) {
            return abilityWindow2.hasWindowFocus();
        }
        Log.error(LABEL, "window is not initialized yet, check window focus failed", new Object[0]);
        return false;
    }

    public Component getCurrentFocus() {
        if (isWindowProxyInitialized()) {
            return this.abilityWindow.getCurrentFocus();
        }
        Log.error(LABEL, "unable to get current window focus", new Object[0]);
        return null;
    }

    public boolean setSwipeToDismiss(boolean z) {
        AbilityWindow abilityWindow2 = this.abilityWindow;
        if (abilityWindow2 != null) {
            return abilityWindow2.setSwipeToDismiss(z);
        }
        Log.error(LABEL, "window is not initialized yet, set Swipe to Dismiss failed", new Object[0]);
        return false;
    }

    public void setVolumeTypeAdjustedByKey(int i) {
        AbilityWindow abilityWindow2 = this.abilityWindow;
        if (abilityWindow2 == null) {
            Log.error(LABEL, "window proxy is not initialized yet, set Volume Type failed.", new Object[0]);
        } else {
            abilityWindow2.setVolumeTypeAdjustedByKey(i);
        }
    }

    public int getVolumeTypeAdjustedByKey() {
        AbilityWindow abilityWindow2 = this.abilityWindow;
        if (abilityWindow2 != null) {
            return abilityWindow2.getVolumeTypeAdjustedByKey();
        }
        Log.error(LABEL, "window proxy is not initialized yet, get Volume Type failed.", new Object[0]);
        return -1;
    }

    public String getAbilityName() {
        String bundleName = this.abilityInfo.getBundleName();
        String className = this.abilityInfo.getClassName();
        int length = bundleName.length();
        return (!className.startsWith(bundleName) || className.length() <= length || className.charAt(length) != '.') ? className : className.substring(length + 1);
    }

    private void setWindowAttribute(Intent intent2) {
        if (intent2 == null) {
            Log.error(LABEL, "intent is null, set window layout failed.", new Object[0]);
            return;
        }
        AbilityWindow abilityWindow2 = this.abilityWindow;
        if (abilityWindow2 == null) {
            Log.error(LABEL, "window proxy is not initialized yet, set window layout failed.", new Object[0]);
        } else {
            abilityWindow2.setWindowAttribute(intent2);
        }
    }

    private void setUiAttachedAllowed(boolean z) {
        UIContent uIContent = this.uiContent;
        if (uIContent != null) {
            uIContent.setUiAttachedAllowed(z);
        }
    }

    private void start(Intent intent2) throws LifecycleException, IllegalStateException, IllegalArgumentException {
        if (this.lifecycleState == STATE_INITIAL) {
            AbilityWindow abilityWindow2 = this.abilityWindow;
            if (abilityWindow2 != null) {
                abilityWindow2.onPreAbilityStart();
                Bytrace.startTrace(2147483648L, "setlayout");
                setWindowAttribute(intent2);
                Bytrace.finishTrace(2147483648L, "setlayout");
                setUiAttachedAllowed(true);
            }
            Bytrace.startTrace(2147483648L, "onStart");
            long currentTimeMillis = System.currentTimeMillis();
            onStart(intent2);
            debugLog(JLogConstants.JLID_ABILITY_ONSTART, this.abilityInfo, currentTimeMillis);
            Bytrace.finishTrace(2147483648L, "onStart");
            AbilityInfo abilityInfo2 = this.abilityInfo;
            if (abilityInfo2 != null && abilityInfo2.getType() == AbilityInfo.AbilityType.PAGE) {
                dispatchLifecycle(Lifecycle.Event.ON_START, intent2);
            }
            dispatchAbilityLifecycle(intent2, AbilityLifecycleExecutor.Action.START);
            AbilityWindow abilityWindow3 = this.abilityWindow;
            if (abilityWindow3 != null) {
                abilityWindow3.onPostAbilityStart();
            }
            this.lifecycleState = STATE_INACTIVE;
            return;
        }
        throw new LifecycleException(LIFECYCLE_CHANGE_ERROR_LOG + this.lifecycleState + ", action: start");
    }

    private void stop() throws LifecycleException, IllegalStateException, IllegalArgumentException {
        if (this.lifecycleState == STATE_BACKGROUND) {
            this.isTerminating = true;
            if (this.abilityWindow != null) {
                setUiAttachedAllowed(false);
            }
            Bytrace.startTrace(2147483648L, "ability onStop");
            long currentTimeMillis = System.currentTimeMillis();
            onStop();
            debugLog(JLogConstants.JLID_ABILITY_ONSTOP, this.abilityInfo, currentTimeMillis);
            Bytrace.finishTrace(2147483648L, "ability onStop");
            AbilityInfo abilityInfo2 = this.abilityInfo;
            if (abilityInfo2 != null && abilityInfo2.getType() == AbilityInfo.AbilityType.PAGE) {
                dispatchLifecycle(Lifecycle.Event.ON_STOP, null);
            }
            dispatchAbilityLifecycle(null, AbilityLifecycleExecutor.Action.STOP);
            AbilityWindow abilityWindow2 = this.abilityWindow;
            if (abilityWindow2 != null) {
                abilityWindow2.onPostAbilityStop();
                this.abilityWindow = null;
            }
            this.lifecycleState = STATE_UNINITIALIZED;
            ConnectionScheduler connectionScheduler2 = this.connectionScheduler;
            if (connectionScheduler2 != null) {
                try {
                    connectionScheduler2.closeServiceConnection(this, null);
                } catch (IllegalStateException unused) {
                    Log.info(LABEL, "conn is busy while ability disconnectAbility", new Object[0]);
                }
            }
            UIContent uIContent = this.uiContent;
            if (uIContent != null) {
                uIContent.reset();
                this.uiContent = null;
            }
            restoreServiceContext();
            this.sliceManagerInitializationDone = false;
            return;
        }
        throw new LifecycleException(LIFECYCLE_CHANGE_ERROR_LOG + this.lifecycleState + ", action: stop");
    }

    private void active(Intent intent2) throws LifecycleException, IllegalStateException, IllegalArgumentException {
        if (this.lifecycleState == STATE_INACTIVE) {
            Bytrace.startTrace(2147483648L, "setlayout");
            setWindowAttribute(intent2);
            Bytrace.finishTrace(2147483648L, "setlayout");
            setUiAttachedAllowed(true);
            long currentTimeMillis = System.currentTimeMillis();
            Bytrace.startTrace(2147483648L, "ability onActive");
            onActive();
            debugLog(JLogConstants.JLID_ABILITY_ONACTIVE, this.abilityInfo, currentTimeMillis);
            Bytrace.finishTrace(2147483648L, "ability onActive");
            AbilityInfo abilityInfo2 = this.abilityInfo;
            if (abilityInfo2 != null && abilityInfo2.getType() == AbilityInfo.AbilityType.PAGE) {
                dispatchLifecycle(Lifecycle.Event.ON_ACTIVE, null);
            }
            dispatchAbilityLifecycle(intent2, AbilityLifecycleExecutor.Action.ACTIVE);
            this.lifecycleState = STATE_ACTIVE;
            return;
        }
        throw new LifecycleException(LIFECYCLE_CHANGE_ERROR_LOG + this.lifecycleState + ", action: active");
    }

    private void inactive() throws LifecycleException, IllegalStateException, IllegalArgumentException {
        if (this.lifecycleState == STATE_ACTIVE) {
            setUiAttachedAllowed(true);
            Bytrace.startTrace(2147483648L, "ability onInactive");
            long currentTimeMillis = System.currentTimeMillis();
            onInactive();
            debugLog(JLogConstants.JLID_ABILITY_ONINACTIVE, this.abilityInfo, currentTimeMillis);
            Bytrace.finishTrace(2147483648L, "ability onInactive");
            AbilityInfo abilityInfo2 = this.abilityInfo;
            if (abilityInfo2 != null && abilityInfo2.getType() == AbilityInfo.AbilityType.PAGE) {
                dispatchLifecycle(Lifecycle.Event.ON_INACTIVE, null);
            }
            dispatchAbilityLifecycle(null, AbilityLifecycleExecutor.Action.INACTIVE);
            this.lifecycleState = STATE_INACTIVE;
            return;
        }
        throw new LifecycleException(LIFECYCLE_CHANGE_ERROR_LOG + this.lifecycleState + ", action: inactive");
    }

    private void foreground(Intent intent2) throws LifecycleException, IllegalStateException, IllegalArgumentException {
        if (this.lifecycleState == STATE_BACKGROUND) {
            setUiAttachedAllowed(true);
            Bytrace.startTrace(2147483648L, "ability onForeground");
            long currentTimeMillis = System.currentTimeMillis();
            onForeground(intent2);
            debugLog(JLogConstants.JLID_ABILITY_ONFOREGROUNG, this.abilityInfo, currentTimeMillis);
            Bytrace.finishTrace(2147483648L, "ability onForeground");
            AbilityInfo abilityInfo2 = this.abilityInfo;
            if (abilityInfo2 != null && abilityInfo2.getType() == AbilityInfo.AbilityType.PAGE) {
                dispatchLifecycle(Lifecycle.Event.ON_FOREGROUND, intent2);
            }
            dispatchAbilityLifecycle(intent2, AbilityLifecycleExecutor.Action.FOREGROUND);
            this.lifecycleState = STATE_INACTIVE;
            return;
        }
        throw new LifecycleException(LIFECYCLE_CHANGE_ERROR_LOG + this.lifecycleState + ", action: foreground");
    }

    private void background() throws LifecycleException, IllegalStateException, IllegalArgumentException {
        if (this.lifecycleState == STATE_INACTIVE) {
            setUiAttachedAllowed(false);
            Bytrace.startTrace(2147483648L, "ability onBackground");
            long currentTimeMillis = System.currentTimeMillis();
            onBackground();
            debugLog(JLogConstants.JLID_ABILITY_ONBACKGROUNG, this.abilityInfo, currentTimeMillis);
            Bytrace.finishTrace(2147483648L, "ability onBackground");
            AbilityInfo abilityInfo2 = this.abilityInfo;
            if (abilityInfo2 != null && abilityInfo2.getType() == AbilityInfo.AbilityType.PAGE) {
                dispatchLifecycle(Lifecycle.Event.ON_BACKGROUND, null);
            }
            dispatchAbilityLifecycle(null, AbilityLifecycleExecutor.Action.BACKGROUND);
            this.lifecycleState = STATE_BACKGROUND;
            return;
        }
        throw new LifecycleException(LIFECYCLE_CHANGE_ERROR_LOG + this.lifecycleState + ", action: background");
    }

    public final int getCurrentState() {
        return this.lifecycleState;
    }

    public final AbilityLifecycleExecutor.LifecycleState getState() {
        return AbilityLifecycleExecutor.LifecycleState.intToEnum(this.lifecycleState);
    }

    /* access modifiers changed from: package-private */
    public final ConnectionScheduler getConnectionScheduler() {
        return this.connectionScheduler;
    }

    /* access modifiers changed from: package-private */
    public final void registerAbilityLifecycleCallback(IAbilityLifecycleCallback iAbilityLifecycleCallback) {
        if (iAbilityLifecycleCallback != null) {
            this.callback = iAbilityLifecycleCallback;
            return;
        }
        throw new IllegalArgumentException("Callback is illegal");
    }

    private IRemoteObject connectAbilityService(Intent intent2) throws IllegalStateException, IllegalArgumentException {
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 == null) {
            throw new IllegalStateException(NEEDINIT_ABILITY_LOG);
        } else if (abilityInfo2.getType() == AbilityInfo.AbilityType.SERVICE) {
            Log.info(LABEL, "start to connectAbility. name:%{public}s, element:%s.", this.abilityInfo.getClassName(), Optional.ofNullable(intent2).map($$Lambda$5NIH3kVWNfBSHcIt4qMC1aQ8cu0.INSTANCE).map($$Lambda$Gbq1Su8pWvMr5cS7vkDL3qT3QMg.INSTANCE).orElse("null"));
            Bytrace.startTrace(2147483648L, "onConnect");
            IRemoteObject onConnect = onConnect(intent2);
            Bytrace.finishTrace(2147483648L, "onConnect");
            return onConnect;
        } else {
            throw new IllegalArgumentException("connect ability failed, current type is " + this.abilityInfo.getType());
        }
    }

    private void disconnectAbilityService(Intent intent2) throws IllegalStateException, IllegalArgumentException {
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 == null) {
            throw new IllegalStateException(NEEDINIT_ABILITY_LOG);
        } else if (abilityInfo2.getType() == AbilityInfo.AbilityType.SERVICE) {
            Log.info(LABEL, "start to disconnectAbility. name:%{public}s.", this.abilityInfo.getClassName());
            Bytrace.startTrace(2147483648L, "onDisconnect");
            onDisconnect(intent2);
            Bytrace.finishTrace(2147483648L, "onDisconnect");
        } else {
            throw new IllegalArgumentException("disconnect ability failed, current type is: " + this.abilityInfo.getType());
        }
    }

    private DataAbilityResult[] executeBatchInner(ArrayList<DataAbilityOperation> arrayList) throws OperationExecuteException {
        if (arrayList == null) {
            return new DataAbilityResult[0];
        }
        int size = arrayList.size();
        DataAbilityResult[] dataAbilityResultArr = new DataAbilityResult[size];
        for (int i = 0; i < size; i++) {
            DataAbilityOperation dataAbilityOperation = arrayList.get(i);
            if (dataAbilityOperation == null) {
                dataAbilityResultArr[i] = new DataAbilityResult(0);
            } else {
                executeOperation(dataAbilityOperation, dataAbilityResultArr, i);
            }
        }
        return dataAbilityResultArr;
    }

    public void executeOperation(DataAbilityOperation dataAbilityOperation, DataAbilityResult[] dataAbilityResultArr, int i) throws OperationExecuteException {
        int i2;
        if (this.abilityInfo.getType() != AbilityInfo.AbilityType.DATA) {
            throw new IllegalArgumentException("data ability type failed, current type is: " + this.abilityInfo.getType());
        } else if (i < 0) {
            throw new IllegalArgumentException("operation result index should not below zero");
        } else if (dataAbilityResultArr == null) {
            throw new OperationExecuteException("reference is invalid");
        } else if (i < dataAbilityResultArr.length) {
            int i3 = 0;
            if (dataAbilityOperation == null) {
                dataAbilityResultArr[i] = new DataAbilityResult(0);
                return;
            }
            ValuesBucket parseValuesBucketReference = parseValuesBucketReference(dataAbilityResultArr, dataAbilityOperation, i);
            DataAbilityPredicates parsePredictionArgsReference = parsePredictionArgsReference(dataAbilityResultArr, dataAbilityOperation, i);
            if (dataAbilityOperation.isInsertOperation()) {
                i2 = insert(dataAbilityOperation.getUri(), parseValuesBucketReference);
            } else if (dataAbilityOperation.isDeleteOperation()) {
                i2 = delete(dataAbilityOperation.getUri(), parsePredictionArgsReference);
            } else if (dataAbilityOperation.isUpdateOperation()) {
                i2 = update(dataAbilityOperation.getUri(), parseValuesBucketReference, parsePredictionArgsReference);
            } else if (dataAbilityOperation.isAssertOperation()) {
                ResultSet query = query(dataAbilityOperation.getUri(), null, parsePredictionArgsReference);
                if (query != null) {
                    i3 = query.getRowCount();
                }
                if (!checkAssertQueryResult(query, dataAbilityOperation.getValuesBucket())) {
                    if (query != null) {
                        query.close();
                    }
                    throw new OperationExecuteException("Query Result is not equal to expected value.");
                }
                if (query != null) {
                    query.close();
                }
                i2 = i3;
            } else {
                throw new IllegalStateException("bad type, " + dataAbilityOperation.getType());
            }
            if (dataAbilityOperation.getExpectedCount() == null || dataAbilityOperation.getExpectedCount().intValue() == i2) {
                dataAbilityResultArr[i] = new DataAbilityResult(i2);
                return;
            }
            throw new OperationExecuteException("Expected " + dataAbilityOperation.getExpectedCount() + " rows but actual " + i2);
        } else {
            throw new OperationExecuteException("reference and refIndex is not match");
        }
    }

    private long changeRefToValue(DataAbilityResult[] dataAbilityResultArr, int i, Integer num) throws OperationExecuteException {
        if (num.intValue() >= i) {
            throw new OperationExecuteException("asked for reference " + num + " but there are only " + i + " references");
        } else if (dataAbilityResultArr == null) {
            throw new OperationExecuteException("reference is invalid");
        } else if (num.intValue() >= dataAbilityResultArr.length) {
            throw new OperationExecuteException("reference and refIndex is not match");
        } else if (dataAbilityResultArr[num.intValue()] != null) {
            DataAbilityResult dataAbilityResult = dataAbilityResultArr[num.intValue()];
            if (dataAbilityResult.getUri() != null) {
                return DataUriUtils.getId(dataAbilityResult.getUri());
            }
            return (long) dataAbilityResult.getCount().intValue();
        } else {
            throw new OperationExecuteException("DataAbilityResult is empty");
        }
    }

    public ValuesBucket parseValuesBucketReference(DataAbilityResult[] dataAbilityResultArr, DataAbilityOperation dataAbilityOperation, int i) throws OperationExecuteException {
        ValuesBucket valuesBucket;
        if (dataAbilityOperation == null) {
            throw new OperationExecuteException("operation is invalid");
        } else if (dataAbilityOperation.getValuesBucketReferences() == null) {
            return dataAbilityOperation.getValuesBucket();
        } else {
            if (dataAbilityOperation.getValuesBucket() == null) {
                valuesBucket = new ValuesBucket();
            } else {
                valuesBucket = new ValuesBucket(dataAbilityOperation.getValuesBucket());
            }
            for (Map.Entry<String, Object> entry : dataAbilityOperation.getValuesBucketReferences().getAll()) {
                String key = entry.getKey();
                Integer integer = dataAbilityOperation.getValuesBucketReferences().getInteger(key);
                if (integer != null) {
                    valuesBucket.putLong(key, Long.valueOf(changeRefToValue(dataAbilityResultArr, i, integer)));
                } else {
                    throw new OperationExecuteException("values reference " + key + " is not an integer");
                }
            }
            return valuesBucket;
        }
    }

    public DataAbilityPredicates parsePredictionArgsReference(DataAbilityResult[] dataAbilityResultArr, DataAbilityOperation dataAbilityOperation, int i) throws OperationExecuteException {
        if (dataAbilityOperation == null) {
            throw new OperationExecuteException("operation is invalid");
        } else if (dataAbilityOperation.getDataAbilityPredicatesBackReferences() == null) {
            return dataAbilityOperation.getDataAbilityPredicates();
        } else {
            if (dataAbilityOperation.getDataAbilityPredicates() != null) {
                List<String> whereArgs = dataAbilityOperation.getDataAbilityPredicates().getWhereArgs();
                if (whereArgs != null) {
                    ArrayList arrayList = new ArrayList(whereArgs.size());
                    arrayList.addAll(whereArgs);
                    for (Map.Entry<Integer, Integer> entry : dataAbilityOperation.getDataAbilityPredicatesBackReferences().entrySet()) {
                        arrayList.set(entry.getKey().intValue(), String.valueOf(changeRefToValue(dataAbilityResultArr, i, Integer.valueOf(entry.getValue().intValue()))));
                    }
                    DataAbilityPredicates dataAbilityPredicates = dataAbilityOperation.getDataAbilityPredicates();
                    dataAbilityPredicates.setWhereArgs(arrayList);
                    return dataAbilityPredicates;
                }
                throw new OperationExecuteException("sqlargs is invalid in operation");
            }
            throw new OperationExecuteException("predicates is not exist");
        }
    }

    private boolean checkAssertQueryResult(ResultSet resultSet, ValuesBucket valuesBucket) {
        if (!(valuesBucket == null || resultSet == null)) {
            Set<Map.Entry<String, Object>> all = valuesBucket.getAll();
            int rowCount = resultSet.getRowCount();
            for (int i = 0; i < rowCount; i++) {
                for (Map.Entry<String, Object> entry : all) {
                    String string = resultSet.getString(resultSet.getColumnIndexForName(entry.getKey()));
                    String obj = entry.getValue() != null ? entry.getValue().toString() : null;
                    if (!((string == null && obj == null) || string == null || string.equals(obj))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static void debugLog(int i, AbilityInfo abilityInfo2, long j) {
        if (abilityInfo2 != null) {
            long currentTimeMillis = System.currentTimeMillis();
            JLog.debug(i, abilityInfo2.getBundleName() + PsuedoNames.PSEUDONAME_ROOT + abilityInfo2.getClassName() + " cost: " + (currentTimeMillis - j) + "ms");
        }
    }

    private class AbilityFormProvider extends IAbilityFormProvider.FormProviderStub {
        private AbilityFormProvider() {
        }

        /* synthetic */ AbilityFormProvider(Ability ability, AnonymousClass1 r2) {
            this();
        }

        @Override // ohos.aafwk.ability.IAbilityFormProvider
        public AbilityForm acquireAbilityForm() {
            if (Ability.this.verifyCallingOrSelfPermission(AbilityForm.PERMISSION_REQUIRE_FORM) != 0) {
                Log.warn(Ability.LABEL, "acquireAbilityForm caller permission denied", new Object[0]);
                return null;
            }
            if (Ability.this.abilityForm == null) {
                Ability ability = Ability.this;
                ability.abilityForm = ability.onCreateForm();
            }
            return Ability.this.abilityForm;
        }

        @Override // ohos.aafwk.ability.IAbilityFormProvider
        public ProviderFormInfo acquireProviderFormInfo(Intent intent) {
            if (Ability.this.verifyCallingOrSelfPermission(AbilityForm.PERMISSION_REQUIRE_FORM) == 0) {
                return Ability.this.onCreateForm(intent);
            }
            Log.warn(Ability.LABEL, "acquireProviderFormInfo caller permission denied", new Object[0]);
            return null;
        }

        @Override // ohos.aafwk.ability.IAbilityFormProvider
        public void notifyFormDelete(int i) {
            Ability.this.onDeleteForm(i);
        }

        @Override // ohos.aafwk.ability.IAbilityFormProvider
        public void notifyFormUpdate(int i) {
            Ability.this.onUpdateForm(i);
        }

        @Override // ohos.aafwk.ability.IAbilityFormProvider
        public void fireFormEvent(int i, String str) {
            Ability.this.onTriggerFormEvent(i, str);
        }
    }

    private void initBasicEnv(AbilityInfo abilityInfo2) {
        if (!this.basicInitializationDone) {
            this.abilityInfo = abilityInfo2;
            super.init();
            this.basicInitializationDone = true;
        }
    }

    private AbilitySliceRoute getAbilitySliceRoute() throws IllegalStateException {
        AbilitySliceManager abilitySliceManager2 = this.abilitySliceManager;
        if (abilitySliceManager2 != null) {
            return abilitySliceManager2.getAbilitySliceRoute();
        }
        throw new IllegalStateException(NEEDINIT_SLICEMANAGER_LOG);
    }

    private boolean isEventHandlingCapable() {
        if (this.abilityInfo.getType() == AbilityInfo.AbilityType.PAGE) {
            return isWindowProxyInitialized() && !this.isTerminating;
        }
        EventInfo eventInfo = new EventInfo();
        eventInfo.setEventId(AbilityHiviewWrapper.EVENT_ID_ZFRAMEWORK_DISPATCH_EVENT_FAILED);
        eventInfo.setBundleName(this.abilityInfo.getBundleName());
        eventInfo.setAbilityName(this.abilityInfo.getClassName());
        eventInfo.setErrorType(1);
        AbilityHiviewWrapper.sendEvent(eventInfo);
        Log.error(LABEL, "no-page ability do not handle input event", new Object[0]);
        return false;
    }

    private boolean isWindowProxyInitialized() {
        if (this.abilitySliceManager == null) {
            Log.error(LABEL, "slice manager is not initialized yet", new Object[0]);
            return false;
        } else if (this.abilityWindow != null) {
            return true;
        } else {
            Log.error(LABEL, "window is not initialized yet", new Object[0]);
            return false;
        }
    }

    private void dumpHelp(String str, PrintWriter printWriter) {
        printWriter.println(str + "Dump ability and slice tools");
        printWriter.println(str + "Version 1.0.0");
        printWriter.println();
        printWriter.println(str + "Supported additional option:");
        printWriter.println(PREFIX + str + "[-h]                  dump tool helper");
        printWriter.println(PREFIX + str + "[-a]                  only dump ability information");
        printWriter.println(PREFIX + str + "[-s]                  only dump PAGE ability's slice information");
        printWriter.println(PREFIX + str + "[-x <xml path>]       dump top ability slice component container information to xml");
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX);
        sb.append(str);
        Log.dumpHelp(sb.toString(), printWriter);
        printWriter.println(PREFIX + str + "[](no args)           dump ability and slice information");
        printWriter.println();
        printWriter.println(str + "Examples:");
        printWriter.println(PREFIX + str + "dumpsys activity xxActivity -ability -a");
    }

    private void dumpAbility(String str, PrintWriter printWriter) {
        printWriter.print(str + "Ability type: " + this.abilityInfo.getType());
        if (this.abilityFormProvider != null) {
            printWriter.print(" [Form]");
        }
        printWriter.println();
        printWriter.println(str + "Ability state: " + this.lifecycleState);
        int i = AnonymousClass1.$SwitchMap$ohos$bundle$AbilityInfo$AbilityType[this.abilityInfo.getType().ordinal()];
        if (i == 1) {
            printWriter.println(str + "Ability orientation: " + this.abilityInfo.getOrientation());
            printWriter.println(str + "Ability launchMode: " + this.abilityInfo.getLaunchMode());
            printWriter.println(str + "Ability process: " + this.abilityInfo.getProcess());
            printWriter.println(str + "Ability targetAbility: " + this.abilityInfo.getTargetAbility());
            this.abilitySliceManager.dumpRoute(str, printWriter);
            if (this.abilityFormProvider == null) {
                printWriter.println(str + "Ability connected service list:");
                ConnectionScheduler connectionScheduler2 = this.connectionScheduler;
                connectionScheduler2.dumpServiceList(PREFIX + str, printWriter, this);
            }
        } else if (i == 2) {
            printWriter.println(str + "Service connected service list:");
            ConnectionScheduler connectionScheduler3 = this.connectionScheduler;
            connectionScheduler3.dumpServiceList(PREFIX + str, printWriter, this);
        }
    }

    private void dispatchAbilityLifecycle(Intent intent2, AbilityLifecycleExecutor.Action action) throws IllegalStateException {
        if (this.callback != null) {
            switch (action) {
                case START:
                    this.callback.onAbilityStart(intent2);
                    return;
                case STOP:
                    this.callback.onAbilityStop();
                    return;
                case ACTIVE:
                    this.callback.onAbilityActive(intent2);
                    return;
                case INACTIVE:
                    this.callback.onAbilityInactive();
                    return;
                case FOREGROUND:
                    this.callback.onAbilityForeground(intent2);
                    return;
                case BACKGROUND:
                    this.callback.onAbilityBackground();
                    return;
                default:
                    Log.error(LABEL, "Unsupported action:%{public}s", action);
                    return;
            }
        }
    }

    private void initAbilityDelegation(Intent intent2) {
        if (intent2 == null) {
            Log.warn(LABEL, "Intent is null, no need to init AbilityDelegation", new Object[0]);
            return;
        }
        this.abilityDelegation = getDelegation();
        if (this.abilityDelegation != null) {
            updateAbilityDelegation(intent2);
            return;
        }
        String stringParam = intent2.getStringParam(AbilityDelegation.RUN_TEST);
        if (stringParam == null || stringParam.isEmpty()) {
            Log.debug(LABEL, "RUN_TEST is null, no need to init AbilityDelegation", new Object[0]);
            return;
        }
        String stringParam2 = intent2.getStringParam(AbilityDelegation.LIB_PATH);
        if (stringParam2 == null || stringParam2.isEmpty()) {
            Log.warn(LABEL, "LIB_PATH is null, use inside ClassLoader", new Object[0]);
            stringParam2 = "";
        }
        try {
            ClassLoader createClassLoader = ClassLoaderFactory.createClassLoader(stringParam2, getClass().getClassLoader());
            Constructor<?> declaredConstructor = createClassLoader.loadClass(DELEGATION).getDeclaredConstructor(new Class[0]);
            declaredConstructor.setAccessible(true);
            Object newInstance = declaredConstructor.newInstance(new Object[0]);
            if (newInstance instanceof AbilityDelegation) {
                this.abilityDelegation = (AbilityDelegation) newInstance;
                initDelegation(this, intent2, createClassLoader);
                return;
            }
            Log.error(LABEL, "[%{public}s] should extends AbilityDelegation", DELEGATION);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            Log.error(LABEL, "Construct [%{public}s] error: %{public}s", DELEGATION, e);
        }
    }

    private AbilityDelegation getDelegation() {
        AbilityDelegation abilityDelegation2 = this.abilityDelegation;
        if (abilityDelegation2 != null) {
            return abilityDelegation2;
        }
        try {
            Method declaredMethod = AbilityDelegation.class.getDeclaredMethod("getInstance", new Class[0]);
            declaredMethod.setAccessible(true);
            Object invoke = declaredMethod.invoke(null, new Object[0]);
            if (invoke instanceof AbilityDelegation) {
                return (AbilityDelegation) invoke;
            }
            return null;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.error(LABEL, "Get instance of AbilityDelegation failed. %{public}s", e);
            return null;
        }
    }

    private void updateAbilityDelegation(Intent intent2) {
        if (this.abilityDelegation == null) {
            Log.error(LABEL, "abilityDelegation is null, updateAbility failed.", new Object[0]);
            return;
        }
        try {
            Method declaredMethod = AbilityDelegation.class.getDeclaredMethod("updateAbility", Ability.class, Intent.class);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(this.abilityDelegation, this, intent2);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.error(LABEL, "updateAbility failed.%{public}s", e);
        }
    }

    private void initDelegation(Ability ability, Intent intent2, ClassLoader classLoader) {
        if (this.abilityDelegation == null) {
            Log.error(LABEL, "abilityDelegation is null, init failed.", new Object[0]);
            return;
        }
        try {
            Method declaredMethod = AbilityDelegation.class.getDeclaredMethod("init", Ability.class, Intent.class, ClassLoader.class);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(this.abilityDelegation, ability, intent2, classLoader);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.error(LABEL, "AbilityDelegation init failed.%{public}s", e);
        }
    }

    private void runDelegation() {
        if (this.abilityDelegation == null) {
            Log.error(LABEL, "abilityDelegation is null, run failed.", new Object[0]);
            return;
        }
        try {
            Method declaredMethod = AbilityDelegation.class.getDeclaredMethod("runTestCase", new Class[0]);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(this.abilityDelegation, new Object[0]);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            Log.error(LABEL, "AbilityDelegation run failed.%{public}s", e);
        }
    }

    private void handleLifecycleTransaction(Intent intent2, int i) throws IllegalArgumentException, LifecycleException {
        AbilityInfo abilityInfo2;
        if (this.lifecycleState == STATE_INITIAL) {
            if (intent2 == null && ((abilityInfo2 = this.abilityInfo) == null || abilityInfo2.getType() == AbilityInfo.AbilityType.PAGE)) {
                throw new IllegalArgumentException("input argument intent is null");
            }
            if (intent2 != null) {
                initAbilityDelegation(intent2);
            }
            start(intent2);
        }
        if (this.lifecycleState == STATE_ACTIVE) {
            inactive();
        }
        AbilityLifecycleExecutor.LifecycleState intToEnum = AbilityLifecycleExecutor.LifecycleState.intToEnum(i);
        if (intToEnum != null) {
            int i2 = AnonymousClass1.$SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$LifecycleState[intToEnum.ordinal()];
            if (i2 == 1) {
                if (this.lifecycleState == STATE_INACTIVE) {
                    background();
                }
                stop();
            } else if (i2 != 2) {
                if (i2 == 3) {
                    if (this.lifecycleState == STATE_BACKGROUND) {
                        if (this.abilityDelegation != null) {
                            updateAbilityDelegation(intent2);
                        }
                        foreground(intent2);
                    }
                    active(intent2);
                    if (this.abilityDelegation != null) {
                        runDelegation();
                    }
                } else if (i2 != 4) {
                    throw new LifecycleException("Error target state:" + i);
                } else if (this.lifecycleState == STATE_INACTIVE) {
                    background();
                }
            } else if (this.lifecycleState == STATE_BACKGROUND) {
                if (this.abilityDelegation != null) {
                    updateAbilityDelegation(intent2);
                }
                foreground(intent2);
            }
        } else {
            throw new LifecycleException("Error target state:" + i);
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.aafwk.ability.Ability$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$LifecycleState = new int[AbilityLifecycleExecutor.LifecycleState.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$bundle$AbilityInfo$AbilityType = new int[AbilityInfo.AbilityType.values().length];

        static {
            try {
                $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$LifecycleState[AbilityLifecycleExecutor.LifecycleState.INITIAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$LifecycleState[AbilityLifecycleExecutor.LifecycleState.INACTIVE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$LifecycleState[AbilityLifecycleExecutor.LifecycleState.ACTIVE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$LifecycleState[AbilityLifecycleExecutor.LifecycleState.BACKGROUND.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$Action = new int[AbilityLifecycleExecutor.Action.values().length];
            try {
                $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$Action[AbilityLifecycleExecutor.Action.START.ordinal()] = 1;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$Action[AbilityLifecycleExecutor.Action.STOP.ordinal()] = 2;
            } catch (NoSuchFieldError unused6) {
            }
            try {
                $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$Action[AbilityLifecycleExecutor.Action.ACTIVE.ordinal()] = 3;
            } catch (NoSuchFieldError unused7) {
            }
            try {
                $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$Action[AbilityLifecycleExecutor.Action.INACTIVE.ordinal()] = 4;
            } catch (NoSuchFieldError unused8) {
            }
            try {
                $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$Action[AbilityLifecycleExecutor.Action.FOREGROUND.ordinal()] = 5;
            } catch (NoSuchFieldError unused9) {
            }
            try {
                $SwitchMap$ohos$aafwk$ability$AbilityLifecycleExecutor$Action[AbilityLifecycleExecutor.Action.BACKGROUND.ordinal()] = 6;
            } catch (NoSuchFieldError unused10) {
            }
            try {
                $SwitchMap$ohos$bundle$AbilityInfo$AbilityType[AbilityInfo.AbilityType.PAGE.ordinal()] = 1;
            } catch (NoSuchFieldError unused11) {
            }
            try {
                $SwitchMap$ohos$bundle$AbilityInfo$AbilityType[AbilityInfo.AbilityType.SERVICE.ordinal()] = 2;
            } catch (NoSuchFieldError unused12) {
            }
            try {
                $SwitchMap$ohos$bundle$AbilityInfo$AbilityType[AbilityInfo.AbilityType.DATA.ordinal()] = 3;
            } catch (NoSuchFieldError unused13) {
            }
        }
    }
}
