package ohos.abilityshell;

import java.util.concurrent.CountDownLatch;
import ohos.appexecfwk.utils.AppLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;
import ohos.rpc.RemoteObject;

public class DataAbilityCallback extends RemoteObject implements IRemoteBroker {
    private static final String DESCRIPTOR = "ohos.abilityshell.DataAbilityCallback";
    private static final int ON_GET_REMOTE_DATA_ABILITY_DONE = 0;
    private static final int ON_GET_REMOTE_DATA_ABILITY_FAILED = 1;
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");
    private final CountDownLatch latch;
    private volatile IRemoteObject remoteDataAbility;

    public IRemoteObject asObject() {
        return this;
    }

    public DataAbilityCallback(CountDownLatch countDownLatch) {
        super("");
        this.latch = countDownLatch;
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) throws RemoteException {
        if (messageParcel == null || messageParcel2 == null) {
            AppLog.e(SHELL_LABEL, "DataAbilityCallback::onRemoteRequest param invalid", new Object[0]);
            return false;
        } else if (!DESCRIPTOR.equals(messageParcel.readInterfaceToken())) {
            AppLog.e(SHELL_LABEL, "DataAbilityCallback::onRemoteRequest token is invalid.", new Object[0]);
            return false;
        } else {
            if (i == 0) {
                AppLog.d(SHELL_LABEL, "DataAbilityCallback::ON_GET_REMOTE_DATA_ABILITY_DONE receive", new Object[0]);
                if (messageParcel.readInt() != 0) {
                    AppLog.e(SHELL_LABEL, "DataAbilityCallback::ON_GET_REMOTE_DATA_ABILITY_DONE failed", new Object[0]);
                    return false;
                }
                this.remoteDataAbility = messageParcel.readRemoteObject();
                this.latch.countDown();
            } else if (i != 1) {
                AppLog.w(SHELL_LABEL, "DataAbilityCallback::onRemoteRequest unknown code", new Object[0]);
                return DataAbilityCallback.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } else {
                AppLog.w(SHELL_LABEL, "DataAbilityCallback::ON_GET_REMOTE_DATA_ABILITY_FAILED receive", new Object[0]);
                this.latch.countDown();
            }
            return true;
        }
    }

    public IRemoteObject getRemoteDataAbility() {
        return this.remoteDataAbility;
    }
}
