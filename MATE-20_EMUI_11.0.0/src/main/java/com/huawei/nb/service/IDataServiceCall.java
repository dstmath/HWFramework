package com.huawei.nb.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nb.callback.IDeleteCallback;
import com.huawei.nb.callback.IFetchCallback;
import com.huawei.nb.callback.IInsertCallback;
import com.huawei.nb.callback.ISubscribeCallback;
import com.huawei.nb.callback.IUpdateCallback;
import com.huawei.nb.container.ObjectContainer;
import com.huawei.nb.notification.IModelObserver;
import com.huawei.nb.notification.ModelObserverInfo;
import com.huawei.nb.query.QueryContainer;
import com.huawei.nb.query.bulkcursor.BulkCursorDescriptor;

public interface IDataServiceCall extends IInterface {
    int batchImport(String str, String str2, String str3, int i) throws RemoteException;

    boolean clearUserData(String str, int i) throws RemoteException;

    BulkCursorDescriptor executeCursorQueryDirect(QueryContainer queryContainer) throws RemoteException;

    int executeDelete(ObjectContainer objectContainer, boolean z, IDeleteCallback iDeleteCallback) throws RemoteException;

    int executeDeleteDirect(ObjectContainer objectContainer, boolean z) throws RemoteException;

    int executeInsert(ObjectContainer objectContainer, IInsertCallback iInsertCallback) throws RemoteException;

    ObjectContainer executeInsertDirect(ObjectContainer objectContainer) throws RemoteException;

    int executeInsertEfficiently(ObjectContainer objectContainer) throws RemoteException;

    int executeQuery(QueryContainer queryContainer, IFetchCallback iFetchCallback) throws RemoteException;

    ObjectContainer executeQueryDirect(QueryContainer queryContainer) throws RemoteException;

    int executeUpdate(ObjectContainer objectContainer, IUpdateCallback iUpdateCallback) throws RemoteException;

    int executeUpdateDirect(ObjectContainer objectContainer) throws RemoteException;

    String getDatabaseVersion(String str) throws RemoteException;

    ObjectContainer getScheduledJobs() throws RemoteException;

    boolean handleAuthorityGrant(ObjectContainer objectContainer, int i) throws RemoteException;

    ObjectContainer handleDataLifeCycleConfig(int i, ObjectContainer objectContainer) throws RemoteException;

    int registerModelObserver(ModelObserverInfo modelObserverInfo, IModelObserver iModelObserver, ISubscribeCallback iSubscribeCallback) throws RemoteException;

