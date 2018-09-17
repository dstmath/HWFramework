package com.android.server.location;

import android.content.Context;
import android.hardware.location.IFusedLocationHardware;
import android.hardware.location.IFusedLocationHardware.Stub;
import android.hardware.location.IFusedLocationHardwareSink;
import android.location.FusedBatchOptions;
import android.os.RemoteException;

public class FusedLocationHardwareSecure extends Stub {
    private final Context mContext;
    private final IFusedLocationHardware mLocationHardware;
    private final String mPermissionId;

    public FusedLocationHardwareSecure(IFusedLocationHardware locationHardware, Context context, String permissionId) {
        this.mLocationHardware = locationHardware;
        this.mContext = context;
        this.mPermissionId = permissionId;
    }

    private void checkPermissions() {
        this.mContext.enforceCallingPermission(this.mPermissionId, String.format("Permission '%s' not granted to access FusedLocationHardware", new Object[]{this.mPermissionId}));
    }

    public void registerSink(IFusedLocationHardwareSink eventSink) throws RemoteException {
        checkPermissions();
        this.mLocationHardware.registerSink(eventSink);
    }

    public void unregisterSink(IFusedLocationHardwareSink eventSink) throws RemoteException {
        checkPermissions();
        this.mLocationHardware.unregisterSink(eventSink);
    }

    public int getSupportedBatchSize() throws RemoteException {
        checkPermissions();
        return this.mLocationHardware.getSupportedBatchSize();
    }

    public void startBatching(int id, FusedBatchOptions batchOptions) throws RemoteException {
        checkPermissions();
        this.mLocationHardware.startBatching(id, batchOptions);
    }

    public void stopBatching(int id) throws RemoteException {
        checkPermissions();
        this.mLocationHardware.stopBatching(id);
    }

    public void updateBatchingOptions(int id, FusedBatchOptions batchoOptions) throws RemoteException {
        checkPermissions();
        this.mLocationHardware.updateBatchingOptions(id, batchoOptions);
    }

    public void requestBatchOfLocations(int batchSizeRequested) throws RemoteException {
        checkPermissions();
        this.mLocationHardware.requestBatchOfLocations(batchSizeRequested);
    }

    public boolean supportsDiagnosticDataInjection() throws RemoteException {
        checkPermissions();
        return this.mLocationHardware.supportsDiagnosticDataInjection();
    }

    public void injectDiagnosticData(String data) throws RemoteException {
        checkPermissions();
        this.mLocationHardware.injectDiagnosticData(data);
    }

    public boolean supportsDeviceContextInjection() throws RemoteException {
        checkPermissions();
        return this.mLocationHardware.supportsDeviceContextInjection();
    }

    public void injectDeviceContext(int deviceEnabledContext) throws RemoteException {
        checkPermissions();
        this.mLocationHardware.injectDeviceContext(deviceEnabledContext);
    }

    public void flushBatchedLocations() throws RemoteException {
        checkPermissions();
        this.mLocationHardware.flushBatchedLocations();
    }

    public int getVersion() throws RemoteException {
        checkPermissions();
        return this.mLocationHardware.getVersion();
    }
}
