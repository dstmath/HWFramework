package android.hardware.location;

import android.content.Context;
import android.hardware.location.IActivityRecognitionHardware;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.Array;

public class ActivityRecognitionHardware extends IActivityRecognitionHardware.Stub {
    /* access modifiers changed from: private */
    public static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String ENFORCE_HW_PERMISSION_MESSAGE = "Permission 'android.permission.LOCATION_HARDWARE' not granted to access ActivityRecognitionHardware";
    private static final int EVENT_TYPE_COUNT = 3;
    private static final int EVENT_TYPE_DISABLED = 0;
    private static final int EVENT_TYPE_ENABLED = 1;
    private static final String HARDWARE_PERMISSION = "android.permission.LOCATION_HARDWARE";
    private static final int INVALID_ACTIVITY_TYPE = -1;
    private static final int NATIVE_SUCCESS_RESULT = 0;
    private static final String TAG = "ActivityRecognitionHW";
    private static ActivityRecognitionHardware sSingletonInstance;
    private static final Object sSingletonInstanceLock = new Object();
    private final Context mContext;
    /* access modifiers changed from: private */
    public final SinkList mSinks = new SinkList();
    private final String[] mSupportedActivities;
    /* access modifiers changed from: private */
    public final int mSupportedActivitiesCount;
    /* access modifiers changed from: private */
    public final int[][] mSupportedActivitiesEnabledEvents;

    private static class Event {
        public int activity;
        public long timestamp;
        public int type;

        private Event() {
        }
    }

    private class SinkList extends RemoteCallbackList<IActivityRecognitionHardwareSink> {
        private SinkList() {
        }

        public void onCallbackDied(IActivityRecognitionHardwareSink callback) {
            int callbackCount = ActivityRecognitionHardware.this.mSinks.getRegisteredCallbackCount();
            if (ActivityRecognitionHardware.DEBUG) {
                Log.d(ActivityRecognitionHardware.TAG, "RegisteredCallbackCount: " + callbackCount);
            }
            if (callbackCount == 0) {
                for (int activity = 0; activity < ActivityRecognitionHardware.this.mSupportedActivitiesCount; activity++) {
                    for (int event = 0; event < 3; event++) {
                        disableActivityEventIfEnabled(activity, event);
                    }
                }
            }
        }

        private void disableActivityEventIfEnabled(int activityType, int eventType) {
            if (ActivityRecognitionHardware.this.mSupportedActivitiesEnabledEvents[activityType][eventType] == 1) {
                int result = ActivityRecognitionHardware.this.nativeDisableActivityEvent(activityType, eventType);
                ActivityRecognitionHardware.this.mSupportedActivitiesEnabledEvents[activityType][eventType] = 0;
                Log.e(ActivityRecognitionHardware.TAG, String.format("DisableActivityEvent: activityType=%d, eventType=%d, result=%d", new Object[]{Integer.valueOf(activityType), Integer.valueOf(eventType), Integer.valueOf(result)}));
            }
        }
    }

    private static native void nativeClassInit();

    /* access modifiers changed from: private */
    public native int nativeDisableActivityEvent(int i, int i2);

    private native int nativeEnableActivityEvent(int i, int i2, long j);

    private native int nativeFlush();

    private native String[] nativeGetSupportedActivities();

    private native void nativeInitialize();

    private static native boolean nativeIsSupported();

    private native void nativeRelease();

    static {
        nativeClassInit();
    }

    private ActivityRecognitionHardware(Context context) {
        nativeInitialize();
        this.mContext = context;
        this.mSupportedActivities = fetchSupportedActivities();
        this.mSupportedActivitiesCount = this.mSupportedActivities.length;
        this.mSupportedActivitiesEnabledEvents = (int[][]) Array.newInstance(int.class, new int[]{this.mSupportedActivitiesCount, 3});
    }

