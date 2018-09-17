package android.app;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INonHardwareAcceleratedPackagesManager extends IInterface {

    public static abstract class Stub extends Binder implements INonHardwareAcceleratedPackagesManager {
        private static final String DESCRIPTOR = "android.app.INonHardwareAcceleratedPackagesManager";
        static final int TRANSACTION_getForceEnabled = 2;
        static final int TRANSACTION_hasPackage = 3;
        static final int TRANSACTION_removePackage = 4;
        static final int TRANSACTION_setForceEnabled = 1;

        private static class Proxy implements INonHardwareAcceleratedPackagesManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void setForceEnabled(String pkgName, boolean force) throws RemoteException {
                int i = Stub.TRANSACTION_setForceEnabled;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!force) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setForceEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getForceEnabled(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(Stub.TRANSACTION_getForceEnabled, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean hasPackage(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(Stub.TRANSACTION_hasPackage, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePackage(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    this.mRemote.transact(Stub.TRANSACTION_removePackage, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INonHardwareAcceleratedPackagesManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INonHardwareAcceleratedPackagesManager)) {
                return new Proxy(obj);
            }
            return (INonHardwareAcceleratedPackagesManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            boolean _result;
            switch (code) {
                case TRANSACTION_setForceEnabled /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setForceEnabled(data.readString(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getForceEnabled /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getForceEnabled(data.readString());
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_setForceEnabled;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_hasPackage /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = hasPackage(data.readString());
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_setForceEnabled;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_removePackage /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    removePackage(data.readString());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean getForceEnabled(String str) throws RemoteException;

    boolean hasPackage(String str) throws RemoteException;

    void removePackage(String str) throws RemoteException;

    void setForceEnabled(String str, boolean z) throws RemoteException;
}
