package com.huawei.hilink.framework.aidl;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.hilink.framework.aidl.IRequestHandler;
import com.huawei.hilink.framework.aidl.IResponseCallback;
import com.huawei.hilink.framework.aidl.IServiceFoundCallback;

public interface IHilinkService extends IInterface {

    public static abstract class Stub extends Binder implements IHilinkService {
        private static final String DESCRIPTOR = "com.huawei.hilink.framework.aidl.IHilinkService";
        static final int TRANSACTION_call = 2;
        static final int TRANSACTION_discover = 1;
        static final int TRANSACTION_publishCanbeOffline = 4;
        static final int TRANSACTION_publishKeepOnline = 3;
        static final int TRANSACTION_sendResponse = 6;
        static final int TRANSACTION_unpublish = 5;

        private static class Proxy implements IHilinkService {
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

            public int sendResponse(int errorCode, String payload, int requestID) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    _data.writeString(payload);
                    _data.writeInt(requestID);
                    this.mRemote.transact(6, _data, _reply, 0);
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

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: com.huawei.hilink.framework.aidl.DiscoverRequest} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v7, resolved type: com.huawei.hilink.framework.aidl.CallRequest} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v13, resolved type: android.app.PendingIntent} */
        /* JADX WARNING: type inference failed for: r0v2 */
        /* JADX WARNING: type inference failed for: r0v22 */
        /* JADX WARNING: type inference failed for: r0v23 */
        /* JADX WARNING: type inference failed for: r0v24 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ? _arg2 = 0;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = DiscoverRequest.CREATOR.createFromParcel(data);
                        }
                        int _result = discover(_arg2, IServiceFoundCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg2 = CallRequest.CREATOR.createFromParcel(data);
                        }
                        int _result2 = call(_arg2, IResponseCallback.Stub.asInterface(data.readStrongBinder()));
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
                        String _arg0 = data.readString();
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                        }
                        int _result4 = publishCanbeOffline(_arg0, _arg1, _arg2);
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
                        int _result5 = sendResponse(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    int call(CallRequest callRequest, IResponseCallback iResponseCallback) throws RemoteException;

    int discover(DiscoverRequest discoverRequest, IServiceFoundCallback iServiceFoundCallback) throws RemoteException;

    int publishCanbeOffline(String str, String str2, PendingIntent pendingIntent) throws RemoteException;

    int publishKeepOnline(String str, String str2, IRequestHandler iRequestHandler) throws RemoteException;

    int sendResponse(int i, String str, int i2) throws RemoteException;

    void unpublish(String str) throws RemoteException;
}
