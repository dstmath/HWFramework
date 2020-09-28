package android.emcom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHandoffServiceCallback extends IInterface {
    int batchRegisterHandoff(String str) throws RemoteException;

    int realRegisterHandoff(String str, int i) throws RemoteException;

    int realUnRegisterHandoff(String str, int i) throws RemoteException;

    public static class Default implements IHandoffServiceCallback {
        @Override // android.emcom.IHandoffServiceCallback
        public int realRegisterHandoff(String packageName, int dataType) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IHandoffServiceCallback
        public int realUnRegisterHandoff(String packageName, int dataType) throws RemoteException {
            return 0;
        }

        @Override // android.emcom.IHandoffServiceCallback
        public int batchRegisterHandoff(String handoffRegisterInfo) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHandoffServiceCallback {
        private static final String DESCRIPTOR = "android.emcom.IHandoffServiceCallback";
        static final int TRANSACTION_batchRegisterHandoff = 3;
        static final int TRANSACTION_realRegisterHandoff = 1;
        static final int TRANSACTION_realUnRegisterHandoff = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHandoffServiceCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHandoffServiceCallback)) {
                return new Proxy(obj);
            }
            return (IHandoffServiceCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = realRegisterHandoff(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = realUnRegisterHandoff(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = batchRegisterHandoff(data.readString());
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHandoffServiceCallback {
            public static IHandoffServiceCallback sDefaultImpl;
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

            @Override // android.emcom.IHandoffServiceCallback
            public int realRegisterHandoff(String packageName, int dataType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(dataType);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().realRegisterHandoff(packageName, dataType);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IHandoffServiceCallback
            public int realUnRegisterHandoff(String packageName, int dataType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(dataType);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().realUnRegisterHandoff(packageName, dataType);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.emcom.IHandoffServiceCallback
            public int batchRegisterHandoff(String handoffRegisterInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(handoffRegisterInfo);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().batchRegisterHandoff(handoffRegisterInfo);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHandoffServiceCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHandoffServiceCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
