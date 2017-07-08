package android.net;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IEthernetManager extends IInterface {

    public static abstract class Stub extends Binder implements IEthernetManager {
        private static final String DESCRIPTOR = "android.net.IEthernetManager";
        static final int TRANSACTION_addListener = 4;
        static final int TRANSACTION_getConfiguration = 1;
        static final int TRANSACTION_isAvailable = 3;
        static final int TRANSACTION_removeListener = 5;
        static final int TRANSACTION_setConfiguration = 2;

        private static class Proxy implements IEthernetManager {
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

            public IpConfiguration getConfiguration() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    IpConfiguration ipConfiguration;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getConfiguration, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        ipConfiguration = (IpConfiguration) IpConfiguration.CREATOR.createFromParcel(_reply);
                    } else {
                        ipConfiguration = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return ipConfiguration;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setConfiguration(IpConfiguration config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(Stub.TRANSACTION_getConfiguration);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_setConfiguration, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isAvailable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isAvailable, _data, _reply, 0);
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

            public void addListener(IEthernetServiceListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeListener(IEthernetServiceListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_removeListener, _data, _reply, 0);
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

        public static IEthernetManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IEthernetManager)) {
                return new Proxy(obj);
            }
            return (IEthernetManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            switch (code) {
                case TRANSACTION_getConfiguration /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    IpConfiguration _result = getConfiguration();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getConfiguration);
                        _result.writeToParcel(reply, TRANSACTION_getConfiguration);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_setConfiguration /*2*/:
                    IpConfiguration ipConfiguration;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        ipConfiguration = (IpConfiguration) IpConfiguration.CREATOR.createFromParcel(data);
                    } else {
                        ipConfiguration = null;
                    }
                    setConfiguration(ipConfiguration);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isAvailable /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result2 = isAvailable();
                    reply.writeNoException();
                    if (_result2) {
                        i = TRANSACTION_getConfiguration;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_addListener /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    addListener(android.net.IEthernetServiceListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeListener /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeListener(android.net.IEthernetServiceListener.Stub.asInterface(data.readStrongBinder()));
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

    void addListener(IEthernetServiceListener iEthernetServiceListener) throws RemoteException;

    IpConfiguration getConfiguration() throws RemoteException;

    boolean isAvailable() throws RemoteException;

    void removeListener(IEthernetServiceListener iEthernetServiceListener) throws RemoteException;

    void setConfiguration(IpConfiguration ipConfiguration) throws RemoteException;
}
