package android.net.lowpan;

import android.net.lowpan.ILowpanInterface;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILowpanManagerListener extends IInterface {
    void onInterfaceAdded(ILowpanInterface iLowpanInterface) throws RemoteException;

    void onInterfaceRemoved(ILowpanInterface iLowpanInterface) throws RemoteException;

    public static class Default implements ILowpanManagerListener {
        @Override // android.net.lowpan.ILowpanManagerListener
        public void onInterfaceAdded(ILowpanInterface lowpanInterface) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanManagerListener
        public void onInterfaceRemoved(ILowpanInterface lowpanInterface) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILowpanManagerListener {
        private static final String DESCRIPTOR = "android.net.lowpan.ILowpanManagerListener";
        static final int TRANSACTION_onInterfaceAdded = 1;
        static final int TRANSACTION_onInterfaceRemoved = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILowpanManagerListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILowpanManagerListener)) {
                return new Proxy(obj);
            }
            return (ILowpanManagerListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onInterfaceAdded";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "onInterfaceRemoved";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onInterfaceAdded(ILowpanInterface.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onInterfaceRemoved(ILowpanInterface.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ILowpanManagerListener {
            public static ILowpanManagerListener sDefaultImpl;
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

            @Override // android.net.lowpan.ILowpanManagerListener
            public void onInterfaceAdded(ILowpanInterface lowpanInterface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lowpanInterface != null ? lowpanInterface.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInterfaceAdded(lowpanInterface);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanManagerListener
            public void onInterfaceRemoved(ILowpanInterface lowpanInterface) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(lowpanInterface != null ? lowpanInterface.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onInterfaceRemoved(lowpanInterface);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILowpanManagerListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILowpanManagerListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
