package android.hardware.input;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IInputDevicesChangedListener extends IInterface {

    public static abstract class Stub extends Binder implements IInputDevicesChangedListener {
        private static final String DESCRIPTOR = "android.hardware.input.IInputDevicesChangedListener";
        static final int TRANSACTION_onInputDevicesChanged = 1;

        private static class Proxy implements IInputDevicesChangedListener {
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

            public void onInputDevicesChanged(int[] deviceIdAndGeneration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(deviceIdAndGeneration);
                    this.mRemote.transact(Stub.TRANSACTION_onInputDevicesChanged, _data, null, Stub.TRANSACTION_onInputDevicesChanged);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IInputDevicesChangedListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IInputDevicesChangedListener)) {
                return new Proxy(obj);
            }
            return (IInputDevicesChangedListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onInputDevicesChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onInputDevicesChanged(data.createIntArray());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onInputDevicesChanged(int[] iArr) throws RemoteException;
}
