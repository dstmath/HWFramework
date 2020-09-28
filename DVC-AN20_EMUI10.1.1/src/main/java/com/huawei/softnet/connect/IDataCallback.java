package com.huawei.softnet.connect;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDataCallback extends IInterface {
    int onBlockReceive(String str, String str2, byte[] bArr, int i, String str3) throws RemoteException;

    int onByteReceive(String str, String str2, byte[] bArr, int i, String str3) throws RemoteException;

    String onCommonUpdate(String str) throws RemoteException;

    int onFileReceive(String str, String str2, String str3, String str4) throws RemoteException;

    int onSendFileStateUpdate(String str, String str2, int i, String str3) throws RemoteException;

    int onStreamReceive(String str, String str2, DataPayload dataPayload, String str3) throws RemoteException;

    public static class Default implements IDataCallback {
        @Override // com.huawei.softnet.connect.IDataCallback
        public int onByteReceive(String remoteDeviceId, String remoteModuleName, byte[] data, int len, String para) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.connect.IDataCallback
        public int onBlockReceive(String remoteDeviceId, String remoteModuleName, byte[] data, int len, String para) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.connect.IDataCallback
        public int onFileReceive(String remoteDeviceId, String remoteModuleName, String file, String para) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.connect.IDataCallback
        public int onStreamReceive(String remoteDeviceId, String remoteModuleName, DataPayload stream, String para) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.connect.IDataCallback
        public int onSendFileStateUpdate(String remoteDeviceId, String remoteModuleName, int state, String para) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.softnet.connect.IDataCallback
        public String onCommonUpdate(String para) throws RemoteException {
            return null;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDataCallback {
        private static final String DESCRIPTOR = "com.huawei.softnet.connect.IDataCallback";
        static final int TRANSACTION_onBlockReceive = 2;
        static final int TRANSACTION_onByteReceive = 1;
        static final int TRANSACTION_onCommonUpdate = 6;
        static final int TRANSACTION_onFileReceive = 3;
        static final int TRANSACTION_onSendFileStateUpdate = 5;
        static final int TRANSACTION_onStreamReceive = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDataCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDataCallback)) {
                return new Proxy(obj);
            }
            return (IDataCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DataPayload _arg2;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = onByteReceive(data.readString(), data.readString(), data.createByteArray(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = onBlockReceive(data.readString(), data.readString(), data.createByteArray(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = onFileReceive(data.readString(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = DataPayload.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        int _result4 = onStreamReceive(_arg0, _arg1, _arg2, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = onSendFileStateUpdate(data.readString(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _result6 = onCommonUpdate(data.readString());
                        reply.writeNoException();
                        reply.writeString(_result6);
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
        public static class Proxy implements IDataCallback {
            public static IDataCallback sDefaultImpl;
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

            @Override // com.huawei.softnet.connect.IDataCallback
            public int onByteReceive(String remoteDeviceId, String remoteModuleName, byte[] data, int len, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModuleName);
                    _data.writeByteArray(data);
                    _data.writeInt(len);
                    _data.writeString(para);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onByteReceive(remoteDeviceId, remoteModuleName, data, len, para);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.connect.IDataCallback
            public int onBlockReceive(String remoteDeviceId, String remoteModuleName, byte[] data, int len, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModuleName);
                    _data.writeByteArray(data);
                    _data.writeInt(len);
                    _data.writeString(para);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onBlockReceive(remoteDeviceId, remoteModuleName, data, len, para);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.connect.IDataCallback
            public int onFileReceive(String remoteDeviceId, String remoteModuleName, String file, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModuleName);
                    _data.writeString(file);
                    _data.writeString(para);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onFileReceive(remoteDeviceId, remoteModuleName, file, para);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.connect.IDataCallback
            public int onStreamReceive(String remoteDeviceId, String remoteModuleName, DataPayload stream, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModuleName);
                    if (stream != null) {
                        _data.writeInt(1);
                        stream.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(para);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onStreamReceive(remoteDeviceId, remoteModuleName, stream, para);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.connect.IDataCallback
            public int onSendFileStateUpdate(String remoteDeviceId, String remoteModuleName, int state, String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(remoteDeviceId);
                    _data.writeString(remoteModuleName);
                    _data.writeInt(state);
                    _data.writeString(para);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onSendFileStateUpdate(remoteDeviceId, remoteModuleName, state, para);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.softnet.connect.IDataCallback
            public String onCommonUpdate(String para) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(para);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onCommonUpdate(para);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDataCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDataCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
