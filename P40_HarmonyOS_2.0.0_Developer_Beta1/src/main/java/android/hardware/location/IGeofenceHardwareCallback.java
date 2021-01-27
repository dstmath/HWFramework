package android.hardware.location;

import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGeofenceHardwareCallback extends IInterface {
    void onGeofenceAdd(int i, int i2) throws RemoteException;

    void onGeofencePause(int i, int i2) throws RemoteException;

    void onGeofenceRemove(int i, int i2) throws RemoteException;

    void onGeofenceResume(int i, int i2) throws RemoteException;

    void onGeofenceTransition(int i, int i2, Location location, long j, int i3) throws RemoteException;

    public static class Default implements IGeofenceHardwareCallback {
        @Override // android.hardware.location.IGeofenceHardwareCallback
        public void onGeofenceTransition(int geofenceId, int transition, Location location, long timestamp, int monitoringType) throws RemoteException {
        }

        @Override // android.hardware.location.IGeofenceHardwareCallback
        public void onGeofenceAdd(int geofenceId, int status) throws RemoteException {
        }

        @Override // android.hardware.location.IGeofenceHardwareCallback
        public void onGeofenceRemove(int geofenceId, int status) throws RemoteException {
        }

        @Override // android.hardware.location.IGeofenceHardwareCallback
        public void onGeofencePause(int geofenceId, int status) throws RemoteException {
        }

        @Override // android.hardware.location.IGeofenceHardwareCallback
        public void onGeofenceResume(int geofenceId, int status) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGeofenceHardwareCallback {
        private static final String DESCRIPTOR = "android.hardware.location.IGeofenceHardwareCallback";
        static final int TRANSACTION_onGeofenceAdd = 2;
        static final int TRANSACTION_onGeofencePause = 4;
        static final int TRANSACTION_onGeofenceRemove = 3;
        static final int TRANSACTION_onGeofenceResume = 5;
        static final int TRANSACTION_onGeofenceTransition = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGeofenceHardwareCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeofenceHardwareCallback)) {
                return new Proxy(obj);
            }
            return (IGeofenceHardwareCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "onGeofenceTransition";
            }
            if (transactionCode == 2) {
                return "onGeofenceAdd";
            }
            if (transactionCode == 3) {
                return "onGeofenceRemove";
            }
            if (transactionCode == 4) {
                return "onGeofencePause";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "onGeofenceResume";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Location _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = Location.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                onGeofenceTransition(_arg0, _arg1, _arg2, data.readLong(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onGeofenceAdd(data.readInt(), data.readInt());
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onGeofenceRemove(data.readInt(), data.readInt());
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                onGeofencePause(data.readInt(), data.readInt());
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                onGeofenceResume(data.readInt(), data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGeofenceHardwareCallback {
            public static IGeofenceHardwareCallback sDefaultImpl;
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

            @Override // android.hardware.location.IGeofenceHardwareCallback
            public void onGeofenceTransition(int geofenceId, int transition, Location location, long timestamp, int monitoringType) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(geofenceId);
                    } catch (Throwable th2) {
                        th = th2;
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(transition);
                        if (location != null) {
                            _data.writeInt(1);
                            location.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeLong(timestamp);
                        } catch (Throwable th3) {
                            th = th3;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(monitoringType);
                            try {
                                if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().onGeofenceTransition(geofenceId, transition, location, timestamp, monitoringType);
                                _data.recycle();
                            } catch (Throwable th4) {
                                th = th4;
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th5) {
                            th = th5;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th6) {
                        th = th6;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th7) {
                    th = th7;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.hardware.location.IGeofenceHardwareCallback
            public void onGeofenceAdd(int geofenceId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(status);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGeofenceAdd(geofenceId, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IGeofenceHardwareCallback
            public void onGeofenceRemove(int geofenceId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(status);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGeofenceRemove(geofenceId, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IGeofenceHardwareCallback
            public void onGeofencePause(int geofenceId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(status);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGeofencePause(geofenceId, status);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IGeofenceHardwareCallback
            public void onGeofenceResume(int geofenceId, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(status);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onGeofenceResume(geofenceId, status);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IGeofenceHardwareCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGeofenceHardwareCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
