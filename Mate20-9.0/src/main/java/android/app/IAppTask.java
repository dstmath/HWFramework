package android.app;

import android.app.ActivityManager;
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
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityManager.RecentTaskInfo getTaskInfo() throws RemoteException {
                ActivityManager.RecentTaskInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ActivityManager.RecentTaskInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void moveToFront() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
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
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(resolvedType);
                    if (options != null) {
                        _data.writeInt(1);
                        options.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setExcludeFromRecents(boolean exclude) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(exclude);
                    this.mRemote.transact(5, _data, _reply, 0);
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
            Intent _arg2;
            Bundle _arg4;
            if (code != 1598968902) {
                boolean _arg0 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        finishAndRemoveTask();
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        ActivityManager.RecentTaskInfo _result = getTaskInfo();
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        moveToFront();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg02 = data.readStrongBinder();
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        String _arg3 = data.readString();
                        if (data.readInt() != 0) {
                            _arg4 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        int _result2 = startActivity(_arg02, _arg1, _arg2, _arg3, _arg4);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = true;
                        }
                        setExcludeFromRecents(_arg0);
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
    }

    void finishAndRemoveTask() throws RemoteException;

    ActivityManager.RecentTaskInfo getTaskInfo() throws RemoteException;

    void moveToFront() throws RemoteException;

    void setExcludeFromRecents(boolean z) throws RemoteException;

    int startActivity(IBinder iBinder, String str, Intent intent, String str2, Bundle bundle) throws RemoteException;
}
