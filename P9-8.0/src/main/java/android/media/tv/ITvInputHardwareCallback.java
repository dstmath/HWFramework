package android.media.tv;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITvInputHardwareCallback extends IInterface {

    public static abstract class Stub extends Binder implements ITvInputHardwareCallback {
        private static final String DESCRIPTOR = "android.media.tv.ITvInputHardwareCallback";
        static final int TRANSACTION_onReleased = 1;
        static final int TRANSACTION_onStreamConfigChanged = 2;

        private static class Proxy implements ITvInputHardwareCallback {
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

            public void onReleased() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onStreamConfigChanged(TvStreamConfig[] configs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(configs, 0);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITvInputHardwareCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITvInputHardwareCallback)) {
                return new Proxy(obj);
            }
            return (ITvInputHardwareCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onReleased();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onStreamConfigChanged((TvStreamConfig[]) data.createTypedArray(TvStreamConfig.CREATOR));
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onReleased() throws RemoteException;

    void onStreamConfigChanged(TvStreamConfig[] tvStreamConfigArr) throws RemoteException;
}
