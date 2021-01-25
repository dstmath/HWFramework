package android.net.lowpan;

import android.net.IpPrefix;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILowpanInterfaceListener extends IInterface {
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

    public static class Default implements ILowpanInterfaceListener {
        @Override // android.net.lowpan.ILowpanInterfaceListener
        public void onEnabledChanged(boolean value) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanInterfaceListener
        public void onConnectedChanged(boolean value) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanInterfaceListener
        public void onUpChanged(boolean value) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanInterfaceListener
        public void onRoleChanged(String value) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanInterfaceListener
        public void onStateChanged(String value) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanInterfaceListener
        public void onLowpanIdentityChanged(LowpanIdentity value) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanInterfaceListener
        public void onLinkNetworkAdded(IpPrefix value) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanInterfaceListener
        public void onLinkNetworkRemoved(IpPrefix value) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanInterfaceListener
        public void onLinkAddressAdded(String value) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanInterfaceListener
        public void onLinkAddressRemoved(String value) throws RemoteException {
        }

        @Override // android.net.lowpan.ILowpanInterfaceListener
        public void onReceiveFromCommissioner(byte[] packet) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "onEnabledChanged";
                case 2:
                    return "onConnectedChanged";
                case 3:
                    return "onUpChanged";
                case 4:
                    return "onRoleChanged";
                case 5:
                    return "onStateChanged";
                case 6:
                    return "onLowpanIdentityChanged";
                case 7:
                    return "onLinkNetworkAdded";
                case 8:
                    return "onLinkNetworkRemoved";
                case 9:
                    return "onLinkAddressAdded";
                case 10:
                    return "onLinkAddressRemoved";
                case 11:
                    return "onReceiveFromCommissioner";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            LowpanIdentity _arg0;
            IpPrefix _arg02;
            IpPrefix _arg03;
            if (code != 1598968902) {
                boolean _arg04 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        onEnabledChanged(_arg04);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        onConnectedChanged(_arg04);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = true;
                        }
                        onUpChanged(_arg04);
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
                            _arg0 = LowpanIdentity.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onLowpanIdentityChanged(_arg0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = IpPrefix.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        onLinkNetworkAdded(_arg02);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = IpPrefix.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        onLinkNetworkRemoved(_arg03);
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

        /* access modifiers changed from: private */
        public static class Proxy implements ILowpanInterfaceListener {
            public static ILowpanInterfaceListener sDefaultImpl;
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

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onEnabledChanged(boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value ? 1 : 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onEnabledChanged(value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onConnectedChanged(boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value ? 1 : 0);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onConnectedChanged(value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onUpChanged(boolean value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(value ? 1 : 0);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onUpChanged(value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onRoleChanged(String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(value);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRoleChanged(value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onStateChanged(String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(value);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onStateChanged(value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
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
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLowpanIdentityChanged(value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
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
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLinkNetworkAdded(value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
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
                    if (this.mRemote.transact(8, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLinkNetworkRemoved(value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onLinkAddressAdded(String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(value);
                    if (this.mRemote.transact(9, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLinkAddressAdded(value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onLinkAddressRemoved(String value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(value);
                    if (this.mRemote.transact(10, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onLinkAddressRemoved(value);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.lowpan.ILowpanInterfaceListener
            public void onReceiveFromCommissioner(byte[] packet) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(packet);
                    if (this.mRemote.transact(11, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onReceiveFromCommissioner(packet);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILowpanInterfaceListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILowpanInterfaceListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
