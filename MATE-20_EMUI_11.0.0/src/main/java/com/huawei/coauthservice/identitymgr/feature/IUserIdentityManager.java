package com.huawei.coauthservice.identitymgr.feature;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.coauthservice.identitymgr.feature.ICreateIdmGroupCallback;
import com.huawei.coauthservice.identitymgr.feature.IDeleteIdmGroupCallback;
import com.huawei.coauthservice.identitymgr.feature.IGetIdmGroupCallback;
import com.huawei.coauthservice.identitymgr.feature.IInitIdmServiceCallback;
import com.huawei.coauthservice.identitymgr.feature.IPurgeIdmGroupCallback;
import com.huawei.coauthservice.identitymgr.model.CreateGroupInfo;
import com.huawei.coauthservice.identitymgr.model.DeleteGroupInfo;
import com.huawei.coauthservice.identitymgr.model.PurgeGroupInfo;
import com.huawei.coauthservice.identitymgr.model.UserType;

public interface IUserIdentityManager extends IInterface {
    void createGroup(CreateGroupInfo createGroupInfo, ICreateIdmGroupCallback iCreateIdmGroupCallback) throws RemoteException;

    void deleteGroup(DeleteGroupInfo deleteGroupInfo, IDeleteIdmGroupCallback iDeleteIdmGroupCallback) throws RemoteException;

    void getGroups(UserType userType, String str, IGetIdmGroupCallback iGetIdmGroupCallback) throws RemoteException;

    void initService(UserType userType, IInitIdmServiceCallback iInitIdmServiceCallback) throws RemoteException;

    void purgeGroup(PurgeGroupInfo purgeGroupInfo, IPurgeIdmGroupCallback iPurgeIdmGroupCallback) throws RemoteException;

    public static abstract class Stub extends Binder implements IUserIdentityManager {
        private static final String DESCRIPTOR = "com.huawei.coauthservice.identitymgr.feature.IUserIdentityManager";
        static final int TRANSACTION_createGroup = 2;
        static final int TRANSACTION_deleteGroup = 3;
        static final int TRANSACTION_getGroups = 4;
        static final int TRANSACTION_initService = 1;
        static final int TRANSACTION_purgeGroup = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IUserIdentityManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IUserIdentityManager)) {
                return new Proxy(obj);
            }
            return (IUserIdentityManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            UserType _arg0;
            CreateGroupInfo _arg02;
            DeleteGroupInfo _arg03;
            UserType _arg04;
            PurgeGroupInfo _arg05;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = UserType.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                initService(_arg0, IInitIdmServiceCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = CreateGroupInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                createGroup(_arg02, ICreateIdmGroupCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg03 = DeleteGroupInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg03 = null;
                }
                deleteGroup(_arg03, IDeleteIdmGroupCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg04 = UserType.CREATOR.createFromParcel(data);
                } else {
                    _arg04 = null;
                }
                getGroups(_arg04, data.readString(), IGetIdmGroupCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg05 = PurgeGroupInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg05 = null;
                }
                purgeGroup(_arg05, IPurgeIdmGroupCallback.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IUserIdentityManager {
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

            @Override // com.huawei.coauthservice.identitymgr.feature.IUserIdentityManager
            public void initService(UserType userType, IInitIdmServiceCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (userType != null) {
                        _data.writeInt(1);
                        userType.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.identitymgr.feature.IUserIdentityManager
            public void createGroup(CreateGroupInfo createGroupInfo, ICreateIdmGroupCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (createGroupInfo != null) {
                        _data.writeInt(1);
                        createGroupInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.identitymgr.feature.IUserIdentityManager
            public void deleteGroup(DeleteGroupInfo deleteGroupInfo, IDeleteIdmGroupCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (deleteGroupInfo != null) {
                        _data.writeInt(1);
                        deleteGroupInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.identitymgr.feature.IUserIdentityManager
            public void getGroups(UserType userType, String moduleName, IGetIdmGroupCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (userType != null) {
                        _data.writeInt(1);
                        userType.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(moduleName);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.coauthservice.identitymgr.feature.IUserIdentityManager
            public void purgeGroup(PurgeGroupInfo purgeGroupInfo, IPurgeIdmGroupCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (purgeGroupInfo != null) {
                        _data.writeInt(1);
                        purgeGroupInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
