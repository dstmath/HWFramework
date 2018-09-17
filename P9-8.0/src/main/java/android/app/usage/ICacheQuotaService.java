package android.app.usage;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteCallback;
import android.os.RemoteException;
import java.util.List;

public interface ICacheQuotaService extends IInterface {

    public static abstract class Stub extends Binder implements ICacheQuotaService {
        private static final String DESCRIPTOR = "android.app.usage.ICacheQuotaService";
        static final int TRANSACTION_computeCacheQuotaHints = 1;

        private static class Proxy implements ICacheQuotaService {
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
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    RemoteCallback _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (RemoteCallback) RemoteCallback.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    computeCacheQuotaHints(_arg0, data.createTypedArrayList(CacheQuotaHint.CREATOR));
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void computeCacheQuotaHints(RemoteCallback remoteCallback, List<CacheQuotaHint> list) throws RemoteException;
}