    int unregisterModelObserver(ModelObserverInfo modelObserverInfo, IModelObserver iModelObserver, ISubscribeCallback iSubscribeCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements IDataServiceCall {
        private static final String DESCRIPTOR = "com.huawei.nb.service.IDataServiceCall";
        static final int TRANSACTION_batchImport = 1;
        static final int TRANSACTION_clearUserData = 14;
        static final int TRANSACTION_executeCursorQueryDirect = 7;
        static final int TRANSACTION_executeDelete = 5;
        static final int TRANSACTION_executeDeleteDirect = 9;
        static final int TRANSACTION_executeInsert = 3;
        static final int TRANSACTION_executeInsertDirect = 15;
        static final int TRANSACTION_executeInsertEfficiently = 16;
        static final int TRANSACTION_executeQuery = 2;
        static final int TRANSACTION_executeQueryDirect = 6;
        static final int TRANSACTION_executeUpdate = 4;
        static final int TRANSACTION_executeUpdateDirect = 8;
        static final int TRANSACTION_getDatabaseVersion = 13;
        static final int TRANSACTION_getScheduledJobs = 12;
        static final int TRANSACTION_handleAuthorityGrant = 17;
        static final int TRANSACTION_handleDataLifeCycleConfig = 18;
        static final int TRANSACTION_registerModelObserver = 10;
        static final int TRANSACTION_unregisterModelObserver = 11;

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDataServiceCall asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface == null || !(queryLocalInterface instanceof IDataServiceCall)) {
                return new Proxy(iBinder);
            }
            return (IDataServiceCall) queryLocalInterface;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            if (i != 1598968902) {
                boolean z = false;
                QueryContainer queryContainer = null;
                ObjectContainer objectContainer = null;
                ObjectContainer objectContainer2 = null;
                ObjectContainer objectContainer3 = null;
                ObjectContainer objectContainer4 = null;
                ModelObserverInfo modelObserverInfo = null;
                ModelObserverInfo modelObserverInfo2 = null;
                ObjectContainer objectContainer5 = null;
                ObjectContainer objectContainer6 = null;
                QueryContainer queryContainer2 = null;
                QueryContainer queryContainer3 = null;
                ObjectContainer objectContainer7 = null;
                ObjectContainer objectContainer8 = null;
                ObjectContainer objectContainer9 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        int batchImport = batchImport(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(batchImport);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            queryContainer = QueryContainer.CREATOR.createFromParcel(parcel);
                        }
                        int executeQuery = executeQuery(queryContainer, IFetchCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(executeQuery);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer9 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        int executeInsert = executeInsert(objectContainer9, IInsertCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(executeInsert);
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer8 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        int executeUpdate = executeUpdate(objectContainer8, IUpdateCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(executeUpdate);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer7 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        if (parcel.readInt() != 0) {
                            z = true;
                        }
                        int executeDelete = executeDelete(objectContainer7, z, IDeleteCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(executeDelete);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            queryContainer3 = QueryContainer.CREATOR.createFromParcel(parcel);
                        }
                        ObjectContainer executeQueryDirect = executeQueryDirect(queryContainer3);
                        parcel2.writeNoException();
                        if (executeQueryDirect != null) {
                            parcel2.writeInt(1);
                            executeQueryDirect.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            queryContainer2 = QueryContainer.CREATOR.createFromParcel(parcel);
                        }
                        BulkCursorDescriptor executeCursorQueryDirect = executeCursorQueryDirect(queryContainer2);
                        parcel2.writeNoException();
                        if (executeCursorQueryDirect != null) {
                            parcel2.writeInt(1);
                            executeCursorQueryDirect.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer6 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        int executeUpdateDirect = executeUpdateDirect(objectContainer6);
                        parcel2.writeNoException();
                        parcel2.writeInt(executeUpdateDirect);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer5 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        if (parcel.readInt() != 0) {
                            z = true;
                        }
                        int executeDeleteDirect = executeDeleteDirect(objectContainer5, z);
                        parcel2.writeNoException();
                        parcel2.writeInt(executeDeleteDirect);
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            modelObserverInfo2 = ModelObserverInfo.CREATOR.createFromParcel(parcel);
                        }
                        int registerModelObserver = registerModelObserver(modelObserverInfo2, IModelObserver.Stub.asInterface(parcel.readStrongBinder()), ISubscribeCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(registerModelObserver);
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            modelObserverInfo = ModelObserverInfo.CREATOR.createFromParcel(parcel);
                        }
                        int unregisterModelObserver = unregisterModelObserver(modelObserverInfo, IModelObserver.Stub.asInterface(parcel.readStrongBinder()), ISubscribeCallback.Stub.asInterface(parcel.readStrongBinder()));
                        parcel2.writeNoException();
                        parcel2.writeInt(unregisterModelObserver);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        ObjectContainer scheduledJobs = getScheduledJobs();
                        parcel2.writeNoException();
                        if (scheduledJobs != null) {
                            parcel2.writeInt(1);
                            scheduledJobs.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        String databaseVersion = getDatabaseVersion(parcel.readString());
                        parcel2.writeNoException();
                        parcel2.writeString(databaseVersion);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean clearUserData = clearUserData(parcel.readString(), parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(clearUserData ? 1 : 0);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer4 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        ObjectContainer executeInsertDirect = executeInsertDirect(objectContainer4);
                        parcel2.writeNoException();
                        if (executeInsertDirect != null) {
                            parcel2.writeInt(1);
                            executeInsertDirect.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer3 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        int executeInsertEfficiently = executeInsertEfficiently(objectContainer3);
                        parcel2.writeNoException();
                        parcel2.writeInt(executeInsertEfficiently);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (parcel.readInt() != 0) {
                            objectContainer2 = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        boolean handleAuthorityGrant = handleAuthorityGrant(objectContainer2, parcel.readInt());
                        parcel2.writeNoException();
                        parcel2.writeInt(handleAuthorityGrant ? 1 : 0);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        int readInt = parcel.readInt();
                        if (parcel.readInt() != 0) {
                            objectContainer = ObjectContainer.CREATOR.createFromParcel(parcel);
                        }
                        ObjectContainer handleDataLifeCycleConfig = handleDataLifeCycleConfig(readInt, objectContainer);
                        parcel2.writeNoException();
                        if (handleDataLifeCycleConfig != null) {
                            parcel2.writeInt(1);
                            handleDataLifeCycleConfig.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    default:
                        return super.onTransact(i, parcel, parcel2, i2);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDataServiceCall {
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

            @Override // com.huawei.nb.service.IDataServiceCall
            public int batchImport(String str, String str2, String str3, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    obtain.writeString(str3);
                    obtain.writeInt(i);
                    this.mRemote.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public int executeQuery(QueryContainer queryContainer, IFetchCallback iFetchCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (queryContainer != null) {
                        obtain.writeInt(1);
                        queryContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(iFetchCallback != null ? iFetchCallback.asBinder() : null);
                    this.mRemote.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public int executeInsert(ObjectContainer objectContainer, IInsertCallback iInsertCallback) throws RemoteException {
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
                    obtain.writeStrongBinder(iInsertCallback != null ? iInsertCallback.asBinder() : null);
                    this.mRemote.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public int executeUpdate(ObjectContainer objectContainer, IUpdateCallback iUpdateCallback) throws RemoteException {
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
                    obtain.writeStrongBinder(iUpdateCallback != null ? iUpdateCallback.asBinder() : null);
                    this.mRemote.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public int executeDelete(ObjectContainer objectContainer, boolean z, IDeleteCallback iDeleteCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (!z) {
                        i = 0;
                    }
                    obtain.writeInt(i);
                    obtain.writeStrongBinder(iDeleteCallback != null ? iDeleteCallback.asBinder() : null);
                    this.mRemote.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public ObjectContainer executeQueryDirect(QueryContainer queryContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (queryContainer != null) {
                        obtain.writeInt(1);
                        queryContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? ObjectContainer.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public BulkCursorDescriptor executeCursorQueryDirect(QueryContainer queryContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (queryContainer != null) {
                        obtain.writeInt(1);
                        queryContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? BulkCursorDescriptor.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public int executeUpdateDirect(ObjectContainer objectContainer) throws RemoteException {
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

            @Override // com.huawei.nb.service.IDataServiceCall
            public int executeDeleteDirect(ObjectContainer objectContainer, boolean z) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (!z) {
                        i = 0;
                    }
                    obtain.writeInt(i);
                    this.mRemote.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public int registerModelObserver(ModelObserverInfo modelObserverInfo, IModelObserver iModelObserver, ISubscribeCallback iSubscribeCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (modelObserverInfo != null) {
                        obtain.writeInt(1);
                        modelObserverInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iModelObserver != null ? iModelObserver.asBinder() : null);
                    if (iSubscribeCallback != null) {
                        iBinder = iSubscribeCallback.asBinder();
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

            @Override // com.huawei.nb.service.IDataServiceCall
            public int unregisterModelObserver(ModelObserverInfo modelObserverInfo, IModelObserver iModelObserver, ISubscribeCallback iSubscribeCallback) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (modelObserverInfo != null) {
                        obtain.writeInt(1);
                        modelObserverInfo.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    IBinder iBinder = null;
                    obtain.writeStrongBinder(iModelObserver != null ? iModelObserver.asBinder() : null);
                    if (iSubscribeCallback != null) {
                        iBinder = iSubscribeCallback.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.mRemote.transact(11, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public ObjectContainer getScheduledJobs() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? ObjectContainer.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public String getDatabaseVersion(String str) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    this.mRemote.transact(13, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readString();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public boolean clearUserData(String str, int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    boolean z = false;
                    this.mRemote.transact(14, obtain, obtain2, 0);
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

            @Override // com.huawei.nb.service.IDataServiceCall
            public ObjectContainer executeInsertDirect(ObjectContainer objectContainer) throws RemoteException {
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
                    this.mRemote.transact(15, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? ObjectContainer.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public int executeInsertEfficiently(ObjectContainer objectContainer) throws RemoteException {
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
                    this.mRemote.transact(16, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // com.huawei.nb.service.IDataServiceCall
            public boolean handleAuthorityGrant(ObjectContainer objectContainer, int i) throws RemoteException {
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
                    this.mRemote.transact(17, obtain, obtain2, 0);
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

            @Override // com.huawei.nb.service.IDataServiceCall
            public ObjectContainer handleDataLifeCycleConfig(int i, ObjectContainer objectContainer) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    obtain.writeInt(i);
                    if (objectContainer != null) {
                        obtain.writeInt(1);
                        objectContainer.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.mRemote.transact(18, obtain, obtain2, 0);
                    obtain2.readException();
                    return obtain2.readInt() != 0 ? ObjectContainer.CREATOR.createFromParcel(obtain2) : null;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }
    }
}
