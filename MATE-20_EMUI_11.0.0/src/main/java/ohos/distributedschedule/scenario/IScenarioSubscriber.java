package ohos.distributedschedule.scenario;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

/* access modifiers changed from: package-private */
public interface IScenarioSubscriber extends IRemoteBroker {
    void onScenarioNotify(ScenarioResult scenarioResult) throws RemoteException;
}
