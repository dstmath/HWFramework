package android.telephony.mbms;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IMbmsStreamingSessionCallback extends IInterface {
    void onError(int i, String str) throws RemoteException;

    void onMiddlewareReady() throws RemoteException;

    void onStreamingServicesUpdated(List<StreamingServiceInfo> list) throws RemoteException;

    public static class Default implements IMbmsStreamingSessionCallback {
        @Override // android.telephony.mbms.IMbmsStreamingSessionCallback
        public void onError(int errorCode, String message) throws RemoteException {
        }

        @Override // android.telephony.mbms.IMbmsStreamingSessionCallback
        public void onStreamingServicesUpdated(List<StreamingServiceInfo> list) throws RemoteException {
        }

        @Override // android.telephony.mbms.IMbmsStreamingSessionCallback
        public void onMiddlewareReady() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IMbmsStreamingSessionCallback {
        private static final String DESCRIPTOR = "android.telephony.mbms.IMbmsStreamingSessionCallback";
        static final int TRANSACTION_onError = 1;
        static final int TRANSACTION_onMiddlewareReady = 3;
        static final int TRANSACTION_onStreamingServicesUpdated = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMbmsStreamingSessionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMbmsStreamingSessionCallback)) {
                return new Proxy(obj);
            }
            return (IMbmsStreamingSessionCallback) iin;
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
                return "onStreamingServicesUpdated";
            }
            if (transactionCode != 3) {
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
                onStreamingServicesUpdated(data.createTypedArrayList(StreamingServiceInfo.CREATOR));
                return true;
            } else if (code == 3) {
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
        public static class Proxy implements IMbmsStreamingSessionCallback {
            public static IMbmsStreamingSessionCallback sDefaultImpl;
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

            @Override // android.telephony.mbms.IMbmsStreamingSessionCallback
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

            @Override // android.telephony.mbms.IMbmsStreamingSessionCallback
            public void onStreamingServicesUpdated(List<StreamingServiceInfo> services) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(services);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStreamingServicesUpdated(services);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.telephony.mbms.IMbmsStreamingSessionCallback
            public void onMiddlewareReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onMiddlewareReady();
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IMbmsStreamingSessionCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IMbmsStreamingSessionCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
