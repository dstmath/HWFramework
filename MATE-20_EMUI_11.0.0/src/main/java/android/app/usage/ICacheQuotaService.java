package android.app.usage;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteCallback;
import android.os.RemoteException;
import java.util.List;

public interface ICacheQuotaService extends IInterface {
    void computeCacheQuotaHints(RemoteCallback remoteCallback, List<CacheQuotaHint> list) throws RemoteException;

    public static class Default implements ICacheQuotaService {
        @Override // android.app.usage.ICacheQuotaService
        public void computeCacheQuotaHints(RemoteCallback callback, List<CacheQuotaHint> list) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICacheQuotaService {
        private static final String DESCRIPTOR = "android.app.usage.ICacheQuotaService";
        static final int TRANSACTION_computeCacheQuotaHints = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICacheQuotaService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICacheQuotaService)) {
                return new Proxy(obj);
            }
            return (ICacheQuotaService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "computeCacheQuotaHints";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RemoteCallback _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = RemoteCallback.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                computeCacheQuotaHints(_arg0, data.createTypedArrayList(CacheQuotaHint.CREATOR));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ICacheQuotaService {
            public static ICacheQuotaService sDefaultImpl;
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

            @Override // android.app.usage.ICacheQuotaService
            public void computeCacheQuotaHints(RemoteCallback callback, List<CacheQuotaHint> requests) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeTypedList(requests);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().computeCacheQuotaHints(callback, requests);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICacheQuotaService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICacheQuotaService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
