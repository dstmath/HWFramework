package com.huawei.servicehost.pp;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIPRequest4CreatePipeline extends IInterface {
    void SetClickDown(boolean z) throws RemoteException;

    void SetCommand(String str, String str2) throws RemoteException;

    String getFilePath() throws RemoteException;

    String getLayout() throws RemoteException;

    void setAfRegion(byte[] bArr) throws RemoteException;

    void setFilePath(String str) throws RemoteException;

    void setForegroundSession(IBinder iBinder) throws RemoteException;

    void setLayout(String str) throws RemoteException;

    public static class Default implements IIPRequest4CreatePipeline {
        @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
        public String getLayout() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
        public void setLayout(String val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
        public String getFilePath() throws RemoteException {
            return null;
        }

        @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
        public void setFilePath(String val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
        public void setForegroundSession(IBinder val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
        public void setAfRegion(byte[] val) throws RemoteException {
        }

        @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
        public void SetCommand(String commandType, String commandValue) throws RemoteException {
        }

        @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
        public void SetClickDown(boolean isClickDown) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPRequest4CreatePipeline {
        private static final String DESCRIPTOR = "com.huawei.servicehost.pp.IIPRequest4CreatePipeline";
        static final int TRANSACTION_SetClickDown = 8;
        static final int TRANSACTION_SetCommand = 7;
        static final int TRANSACTION_getFilePath = 3;
        static final int TRANSACTION_getLayout = 1;
        static final int TRANSACTION_setAfRegion = 6;
        static final int TRANSACTION_setFilePath = 4;
        static final int TRANSACTION_setForegroundSession = 5;
        static final int TRANSACTION_setLayout = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPRequest4CreatePipeline asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPRequest4CreatePipeline)) {
                return new Proxy(obj);
            }
            return (IIPRequest4CreatePipeline) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _result = getLayout();
                        reply.writeNoException();
                        reply.writeString(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setLayout(data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getFilePath();
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setFilePath(data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setForegroundSession(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setAfRegion(data.createByteArray());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        SetCommand(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        SetClickDown(data.readInt() != 0);
                        reply.writeNoException();
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
        public static class Proxy implements IIPRequest4CreatePipeline {
            public static IIPRequest4CreatePipeline sDefaultImpl;
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

            @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
            public String getLayout() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLayout();
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

            @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
            public void setLayout(String val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(val);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setLayout(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
            public String getFilePath() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFilePath();
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

            @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
            public void setFilePath(String val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(val);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFilePath(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
            public void setForegroundSession(IBinder val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(val);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setForegroundSession(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
            public void setAfRegion(byte[] val) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(val);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAfRegion(val);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
            public void SetCommand(String commandType, String commandValue) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(commandType);
                    _data.writeString(commandValue);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().SetCommand(commandType, commandValue);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.servicehost.pp.IIPRequest4CreatePipeline
            public void SetClickDown(boolean isClickDown) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isClickDown ? 1 : 0);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().SetClickDown(isClickDown);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IIPRequest4CreatePipeline impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPRequest4CreatePipeline getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
