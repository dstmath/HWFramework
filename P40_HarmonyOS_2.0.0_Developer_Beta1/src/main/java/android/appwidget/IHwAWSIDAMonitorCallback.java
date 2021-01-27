package android.appwidget;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwAWSIDAMonitorCallback extends IInterface {
    void updateWidgetFlushReport(int i, String str) throws RemoteException;

    public static class Default implements IHwAWSIDAMonitorCallback {
        @Override // android.appwidget.IHwAWSIDAMonitorCallback
        public void updateWidgetFlushReport(int userId, String packageName) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwAWSIDAMonitorCallback {
        private static final String DESCRIPTOR = "android.appwidget.IHwAWSIDAMonitorCallback";
        static final int TRANSACTION_updateWidgetFlushReport = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAWSIDAMonitorCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAWSIDAMonitorCallback)) {
                return new Proxy(obj);
            }
            return (IHwAWSIDAMonitorCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "updateWidgetFlushReport";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                updateWidgetFlushReport(data.readInt(), data.readString());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwAWSIDAMonitorCallback {
            public static IHwAWSIDAMonitorCallback sDefaultImpl;
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

            @Override // android.appwidget.IHwAWSIDAMonitorCallback
            public void updateWidgetFlushReport(int userId, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateWidgetFlushReport(userId, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwAWSIDAMonitorCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwAWSIDAMonitorCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
