package android.net.lowpan;

import android.net.IpPrefix;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILowpanInterfaceListener extends IInterface {

    public static abstract class Stub extends Binder implements ILowpanInterfaceListener {
        private static final String DESCRIPTOR = "android.net.lowpan.ILowpanInterfaceListener";
        static final int TRANSACTION_onConnectedChanged = 2;
        static final int TRANSACTION_onEnabledChanged = 1;
        static final int TRANSACTION_onLinkAddressAdded = 9;
        static final int TRANSACTION_onLinkAddressRemoved = 10;
        static final int TRANSACTION_onLinkNetworkAdded = 7;
        static final int TRANSACTION_onLinkNetworkRemoved = 8;
        static final int TRANSACTION_onLowpanIdentityChanged = 6;
        static final int TRANSACTION_onReceiveFromCommissioner = 11;
        static final int TRANSACTION_onRoleChanged = 4;
        static final int TRANSACTION_onStateChanged = 5;
        static final int TRANSACTION_onUpChanged = 3;

        private static class Proxy implements ILowpanInterfaceListener {
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

            public void onEnabledChanged(boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onConnectedChanged(boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onUpChanged(boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onRoleChanged(String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(value);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onStateChanged(String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(value);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onLowpanIdentityChanged(LowpanIdentity value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (value != null) {
                        _data.writeInt(1);
                        value.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onLinkNetworkAdded(IpPrefix value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (value != null) {
                        _data.writeInt(1);
                        value.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onLinkNetworkRemoved(IpPrefix value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (value != null) {
                        _data.writeInt(1);
                        value.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(8, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onLinkAddressAdded(String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(value);
                    this.mRemote.transact(9, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onLinkAddressRemoved(String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(value);
                    this.mRemote.transact(10, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onReceiveFromCommissioner(byte[] packet) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(packet);
                    this.mRemote.transact(11, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILowpanInterfaceListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILowpanInterfaceListener)) {
                return new Proxy(obj);
            }
            return (ILowpanInterfaceListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v0, resolved type: android.net.lowpan.LowpanIdentity} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v4, resolved type: android.net.lowpan.LowpanIdentity} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v7, resolved type: android.net.lowpan.LowpanIdentity} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v15, resolved type: android.net.IpPrefix} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v10, resolved type: android.net.lowpan.LowpanIdentity} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v19, resolved type: android.net.IpPrefix} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v13, resolved type: android.net.lowpan.LowpanIdentity} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v14, resolved type: android.net.lowpan.LowpanIdentity} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v15, resolved type: android.net.lowpan.LowpanIdentity} */
        /* JADX WARNING: type inference failed for: r3v9, types: [android.net.IpPrefix] */
        /* JADX WARNING: type inference failed for: r3v12, types: [android.net.IpPrefix] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                boolean _arg0 = false;
                LowpanIdentity _arg02 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onEnabledChanged(_arg0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onConnectedChanged(_arg0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        onUpChanged(_arg0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onRoleChanged(data.readString());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onStateChanged(data.readString());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = LowpanIdentity.CREATOR.createFromParcel(data);
                        }
                        onLowpanIdentityChanged(_arg02);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = IpPrefix.CREATOR.createFromParcel(data);
                        }
                        onLinkNetworkAdded(_arg02);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = IpPrefix.CREATOR.createFromParcel(data);
                        }
                        onLinkNetworkRemoved(_arg02);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onLinkAddressAdded(data.readString());
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        onLinkAddressRemoved(data.readString());
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onReceiveFromCommissioner(data.createByteArray());
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

    void onConnectedChanged(boolean z) throws RemoteException;

    void onEnabledChanged(boolean z) throws RemoteException;

    void onLinkAddressAdded(String str) throws RemoteException;

    void onLinkAddressRemoved(String str) throws RemoteException;

    void onLinkNetworkAdded(IpPrefix ipPrefix) throws RemoteException;

    void onLinkNetworkRemoved(IpPrefix ipPrefix) throws RemoteException;

    void onLowpanIdentityChanged(LowpanIdentity lowpanIdentity) throws RemoteException;

    void onReceiveFromCommissioner(byte[] bArr) throws RemoteException;

    void onRoleChanged(String str) throws RemoteException;

    void onStateChanged(String str) throws RemoteException;

    void onUpChanged(boolean z) throws RemoteException;
}
