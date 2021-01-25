package com.huawei.distributedgw;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.distributedgw.IDistributedGatewayStateCallback;

public interface IDistributedGatewayService extends IInterface {
    boolean disableInternetSharing(InternetSharingRequest internetSharingRequest) throws RemoteException;

    boolean enableInternetSharing(InternetSharingRequest internetSharingRequest) throws RemoteException;

    boolean isInternetSharing(InternetSharingRequest internetSharingRequest) throws RemoteException;

    void regStateCallback(IDistributedGatewayStateCallback iDistributedGatewayStateCallback) throws RemoteException;

    void unregStateCallback(IDistributedGatewayStateCallback iDistributedGatewayStateCallback) throws RemoteException;

    public static class Default implements IDistributedGatewayService {
        @Override // com.huawei.distributedgw.IDistributedGatewayService
        public boolean enableInternetSharing(InternetSharingRequest request) throws RemoteException {
            return false;
        }

        @Override // com.huawei.distributedgw.IDistributedGatewayService
        public boolean disableInternetSharing(InternetSharingRequest request) throws RemoteException {
            return false;
        }

        @Override // com.huawei.distributedgw.IDistributedGatewayService
        public boolean isInternetSharing(InternetSharingRequest request) throws RemoteException {
            return false;
        }

        @Override // com.huawei.distributedgw.IDistributedGatewayService
        public void regStateCallback(IDistributedGatewayStateCallback callback) throws RemoteException {
        }

        @Override // com.huawei.distributedgw.IDistributedGatewayService
        public void unregStateCallback(IDistributedGatewayStateCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IDistributedGatewayService {
        private static final String DESCRIPTOR = "com.huawei.distributedgw.IDistributedGatewayService";
        static final int TRANSACTION_disableInternetSharing = 2;
        static final int TRANSACTION_enableInternetSharing = 1;
        static final int TRANSACTION_isInternetSharing = 3;
        static final int TRANSACTION_regStateCallback = 4;
        static final int TRANSACTION_unregStateCallback = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IDistributedGatewayService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IDistributedGatewayService)) {
                return new Proxy(obj);
            }
            return (IDistributedGatewayService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            InternetSharingRequest _arg0;
            InternetSharingRequest _arg02;
            InternetSharingRequest _arg03;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = InternetSharingRequest.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                boolean enableInternetSharing = enableInternetSharing(_arg0);
                reply.writeNoException();
                reply.writeInt(enableInternetSharing ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = InternetSharingRequest.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                boolean disableInternetSharing = disableInternetSharing(_arg02);
                reply.writeNoException();
                reply.writeInt(disableInternetSharing ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = InternetSharingRequest.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                boolean isInternetSharing = isInternetSharing(_arg03);
                reply.writeNoException();
                reply.writeInt(isInternetSharing ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                regStateCallback(IDistributedGatewayStateCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                unregStateCallback(IDistributedGatewayStateCallback.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IDistributedGatewayService {
            public static IDistributedGatewayService sDefaultImpl;
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

            @Override // com.huawei.distributedgw.IDistributedGatewayService
            public boolean enableInternetSharing(InternetSharingRequest request) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().enableInternetSharing(request);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.distributedgw.IDistributedGatewayService
            public boolean disableInternetSharing(InternetSharingRequest request) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().disableInternetSharing(request);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.distributedgw.IDistributedGatewayService
            public boolean isInternetSharing(InternetSharingRequest request) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isInternetSharing(request);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.distributedgw.IDistributedGatewayService
            public void regStateCallback(IDistributedGatewayStateCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().regStateCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.distributedgw.IDistributedGatewayService
            public void unregStateCallback(IDistributedGatewayStateCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregStateCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IDistributedGatewayService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IDistributedGatewayService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
