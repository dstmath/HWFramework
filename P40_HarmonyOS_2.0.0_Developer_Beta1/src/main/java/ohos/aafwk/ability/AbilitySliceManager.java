package ohos.aafwk.ability;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Optional;
import ohos.aafwk.ability.startsetting.AbilityStartSetting;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.aafwk.utils.log.KeyLog;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.agp.window.service.WindowManager;
import ohos.app.AbilityContext;
import ohos.app.Context;
import ohos.bundle.AbilityInfo;
import ohos.ivicommon.drivingsafety.DrivingSafetyClient;
import ohos.ivicommon.drivingsafety.model.ControlItemEnum;
import ohos.ivicommon.drivingsafety.model.Position;

public final class AbilitySliceManager implements IAbilityLifecycleCallback {
    private static final LogLabel LABEL = LogLabel.create();
    static final int MAX_NUM_OF_PENDING_SLICE_RESULTS = 65534;
    private static final String NULL_ABILITY_LOG = "Ability is null";
    static final int REQUEST_SLICE_INDEX_MARK = -65536;
    static final int REQUEST_SLICE_INDEX_SHIFT = 16;
    private Ability ability = null;
    private AbilitySliceAnimator abilitySliceAnimator = new AbilitySliceAnimator();
    private AbilitySliceRoute abilitySliceRoute = null;
    private AbilitySliceScheduler abilitySliceScheduler = null;
    private AbilitySliceTransitionController abilitySliceTransitionController = null;
    private ConnectionScheduler connectionScheduler = null;
    private HashMap<Integer, AbilitySlice> pendingSliceResults = new HashMap<>();
    private int requestSliceIndex = 0;
    private boolean windowFocused = false;

    private static int getRequestCode(int i) {
        return i & 65535;
    }

    private static int getSliceIndex(int i) {
        return (i & -65536) >> 16;
    }

    private static int transferRequestCode(int i, int i2) {
        return (i << 16) + (i2 & 65535);
    }

    /* access modifiers changed from: package-private */
    public void init(Ability ability2) {
        if (ability2 != null) {
            this.ability = ability2;
            this.abilitySliceRoute = new AbilitySliceRoute();
            this.abilitySliceTransitionController = new AbilitySliceTransitionController();
            this.abilitySliceScheduler = new AbilitySliceScheduler();
            try {
                this.abilitySliceScheduler.attach(this);
            } catch (IllegalArgumentException e) {
                Log.error(LABEL, "Attach abilitySliceScheduler failed: %{public}s.", e);
            }
            this.ability.registerAbilityLifecycleCallback(this);
            this.connectionScheduler = ability2.getConnectionScheduler();
            Log.debug(LABEL, "AbilitySliceManager init success.", new Object[0]);
            return;
        }
        throw new IllegalArgumentException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: package-private */
    public void startAbility(Intent intent) {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            ability2.startAbility(intent, AbilityStartSetting.getEmptySetting());
            return;
        }
        throw new IllegalStateException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: package-private */
    public void startAbility(Intent intent, AbilityStartSetting abilityStartSetting) {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            ability2.startAbility(intent, abilityStartSetting);
            return;
        }
        throw new IllegalStateException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: package-private */
    public void startAbilityForResult(AbilitySlice abilitySlice, Intent intent, int i, AbilityStartSetting abilityStartSetting) {
        if (this.ability != null) {
            int transferRequestCode = transferRequestCode(allocateRequestSliceIndex(abilitySlice), i);
            KeyLog.infoBound("[%{public}s][%{public}s][%{public}s]: element: %{public}s, transferCode: %{public}d", LABEL.getTag(), KeyLog.START_ABILITY_FORRESULT, KeyLog.LogState.START, Optional.ofNullable(intent).map($$Lambda$5NIH3kVWNfBSHcIt4qMC1aQ8cu0.INSTANCE).map($$Lambda$Gbq1Su8pWvMr5cS7vkDL3qT3QMg.INSTANCE).orElse("null"), Integer.valueOf(transferRequestCode));
            this.ability.startAbilityForResult(intent, transferRequestCode, abilityStartSetting);
            KeyLog.infoBound(KeyLog.KEYLOG_FMT_ARGS, LABEL.getTag(), KeyLog.START_ABILITY_FORRESULT, KeyLog.LogState.END);
            return;
        }
        throw new IllegalStateException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: package-private */
    public void startAbilityForResult(AbilitySlice abilitySlice, Intent intent, int i) {
        startAbilityForResult(abilitySlice, intent, i, AbilityStartSetting.getEmptySetting());
    }

    /* access modifiers changed from: package-private */
    public boolean stopAbility(Intent intent) {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            return ability2.stopAbility(intent);
        }
        throw new IllegalStateException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: package-private */
    public void terminateAbility() {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            ability2.terminateAbility();
            return;
        }
        throw new IllegalStateException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: package-private */
    public void present(AbilitySlice abilitySlice, AbilitySlice abilitySlice2, Intent intent) {
        this.abilitySliceScheduler.addAbilitySlice(abilitySlice, abilitySlice2, intent, false);
    }

    public void presentSync(AbilitySlice abilitySlice, AbilitySlice abilitySlice2, Intent intent) {
        this.abilitySliceScheduler.addAbilitySlice(abilitySlice, abilitySlice2, intent, true);
    }

    /* access modifiers changed from: package-private */
    public void presentForResult(AbilitySlice abilitySlice, AbilitySlice abilitySlice2, Intent intent, int i) {
        this.abilitySliceScheduler.addAbilitySliceForResult(abilitySlice, abilitySlice2, intent, i);
    }

    /* access modifiers changed from: package-private */
    public void terminate(AbilitySlice abilitySlice, Intent intent) {
        this.abilitySliceScheduler.removeAbilitySlice(abilitySlice, intent, false);
    }

    public void terminateSync(AbilitySlice abilitySlice, Intent intent) {
        this.abilitySliceScheduler.removeAbilitySlice(abilitySlice, intent, true);
    }

    /* access modifiers changed from: package-private */
    public AbilitySliceRoute getAbilitySliceRoute() {
        return this.abilitySliceRoute;
    }

    /* access modifiers changed from: package-private */
    public Context getContext() {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            return ability2.getContext();
        }
        throw new IllegalStateException("Ability is null, getContext failed.");
    }

    /* access modifiers changed from: package-private */
    public Object getAbilityShell() {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            return ability2.getAbilityShell();
        }
        throw new IllegalStateException("Ability is null, getAbilityShell failed.");
    }

