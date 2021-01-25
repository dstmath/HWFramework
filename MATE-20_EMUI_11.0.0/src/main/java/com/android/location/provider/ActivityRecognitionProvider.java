package com.android.location.provider;

import android.hardware.location.ActivityChangedEvent;
import android.hardware.location.ActivityRecognitionEvent;
import android.hardware.location.IActivityRecognitionHardware;
import android.hardware.location.IActivityRecognitionHardwareSink;
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
    private final HashSet<Sink> mSinkSet = new HashSet<>();

    public interface Sink {
        void onActivityChanged(ActivityChangedEvent activityChangedEvent);
    }

    public ActivityRecognitionProvider(IActivityRecognitionHardware service) throws RemoteException {
        Preconditions.checkNotNull(service);
        this.mService = service;
        this.mService.registerSink(new SinkTransport());
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

    private final class SinkTransport extends IActivityRecognitionHardwareSink.Stub {
        private SinkTransport() {
        }

        public void onActivityChanged(ActivityChangedEvent event) {
            Collection<Sink> sinks;
            synchronized (ActivityRecognitionProvider.this.mSinkSet) {
                if (!ActivityRecognitionProvider.this.mSinkSet.isEmpty()) {
                    sinks = new ArrayList<>(ActivityRecognitionProvider.this.mSinkSet);
                } else {
                    return;
                }
            }
            ArrayList<ActivityRecognitionEvent> gmsEvents = new ArrayList<>();
            for (ActivityRecognitionEvent reportingEvent : event.getActivityRecognitionEvents()) {
                gmsEvents.add(new ActivityRecognitionEvent(reportingEvent.getActivity(), reportingEvent.getEventType(), reportingEvent.getTimestampNs()));
            }
            ActivityChangedEvent gmsEvent = new ActivityChangedEvent(gmsEvents);
            for (Sink sink : sinks) {
                sink.onActivityChanged(gmsEvent);
            }
        }
    }
}
