package com.huawei.hilink.framework.aidl;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.hilink.framework.aidl.IConnectResultCallback;
import com.huawei.hilink.framework.aidl.IConnectionStateCallback;
import com.huawei.hilink.framework.aidl.IRequestHandler;
import com.huawei.hilink.framework.aidl.IResponseCallback;
import com.huawei.hilink.framework.aidl.IServiceFoundCallback;

public interface IHilinkService extends IInterface {
    int call(CallRequest callRequest, IResponseCallback iResponseCallback) throws RemoteException;

    int connect(ConnectRequest connectRequest, IConnectResultCallback iConnectResultCallback) throws RemoteException;

    int discover(DiscoverRequest discoverRequest, IServiceFoundCallback iServiceFoundCallback) throws RemoteException;

    int publishCanbeOffline(String str, String str2, PendingIntent pendingIntent) throws RemoteException;

    int publishKeepOnline(String str, String str2, IRequestHandler iRequestHandler) throws RemoteException;

    void registerConnectionStateCallback(IConnectionStateCallback iConnectionStateCallback) throws RemoteException;

    int sendResponse(int i, String str, CallRequest callRequest) throws RemoteException;

    void unpublish(String str) throws RemoteException;

    void unregisterConnectionStateCallback(IConnectionStateCallback iConnectionStateCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements IHilinkService {
        private static final String DESCRIPTOR = "com.huawei.hilink.framework.aidl.IHilinkService";
        static final int TRANSACTION_call = 2;
        static final int TRANSACTION_connect = 9;
        static final int TRANSACTION_discover = 1;
        static final int TRANSACTION_publishCanbeOffline = 4;
        static final int TRANSACTION_publishKeepOnline = 3;
        static final int TRANSACTION_registerConnectionStateCallback = 7;
        static final int TRANSACTION_sendResponse = 6;
        static final int TRANSACTION_unpublish = 5;
        static final int TRANSACTION_unregisterConnectionStateCallback = 8;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHilinkService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHilinkService)) {
                return new Proxy(obj);
            }
            return (IHilinkService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DiscoverRequest _arg0;
            CallRequest _arg02;
            PendingIntent _arg2;
            CallRequest _arg22;
            ConnectRequest _arg03;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = DiscoverRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        int _result = discover(_arg0, IServiceFoundCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = CallRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        int _result2 = call(_arg02, IResponseCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = publishKeepOnline(data.readString(), data.readString(), IRequestHandler.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result4 = publishCanbeOffline(_arg04, _arg1, _arg2);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        unpublish(data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg22 = CallRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        int _result5 = sendResponse(_arg05, _arg12, _arg22);
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        registerConnectionStateCallback(IConnectionStateCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterConnectionStateCallback(IConnectionStateCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ConnectRequest.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        int _result6 = connect(_arg03, IConnectResultCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHilinkService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // com.huawei.hilink.framework.aidl.IHilinkService
            public int discover(DiscoverRequest request, IServiceFoundCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hilink.framework.aidl.IHilinkService
            public int call(CallRequest request, IResponseCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hilink.framework.aidl.IHilinkService
            public int publishKeepOnline(String serviceType, String serviceID, IRequestHandler requestHandler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceType);
                    _data.writeString(serviceID);
                    _data.writeStrongBinder(requestHandler != null ? requestHandler.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hilink.framework.aidl.IHilinkService
            public int publishCanbeOffline(String serviceType, String serviceID, PendingIntent pendingIntent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceType);
                    _data.writeString(serviceID);
                    if (pendingIntent != null) {
                        _data.writeInt(1);
                        pendingIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hilink.framework.aidl.IHilinkService
            public void unpublish(String serviceID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(serviceID);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hilink.framework.aidl.IHilinkService
            public int sendResponse(int errorCode, String payload, CallRequest callRequest) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    _data.writeString(payload);
                    if (callRequest != null) {
                        _data.writeInt(1);
                        callRequest.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hilink.framework.aidl.IHilinkService
            public void registerConnectionStateCallback(IConnectionStateCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hilink.framework.aidl.IHilinkService
            public void unregisterConnectionStateCallback(IConnectionStateCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hilink.framework.aidl.IHilinkService
            public int connect(ConnectRequest request, IConnectResultCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
