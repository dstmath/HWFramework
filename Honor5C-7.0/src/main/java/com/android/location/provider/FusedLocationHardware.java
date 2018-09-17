package com.android.location.provider;

import android.hardware.location.IFusedLocationHardware;
import android.hardware.location.IFusedLocationHardwareSink;
import android.hardware.location.IFusedLocationHardwareSink.Stub;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import java.util.HashMap;
import java.util.Map.Entry;

public final class FusedLocationHardware {
    private static final String TAG = "FusedLocationHardware";
    private IFusedLocationHardwareSink mInternalSink;
    private IFusedLocationHardware mLocationHardware;
    HashMap<FusedLocationHardwareSink, DispatcherHandler> mSinkList;

    private class DispatcherHandler extends Handler {
        public static final int DISPATCH_CAPABILITIES = 3;
        public static final int DISPATCH_DIAGNOSTIC_DATA = 2;
        public static final int DISPATCH_LOCATION = 1;
        public static final int DISPATCH_STATUS = 4;

        public DispatcherHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message message) {
            MessageCommand command = message.obj;
            switch (message.what) {
                case DISPATCH_LOCATION /*1*/:
                    command.dispatchLocation();
                case DISPATCH_DIAGNOSTIC_DATA /*2*/:
                    command.dispatchDiagnosticData();
                case DISPATCH_CAPABILITIES /*3*/:
                    command.dispatchCapabilities();
                case DISPATCH_STATUS /*4*/:
                    command.dispatchStatus();
                default:
                    Log.e(FusedLocationHardware.TAG, "Invalid dispatch message");
            }
        }
    }

    private class MessageCommand {
        private final int mCapabilities;
        private final String mData;
        private final Location[] mLocations;
        private final FusedLocationHardwareSink mSink;
        private final int mStatus;

        public MessageCommand(FusedLocationHardwareSink sink, Location[] locations, String data, int capabilities, int status) {
            this.mSink = sink;
            this.mLocations = locations;
            this.mData = data;
            this.mCapabilities = capabilities;
            this.mStatus = status;
        }

        public void dispatchLocation() {
            this.mSink.onLocationAvailable(this.mLocations);
        }

        public void dispatchDiagnosticData() {
            this.mSink.onDiagnosticDataAvailable(this.mData);
        }

        public void dispatchCapabilities() {
            this.mSink.onCapabilities(this.mCapabilities);
        }

        public void dispatchStatus() {
            this.mSink.onStatusChanged(this.mStatus);
        }
    }

    public FusedLocationHardware(IFusedLocationHardware locationHardware) {
        this.mSinkList = new HashMap();
        this.mInternalSink = new Stub() {
            public void onLocationAvailable(Location[] locations) {
                FusedLocationHardware.this.dispatchLocations(locations);
            }

            public void onDiagnosticDataAvailable(String data) {
                FusedLocationHardware.this.dispatchDiagnosticData(data);
            }

            public void onCapabilities(int capabilities) {
                FusedLocationHardware.this.dispatchCapabilities(capabilities);
            }

            public void onStatusChanged(int status) {
                FusedLocationHardware.this.dispatchStatus(status);
            }
        };
        this.mLocationHardware = locationHardware;
    }

    public void registerSink(FusedLocationHardwareSink sink, Looper looper) {
        if (sink == null || looper == null) {
            throw new IllegalArgumentException("Parameter sink and looper cannot be null.");
        }
        synchronized (this.mSinkList) {
            boolean registerSink = this.mSinkList.size() == 0;
            if (this.mSinkList.containsKey(sink)) {
                return;
            }
            HashMap<FusedLocationHardwareSink, DispatcherHandler> newSinkList = new HashMap(this.mSinkList);
            newSinkList.put(sink, new DispatcherHandler(looper));
            this.mSinkList = newSinkList;
            if (registerSink) {
                try {
                    this.mLocationHardware.registerSink(this.mInternalSink);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException at registerSink");
                }
            }
        }
    }

    public void unregisterSink(FusedLocationHardwareSink sink) {
        if (sink == null) {
            throw new IllegalArgumentException("Parameter sink cannot be null.");
        }
        synchronized (this.mSinkList) {
            if (this.mSinkList.containsKey(sink)) {
                HashMap<FusedLocationHardwareSink, DispatcherHandler> newSinkList = new HashMap(this.mSinkList);
                newSinkList.remove(sink);
                boolean unregisterSink = newSinkList.size() == 0;
                this.mSinkList = newSinkList;
                if (unregisterSink) {
                    try {
                        this.mLocationHardware.unregisterSink(this.mInternalSink);
                    } catch (RemoteException e) {
                        Log.e(TAG, "RemoteException at unregisterSink");
                    }
                }
                return;
            }
        }
    }

    public int getSupportedBatchSize() {
        try {
            return this.mLocationHardware.getSupportedBatchSize();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException at getSupportedBatchSize");
            return 0;
        }
    }

    public void startBatching(int id, GmsFusedBatchOptions batchOptions) {
        try {
            this.mLocationHardware.startBatching(id, batchOptions.getParcelableOptions());
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException at startBatching");
        }
    }

    public void stopBatching(int id) {
        try {
            this.mLocationHardware.stopBatching(id);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException at stopBatching");
        }
    }

    public void updateBatchingOptions(int id, GmsFusedBatchOptions batchOptions) {
        try {
            this.mLocationHardware.updateBatchingOptions(id, batchOptions.getParcelableOptions());
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException at updateBatchingOptions");
        }
    }

    public void requestBatchOfLocations(int batchSizeRequest) {
        try {
            this.mLocationHardware.requestBatchOfLocations(batchSizeRequest);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException at requestBatchOfLocations");
        }
    }

    public void flushBatchedLocations() {
        try {
            this.mLocationHardware.flushBatchedLocations();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException at flushBatchedLocations");
        }
    }

    public boolean supportsDiagnosticDataInjection() {
        try {
            return this.mLocationHardware.supportsDiagnosticDataInjection();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException at supportsDiagnisticDataInjection");
            return false;
        }
    }

    public void injectDiagnosticData(String data) {
        try {
            this.mLocationHardware.injectDiagnosticData(data);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException at injectDiagnosticData");
        }
    }

    public boolean supportsDeviceContextInjection() {
        try {
            return this.mLocationHardware.supportsDeviceContextInjection();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException at supportsDeviceContextInjection");
            return false;
        }
    }

    public void injectDeviceContext(int deviceEnabledContext) {
        try {
            this.mLocationHardware.injectDeviceContext(deviceEnabledContext);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException at injectDeviceContext");
        }
    }

    public int getVersion() {
        try {
            return this.mLocationHardware.getVersion();
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException at getVersion");
            return 1;
        }
    }

    private void dispatchLocations(Location[] locations) {
        synchronized (this.mSinkList) {
            HashMap<FusedLocationHardwareSink, DispatcherHandler> sinks = this.mSinkList;
        }
        for (Entry<FusedLocationHardwareSink, DispatcherHandler> entry : sinks.entrySet()) {
            Message.obtain((Handler) entry.getValue(), 1, new MessageCommand((FusedLocationHardwareSink) entry.getKey(), locations, null, 0, 0)).sendToTarget();
        }
    }

    private void dispatchDiagnosticData(String data) {
        synchronized (this.mSinkList) {
            HashMap<FusedLocationHardwareSink, DispatcherHandler> sinks = this.mSinkList;
        }
        for (Entry<FusedLocationHardwareSink, DispatcherHandler> entry : sinks.entrySet()) {
            Message.obtain((Handler) entry.getValue(), 2, new MessageCommand((FusedLocationHardwareSink) entry.getKey(), null, data, 0, 0)).sendToTarget();
        }
    }

    private void dispatchCapabilities(int capabilities) {
        synchronized (this.mSinkList) {
            HashMap<FusedLocationHardwareSink, DispatcherHandler> sinks = this.mSinkList;
        }
        for (Entry<FusedLocationHardwareSink, DispatcherHandler> entry : sinks.entrySet()) {
            Message.obtain((Handler) entry.getValue(), 3, new MessageCommand((FusedLocationHardwareSink) entry.getKey(), null, null, capabilities, 0)).sendToTarget();
        }
    }

    private void dispatchStatus(int status) {
        synchronized (this.mSinkList) {
            HashMap<FusedLocationHardwareSink, DispatcherHandler> sinks = this.mSinkList;
        }
        for (Entry<FusedLocationHardwareSink, DispatcherHandler> entry : sinks.entrySet()) {
            Message.obtain((Handler) entry.getValue(), 4, new MessageCommand((FusedLocationHardwareSink) entry.getKey(), null, null, 0, status)).sendToTarget();
        }
    }
}
