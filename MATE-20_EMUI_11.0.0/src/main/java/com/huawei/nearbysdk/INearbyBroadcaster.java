package com.huawei.nearbysdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nearbysdk.IBroadcastAdvResultCallBack;
import com.huawei.nearbysdk.IBroadcastScanResultCallBack;

public interface INearbyBroadcaster extends IInterface {
    void broadcast(byte[] bArr, int i, int i2, int i3) throws RemoteException;

    void broadcastWithCallback(IBroadcastAdvResultCallBack iBroadcastAdvResultCallBack, byte[] bArr, int i, int i2, int i3) throws RemoteException;

    boolean changeBroadcastContent(int i, int i2, byte[] bArr) throws RemoteException;

    void receiveBroadcast(IBroadcastScanResultCallBack iBroadcastScanResultCallBack, int i, int i2, int i3) throws RemoteException;

    void setHivoiceBinding(boolean z) throws RemoteException;

    void stopBroadcast(int i, int i2) throws RemoteException;

    void stopReceiveBroadcast(int i, int i2) throws RemoteException;

    public static abstract class Stub extends Binder implements INearbyBroadcaster {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.INearbyBroadcaster";
        static final int TRANSACTION_broadcast = 1;
        static final int TRANSACTION_broadcastWithCallback = 7;
        static final int TRANSACTION_changeBroadcastContent = 5;
        static final int TRANSACTION_receiveBroadcast = 2;
        static final int TRANSACTION_setHivoiceBinding = 6;
        static final int TRANSACTION_stopBroadcast = 3;
        static final int TRANSACTION_stopReceiveBroadcast = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INearbyBroadcaster asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INearbyBroadcaster)) {
                return new Proxy(obj);
            }
            return (INearbyBroadcaster) iin;
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
                        broadcast(data.createByteArray(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        receiveBroadcast(IBroadcastScanResultCallBack.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        stopBroadcast(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        stopReceiveBroadcast(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean changeBroadcastContent = changeBroadcastContent(data.readInt(), data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(changeBroadcastContent ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setHivoiceBinding(data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        broadcastWithCallback(IBroadcastAdvResultCallBack.Stub.asInterface(data.readStrongBinder()), data.createByteArray(), data.readInt(), data.readInt(), data.readInt());
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

        private static class Proxy implements INearbyBroadcaster {
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

            @Override // com.huawei.nearbysdk.INearbyBroadcaster
            public void broadcast(byte[] msg, int businessid, int timeout, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(msg);
                    _data.writeInt(businessid);
                    _data.writeInt(timeout);
                    _data.writeInt(type);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyBroadcaster
            public void receiveBroadcast(IBroadcastScanResultCallBack callback, int businessid, int timeout, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeInt(businessid);
                    _data.writeInt(timeout);
                    _data.writeInt(type);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyBroadcaster
            public void stopBroadcast(int bussinessId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bussinessId);
                    _data.writeInt(type);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyBroadcaster
            public void stopReceiveBroadcast(int bussinessId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bussinessId);
                    _data.writeInt(type);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyBroadcaster
            public boolean changeBroadcastContent(int businessId, int type, byte[] message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessId);
                    _data.writeInt(type);
                    _data.writeByteArray(message);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
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

            @Override // com.huawei.nearbysdk.INearbyBroadcaster
            public void setHivoiceBinding(boolean isBinding) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isBinding ? 1 : 0);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.INearbyBroadcaster
            public void broadcastWithCallback(IBroadcastAdvResultCallBack callback, byte[] msg, int businessid, int timeout, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    _data.writeByteArray(msg);
                    _data.writeInt(businessid);
                    _data.writeInt(timeout);
                    _data.writeInt(type);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
