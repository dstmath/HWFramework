package com.huawei.dmsdpsdk2;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISecureFileListener extends IInterface {
    long getSecureFileSize(String str) throws RemoteException;

    byte[] readSecureFile(String str) throws RemoteException;

    boolean writeSecureFile(String str, byte[] bArr) throws RemoteException;

    public static class Default implements ISecureFileListener {
        @Override // com.huawei.dmsdpsdk2.ISecureFileListener
        public long getSecureFileSize(String fileName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.dmsdpsdk2.ISecureFileListener
        public byte[] readSecureFile(String fileName) throws RemoteException {
            return null;
        }

        @Override // com.huawei.dmsdpsdk2.ISecureFileListener
        public boolean writeSecureFile(String fileName, byte[] bytes) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISecureFileListener {
        private static final String DESCRIPTOR = "com.huawei.dmsdpsdk.ISecureFileListener";
        static final int TRANSACTION_getSecureFileSize = 1;
        static final int TRANSACTION_readSecureFile = 2;
        static final int TRANSACTION_writeSecureFile = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISecureFileListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISecureFileListener)) {
                return new Proxy(obj);
            }
            return (ISecureFileListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                long _result = getSecureFileSize(data.readString());
                reply.writeNoException();
                reply.writeLong(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                byte[] _result2 = readSecureFile(data.readString());
                reply.writeNoException();
                reply.writeByteArray(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean writeSecureFile = writeSecureFile(data.readString(), data.createByteArray());
                reply.writeNoException();
                reply.writeInt(writeSecureFile ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISecureFileListener {
            public static ISecureFileListener sDefaultImpl;
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

            @Override // com.huawei.dmsdpsdk2.ISecureFileListener
            public long getSecureFileSize(String fileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileName);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getSecureFileSize(fileName);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.ISecureFileListener
            public byte[] readSecureFile(String fileName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileName);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().readSecureFile(fileName);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.dmsdpsdk2.ISecureFileListener
            public boolean writeSecureFile(String fileName, byte[] bytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(fileName);
                    _data.writeByteArray(bytes);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().writeSecureFile(fileName, bytes);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISecureFileListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISecureFileListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
