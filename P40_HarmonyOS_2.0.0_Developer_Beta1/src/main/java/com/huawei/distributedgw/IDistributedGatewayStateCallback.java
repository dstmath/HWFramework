package com.huawei.distributedgw;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDistributedGatewayStateCallback extends IInterface {
    void onBorrowingStateChanged(InternetBorrowingRequest internetBorrowingRequest, int i) throws RemoteException;

    void onSharingStateChanged(InternetSharingRequest internetSharingRequest, int i) throws RemoteException;

    public static class Default implements IDistributedGatewayStateCallback {
        @Override // com.huawei.distributedgw.IDistributedGatewayStateCallback
        public void onSharingStateChanged(InternetSharingRequest request, int state) throws RemoteException {
        }

        @Override // com.huawei.distributedgw.IDistributedGatewayStateCallback
        public void onBorrowingStateChanged(InternetBorrowingRequest borrow, int state) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDistributedGatewayStateCallback {
        private static final String DESCRIPTOR = "com.huawei.distributedgw.IDistributedGatewayStateCallback";
        static final int TRANSACTION_onBorrowingStateChanged = 2;
        static final int TRANSACTION_onSharingStateChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDistributedGatewayStateCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDistributedGatewayStateCallback)) {
                return new Proxy(obj);
            }
            return (IDistributedGatewayStateCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            InternetSharingRequest _arg0;
            InternetBorrowingRequest _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = InternetSharingRequest.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onSharingStateChanged(_arg0, data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = InternetBorrowingRequest.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                onBorrowingStateChanged(_arg02, data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IDistributedGatewayStateCallback {
            public static IDistributedGatewayStateCallback sDefaultImpl;
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

            @Override // com.huawei.distributedgw.IDistributedGatewayStateCallback
            public void onSharingStateChanged(InternetSharingRequest request, int state) throws RemoteException {
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
                    _data.writeInt(state);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSharingStateChanged(request, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.distributedgw.IDistributedGatewayStateCallback
            public void onBorrowingStateChanged(InternetBorrowingRequest borrow, int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (borrow != null) {
                        _data.writeInt(1);
                        borrow.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(state);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onBorrowingStateChanged(borrow, state);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDistributedGatewayStateCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDistributedGatewayStateCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
