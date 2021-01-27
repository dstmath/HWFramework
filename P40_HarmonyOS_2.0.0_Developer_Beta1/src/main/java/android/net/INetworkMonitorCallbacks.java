package android.net;

import android.net.INetworkMonitor;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INetworkMonitorCallbacks extends IInterface {
    public static final int VERSION = 3;

    int getInterfaceVersion() throws RemoteException;

    void hideProvisioningNotification() throws RemoteException;

    void notifyNetworkTested(int i, String str) throws RemoteException;

    void notifyPrivateDnsConfigResolved(PrivateDnsConfigParcel privateDnsConfigParcel) throws RemoteException;

    void onNetworkMonitorCreated(INetworkMonitor iNetworkMonitor) throws RemoteException;

    void showProvisioningNotification(String str, String str2) throws RemoteException;

    public static class Default implements INetworkMonitorCallbacks {
        @Override // android.net.INetworkMonitorCallbacks
        public void onNetworkMonitorCreated(INetworkMonitor networkMonitor) throws RemoteException {
        }

        @Override // android.net.INetworkMonitorCallbacks
        public void notifyNetworkTested(int testResult, String redirectUrl) throws RemoteException {
        }

        @Override // android.net.INetworkMonitorCallbacks
        public void notifyPrivateDnsConfigResolved(PrivateDnsConfigParcel config) throws RemoteException {
        }

        @Override // android.net.INetworkMonitorCallbacks
        public void showProvisioningNotification(String action, String packageName) throws RemoteException {
        }

        @Override // android.net.INetworkMonitorCallbacks
        public void hideProvisioningNotification() throws RemoteException {
        }

        @Override // android.net.INetworkMonitorCallbacks
        public int getInterfaceVersion() {
            return -1;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INetworkMonitorCallbacks {
        private static final String DESCRIPTOR = "android.net.INetworkMonitorCallbacks";
        static final int TRANSACTION_getInterfaceVersion = 16777215;
        static final int TRANSACTION_hideProvisioningNotification = 5;
        static final int TRANSACTION_notifyNetworkTested = 2;
        static final int TRANSACTION_notifyPrivateDnsConfigResolved = 3;
        static final int TRANSACTION_onNetworkMonitorCreated = 1;
        static final int TRANSACTION_showProvisioningNotification = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INetworkMonitorCallbacks asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INetworkMonitorCallbacks)) {
                return new Proxy(obj);
            }
            return (INetworkMonitorCallbacks) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PrivateDnsConfigParcel _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onNetworkMonitorCreated(INetworkMonitor.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                notifyNetworkTested(data.readInt(), data.readString());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = PrivateDnsConfigParcel.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                notifyPrivateDnsConfigResolved(_arg0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                showProvisioningNotification(data.readString(), data.readString());
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                hideProvisioningNotification();
                return true;
            } else if (code == TRANSACTION_getInterfaceVersion) {
                data.enforceInterface(DESCRIPTOR);
                reply.writeNoException();
                reply.writeInt(getInterfaceVersion());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INetworkMonitorCallbacks {
            public static INetworkMonitorCallbacks sDefaultImpl;
            private int mCachedVersion = -1;
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

            @Override // android.net.INetworkMonitorCallbacks
            public void onNetworkMonitorCreated(INetworkMonitor networkMonitor) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(networkMonitor != null ? networkMonitor.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onNetworkMonitorCreated(networkMonitor);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitorCallbacks
            public void notifyNetworkTested(int testResult, String redirectUrl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(testResult);
                    _data.writeString(redirectUrl);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyNetworkTested(testResult, redirectUrl);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitorCallbacks
            public void notifyPrivateDnsConfigResolved(PrivateDnsConfigParcel config) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (config != null) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().notifyPrivateDnsConfigResolved(config);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitorCallbacks
            public void showProvisioningNotification(String action, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(action);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().showProvisioningNotification(action, packageName);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitorCallbacks
            public void hideProvisioningNotification() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().hideProvisioningNotification();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.INetworkMonitorCallbacks
            public int getInterfaceVersion() throws RemoteException {
                if (this.mCachedVersion == -1) {
                    Parcel data = Parcel.obtain();
                    Parcel reply = Parcel.obtain();
                    try {
                        data.writeInterfaceToken(Stub.DESCRIPTOR);
                        this.mRemote.transact(Stub.TRANSACTION_getInterfaceVersion, data, reply, 0);
                        reply.readException();
                        this.mCachedVersion = reply.readInt();
                    } finally {
                        reply.recycle();
                        data.recycle();
                    }
                }
                return this.mCachedVersion;
            }
        }

        public static boolean setDefaultImpl(INetworkMonitorCallbacks impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INetworkMonitorCallbacks getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
