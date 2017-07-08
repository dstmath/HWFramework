package android.net;

import android.app.PendingIntent;
import android.net.ConnectivityMetricsEvent.Reference;
import android.net.IConnectivityMetricsLogger.Stub;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public class ConnectivityMetricsLogger {
    public static final int COMPONENT_TAG_BLUETOOTH = 1;
    public static final int COMPONENT_TAG_CONNECTIVITY = 0;
    public static final int COMPONENT_TAG_TELECOM = 3;
    public static final int COMPONENT_TAG_TELEPHONY = 4;
    public static final int COMPONENT_TAG_WIFI = 2;
    public static final String CONNECTIVITY_METRICS_LOGGER_SERVICE = "connectivity_metrics_logger";
    public static final String DATA_KEY_EVENTS_COUNT = "count";
    private static final boolean DBG = true;
    public static final int NUMBER_OF_COMPONENTS = 5;
    private static String TAG = null;
    public static final int TAG_SKIPPED_EVENTS = -1;
    private int mNumSkippedEvents;
    private IConnectivityMetricsLogger mService;
    private long mServiceUnblockedTimestampMillis;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.net.ConnectivityMetricsLogger.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.net.ConnectivityMetricsLogger.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.ConnectivityMetricsLogger.<clinit>():void");
    }

    public ConnectivityMetricsLogger() {
        this.mServiceUnblockedTimestampMillis = 0;
        this.mNumSkippedEvents = COMPONENT_TAG_CONNECTIVITY;
        this.mService = Stub.asInterface(ServiceManager.getService(CONNECTIVITY_METRICS_LOGGER_SERVICE));
    }

    public void logEvent(long timestamp, int componentTag, int eventTag, Parcelable data) {
        if (this.mService == null) {
            Log.d(TAG, "logEvent(" + componentTag + "," + eventTag + ") Service not ready");
        } else if (this.mServiceUnblockedTimestampMillis <= 0 || System.currentTimeMillis() >= this.mServiceUnblockedTimestampMillis) {
            long result;
            ConnectivityMetricsEvent connectivityMetricsEvent = null;
            if (this.mNumSkippedEvents > 0) {
                Bundle b = new Bundle();
                b.putInt(DATA_KEY_EVENTS_COUNT, this.mNumSkippedEvents);
                connectivityMetricsEvent = new ConnectivityMetricsEvent(this.mServiceUnblockedTimestampMillis, componentTag, TAG_SKIPPED_EVENTS, b);
                this.mServiceUnblockedTimestampMillis = 0;
            }
            ConnectivityMetricsEvent event = new ConnectivityMetricsEvent(timestamp, componentTag, eventTag, data);
            if (connectivityMetricsEvent == null) {
                try {
                    result = this.mService.logEvent(event);
                } catch (RemoteException e) {
                    Log.e(TAG, "Error logging event " + e.getMessage());
                }
            } else {
                IConnectivityMetricsLogger iConnectivityMetricsLogger = this.mService;
                ConnectivityMetricsEvent[] connectivityMetricsEventArr = new ConnectivityMetricsEvent[COMPONENT_TAG_WIFI];
                connectivityMetricsEventArr[COMPONENT_TAG_CONNECTIVITY] = connectivityMetricsEvent;
                connectivityMetricsEventArr[COMPONENT_TAG_BLUETOOTH] = event;
                result = iConnectivityMetricsLogger.logEvents(connectivityMetricsEventArr);
            }
            if (result == 0) {
                this.mNumSkippedEvents = COMPONENT_TAG_CONNECTIVITY;
            } else {
                this.mNumSkippedEvents += COMPONENT_TAG_BLUETOOTH;
                if (result > 0) {
                    this.mServiceUnblockedTimestampMillis = result;
                }
            }
        } else {
            this.mNumSkippedEvents += COMPONENT_TAG_BLUETOOTH;
        }
    }

    public ConnectivityMetricsEvent[] getEvents(Reference reference) {
        try {
            return this.mService.getEvents(reference);
        } catch (RemoteException ex) {
            Log.e(TAG, "IConnectivityMetricsLogger.getEvents: " + ex);
            return null;
        }
    }

    public boolean register(PendingIntent newEventsIntent) {
        try {
            return this.mService.register(newEventsIntent);
        } catch (RemoteException ex) {
            Log.e(TAG, "IConnectivityMetricsLogger.register: " + ex);
            return false;
        }
    }

    public boolean unregister(PendingIntent newEventsIntent) {
        try {
            this.mService.unregister(newEventsIntent);
            return DBG;
        } catch (RemoteException ex) {
            Log.e(TAG, "IConnectivityMetricsLogger.unregister: " + ex);
            return false;
        }
    }
}
