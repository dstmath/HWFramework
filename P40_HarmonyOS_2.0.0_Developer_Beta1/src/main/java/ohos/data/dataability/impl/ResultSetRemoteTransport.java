package ohos.data.dataability.impl;

import java.util.Objects;
import ohos.data.rdb.DataObserver;
import ohos.data.resultset.SharedBlock;
import ohos.data.resultset.SharedResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.IRemoteObject;
import ohos.rpc.MessageOption;
import ohos.rpc.MessageParcel;
import ohos.rpc.RemoteObject;

public class ResultSetRemoteTransport extends RemoteObject implements IResultSetRemoteTransport {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109536, "ResultSetRemoteTransport");
    private DataObserver dataObserver;
    private final Object mLock = new Object();
    private SharedResultSet shareResultSet;

    public IRemoteObject asObject() {
        return this;
    }

    public ResultSetRemoteTransport(SharedResultSet sharedResultSet) {
        super("");
        this.shareResultSet = sharedResultSet;
    }

    public boolean onRemoteRequest(int i, MessageParcel messageParcel, MessageParcel messageParcel2, MessageOption messageOption) {
        if (i == 2) {
            boolean onMove = onMove(messageParcel.readInt(), messageParcel.readInt());
            SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
            messageParcel2.writeBoolean(onMove);
            return true;
        } else if (i == 3) {
            SharedBlock block = getBlock();
            SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
            if (block == null) {
                messageParcel2.writeInt(0);
            } else {
                messageParcel2.writeInt(1);
                messageParcel2.writeString(block.getName());
                messageParcel2.writeInt(block.getStartRowIndex());
                byte[] rawData = block.getRawData();
                if (rawData == null) {
                    messageParcel2.writeInt(0);
                } else {
                    int length = rawData.length;
                    HiLog.info(LABEL, "block raw data bytes length:%{public}d", new Object[]{Integer.valueOf(length)});
                    messageParcel2.writeInt(length);
                    messageParcel2.writeRawData(rawData, length);
                }
            }
            return true;
        } else if (i == 4) {
            close();
            SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
            return true;
        } else if (i == 5) {
            registerRemoteObserver(new RemoteResultSetObserverProxy(messageParcel.readRemoteObject()));
            SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
            return true;
        } else if (i != 6) {
            try {
                return ResultSetRemoteTransport.super.onRemoteRequest(i, messageParcel, messageParcel2, messageOption);
            } catch (Exception e) {
                SqliteExceptionUtils.writeExceptionToParcel(messageParcel2, e);
                return true;
            }
        } else {
            unregisterRemoteObserver();
            SqliteExceptionUtils.writeNoExceptionToParcel(messageParcel2);
            return true;
        }
    }

    @Override // ohos.data.dataability.impl.IResultSetRemoteTransport
    public boolean onMove(int i, int i2) {
        boolean onGo;
        HiLog.info(LABEL, "onMove start", new Object[0]);
        synchronized (this.mLock) {
            throwIfResultSetIsClosed();
            onGo = this.shareResultSet.onGo(i, i2);
        }
        return onGo;
    }

    @Override // ohos.data.dataability.impl.IResultSetRemoteTransport
    public SharedBlock getBlock() {
        SharedBlock block;
        HiLog.info(LABEL, "getBlock start", new Object[0]);
        synchronized (this.mLock) {
            throwIfResultSetIsClosed();
            block = this.shareResultSet.getBlock();
        }
        return block;
    }

    @Override // ohos.data.dataability.impl.IResultSetRemoteTransport
    public void close() {
        HiLog.info(LABEL, "close start", new Object[0]);
        synchronized (this.mLock) {
            if (this.shareResultSet != null) {
                this.shareResultSet.close();
                this.shareResultSet = null;
            }
        }
    }

    @Override // ohos.data.dataability.impl.IResultSetRemoteTransport
    public void registerRemoteObserver(IRemoteResultSetObserver iRemoteResultSetObserver) {
        synchronized (this.mLock) {
            throwIfResultSetIsClosed();
            Objects.requireNonNull(iRemoteResultSetObserver);
            this.dataObserver = new DataObserver() {
                /* class ohos.data.dataability.impl.$$Lambda$_G03zquh3D3mOL0Z7XSU7A_vJD8 */

                @Override // ohos.data.rdb.DataObserver
                public final void onChange() {
                    IRemoteResultSetObserver.this.onChange();
                }
            };
            this.shareResultSet.registerObserver(this.dataObserver);
        }
    }

    @Override // ohos.data.dataability.impl.IResultSetRemoteTransport
    public void unregisterRemoteObserver() {
        synchronized (this.mLock) {
            throwIfResultSetIsClosed();
            this.shareResultSet.unregisterObserver(this.dataObserver);
        }
    }

    public ResultSetRemoteTransportDescriptor getResultSetRemoteTransportDescriptor() {
        ResultSetRemoteTransportDescriptor resultSetRemoteTransportDescriptor;
        synchronized (this.mLock) {
            throwIfResultSetIsClosed();
            resultSetRemoteTransportDescriptor = new ResultSetRemoteTransportDescriptor();
            resultSetRemoteTransportDescriptor.setRemoteTransport(this);
            resultSetRemoteTransportDescriptor.setColumnNames(this.shareResultSet.getAllColumnNames());
            resultSetRemoteTransportDescriptor.setCount(this.shareResultSet.getRowCount());
        }
        return resultSetRemoteTransportDescriptor;
    }

    private void throwIfResultSetIsClosed() {
        if (this.shareResultSet == null) {
            throw new IllegalStateException("Attempted to access a ResultSet after it has been closed.");
        }
    }
}
