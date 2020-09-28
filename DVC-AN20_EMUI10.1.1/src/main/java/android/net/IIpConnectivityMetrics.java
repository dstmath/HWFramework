package android.net;

import android.net.INetdEventCallback;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIpConnectivityMetrics extends IInterface {
    boolean addNetdEventCallback(int i, INetdEventCallback iNetdEventCallback) throws RemoteException;

    int logEvent(ConnectivityMetricsEvent connectivityMetricsEvent) throws RemoteException;

    boolean removeNetdEventCallback(int i) throws RemoteException;

    public static class Default implements IIpConnectivityMetrics {
        @Override // android.net.IIpConnectivityMetrics
        public int logEvent(ConnectivityMetricsEvent event) throws RemoteException {
            return 0;
        }

        @Override // android.net.IIpConnectivityMetrics
        public boolean addNetdEventCallback(int callerType, INetdEventCallback callback) throws RemoteException {
            return false;
        }

        @Override // android.net.IIpConnectivityMetrics
        public boolean removeNetdEventCallback(int callerType) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIpConnectivityMetrics {
        private static final String DESCRIPTOR = "android.net.IIpConnectivityMetrics";
        static final int TRANSACTION_addNetdEventCallback = 2;
        static final int TRANSACTION_logEvent = 1;
        static final int TRANSACTION_removeNetdEventCallback = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIpConnectivityMetrics asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIpConnectivityMetrics)) {
                return new Proxy(obj);
            }
            return (IIpConnectivityMetrics) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "logEvent";
            }
            if (transactionCode == 2) {
                return "addNetdEventCallback";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "removeNetdEventCallback";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ConnectivityMetricsEvent _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ConnectivityMetricsEvent.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                int _result = logEvent(_arg0);
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean addNetdEventCallback = addNetdEventCallback(data.readInt(), INetdEventCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(addNetdEventCallback ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean removeNetdEventCallback = removeNetdEventCallback(data.readInt());
                reply.writeNoException();
                reply.writeInt(removeNetdEventCallback ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IIpConnectivityMetrics {
            public static IIpConnectivityMetrics sDefaultImpl;
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

            @Override // android.net.IIpConnectivityMetrics
            public int logEvent(ConnectivityMetricsEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().logEvent(event);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.net.IIpConnectivityMetrics
            public boolean addNetdEventCallback(int callerType, INetdEventCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callerType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addNetdEventCallback(callerType, callback);
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

            @Override // android.net.IIpConnectivityMetrics
            public boolean removeNetdEventCallback(int callerType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callerType);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeNetdEventCallback(callerType);
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

        public static boolean setDefaultImpl(IIpConnectivityMetrics impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIpConnectivityMetrics getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
