package com.huawei.nb.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nb.callback.IAIFetchCallback;
import com.huawei.nb.callback.IAISubscribeCallback;
import com.huawei.nb.callback.IDeleteResInfoCallBack;
import com.huawei.nb.callback.IUpdatePackageCallBack;
import com.huawei.nb.callback.IUpdatePackageCheckCallBack;
import com.huawei.nb.container.ObjectContainer;
import com.huawei.nb.notification.IAIObserver;
import com.huawei.nb.notification.ModelObserverInfo;

public interface IAIServiceCall extends IInterface {

    public static abstract class Stub extends Binder implements IAIServiceCall {
        private static final String DESCRIPTOR = "com.huawei.nb.service.IAIServiceCall";
        static final int TRANSACTION_deleteResInfoAgent = 5;
        static final int TRANSACTION_insertResInfoAgent = 6;
        static final int TRANSACTION_registerObserver = 8;
        static final int TRANSACTION_requestAiModel = 1;
        static final int TRANSACTION_requestAiModelAsync = 2;
        static final int TRANSACTION_unregisterObserver = 9;
        static final int TRANSACTION_updatePackageAgent = 4;
        static final int TRANSACTION_updatePackageCheckAgent = 3;
        static final int TRANSACTION_updateResInfoAgent = 7;

        private static class Proxy implements IAIServiceCall {
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

            public ObjectContainer requestAiModel(ObjectContainer requestContainer) throws RemoteException {
                ObjectContainer _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (requestContainer != null) {
                        _data.writeInt(1);
                        requestContainer.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
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

            public int requestAiModelAsync(ObjectContainer requestContainer, IAIFetchCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (requestContainer != null) {
                        _data.writeInt(1);
                        requestContainer.writeToParcel(_data, 0);
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

            public void updatePackageCheckAgent(ObjectContainer resources, IUpdatePackageCheckCallBack cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (resources != null) {
                        _data.writeInt(1);
                        resources.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updatePackageAgent(ObjectContainer resources, IUpdatePackageCallBack cb, long refreshInterval, long refreshBucketSize, boolean wifiOnly) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (resources != null) {
                        _data.writeInt(1);
                        resources.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    _data.writeLong(refreshInterval);
                    _data.writeLong(refreshBucketSize);
                    if (!wifiOnly) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteResInfoAgent(ObjectContainer resources, IDeleteResInfoCallBack cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (resources != null) {
                        _data.writeInt(1);
                        resources.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ObjectContainer insertResInfoAgent(ObjectContainer resource) throws RemoteException {
                ObjectContainer _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (resource != null) {
                        _data.writeInt(1);
                        resource.writeToParcel(_data, 0);
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

            public boolean updateResInfoAgent(ObjectContainer resource) throws RemoteException {
                boolean _result = true;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (resource != null) {
                        _data.writeInt(1);
                        resource.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
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

            public int registerObserver(ModelObserverInfo info, IAIObserver observer, IAISubscribeCallback cb) throws RemoteException {
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
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unregisterObserver(ModelObserverInfo info, IAIObserver observer, IAISubscribeCallback cb) throws RemoteException {
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAIServiceCall asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAIServiceCall)) {
                return new Proxy(obj);
            }
            return (IAIServiceCall) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ModelObserverInfo _arg0;
            ModelObserverInfo _arg02;
            ObjectContainer _arg03;
            ObjectContainer _arg04;
            ObjectContainer _arg05;
            ObjectContainer _arg06;
            ObjectContainer _arg07;
            ObjectContainer _arg08;
            ObjectContainer _arg09;
            boolean _arg4 = false;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg09 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg09 = null;
                    }
                    ObjectContainer _result = requestAiModel(_arg09);
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg08 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg08 = null;
                    }
                    int _result2 = requestAiModelAsync(_arg08, IAIFetchCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg07 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg07 = null;
                    }
                    updatePackageCheckAgent(_arg07, IUpdatePackageCheckCallBack.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg06 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg06 = null;
                    }
                    IUpdatePackageCallBack _arg1 = IUpdatePackageCallBack.Stub.asInterface(data.readStrongBinder());
                    long _arg2 = data.readLong();
                    long _arg3 = data.readLong();
                    if (data.readInt() != 0) {
                        _arg4 = true;
                    }
                    updatePackageAgent(_arg06, _arg1, _arg2, _arg3, _arg4);
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg05 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg05 = null;
                    }
                    deleteResInfoAgent(_arg05, IDeleteResInfoCallBack.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg04 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    ObjectContainer _result3 = insertResInfoAgent(_arg04);
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        _result3.writeToParcel(reply, 1);
                        return true;
                    }
                    reply.writeInt(0);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = ObjectContainer.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    boolean _result4 = updateResInfoAgent(_arg03);
                    reply.writeNoException();
                    if (_result4) {
                        _arg4 = true;
                    }
                    reply.writeInt(_arg4 ? 1 : 0);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = ModelObserverInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    int _result5 = registerObserver(_arg02, IAIObserver.Stub.asInterface(data.readStrongBinder()), IAISubscribeCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result5);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = ModelObserverInfo.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    int _result6 = unregisterObserver(_arg0, IAIObserver.Stub.asInterface(data.readStrongBinder()), IAISubscribeCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result6);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void deleteResInfoAgent(ObjectContainer objectContainer, IDeleteResInfoCallBack iDeleteResInfoCallBack) throws RemoteException;

    ObjectContainer insertResInfoAgent(ObjectContainer objectContainer) throws RemoteException;

    int registerObserver(ModelObserverInfo modelObserverInfo, IAIObserver iAIObserver, IAISubscribeCallback iAISubscribeCallback) throws RemoteException;

    ObjectContainer requestAiModel(ObjectContainer objectContainer) throws RemoteException;

    int requestAiModelAsync(ObjectContainer objectContainer, IAIFetchCallback iAIFetchCallback) throws RemoteException;

    int unregisterObserver(ModelObserverInfo modelObserverInfo, IAIObserver iAIObserver, IAISubscribeCallback iAISubscribeCallback) throws RemoteException;

    void updatePackageAgent(ObjectContainer objectContainer, IUpdatePackageCallBack iUpdatePackageCallBack, long j, long j2, boolean z) throws RemoteException;

    void updatePackageCheckAgent(ObjectContainer objectContainer, IUpdatePackageCheckCallBack iUpdatePackageCheckCallBack) throws RemoteException;

    boolean updateResInfoAgent(ObjectContainer objectContainer) throws RemoteException;
}
