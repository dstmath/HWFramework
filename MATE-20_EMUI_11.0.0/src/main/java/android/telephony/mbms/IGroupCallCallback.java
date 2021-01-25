package android.telephony.mbms;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGroupCallCallback extends IInterface {
    void onBroadcastSignalStrengthUpdated(int i) throws RemoteException;

    void onError(int i, String str) throws RemoteException;

    void onGroupCallStateChanged(int i, int i2) throws RemoteException;

    public static class Default implements IGroupCallCallback {
        @Override // android.telephony.mbms.IGroupCallCallback
        public void onError(int errorCode, String message) throws RemoteException {
        }

        @Override // android.telephony.mbms.IGroupCallCallback
        public void onGroupCallStateChanged(int state, int reason) throws RemoteException {
        }

        @Override // android.telephony.mbms.IGroupCallCallback
        public void onBroadcastSignalStrengthUpdated(int signalStrength) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGroupCallCallback {
        private static final String DESCRIPTOR = "android.telephony.mbms.IGroupCallCallback";
        static final int TRANSACTION_onBroadcastSignalStrengthUpdated = 3;
        static final int TRANSACTION_onError = 1;
        static final int TRANSACTION_onGroupCallStateChanged = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGroupCallCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGroupCallCallback)) {
                return new Proxy(obj);
            }
            return (IGroupCallCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onError";
            }
            if (transactionCode == 2) {
                return "onGroupCallStateChanged";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "onBroadcastSignalStrengthUpdated";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onError(data.readInt(), data.readString());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onGroupCallStateChanged(data.readInt(), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onBroadcastSignalStrengthUpdated(data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGroupCallCallback {
            public static IGroupCallCallback sDefaultImpl;
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

            @Override // android.telephony.mbms.IGroupCallCallback
            public void onError(int errorCode, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    _data.writeString(message);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onError(errorCode, message);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.telephony.mbms.IGroupCallCallback
            public void onGroupCallStateChanged(int state, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    _data.writeInt(reason);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGroupCallStateChanged(state, reason);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.telephony.mbms.IGroupCallCallback
            public void onBroadcastSignalStrengthUpdated(int signalStrength) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(signalStrength);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onBroadcastSignalStrengthUpdated(signalStrength);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGroupCallCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGroupCallCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
