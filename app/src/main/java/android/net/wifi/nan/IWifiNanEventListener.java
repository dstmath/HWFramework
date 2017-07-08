package android.net.wifi.nan;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IWifiNanEventListener extends IInterface {

    public static abstract class Stub extends Binder implements IWifiNanEventListener {
        private static final String DESCRIPTOR = "android.net.wifi.nan.IWifiNanEventListener";
        static final int TRANSACTION_onConfigCompleted = 1;
        static final int TRANSACTION_onConfigFailed = 2;
        static final int TRANSACTION_onIdentityChanged = 4;
        static final int TRANSACTION_onNanDown = 3;

        private static class Proxy implements IWifiNanEventListener {
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

            public void onConfigCompleted(ConfigRequest completedConfig) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (completedConfig != null) {
                        _data.writeInt(Stub.TRANSACTION_onConfigCompleted);
                        completedConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onConfigCompleted, _data, null, Stub.TRANSACTION_onConfigCompleted);
                } finally {
                    _data.recycle();
                }
            }

            public void onConfigFailed(ConfigRequest failedConfig, int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (failedConfig != null) {
                        _data.writeInt(Stub.TRANSACTION_onConfigCompleted);
                        failedConfig.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_onConfigFailed, _data, null, Stub.TRANSACTION_onConfigCompleted);
                } finally {
                    _data.recycle();
                }
            }

            public void onNanDown(int reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reason);
                    this.mRemote.transact(Stub.TRANSACTION_onNanDown, _data, null, Stub.TRANSACTION_onConfigCompleted);
                } finally {
                    _data.recycle();
                }
            }

            public void onIdentityChanged() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_onIdentityChanged, _data, null, Stub.TRANSACTION_onConfigCompleted);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IWifiNanEventListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IWifiNanEventListener)) {
                return new Proxy(obj);
            }
            return (IWifiNanEventListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ConfigRequest configRequest;
            switch (code) {
                case TRANSACTION_onConfigCompleted /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        configRequest = (ConfigRequest) ConfigRequest.CREATOR.createFromParcel(data);
                    } else {
                        configRequest = null;
                    }
                    onConfigCompleted(configRequest);
                    return true;
                case TRANSACTION_onConfigFailed /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        configRequest = (ConfigRequest) ConfigRequest.CREATOR.createFromParcel(data);
                    } else {
                        configRequest = null;
                    }
                    onConfigFailed(configRequest, data.readInt());
                    return true;
                case TRANSACTION_onNanDown /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNanDown(data.readInt());
                    return true;
                case TRANSACTION_onIdentityChanged /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onIdentityChanged();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onConfigCompleted(ConfigRequest configRequest) throws RemoteException;

    void onConfigFailed(ConfigRequest configRequest, int i) throws RemoteException;

    void onIdentityChanged() throws RemoteException;

    void onNanDown(int i) throws RemoteException;
}
