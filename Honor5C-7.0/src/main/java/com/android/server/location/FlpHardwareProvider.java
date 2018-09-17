package com.android.server.location;

import android.content.Context;
import android.hardware.location.GeofenceHardwareImpl;
import android.hardware.location.GeofenceHardwareRequestParcelable;
import android.hardware.location.IFusedLocationHardware;
import android.hardware.location.IFusedLocationHardware.Stub;
import android.hardware.location.IFusedLocationHardwareSink;
import android.location.FusedBatchOptions;
import android.location.IFusedGeofenceHardware;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Binder;
import android.os.Bundle;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

public class FlpHardwareProvider {
    private static final boolean DEBUG = true;
    private static final int FIRST_VERSION_WITH_FLUSH_LOCATIONS = 2;
    private static final int FLP_GEOFENCE_MONITOR_STATUS_AVAILABLE = 2;
    private static final int FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE = 1;
    private static final int FLP_RESULT_ERROR = -1;
    private static final int FLP_RESULT_ID_EXISTS = -4;
    private static final int FLP_RESULT_ID_UNKNOWN = -5;
    private static final int FLP_RESULT_INSUFFICIENT_MEMORY = -2;
    private static final int FLP_RESULT_INVALID_GEOFENCE_TRANSITION = -6;
    private static final int FLP_RESULT_SUCCESS = 0;
    private static final int FLP_RESULT_TOO_MANY_GEOFENCES = -3;
    public static final String GEOFENCING = "Geofencing";
    public static final String LOCATION = "Location";
    private static final String TAG = "FlpHardwareProvider";
    private static FlpHardwareProvider sSingletonInstance;
    private int mBatchingCapabilities;
    private final Context mContext;
    private final IFusedGeofenceHardware mGeofenceHardwareService;
    private GeofenceHardwareImpl mGeofenceHardwareSink;
    private boolean mHaveBatchingCapabilities;
    private final IFusedLocationHardware mLocationHardware;
    private IFusedLocationHardwareSink mLocationSink;
    private final Object mLocationSinkLock;
    private int mVersion;

    private final class NetworkLocationListener implements LocationListener {
        private NetworkLocationListener() {
        }

