package ohos.data.dataability.impl;

import ohos.data.resultset.SharedBlock;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IResultSetRemoteTransport extends IRemoteBroker {
    public static final int CLOSE_TRANSACTION_ID = 4;
    public static final int GET_BLOCK_TRANSACTION_ID = 3;
    public static final int ON_MOVE_TRANSACTION_ID = 2;
    public static final int REGISTER_OBSERVER_TRANSACTION_ID = 5;
    public static final int UNREGISTER_OBSERVER_TRANSACTION_ID = 6;

    void close() throws RemoteException;

    SharedBlock getBlock() throws RemoteException;

    boolean onMove(int i, int i2) throws RemoteException;

    void registerRemoteObserver(IRemoteResultSetObserver iRemoteResultSetObserver) throws RemoteException;

    void unregisterRemoteObserver() throws RemoteException;
}
