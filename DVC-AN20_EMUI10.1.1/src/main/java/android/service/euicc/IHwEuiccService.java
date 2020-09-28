package android.service.euicc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.service.euicc.IHwGetSmdsAddressCallback;
import android.service.euicc.IHwResetMemoryCallback;
import android.service.euicc.IHwSetDefaultSmdpAddressCallback;

public interface IHwEuiccService extends IInterface {
    void cancelSession() throws RemoteException;

    void requestDefaultSmdpAddress(String str, IHwGetSmdsAddressCallback iHwGetSmdsAddressCallback) throws RemoteException;

    void resetMemory(String str, int i, IHwResetMemoryCallback iHwResetMemoryCallback) throws RemoteException;

    void setDefaultSmdpAddress(String str, String str2, IHwSetDefaultSmdpAddressCallback iHwSetDefaultSmdpAddressCallback) throws RemoteException;

    public static class Default implements IHwEuiccService {
        @Override // android.service.euicc.IHwEuiccService
        public void requestDefaultSmdpAddress(String cardId, IHwGetSmdsAddressCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IHwEuiccService
        public void resetMemory(String cardId, int options, IHwResetMemoryCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IHwEuiccService
        public void setDefaultSmdpAddress(String cardId, String address, IHwSetDefaultSmdpAddressCallback callback) throws RemoteException {
        }

        @Override // android.service.euicc.IHwEuiccService
        public void cancelSession() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwEuiccService {
        private static final String DESCRIPTOR = "android.service.euicc.IHwEuiccService";
        static final int TRANSACTION_cancelSession = 4;
        static final int TRANSACTION_requestDefaultSmdpAddress = 1;
        static final int TRANSACTION_resetMemory = 2;
        static final int TRANSACTION_setDefaultSmdpAddress = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwEuiccService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwEuiccService)) {
                return new Proxy(obj);
            }
            return (IHwEuiccService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "requestDefaultSmdpAddress";
            }
            if (transactionCode == 2) {
                return "resetMemory";
            }
            if (transactionCode == 3) {
                return "setDefaultSmdpAddress";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "cancelSession";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                requestDefaultSmdpAddress(data.readString(), IHwGetSmdsAddressCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                resetMemory(data.readString(), data.readInt(), IHwResetMemoryCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                setDefaultSmdpAddress(data.readString(), data.readString(), IHwSetDefaultSmdpAddressCallback.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                cancelSession();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwEuiccService {
            public static IHwEuiccService sDefaultImpl;
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

            @Override // android.service.euicc.IHwEuiccService
            public void requestDefaultSmdpAddress(String cardId, IHwGetSmdsAddressCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cardId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().requestDefaultSmdpAddress(cardId, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IHwEuiccService
            public void resetMemory(String cardId, int options, IHwResetMemoryCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cardId);
                    _data.writeInt(options);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().resetMemory(cardId, options, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IHwEuiccService
            public void setDefaultSmdpAddress(String cardId, String address, IHwSetDefaultSmdpAddressCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(cardId);
                    _data.writeString(address);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setDefaultSmdpAddress(cardId, address, callback);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.service.euicc.IHwEuiccService
            public void cancelSession() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().cancelSession();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwEuiccService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwEuiccService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
