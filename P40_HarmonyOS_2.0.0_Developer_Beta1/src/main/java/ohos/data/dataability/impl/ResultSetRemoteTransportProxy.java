package ohos.data.dataability.impl;

import ohos.data.resultset.SharedBlock;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteException;

public class ResultSetRemoteTransportProxy implements IResultSetRemoteTransport {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "ResultSetRemoteTransportProxy");
    private IRemoteObject remote;

    public ResultSetRemoteTransportProxy(IRemoteObject iRemoteObject) {
        this.remote = iRemoteObject;
    }

    public IRemoteObject asObject() {
        return this.remote;
    }

    @Override // ohos.data.dataability.impl.IResultSetRemoteTransport
    public SharedBlock getBlock() throws RemoteException {
        if (this.remote == null) {
            HiLog.error(LABEL, "getBlock: remote cannot be null.", new Object[0]);
            return null;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            if (!this.remote.sendRequest(3, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "getBlock transact fail", new Object[0]);
                return null;
            }
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            if (obtain2.readInt() == 0) {
                obtain2.reclaim();
                obtain.reclaim();
                return null;
            }
            String readString = obtain2.readString();
            int readInt = obtain2.readInt();
            byte[] readRawData = obtain2.readRawData(obtain2.readInt());
            SharedBlock sharedBlock = new SharedBlock(readString);
            sharedBlock.setStartRowIndex(readInt);
            if (readRawData != null) {
                sharedBlock.setRawData(readRawData);
                HiLog.info(LABEL, "proxy getBlock raw data bytes size:%{public}d", new Object[]{Integer.valueOf(readRawData.length)});
            }
            obtain2.reclaim();
            obtain.reclaim();
            return sharedBlock;
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.data.dataability.impl.IResultSetRemoteTransport
    public boolean onMove(int i, int i2) throws RemoteException {
        if (this.remote == null) {
            HiLog.error(LABEL, "onMove: remote cannot be null.", new Object[0]);
            return false;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        MessageOption messageOption = new MessageOption();
        try {
            obtain.writeInt(i);
            obtain.writeInt(i2);
            if (!this.remote.sendRequest(2, obtain, obtain2, messageOption)) {
                HiLog.error(LABEL, "onMove transact fail", new Object[0]);
                return false;
            }
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            boolean readBoolean = obtain2.readBoolean();
            obtain2.reclaim();
            obtain.reclaim();
            return readBoolean;
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.data.dataability.impl.IResultSetRemoteTransport
    public void close() throws RemoteException {
        if (this.remote == null) {
            HiLog.error(LABEL, "close: remote cannot be null.", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            if (!this.remote.sendRequest(4, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "close transact fail", new Object[0]);
                return;
            }
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            obtain2.reclaim();
            obtain.reclaim();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }

    @Override // ohos.data.dataability.impl.IResultSetRemoteTransport
    public void registerRemoteObserver(IRemoteResultSetObserver iRemoteResultSetObserver) throws RemoteException {
        if (this.remote == null) {
            HiLog.error(LABEL, "registerObserver: remote cannot be null.", new Object[0]);
        } else if (!(iRemoteResultSetObserver instanceof RemoteResultSetObserverStub)) {
            HiLog.error(LABEL, "observer's type should be RemoteResultSetObserverStub.", new Object[0]);
        } else {
            RemoteResultSetObserverStub remoteResultSetObserverStub = (RemoteResultSetObserverStub) iRemoteResultSetObserver;
            MessageParcel obtain = MessageParcel.obtain();
            MessageParcel obtain2 = MessageParcel.obtain();
            MessageOption messageOption = new MessageOption();
            try {
                obtain.writeRemoteObject(remoteResultSetObserverStub);
                if (!this.remote.sendRequest(5, obtain, obtain2, messageOption)) {
                    HiLog.error(LABEL, "registerRemoteObserver transact fail", new Object[0]);
                    return;
                }
                SqliteExceptionUtils.readExceptionFromParcel(obtain2);
                obtain2.reclaim();
                obtain.reclaim();
            } finally {
                obtain2.reclaim();
                obtain.reclaim();
            }
        }
    }

    @Override // ohos.data.dataability.impl.IResultSetRemoteTransport
    public void unregisterRemoteObserver() throws RemoteException {
        if (this.remote == null) {
            HiLog.error(LABEL, "unregisterObserver: remote cannot be null.", new Object[0]);
            return;
        }
        MessageParcel obtain = MessageParcel.obtain();
        MessageParcel obtain2 = MessageParcel.obtain();
        try {
            if (!this.remote.sendRequest(6, obtain, obtain2, new MessageOption())) {
                HiLog.error(LABEL, "unregisterRemoteObserver transact fail", new Object[0]);
                return;
            }
            SqliteExceptionUtils.readExceptionFromParcel(obtain2);
            obtain2.reclaim();
            obtain.reclaim();
        } finally {
            obtain2.reclaim();
            obtain.reclaim();
        }
    }
}
