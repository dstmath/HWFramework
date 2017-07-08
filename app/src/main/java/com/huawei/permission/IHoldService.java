package com.huawei.permission;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHoldService extends IInterface {

    public static abstract class Stub extends Binder implements IHoldService {
        private static final String DESCRIPTOR = "com.huawei.permission.IHoldService";
        static final int TRANSACTION_addRecord = 4;
        static final int TRANSACTION_callHsmService = 9;
        static final int TRANSACTION_checkBeforeShowDialog = 6;
        static final int TRANSACTION_checkBeforeShowDialogWithPid = 7;
        static final int TRANSACTION_checkPreBlock = 10;
        static final int TRANSACTION_checkSystemAppInternal = 2;
        static final int TRANSACTION_getPendingIntent = 3;
        static final int TRANSACTION_holdServiceByRequestPermission = 1;
        static final int TRANSACTION_releaseHoldService = 5;
        static final int TRANSACTION_removeRuntimePermissions = 8;

        private static class Proxy implements IHoldService {
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

            public int holdServiceByRequestPermission(int uid, int pid, int permissionType, String desAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(permissionType);
                    _data.writeString(desAddr);
                    this.mRemote.transact(Stub.TRANSACTION_holdServiceByRequestPermission, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean checkSystemAppInternal(int callUid, boolean excludeCust) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callUid);
                    if (excludeCust) {
                        i = Stub.TRANSACTION_holdServiceByRequestPermission;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_checkSystemAppInternal, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public PendingIntent getPendingIntent(int requestCode, Intent intent, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PendingIntent pendingIntent;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(requestCode);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_holdServiceByRequestPermission);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_getPendingIntent, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(_reply);
                    } else {
                        pendingIntent = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return pendingIntent;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void addRecord(int uid, int permissionType, int state, boolean click) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(permissionType);
                    _data.writeInt(state);
                    if (click) {
                        i = Stub.TRANSACTION_holdServiceByRequestPermission;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_addRecord, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int releaseHoldService(int uid, int userSelection, int permissionType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(userSelection);
                    _data.writeInt(permissionType);
                    this.mRemote.transact(Stub.TRANSACTION_releaseHoldService, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkBeforeShowDialog(int uid, int permissionType, String desAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(permissionType);
                    _data.writeString(desAddr);
                    this.mRemote.transact(Stub.TRANSACTION_checkBeforeShowDialog, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkBeforeShowDialogWithPid(int uid, int pid, int permissionType, String desAddr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(permissionType);
                    _data.writeString(desAddr);
                    this.mRemote.transact(Stub.TRANSACTION_checkBeforeShowDialogWithPid, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeRuntimePermissions(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(Stub.TRANSACTION_removeRuntimePermissions, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle callHsmService(String method, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    Bundle bundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(method);
                    if (params != null) {
                        _data.writeInt(Stub.TRANSACTION_holdServiceByRequestPermission);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_callHsmService, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        bundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return bundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean checkPreBlock(int callUid, int permissionType, boolean showToast) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(callUid);
                    _data.writeInt(permissionType);
                    if (showToast) {
                        i = Stub.TRANSACTION_holdServiceByRequestPermission;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_checkPreBlock, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHoldService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHoldService)) {
                return new Proxy(obj);
            }
            return (IHoldService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            boolean _result2;
            switch (code) {
                case TRANSACTION_holdServiceByRequestPermission /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = holdServiceByRequestPermission(data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_checkSystemAppInternal /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = checkSystemAppInternal(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_holdServiceByRequestPermission : 0);
                    return true;
                case TRANSACTION_getPendingIntent /*3*/:
                    Intent intent;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    PendingIntent _result3 = getPendingIntent(_arg0, intent, data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(TRANSACTION_holdServiceByRequestPermission);
                        _result3.writeToParcel(reply, TRANSACTION_holdServiceByRequestPermission);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_addRecord /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    addRecord(data.readInt(), data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_releaseHoldService /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = releaseHoldService(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_checkBeforeShowDialog /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = checkBeforeShowDialog(data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_checkBeforeShowDialogWithPid /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = checkBeforeShowDialogWithPid(data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_removeRuntimePermissions /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeRuntimePermissions(data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_callHsmService /*9*/:
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    String _arg02 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    Bundle _result4 = callHsmService(_arg02, bundle);
                    reply.writeNoException();
                    if (_result4 != null) {
                        reply.writeInt(TRANSACTION_holdServiceByRequestPermission);
                        _result4.writeToParcel(reply, TRANSACTION_holdServiceByRequestPermission);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_checkPreBlock /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = checkPreBlock(data.readInt(), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_holdServiceByRequestPermission : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void addRecord(int i, int i2, int i3, boolean z) throws RemoteException;

    Bundle callHsmService(String str, Bundle bundle) throws RemoteException;

    int checkBeforeShowDialog(int i, int i2, String str) throws RemoteException;

    int checkBeforeShowDialogWithPid(int i, int i2, int i3, String str) throws RemoteException;

    boolean checkPreBlock(int i, int i2, boolean z) throws RemoteException;

    boolean checkSystemAppInternal(int i, boolean z) throws RemoteException;

    PendingIntent getPendingIntent(int i, Intent intent, int i2) throws RemoteException;

    int holdServiceByRequestPermission(int i, int i2, int i3, String str) throws RemoteException;

    int releaseHoldService(int i, int i2, int i3) throws RemoteException;

    void removeRuntimePermissions(String str) throws RemoteException;
}
