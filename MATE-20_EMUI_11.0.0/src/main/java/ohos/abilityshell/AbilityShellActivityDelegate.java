package ohos.abilityshell;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import ohos.aafwk.ability.IAbilityContinuation;
import ohos.aafwk.content.Intent;
import ohos.abilityshell.IReverseContinuationSchedulerMaster;
import ohos.abilityshell.IReverseContinuationSchedulerSlave;
import ohos.abilityshell.delegation.AbilityDelegator;
import ohos.abilityshell.utils.AbilityLoader;
import ohos.abilityshell.utils.AbilityResolver;
import ohos.abilityshell.utils.AbilityShellConverterUtils;
import ohos.abilityshell.utils.BinderConverter;
import ohos.abilityshell.utils.LifecycleState;
import ohos.abilityshell.utils.SelectAbilityUtils;
import ohos.accessibility.AccessibilityEventInfo;
import ohos.accessibility.BarrierFreeInnerClient;
import ohos.app.ContextDeal;
import ohos.app.dispatcher.threading.AndroidTaskLooper;
import ohos.appexecfwk.utils.AppLog;
import ohos.appexecfwk.utils.HiTraceUtil;
import ohos.appexecfwk.utils.JLogUtil;
import ohos.appexecfwk.utils.StringUtils;
import ohos.bundle.AbilityInfo;
import ohos.bundle.BundleInfo;
import ohos.bundle.ElementName;
import ohos.devtools.JLogConstants;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.multimodalinput.event.MouseEvent;
import ohos.multimodalinput.event.MultimodalEvent;
import ohos.multimodalinput.event.RotationEvent;
import ohos.multimodalinput.event.TouchEvent;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;
import ohos.security.permission.PermissionConversion;
import ohos.tools.Bytrace;
import ohos.utils.PacMap;
import ohos.utils.adapter.PacMapUtils;

public class AbilityShellActivityDelegate extends AbilityShellDelegate {
    private static final int DEFAULT_DMS_SESSION_ID = 0;
    private static final String DMS_ORIGIN_DEVICE_ID = "deviceId";
    private static final String DMS_SESSION_ID = "sessionId";
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private Object abilityShell;
    private ContextDeal contextDeal;
    private ContinuationHandler continuationHandler;
    private IReverseContinuationSchedulerSlave.ReverseContinuationSchedulerSlaveStub continuationSchedulerSlave;
    private IRemoteObject continueToken = null;
    private boolean isLoadAsForm = false;
    private final Object tokenLock = new Object();
    private volatile boolean tokenRegistered = false;

    public AbilityShellActivityDelegate(IAbilityShell iAbilityShell) {
        this.abilityShell = iAbilityShell;
    }

    public void onCreate(Bundle bundle) {
        Activity activity = (Activity) this.abilityShell;
        HarmonyLoader.waitForLoadHarmony();
        Bytrace.startTrace(2147483648L, "startAbility_onCreate");
        this.abilityInfo = AbilityShellConverterUtils.convertToAbilityInfo(createShellInfo(this.abilityShell));
        if (this.abilityInfo == null) {
            AppLog.w(SHELL_LABEL, "AbilityShellActivityDelegate::onCreate can't find AbilityInfo and ShellInfo relationship from bms", new Object[0]);
            return;
        }
        BundleInfo bundleInfo = HarmonyApplication.getInstance().getBundleInfo();
        if (bundleInfo.isDifferentName()) {
            this.abilityInfo.setClassName(this.abilityInfo.getClassName().replaceFirst(bundleInfo.getOriginalName(), bundleInfo.getName()));
            AppLog.d(SHELL_LABEL, "AbilityShellActivityDelegate::onCreate ability class name %{private}s", this.abilityInfo.getClassName());
        }
        AbilityInfo abilityInfoByName = bundleInfo.getAbilityInfoByName(this.abilityInfo.getClassName());
        if (abilityInfoByName != null) {
            this.abilityInfo = abilityInfoByName;
        }
        Optional<Intent> mapToHarmonyIntent = mapToHarmonyIntent(activity.getIntent());
        if (!mapToHarmonyIntent.isPresent()) {
            AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::onCreate createZidaneIntent failed", new Object[0]);
            return;
        }
        this.zidaneIntent = mapToHarmonyIntent.get();
        byte[] byteArrayParam = this.zidaneIntent.getByteArrayParam("hitraceId");
        if (!HiTrace.getId().isValid() && byteArrayParam != null) {
            HiTrace.setId(new HiTraceId(byteArrayParam));
        }
        this.contextDeal = createActivityContextDeal(this.abilityInfo);
        Bytrace.finishTrace(2147483648L, "startAbility_onCreate");
        checkHapHasLoaded(this.abilityInfo);
        handleLoadAbility();
        if (isFlagExists(8, this.zidaneIntent.getFlags())) {
            AppLog.i(SHELL_LABEL, "AbilityShellActivityDelegate::onCreate FLAG_ABILITY_CONTINUATION", new Object[0]);
            handleCreateAsContinuation(this.zidaneIntent);
        }
        AbilityDelegator.getInstance().runUnittest(this.zidaneIntent);
        AbilityDelegator.getInstance().matchAbility(this.ability, null);
    }

