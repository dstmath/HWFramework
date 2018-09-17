package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IANQPDoneCallback extends IInterface {

    public static abstract class Stub extends Binder implements IANQPDoneCallback {
        private static final String DESCRIPTOR = "android.net.wifi.IANQPDoneCallback";
        static final int TRANSACTION_OnANQPDone = 1;

        private static class Proxy implements IANQPDoneCallback {
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

            public void OnANQPDone(byte[] bssid, byte[] anqp_result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(bssid);
                    _data.writeByteArray(anqp_result);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IANQPDoneCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IANQPDoneCallback)) {
                return new Proxy(obj);
            }
            return (IANQPDoneCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    OnANQPDone(data.createByteArray(), data.createByteArray());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void OnANQPDone(byte[] bArr, byte[] bArr2) throws RemoteException;
}
