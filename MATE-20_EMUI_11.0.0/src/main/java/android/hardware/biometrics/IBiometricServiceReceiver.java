package android.hardware.biometrics;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBiometricServiceReceiver extends IInterface {
    void onAcquired(int i, String str) throws RemoteException;

    void onAuthenticationFailed() throws RemoteException;

    void onAuthenticationSucceeded() throws RemoteException;

    void onDialogDismissed(int i) throws RemoteException;

    void onError(int i, String str) throws RemoteException;

    public static class Default implements IBiometricServiceReceiver {
        @Override // android.hardware.biometrics.IBiometricServiceReceiver
        public void onAuthenticationSucceeded() throws RemoteException {
        }

        @Override // android.hardware.biometrics.IBiometricServiceReceiver
        public void onAuthenticationFailed() throws RemoteException {
        }

        @Override // android.hardware.biometrics.IBiometricServiceReceiver
        public void onError(int error, String message) throws RemoteException {
        }

        @Override // android.hardware.biometrics.IBiometricServiceReceiver
        public void onAcquired(int acquiredInfo, String message) throws RemoteException {
        }

        @Override // android.hardware.biometrics.IBiometricServiceReceiver
        public void onDialogDismissed(int reason) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBiometricServiceReceiver {
        private static final String DESCRIPTOR = "android.hardware.biometrics.IBiometricServiceReceiver";
        static final int TRANSACTION_onAcquired = 4;
        static final int TRANSACTION_onAuthenticationFailed = 2;
        static final int TRANSACTION_onAuthenticationSucceeded = 1;
        static final int TRANSACTION_onDialogDismissed = 5;
        static final int TRANSACTION_onError = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBiometricServiceReceiver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBiometricServiceReceiver)) {
                return new Proxy(obj);
            }
            return (IBiometricServiceReceiver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onAuthenticationSucceeded";
            }
            if (transactionCode == 2) {
                return "onAuthenticationFailed";
            }
            if (transactionCode == 3) {
                return "onError";
            }
            if (transactionCode == 4) {
                return "onAcquired";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "onDialogDismissed";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onAuthenticationSucceeded();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onAuthenticationFailed();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onError(data.readInt(), data.readString());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onAcquired(data.readInt(), data.readString());
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                onDialogDismissed(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IBiometricServiceReceiver {
            public static IBiometricServiceReceiver sDefaultImpl;
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

            @Override // android.hardware.biometrics.IBiometricServiceReceiver
            public void onAuthenticationSucceeded() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAuthenticationSucceeded();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.biometrics.IBiometricServiceReceiver
            public void onAuthenticationFailed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAuthenticationFailed();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.biometrics.IBiometricServiceReceiver
            public void onError(int error, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(error);
                    _data.writeString(message);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onError(error, message);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.biometrics.IBiometricServiceReceiver
            public void onAcquired(int acquiredInfo, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(acquiredInfo);
                    _data.writeString(message);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAcquired(acquiredInfo, message);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.biometrics.IBiometricServiceReceiver
            public void onDialogDismissed(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onDialogDismissed(reason);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IBiometricServiceReceiver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBiometricServiceReceiver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