    private void handleCreateAsContinuation(Intent intent) {
        if (this.ability == null) {
            AppLog.e(SHELL_LABEL, "ability is null, continuation data fail", new Object[0]);
            return;
        }
        boolean isFlagExists = isFlagExists(96, intent.getFlags());
        boolean scheduleRestoreData = this.ability.scheduleRestoreData(intent.getParams(), isFlagExists, intent.getStringParam("originalDeviceId"));
        AppLog.d(SHELL_LABEL, "AbilityShellActivityDelegate::handleCreateAsContinuation restore result: %{public}b", Boolean.valueOf(scheduleRestoreData));
        if (scheduleRestoreData && isFlagExists) {
            AppLog.d(SHELL_LABEL, "AbilityShellActivityDelegate::handleCreateAsContinuation reversible", new Object[0]);
            this.continuationSchedulerSlave = new IReverseContinuationSchedulerSlave.ReverseContinuationSchedulerSlaveStub(this.continuationHandler, this.handler);
        }
        int intParam = intent.getIntParam(DMS_SESSION_ID, 0);
        String stringParam = intent.getStringParam(DMS_ORIGIN_DEVICE_ID);
        AppLog.d(SHELL_LABEL, "AbilityShellActivityDelegate::handleCreateAsContinuation sessionId: %{public}d", Integer.valueOf(intParam));
        long currentTimeMillis = System.currentTimeMillis();
        initDistSchedulerHost();
        try {
            this.distributedImpl.notifyCompleteContinuation(stringParam, intParam, scheduleRestoreData, this.continuationSchedulerSlave);
        } catch (RemoteException e) {
            AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::handleCreateAsContinuation RemoteException: %{public}s", e.getMessage());
        } catch (Throwable th) {
            JLogUtil.debugLog(JLogConstants.JLID_REMOTE_CONTINUE_ABILITY_END, this.abilityInfo.getBundleName(), this.abilityInfo.getClassName(), currentTimeMillis);
            throw th;
        }
        JLogUtil.debugLog(JLogConstants.JLID_REMOTE_CONTINUE_ABILITY_END, this.abilityInfo.getBundleName(), this.abilityInfo.getClassName(), currentTimeMillis);
    }

    private void initDistSchedulerHost() {
        if (this.distSchedulerHost == null) {
            this.distSchedulerHost = new ContinuationSchedulerForDmsStub(this.continuationHandler, this.handler);
        }
    }

    private boolean initAbilityTokenIfNeed() {
        if (this.continueToken != null) {
            return true;
        }
        Optional<IRemoteObject> remoteObjectFromContext = BinderConverter.getRemoteObjectFromContext((Context) this.abilityShell);
        if (!remoteObjectFromContext.isPresent()) {
            AppLog.w(SHELL_LABEL, "AbilityShellActivity::initAbilityTokenIfNeed get token fail.", new Object[0]);
            return false;
        }
        this.continueToken = remoteObjectFromContext.get();
        return true;
    }

    private boolean registerAbilityTokenIfNeed(IRemoteObject iRemoteObject) {
        if (this.tokenRegistered) {
            return true;
        }
        synchronized (this.tokenLock) {
            if (this.tokenRegistered) {
                return true;
            }
            boolean registerAbilityToken = registerAbilityToken(iRemoteObject);
            if (registerAbilityToken) {
                this.tokenRegistered = true;
            }
            return registerAbilityToken;
        }
    }

