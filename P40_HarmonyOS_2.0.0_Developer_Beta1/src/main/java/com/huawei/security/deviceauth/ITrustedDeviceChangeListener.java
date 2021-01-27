package com.huawei.security.deviceauth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITrustedDeviceChangeListener extends IInterface {
    void onDeviceBound(String str, String str2) throws RemoteException;

    void onDeviceNotTrusted(String str) throws RemoteException;

    void onDeviceUnbound(String str, String str2) throws RemoteException;

    void onGroupCreated(String str) throws RemoteException;

    void onGroupDeleted(String str) throws RemoteException;

    void onLastGroupDeleted(String str, int i) throws RemoteException;

    void onTrustedDeviceNumChanged(int i) throws RemoteException;

    public static class Default implements ITrustedDeviceChangeListener {
        @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
        public void onDeviceNotTrusted(String connDeviceId) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
        public void onDeviceUnbound(String connDeviceId, String groupInfo) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
        public void onDeviceBound(String connDeviceId, String groupInfo) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
        public void onGroupCreated(String groupInfo) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
        public void onGroupDeleted(String groupInfo) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
        public void onTrustedDeviceNumChanged(int trustedDeviceNum) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
        public void onLastGroupDeleted(String connDeviceId, int groupType) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITrustedDeviceChangeListener {
        private static final String DESCRIPTOR = "com.huawei.security.deviceauth.ITrustedDeviceChangeListener";
        static final int TRANSACTION_onDeviceBound = 3;
        static final int TRANSACTION_onDeviceNotTrusted = 1;
        static final int TRANSACTION_onDeviceUnbound = 2;
        static final int TRANSACTION_onGroupCreated = 4;
        static final int TRANSACTION_onGroupDeleted = 5;
        static final int TRANSACTION_onLastGroupDeleted = 7;
        static final int TRANSACTION_onTrustedDeviceNumChanged = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITrustedDeviceChangeListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITrustedDeviceChangeListener)) {
                return new Proxy(obj);
            }
            return (ITrustedDeviceChangeListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onDeviceNotTrusted(data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onDeviceUnbound(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onDeviceBound(data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onGroupCreated(data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onGroupDeleted(data.readString());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onTrustedDeviceNumChanged(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onLastGroupDeleted(data.readString(), data.readInt());
                        reply.writeNoException();
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
        public static class Proxy implements ITrustedDeviceChangeListener {
            public static ITrustedDeviceChangeListener sDefaultImpl;
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

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onDeviceNotTrusted(String connDeviceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connDeviceId);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDeviceNotTrusted(connDeviceId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onDeviceUnbound(String connDeviceId, String groupInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connDeviceId);
                    _data.writeString(groupInfo);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDeviceUnbound(connDeviceId, groupInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onDeviceBound(String connDeviceId, String groupInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connDeviceId);
                    _data.writeString(groupInfo);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onDeviceBound(connDeviceId, groupInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onGroupCreated(String groupInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupInfo);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onGroupCreated(groupInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onGroupDeleted(String groupInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupInfo);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onGroupDeleted(groupInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onTrustedDeviceNumChanged(int trustedDeviceNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(trustedDeviceNum);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onTrustedDeviceNumChanged(trustedDeviceNum);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.ITrustedDeviceChangeListener
            public void onLastGroupDeleted(String connDeviceId, int groupType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(connDeviceId);
                    _data.writeInt(groupType);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onLastGroupDeleted(connDeviceId, groupType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITrustedDeviceChangeListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITrustedDeviceChangeListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
