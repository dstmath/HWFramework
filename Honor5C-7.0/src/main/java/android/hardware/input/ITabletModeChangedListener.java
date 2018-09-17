package android.hardware.input;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITabletModeChangedListener extends IInterface {

    public static abstract class Stub extends Binder implements ITabletModeChangedListener {
        private static final String DESCRIPTOR = "android.hardware.input.ITabletModeChangedListener";
        static final int TRANSACTION_onTabletModeChanged = 1;

        private static class Proxy implements ITabletModeChangedListener {
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

            public void onTabletModeChanged(long whenNanos, boolean inTabletMode) throws RemoteException {
                int i = Stub.TRANSACTION_onTabletModeChanged;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(whenNanos);
                    if (!inTabletMode) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_onTabletModeChanged, _data, null, Stub.TRANSACTION_onTabletModeChanged);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITabletModeChangedListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITabletModeChangedListener)) {
                return new Proxy(obj);
            }
            return (ITabletModeChangedListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg1 = false;
            switch (code) {
                case TRANSACTION_onTabletModeChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    long _arg0 = data.readLong();
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    }
                    onTabletModeChanged(_arg0, _arg1);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onTabletModeChanged(long j, boolean z) throws RemoteException;
}
