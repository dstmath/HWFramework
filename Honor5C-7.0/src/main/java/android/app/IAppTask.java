package android.app;

import android.app.ActivityManager.RecentTaskInfo;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAppTask extends IInterface {

    public static abstract class Stub extends Binder implements IAppTask {
        private static final String DESCRIPTOR = "android.app.IAppTask";
        static final int TRANSACTION_finishAndRemoveTask = 1;
        static final int TRANSACTION_getTaskInfo = 2;
        static final int TRANSACTION_moveToFront = 3;
        static final int TRANSACTION_setExcludeFromRecents = 5;
        static final int TRANSACTION_startActivity = 4;

        private static class Proxy implements IAppTask {
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

            public void finishAndRemoveTask() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_finishAndRemoveTask, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public RecentTaskInfo getTaskInfo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    RecentTaskInfo recentTaskInfo;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getTaskInfo, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        recentTaskInfo = (RecentTaskInfo) RecentTaskInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        recentTaskInfo = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return recentTaskInfo;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void moveToFront() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_moveToFront, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startActivity(IBinder whoThread, String callingPackage, Intent intent, String resolvedType, Bundle options) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(whoThread);
                    _data.writeString(callingPackage);
                    if (intent != null) {
                        _data.writeInt(Stub.TRANSACTION_finishAndRemoveTask);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    if (options != null) {
                        _data.writeInt(Stub.TRANSACTION_finishAndRemoveTask);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_startActivity, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setExcludeFromRecents(boolean exclude) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (exclude) {
                        i = Stub.TRANSACTION_finishAndRemoveTask;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setExcludeFromRecents, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppTask asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppTask)) {
                return new Proxy(obj);
            }
            return (IAppTask) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            switch (code) {
                case TRANSACTION_finishAndRemoveTask /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    finishAndRemoveTask();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getTaskInfo /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    RecentTaskInfo _result = getTaskInfo();
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_finishAndRemoveTask);
                        _result.writeToParcel(reply, TRANSACTION_finishAndRemoveTask);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_moveToFront /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    moveToFront();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_startActivity /*4*/:
                    Intent intent;
                    Bundle bundle;
                    data.enforceInterface(DESCRIPTOR);
                    IBinder _arg02 = data.readStrongBinder();
                    String _arg1 = data.readString();
                    if (data.readInt() != 0) {
                        intent = (Intent) Intent.CREATOR.createFromParcel(data);
                    } else {
                        intent = null;
                    }
                    String _arg3 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    int _result2 = startActivity(_arg02, _arg1, intent, _arg3, bundle);
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case TRANSACTION_setExcludeFromRecents /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    setExcludeFromRecents(_arg0);
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void finishAndRemoveTask() throws RemoteException;

    RecentTaskInfo getTaskInfo() throws RemoteException;

    void moveToFront() throws RemoteException;

    void setExcludeFromRecents(boolean z) throws RemoteException;

    int startActivity(IBinder iBinder, String str, Intent intent, String str2, Bundle bundle) throws RemoteException;
}
