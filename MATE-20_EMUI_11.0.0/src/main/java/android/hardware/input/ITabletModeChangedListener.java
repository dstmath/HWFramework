package android.hardware.input;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITabletModeChangedListener extends IInterface {
    void onTabletModeChanged(long j, boolean z) throws RemoteException;

    public static class Default implements ITabletModeChangedListener {
        @Override // android.hardware.input.ITabletModeChangedListener
        public void onTabletModeChanged(long whenNanos, boolean inTabletMode) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITabletModeChangedListener {
        private static final String DESCRIPTOR = "android.hardware.input.ITabletModeChangedListener";
        static final int TRANSACTION_onTabletModeChanged = 1;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "onTabletModeChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onTabletModeChanged(data.readLong(), data.readInt() != 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ITabletModeChangedListener {
            public static ITabletModeChangedListener sDefaultImpl;
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

            @Override // android.hardware.input.ITabletModeChangedListener
            public void onTabletModeChanged(long whenNanos, boolean inTabletMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(whenNanos);
                    _data.writeInt(inTabletMode ? 1 : 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onTabletModeChanged(whenNanos, inTabletMode);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITabletModeChangedListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITabletModeChangedListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
