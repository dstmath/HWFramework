package android.media;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMediaCasService extends IInterface {

    public static abstract class Stub extends Binder implements IMediaCasService {
        private static final String DESCRIPTOR = "android.media.IMediaCasService";
        static final int TRANSACTION_createDescrambler = 5;
        static final int TRANSACTION_createPlugin = 3;
        static final int TRANSACTION_enumeratePlugins = 1;
        static final int TRANSACTION_isDescramblerSupported = 4;
        static final int TRANSACTION_isSystemIdSupported = 2;

        private static class Proxy implements IMediaCasService {
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

            public ParcelableCasPluginDescriptor[] enumeratePlugins() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    ParcelableCasPluginDescriptor[] _result = (ParcelableCasPluginDescriptor[]) _reply.createTypedArray(ParcelableCasPluginDescriptor.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isSystemIdSupported(int CA_system_id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(CA_system_id);
                    this.mRemote.transact(2, _data, _reply, 0);
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

            public ICas createPlugin(int CA_system_id, ICasListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(CA_system_id);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    ICas _result = android.media.ICas.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isDescramblerSupported(int CA_system_id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(CA_system_id);
                    this.mRemote.transact(4, _data, _reply, 0);
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

            public IDescrambler createDescrambler(int CA_system_id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(CA_system_id);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    IDescrambler _result = android.media.IDescrambler.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMediaCasService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMediaCasService)) {
                return new Proxy(obj);
            }
            return (IMediaCasService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            IBinder iBinder = null;
            boolean _result;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    ParcelableCasPluginDescriptor[] _result2 = enumeratePlugins();
                    reply.writeNoException();
                    reply.writeTypedArray(_result2, 1);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isSystemIdSupported(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 3:
                    IBinder asBinder;
                    data.enforceInterface(DESCRIPTOR);
                    ICas _result3 = createPlugin(data.readInt(), android.media.ICasListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result3 != null) {
                        asBinder = _result3.asBinder();
                    } else {
                        asBinder = null;
                    }
                    reply.writeStrongBinder(asBinder);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result = isDescramblerSupported(data.readInt());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    IDescrambler _result4 = createDescrambler(data.readInt());
                    reply.writeNoException();
                    if (_result4 != null) {
                        iBinder = _result4.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    IDescrambler createDescrambler(int i) throws RemoteException;

    ICas createPlugin(int i, ICasListener iCasListener) throws RemoteException;

    ParcelableCasPluginDescriptor[] enumeratePlugins() throws RemoteException;

    boolean isDescramblerSupported(int i) throws RemoteException;

    boolean isSystemIdSupported(int i) throws RemoteException;
}
