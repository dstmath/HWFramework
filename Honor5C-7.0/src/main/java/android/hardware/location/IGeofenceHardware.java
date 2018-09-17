package android.hardware.location;

import android.location.IFusedGeofenceHardware;
import android.location.IGpsGeofenceHardware;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGeofenceHardware extends IInterface {

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

        private static class Proxy implements IGeofenceHardware {
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

            public void setGpsGeofenceHardware(IGpsGeofenceHardware service) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        iBinder = service.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setGpsGeofenceHardware, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setFusedGeofenceHardware(IFusedGeofenceHardware service) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (service != null) {
                        iBinder = service.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setFusedGeofenceHardware, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getMonitoringTypes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_getMonitoringTypes, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStatusOfMonitoringType(int monitoringType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(monitoringType);
                    this.mRemote.transact(Stub.TRANSACTION_getStatusOfMonitoringType, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addCircularFence(int monitoringType, GeofenceHardwareRequestParcelable request, IGeofenceHardwareCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(monitoringType);
                    if (request != null) {
                        _data.writeInt(Stub.TRANSACTION_setGpsGeofenceHardware);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_addCircularFence, _data, _reply, 0);
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

            public boolean removeGeofence(int id, int monitoringType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(monitoringType);
                    this.mRemote.transact(Stub.TRANSACTION_removeGeofence, _data, _reply, 0);
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

            public boolean pauseGeofence(int id, int monitoringType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(monitoringType);
                    this.mRemote.transact(Stub.TRANSACTION_pauseGeofence, _data, _reply, 0);
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

            public boolean resumeGeofence(int id, int monitoringType, int monitorTransitions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    _data.writeInt(monitoringType);
                    _data.writeInt(monitorTransitions);
                    this.mRemote.transact(Stub.TRANSACTION_resumeGeofence, _data, _reply, 0);
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

            public boolean registerForMonitorStateChangeCallback(int monitoringType, IGeofenceHardwareMonitorCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(monitoringType);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_registerForMonitorStateChangeCallback, _data, _reply, 0);
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

            public boolean unregisterForMonitorStateChangeCallback(int monitoringType, IGeofenceHardwareMonitorCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(monitoringType);
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_unregisterForMonitorStateChangeCallback, _data, _reply, 0);
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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            switch (code) {
                case TRANSACTION_setGpsGeofenceHardware /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setGpsGeofenceHardware(android.location.IGpsGeofenceHardware.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_setFusedGeofenceHardware /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    setFusedGeofenceHardware(android.location.IFusedGeofenceHardware.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getMonitoringTypes /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    int[] _result2 = getMonitoringTypes();
                    reply.writeNoException();
                    reply.writeIntArray(_result2);
                    return true;
                case TRANSACTION_getStatusOfMonitoringType /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    int _result3 = getStatusOfMonitoringType(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_addCircularFence /*5*/:
                    GeofenceHardwareRequestParcelable geofenceHardwareRequestParcelable;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        geofenceHardwareRequestParcelable = (GeofenceHardwareRequestParcelable) GeofenceHardwareRequestParcelable.CREATOR.createFromParcel(data);
                    } else {
                        geofenceHardwareRequestParcelable = null;
                    }
                    _result = addCircularFence(_arg0, geofenceHardwareRequestParcelable, android.hardware.location.IGeofenceHardwareCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setGpsGeofenceHardware : 0);
                    return true;
                case TRANSACTION_removeGeofence /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = removeGeofence(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setGpsGeofenceHardware : 0);
                    return true;
                case TRANSACTION_pauseGeofence /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = pauseGeofence(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setGpsGeofenceHardware : 0);
                    return true;
                case TRANSACTION_resumeGeofence /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = resumeGeofence(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setGpsGeofenceHardware : 0);
                    return true;
                case TRANSACTION_registerForMonitorStateChangeCallback /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = registerForMonitorStateChangeCallback(data.readInt(), android.hardware.location.IGeofenceHardwareMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setGpsGeofenceHardware : 0);
                    return true;
                case TRANSACTION_unregisterForMonitorStateChangeCallback /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = unregisterForMonitorStateChangeCallback(data.readInt(), android.hardware.location.IGeofenceHardwareMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setGpsGeofenceHardware : 0);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

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
}