    public static ActivityRecognitionHardware getInstance(Context context) {
        ActivityRecognitionHardware activityRecognitionHardware;
        synchronized (sSingletonInstanceLock) {
            if (sSingletonInstance == null) {
                sSingletonInstance = new ActivityRecognitionHardware(context);
            }
            activityRecognitionHardware = sSingletonInstance;
        }
        return activityRecognitionHardware;
    }

    public static boolean isSupported() {
        return nativeIsSupported();
    }

    public String[] getSupportedActivities() {
        checkPermissions();
        return this.mSupportedActivities;
    }

    public boolean isActivitySupported(String activity) {
        checkPermissions();
        return getActivityType(activity) != -1;
    }

    public boolean registerSink(IActivityRecognitionHardwareSink sink) {
        checkPermissions();
        return this.mSinks.register(sink);
    }

    public boolean unregisterSink(IActivityRecognitionHardwareSink sink) {
        checkPermissions();
        return this.mSinks.unregister(sink);
    }

    public boolean enableActivityEvent(String activity, int eventType, long reportLatencyNs) {
        checkPermissions();
        int activityType = getActivityType(activity);
        if (activityType == -1 || nativeEnableActivityEvent(activityType, eventType, reportLatencyNs) != 0) {
            return false;
        }
        this.mSupportedActivitiesEnabledEvents[activityType][eventType] = 1;
        return true;
    }

    public boolean disableActivityEvent(String activity, int eventType) {
        checkPermissions();
        int activityType = getActivityType(activity);
        if (activityType == -1 || nativeDisableActivityEvent(activityType, eventType) != 0) {
            return false;
        }
        this.mSupportedActivitiesEnabledEvents[activityType][eventType] = 0;
        return true;
    }

    public boolean flush() {
        checkPermissions();
        return nativeFlush() == 0;
    }

    private void onActivityChanged(Event[] events) {
        if (events == null || events.length == 0) {
            if (DEBUG != 0) {
                Log.d(TAG, "No events to broadcast for onActivityChanged.");
            }
            return;
        }
        int eventsLength = events.length;
        ActivityRecognitionEvent[] activityRecognitionEventArray = new ActivityRecognitionEvent[eventsLength];
        for (int i = 0; i < eventsLength; i++) {
            Event event = events[i];
            activityRecognitionEventArray[i] = new ActivityRecognitionEvent(getActivityName(event.activity), event.type, event.timestamp);
        }
        ActivityChangedEvent activityChangedEvent = new ActivityChangedEvent(activityRecognitionEventArray);
        int size = this.mSinks.beginBroadcast();
        for (int i2 = 0; i2 < size; i2++) {
            try {
                ((IActivityRecognitionHardwareSink) this.mSinks.getBroadcastItem(i2)).onActivityChanged(activityChangedEvent);
            } catch (RemoteException e) {
                Log.e(TAG, "Error delivering activity changed event.", e);
            }
        }
        this.mSinks.finishBroadcast();
    }

    private String getActivityName(int activityType) {
        if (activityType >= 0 && activityType < this.mSupportedActivities.length) {
            return this.mSupportedActivities[activityType];
        }
        Log.e(TAG, String.format("Invalid ActivityType: %d, SupportedActivities: %d", new Object[]{Integer.valueOf(activityType), Integer.valueOf(this.mSupportedActivities.length)}));
        return null;
    }

    private int getActivityType(String activity) {
        if (TextUtils.isEmpty(activity)) {
            return -1;
        }
        int supportedActivitiesLength = this.mSupportedActivities.length;
        for (int i = 0; i < supportedActivitiesLength; i++) {
            if (activity.equals(this.mSupportedActivities[i])) {
                return i;
            }
        }
        return -1;
    }

    private void checkPermissions() {
        this.mContext.enforceCallingPermission("android.permission.LOCATION_HARDWARE", ENFORCE_HW_PERMISSION_MESSAGE);
    }

    private String[] fetchSupportedActivities() {
        String[] supportedActivities = nativeGetSupportedActivities();
        if (supportedActivities != null) {
            return supportedActivities;
        }
        return new String[0];
    }
}