    private boolean registerAbilityToken(IRemoteObject iRemoteObject) {
        Integer num;
        try {
            initDistSchedulerHost();
            num = this.distributedImpl.registerAbilityToken(iRemoteObject, this.distSchedulerHost);
        } catch (RemoteException e) {
            AppLog.e(SHELL_LABEL, "AbilityShellDelegate::registerAbilityToken RemoteException: %{public}s", e.getMessage());
            num = null;
        }
        if (num != null) {
            checkDmsInterfaceResult(num.intValue(), "registerAbilityToken");
        }
        return num != null && num.intValue() == 0;
    }

    private void unregisterAbilityTokenIfNeed(IRemoteObject iRemoteObject) {
        if (this.tokenRegistered) {
            synchronized (this.tokenLock) {
                if (this.tokenRegistered) {
                    if (unregisterAbilityToken(iRemoteObject)) {
                        this.tokenRegistered = false;
                    }
                }
            }
        }
    }

    private boolean unregisterAbilityToken(IRemoteObject iRemoteObject) {
        Integer num;
        AppLog.i(SHELL_LABEL, "AbilityShellActivityDelegate::unregisterAbilityToken", new Object[0]);
        try {
            num = this.distributedImpl.unregisterAbilityToken(iRemoteObject, this.distSchedulerHost);
        } catch (RemoteException e) {
            AppLog.e(SHELL_LABEL, "AbilityShellDelegate::unregisterAbilityToken RemoteException: %{public}s", e.getMessage());
            num = null;
        }
        if (num != null) {
            checkDmsInterfaceResult(num.intValue(), "unregisterAbilityToken");
        }
        return num != null && num.intValue() == 0;
    }

    public void onPostCreate(Bundle bundle) {
        this.ability.schedulePostStart(PacMapUtils.convertFromBundle(bundle));
    }

    public void onStart() {
        HarmonyApplication.getInstance().waitForUserApplicationStart();
        HarmonyApplication.getInstance().setTopAbility(this.ability);
        scheduleAbilityLifecycle(this.zidaneIntent, LifecycleState.AbilityState.INACTIVE_STATE.getValue());
        AbilityDelegator.getInstance().matchAbility(this.ability, this.zidaneIntent);
    }

    public void onResume() {
        Optional<Intent> mapToHarmonyIntent = mapToHarmonyIntent(((Activity) this.abilityShell).getIntent());
        if (!mapToHarmonyIntent.isPresent()) {
            AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::onResume createZidaneIntent failed", new Object[0]);
            return;
        }
        this.zidaneIntent = mapToHarmonyIntent.get();
        HarmonyApplication.getInstance().setTopAbility(this.ability);
        scheduleAbilityLifecycle(this.zidaneIntent, LifecycleState.AbilityState.ACTIVE_STATE.getValue());
        AbilityDelegator.getInstance().matchAbilityWaiter(this.ability, this.zidaneIntent);
        AbilityDelegator.getInstance().matchAbility(this.ability, this.zidaneIntent);
    }

    public void onPostResume() {
        this.ability.schedulePostActive();
    }

    public void onPause() {
        unregisterAbilityTokenIfNeed(this.continueToken);
        this.continueToken = null;
        scheduleAbilityLifecycle(this.zidaneIntent, LifecycleState.AbilityState.INACTIVE_STATE.getValue());
        AbilityDelegator.getInstance().matchAbility(this.ability, this.zidaneIntent);
    }

    public void onStop() {
        if (this.ability.equals(HarmonyApplication.getInstance().getTopAbility())) {
            HarmonyApplication.getInstance().setTopAbility(null);
        }
        scheduleAbilityLifecycle(this.zidaneIntent, LifecycleState.AbilityState.BACKGROUND_STATE.getValue());
        AbilityDelegator.getInstance().matchAbility(this.ability, null);
    }

    public void onRestart() {
        scheduleAbilityLifecycle(this.zidaneIntent, LifecycleState.AbilityState.INACTIVE_STATE.getValue());
        AbilityDelegator.getInstance().matchAbility(this.ability, this.zidaneIntent);
    }

