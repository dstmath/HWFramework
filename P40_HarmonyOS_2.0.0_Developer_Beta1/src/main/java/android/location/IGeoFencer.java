package android.location;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGeoFencer extends IInterface {
    void clearGeoFence(IBinder iBinder, PendingIntent pendingIntent) throws RemoteException;

    void clearGeoFenceUser(int i) throws RemoteException;

    boolean setGeoFence(IBinder iBinder, GeoFenceParams geoFenceParams) throws RemoteException;

    public static class Default implements IGeoFencer {
        @Override // android.location.IGeoFencer
        public boolean setGeoFence(IBinder who, GeoFenceParams params) throws RemoteException {
            return false;
        }

        @Override // android.location.IGeoFencer
        public void clearGeoFence(IBinder who, PendingIntent fence) throws RemoteException {
        }

        @Override // android.location.IGeoFencer
        public void clearGeoFenceUser(int uid) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGeoFencer {
        private static final String DESCRIPTOR = "android.location.IGeoFencer";
        static final int TRANSACTION_clearGeoFence = 2;
        static final int TRANSACTION_clearGeoFenceUser = 3;
        static final int TRANSACTION_setGeoFence = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGeoFencer asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeoFencer)) {
                return new Proxy(obj);
            }
            return (IGeoFencer) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            GeoFenceParams _arg1;
            PendingIntent _arg12;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IBinder _arg0 = data.readStrongBinder();
                if (data.readInt() != 0) {
                    _arg1 = GeoFenceParams.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                boolean geoFence = setGeoFence(_arg0, _arg1);
                reply.writeNoException();
                reply.writeInt(geoFence ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                IBinder _arg02 = data.readStrongBinder();
                if (data.readInt() != 0) {
                    _arg12 = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                clearGeoFence(_arg02, _arg12);
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                clearGeoFenceUser(data.readInt());
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
        public static class Proxy implements IGeoFencer {
            public static IGeoFencer sDefaultImpl;
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

            @Override // android.location.IGeoFencer
            public boolean setGeoFence(IBinder who, GeoFenceParams params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(who);
                    boolean _result = true;
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setGeoFence(who, params);
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

            @Override // android.location.IGeoFencer
            public void clearGeoFence(IBinder who, PendingIntent fence) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(who);
                    if (fence != null) {
                        _data.writeInt(1);
                        fence.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearGeoFence(who, fence);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.location.IGeoFencer
            public void clearGeoFenceUser(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearGeoFenceUser(uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGeoFencer impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGeoFencer getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
