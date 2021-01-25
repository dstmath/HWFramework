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

    void OnApLinkedStaJoin(String str) throws RemoteException;

    void OnApLinkedStaLeave(String str) throws RemoteException;

    void onNumAssociatedStationsChanged(int i) throws RemoteException;

    void onSoftApChannelSwitched(int i, int i2) throws RemoteException;

    public static class Default implements IApInterfaceEventCallback {
        @Override // android.net.wifi.IApInterfaceEventCallback
        public void onNumAssociatedStationsChanged(int numStations) throws RemoteException {
        }

        @Override // android.net.wifi.IApInterfaceEventCallback
        public void onSoftApChannelSwitched(int frequency, int bandwidth) throws RemoteException {
        }

        @Override // android.net.wifi.IApInterfaceEventCallback
        public void OnApLinkedStaJoin(String macStr) throws RemoteException {
        }

        @Override // android.net.wifi.IApInterfaceEventCallback
        public void OnApLinkedStaLeave(String macStr) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IApInterfaceEventCallback {
        private static final String DESCRIPTOR = "android.net.wifi.IApInterfaceEventCallback";
        static final int TRANSACTION_OnApLinkedStaJoin = 3;
        static final int TRANSACTION_OnApLinkedStaLeave = 4;
        static final int TRANSACTION_onNumAssociatedStationsChanged = 1;
        static final int TRANSACTION_onSoftApChannelSwitched = 2;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onNumAssociatedStationsChanged(data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onSoftApChannelSwitched(data.readInt(), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                OnApLinkedStaJoin(data.readString());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                OnApLinkedStaLeave(data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IApInterfaceEventCallback {
            public static IApInterfaceEventCallback sDefaultImpl;
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

            @Override // android.net.wifi.IApInterfaceEventCallback
            public void onNumAssociatedStationsChanged(int numStations) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(numStations);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNumAssociatedStationsChanged(numStations);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IApInterfaceEventCallback
            public void onSoftApChannelSwitched(int frequency, int bandwidth) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(frequency);
                    _data.writeInt(bandwidth);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSoftApChannelSwitched(frequency, bandwidth);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IApInterfaceEventCallback
            public void OnApLinkedStaJoin(String macStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(macStr);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnApLinkedStaJoin(macStr);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IApInterfaceEventCallback
            public void OnApLinkedStaLeave(String macStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(macStr);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnApLinkedStaLeave(macStr);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IApInterfaceEventCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IApInterfaceEventCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