    public void onDestroy() {
        notifyTerminationToMasterIfNeed();
        HarmonyApplication instance = HarmonyApplication.getInstance();
        if (this.isLoadAsForm) {
            AppLog.i(SHELL_LABEL, "AbilityShellActivityDelegate::onDestroy FormType destroy", new Object[0]);
            instance.removeOrSubRef(this.abilityInfo.getClassName());
        }
        scheduleAbilityLifecycle(this.zidaneIntent, LifecycleState.AbilityState.INITIAL_STATE.getValue());
        instance.getApplication().removeAbilityRecord(this.abilityShell);
        AbilityDelegator.getInstance().matchAbility(this.ability, null);
    }

    private void notifyTerminationToMasterIfNeed() {
        if (this.continuationSchedulerSlave != null) {
            this.continuationHandler.notifyTerminationToMaster();
        }
    }

    public void onActivityResult(int i, int i2, android.content.Intent intent) {
        Intent intent2 = null;
        if (intent != null) {
            Optional<Intent> mapToHarmonyIntent = mapToHarmonyIntent(intent, null);
            if (mapToHarmonyIntent.isPresent()) {
                intent2 = mapToHarmonyIntent.get();
            }
        }
        this.ability.scheduleAbilityResult(i, i2, intent2);
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        Optional<MultimodalEvent> convertKeyEvent = AndroidEventProcessor.convertKeyEvent(keyEvent);
        if (!convertKeyEvent.isPresent()) {
            AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::convertKeyEventThenDispatch event invalid", new Object[0]);
            return false;
        }
        ohos.multimodalinput.event.KeyEvent keyEvent2 = (MultimodalEvent) convertKeyEvent.get();
        if (!(keyEvent2 instanceof ohos.multimodalinput.event.KeyEvent)) {
            AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::convertKeyEventThenDispatch not KeyEvent", new Object[0]);
            return false;
        }
        ohos.multimodalinput.event.KeyEvent keyEvent3 = keyEvent2;
        return this.ability.onKeyDown(keyEvent3.getKeyCode(), keyEvent3);
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        Optional<MultimodalEvent> convertKeyEvent = AndroidEventProcessor.convertKeyEvent(keyEvent);
        if (!convertKeyEvent.isPresent()) {
            AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::convertKeyEventThenDispatch event invalid", new Object[0]);
            return false;
        }
        ohos.multimodalinput.event.KeyEvent keyEvent2 = (MultimodalEvent) convertKeyEvent.get();
        if (!(keyEvent2 instanceof ohos.multimodalinput.event.KeyEvent)) {
            AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::convertKeyEventThenDispatch not KeyEvent", new Object[0]);
            return false;
        }
        ohos.multimodalinput.event.KeyEvent keyEvent3 = keyEvent2;
        return this.ability.onKeyUp(keyEvent3.getKeyCode(), keyEvent3);
    }

    public void onBackPressed() {
        AppLog.i(SHELL_LABEL, "AbilityShellActivityDelegate::onBackPressed", new Object[0]);
        this.ability.notifyBackKeyPressed();
    }

    public void onSaveInstanceState(Bundle bundle) {
        PacMap pacMap = new PacMap();
        this.ability.onSaveAbilityState(pacMap);
        bundle.putAll(PacMapUtils.convertIntoBundle(pacMap));
    }

    public void onRestoreInstanceState(Bundle bundle) {
        this.ability.onRestoreAbilityState(PacMapUtils.convertFromBundle(bundle));
    }

    public CharSequence onCreateDescription() {
        return this.ability.onNewDescription();
    }

    public void onUserLeaveHint() {
        this.ability.onLeaveForeground();
    }

    public void onUserInteraction() {
        this.ability.onEventDispatch();
    }

    public boolean convertTouchEventThenDispatch(MotionEvent motionEvent) {
        Optional<MultimodalEvent> convertTouchEvent = AndroidEventProcessor.convertTouchEvent(motionEvent);
        if (!convertTouchEvent.isPresent()) {
            AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::convertTouchEventThenDispatch event invalid", new Object[0]);
            return false;
        }
        TouchEvent touchEvent = (MultimodalEvent) convertTouchEvent.get();
        if (touchEvent instanceof TouchEvent) {
            return this.ability.dispatchTouchEvent(touchEvent);
        }
        if (touchEvent instanceof MouseEvent) {
            return this.ability.dispatchMouseEvent((MouseEvent) touchEvent);
        }
        if (touchEvent instanceof RotationEvent) {
            return this.ability.dispatchRotationEvent((RotationEvent) touchEvent);
        }
        AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::convertTouchEventThenDispatch not TouchEvent", new Object[0]);
        return false;
    }

