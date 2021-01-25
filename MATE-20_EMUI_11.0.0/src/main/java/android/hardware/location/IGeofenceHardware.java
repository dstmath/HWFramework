package android.hardware.location;

import android.hardware.location.IGeofenceHardwareCallback;
import android.hardware.location.IGeofenceHardwareMonitorCallback;
import android.location.IFusedGeofenceHardware;
import android.location.IGpsGeofenceHardware;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGeofenceHardware extends IInterface {
    boolean addCircularFence(int i, GeofenceHardwareRequestParcelable geofenceHardwareRequestParcelable, IGeofenceHardwareCallback iGeofenceHardwareCallback) throws RemoteException;

    int[] getMonitoringTypes() throws RemoteException;

    int getStatusOfMonitoringType(int i) throws RemoteException;

    boolean pauseGeofence(int i, int i2) throws RemoteException;

    boolean registerForMonitorStateChangeCallback(int i, IGeofenceHardwareMonitorCallback iGeofenceHardwareMonitorCallback) throws RemoteException;

    boolean removeGeofence(int i, int i2) throws RemoteException;

    boolean resumeGeofence(int i, int i2, int i3) throws RemoteException;

    void setFusedGeofenceHardware(IFusedGeofenceHardware iFusedGeofenceHardware) throws RemoteException;

    void setGpsGeofenceHardware(IGpsGeofenceHardware iGpsGeofenceHardware) throws RemoteException;

    boolean unregisterForMonitorStateChangeCallback(int i, IGeofenceHardwareMonitorCallback iGeofenceHardwareMonitorCallback) throws RemoteException;

    public static class Default implements IGeofenceHardware {
        @Override // android.hardware.location.IGeofenceHardware
        public void setGpsGeofenceHardware(IGpsGeofenceHardware service) throws RemoteException {
        }

        @Override // android.hardware.location.IGeofenceHardware
        public void setFusedGeofenceHardware(IFusedGeofenceHardware service) throws RemoteException {
        }

        @Override // android.hardware.location.IGeofenceHardware
        public int[] getMonitoringTypes() throws RemoteException {
            return null;
        }

        @Override // android.hardware.location.IGeofenceHardware
        public int getStatusOfMonitoringType(int monitoringType) throws RemoteException {
            return 0;
        }

        @Override // android.hardware.location.IGeofenceHardware
        public boolean addCircularFence(int monitoringType, GeofenceHardwareRequestParcelable request, IGeofenceHardwareCallback callback) throws RemoteException {
            return false;
        }

        @Override // android.hardware.location.IGeofenceHardware
        public boolean removeGeofence(int id, int monitoringType) throws RemoteException {
            return false;
        }

        @Override // android.hardware.location.IGeofenceHardware
        public boolean pauseGeofence(int id, int monitoringType) throws RemoteException {
            return false;
        }

        @Override // android.hardware.location.IGeofenceHardware
        public boolean resumeGeofence(int id, int monitoringType, int monitorTransitions) throws RemoteException {
            return false;
        }

        @Override // android.hardware.location.IGeofenceHardware
        public boolean registerForMonitorStateChangeCallback(int monitoringType, IGeofenceHardwareMonitorCallback callback) throws RemoteException {
            return false;
        }

        @Override // android.hardware.location.IGeofenceHardware
        public boolean unregisterForMonitorStateChangeCallback(int monitoringType, IGeofenceHardwareMonitorCallback callback) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IGeofenceHardware {
        private static final String DESCRIPTOR = "android.hardware.location.IGeofenceHardware";
        static final int TRANSACTION_addCircularFence = 5;
        static final int TRANSACTION_getMonitoringTypes = 3;
        static final int TRANSACTION_getStatusOfMonitoringType = 4;
        static final int TRANSACTION_pauseGeofence = 7;
        static final int TRANSACTION_registerForMonitorStateChangeCallback = 9;
        static final int TRANSACTION_removeGeofence = 6;
        static final int TRANSACTION_resumeGeofence = 8;
        static final int TRANSACTION_setFusedGeofenceHardware = 2;
        static final int TRANSACTION_setGpsGeofenceHardware = 1;
        static final int TRANSACTION_unregisterForMonitorStateChangeCallback = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGeofenceHardware asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeofenceHardware)) {
                return new Proxy(obj);
            }
            return (IGeofenceHardware) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setGpsGeofenceHardware";
                case 2:
                    return "setFusedGeofenceHardware";
                case 3:
                    return "getMonitoringTypes";
                case 4:
                    return "getStatusOfMonitoringType";
                case 5:
                    return "addCircularFence";
                case 6:
                    return "removeGeofence";
                case 7:
                    return "pauseGeofence";
                case 8:
                    return "resumeGeofence";
                case 9:
                    return "registerForMonitorStateChangeCallback";
                case 10:
                    return "unregisterForMonitorStateChangeCallback";
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
            GeofenceHardwareRequestParcelable _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        setGpsGeofenceHardware(IGpsGeofenceHardware.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        setFusedGeofenceHardware(IFusedGeofenceHardware.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int[] _result = getMonitoringTypes();
                        reply.writeNoException();
                        reply.writeIntArray(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = getStatusOfMonitoringType(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = GeofenceHardwareRequestParcelable.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        boolean addCircularFence = addCircularFence(_arg0, _arg1, IGeofenceHardwareCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(addCircularFence ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeGeofence = removeGeofence(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(removeGeofence ? 1 : 0);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        boolean pauseGeofence = pauseGeofence(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(pauseGeofence ? 1 : 0);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        boolean resumeGeofence = resumeGeofence(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(resumeGeofence ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerForMonitorStateChangeCallback = registerForMonitorStateChangeCallback(data.readInt(), IGeofenceHardwareMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerForMonitorStateChangeCallback ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unregisterForMonitorStateChangeCallback = unregisterForMonitorStateChangeCallback(data.readInt(), IGeofenceHardwareMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unregisterForMonitorStateChangeCallback ? 1 : 0);
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
        public static class Proxy implements IGeofenceHardware {
            public static IGeofenceHardware sDefaultImpl;
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

            @Override // android.hardware.location.IGeofenceHardware
            public void setGpsGeofenceHardware(IGpsGeofenceHardware service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(service != null ? service.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setGpsGeofenceHardware(service);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IGeofenceHardware
            public void setFusedGeofenceHardware(IFusedGeofenceHardware service) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(service != null ? service.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setFusedGeofenceHardware(service);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IGeofenceHardware
            public int[] getMonitoringTypes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMonitoringTypes();
                    }
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.hardware.location.IGeofenceHardware
            public int getStatusOfMonitoringType(int monitoringType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(monitoringType);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStatusOfMonitoringType(monitoringType);
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

            @Override // android.hardware.location.IGeofenceHardware
            public boolean addCircularFence(int monitoringType, GeofenceHardwareRequestParcelable request, IGeofenceHardwareCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(monitoringType);
                    boolean _result = true;
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addCircularFence(monitoringType, request, callback);
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

            @Override // android.hardware.location.IGeofenceHardware
            public boolean removeGeofence(int id, int monitoringType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(monitoringType);
                    boolean _result = false;
                    if (!this.mRemote.transact(6, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeGeofence(id, monitoringType);
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

            @Override // android.hardware.location.IGeofenceHardware
            public boolean pauseGeofence(int id, int monitoringType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(monitoringType);
                    boolean _result = false;
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().pauseGeofence(id, monitoringType);
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

            @Override // android.hardware.location.IGeofenceHardware
            public boolean resumeGeofence(int id, int monitoringType, int monitorTransitions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(monitoringType);
                    _data.writeInt(monitorTransitions);
                    boolean _result = false;
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resumeGeofence(id, monitoringType, monitorTransitions);
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

            @Override // android.hardware.location.IGeofenceHardware
            public boolean registerForMonitorStateChangeCallback(int monitoringType, IGeofenceHardwareMonitorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(monitoringType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerForMonitorStateChangeCallback(monitoringType, callback);
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

            @Override // android.hardware.location.IGeofenceHardware
            public boolean unregisterForMonitorStateChangeCallback(int monitoringType, IGeofenceHardwareMonitorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(monitoringType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unregisterForMonitorStateChangeCallback(monitoringType, callback);
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

        public static boolean setDefaultImpl(IGeofenceHardware impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IGeofenceHardware getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
