package com.android.location.provider;

import android.hardware.location.ActivityChangedEvent;
import android.hardware.location.IActivityRecognitionHardware;
import android.hardware.location.IActivityRecognitionHardwareSink.Stub;
import android.os.RemoteException;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public final class ActivityRecognitionProvider {
    public static final String ACTIVITY_IN_VEHICLE = "android.activity_recognition.in_vehicle";
    public static final String ACTIVITY_ON_BICYCLE = "android.activity_recognition.on_bicycle";
    public static final String ACTIVITY_RUNNING = "android.activity_recognition.running";
    public static final String ACTIVITY_STILL = "android.activity_recognition.still";
    public static final String ACTIVITY_TILTING = "android.activity_recognition.tilting";
    public static final String ACTIVITY_WALKING = "android.activity_recognition.walking";
    public static final int EVENT_TYPE_ENTER = 1;
    public static final int EVENT_TYPE_EXIT = 2;
    public static final int EVENT_TYPE_FLUSH_COMPLETE = 0;
    private final IActivityRecognitionHardware mService;
    private final HashSet<Sink> mSinkSet = new HashSet();

    public interface Sink {
        void onActivityChanged(ActivityChangedEvent activityChangedEvent);
    }

    private final class SinkTransport extends Stub {
        /* synthetic */ SinkTransport(ActivityRecognitionProvider this$0, SinkTransport -this1) {
            this();
        }

        private SinkTransport() {
        }

        /* JADX WARNING: Missing block: B:10:0x0021, code:
            r2 = new java.util.ArrayList();
            r4 = r13.getActivityRecognitionEvents().iterator();
     */
        /* JADX WARNING: Missing block: B:12:0x0032, code:
            if (r4.hasNext() == false) goto L_0x0052;
     */
        /* JADX WARNING: Missing block: B:13:0x0034, code:
            r3 = (android.hardware.location.ActivityRecognitionEvent) r4.next();
            r2.add(new com.android.location.provider.ActivityRecognitionEvent(r3.getActivity(), r3.getEventType(), r3.getTimestampNs()));
     */
        /* JADX WARNING: Missing block: B:17:0x0052, code:
            r0 = new com.android.location.provider.ActivityChangedEvent(r2);
            r6 = r7.iterator();
     */
        /* JADX WARNING: Missing block: B:19:0x005f, code:
            if (r6.hasNext() == false) goto L_0x006b;
     */
        /* JADX WARNING: Missing block: B:20:0x0061, code:
            ((com.android.location.provider.ActivityRecognitionProvider.Sink) r6.next()).onActivityChanged(r0);
     */
        /* JADX WARNING: Missing block: B:21:0x006b, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onActivityChanged(ActivityChangedEvent event) {
            synchronized (ActivityRecognitionProvider.this.mSinkSet) {
                if (ActivityRecognitionProvider.this.mSinkSet.isEmpty()) {
                    return;
                }
                Collection<Sink> sinks = new ArrayList(ActivityRecognitionProvider.this.mSinkSet);
            }
        }
    }

    public ActivityRecognitionProvider(IActivityRecognitionHardware service) throws RemoteException {
        Preconditions.checkNotNull(service);
        this.mService = service;
        this.mService.registerSink(new SinkTransport(this, null));
    }

    public String[] getSupportedActivities() throws RemoteException {
        return this.mService.getSupportedActivities();
    }

    public boolean isActivitySupported(String activity) throws RemoteException {
        return this.mService.isActivitySupported(activity);
    }

    public void registerSink(Sink sink) {
        Preconditions.checkNotNull(sink);
        synchronized (this.mSinkSet) {
            this.mSinkSet.add(sink);
        }
    }

    public void unregisterSink(Sink sink) {
        Preconditions.checkNotNull(sink);
        synchronized (this.mSinkSet) {
            this.mSinkSet.remove(sink);
        }
    }

    public boolean enableActivityEvent(String activity, int eventType, long reportLatencyNs) throws RemoteException {
        return this.mService.enableActivityEvent(activity, eventType, reportLatencyNs);
    }

    public boolean disableActivityEvent(String activity, int eventType) throws RemoteException {
        return this.mService.disableActivityEvent(activity, eventType);
    }

    public boolean flush() throws RemoteException {
        return this.mService.flush();
    }
}
