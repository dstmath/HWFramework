package ohos.aafwk.ability;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import ohos.aafwk.ability.ContinuationManager;
import ohos.aafwk.content.IntentParams;
import ohos.aafwk.utils.log.Log;
import ohos.aafwk.utils.log.LogLabel;
import ohos.bundle.AbilityInfo;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.devtools.JLog;
import ohos.devtools.JLogConstants;
import ohos.global.icu.text.DateFormat;
import ohos.tools.Bytrace;

/* access modifiers changed from: package-private */
public final class ContinuationManager {
    private static final LogLabel LABEL = LogLabel.create();
    private static final int TIMEOUT_MS_WAIT_DMS_NOTIFY_CONTINUATION_COMPLETE = 6000;
    private static final int TIMEOUT_MS_WAIT_DMS_SCHEDULE_START_CONTINUATION = 5000;
    private static final int TIMEOUT_MS_WAIT_REMOTE_NOTIFY_BACK = 6000;
    private Ability ability;
    private IAbilityContinuation continuableAbility;
    private ContinuationState continuationState;
    private volatile Handler mainHandler;
    private String originalDeviceId;
    private ProgressState progressState;
    private boolean reversible;

    /* access modifiers changed from: private */
    public enum ProgressState {
        INITIAL,
        WAITING_SCHEDULE,
        IN_PROGRESS
    }

    ContinuationManager(Ability ability2) {
        if (ability2 instanceof IAbilityContinuation) {
            this.ability = ability2;
            this.continuableAbility = (IAbilityContinuation) ability2;
            this.progressState = ProgressState.INITIAL;
            this.continuationState = ContinuationState.LOCAL_RUNNING;
            return;
        }
        Log.warn(LABEL, "Ability not available to Continuation.", new Object[0]);
        throw new IllegalArgumentException("Ability is not an IAbilityContinuation.");
    }

    /* access modifiers changed from: package-private */
    public void continueAbility(boolean z, String str) throws IllegalStateException {
        Log.debug(LABEL, "continueAbility start", new Object[0]);
        if (checkContinuationIllegal()) {
            throw new IllegalStateException("Ability not available to continueAbility.");
        } else if (this.progressState != ProgressState.INITIAL) {
            throw new IllegalStateException("Another request in progress.");
        } else if (this.continuationState == ContinuationState.LOCAL_RUNNING) {
            long currentTimeMillis = System.currentTimeMillis();
            if (AbilityShellUtils.continueAbility(this.ability, z, str)) {
                this.reversible = z;
                changeProcessState(ProgressState.WAITING_SCHEDULE);
                restoreStateWhenTimeout(5000, ProgressState.WAITING_SCHEDULE);
            }
            debugLog(this.ability.getAbilityInfo(), currentTimeMillis);
        } else {
            throw new IllegalStateException("Illegal continuation state. Current state is " + this.continuationState);
        }
    }

