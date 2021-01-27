package android.hardware;

import android.hardware.ISensorPrivacyListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISensorPrivacyManager extends IInterface {
    void addSensorPrivacyListener(ISensorPrivacyListener iSensorPrivacyListener) throws RemoteException;

    boolean isSensorPrivacyEnabled() throws RemoteException;

    void removeSensorPrivacyListener(ISensorPrivacyListener iSensorPrivacyListener) throws RemoteException;

    void setSensorPrivacy(boolean z) throws RemoteException;

    public static class Default implements ISensorPrivacyManager {
        @Override // android.hardware.ISensorPrivacyManager
        public void addSensorPrivacyListener(ISensorPrivacyListener listener) throws RemoteException {
        }

        @Override // android.hardware.ISensorPrivacyManager
        public void removeSensorPrivacyListener(ISensorPrivacyListener listener) throws RemoteException {
        }

        @Override // android.hardware.ISensorPrivacyManager
        public boolean isSensorPrivacyEnabled() throws RemoteException {
            return false;
        }

        @Override // android.hardware.ISensorPrivacyManager
        public void setSensorPrivacy(boolean enable) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISensorPrivacyManager {
        private static final String DESCRIPTOR = "android.hardware.ISensorPrivacyManager";
        static final int TRANSACTION_addSensorPrivacyListener = 1;
        static final int TRANSACTION_isSensorPrivacyEnabled = 3;
        static final int TRANSACTION_removeSensorPrivacyListener = 2;
        static final int TRANSACTION_setSensorPrivacy = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISensorPrivacyManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISensorPrivacyManager)) {
                return new Proxy(obj);
            }
            return (ISensorPrivacyManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "addSensorPrivacyListener";
            }
            if (transactionCode == 2) {
                return "removeSensorPrivacyListener";
            }
            if (transactionCode == 3) {
                return "isSensorPrivacyEnabled";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "setSensorPrivacy";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                addSensorPrivacyListener(ISensorPrivacyListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                removeSensorPrivacyListener(ISensorPrivacyListener.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean isSensorPrivacyEnabled = isSensorPrivacyEnabled();
                reply.writeNoException();
                reply.writeInt(isSensorPrivacyEnabled ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                setSensorPrivacy(data.readInt() != 0);
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
        public static class Proxy implements ISensorPrivacyManager {
            public static ISensorPrivacyManager sDefaultImpl;
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

            @Override // android.hardware.ISensorPrivacyManager
            public void addSensorPrivacyListener(ISensorPrivacyListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addSensorPrivacyListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ISensorPrivacyManager
            public void removeSensorPrivacyListener(ISensorPrivacyListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeSensorPrivacyListener(listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.ISensorPrivacyManager
            public boolean isSensorPrivacyEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isSensorPrivacyEnabled();
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

            @Override // android.hardware.ISensorPrivacyManager
            public void setSensorPrivacy(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setSensorPrivacy(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISensorPrivacyManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISensorPrivacyManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
