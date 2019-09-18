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

        private static class Proxy implements IKvServiceCall {
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

            public boolean put(ObjectContainer keyContainer) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyContainer != null) {
                        _data.writeInt(1);
                        keyContainer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
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

            public ObjectContainer get(ObjectContainer keyContainer) throws RemoteException {
                ObjectContainer _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyContainer != null) {
                        _data.writeInt(1);
                        keyContainer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
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

            public boolean delete(ObjectContainer keyContainer) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyContainer != null) {
                        _data.writeInt(1);
                        keyContainer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
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

            public boolean grant(ObjectContainer keyContainer, String packageName, int authority) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyContainer != null) {
                        _data.writeInt(1);
                        keyContainer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    _data.writeInt(authority);
                    this.mRemote.transact(4, _data, _reply, 0);
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

            public boolean setVersion(ObjectContainer keyContainer, String version) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyContainer != null) {
                        _data.writeInt(1);
                        keyContainer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(version);
                    this.mRemote.transact(5, _data, _reply, 0);
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

            public String getVersion(ObjectContainer keyContainer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyContainer != null) {
                        _data.writeInt(1);
                        keyContainer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setCloneStatus(ObjectContainer keyContainer, int clone) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyContainer != null) {
                        _data.writeInt(1);
                        keyContainer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(clone);
                    this.mRemote.transact(7, _data, _reply, 0);
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

            public int getCloneStatus(ObjectContainer keyContainer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyContainer != null) {
                        _data.writeInt(1);
                        keyContainer.writeToParcel(_data, 0);
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

            public int registerObserver(KeyObserverInfo info, IKvObserver observer, IKvSubscribeCallback cb) throws RemoteException {
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
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unRegisterObserver(KeyObserverInfo info, IKvObserver observer, IKvSubscribeCallback cb) throws RemoteException {
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

            public boolean clearDataByOwner(String ownerPkgName) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(ownerPkgName);
                    this.mRemote.transact(11, _data, _reply, 0);
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

            public boolean setDataClearStatus(ObjectContainer keyContainer, int status) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyContainer != null) {
                        _data.writeInt(1);
                        keyContainer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(status);
                    this.mRemote.transact(12, _data, _reply, 0);
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

            public int getDataClearStatus(ObjectContainer keyContainer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (keyContainer != null) {
                        _data.writeInt(1);
                        keyContainer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IKvServiceCall asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IKvServiceCall)) {
                return new Proxy(obj);
            }
            return (IKvServiceCall) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ObjectContainer _arg0;
            ObjectContainer _arg02;
            KeyObserverInfo _arg03;
            KeyObserverInfo _arg04;
            ObjectContainer _arg05;
            ObjectContainer _arg06;
            ObjectContainer _arg07;
            ObjectContainer _arg08;
            ObjectContainer _arg09;
            ObjectContainer _arg010;
            ObjectContainer _arg011;
            ObjectContainer _arg012;
            int i = 0;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg012 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg012 = null;
                    }
                    boolean _result = put(_arg012);
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg011 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg011 = null;
                    }
                    ObjectContainer _result2 = get(_arg011);
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(1);
                        _result2.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg010 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg010 = null;
                    }
                    boolean _result3 = delete(_arg010);
                    reply.writeNoException();
                    if (_result3) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg09 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg09 = null;
                    }
                    boolean _result4 = grant(_arg09, data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result4) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg08 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg08 = null;
                    }
                    boolean _result5 = setVersion(_arg08, data.readString());
                    reply.writeNoException();
                    if (_result5) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg07 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg07 = null;
                    }
                    String _result6 = getVersion(_arg07);
                    reply.writeNoException();
                    reply.writeString(_result6);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg06 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg06 = null;
                    }
                    boolean _result7 = setCloneStatus(_arg06, data.readInt());
                    reply.writeNoException();
                    if (_result7) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg05 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg05 = null;
                    }
                    int _result8 = getCloneStatus(_arg05);
                    reply.writeNoException();
                    reply.writeInt(_result8);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg04 = KeyObserverInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    int _result9 = registerObserver(_arg04, IKvObserver.Stub.asInterface(data.readStrongBinder()), IKvSubscribeCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result9);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = KeyObserverInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    int _result10 = unRegisterObserver(_arg03, IKvObserver.Stub.asInterface(data.readStrongBinder()), IKvSubscribeCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result10);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result11 = clearDataByOwner(data.readString());
                    reply.writeNoException();
                    if (_result11) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 12:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    boolean _result12 = setDataClearStatus(_arg02, data.readInt());
                    reply.writeNoException();
                    if (_result12) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    int _result13 = getDataClearStatus(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result13);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

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
}
