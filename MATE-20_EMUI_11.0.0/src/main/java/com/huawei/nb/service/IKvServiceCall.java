package com.huawei.nb.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nb.callback.IKvSubscribeCallback;
import com.huawei.nb.container.ObjectContainer;
import com.huawei.nb.notification.IKvObserver;
import com.huawei.nb.notification.KeyObserverInfo;

public interface IKvServiceCall extends IInterface {
    boolean clearDataByOwner(String str) throws RemoteException;

    boolean delete(ObjectContainer objectContainer) throws RemoteException;

    ObjectContainer get(ObjectContainer objectContainer) throws RemoteException;

    int getCloneStatus(ObjectContainer objectContainer) throws RemoteException;

    int getDataClearStatus(ObjectContainer objectContainer) throws RemoteException;

    String getVersion(ObjectContainer objectContainer) throws RemoteException;

    boolean grant(ObjectContainer objectContainer, String str, int i) throws RemoteException;

    boolean put(ObjectContainer objectContainer) throws RemoteException;

    int registerObserver(KeyObserverInfo keyObserverInfo, IKvObserver iKvObserver, IKvSubscribeCallback iKvSubscribeCallback) throws RemoteException;

    boolean setCloneStatus(ObjectContainer objectContainer, int i) throws RemoteException;

    boolean setDataClearStatus(ObjectContainer objectContainer, int i) throws RemoteException;

    boolean setVersion(ObjectContainer objectContainer, String str) throws RemoteException;

    int unRegisterObserver(KeyObserverInfo keyObserverInfo, IKvObserver iKvObserver, IKvSubscribeCallback iKvSubscribeCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements IKvServiceCall {
        private static final String DESCRIPTOR = "com.huawei.nb.service.IKvServiceCall";
        static final int TRANSACTION_clearDataByOwner = 11;
        static final int TRANSACTION_delete = 3;
        static final int TRANSACTION_get = 2;
        static final int TRANSACTION_getCloneStatus = 8;
        static final int TRANSACTION_getDataClearStatus = 13;
        static final int TRANSACTION_getVersion = 6;
        static final int TRANSACTION_grant = 4;
        static final int TRANSACTION_put = 1;
        static final int TRANSACTION_registerObserver = 9;
        static final int TRANSACTION_setCloneStatus = 7;
        static final int TRANSACTION_setDataClearStatus = 12;
        static final int TRANSACTION_setVersion = 5;
        static final int TRANSACTION_unRegisterObserver = 10;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IKvServiceCall asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IKvServiceCall)) {
                return new Proxy(iBinder);
            }
            return (IKvServiceCall) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                ObjectContainer objectContainer = null;
                ObjectContainer objectContainer2 = null;
                ObjectContainer objectContainer3 = null;
                KeyObserverInfo keyObserverInfo = null;
                KeyObserverInfo keyObserverInfo2 = null;
                ObjectContainer objectContainer4 = null;
                ObjectContainer objectContainer5 = null;
                ObjectContainer objectContainer6 = null;
                ObjectContainer objectContainer7 = null;
                ObjectContainer objectContainer8 = null;
                ObjectContainer objectContainer9 = null;
                ObjectContainer objectContainer10 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        boolean put = put(objectContainer);
                        parcel2.writeNoException();
                        parcel2.writeInt(put ? 1 : 0);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer10 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        ObjectContainer objectContainer11 = get(objectContainer10);
                        parcel2.writeNoException();
                        if (objectContainer11 != null) {
                            parcel2.writeInt(1);
                            objectContainer11.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer9 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        boolean delete = delete(objectContainer9);
                        parcel2.writeNoException();
                        parcel2.writeInt(delete ? 1 : 0);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer8 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        boolean grant = grant(objectContainer8, parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(grant ? 1 : 0);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer7 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        boolean version = setVersion(objectContainer7, parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(version ? 1 : 0);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer6 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        String version2 = getVersion(objectContainer6);
                        parcel2.writeNoException();
                        parcel2.writeString(version2);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer5 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        boolean cloneStatus = setCloneStatus(objectContainer5, parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(cloneStatus ? 1 : 0);
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer4 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        int cloneStatus2 = getCloneStatus(objectContainer4);
                        parcel2.writeNoException();
                        parcel2.writeInt(cloneStatus2);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            keyObserverInfo2 = KeyObserverInfo.CREATOR.createFromParcel(parcel);
                        }
                        int registerObserver = registerObserver(keyObserverInfo2, IKvObserver.Stub.asInterface(parcel.readStrongBinder()), IKvSubscribeCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(registerObserver);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            keyObserverInfo = KeyObserverInfo.CREATOR.createFromParcel(parcel);
                        }
                        int unRegisterObserver = unRegisterObserver(keyObserverInfo, IKvObserver.Stub.asInterface(parcel.readStrongBinder()), IKvSubscribeCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(unRegisterObserver);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean clearDataByOwner = clearDataByOwner(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeInt(clearDataByOwner ? 1 : 0);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer3 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        boolean dataClearStatus = setDataClearStatus(objectContainer3, parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(dataClearStatus ? 1 : 0);
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer2 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        int dataClearStatus2 = getDataClearStatus(objectContainer2);
                        parcel2.writeNoException();
                        parcel2.writeInt(dataClearStatus2);
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IKvServiceCall {
            private IBinder mRemote;

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public boolean put(ObjectContainer objectContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public ObjectContainer get(ObjectContainer objectContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? ObjectContainer.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public boolean delete(ObjectContainer objectContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public boolean grant(ObjectContainer objectContainer, String str, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public boolean setVersion(ObjectContainer objectContainer, String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeString(str);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public String getVersion(ObjectContainer objectContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public boolean setCloneStatus(ObjectContainer objectContainer, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i);
                    this.mRemote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public int getCloneStatus(ObjectContainer objectContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public int registerObserver(KeyObserverInfo keyObserverInfo, IKvObserver iKvObserver, IKvSubscribeCallback iKvSubscribeCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyObserverInfo != null) {
                        obtain.writeInt(1);
                        keyObserverInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iKvObserver != null ? iKvObserver.asBinder() : null);
                    if (iKvSubscribeCallback != null) {
                        iBinder = iKvSubscribeCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.mRemote.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public int unRegisterObserver(KeyObserverInfo keyObserverInfo, IKvObserver iKvObserver, IKvSubscribeCallback iKvSubscribeCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyObserverInfo != null) {
                        obtain.writeInt(1);
                        keyObserverInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iKvObserver != null ? iKvObserver.asBinder() : null);
                    if (iKvSubscribeCallback != null) {
                        iBinder = iKvSubscribeCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.mRemote.transact(10, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public boolean clearDataByOwner(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    boolean z = false;
                    this.mRemote.transact(11, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public boolean setDataClearStatus(ObjectContainer objectContainer, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean z = true;
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeInt(i);
                    this.mRemote.transact(12, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() == 0) {
                        z = false;
                    }
                    return z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IKvServiceCall
            public int getDataClearStatus(ObjectContainer objectContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(13, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
