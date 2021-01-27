package android.app;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IActivityController extends IInterface {
    boolean activityResuming(String str) throws RemoteException;

    boolean activityStarting(Intent intent, String str) throws RemoteException;

    boolean appCrashed(String str, int i, String str2, String str3, long j, String str4) throws RemoteException;

    int appEarlyNotResponding(String str, int i, String str2) throws RemoteException;

    int appNotResponding(String str, int i, String str2) throws RemoteException;

    int systemNotResponding(String str) throws RemoteException;

    public static class Default implements IActivityController {
        @Override // android.app.IActivityController
        public boolean activityStarting(Intent intent, String pkg) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityController
        public boolean activityResuming(String pkg) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityController
        public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) throws RemoteException {
            return false;
        }

        @Override // android.app.IActivityController
        public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityController
        public int appNotResponding(String processName, int pid, String processStats) throws RemoteException {
            return 0;
        }

        @Override // android.app.IActivityController
        public int systemNotResponding(String msg) throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IActivityController {
        private static final String DESCRIPTOR = "android.app.IActivityController";
        static final int TRANSACTION_activityResuming = 2;
        static final int TRANSACTION_activityStarting = 1;
        static final int TRANSACTION_appCrashed = 3;
        static final int TRANSACTION_appEarlyNotResponding = 4;
        static final int TRANSACTION_appNotResponding = 5;
        static final int TRANSACTION_systemNotResponding = 6;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IActivityController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IActivityController)) {
                return new Proxy(obj);
            }
            return (IActivityController) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "activityStarting";
                case 2:
                    return "activityResuming";
                case 3:
                    return "appCrashed";
                case 4:
                    return "appEarlyNotResponding";
                case 5:
                    return "appNotResponding";
                case 6:
                    return "systemNotResponding";
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
            Intent _arg0;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean activityStarting = activityStarting(_arg0, data.readString());
                        reply.writeNoException();
                        reply.writeInt(activityStarting ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean activityResuming = activityResuming(data.readString());
                        reply.writeNoException();
                        reply.writeInt(activityResuming ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean appCrashed = appCrashed(data.readString(), data.readInt(), data.readString(), data.readString(), data.readLong(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(appCrashed ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = appEarlyNotResponding(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = appNotResponding(data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = systemNotResponding(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
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
        public static class Proxy implements IActivityController {
            public static IActivityController sDefaultImpl;
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

            @Override // android.app.IActivityController
            public boolean activityStarting(Intent intent, String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(pkg);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().activityStarting(intent, pkg);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityController
            public boolean activityResuming(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().activityResuming(pkg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityController
            public boolean appCrashed(String processName, int pid, String shortMsg, String longMsg, long timeMillis, String stackTrace) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(processName);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(shortMsg);
                        try {
                            _data.writeString(longMsg);
                            _data.writeLong(timeMillis);
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(stackTrace);
                        boolean _result = false;
                        if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() != 0) {
                                _result = true;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        boolean appCrashed = Stub.getDefaultImpl().appCrashed(processName, pid, shortMsg, longMsg, timeMillis, stackTrace);
                        _reply.recycle();
                        _data.recycle();
                        return appCrashed;
                    } catch (Throwable th6) {
                        th = th6;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.app.IActivityController
            public int appEarlyNotResponding(String processName, int pid, String annotation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(pid);
                    _data.writeString(annotation);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().appEarlyNotResponding(processName, pid, annotation);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityController
            public int appNotResponding(String processName, int pid, String processStats) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(pid);
                    _data.writeString(processStats);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().appNotResponding(processName, pid, processStats);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.IActivityController
            public int systemNotResponding(String msg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(msg);
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().systemNotResponding(msg);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IActivityController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IActivityController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
