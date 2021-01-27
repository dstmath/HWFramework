package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWifiRepeaterConfirmListener extends IInterface {
    void onCancel() throws RemoteException;

    void onConfirm(int i, int i2) throws RemoteException;

    void onDisconnectP2p() throws RemoteException;

    void onRenameSsid() throws RemoteException;

    void onShareMobile() throws RemoteException;

    public static class Default implements IWifiRepeaterConfirmListener {
        @Override // android.net.wifi.IWifiRepeaterConfirmListener
        public void onConfirm(int confirmType, int value) throws RemoteException {
        }

        @Override // android.net.wifi.IWifiRepeaterConfirmListener
        public void onCancel() throws RemoteException {
        }

        @Override // android.net.wifi.IWifiRepeaterConfirmListener
        public void onDisconnectP2p() throws RemoteException {
        }

        @Override // android.net.wifi.IWifiRepeaterConfirmListener
        public void onShareMobile() throws RemoteException {
        }

        @Override // android.net.wifi.IWifiRepeaterConfirmListener
        public void onRenameSsid() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IWifiRepeaterConfirmListener {
        private static final String DESCRIPTOR = "android.net.wifi.IWifiRepeaterConfirmListener";
        static final int TRANSACTION_onCancel = 2;
        static final int TRANSACTION_onConfirm = 1;
        static final int TRANSACTION_onDisconnectP2p = 3;
        static final int TRANSACTION_onRenameSsid = 5;
        static final int TRANSACTION_onShareMobile = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiRepeaterConfirmListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiRepeaterConfirmListener)) {
                return new Proxy(obj);
            }
            return (IWifiRepeaterConfirmListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onConfirm";
            }
            if (transactionCode == 2) {
                return "onCancel";
            }
            if (transactionCode == 3) {
                return "onDisconnectP2p";
            }
            if (transactionCode == 4) {
                return "onShareMobile";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "onRenameSsid";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onConfirm(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onCancel();
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onDisconnectP2p();
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onShareMobile();
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                onRenameSsid();
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
        public static class Proxy implements IWifiRepeaterConfirmListener {
            public static IWifiRepeaterConfirmListener sDefaultImpl;
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

            @Override // android.net.wifi.IWifiRepeaterConfirmListener
            public void onConfirm(int confirmType, int value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(confirmType);
                    _data.writeInt(value);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onConfirm(confirmType, value);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiRepeaterConfirmListener
            public void onCancel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCancel();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiRepeaterConfirmListener
            public void onDisconnectP2p() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDisconnectP2p();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiRepeaterConfirmListener
            public void onShareMobile() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onShareMobile();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IWifiRepeaterConfirmListener
            public void onRenameSsid() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRenameSsid();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IWifiRepeaterConfirmListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IWifiRepeaterConfirmListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
