package com.huawei.nearbysdk;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.nearbysdk.IInternalChannelCreateRequest;
import com.huawei.nearbysdk.InternalNearbySocket;

public interface IInternalSocketListener extends IInterface {
    void onConnectRequest(IInternalChannelCreateRequest iInternalChannelCreateRequest) throws RemoteException;

    void onHwShareIConnectRequest(InternalNearbySocket internalNearbySocket, String str) throws RemoteException;

    void onStatusChange(int i) throws RemoteException;

    public static abstract class Stub extends Binder implements IInternalSocketListener {
        private static final String DESCRIPTOR = "com.huawei.nearbysdk.IInternalSocketListener";
        static final int TRANSACTION_onConnectRequest = 2;
        static final int TRANSACTION_onHwShareIConnectRequest = 3;
        static final int TRANSACTION_onStatusChange = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInternalSocketListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInternalSocketListener)) {
                return new Proxy(obj);
            }
            return (IInternalSocketListener) iin;
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
                        onStatusChange(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onConnectRequest(IInternalChannelCreateRequest.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onHwShareIConnectRequest(InternalNearbySocket.Stub.asInterface(data.readStrongBinder()), data.readString());
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

        private static class Proxy implements IInternalSocketListener {
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

            @Override // com.huawei.nearbysdk.IInternalSocketListener
            public void onStatusChange(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.IInternalSocketListener
            public void onConnectRequest(IInternalChannelCreateRequest request) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(request != null ? request.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.nearbysdk.IInternalSocketListener
            public void onHwShareIConnectRequest(InternalNearbySocket socket, String passpharse) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(socket != null ? socket.asBinder() : null);
                    _data.writeString(passpharse);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
