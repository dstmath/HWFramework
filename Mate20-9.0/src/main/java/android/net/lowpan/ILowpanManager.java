package android.net.lowpan;

import android.net.lowpan.ILowpanInterface;
import android.net.lowpan.ILowpanManagerListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILowpanManager extends IInterface {
    public static final String LOWPAN_SERVICE_NAME = "lowpan";

    public static abstract class Stub extends Binder implements ILowpanManager {
        private static final String DESCRIPTOR = "android.net.lowpan.ILowpanManager";
        static final int TRANSACTION_addInterface = 5;
        static final int TRANSACTION_addListener = 3;
        static final int TRANSACTION_getInterface = 1;
        static final int TRANSACTION_getInterfaceList = 2;
        static final int TRANSACTION_removeInterface = 6;
        static final int TRANSACTION_removeListener = 4;

        private static class Proxy implements ILowpanManager {
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

            public ILowpanInterface getInterface(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(name);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return ILowpanInterface.Stub.asInterface(_reply.readStrongBinder());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] getInterfaceList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addListener(ILowpanManagerListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeListener(ILowpanManagerListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addInterface(ILowpanInterface lowpan_interface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lowpan_interface != null ? lowpan_interface.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeInterface(ILowpanInterface lowpan_interface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lowpan_interface != null ? lowpan_interface.asBinder() : null);
                    this.mRemote.transact(6, _data, _reply, 0);
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

        public static ILowpanManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILowpanManager)) {
                return new Proxy(obj);
            }
            return (ILowpanManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        ILowpanInterface _result = getInterface(data.readString());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result != null ? _result.asBinder() : null);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String[] _result2 = getInterfaceList();
                        reply.writeNoException();
                        reply.writeStringArray(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        addListener(ILowpanManagerListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        removeListener(ILowpanManagerListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        addInterface(ILowpanInterface.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        removeInterface(ILowpanInterface.Stub.asInterface(data.readStrongBinder()));
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
    }

    void addInterface(ILowpanInterface iLowpanInterface) throws RemoteException;

    void addListener(ILowpanManagerListener iLowpanManagerListener) throws RemoteException;

    ILowpanInterface getInterface(String str) throws RemoteException;

    String[] getInterfaceList() throws RemoteException;

    void removeInterface(ILowpanInterface iLowpanInterface) throws RemoteException;

    void removeListener(ILowpanManagerListener iLowpanManagerListener) throws RemoteException;
}
