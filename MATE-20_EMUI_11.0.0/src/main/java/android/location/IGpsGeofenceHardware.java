package android.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGpsGeofenceHardware extends IInterface {
    boolean addCircularHardwareGeofence(int i, double d, double d2, double d3, int i2, int i3, int i4, int i5) throws RemoteException;

    boolean isHardwareGeofenceSupported() throws RemoteException;

    boolean pauseHardwareGeofence(int i) throws RemoteException;

    boolean removeHardwareGeofence(int i) throws RemoteException;

    boolean resumeHardwareGeofence(int i, int i2) throws RemoteException;

    public static class Default implements IGpsGeofenceHardware {
        @Override // android.location.IGpsGeofenceHardware
        public boolean isHardwareGeofenceSupported() throws RemoteException {
            return false;
        }

        @Override // android.location.IGpsGeofenceHardware
        public boolean addCircularHardwareGeofence(int geofenceId, double latitude, double longitude, double radius, int lastTransition, int monitorTransition, int notificationResponsiveness, int unknownTimer) throws RemoteException {
            return false;
        }

        @Override // android.location.IGpsGeofenceHardware
        public boolean removeHardwareGeofence(int geofenceId) throws RemoteException {
            return false;
        }

        @Override // android.location.IGpsGeofenceHardware
        public boolean pauseHardwareGeofence(int geofenceId) throws RemoteException {
            return false;
        }

        @Override // android.location.IGpsGeofenceHardware
        public boolean resumeHardwareGeofence(int geofenceId, int monitorTransition) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGpsGeofenceHardware {
        private static final String DESCRIPTOR = "android.location.IGpsGeofenceHardware";
        static final int TRANSACTION_addCircularHardwareGeofence = 2;
        static final int TRANSACTION_isHardwareGeofenceSupported = 1;
        static final int TRANSACTION_pauseHardwareGeofence = 4;
        static final int TRANSACTION_removeHardwareGeofence = 3;
        static final int TRANSACTION_resumeHardwareGeofence = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGpsGeofenceHardware asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGpsGeofenceHardware)) {
                return new Proxy(obj);
            }
            return (IGpsGeofenceHardware) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "isHardwareGeofenceSupported";
            }
            if (transactionCode == 2) {
                return "addCircularHardwareGeofence";
            }
            if (transactionCode == 3) {
                return "removeHardwareGeofence";
            }
            if (transactionCode == 4) {
                return "pauseHardwareGeofence";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "resumeHardwareGeofence";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean isHardwareGeofenceSupported = isHardwareGeofenceSupported();
                reply.writeNoException();
                reply.writeInt(isHardwareGeofenceSupported ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                boolean addCircularHardwareGeofence = addCircularHardwareGeofence(data.readInt(), data.readDouble(), data.readDouble(), data.readDouble(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(addCircularHardwareGeofence ? 1 : 0);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean removeHardwareGeofence = removeHardwareGeofence(data.readInt());
                reply.writeNoException();
                reply.writeInt(removeHardwareGeofence ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                boolean pauseHardwareGeofence = pauseHardwareGeofence(data.readInt());
                reply.writeNoException();
                reply.writeInt(pauseHardwareGeofence ? 1 : 0);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                boolean resumeHardwareGeofence = resumeHardwareGeofence(data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(resumeHardwareGeofence ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IGpsGeofenceHardware {
            public static IGpsGeofenceHardware sDefaultImpl;
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

            @Override // android.location.IGpsGeofenceHardware
            public boolean isHardwareGeofenceSupported() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isHardwareGeofenceSupported();
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

            @Override // android.location.IGpsGeofenceHardware
            public boolean addCircularHardwareGeofence(int geofenceId, double latitude, double longitude, double radius, int lastTransition, int monitorTransition, int notificationResponsiveness, int unknownTimer) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(geofenceId);
                        _data.writeDouble(latitude);
                        _data.writeDouble(longitude);
                        _data.writeDouble(radius);
                        _data.writeInt(lastTransition);
                        _data.writeInt(monitorTransition);
                        _data.writeInt(notificationResponsiveness);
                        _data.writeInt(unknownTimer);
                        boolean _result = false;
                        if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() != 0) {
                                _result = true;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        boolean addCircularHardwareGeofence = Stub.getDefaultImpl().addCircularHardwareGeofence(geofenceId, latitude, longitude, radius, lastTransition, monitorTransition, notificationResponsiveness, unknownTimer);
                        _reply.recycle();
                        _data.recycle();
                        return addCircularHardwareGeofence;
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // android.location.IGpsGeofenceHardware
            public boolean removeHardwareGeofence(int geofenceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeHardwareGeofence(geofenceId);
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

            @Override // android.location.IGpsGeofenceHardware
            public boolean pauseHardwareGeofence(int geofenceId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    boolean _result = false;
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().pauseHardwareGeofence(geofenceId);
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

            @Override // android.location.IGpsGeofenceHardware
            public boolean resumeHardwareGeofence(int geofenceId, int monitorTransition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(geofenceId);
                    _data.writeInt(monitorTransition);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resumeHardwareGeofence(geofenceId, monitorTransition);
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
        }

        public static boolean setDefaultImpl(IGpsGeofenceHardware impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGpsGeofenceHardware getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
