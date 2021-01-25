package android.se.omapi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.se.omapi.ISecureElementChannel;
import android.se.omapi.ISecureElementListener;

public interface ISecureElementSession extends IInterface {
    void close() throws RemoteException;

    void closeChannels() throws RemoteException;

    byte[] getAtr() throws RemoteException;

    boolean isClosed() throws RemoteException;

    ISecureElementChannel openBasicChannel(byte[] bArr, byte b, ISecureElementListener iSecureElementListener) throws RemoteException;

    ISecureElementChannel openLogicalChannel(byte[] bArr, byte b, ISecureElementListener iSecureElementListener) throws RemoteException;

    public static class Default implements ISecureElementSession {
        @Override // android.se.omapi.ISecureElementSession
        public byte[] getAtr() throws RemoteException {
            return null;
        }

        @Override // android.se.omapi.ISecureElementSession
        public void close() throws RemoteException {
        }

        @Override // android.se.omapi.ISecureElementSession
        public void closeChannels() throws RemoteException {
        }

        @Override // android.se.omapi.ISecureElementSession
        public boolean isClosed() throws RemoteException {
            return false;
        }

        @Override // android.se.omapi.ISecureElementSession
        public ISecureElementChannel openBasicChannel(byte[] aid, byte p2, ISecureElementListener listener) throws RemoteException {
            return null;
        }

        @Override // android.se.omapi.ISecureElementSession
        public ISecureElementChannel openLogicalChannel(byte[] aid, byte p2, ISecureElementListener listener) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ISecureElementSession {
        private static final String DESCRIPTOR = "android.se.omapi.ISecureElementSession";
        static final int TRANSACTION_close = 2;
        static final int TRANSACTION_closeChannels = 3;
        static final int TRANSACTION_getAtr = 1;
        static final int TRANSACTION_isClosed = 4;
        static final int TRANSACTION_openBasicChannel = 5;
        static final int TRANSACTION_openLogicalChannel = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ISecureElementSession asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ISecureElementSession)) {
                return new Proxy(obj);
            }
            return (ISecureElementSession) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getAtr";
                case 2:
                    return "close";
                case 3:
                    return "closeChannels";
                case 4:
                    return "isClosed";
                case 5:
                    return "openBasicChannel";
                case 6:
                    return "openLogicalChannel";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                IBinder iBinder = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result = getAtr();
                        reply.writeNoException();
                        reply.writeByteArray(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        close();
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        closeChannels();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isClosed = isClosed();
                        reply.writeNoException();
                        reply.writeInt(isClosed ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        ISecureElementChannel _result2 = openBasicChannel(data.createByteArray(), data.readByte(), ISecureElementListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result2 != null) {
                            iBinder = _result2.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        ISecureElementChannel _result3 = openLogicalChannel(data.createByteArray(), data.readByte(), ISecureElementListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        if (_result3 != null) {
                            iBinder = _result3.asBinder();
                        }
                        reply.writeStrongBinder(iBinder);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ISecureElementSession {
            public static ISecureElementSession sDefaultImpl;
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

            @Override // android.se.omapi.ISecureElementSession
            public byte[] getAtr() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getAtr();
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.se.omapi.ISecureElementSession
            public void close() throws RemoteException {
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
                    Stub.getDefaultImpl().close();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.se.omapi.ISecureElementSession
            public void closeChannels() throws RemoteException {
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
                    Stub.getDefaultImpl().closeChannels();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.se.omapi.ISecureElementSession
            public boolean isClosed() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isClosed();
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

            @Override // android.se.omapi.ISecureElementSession
            public ISecureElementChannel openBasicChannel(byte[] aid, byte p2, ISecureElementListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(aid);
                    _data.writeByte(p2);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openBasicChannel(aid, p2, listener);
                    }
                    _reply.readException();
                    ISecureElementChannel _result = ISecureElementChannel.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.se.omapi.ISecureElementSession
            public ISecureElementChannel openLogicalChannel(byte[] aid, byte p2, ISecureElementListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(aid);
                    _data.writeByte(p2);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().openLogicalChannel(aid, p2, listener);
                    }
                    _reply.readException();
                    ISecureElementChannel _result = ISecureElementChannel.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ISecureElementSession impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ISecureElementSession getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
