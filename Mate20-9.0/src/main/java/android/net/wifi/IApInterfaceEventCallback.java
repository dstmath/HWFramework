package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IApInterfaceEventCallback extends IInterface {
    public static final int BANDWIDTH_160 = 6;
    public static final int BANDWIDTH_20 = 2;
    public static final int BANDWIDTH_20_NOHT = 1;
    public static final int BANDWIDTH_40 = 3;
    public static final int BANDWIDTH_80 = 4;
    public static final int BANDWIDTH_80P80 = 5;
    public static final int BANDWIDTH_INVALID = 0;

    public static abstract class Stub extends Binder implements IApInterfaceEventCallback {
        private static final String DESCRIPTOR = "android.net.wifi.IApInterfaceEventCallback";
        static final int TRANSACTION_OnApLinkedStaJoin = 3;
        static final int TRANSACTION_OnApLinkedStaLeave = 4;
        static final int TRANSACTION_onNumAssociatedStationsChanged = 1;
        static final int TRANSACTION_onSoftApChannelSwitched = 2;

        private static class Proxy implements IApInterfaceEventCallback {
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

            public void onNumAssociatedStationsChanged(int numStations) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(numStations);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSoftApChannelSwitched(int frequency, int bandwidth) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(frequency);
                    _data.writeInt(bandwidth);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void OnApLinkedStaJoin(String macStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(macStr);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void OnApLinkedStaLeave(String macStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(macStr);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IApInterfaceEventCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IApInterfaceEventCallback)) {
                return new Proxy(obj);
            }
            return (IApInterfaceEventCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onNumAssociatedStationsChanged(data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onSoftApChannelSwitched(data.readInt(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        OnApLinkedStaJoin(data.readString());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        OnApLinkedStaLeave(data.readString());
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

    void OnApLinkedStaJoin(String str) throws RemoteException;

    void OnApLinkedStaLeave(String str) throws RemoteException;

    void onNumAssociatedStationsChanged(int i) throws RemoteException;

    void onSoftApChannelSwitched(int i, int i2) throws RemoteException;
}