    public boolean convertKeyEventThenDispatch(KeyEvent keyEvent) {
        Optional<MultimodalEvent> convertKeyEvent = AndroidEventProcessor.convertKeyEvent(keyEvent);
        if (!convertKeyEvent.isPresent()) {
            AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::convertKeyEventThenDispatch event invalid", new Object[0]);
            return false;
        }
        ohos.multimodalinput.event.KeyEvent keyEvent2 = (MultimodalEvent) convertKeyEvent.get();
        if (keyEvent2 instanceof ohos.multimodalinput.event.KeyEvent) {
            return this.ability.dispatchKeyBoardEvent(keyEvent2);
        }
        AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::convertKeyEventThenDispatch not KeyBoardEvent", new Object[0]);
        return false;
    }

    public void dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        AccessibilityEventInfo accessibilityEventInfo = new AccessibilityEventInfo();
        BarrierFreeInnerClient.fillBarrierFreeEventInfo(accessibilityEvent, accessibilityEventInfo);
        this.ability.dispatchAccessibilityEventInfo(accessibilityEventInfo);
        BarrierFreeInnerClient.fillAccessibilityEventInfo(this.contextDeal, accessibilityEventInfo, accessibilityEvent);
    }

    public void onConfigurationChanged(AbilityInfo.DisplayOrientation displayOrientation) {
        this.ability.dispatchOrientationChange(displayOrientation);
    }

    public void onWindowFocusChanged(boolean z) {
        this.ability.onWindowFocusChanged(z);
    }

    public void onNewIntent(android.content.Intent intent) {
        Optional<Intent> mapToHarmonyIntent = mapToHarmonyIntent(intent);
        if (!mapToHarmonyIntent.isPresent()) {
            AppLog.e(SHELL_LABEL, "AbilityShellActivityDelegate::onNewIntent createZidaneIntent failed", new Object[0]);
            return;
        }
        this.zidaneIntent = mapToHarmonyIntent.get();
        this.ability.scheduleNewIntent(this.zidaneIntent);
    }

    public void onTrimMemory(int i) {
        this.ability.onMemoryLevel(i);
    }

    /* access modifiers changed from: package-private */
    public boolean handleContinueAbility(boolean z, String str) {
        if (!initAbilityTokenIfNeed() || !registerAbilityTokenIfNeed(this.continueToken)) {
            return false;
        }
        this.continuationHandler.setReversible(z);
        return continueAbility(this.abilityShell, this.continueToken, str);
    }

    /* access modifiers changed from: package-private */
    public boolean handleReverseContinueAbility() {
        return this.continuationHandler.reverseContinueAbility();
    }

    public void onRequestPermissionsFromUserResult(int i, String[] strArr, int[] iArr) {
        if (!(this.abilityShell instanceof AbilityShellActivity)) {
            AppLog.e(SHELL_LABEL, "AbilityShellDelegate::onRequestPermissionsFromUserResult can't be called,ability is not instance of AbilityShellActivity", new Object[0]);
            return;
        }
        String[] fetchRequestPermissions = PermissionConversion.fetchRequestPermissions(i);
        if (!checkGrantedResult(fetchRequestPermissions, strArr, iArr)) {
            AppLog.e(SHELL_LABEL, "AbilityShellDelegate::onRequestPermissionsFromUserResult can't be called,checkGrantedResult failed", new Object[0]);
            return;
        }
        int length = fetchRequestPermissions.length;
        int[] iArr2 = new int[length];
        for (int i2 = 0; i2 < length; i2++) {
            int i3 = 0;
            while (true) {
                if (i3 >= strArr.length) {
                    break;
                } else if (PermissionConversion.getAosPermissionNameIfPossible(fetchRequestPermissions[i2]).equals(strArr[i3])) {
                    iArr2[i2] = iArr[i3];
                    break;
                } else {
                    iArr2[i2] = 0;
                    i3++;
                }
            }
        }
        this.ability.onRequestPermissionsFromUserResult(i, fetchRequestPermissions, iArr2);
    }

    private void handleLoadAbility() {
        HarmonyApplication instance = HarmonyApplication.getInstance();
        if (this.abilityInfo.getFormEnabled()) {
            AppLog.d(SHELL_LABEL, "AbilityShellActivityDelegate::onCreate isFormType", new Object[0]);
            this.isLoadAsForm = true;
            FormAbility formAbility = instance.getFormAbility(this.abilityInfo.getClassName());
            if (formAbility != null) {
                formAbility.addRefCount();
                this.ability = formAbility.getAbility();
                this.ability.setAbilityShell(this.abilityShell);
                this.ability.init((ohos.app.Context) this.contextDeal, this.abilityInfo);
                return;
            }
            this.ability = new AbilityLoader().setAbilityInfo(this.abilityInfo).setContext(this.contextDeal).setAbilityShell(this.abilityShell).loadAbility();
            FormAbility formAbility2 = new FormAbility(this.ability);
            instance.addFormAbility(this.abilityInfo.getClassName(), formAbility2);
            formAbility2.addRefCount();
            return;
        }
        AppLog.d(SHELL_LABEL, "AbilityShellActivityDelegate::onCreate normal ability", new Object[0]);
        loadAbility(this.abilityInfo, this.contextDeal, this.abilityShell);
        if (this.ability instanceof IAbilityContinuation) {
            this.continuationHandler = new ContinuationHandler(this.ability, this.distributedImpl);
            ContinuationHandler continuationHandler2 = this.continuationHandler;
            continuationHandler2.setMasterStub(new IReverseContinuationSchedulerMaster.ReverseContinuationSchedulerMasterStub(continuationHandler2, this.handler));
            this.continuationHandler.setAbilityInfo(this.abilityInfo);
            this.continuationHandler.setParamsClassLoader(HarmonyApplication.getInstance().getClassLoader());
        }
    }

    private boolean checkGrantedResult(String[] strArr, String[] strArr2, int[] iArr) {
        if (strArr == null || strArr.length == 0) {
            AppLog.w(SHELL_LABEL, "AbilityShellDelegate::checkGrantedResult zPermissions is empty", new Object[0]);
            return false;
        } else if (strArr2 == null) {
            AppLog.w(SHELL_LABEL, "AbilityShellDelegate::checkGrantedResult permissions is null", new Object[0]);
            return false;
        } else if (iArr == null) {
            AppLog.w(SHELL_LABEL, "AbilityShellDelegate::checkGrantedResult grantResults is null", new Object[0]);
            return false;
        } else {
            int length = strArr2.length;
            if (length != 0 && length == iArr.length) {
                return true;
            }
            AppLog.w(SHELL_LABEL, "AbilityShellDelegate::checkGrantedResult permissions is empty or doesn't match the grantResults", new Object[0]);
            return false;
        }
    }

    private ContextDeal createActivityContextDeal(AbilityInfo abilityInfo) {
        Context context = (Context) this.abilityShell;
        ContextDeal contextDeal2 = new ContextDeal(context, context.getClassLoader());
        contextDeal2.setAbilityInfo(abilityInfo);
        contextDeal2.setHapModuleInfo(HarmonyApplication.getInstance().getHapModuleInfoByAbilityInfo(this.abilityInfo));
        contextDeal2.setApplication(HarmonyApplication.getInstance().getApplication());
        contextDeal2.setMainLooper(new AndroidTaskLooper(Looper.getMainLooper()));
        HarmonyApplication.getInstance().getApplication().addAbilityRecord(this.abilityShell, contextDeal2);
        return contextDeal2;
    }

    private boolean continueAbility(Object obj, final IRemoteObject iRemoteObject, String str) {
        if (!StringUtils.isEmpty(str)) {
            return continueAbilityInnerWithSpecifiedId(iRemoteObject, str);
        }
        List<AbilityShellData> fetchRemoteAbilities = fetchRemoteAbilities();
        if (fetchRemoteAbilities == null || fetchRemoteAbilities.isEmpty()) {
            AppLog.w(SHELL_LABEL, "AbilityShellDelegate::continueAbility fetchAbilities failed!", new Object[0]);
            return false;
        } else if (fetchRemoteAbilities.size() == 1) {
            AppLog.d(SHELL_LABEL, "AbilityShellDelegate::continueAbility only one ability, don't show dialog!", new Object[0]);
            return continueAbilityInner(iRemoteObject, fetchRemoteAbilities.get(0).getAbilityInfo().getDeviceId());
        } else {
            new AbilityResolver((Context) obj, fetchRemoteAbilities, new AbilityResolver.IResolveResult() {
                /* class ohos.abilityshell.AbilityShellActivityDelegate.AnonymousClass1 */

                @Override // ohos.abilityshell.utils.AbilityResolver.IResolveResult
                public void onResolveResult(AbilityShellData abilityShellData) {
                    AbilityShellActivityDelegate.this.ability.onSubmitContinuationRequestResult(abilityShellData != null ? AbilityShellActivityDelegate.this.continueAbilityInner(iRemoteObject, abilityShellData.getAbilityInfo().getDeviceId()) : false);
                }
            }).show();
            return true;
        }
    }

    private List<AbilityShellData> fetchRemoteAbilities() {
        Intent intent = new Intent();
        intent.setElement(new ElementName(null, this.abilityInfo.getBundleName(), this.abilityInfo.getClassName()));
        intent.setFlags(256);
        List<AbilityShellData> fetchAbilities = SelectAbilityUtils.fetchAbilities(null, intent);
        if (fetchAbilities == null || fetchAbilities.isEmpty()) {
            AppLog.e(SHELL_LABEL, "AbilityShellDelegate::fetchRemoteAbilities fetchAbilities failed", new Object[0]);
            return fetchAbilities;
        }
        Iterator<AbilityShellData> it = fetchAbilities.iterator();
        while (it.hasNext()) {
            AbilityShellData next = it.next();
            AppLog.d(SHELL_LABEL, "AbilityShellDelegate::fetchRemoteAbilities data id is %{private}s", next.getAbilityInfo().getDeviceId());
            if (next.getLocal()) {
                AppLog.d(SHELL_LABEL, "AbilityShellDelegate::fetchRemoteAbilities remove local ability data.", new Object[0]);
                it.remove();
            }
        }
        return fetchAbilities;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean continueAbilityInner(IRemoteObject iRemoteObject, String str) {
        HiTraceId hiTraceBegin = HiTraceUtil.hiTraceBegin("continueAbility");
        Integer num = null;
        try {
            AppLog.d(SHELL_LABEL, "AbilityShellDelegate::continueAbilityInner deviceId %{private}s", str);
            Intent intent = new Intent();
            intent.setElement(new ElementName(null, this.abilityInfo.getBundleName(), this.abilityInfo.getClassName()));
            num = this.distributedImpl.continueAbility(iRemoteObject, str, intent);
            AppLog.i(SHELL_LABEL, "AbilityShellDelegate::continueAbility result %{public}d", num);
        } catch (RemoteException e) {
            AppLog.e(SHELL_LABEL, "AbilityShellDelegate::continueAbility RemoteException: %{public}s", e.getMessage());
        }
        HiTrace.end(hiTraceBegin);
        return num != null && num.intValue() == 0;
    }

    private boolean continueAbilityInnerWithSpecifiedId(IRemoteObject iRemoteObject, String str) {
        HiTraceId hiTraceBegin = HiTraceUtil.hiTraceBegin("continueAbilityInnerWithSpecifiedId");
        Integer num = null;
        try {
            AppLog.d(SHELL_LABEL, "AbilityShellDelegate::continueAbilityInnerWithSpecifiedId deviceId %{private}s", str);
            Intent intent = new Intent();
            intent.setElement(new ElementName(str, this.abilityInfo.getBundleName(), this.abilityInfo.getClassName()));
            num = this.distributedImpl.continueAbility(iRemoteObject, "", intent);
            AppLog.i(SHELL_LABEL, "AbilityShellDelegate::continueAbilityInnerWithSpecifiedId result %{public}d", num);
        } catch (RemoteException e) {
            AppLog.e(SHELL_LABEL, "AbilityShellDelegate::continueAbilityInnerWithSpecifiedId RemoteException: %{public}s", e.getMessage());
        }
        HiTrace.end(hiTraceBegin);
        return num != null && num.intValue() == 0;
    }
}
