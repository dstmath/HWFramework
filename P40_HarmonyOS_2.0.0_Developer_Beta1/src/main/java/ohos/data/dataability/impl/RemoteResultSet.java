package ohos.data.dataability.impl;

import java.lang.ref.WeakReference;
import java.util.Vector;
import ohos.data.rdb.DataObserver;
import ohos.data.resultset.AbsSharedResultSet;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class RemoteResultSet extends AbsSharedResultSet {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218109520, "RemoteResultSet");
    private final Object LOCK = new Object();
    private String[] columns;
    private int count;
    private RemoteDataObservable dataObservable = new RemoteDataObservable();
    private boolean isClosed = false;
    private IResultSetRemoteTransport remoteTransport;
    private RemoteResultSetObserverStub stub;

    public RemoteResultSet(ResultSetRemoteTransportDescriptor resultSetRemoteTransportDescriptor) {
        super("");
        if (resultSetRemoteTransportDescriptor != null) {
            this.remoteTransport = resultSetRemoteTransportDescriptor.getRemoteTransport();
            this.columns = resultSetRemoteTransportDescriptor.getColumnNames();
            this.count = resultSetRemoteTransportDescriptor.getCount();
            this.sharedBlock = null;
            return;
        }
        HiLog.info(LABEL, "RemoteResultSet: descriptor cannot be null.", new Object[0]);
        throw new IllegalArgumentException("descriptor cannot be null");
    }

    private void throwIfResultSetIsClosed() {
        if (this.remoteTransport == null) {
            throw new IllegalStateException("Attempted to access a resultSet after it has been closed.");
        }
    }

    @Override // ohos.data.resultset.AbsSharedResultSet, ohos.data.resultset.SharedResultSet
    public boolean onGo(int i, int i2) {
        throwIfResultSetIsClosed();
        try {
            if (this.sharedBlock == null) {
                if (!this.remoteTransport.onMove(i, i2)) {
                    return false;
                }
                setBlock(this.remoteTransport.getBlock());
            }
            if (this.sharedBlock == null) {
                HiLog.info(LABEL, "sharedBlock is null object.", new Object[0]);
                return false;
            }
            if (i2 < this.sharedBlock.getStartRowIndex() || i2 >= this.sharedBlock.getStartRowIndex() + this.sharedBlock.getRowCount()) {
                if (!this.remoteTransport.onMove(i, i2)) {
                    return false;
                }
                setBlock(this.remoteTransport.getBlock());
            }
            if (this.sharedBlock == null) {
                return false;
            }
            return true;
        } catch (RemoteException unused) {
            HiLog.info(LABEL, "Unable to getBlock for the remote connection is dead.", new Object[0]);
            return false;
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getRowCount() {
        throwIfResultSetIsClosed();
        return this.count;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public int getColumnCount() {
        throwIfResultSetIsClosed();
        return this.columns.length;
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public String[] getAllColumnNames() {
        throwIfResultSetIsClosed();
        return this.columns;
    }

    @Override // ohos.data.resultset.AbsSharedResultSet, ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public void close() {
        synchronized (this.LOCK) {
            super.close();
            if (this.sharedBlock != null) {
                this.sharedBlock.close();
            }
            this.isClosed = true;
            if (this.remoteTransport != null) {
                try {
                    unregisterRemoteObserver();
                    this.dataObservable.removeAll();
                    this.remoteTransport.close();
                } catch (RemoteException unused) {
                    HiLog.info(LABEL, "Remote process exception when block is closed.", new Object[0]);
                } catch (Throwable th) {
                    this.remoteTransport = null;
                    throw th;
                }
                this.remoteTransport = null;
            }
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public boolean isClosed() {
        return this.isClosed;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.data.resultset.AbsResultSet
    public void notifyChange() {
        super.notifyChange();
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public void registerObserver(DataObserver dataObserver) {
        synchronized (this.LOCK) {
            throwIfResultSetIsClosed();
            super.registerObserver(dataObserver);
            this.dataObservable.add(dataObserver);
            try {
                if (this.stub == null) {
                    this.stub = new RemoteResultSetObserverStub("RemoteResultSetObserver", new WeakReference(this));
                    this.remoteTransport.registerRemoteObserver(this.stub);
                }
            } catch (RemoteException unused) {
                HiLog.info(LABEL, "Remote process exception when block is closed.", new Object[0]);
            }
        }
    }

    @Override // ohos.data.resultset.AbsResultSet, ohos.data.resultset.ResultSet
    public void unregisterObserver(DataObserver dataObserver) {
        synchronized (this.LOCK) {
            throwIfResultSetIsClosed();
            super.unregisterObserver(dataObserver);
            this.dataObservable.remove(dataObserver);
            if (this.dataObservable.isEmpty()) {
                unregisterRemoteObserver();
            }
        }
    }

    private void unregisterRemoteObserver() {
        try {
            if (this.stub != null) {
                this.remoteTransport.unregisterRemoteObserver();
                this.stub = null;
            }
        } catch (RemoteException unused) {
            HiLog.info(LABEL, "Remote process exception when block is closed.", new Object[0]);
        }
    }

    private static class RemoteDataObservable {
        private Vector<DataObserver> observers;

        private RemoteDataObservable() {
            this.observers = new Vector<>();
        }

        public void add(DataObserver dataObserver) {
            if (dataObserver == null) {
                throw new IllegalArgumentException("input observer cannot be null!");
            } else if (!this.observers.contains(dataObserver)) {
                this.observers.add(dataObserver);
            }
        }

        public void remove(DataObserver dataObserver) {
            if (dataObserver != null) {
                this.observers.remove(dataObserver);
                return;
            }
            throw new IllegalArgumentException("input observer cannot be null!");
        }

        public boolean isEmpty() {
            return this.observers.isEmpty();
        }

        public void removeAll() {
            this.observers.clear();
        }
    }
}
