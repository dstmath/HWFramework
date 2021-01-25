package ohos.abilityshell;

import ohos.rpc.IRemoteObject;

interface IContinuationSchedulerForDms {
    void receiveSlaveScheduler(IRemoteObject iRemoteObject);

    void scheduleCompleteContinuation(int i);

    int scheduleStartContinuation(IRemoteObject iRemoteObject, String str);
}
