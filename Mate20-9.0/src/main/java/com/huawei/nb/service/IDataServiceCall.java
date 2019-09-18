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

        private static class Proxy implements IDataServiceCall {
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

            public int batchImport(String database, String table, String dataFile, int dataFmt) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(database);
                    _data.writeString(table);
                    _data.writeString(dataFile);
                    _data.writeInt(dataFmt);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeQuery(QueryContainer query, IFetchCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (query != null) {
                        _data.writeInt(1);
                        query.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeInsert(ObjectContainer oc, IInsertCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oc != null) {
                        _data.writeInt(1);
                        oc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeUpdate(ObjectContainer oc, IUpdateCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oc != null) {
                        _data.writeInt(1);
                        oc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeDelete(ObjectContainer oc, boolean deleteAll, IDeleteCallback cb) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oc != null) {
                        _data.writeInt(1);
                        oc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!deleteAll) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ObjectContainer executeQueryDirect(QueryContainer query) throws RemoteException {
                ObjectContainer _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (query != null) {
                        _data.writeInt(1);
                        query.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ObjectContainer.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public BulkCursorDescriptor executeCursorQueryDirect(QueryContainer query) throws RemoteException {
                BulkCursorDescriptor _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (query != null) {
                        _data.writeInt(1);
                        query.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = BulkCursorDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeUpdateDirect(ObjectContainer oc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oc != null) {
                        _data.writeInt(1);
                        oc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeDeleteDirect(ObjectContainer oc, boolean deleteAll) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oc != null) {
                        _data.writeInt(1);
                        oc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!deleteAll) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int registerModelObserver(ModelObserverInfo info, IModelObserver observer, ISubscribeCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unregisterModelObserver(ModelObserverInfo info, IModelObserver observer, ISubscribeCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ObjectContainer getScheduledJobs() throws RemoteException {
                ObjectContainer _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ObjectContainer.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getDatabaseVersion(String databaseName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(databaseName);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean clearUserData(String databaseName, int type) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(databaseName);
                    _data.writeInt(type);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ObjectContainer executeInsertDirect(ObjectContainer oc) throws RemoteException {
                ObjectContainer _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oc != null) {
                        _data.writeInt(1);
                        oc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ObjectContainer.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int executeInsertEfficiently(ObjectContainer oc) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oc != null) {
                        _data.writeInt(1);
                        oc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean handleAuthorityGrant(ObjectContainer oc, int operationType) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (oc != null) {
                        _data.writeInt(1);
                        oc.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(operationType);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ObjectContainer handleDataLifeCycleConfig(int actionCode, ObjectContainer resources) throws RemoteException {
                ObjectContainer _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(actionCode);
                    if (resources != null) {
                        _data.writeInt(1);
                        resources.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ObjectContainer.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDataServiceCall asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDataServiceCall)) {
                return new Proxy(obj);
            }
            return (IDataServiceCall) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ObjectContainer _arg1;
            ObjectContainer _arg0;
            ObjectContainer _arg02;
            ObjectContainer _arg03;
            ModelObserverInfo _arg04;
            ModelObserverInfo _arg05;
            ObjectContainer _arg06;
            boolean _arg12;
            ObjectContainer _arg07;
            QueryContainer _arg08;
            QueryContainer _arg09;
            ObjectContainer _arg010;
            boolean _arg13;
            ObjectContainer _arg011;
            ObjectContainer _arg012;
            QueryContainer _arg013;
            int i = 0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    int _result = batchImport(data.readString(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg013 = QueryContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg013 = null;
                    }
                    int _result2 = executeQuery(_arg013, IFetchCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg012 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg012 = null;
                    }
                    int _result3 = executeInsert(_arg012, IInsertCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg011 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg011 = null;
                    }
                    int _result4 = executeUpdate(_arg011, IUpdateCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result4);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg010 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg010 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg13 = true;
                    } else {
                        _arg13 = false;
                    }
                    int _result5 = executeDelete(_arg010, _arg13, IDeleteCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg09 = QueryContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg09 = null;
                    }
                    ObjectContainer _result6 = executeQueryDirect(_arg09);
                    reply.writeNoException();
                    if (_result6 != null) {
                        reply.writeInt(1);
                        _result6.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg08 = QueryContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg08 = null;
                    }
                    BulkCursorDescriptor _result7 = executeCursorQueryDirect(_arg08);
                    reply.writeNoException();
                    if (_result7 != null) {
                        reply.writeInt(1);
                        _result7.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg07 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg07 = null;
                    }
                    int _result8 = executeUpdateDirect(_arg07);
                    reply.writeNoException();
                    reply.writeInt(_result8);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg06 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg06 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg12 = true;
                    } else {
                        _arg12 = false;
                    }
                    int _result9 = executeDeleteDirect(_arg06, _arg12);
                    reply.writeNoException();
                    reply.writeInt(_result9);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg05 = ModelObserverInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg05 = null;
                    }
                    int _result10 = registerModelObserver(_arg05, IModelObserver.Stub.asInterface(data.readStrongBinder()), ISubscribeCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result10);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg04 = ModelObserverInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    int _result11 = unregisterModelObserver(_arg04, IModelObserver.Stub.asInterface(data.readStrongBinder()), ISubscribeCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result11);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    ObjectContainer _result12 = getScheduledJobs();
                    reply.writeNoException();
                    if (_result12 != null) {
                        reply.writeInt(1);
                        _result12.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    String _result13 = getDatabaseVersion(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result13);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result14 = clearUserData(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result14) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    ObjectContainer _result15 = executeInsertDirect(_arg03);
                    reply.writeNoException();
                    if (_result15 != null) {
                        reply.writeInt(1);
                        _result15.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    int _result16 = executeInsertEfficiently(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result16);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    boolean _result17 = handleAuthorityGrant(_arg0, data.readInt());
                    reply.writeNoException();
                    if (_result17) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    int _arg014 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg1 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    ObjectContainer _result18 = handleDataLifeCycleConfig(_arg014, _arg1);
                    reply.writeNoException();
                    if (_result18 != null) {
                        reply.writeInt(1);
                        _result18.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

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
}
