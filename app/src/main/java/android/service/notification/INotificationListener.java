package android.service.notification;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INotificationListener extends IInterface {

    public static abstract class Stub extends Binder implements INotificationListener {
        private static final String DESCRIPTOR = "android.service.notification.INotificationListener";
        static final int TRANSACTION_onInterruptionFilterChanged = 6;
        static final int TRANSACTION_onListenerConnected = 1;
        static final int TRANSACTION_onListenerHintsChanged = 5;
        static final int TRANSACTION_onNotificationActionClick = 10;
        static final int TRANSACTION_onNotificationClick = 9;
        static final int TRANSACTION_onNotificationEnqueued = 7;
        static final int TRANSACTION_onNotificationPosted = 2;
        static final int TRANSACTION_onNotificationRankingUpdate = 4;
        static final int TRANSACTION_onNotificationRemoved = 3;
        static final int TRANSACTION_onNotificationRemovedReason = 11;
        static final int TRANSACTION_onNotificationVisibilityChanged = 8;

        private static class Proxy implements INotificationListener {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void onListenerConnected(NotificationRankingUpdate update) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (update != null) {
                        _data.writeInt(Stub.TRANSACTION_onListenerConnected);
                        update.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onListenerConnected, _data, null, Stub.TRANSACTION_onListenerConnected);
                } finally {
                    _data.recycle();
                }
            }

            public void onNotificationPosted(IStatusBarNotificationHolder notificationHolder, NotificationRankingUpdate update) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (notificationHolder != null) {
                        iBinder = notificationHolder.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (update != null) {
                        _data.writeInt(Stub.TRANSACTION_onListenerConnected);
                        update.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationPosted, _data, null, Stub.TRANSACTION_onListenerConnected);
                } finally {
                    _data.recycle();
                }
            }

            public void onNotificationRemoved(IStatusBarNotificationHolder notificationHolder, NotificationRankingUpdate update) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (notificationHolder != null) {
                        iBinder = notificationHolder.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (update != null) {
                        _data.writeInt(Stub.TRANSACTION_onListenerConnected);
                        update.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationRemoved, _data, null, Stub.TRANSACTION_onListenerConnected);
                } finally {
                    _data.recycle();
                }
            }

            public void onNotificationRankingUpdate(NotificationRankingUpdate update) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (update != null) {
                        _data.writeInt(Stub.TRANSACTION_onListenerConnected);
                        update.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationRankingUpdate, _data, null, Stub.TRANSACTION_onListenerConnected);
                } finally {
                    _data.recycle();
                }
            }

            public void onListenerHintsChanged(int hints) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(hints);
                    this.mRemote.transact(Stub.TRANSACTION_onListenerHintsChanged, _data, null, Stub.TRANSACTION_onListenerConnected);
                } finally {
                    _data.recycle();
                }
            }

            public void onInterruptionFilterChanged(int interruptionFilter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(interruptionFilter);
                    this.mRemote.transact(Stub.TRANSACTION_onInterruptionFilterChanged, _data, null, Stub.TRANSACTION_onListenerConnected);
                } finally {
                    _data.recycle();
                }
            }

            public void onNotificationEnqueued(IStatusBarNotificationHolder notificationHolder, int importance, boolean user) throws RemoteException {
                int i = Stub.TRANSACTION_onListenerConnected;
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (notificationHolder != null) {
                        iBinder = notificationHolder.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(importance);
                    if (!user) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationEnqueued, _data, null, Stub.TRANSACTION_onListenerConnected);
                } finally {
                    _data.recycle();
                }
            }

            public void onNotificationVisibilityChanged(String key, long time, boolean visible) throws RemoteException {
                int i = Stub.TRANSACTION_onListenerConnected;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeLong(time);
                    if (!visible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationVisibilityChanged, _data, null, Stub.TRANSACTION_onListenerConnected);
                } finally {
                    _data.recycle();
                }
            }

            public void onNotificationClick(String key, long time) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeLong(time);
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationClick, _data, null, Stub.TRANSACTION_onListenerConnected);
                } finally {
                    _data.recycle();
                }
            }

            public void onNotificationActionClick(String key, long time, int actionIndex) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeLong(time);
                    _data.writeInt(actionIndex);
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationActionClick, _data, null, Stub.TRANSACTION_onListenerConnected);
                } finally {
                    _data.recycle();
                }
            }

            public void onNotificationRemovedReason(String key, long time, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(key);
                    _data.writeLong(time);
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_onNotificationRemovedReason, _data, null, Stub.TRANSACTION_onListenerConnected);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INotificationListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INotificationListener)) {
                return new Proxy(obj);
            }
            return (INotificationListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NotificationRankingUpdate notificationRankingUpdate;
            IStatusBarNotificationHolder _arg0;
            NotificationRankingUpdate notificationRankingUpdate2;
            switch (code) {
                case TRANSACTION_onListenerConnected /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        notificationRankingUpdate = (NotificationRankingUpdate) NotificationRankingUpdate.CREATOR.createFromParcel(data);
                    } else {
                        notificationRankingUpdate = null;
                    }
                    onListenerConnected(notificationRankingUpdate);
                    return true;
                case TRANSACTION_onNotificationPosted /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.service.notification.IStatusBarNotificationHolder.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        notificationRankingUpdate2 = (NotificationRankingUpdate) NotificationRankingUpdate.CREATOR.createFromParcel(data);
                    } else {
                        notificationRankingUpdate2 = null;
                    }
                    onNotificationPosted(_arg0, notificationRankingUpdate2);
                    return true;
                case TRANSACTION_onNotificationRemoved /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = android.service.notification.IStatusBarNotificationHolder.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        notificationRankingUpdate2 = (NotificationRankingUpdate) NotificationRankingUpdate.CREATOR.createFromParcel(data);
                    } else {
                        notificationRankingUpdate2 = null;
                    }
                    onNotificationRemoved(_arg0, notificationRankingUpdate2);
                    return true;
                case TRANSACTION_onNotificationRankingUpdate /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        notificationRankingUpdate = (NotificationRankingUpdate) NotificationRankingUpdate.CREATOR.createFromParcel(data);
                    } else {
                        notificationRankingUpdate = null;
                    }
                    onNotificationRankingUpdate(notificationRankingUpdate);
                    return true;
                case TRANSACTION_onListenerHintsChanged /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onListenerHintsChanged(data.readInt());
                    return true;
                case TRANSACTION_onInterruptionFilterChanged /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    onInterruptionFilterChanged(data.readInt());
                    return true;
                case TRANSACTION_onNotificationEnqueued /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationEnqueued(android.service.notification.IStatusBarNotificationHolder.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt() != 0);
                    return true;
                case TRANSACTION_onNotificationVisibilityChanged /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationVisibilityChanged(data.readString(), data.readLong(), data.readInt() != 0);
                    return true;
                case TRANSACTION_onNotificationClick /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationClick(data.readString(), data.readLong());
                    return true;
                case TRANSACTION_onNotificationActionClick /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationActionClick(data.readString(), data.readLong(), data.readInt());
                    return true;
                case TRANSACTION_onNotificationRemovedReason /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotificationRemovedReason(data.readString(), data.readLong(), data.readInt());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onInterruptionFilterChanged(int i) throws RemoteException;

    void onListenerConnected(NotificationRankingUpdate notificationRankingUpdate) throws RemoteException;

    void onListenerHintsChanged(int i) throws RemoteException;

    void onNotificationActionClick(String str, long j, int i) throws RemoteException;

    void onNotificationClick(String str, long j) throws RemoteException;

    void onNotificationEnqueued(IStatusBarNotificationHolder iStatusBarNotificationHolder, int i, boolean z) throws RemoteException;

    void onNotificationPosted(IStatusBarNotificationHolder iStatusBarNotificationHolder, NotificationRankingUpdate notificationRankingUpdate) throws RemoteException;

    void onNotificationRankingUpdate(NotificationRankingUpdate notificationRankingUpdate) throws RemoteException;

    void onNotificationRemoved(IStatusBarNotificationHolder iStatusBarNotificationHolder, NotificationRankingUpdate notificationRankingUpdate) throws RemoteException;

    void onNotificationRemovedReason(String str, long j, int i) throws RemoteException;

    void onNotificationVisibilityChanged(String str, long j, boolean z) throws RemoteException;
}
