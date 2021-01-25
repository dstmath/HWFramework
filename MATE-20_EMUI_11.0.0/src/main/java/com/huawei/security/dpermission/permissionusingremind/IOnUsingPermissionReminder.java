package com.huawei.security.dpermission.permissionusingremind;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOnUsingPermissionReminder extends IInterface {
    void startUsingPermission(PermissionReminderInfoParcel permissionReminderInfoParcel) throws RemoteException;

    void stopUsingPermission(PermissionReminderInfoParcel permissionReminderInfoParcel) throws RemoteException;

    public static abstract class Stub extends Binder implements IOnUsingPermissionReminder {
        private static final String DESCRIPTOR = "com.huawei.security.dpermission.permissionusingremind.IOnUsingPermissionReminder";
        static final int TRANSACTION_startUsingPermission = 1;
        static final int TRANSACTION_stopUsingPermission = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOnUsingPermissionReminder asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOnUsingPermissionReminder)) {
                return new Proxy(obj);
            }
            return (IOnUsingPermissionReminder) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PermissionReminderInfoParcel _arg0;
            PermissionReminderInfoParcel _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = PermissionReminderInfoParcel.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                startUsingPermission(_arg0);
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = PermissionReminderInfoParcel.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                stopUsingPermission(_arg02);
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
        public static class Proxy implements IOnUsingPermissionReminder {
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

            @Override // com.huawei.security.dpermission.permissionusingremind.IOnUsingPermissionReminder
            public void startUsingPermission(PermissionReminderInfoParcel info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.dpermission.permissionusingremind.IOnUsingPermissionReminder
            public void stopUsingPermission(PermissionReminderInfoParcel info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
