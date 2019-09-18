package android.telephony.mbms;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IMbmsDownloadSessionCallback extends IInterface {

    public static abstract class Stub extends Binder implements IMbmsDownloadSessionCallback {
        private static final String DESCRIPTOR = "android.telephony.mbms.IMbmsDownloadSessionCallback";
        static final int TRANSACTION_onError = 1;
        static final int TRANSACTION_onFileServicesUpdated = 2;
        static final int TRANSACTION_onMiddlewareReady = 3;

        private static class Proxy implements IMbmsDownloadSessionCallback {
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

            public void onError(int errorCode, String message) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    _data.writeString(message);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onFileServicesUpdated(List<FileServiceInfo> services) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(services);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMiddlewareReady() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IMbmsDownloadSessionCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IMbmsDownloadSessionCallback)) {
                return new Proxy(obj);
            }
            return (IMbmsDownloadSessionCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onError(data.readInt(), data.readString());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onFileServicesUpdated(data.createTypedArrayList(FileServiceInfo.CREATOR));
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onMiddlewareReady();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onError(int i, String str) throws RemoteException;

    void onFileServicesUpdated(List<FileServiceInfo> list) throws RemoteException;

    void onMiddlewareReady() throws RemoteException;
}