    private void initMainHandlerIfNeed() {
        if (this.mainHandler == null) {
            Log.debug(LABEL, "Try to init main handler", new Object[0]);
            synchronized (this) {
                if (this.mainHandler == null) {
                    this.mainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
    }

    private void restoreStateWhenTimeout(long j, ProgressState progressState2) {
        initMainHandlerIfNeed();
        this.mainHandler.post(new Runnable(j, progressState2) {
            /* class ohos.aafwk.ability.$$Lambda$ContinuationManager$3a8T0mArNxgbekFckwbjcNOSNDE */
            private final /* synthetic */ long f$1;
            private final /* synthetic */ ContinuationManager.ProgressState f$2;

            {
                this.f$1 = r2;
                this.f$2 = r4;
            }

            @Override // java.lang.Runnable
            public final void run() {
                ContinuationManager.this.lambda$restoreStateWhenTimeout$0$ContinuationManager(this.f$1, this.f$2);
            }
        });
    }

    public /* synthetic */ void lambda$restoreStateWhenTimeout$0$ContinuationManager(long j, final ProgressState progressState2) {
        new CountDownTimer(j, j) {
            /* class ohos.aafwk.ability.ContinuationManager.AnonymousClass1 */

            @Override // android.os.CountDownTimer
            public void onTick(long j) {
            }

            @Override // android.os.CountDownTimer
            public void onFinish() {
                Log.debug(ContinuationManager.LABEL, "Continuation state checkpoint. unexpected[%{public}s] current[%{public}s]", progressState2, ContinuationManager.this.progressState);
                if (ContinuationManager.this.progressState == progressState2) {
                    Log.warn(ContinuationManager.LABEL, "Wait state change timeout. Restore state.", new Object[0]);
                    ContinuationManager.this.changeProcessState(ProgressState.INITIAL);
                }
            }
        }.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void changeProcessState(ProgressState progressState2) {
        Log.debug(LABEL, "changeProcessState: %{public}s -> %{public}s", this.progressState, progressState2);
        this.progressState = progressState2;
    }

    /* access modifiers changed from: package-private */
    public void onSubmitContinuationRequestResult(boolean z) {
        Log.info(LABEL, "onSubmitContinuationRequestResult: submitSuccess=%{public}b", Boolean.valueOf(z));
        if (!z) {
            changeProcessState(ProgressState.INITIAL);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean startContinuation() {
        Log.debug(LABEL, "startContinuation: Start", new Object[0]);
        changeProcessState(ProgressState.IN_PROGRESS);
        boolean doScheduleStartContinuation = doScheduleStartContinuation();
        if (!doScheduleStartContinuation) {
            changeProcessState(ProgressState.INITIAL);
        }
        return doScheduleStartContinuation;
    }

    private boolean doScheduleStartContinuation() {
        if (checkContinuationIllegal()) {
            Log.warn(LABEL, "Ability not available to startContinuation.", new Object[0]);
            return false;
        }
        Bytrace.startTrace(2147483648L, "onStartContinuation");
        if (!this.continuableAbility.onStartContinuation()) {
            Bytrace.finishTrace(2147483648L, "onStartContinuation");
            return false;
        }
        Bytrace.finishTrace(2147483648L, "onStartContinuation");
        return this.ability.getAbilitySliceManager().onStartContinuation();
    }

    /* access modifiers changed from: package-private */
    public boolean saveData(IntentParams intentParams) {
        Log.debug(LABEL, "saveData: Start", new Object[0]);
        boolean doScheduleSaveData = doScheduleSaveData(intentParams);
        if (!doScheduleSaveData) {
            changeProcessState(ProgressState.INITIAL);
        } else {
            restoreStateWhenTimeout(6000, ProgressState.IN_PROGRESS);
        }
        return doScheduleSaveData;
    }

    private boolean doScheduleSaveData(IntentParams intentParams) {
        if (intentParams == null) {
            Log.error(LABEL, "Input saveData is null.", new Object[0]);
            return false;
        } else if (checkContinuationIllegal()) {
            Log.warn(LABEL, "Ability not available to save data.", new Object[0]);
            return false;
        } else {
            IntentParams intentParams2 = new IntentParams();
            Bytrace.startTrace(2147483648L, "onSaveData");
            boolean onSaveData = this.continuableAbility.onSaveData(intentParams2);
            Bytrace.finishTrace(2147483648L, "onSaveData");
            StringBuilder sb = new StringBuilder();
            String name = getClass().getName();
            for (String str : intentParams2.keySet()) {
                sb.setLength(0);
                sb.append(str);
                sb.append(name);
                intentParams.setParam(sb.toString(), intentParams2.getParam(str));
            }
            if (onSaveData) {
                return this.ability.getAbilitySliceManager().onSaveData(intentParams);
            }
            Log.warn(LABEL, "Ability save data failed.", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean restoreData(IntentParams intentParams, boolean z, String str) {
        Log.debug(LABEL, "restoreData: Start", new Object[0]);
        changeProcessState(ProgressState.IN_PROGRESS);
        boolean doScheduleRestoreData = doScheduleRestoreData(intentParams);
        if (z) {
            this.continuationState = ContinuationState.REPLICA_RUNNING;
        }
        this.originalDeviceId = str;
        changeProcessState(ProgressState.INITIAL);
        return doScheduleRestoreData;
    }

    private boolean doScheduleRestoreData(IntentParams intentParams) {
        if (intentParams == null) {
            Log.error(LABEL, "Input restoreData is null.", new Object[0]);
            return false;
        } else if (checkContinuationIllegal()) {
            Log.warn(LABEL, "Ability not available to restore data.", new Object[0]);
            return false;
        } else {
            IntentParams intentParams2 = new IntentParams();
            String name = getClass().getName();
            for (String str : intentParams.keySet()) {
                if (str.endsWith(name)) {
                    intentParams2.setParam(str.substring(0, str.length() - name.length()), intentParams.getParam(str));
                }
            }
            Bytrace.startTrace(2147483648L, "onRestoreData");
            if (!this.continuableAbility.onRestoreData(intentParams2)) {
                Log.warn(LABEL, "Ability restore data failed.", new Object[0]);
                Bytrace.finishTrace(2147483648L, "onRestoreData");
                return false;
            }
            Bytrace.finishTrace(2147483648L, "onRestoreData");
            return this.ability.getAbilitySliceManager().onRestoreData(intentParams);
        }
    }

    /* access modifiers changed from: package-private */
    public String getOriginalDeviceId() {
        return this.continuationState == ContinuationState.REPLICA_RUNNING ? this.originalDeviceId : "";
    }

    /* access modifiers changed from: package-private */
    public ContinuationState getContinuationState() {
        return this.continuationState;
    }

    /* access modifiers changed from: package-private */
    public void completeContinuation(int i) {
        Log.debug(LABEL, "completeContinuation: Start", new Object[0]);
        if (checkContinuationIllegal()) {
            Log.warn(LABEL, "Ability not available to complete continuation.", new Object[0]);
            return;
        }
        if (i == 0 && this.reversible) {
            this.continuationState = ContinuationState.REMOTE_RUNNING;
        }
        changeProcessState(ProgressState.INITIAL);
        Bytrace.startTrace(2147483648L, "onCompleteContinuation");
        this.continuableAbility.onCompleteContinuation(i);
        Bytrace.finishTrace(2147483648L, "onCompleteContinuation");
        this.ability.getAbilitySliceManager().onCompleteContinuation(i);
    }

    /* access modifiers changed from: package-private */
    public void notifyRemoteTerminated() {
        Log.debug(LABEL, "notifyRemoteTerminated: Start", new Object[0]);
        this.continuationState = ContinuationState.LOCAL_RUNNING;
        changeProcessState(ProgressState.INITIAL);
        Bytrace.startTrace(2147483648L, "onRemoteTerminated");
        this.continuableAbility.onRemoteTerminated();
        Bytrace.finishTrace(2147483648L, "onRemoteTerminated");
        this.ability.getAbilitySliceManager().onRemoteTerminated();
    }

    /* access modifiers changed from: package-private */
    public boolean restoreFromRemote(IntentParams intentParams) {
        Log.debug(LABEL, "restoreFromRemote: Start", new Object[0]);
        changeProcessState(ProgressState.IN_PROGRESS);
        boolean doRestoreFromRemote = doRestoreFromRemote(intentParams);
        changeProcessState(ProgressState.INITIAL);
        if (doRestoreFromRemote) {
            this.continuationState = ContinuationState.LOCAL_RUNNING;
        }
        return doRestoreFromRemote;
    }

    private boolean doRestoreFromRemote(IntentParams intentParams) {
        return this.ability.getAbilitySliceManager().clearAndRestore(intentParams);
    }

    private boolean checkContinuationIllegal() {
        if (this.ability.getCurrentState() < 0) {
            Log.warn(LABEL, "Ability illegal unable to Continuation", new Object[0]);
            return true;
        } else if (this.ability.getAbilitySliceManager() != null) {
            return false;
        } else {
            Log.error(LABEL, "SliceManager is null, failed to StartContinuation.", new Object[0]);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean reverseContinueAbility() {
        Log.debug(LABEL, "reverseContinueAbility: Start", new Object[0]);
        if (this.progressState != ProgressState.INITIAL) {
            throw new IllegalStateException("Another request in progress.");
        } else if (this.continuationState == ContinuationState.REMOTE_RUNNING) {
            long currentTimeMillis = System.currentTimeMillis();
            boolean reverseContinueAbility = AbilityShellUtils.reverseContinueAbility(this.ability);
            if (reverseContinueAbility) {
                changeProcessState(ProgressState.WAITING_SCHEDULE);
                restoreStateWhenTimeout(6000, ProgressState.WAITING_SCHEDULE);
            }
            debugLog(this.ability.getAbilityInfo(), currentTimeMillis);
            return reverseContinueAbility;
        } else {
            throw new IllegalStateException("Illegal continuation state. Current state is " + this.continuationState);
        }
    }

    private static void debugLog(AbilityInfo abilityInfo, long j) {
        if (abilityInfo != null) {
            long currentTimeMillis = System.currentTimeMillis();
            JLog.debug(JLogConstants.JLID_LOCAL_CONTINUE_ABILITY_BEGIN, abilityInfo.getBundleName() + PsuedoNames.PSEUDONAME_ROOT + abilityInfo.getClassName() + " cost: " + (currentTimeMillis - j) + DateFormat.MINUTE_SECOND);
        }
    }
}
