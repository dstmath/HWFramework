package android.telephony.mbms;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IMbmsGroupCallSessionCallback extends IInterface {
    void onAvailableSaisUpdated(List list, List list2) throws RemoteException;

    void onError(int i, String str) throws RemoteException;

    void onMiddlewareReady() throws RemoteException;

    void onServiceInterfaceAvailable(String str, int i) throws RemoteException;

    public static class Default implements IMbmsGroupCallSessionCallback {
        @Override // android.telephony.mbms.IMbmsGroupCallSessionCallback
        public void onError(int errorCode, String message) throws RemoteException {
        }

        @Override // android.telephony.mbms.IMbmsGroupCallSessionCallback
        public void onAvailableSaisUpdated(List currentSai, List availableSais) throws RemoteException {
        }

        @Override // android.telephony.mbms.IMbmsGroupCallSessionCallback
        public void onServiceInterfaceAvailable(String interfaceName, int index) throws RemoteException {
        }

        @Override // android.telephony.mbms.IMbmsGroupCallSessionCallback
        public void onMiddlewareReady() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMbmsGroupCallSessionCallback {
        private static final String DESCRIPTOR = "android.telephony.mbms.IMbmsGroupCallSessionCallback";
        static final int TRANSACTION_onAvailableSaisUpdated = 2;
        static final int TRANSACTION_onError = 1;
        static final int TRANSACTION_onMiddlewareReady = 4;
        static final int TRANSACTION_onServiceInterfaceAvailable = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMbmsGroupCallSessionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMbmsGroupCallSessionCallback)) {
                return new Proxy(obj);
            }
            return (IMbmsGroupCallSessionCallback) iin;
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
                return "onAvailableSaisUpdated";
            }
            if (transactionCode == 3) {
                return "onServiceInterfaceAvailable";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "onMiddlewareReady";
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
                ClassLoader cl = getClass().getClassLoader();
                onAvailableSaisUpdated(data.readArrayList(cl), data.readArrayList(cl));
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onServiceInterfaceAvailable(data.readString(), data.readInt());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onMiddlewareReady();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IMbmsGroupCallSessionCallback {
            public static IMbmsGroupCallSessionCallback sDefaultImpl;
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

            @Override // android.telephony.mbms.IMbmsGroupCallSessionCallback
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

            @Override // android.telephony.mbms.IMbmsGroupCallSessionCallback
            public void onAvailableSaisUpdated(List currentSai, List availableSais) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeList(currentSai);
                    _data.writeList(availableSais);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAvailableSaisUpdated(currentSai, availableSais);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.telephony.mbms.IMbmsGroupCallSessionCallback
            public void onServiceInterfaceAvailable(String interfaceName, int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(interfaceName);
                    _data.writeInt(index);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onServiceInterfaceAvailable(interfaceName, index);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.telephony.mbms.IMbmsGroupCallSessionCallback
            public void onMiddlewareReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onMiddlewareReady();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMbmsGroupCallSessionCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMbmsGroupCallSessionCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
