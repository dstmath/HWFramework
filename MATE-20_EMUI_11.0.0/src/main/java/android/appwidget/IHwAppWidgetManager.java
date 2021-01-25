package android.appwidget;

import android.appwidget.IHwAWSIDAMonitorCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwAppWidgetManager extends IInterface {
    boolean registerAWSIMonitorCallback(IHwAWSIDAMonitorCallback iHwAWSIDAMonitorCallback) throws RemoteException;

    public static class Default implements IHwAppWidgetManager {
        @Override // android.appwidget.IHwAppWidgetManager
        public boolean registerAWSIMonitorCallback(IHwAWSIDAMonitorCallback callback) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwAppWidgetManager {
        private static final String DESCRIPTOR = "android.appwidget.IHwAppWidgetManager";
        static final int TRANSACTION_registerAWSIMonitorCallback = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAppWidgetManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAppWidgetManager)) {
                return new Proxy(obj);
            }
            return (IHwAppWidgetManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "registerAWSIMonitorCallback";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean registerAWSIMonitorCallback = registerAWSIMonitorCallback(IHwAWSIDAMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(registerAWSIMonitorCallback ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwAppWidgetManager {
            public static IHwAppWidgetManager sDefaultImpl;
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

            @Override // android.appwidget.IHwAppWidgetManager
            public boolean registerAWSIMonitorCallback(IHwAWSIDAMonitorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerAWSIMonitorCallback(callback);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwAppWidgetManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwAppWidgetManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
