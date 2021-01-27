package com.huawei.security.deviceauth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IHichainGroupChangeListener extends IInterface {
    void onGroupCreated(String str, int i) throws RemoteException;

    void onGroupDeleted(String str, int i) throws RemoteException;

    void onMemberAdded(String str, int i, List<String> list) throws RemoteException;

    void onMemberDeleted(String str, int i, List<String> list) throws RemoteException;

    public static class Default implements IHichainGroupChangeListener {
        @Override // com.huawei.security.deviceauth.IHichainGroupChangeListener
        public void onGroupCreated(String groupId, int groupType) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.IHichainGroupChangeListener
        public void onGroupDeleted(String groupId, int groupType) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.IHichainGroupChangeListener
        public void onMemberAdded(String groupId, int groupType, List<String> list) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.IHichainGroupChangeListener
        public void onMemberDeleted(String groupId, int groupType, List<String> list) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHichainGroupChangeListener {
        private static final String DESCRIPTOR = "com.huawei.security.deviceauth.IHichainGroupChangeListener";
        static final int TRANSACTION_onGroupCreated = 1;
        static final int TRANSACTION_onGroupDeleted = 2;
        static final int TRANSACTION_onMemberAdded = 3;
        static final int TRANSACTION_onMemberDeleted = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHichainGroupChangeListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHichainGroupChangeListener)) {
                return new Proxy(obj);
            }
            return (IHichainGroupChangeListener) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onGroupCreated(data.readString(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onGroupDeleted(data.readString(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onMemberAdded(data.readString(), data.readInt(), data.createStringArrayList());
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onMemberDeleted(data.readString(), data.readInt(), data.createStringArrayList());
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
        public static class Proxy implements IHichainGroupChangeListener {
            public static IHichainGroupChangeListener sDefaultImpl;
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

            @Override // com.huawei.security.deviceauth.IHichainGroupChangeListener
            public void onGroupCreated(String groupId, int groupType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    _data.writeInt(groupType);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onGroupCreated(groupId, groupType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IHichainGroupChangeListener
            public void onGroupDeleted(String groupId, int groupType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    _data.writeInt(groupType);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onGroupDeleted(groupId, groupType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IHichainGroupChangeListener
            public void onMemberAdded(String groupId, int groupType, List<String> memList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    _data.writeInt(groupType);
                    _data.writeStringList(memList);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onMemberAdded(groupId, groupType, memList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.IHichainGroupChangeListener
            public void onMemberDeleted(String groupId, int groupType, List<String> memList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(groupId);
                    _data.writeInt(groupType);
                    _data.writeStringList(memList);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onMemberDeleted(groupId, groupType, memList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHichainGroupChangeListener impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHichainGroupChangeListener getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
