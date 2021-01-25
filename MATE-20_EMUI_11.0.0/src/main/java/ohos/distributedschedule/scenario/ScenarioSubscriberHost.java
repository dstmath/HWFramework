package ohos.distributedschedule.scenario;

import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

/* access modifiers changed from: package-private */
public abstract class ScenarioSubscriberHost extends RemoteObject implements IScenarioSubscriber {
    private static final int CODE_ON_SCENARIO_NOTIFY = 1;
    private static final String DESCRIPTOR = "ohos.distributedschedule.scenario.IScenarioSubscriber";
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109952, TAG);
    private static final String TAG = "ScenarioSubscriberHost";

    public IRemoteObject asObject() {
        return this;
    }

    static {
        try {
            HiLog.info(LABEL, "inner load libipc_core.z.so", new Object[0]);
            System.loadLibrary("ipc_core.z");
        } catch (UnsatisfiedLinkError unused) {
            HiLog.error(LABEL, "fail to load libipc_core.z.so", new Object[0]);
        }
    }

    ScenarioSubscriberHost() {
        super(DESCRIPTOR);
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null) {
            return false;
        }
        if (i != 1) {
            return ScenarioSubscriberHost.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
        }
        ScenarioResult unmarshalling = ScenarioResult.unmarshalling(messageParcel);
        if (unmarshalling == null) {
            HiLog.warn(LABEL, "onRemoteRequest read result failed.", new Object[0]);
            return false;
        }
        onScenarioNotify(unmarshalling);
        return true;
    }
}
