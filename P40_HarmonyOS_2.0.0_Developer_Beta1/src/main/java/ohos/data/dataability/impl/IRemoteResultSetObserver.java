package ohos.data.dataability.impl;

import ohos.rpc.IRemoteBroker;

public interface IRemoteResultSetObserver extends IRemoteBroker {
    public static final int NOTIFY_RESULT_SET_OBSERVER_TRANSACTION_ID = 2;

    void onChange();
}
