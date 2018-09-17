package android.vrsystem;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVRSystemService extends IInterface {

    public static abstract class Stub extends Binder implements IVRSystemService {
        private static final String DESCRIPTOR = "android.vrsystem.IVRSystemService";
        static final int TRANSACTION_acceptInCall = 5;
        static final int TRANSACTION_addVRListener = 3;
        static final int TRANSACTION_deleteVRListener = 4;
        static final int TRANSACTION_endInCall = 6;
        static final int TRANSACTION_getContactName = 2;
        static final int TRANSACTION_isVRmode = 1;

        private static class Proxy implements IVRSystemService {
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

            public boolean isVRmode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_isVRmode, _data, _reply, 0);
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

            public String getContactName(String num) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(num);
                    this.mRemote.transact(Stub.TRANSACTION_getContactName, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addVRListener(IVRListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addVRListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void deleteVRListener(IVRListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_deleteVRListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void acceptInCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_acceptInCall, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void endInCall() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_endInCall, _data, _reply, 0);
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

        public static IVRSystemService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVRSystemService)) {
                return new Proxy(obj);
            }
            return (IVRSystemService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_isVRmode /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = isVRmode();
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_isVRmode : 0);
                    return true;
                case TRANSACTION_getContactName /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    String _result2 = getContactName(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case TRANSACTION_addVRListener /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    addVRListener(android.vrsystem.IVRListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_deleteVRListener /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    deleteVRListener(android.vrsystem.IVRListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_acceptInCall /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    acceptInCall();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_endInCall /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    endInCall();
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

    void acceptInCall() throws RemoteException;

    void addVRListener(IVRListener iVRListener) throws RemoteException;

    void deleteVRListener(IVRListener iVRListener) throws RemoteException;

    void endInCall() throws RemoteException;

    String getContactName(String str) throws RemoteException;

    boolean isVRmode() throws RemoteException;
}
