package ohos.abilityshell;

import java.util.ArrayList;
import java.util.Optional;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentParams;
import ohos.abilityshell.ContinuationSchedulerForDmsStub;
import ohos.abilityshell.IReverseContinuationSchedulerMaster;
import ohos.abilityshell.IReverseContinuationSchedulerSlave;
import ohos.appexecfwk.utils.AppLog;
import ohos.appexecfwk.utils.ErrorCode;
import ohos.appexecfwk.utils.HiTraceUtil;
import ohos.appexecfwk.utils.JLogUtil;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ElementName;
import ohos.devtools.JLogConstants;
import ohos.hiviewdfx.HiLogLabel;
import ohos.hiviewdfx.HiTrace;
import ohos.hiviewdfx.HiTraceId;
import ohos.idn.BasicInfo;
import ohos.idn.DeviceManager;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

/* access modifiers changed from: package-private */
public class ContinuationHandler implements ContinuationSchedulerForDmsStub.IDistributeScheduleHandler, IReverseContinuationSchedulerSlave, IReverseContinuationSchedulerMaster {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218108160, "ContinuationHandler");
    static final String ORIGINAL_DEVICE_ID = "originalDeviceId";
    private final Ability ability;
    private AbilityInfo abilityInfo;
    private final IDistributedManager distManager;
    private String localDeviceId;
    private IReverseContinuationSchedulerMaster masterProxy;
    private IReverseContinuationSchedulerMaster.ReverseContinuationSchedulerMasterStub masterStub;
    private ClassLoader paramsClassLoader;
    private boolean reversible = false;
    private IReverseContinuationSchedulerSlave.ReverseContinuationSchedulerSlaveProxy slaveProxy;

    ContinuationHandler(Ability ability2, IDistributedManager iDistributedManager) {
        this.ability = ability2;
        this.distManager = iDistributedManager;
    }

    /* access modifiers changed from: package-private */
    public void setAbilityInfo(AbilityInfo abilityInfo2) {
        this.abilityInfo = new AbilityInfo(abilityInfo2);
        clearDeviceInfo(this.abilityInfo);
    }

    public void setParamsClassLoader(ClassLoader classLoader) {
        this.paramsClassLoader = classLoader;
    }

    private void clearDeviceInfo(AbilityInfo abilityInfo2) {
        abilityInfo2.setDeviceId(null);
        abilityInfo2.setDeviceTypes(new ArrayList());
    }

    /* access modifiers changed from: package-private */
    public void setReversible(boolean z) {
        this.reversible = z;
    }

    /* access modifiers changed from: package-private */
    public void setMasterStub(IReverseContinuationSchedulerMaster.ReverseContinuationSchedulerMasterStub reverseContinuationSchedulerMasterStub) {
        this.masterStub = reverseContinuationSchedulerMasterStub;
    }

    /* access modifiers changed from: package-private */
    public boolean reverseContinueAbility() {
        if (this.slaveProxy == null) {
            AppLog.w(LABEL, "reverseContinueAbility: Slave proxy not initialized, can not reverse", new Object[0]);
            return false;
        }
        AppLog.d(LABEL, "reverseContinueAbility: Start", new Object[0]);
        HiTraceId hiTraceBegin = HiTraceUtil.hiTraceBegin("reverseContinueAbility");
        boolean reverseContinuation = this.slaveProxy.reverseContinuation();
        HiTrace.end(hiTraceBegin);
        return reverseContinuation;
    }

    /* access modifiers changed from: package-private */
    public void notifyTerminationToMaster() {
        if (this.masterProxy == null) {
            AppLog.w(LABEL, "notifyTerminationToMaster: Master proxy not initialized, can not notify", new Object[0]);
            return;
        }
        AppLog.d(LABEL, "notifyTerminationToMaster: Start", new Object[0]);
        this.masterProxy.notifySlaveTerminated();
    }

    @Override // ohos.abilityshell.ContinuationSchedulerForDmsStub.IDistributeScheduleHandler
    public int handleStartContinuation(IRemoteObject iRemoteObject, String str) {
        if (iRemoteObject == null) {
            return -3;
        }
        AbilityInfo abilityInfo2 = this.abilityInfo;
        if (abilityInfo2 == null || this.ability == null || this.distManager == null) {
            return -2;
        }
        abilityInfo2.setDeviceId(str);
        long currentTimeMillis = System.currentTimeMillis();
        if (!this.ability.scheduleStartContinuation()) {
            AppLog.i(LABEL, "handleStartContinuation: Ability rejected.", new Object[0]);
            JLogUtil.debugLog(JLogConstants.JLID_ABILITY_SHELL_CONTINUE_ABILITY, this.abilityInfo.getBundleName(), this.abilityInfo.getClassName(), currentTimeMillis);
            return ErrorCode.ABILITY_REJECT_CONTINUATION;
        }
        IntentParams intentParams = new IntentParams();
        if (!this.ability.scheduleSaveData(intentParams)) {
            AppLog.w(LABEL, "handleStartContinuation: ScheduleSaveData failed.", new Object[0]);
            JLogUtil.debugLog(JLogConstants.JLID_ABILITY_SHELL_CONTINUE_ABILITY, this.abilityInfo.getBundleName(), this.abilityInfo.getClassName(), currentTimeMillis);
            return ErrorCode.ABILITY_FAILED_SAVE_DATA;
        }
        Intent intent = new Intent();
        intent.setParams(intentParams);
        intent.addFlags(8);
        if (this.abilityInfo.getLaunchMode() != AbilityInfo.LaunchMode.STANDARD) {
            AppLog.i(LABEL, "handleStartContinuation: Clear task.", new Object[0]);
            intent.addFlags(32768);
        }
        if (this.reversible) {
            AppLog.i(LABEL, "handleStartContinuation: Reversible.", new Object[0]);
            intent.addFlags(96);
            intent.setParam(ORIGINAL_DEVICE_ID, getLocalDeviceId());
        }
        intent.setElement(new ElementName("", this.abilityInfo.getBundleName(), this.abilityInfo.getClassName()));
        Integer num = null;
        try {
            num = this.distManager.startContinuation(intent, this.abilityInfo, iRemoteObject);
        } catch (RemoteException e) {
            AppLog.e(LABEL, "handleStartContinuation: RemoteException: %{public}s", e.getMessage());
        } catch (Throwable th) {
            JLogUtil.debugLog(JLogConstants.JLID_ABILITY_SHELL_CONTINUE_ABILITY, this.abilityInfo.getBundleName(), this.abilityInfo.getClassName(), currentTimeMillis);
            throw th;
        }
        JLogUtil.debugLog(JLogConstants.JLID_ABILITY_SHELL_CONTINUE_ABILITY, this.abilityInfo.getBundleName(), this.abilityInfo.getClassName(), currentTimeMillis);
        if (num == null) {
            return -2;
        }
        return num.intValue();
    }

    private String getLocalDeviceId() {
        String str = this.localDeviceId;
        if (str != null) {
            return str;
        }
        Optional localBasicInfo = new DeviceManager().getLocalBasicInfo();
        if (localBasicInfo.isPresent()) {
            this.localDeviceId = ((BasicInfo) localBasicInfo.get()).getNodeId();
            return this.localDeviceId;
        }
        AppLog.e(LABEL, "getLocalDeviceId: Null device id returned.", new Object[0]);
        return null;
    }

    @Override // ohos.abilityshell.ContinuationSchedulerForDmsStub.IDistributeScheduleHandler
    public void handleCompleteContinuation(int i) {
        this.ability.scheduleCompleteContinuation(i);
    }

    @Override // ohos.abilityshell.ContinuationSchedulerForDmsStub.IDistributeScheduleHandler
    public void handleReceiveRemoteScheduler(IRemoteObject iRemoteObject) {
        this.slaveProxy = new IReverseContinuationSchedulerSlave.ReverseContinuationSchedulerSlaveProxy(iRemoteObject);
        if (!iRemoteObject.addDeathRecipient(new IRemoteObject.DeathRecipient() {
            /* class ohos.abilityshell.$$Lambda$ContinuationHandler$ZiWIq7D5KZ5CnknzYVJz3NgJ40o */

            public final void onRemoteDied() {
                ContinuationHandler.this.lambda$handleReceiveRemoteScheduler$0$ContinuationHandler();
            }
        }, 0)) {
            AppLog.e(LABEL, "handleReceiveRemoteScheduler: Register slave dead listener fail", new Object[0]);
        }
        IRemoteObject iRemoteObject2 = this.masterStub;
        if (iRemoteObject2 == null) {
            AppLog.e(LABEL, "handleReceiveRemoteScheduler: Master stub not initialized, skip send to slave", new Object[0]);
        } else {
            this.slaveProxy.passMaster(iRemoteObject2);
        }
    }

    public /* synthetic */ void lambda$handleReceiveRemoteScheduler$0$ContinuationHandler() {
        AppLog.w(LABEL, "handleReceiveRemoteScheduler: Slave died.", new Object[0]);
        notifySlaveTerminated();
    }

    @Override // ohos.abilityshell.IReverseContinuationSchedulerMaster
    public void notifySlaveTerminated() {
        AppLog.d(LABEL, "notifySlaveTerminated: Start", new Object[0]);
        cleanUpAfterReverse();
        this.ability.notifyRemoteTerminated();
    }

    @Override // ohos.abilityshell.IReverseContinuationSchedulerMaster
    public boolean continuationBack(Intent intent) {
        int i = 0;
        AppLog.i(LABEL, "continuationBack: Start", new Object[0]);
        if (intent.getParams() != null && intent.getParams().getClassLoader() == null) {
            intent.getParams().setClassLoader(this.paramsClassLoader);
        }
        if (!this.ability.scheduleRestoreFromRemote(intent.getParams())) {
            AppLog.w(LABEL, "continuationBack: ScheduleRestoreFromRemote failed.", new Object[0]);
            i = ErrorCode.ABILITY_FAILED_RESTORE_DATA;
        }
        this.slaveProxy.notifyReverseResult(i);
        if (i != 0) {
            return true;
        }
        cleanUpAfterReverse();
        return true;
    }

    private void cleanUpAfterReverse() {
        this.slaveProxy = null;
    }

    @Override // ohos.abilityshell.IReverseContinuationSchedulerSlave
    public void passMaster(IRemoteObject iRemoteObject) {
        AppLog.i(LABEL, "passMaster: Start", new Object[0]);
        this.masterProxy = new IReverseContinuationSchedulerMaster.ReverseContinuationSchedulerMasterProxy(iRemoteObject);
    }

    @Override // ohos.abilityshell.IReverseContinuationSchedulerSlave
    public boolean reverseContinuation() {
        AppLog.i(LABEL, "reverseContinuation: Start", new Object[0]);
        if (this.masterProxy == null) {
            AppLog.e(LABEL, "reverseContinuation: Master proxy not initialized, can not reverse", new Object[0]);
            return false;
        } else if (this.abilityInfo == null) {
            AppLog.e(LABEL, "reverseContinuation: abilityInfo is null", new Object[0]);
            return false;
        } else {
            long currentTimeMillis = System.currentTimeMillis();
            if (!this.ability.scheduleStartContinuation()) {
                AppLog.i(LABEL, "reverseContinuation: Ability rejected.", new Object[0]);
                JLogUtil.debugLog(JLogConstants.JLID_ABILITY_SHELL_CONTINUE_ABILITY, this.abilityInfo.getBundleName(), this.abilityInfo.getClassName(), currentTimeMillis);
                return false;
            }
            IntentParams intentParams = new IntentParams();
            if (!this.ability.scheduleSaveData(intentParams)) {
                AppLog.w(LABEL, "reverseContinuation: ScheduleSaveData failed.", new Object[0]);
                JLogUtil.debugLog(JLogConstants.JLID_ABILITY_SHELL_CONTINUE_ABILITY, this.abilityInfo.getBundleName(), this.abilityInfo.getClassName(), currentTimeMillis);
                return false;
            }
            Intent intent = new Intent();
            intent.setParams(intentParams);
            if (!this.masterProxy.continuationBack(intent)) {
                return true;
            }
            AppLog.e(LABEL, "reverseContinuation: ContinuationBack send failed.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.abilityshell.IReverseContinuationSchedulerSlave
    public void notifyReverseResult(int i) {
        AppLog.i(LABEL, "notifyReverseResult: Start. result=%{public}d", Integer.valueOf(i));
        if (i == 0) {
            this.ability.terminateAbility();
        }
    }
}