        public void onLocationChanged(Location location) {
            if ("network".equals(location.getProvider()) && location.hasAccuracy()) {
                FlpHardwareProvider.this.nativeInjectLocation(location);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.FlpHardwareProvider.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.FlpHardwareProvider.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.FlpHardwareProvider.<clinit>():void");
    }

    private native void nativeAddGeofences(GeofenceHardwareRequestParcelable[] geofenceHardwareRequestParcelableArr);

    private static native void nativeClassInit();

    private native void nativeCleanup();

    private native void nativeFlushBatchedLocations();

    private native int nativeGetBatchSize();

    private native void nativeInit();

    private native void nativeInjectDeviceContext(int i);

    private native void nativeInjectDiagnosticData(String str);

    private native void nativeInjectLocation(Location location);

    private native boolean nativeIsDeviceContextSupported();

    private native boolean nativeIsDiagnosticSupported();

    private native boolean nativeIsGeofencingSupported();

    private static native boolean nativeIsSupported();

    private native void nativeModifyGeofenceOption(int i, int i2, int i3, int i4, int i5, int i6);

    private native void nativePauseGeofence(int i);

    private native void nativeRemoveGeofences(int[] iArr);

    private native void nativeRequestBatchedLocation(int i);

    private native void nativeResumeGeofence(int i, int i2);

    private native void nativeStartBatching(int i, FusedBatchOptions fusedBatchOptions);

    private native void nativeStopBatching(int i);

    private native void nativeUpdateBatchingOptions(int i, FusedBatchOptions fusedBatchOptions);

    public static FlpHardwareProvider getInstance(Context context) {
        if (sSingletonInstance == null) {
            sSingletonInstance = new FlpHardwareProvider(context);
            sSingletonInstance.nativeInit();
        }
        return sSingletonInstance;
    }

    private FlpHardwareProvider(Context context) {
        this.mGeofenceHardwareSink = null;
        this.mLocationSink = null;
        this.mVersion = FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE;
        this.mLocationSinkLock = new Object();
        this.mLocationHardware = new Stub() {
            public void registerSink(IFusedLocationHardwareSink eventSink) {
                synchronized (FlpHardwareProvider.this.mLocationSinkLock) {
                    Log.d(FlpHardwareProvider.TAG, "registerSink from pid:" + Binder.getCallingPid());
                    if (FlpHardwareProvider.this.mLocationSink != null) {
                        Log.e(FlpHardwareProvider.TAG, "Replacing an existing IFusedLocationHardware sink");
                    }
                    FlpHardwareProvider.this.mLocationSink = eventSink;
                }
                FlpHardwareProvider.this.maybeSendCapabilities();
            }

            public void unregisterSink(IFusedLocationHardwareSink eventSink) {
                synchronized (FlpHardwareProvider.this.mLocationSinkLock) {
                    Log.d(FlpHardwareProvider.TAG, "unregisterSink from pid:" + Binder.getCallingPid());
                    if (FlpHardwareProvider.this.mLocationSink == eventSink) {
                        FlpHardwareProvider.this.mLocationSink = null;
                    }
                }
            }

            public int getSupportedBatchSize() {
                Log.d(FlpHardwareProvider.TAG, "getSupportedBatchSize");
                return FlpHardwareProvider.this.nativeGetBatchSize();
            }

            public void startBatching(int requestId, FusedBatchOptions options) {
                if (options == null) {
                    Log.e(FlpHardwareProvider.TAG, "startBatching error, options is null!");
                    return;
                }
                Log.d(FlpHardwareProvider.TAG, "startBatching from pid:" + Binder.getCallingPid() + " requestId:" + requestId + " getPeriodInNS:" + options.getPeriodInNS() + " getSourcesToUse:" + options.getSourcesToUse() + " getFlags:" + options.getFlags());
                FlpHardwareProvider.this.nativeStartBatching(requestId, options);
            }

            public void stopBatching(int requestId) {
                Log.d(FlpHardwareProvider.TAG, " stopBatching from pid:" + Binder.getCallingPid() + " requestId:" + requestId);
                FlpHardwareProvider.this.nativeStopBatching(requestId);
            }

            public void updateBatchingOptions(int requestId, FusedBatchOptions options) {
                if (options == null) {
                    Log.e(FlpHardwareProvider.TAG, "updateBatchingOptions error, options is null!");
                    return;
                }
                Log.d(FlpHardwareProvider.TAG, "updateBatchingOptions from pid:" + Binder.getCallingPid() + " requestId:" + requestId + " getPeriodInNS:" + options.getPeriodInNS() + " getSourcesToUse:" + options.getSourcesToUse() + " getFlags:" + options.getFlags());
                FlpHardwareProvider.this.nativeUpdateBatchingOptions(requestId, options);
            }

            public void requestBatchOfLocations(int batchSizeRequested) {
                Log.d(FlpHardwareProvider.TAG, "requestBatchOfLocations from pid:" + Binder.getCallingPid() + " batchSizeRequested:" + batchSizeRequested);
                FlpHardwareProvider.this.nativeRequestBatchedLocation(batchSizeRequested);
            }

            public void flushBatchedLocations() {
                Log.d(FlpHardwareProvider.TAG, "flushBatchedLocations from pid:" + Binder.getCallingPid());
                if (getVersion() >= FlpHardwareProvider.FLP_GEOFENCE_MONITOR_STATUS_AVAILABLE) {
                    FlpHardwareProvider.this.nativeFlushBatchedLocations();
                } else {
                    Log.wtf(FlpHardwareProvider.TAG, "Tried to call flushBatchedLocations on an unsupported implementation");
                }
            }

            public boolean supportsDiagnosticDataInjection() {
                return FlpHardwareProvider.this.nativeIsDiagnosticSupported();
            }

            public void injectDiagnosticData(String data) {
                FlpHardwareProvider.this.nativeInjectDiagnosticData(data);
            }

            public boolean supportsDeviceContextInjection() {
                return FlpHardwareProvider.this.nativeIsDeviceContextSupported();
            }

            public void injectDeviceContext(int deviceEnabledContext) {
                FlpHardwareProvider.this.nativeInjectDeviceContext(deviceEnabledContext);
            }

            public int getVersion() {
                return FlpHardwareProvider.this.getVersion();
            }
        };
        this.mGeofenceHardwareService = new IFusedGeofenceHardware.Stub() {
            public boolean isSupported() {
                return FlpHardwareProvider.this.nativeIsGeofencingSupported();
            }

            public void addGeofences(GeofenceHardwareRequestParcelable[] geofenceRequestsArray) {
                FlpHardwareProvider.this.nativeAddGeofences(geofenceRequestsArray);
            }

            public void removeGeofences(int[] geofenceIds) {
                FlpHardwareProvider.this.nativeRemoveGeofences(geofenceIds);
            }

            public void pauseMonitoringGeofence(int geofenceId) {
                FlpHardwareProvider.this.nativePauseGeofence(geofenceId);
            }

            public void resumeMonitoringGeofence(int geofenceId, int monitorTransitions) {
                FlpHardwareProvider.this.nativeResumeGeofence(geofenceId, monitorTransitions);
            }

            public void modifyGeofenceOptions(int geofenceId, int lastTransition, int monitorTransitions, int notificationResponsiveness, int unknownTimer, int sourcesToUse) {
                FlpHardwareProvider.this.nativeModifyGeofenceOption(geofenceId, lastTransition, monitorTransitions, notificationResponsiveness, unknownTimer, sourcesToUse);
            }
        };
        this.mContext = context;
        LocationManager manager = (LocationManager) this.mContext.getSystemService("location");
        LocationRequest request = LocationRequest.createFromDeprecatedProvider("passive", 0, 0.0f, false);
        request.setHideFromAppOps(DEBUG);
        manager.requestLocationUpdates(request, new NetworkLocationListener(), Looper.myLooper());
    }

    public static boolean isSupported() {
        return nativeIsSupported();
    }

    private void onLocationReport(Location[] locations) {
        Log.d(TAG, "onLocationReport locations[] size:" + locations.length);
        int length = locations.length;
        for (int i = FLP_RESULT_SUCCESS; i < length; i += FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE) {
            Location location = locations[i];
            location.setProvider("fused");
            location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        }
        synchronized (this.mLocationSinkLock) {
            IFusedLocationHardwareSink sink = this.mLocationSink;
        }
        if (sink != null) {
            try {
                sink.onLocationAvailable(locations);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling onLocationAvailable");
            }
        }
    }

    private void onBatchingCapabilities(int capabilities) {
        synchronized (this.mLocationSinkLock) {
            this.mHaveBatchingCapabilities = DEBUG;
            this.mBatchingCapabilities = capabilities;
        }
        maybeSendCapabilities();
        if (this.mGeofenceHardwareSink != null) {
            this.mGeofenceHardwareSink.setVersion(getVersion());
        }
    }

    private void onBatchingStatus(int status) {
        synchronized (this.mLocationSinkLock) {
            IFusedLocationHardwareSink sink = this.mLocationSink;
        }
        if (sink != null) {
            try {
                sink.onStatusChanged(status);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling onBatchingStatus");
            }
        }
    }

    private int getVersion() {
        synchronized (this.mLocationSinkLock) {
            if (this.mHaveBatchingCapabilities) {
                int i = this.mVersion;
                return i;
            }
            return FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE;
        }
    }

    private void setVersion(int version) {
        this.mVersion = version;
        if (this.mGeofenceHardwareSink != null) {
            this.mGeofenceHardwareSink.setVersion(getVersion());
        }
    }

    private void maybeSendCapabilities() {
        synchronized (this.mLocationSinkLock) {
            IFusedLocationHardwareSink sink = this.mLocationSink;
            boolean haveBatchingCapabilities = this.mHaveBatchingCapabilities;
            int batchingCapabilities = this.mBatchingCapabilities;
        }
        if (sink != null && haveBatchingCapabilities) {
            try {
                sink.onCapabilities(batchingCapabilities);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException calling onLocationAvailable");
            }
        }
    }

    private void onDataReport(String data) {
        synchronized (this.mLocationSinkLock) {
            IFusedLocationHardwareSink sink = this.mLocationSink;
        }
        try {
            if (this.mLocationSink != null) {
                sink.onDiagnosticDataAvailable(data);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException calling onDiagnosticDataAvailable");
        }
    }

    private void onGeofenceTransition(int geofenceId, Location location, int transition, long timestamp, int sourcesUsed) {
        getGeofenceHardwareSink().reportGeofenceTransition(geofenceId, updateLocationInformation(location), transition, timestamp, FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE, sourcesUsed);
    }

    private void onGeofenceMonitorStatus(int status, int source, Location location) {
        int monitorStatus;
        Location updatedLocation = null;
        if (location != null) {
            updatedLocation = updateLocationInformation(location);
        }
        switch (status) {
            case FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE /*1*/:
                monitorStatus = FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE;
                break;
            case FLP_GEOFENCE_MONITOR_STATUS_AVAILABLE /*2*/:
                monitorStatus = FLP_RESULT_SUCCESS;
                break;
            default:
                Log.e(TAG, "Invalid FlpHal Geofence monitor status: " + status);
                monitorStatus = FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE;
                break;
        }
        getGeofenceHardwareSink().reportGeofenceMonitorStatus(FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE, monitorStatus, updatedLocation, source);
    }

    private void onGeofenceAdd(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofenceAddStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofenceRemove(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofenceRemoveStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofencePause(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofencePauseStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofenceResume(int geofenceId, int result) {
        getGeofenceHardwareSink().reportGeofenceResumeStatus(geofenceId, translateToGeofenceHardwareStatus(result));
    }

    private void onGeofencingCapabilities(int capabilities) {
        getGeofenceHardwareSink().onCapabilities(capabilities);
    }

    public IFusedLocationHardware getLocationHardware() {
        return this.mLocationHardware;
    }

    public IFusedGeofenceHardware getGeofenceHardware() {
        return this.mGeofenceHardwareService;
    }

    public void cleanup() {
        Log.i(TAG, "Calling nativeCleanup()");
        nativeCleanup();
    }

    private GeofenceHardwareImpl getGeofenceHardwareSink() {
        if (this.mGeofenceHardwareSink == null) {
            this.mGeofenceHardwareSink = GeofenceHardwareImpl.getInstance(this.mContext);
            this.mGeofenceHardwareSink.setVersion(getVersion());
        }
        return this.mGeofenceHardwareSink;
    }

    private static int translateToGeofenceHardwareStatus(int flpHalResult) {
        switch (flpHalResult) {
            case FLP_RESULT_INVALID_GEOFENCE_TRANSITION /*-6*/:
                return 4;
            case FLP_RESULT_ID_UNKNOWN /*-5*/:
                return 3;
            case FLP_RESULT_ID_EXISTS /*-4*/:
                return FLP_GEOFENCE_MONITOR_STATUS_AVAILABLE;
            case FLP_RESULT_TOO_MANY_GEOFENCES /*-3*/:
                return FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE;
            case FLP_RESULT_INSUFFICIENT_MEMORY /*-2*/:
                return 6;
            case FLP_RESULT_ERROR /*-1*/:
                return 5;
            case FLP_RESULT_SUCCESS /*0*/:
                return FLP_RESULT_SUCCESS;
            default:
                String str = TAG;
                Object[] objArr = new Object[FLP_GEOFENCE_MONITOR_STATUS_UNAVAILABLE];
                objArr[FLP_RESULT_SUCCESS] = Integer.valueOf(flpHalResult);
                Log.e(str, String.format("Invalid FlpHal result code: %d", objArr));
                return 5;
        }
    }

    private Location updateLocationInformation(Location location) {
        location.setProvider("fused");
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        return location;
    }
}
