package android.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAppCallback extends IInterface {
    BeamShareData createBeamShareData(byte b) throws RemoteException;

    void onNdefPushComplete(byte b) throws RemoteException;

    void onTagDiscovered(Tag tag) throws RemoteException;

    public static class Default implements IAppCallback {
        @Override // android.nfc.IAppCallback
        public BeamShareData createBeamShareData(byte peerLlcpVersion) throws RemoteException {
            return null;
        }

        @Override // android.nfc.IAppCallback
        public void onNdefPushComplete(byte peerLlcpVersion) throws RemoteException {
        }

        @Override // android.nfc.IAppCallback
        public void onTagDiscovered(Tag tag) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAppCallback {
        private static final String DESCRIPTOR = "android.nfc.IAppCallback";
        static final int TRANSACTION_createBeamShareData = 1;
        static final int TRANSACTION_onNdefPushComplete = 2;
        static final int TRANSACTION_onTagDiscovered = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppCallback)) {
                return new Proxy(obj);
            }
            return (IAppCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "createBeamShareData";
            }
            if (transactionCode == 2) {
                return "onNdefPushComplete";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onTagDiscovered";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Tag _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                BeamShareData _result = createBeamShareData(data.readByte());
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onNdefPushComplete(data.readByte());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = Tag.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                onTagDiscovered(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAppCallback {
            public static IAppCallback sDefaultImpl;
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

            @Override // android.nfc.IAppCallback
            public BeamShareData createBeamShareData(byte peerLlcpVersion) throws RemoteException {
                BeamShareData _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByte(peerLlcpVersion);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createBeamShareData(peerLlcpVersion);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = BeamShareData.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.nfc.IAppCallback
            public void onNdefPushComplete(byte peerLlcpVersion) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByte(peerLlcpVersion);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNdefPushComplete(peerLlcpVersion);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.nfc.IAppCallback
            public void onTagDiscovered(Tag tag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tag != null) {
                        _data.writeInt(1);
                        tag.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTagDiscovered(tag);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAppCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAppCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
