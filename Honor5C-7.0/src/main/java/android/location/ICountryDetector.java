package android.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICountryDetector extends IInterface {

    public static abstract class Stub extends Binder implements ICountryDetector {
        private static final String DESCRIPTOR = "android.location.ICountryDetector";
        static final int TRANSACTION_addCountryListener = 2;
        static final int TRANSACTION_detectCountry = 1;
        static final int TRANSACTION_removeCountryListener = 3;

        private static class Proxy implements ICountryDetector {
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

            public Country detectCountry() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Country country;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_detectCountry, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        country = (Country) Country.CREATOR.createFromParcel(_reply);
                    } else {
                        country = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return country;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addCountryListener(ICountryListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addCountryListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeCountryListener(ICountryListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeCountryListener, _data, _reply, 0);
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

        public static ICountryDetector asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICountryDetector)) {
                return new Proxy(obj);
            }
            return (ICountryDetector) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_detectCountry /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    Country _result = detectCountry();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_detectCountry);
                        _result.writeToParcel(reply, TRANSACTION_detectCountry);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_addCountryListener /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    addCountryListener(android.location.ICountryListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeCountryListener /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeCountryListener(android.location.ICountryListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addCountryListener(ICountryListener iCountryListener) throws RemoteException;

    Country detectCountry() throws RemoteException;

    void removeCountryListener(ICountryListener iCountryListener) throws RemoteException;
}
