package com.huawei.android.os;

import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class BinderRemotely {
    private static final String REMOTE_DBINDER_DESCRIPTOR = "com.huawei.dbinder.IBinderAgent";
    private static final String TAG = "BinderRemotely";
    private IBinder mBinder = null;
    private RemoteDeathRecipient mRemoteDeathRecipient = null;

    public BinderRemotely(IBinder binder) {
        this.mBinder = binder;
    }

    public boolean linkToDeathRemotely(IBinder.DeathRecipient recipient, int flags) {
        if (!isRemotely(flags)) {
            return false;
        }
        if (this.mRemoteDeathRecipient == null) {
            this.mRemoteDeathRecipient = new RemoteDeathRecipient();
        }
        Log.i(TAG, "linkToDeath add " + recipient + " , list size: " + this.mRemoteDeathRecipient.size());
        this.mRemoteDeathRecipient.add(recipient, flags);
        if (this.mRemoteDeathRecipient.size() == 1) {
            linkToDeathRemotely(true);
        }
        return true;
    }

    private boolean isRemotely(int flags) {
        boolean z = true;
        if (this.mRemoteDeathRecipient != null) {
            return true;
        }
        boolean isRemotely = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            IBinder iBinder = this.mBinder;
            IBinder iBinder2 = this.mBinder;
            if (iBinder.transact(IBinder.REMOTE_TRANSACTION, data, reply, 0)) {
                if (reply.readInt() == 0) {
                    z = false;
                }
                isRemotely = z;
            }
        } catch (RemoteException e) {
            Log.w(TAG, "Ignore remote linkToDeath.");
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
        return isRemotely;
    }

    public boolean unlinkToDeathRemotely(IBinder.DeathRecipient recipient, int flags) throws NoSuchElementException {
        RemoteDeathRecipient remoteDeathRecipient = this.mRemoteDeathRecipient;
        if (remoteDeathRecipient == null) {
            return false;
        }
        if (remoteDeathRecipient.isDead()) {
            return true;
        }
        Log.i(TAG, "linkToDeath del " + recipient + " , list size: " + this.mRemoteDeathRecipient.size());
        if (!this.mRemoteDeathRecipient.remove(recipient, flags)) {
            throw new NoSuchElementException("Death link does not exist");
        } else if (!this.mRemoteDeathRecipient.isEmpty()) {
            return true;
        } else {
            linkToDeathRemotely(false);
            return true;
        }
    }

    private void linkToDeathRemotely(boolean isLink) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken(REMOTE_DBINDER_DESCRIPTOR);
            data.writeInt(isLink ? 1 : 0);
            data.writeStrongBinder(this.mRemoteDeathRecipient);
            IBinder iBinder = this.mBinder;
            IBinder iBinder2 = this.mBinder;
            iBinder.transact(IBinder.LINK_TO_DEATH_TRANSACTION_REMOTELY, data, reply, 0);
        } catch (RemoteException e) {
            Log.w(TAG, "Ignore linkToDeath RemoteException, isLink: " + isLink);
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
            throw th;
        }
        data.recycle();
        reply.recycle();
    }

    /* access modifiers changed from: private */
    public static class RemoteDeathRecipient extends Binder {
        private final Object mLock;
        private final List<Pair<IBinder.DeathRecipient, Integer>> mRecipientList;
        private Boolean mRemoteDead;

        private RemoteDeathRecipient() {
            this.mRecipientList = new ArrayList(1);
            this.mLock = new Object();
            this.mRemoteDead = false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void add(IBinder.DeathRecipient recipient, int flags) {
            synchronized (this.mLock) {
                this.mRecipientList.add(new Pair<>(recipient, Integer.valueOf(flags)));
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean remove(IBinder.DeathRecipient recipient, int flags) {
            synchronized (this.mLock) {
                Iterator<Pair<IBinder.DeathRecipient, Integer>> pairIterator = this.mRecipientList.iterator();
                while (pairIterator.hasNext()) {
                    Pair<IBinder.DeathRecipient, Integer> pair = pairIterator.next();
                    if (pair.first == recipient && pair.second.intValue() == flags) {
                        pairIterator.remove();
                        return true;
                    }
                }
                return false;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int size() {
            int size;
            synchronized (this.mLock) {
                size = this.mRecipientList.size();
            }
            return size;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isEmpty() {
            boolean isEmpty;
            synchronized (this.mLock) {
                isEmpty = this.mRecipientList.isEmpty();
            }
            return isEmpty;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isDead() {
            return this.mRemoteDead.booleanValue();
        }

        /* access modifiers changed from: protected */
        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1) {
                return super.onTransact(code, data, reply, flags);
            }
            data.enforceInterface(BinderRemotely.REMOTE_DBINDER_DESCRIPTOR);
            this.mRemoteDead = true;
            synchronized (this.mLock) {
                for (Pair<IBinder.DeathRecipient, Integer> pair : this.mRecipientList) {
                    Log.w(BinderRemotely.TAG, "Remote linkToDeath binderDied: " + pair);
                    pair.first.binderDied();
                }
                this.mRecipientList.clear();
            }
            return true;
        }
    }
}
