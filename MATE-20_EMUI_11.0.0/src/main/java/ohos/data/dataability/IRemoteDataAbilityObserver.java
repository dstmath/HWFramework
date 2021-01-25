package ohos.data.dataability;

import ohos.aafwk.ability.IDataAbilityObserver;
import ohos.rpc.IRemoteBroker;

public interface IRemoteDataAbilityObserver extends IRemoteBroker, IDataAbilityObserver {
    public static final int NOTIFY_OBSERVER_TRANSACTION_ID = 2;
}
