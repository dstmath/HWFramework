package android.hardware.location;

import android.hardware.location.IGeofenceHardwareCallback.Stub;
import android.location.Location;
import android.os.Build.VERSION;
import android.os.RemoteException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public final class GeofenceHardware {
    public static final int GEOFENCE_ENTERED = 1;
    public static final int GEOFENCE_ERROR_ID_EXISTS = 2;
    public static final int GEOFENCE_ERROR_ID_UNKNOWN = 3;
    public static final int GEOFENCE_ERROR_INSUFFICIENT_MEMORY = 6;
    public static final int GEOFENCE_ERROR_INVALID_TRANSITION = 4;
    public static final int GEOFENCE_ERROR_TOO_MANY_GEOFENCES = 1;
    public static final int GEOFENCE_EXITED = 2;
    public static final int GEOFENCE_FAILURE = 5;
    public static final int GEOFENCE_SUCCESS = 0;
    public static final int GEOFENCE_UNCERTAIN = 4;
    public static final int MONITORING_TYPE_FUSED_HARDWARE = 1;
    public static final int MONITORING_TYPE_GPS_HARDWARE = 0;
    public static final int MONITOR_CURRENTLY_AVAILABLE = 0;
    public static final int MONITOR_CURRENTLY_UNAVAILABLE = 1;
    public static final int MONITOR_UNSUPPORTED = 2;
    static final int NUM_MONITORS = 2;
    public static final int SOURCE_TECHNOLOGY_BLUETOOTH = 16;
    public static final int SOURCE_TECHNOLOGY_CELL = 8;
    public static final int SOURCE_TECHNOLOGY_GNSS = 1;
    public static final int SOURCE_TECHNOLOGY_SENSORS = 4;
    public static final int SOURCE_TECHNOLOGY_WIFI = 2;
    private HashMap<GeofenceHardwareCallback, GeofenceHardwareCallbackWrapper> mCallbacks = new HashMap();
    private HashMap<GeofenceHardwareMonitorCallback, GeofenceHardwareMonitorCallbackWrapper> mMonitorCallbacks = new HashMap();
    private IGeofenceHardware mService;

    class GeofenceHardwareCallbackWrapper extends Stub {
        private WeakReference<GeofenceHardwareCallback> mCallback;

        GeofenceHardwareCallbackWrapper(GeofenceHardwareCallback c) {
            this.mCallback = new WeakReference(c);
        }

        public void onGeofenceTransition(int geofenceId, int transition, Location location, long timestamp, int monitoringType) {
            GeofenceHardwareCallback c = (GeofenceHardwareCallback) this.mCallback.get();
            if (c != null) {
                c.onGeofenceTransition(geofenceId, transition, location, timestamp, monitoringType);
            }
        }

        public void onGeofenceAdd(int geofenceId, int status) {
            GeofenceHardwareCallback c = (GeofenceHardwareCallback) this.mCallback.get();
            if (c != null) {
                c.onGeofenceAdd(geofenceId, status);
            }
        }

        public void onGeofenceRemove(int geofenceId, int status) {
            GeofenceHardwareCallback c = (GeofenceHardwareCallback) this.mCallback.get();
            if (c != null) {
                c.onGeofenceRemove(geofenceId, status);
                GeofenceHardware.this.removeCallback(c);
            }
        }

        public void onGeofencePause(int geofenceId, int status) {
            GeofenceHardwareCallback c = (GeofenceHardwareCallback) this.mCallback.get();
            if (c != null) {
                c.onGeofencePause(geofenceId, status);
            }
        }

        public void onGeofenceResume(int geofenceId, int status) {
            GeofenceHardwareCallback c = (GeofenceHardwareCallback) this.mCallback.get();
            if (c != null) {
                c.onGeofenceResume(geofenceId, status);
            }
        }
    }

    class GeofenceHardwareMonitorCallbackWrapper extends IGeofenceHardwareMonitorCallback.Stub {
        private WeakReference<GeofenceHardwareMonitorCallback> mCallback;

        GeofenceHardwareMonitorCallbackWrapper(GeofenceHardwareMonitorCallback c) {
            this.mCallback = new WeakReference(c);
        }

        public void onMonitoringSystemChange(GeofenceHardwareMonitorEvent event) {
            boolean z = false;
            GeofenceHardwareMonitorCallback c = (GeofenceHardwareMonitorCallback) this.mCallback.get();
            if (c != null) {
                int monitoringType = event.getMonitoringType();
                if (event.getMonitoringStatus() == 0) {
                    z = true;
                }
                c.onMonitoringSystemChange(monitoringType, z, event.getLocation());
                if (VERSION.SDK_INT >= 21) {
                    c.onMonitoringSystemChange(event);
                }
            }
        }
    }

    public GeofenceHardware(IGeofenceHardware service) {
        this.mService = service;
    }

    public int[] getMonitoringTypes() {
        try {
            return this.mService.getMonitoringTypes();
        } catch (RemoteException e) {
            return new int[0];
        }
    }

    public int getStatusOfMonitoringType(int monitoringType) {
        try {
            return this.mService.getStatusOfMonitoringType(monitoringType);
        } catch (RemoteException e) {
            return 2;
        }
    }

    public boolean addGeofence(int geofenceId, int monitoringType, GeofenceHardwareRequest geofenceRequest, GeofenceHardwareCallback callback) {
        try {
            if (geofenceRequest.getType() == 0) {
                return this.mService.addCircularFence(monitoringType, new GeofenceHardwareRequestParcelable(geofenceId, geofenceRequest), getCallbackWrapper(callback));
            }
            throw new IllegalArgumentException("Geofence Request type not supported");
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean removeGeofence(int geofenceId, int monitoringType) {
        try {
            return this.mService.removeGeofence(geofenceId, monitoringType);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean pauseGeofence(int geofenceId, int monitoringType) {
        try {
            return this.mService.pauseGeofence(geofenceId, monitoringType);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean resumeGeofence(int geofenceId, int monitoringType, int monitorTransition) {
        try {
            return this.mService.resumeGeofence(geofenceId, monitoringType, monitorTransition);
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean registerForMonitorStateChangeCallback(int monitoringType, GeofenceHardwareMonitorCallback callback) {
        try {
            return this.mService.registerForMonitorStateChangeCallback(monitoringType, getMonitorCallbackWrapper(callback));
        } catch (RemoteException e) {
            return false;
        }
    }

    public boolean unregisterForMonitorStateChangeCallback(int monitoringType, GeofenceHardwareMonitorCallback callback) {
        boolean result = false;
        try {
            result = this.mService.unregisterForMonitorStateChangeCallback(monitoringType, getMonitorCallbackWrapper(callback));
            if (result) {
                removeMonitorCallback(callback);
            }
        } catch (RemoteException e) {
        }
        return result;
    }

    private void removeCallback(GeofenceHardwareCallback callback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(callback);
        }
    }

    private GeofenceHardwareCallbackWrapper getCallbackWrapper(GeofenceHardwareCallback callback) {
        GeofenceHardwareCallbackWrapper wrapper;
        synchronized (this.mCallbacks) {
            wrapper = (GeofenceHardwareCallbackWrapper) this.mCallbacks.get(callback);
            if (wrapper == null) {
                wrapper = new GeofenceHardwareCallbackWrapper(callback);
                this.mCallbacks.put(callback, wrapper);
            }
        }
        return wrapper;
    }

    private void removeMonitorCallback(GeofenceHardwareMonitorCallback callback) {
        synchronized (this.mMonitorCallbacks) {
            this.mMonitorCallbacks.remove(callback);
        }
    }

    private GeofenceHardwareMonitorCallbackWrapper getMonitorCallbackWrapper(GeofenceHardwareMonitorCallback callback) {
        GeofenceHardwareMonitorCallbackWrapper wrapper;
        synchronized (this.mMonitorCallbacks) {
            wrapper = (GeofenceHardwareMonitorCallbackWrapper) this.mMonitorCallbacks.get(callback);
            if (wrapper == null) {
                wrapper = new GeofenceHardwareMonitorCallbackWrapper(callback);
                this.mMonitorCallbacks.put(callback, wrapper);
            }
        }
        return wrapper;
    }
}
