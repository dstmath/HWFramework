package android.hardware.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IGeofenceHardwareMonitorCallback extends IInterface {

    public static abstract class Stub extends Binder implements IGeofenceHardwareMonitorCallback {
        private static final String DESCRIPTOR = "android.hardware.location.IGeofenceHardwareMonitorCallback";
        static final int TRANSACTION_onMonitoringSystemChange = 1;

        private static class Proxy implements IGeofenceHardwareMonitorCallback {
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

            public void onMonitoringSystemChange(GeofenceHardwareMonitorEvent event) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (event != null) {
                        _data.writeInt(Stub.TRANSACTION_onMonitoringSystemChange);
                        event.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onMonitoringSystemChange, _data, null, Stub.TRANSACTION_onMonitoringSystemChange);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IGeofenceHardwareMonitorCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IGeofenceHardwareMonitorCallback)) {
                return new Proxy(obj);
            }
            return (IGeofenceHardwareMonitorCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onMonitoringSystemChange /*1*/:
                    GeofenceHardwareMonitorEvent geofenceHardwareMonitorEvent;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        geofenceHardwareMonitorEvent = (GeofenceHardwareMonitorEvent) GeofenceHardwareMonitorEvent.CREATOR.createFromParcel(data);
                    } else {
                        geofenceHardwareMonitorEvent = null;
                    }
                    onMonitoringSystemChange(geofenceHardwareMonitorEvent);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onMonitoringSystemChange(GeofenceHardwareMonitorEvent geofenceHardwareMonitorEvent) throws RemoteException;
}