    /* access modifiers changed from: package-private */
    public int getAbilityState() {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            return ability2.getCurrentState();
        }
        throw new IllegalStateException("Ability is null, getAbilityState failed.");
    }

    /* access modifiers changed from: package-private */
    public ClassLoader getClassLoader() {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            return ability2.getClassloader();
        }
        throw new IllegalStateException("Ability is null, getClassLoader failed.");
    }

    @Override // ohos.aafwk.ability.IAbilityLifecycleCallback
    public void onAbilityStart(Intent intent) throws IllegalStateException, IllegalArgumentException {
        if (!checkIsDrivingSafety(this.ability)) {
            AbilityShellUtils.showDrivingSafetyTips(this.ability);
        } else {
            this.abilitySliceScheduler.handleStartAbilitySlice(intent);
        }
    }

    private boolean checkIsDrivingSafety(Context context) {
        WindowManager.LayoutConfig layoutParams = this.ability.getWindowProxy().getLayoutParams();
        if (DrivingSafetyClient.isDrivingSafety(context, (ControlItemEnum) null, layoutParams != null ? new Position(layoutParams.x, layoutParams.y, layoutParams.height, layoutParams.width) : null)) {
            return true;
        }
        Log.info(LABEL, "isDrivingSafety Currently not in safe driving mode", new Object[0]);
        return false;
    }

    @Override // ohos.aafwk.ability.IAbilityLifecycleCallback
    public void onAbilityStop() {
        try {
            this.abilitySliceScheduler.handleStopAbilitySlice();
        } catch (IllegalStateException e) {
            Log.error(LABEL, "AS BACKGROUND to INACTIVE failed.", new Object[0]);
            throw e;
        }
    }

    @Override // ohos.aafwk.ability.IAbilityLifecycleCallback
    public void onAbilityActive(Intent intent) {
        if (!checkIsDrivingSafety(this.ability)) {
            AbilityShellUtils.showDrivingSafetyTips(this.ability);
            return;
        }
        try {
            this.abilitySliceScheduler.handleActiveAbilitySlice(intent);
        } catch (IllegalStateException e) {
            Log.error(LABEL, "AS INACTIVE to ACTIVE failed.", new Object[0]);
            throw e;
        }
    }

    @Override // ohos.aafwk.ability.IAbilityLifecycleCallback
    public void onAbilityInactive() {
        try {
            this.abilitySliceScheduler.handleInactiveAbilitySlice();
        } catch (IllegalStateException e) {
            Log.error(LABEL, "AS ACTIVE to INACTIVE failed.", new Object[0]);
            throw e;
        }
    }

    @Override // ohos.aafwk.ability.IAbilityLifecycleCallback
    public void onAbilityForeground(Intent intent) {
        try {
            this.abilitySliceScheduler.handleMoveAbilitySliceToForeground(intent);
        } catch (IllegalStateException e) {
            Log.error(LABEL, "AS BACKGROUND to INACTIVE failed.", new Object[0]);
            throw e;
        }
    }

    @Override // ohos.aafwk.ability.IAbilityLifecycleCallback
    public void onAbilityBackground() {
        try {
            this.abilitySliceScheduler.handleMoveAbilitySliceToBackground();
        } catch (IllegalStateException e) {
            Log.error(LABEL, "AS INACTIVE to BACKGROUND failed.", new Object[0]);
            throw e;
        }
    }

    public void onAbilityNewIntent(Intent intent) {
        try {
            this.abilitySliceScheduler.handleAbilityNewIntent(intent);
        } catch (IllegalStateException e) {
            Log.error(LABEL, "AS Intent failed.", new Object[0]);
            throw e;
        }
    }

    public void onFocusChange(boolean z) {
        this.windowFocused = z;
        Log.info(LABEL, "Window focus change to %{public}b", Boolean.valueOf(this.windowFocused));
    }

    /* access modifiers changed from: package-private */
    public boolean connectAbility(AbilityContext abilityContext, Intent intent, IAbilityConnection iAbilityConnection) throws IllegalStateException, IllegalArgumentException {
        ConnectionScheduler connectionScheduler2;
        if (this.ability != null && (connectionScheduler2 = this.connectionScheduler) != null) {
            return connectionScheduler2.openServiceConnection(abilityContext, intent, iAbilityConnection);
        }
        Log.error(LABEL, "ability or connectionScheduler is null, connect ability failed.", new Object[0]);
        throw new IllegalStateException("ability or connectionScheduler is null, connect ability failed.");
    }

    /* access modifiers changed from: package-private */
    public void disconnectAbility(AbilityContext abilityContext, IAbilityConnection iAbilityConnection) throws IllegalStateException {
        ConnectionScheduler connectionScheduler2 = this.connectionScheduler;
        if (connectionScheduler2 != null) {
            connectionScheduler2.closeServiceConnection(abilityContext, iAbilityConnection);
        } else {
            Log.error(LABEL, "connectionScheduler is null, disconnect ability failed.", new Object[0]);
            throw new IllegalStateException("connectionScheduler is null, disconnect ability failed.");
        }
    }

    /* access modifiers changed from: package-private */
    public void dumpRoute(String str, PrintWriter printWriter) {
        this.abilitySliceRoute.dumpSliceRoute(str, printWriter);
    }

    /* access modifiers changed from: package-private */
    public void dumpSlice(String str, PrintWriter printWriter, String[] strArr) {
        this.abilitySliceScheduler.dumpSliceStack(str, printWriter, strArr);
    }

    /* access modifiers changed from: package-private */
    public void dumpServiceList(String str, PrintWriter printWriter, AbilityContext abilityContext) {
        ConnectionScheduler connectionScheduler2 = this.connectionScheduler;
        if (connectionScheduler2 == null) {
            Log.error(LABEL, "connectionScheduler is null, dump service list failed.", new Object[0]);
            printWriter.println(str + "none");
            return;
        }
        connectionScheduler2.dumpServiceList(str, printWriter, abilityContext);
    }

    /* access modifiers changed from: package-private */
    public void continueAbility(String str) throws IllegalStateException {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            ability2.continueAbility(str);
            return;
        }
        throw new IllegalStateException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: package-private */
    public void continueAbilityReversibly(String str) throws IllegalStateException {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            ability2.continueAbilityReversibly(str);
            return;
        }
        throw new IllegalStateException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: package-private */
    public boolean reverseContinueAbility() throws IllegalStateException {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            return ability2.reverseContinueAbility();
        }
        throw new IllegalStateException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: package-private */
    public String getOriginalDeviceId() {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            return ability2.getOriginalDeviceId();
        }
        throw new IllegalStateException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: package-private */
    public ContinuationState getContinuationState() {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            return ability2.getContinuationState();
        }
        throw new IllegalStateException(NULL_ABILITY_LOG);
    }

    /* access modifiers changed from: package-private */
    public boolean onStartContinuation() {
        return this.abilitySliceScheduler.onStartContinuation();
    }

    /* access modifiers changed from: package-private */
    public boolean onSaveData(IntentParams intentParams) {
        return this.abilitySliceScheduler.onSaveData(intentParams);
    }

    /* access modifiers changed from: package-private */
    public boolean onRestoreData(IntentParams intentParams) {
        return this.abilitySliceScheduler.onRestoreData(intentParams);
    }

    /* access modifiers changed from: package-private */
    public void onCompleteContinuation(int i) {
        this.abilitySliceScheduler.onCompleteContinuation(i);
    }

    /* access modifiers changed from: package-private */
    public void onRemoteTerminated() {
        this.abilitySliceScheduler.onRemoteTerminated();
    }

    /* access modifiers changed from: package-private */
    public boolean clearAndRestore(IntentParams intentParams) {
        return this.abilitySliceScheduler.clearAndRestore(intentParams);
    }

    /* access modifiers changed from: package-private */
    public AbilityWindow getWindowProxy() {
        Ability ability2 = this.ability;
        if (ability2 != null) {
            return ability2.getWindowProxy();
        }
        return null;
    }

    private int allocateRequestSliceIndex(AbilitySlice abilitySlice) {
        if (this.pendingSliceResults.size() < MAX_NUM_OF_PENDING_SLICE_RESULTS) {
            while (this.pendingSliceResults.containsKey(Integer.valueOf(this.requestSliceIndex))) {
                this.requestSliceIndex = (this.requestSliceIndex + 1) % MAX_NUM_OF_PENDING_SLICE_RESULTS;
            }
            int i = this.requestSliceIndex;
            this.pendingSliceResults.put(Integer.valueOf(i), abilitySlice);
            Log.debug(LABEL, "put sliceindex %{public}d to map", Integer.valueOf(i));
            this.requestSliceIndex = (this.requestSliceIndex + 1) % MAX_NUM_OF_PENDING_SLICE_RESULTS;
            return i;
        }
        throw new IllegalStateException("Too many pending slice results");
    }

    public void onAbilityResult(int i, int i2, Intent intent) {
        int sliceIndex = getSliceIndex(i);
        AbilitySlice abilitySlice = this.pendingSliceResults.get(Integer.valueOf(sliceIndex));
        this.pendingSliceResults.remove(Integer.valueOf(sliceIndex));
        Log.debug(LABEL, "remove sliceindex %{public}d from map", Integer.valueOf(sliceIndex));
        if (abilitySlice != null) {
            abilitySlice.onAbilityResult(getRequestCode(i), i2, intent);
        }
    }

    /* access modifiers changed from: package-private */
    public void setAbilitySliceAnimator(AbilitySliceAnimator abilitySliceAnimator2) {
        this.abilitySliceAnimator = abilitySliceAnimator2;
    }

    /* access modifiers changed from: package-private */
    public AbilitySliceTransitionController getTransitionController() {
        return this.abilitySliceTransitionController;
    }

    /* access modifiers changed from: package-private */
    public AbilitySliceAnimator getAbilitySliceAnimator() {
        return this.abilitySliceAnimator;
    }

    /* access modifiers changed from: package-private */
    public boolean notifyBackKeyPressed() {
        AbilitySliceScheduler abilitySliceScheduler2 = this.abilitySliceScheduler;
        if (abilitySliceScheduler2 != null) {
            return abilitySliceScheduler2.notifyBackKeyPressed();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void dispatchOrientationChange(AbilityInfo.DisplayOrientation displayOrientation) {
        AbilitySliceScheduler abilitySliceScheduler2 = this.abilitySliceScheduler;
        if (abilitySliceScheduler2 != null) {
            abilitySliceScheduler2.notifyOrientationChange(displayOrientation);
        }
    }

    /* access modifiers changed from: package-private */
    public AbilitySlice getTopAbilitySlice() {
        return this.abilitySliceScheduler.getTopAbilitySlice();
    }

    /* access modifiers changed from: package-private */
    public Ability getAbility() {
        return this.ability;
    }
}
