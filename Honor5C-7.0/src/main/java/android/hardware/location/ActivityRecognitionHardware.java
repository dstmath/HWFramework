package android.hardware.location;

import android.content.Context;
import android.hardware.location.IActivityRecognitionHardware.Stub;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import java.lang.reflect.Array;

public class ActivityRecognitionHardware extends Stub {
    private static final boolean DEBUG = false;
    private static final String ENFORCE_HW_PERMISSION_MESSAGE = "Permission 'android.permission.LOCATION_HARDWARE' not granted to access ActivityRecognitionHardware";
    private static final int EVENT_TYPE_COUNT = 3;
    private static final int EVENT_TYPE_DISABLED = 0;
    private static final int EVENT_TYPE_ENABLED = 1;
    private static final String HARDWARE_PERMISSION = "android.permission.LOCATION_HARDWARE";
    private static final int INVALID_ACTIVITY_TYPE = -1;
    private static final int NATIVE_SUCCESS_RESULT = 0;
    private static final String TAG = "ActivityRecognitionHW";
    private static ActivityRecognitionHardware sSingletonInstance;
    private static final Object sSingletonInstanceLock = null;
    private final Context mContext;
    private final SinkList mSinks;
    private final String[] mSupportedActivities;
    private final int mSupportedActivitiesCount;
    private final int[][] mSupportedActivitiesEnabledEvents;

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
                for (int activity = ActivityRecognitionHardware.NATIVE_SUCCESS_RESULT; activity < ActivityRecognitionHardware.this.mSupportedActivitiesCount; activity += ActivityRecognitionHardware.EVENT_TYPE_ENABLED) {
                    for (int event = ActivityRecognitionHardware.NATIVE_SUCCESS_RESULT; event < ActivityRecognitionHardware.EVENT_TYPE_COUNT; event += ActivityRecognitionHardware.EVENT_TYPE_ENABLED) {
                        disableActivityEventIfEnabled(activity, event);
                    }
                }
            }
        }

        private void disableActivityEventIfEnabled(int activityType, int eventType) {
            if (ActivityRecognitionHardware.this.mSupportedActivitiesEnabledEvents[activityType][eventType] == ActivityRecognitionHardware.EVENT_TYPE_ENABLED) {
                int result = ActivityRecognitionHardware.this.nativeDisableActivityEvent(activityType, eventType);
                ActivityRecognitionHardware.this.mSupportedActivitiesEnabledEvents[activityType][eventType] = ActivityRecognitionHardware.NATIVE_SUCCESS_RESULT;
                Object[] objArr = new Object[ActivityRecognitionHardware.EVENT_TYPE_COUNT];
                objArr[ActivityRecognitionHardware.NATIVE_SUCCESS_RESULT] = Integer.valueOf(activityType);
                objArr[ActivityRecognitionHardware.EVENT_TYPE_ENABLED] = Integer.valueOf(eventType);
                objArr[2] = Integer.valueOf(result);
                Log.e(ActivityRecognitionHardware.TAG, String.format("DisableActivityEvent: activityType=%d, eventType=%d, result=%d", objArr));
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hardware.location.ActivityRecognitionHardware.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hardware.location.ActivityRecognitionHardware.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hardware.location.ActivityRecognitionHardware.<clinit>():void");
    }

    private static native void nativeClassInit();

    private native int nativeDisableActivityEvent(int i, int i2);

    private native int nativeEnableActivityEvent(int i, int i2, long j);

    private native int nativeFlush();

    private native String[] nativeGetSupportedActivities();

    private native void nativeInitialize();

    private static native boolean nativeIsSupported();

    private native void nativeRelease();

    private ActivityRecognitionHardware(Context context) {
        this.mSinks = new SinkList();
        nativeInitialize();
        this.mContext = context;
        this.mSupportedActivities = fetchSupportedActivities();
        this.mSupportedActivitiesCount = this.mSupportedActivities.length;
        this.mSupportedActivitiesEnabledEvents = (int[][]) Array.newInstance(Integer.TYPE, new int[]{this.mSupportedActivitiesCount, EVENT_TYPE_COUNT});
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
        return getActivityType(activity) != INVALID_ACTIVITY_TYPE ? true : DEBUG;
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
        if (activityType == INVALID_ACTIVITY_TYPE || nativeEnableActivityEvent(activityType, eventType, reportLatencyNs) != 0) {
            return DEBUG;
        }
        this.mSupportedActivitiesEnabledEvents[activityType][eventType] = EVENT_TYPE_ENABLED;
        return true;
    }

    public boolean disableActivityEvent(String activity, int eventType) {
        checkPermissions();
        int activityType = getActivityType(activity);
        if (activityType == INVALID_ACTIVITY_TYPE || nativeDisableActivityEvent(activityType, eventType) != 0) {
            return DEBUG;
        }
        this.mSupportedActivitiesEnabledEvents[activityType][eventType] = NATIVE_SUCCESS_RESULT;
        return true;
    }

    public boolean flush() {
        checkPermissions();
        if (nativeFlush() == 0) {
            return true;
        }
        return DEBUG;
    }

    private void onActivityChanged(Event[] events) {
        if (events == null || events.length == 0) {
            if (DEBUG) {
                Log.d(TAG, "No events to broadcast for onActivityChanged.");
            }
            return;
        }
        int i;
        int eventsLength = events.length;
        ActivityRecognitionEvent[] activityRecognitionEventArray = new ActivityRecognitionEvent[eventsLength];
        for (i = NATIVE_SUCCESS_RESULT; i < eventsLength; i += EVENT_TYPE_ENABLED) {
            Event event = events[i];
            activityRecognitionEventArray[i] = new ActivityRecognitionEvent(getActivityName(event.activity), event.type, event.timestamp);
        }
        ActivityChangedEvent activityChangedEvent = new ActivityChangedEvent(activityRecognitionEventArray);
        int size = this.mSinks.beginBroadcast();
        for (i = NATIVE_SUCCESS_RESULT; i < size; i += EVENT_TYPE_ENABLED) {
            try {
                ((IActivityRecognitionHardwareSink) this.mSinks.getBroadcastItem(i)).onActivityChanged(activityChangedEvent);
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
            return INVALID_ACTIVITY_TYPE;
        }
        int supportedActivitiesLength = this.mSupportedActivities.length;
        for (int i = NATIVE_SUCCESS_RESULT; i < supportedActivitiesLength; i += EVENT_TYPE_ENABLED) {
            if (activity.equals(this.mSupportedActivities[i])) {
                return i;
            }
        }
        return INVALID_ACTIVITY_TYPE;
    }

    private void checkPermissions() {
        this.mContext.enforceCallingPermission(HARDWARE_PERMISSION, ENFORCE_HW_PERMISSION_MESSAGE);
    }

    private String[] fetchSupportedActivities() {
        String[] supportedActivities = nativeGetSupportedActivities();
        if (supportedActivities != null) {
            return supportedActivities;
        }
        return new String[NATIVE_SUCCESS_RESULT];
    }
}
