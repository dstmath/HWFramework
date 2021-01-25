package com.google.android.startop.iorap;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.startop.iorap.ITaskListener;

public interface IIorap extends IInterface {
    void onAppIntentEvent(RequestId requestId, AppIntentEvent appIntentEvent) throws RemoteException;

    void onAppLaunchEvent(RequestId requestId, AppLaunchEvent appLaunchEvent) throws RemoteException;

    void onPackageEvent(RequestId requestId, PackageEvent packageEvent) throws RemoteException;

    void onSystemServiceEvent(RequestId requestId, SystemServiceEvent systemServiceEvent) throws RemoteException;

    void onSystemServiceUserEvent(RequestId requestId, SystemServiceUserEvent systemServiceUserEvent) throws RemoteException;

    void setTaskListener(ITaskListener iTaskListener) throws RemoteException;

    public static class Default implements IIorap {
        @Override // com.google.android.startop.iorap.IIorap
        public void setTaskListener(ITaskListener listener) throws RemoteException {
        }

        @Override // com.google.android.startop.iorap.IIorap
        public void onAppLaunchEvent(RequestId request, AppLaunchEvent event) throws RemoteException {
        }

        @Override // com.google.android.startop.iorap.IIorap
        public void onPackageEvent(RequestId request, PackageEvent event) throws RemoteException {
        }

        @Override // com.google.android.startop.iorap.IIorap
        public void onAppIntentEvent(RequestId request, AppIntentEvent event) throws RemoteException {
        }

        @Override // com.google.android.startop.iorap.IIorap
        public void onSystemServiceEvent(RequestId request, SystemServiceEvent event) throws RemoteException {
        }

        @Override // com.google.android.startop.iorap.IIorap
        public void onSystemServiceUserEvent(RequestId request, SystemServiceUserEvent event) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIorap {
        private static final String DESCRIPTOR = "com.google.android.startop.iorap.IIorap";
        static final int TRANSACTION_onAppIntentEvent = 4;
        static final int TRANSACTION_onAppLaunchEvent = 2;
        static final int TRANSACTION_onPackageEvent = 3;
        static final int TRANSACTION_onSystemServiceEvent = 5;
        static final int TRANSACTION_onSystemServiceUserEvent = 6;
        static final int TRANSACTION_setTaskListener = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIorap asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIorap)) {
                return new Proxy(obj);
            }
            return (IIorap) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RequestId _arg0;
            AppLaunchEvent _arg1;
            RequestId _arg02;
            PackageEvent _arg12;
            RequestId _arg03;
            AppIntentEvent _arg13;
            RequestId _arg04;
            SystemServiceEvent _arg14;
            RequestId _arg05;
            SystemServiceUserEvent _arg15;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setTaskListener(ITaskListener.Stub.asInterface(data.readStrongBinder()));
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = RequestId.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = AppLaunchEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        onAppLaunchEvent(_arg0, _arg1);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = RequestId.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = PackageEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        onPackageEvent(_arg02, _arg12);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = RequestId.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg13 = AppIntentEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        onAppIntentEvent(_arg03, _arg13);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = RequestId.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg14 = SystemServiceEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        onSystemServiceEvent(_arg04, _arg14);
                        return true;
                    case TRANSACTION_onSystemServiceUserEvent /* 6 */:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = RequestId.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg15 = SystemServiceUserEvent.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        onSystemServiceUserEvent(_arg05, _arg15);
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
        public static class Proxy implements IIorap {
            public static IIorap sDefaultImpl;
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

            @Override // com.google.android.startop.iorap.IIorap
            public void setTaskListener(ITaskListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setTaskListener(listener);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.google.android.startop.iorap.IIorap
            public void onAppLaunchEvent(RequestId request, AppLaunchEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAppLaunchEvent(request, event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.google.android.startop.iorap.IIorap
            public void onPackageEvent(RequestId request, PackageEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onPackageEvent(request, event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.google.android.startop.iorap.IIorap
            public void onAppIntentEvent(RequestId request, AppIntentEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAppIntentEvent(request, event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.google.android.startop.iorap.IIorap
            public void onSystemServiceEvent(RequestId request, SystemServiceEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSystemServiceEvent(request, event);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.google.android.startop.iorap.IIorap
            public void onSystemServiceUserEvent(RequestId request, SystemServiceUserEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (event != null) {
                        _data.writeInt(1);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(Stub.TRANSACTION_onSystemServiceUserEvent, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onSystemServiceUserEvent(request, event);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IIorap impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIorap getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
