package android.hardware.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IActivityRecognitionHardwareClient extends IInterface {

    public static abstract class Stub extends Binder implements IActivityRecognitionHardwareClient {
        private static final String DESCRIPTOR = "android.hardware.location.IActivityRecognitionHardwareClient";
        static final int TRANSACTION_onAvailabilityChanged = 1;

        private static class Proxy implements IActivityRecognitionHardwareClient {
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

            public void onAvailabilityChanged(boolean isSupported, IActivityRecognitionHardware instance) throws RemoteException {
                IBinder iBinder = null;
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!isSupported) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (instance != null) {
                        iBinder = instance.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IActivityRecognitionHardwareClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IActivityRecognitionHardwareClient)) {
                return new Proxy(obj);
            }
            return (IActivityRecognitionHardwareClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onAvailabilityChanged(data.readInt() != 0, android.hardware.location.IActivityRecognitionHardware.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onAvailabilityChanged(boolean z, IActivityRecognitionHardware iActivityRecognitionHardware) throws RemoteException;
}
