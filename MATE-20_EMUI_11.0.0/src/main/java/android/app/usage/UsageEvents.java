package android.app.usage;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

public final class UsageEvents implements Parcelable {
    public static final Parcelable.Creator<UsageEvents> CREATOR = new Parcelable.Creator<UsageEvents>() {
        /* class android.app.usage.UsageEvents.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public UsageEvents createFromParcel(Parcel source) {
            return new UsageEvents(source);
        }

        @Override // android.os.Parcelable.Creator
        public UsageEvents[] newArray(int size) {
            return new UsageEvents[size];
        }
    };
    public static final String INSTANT_APP_CLASS_NAME = "android.instant_class";
    public static final String INSTANT_APP_PACKAGE_NAME = "android.instant_app";
    private static final int MAX_EVENT_BUFF_LEN = 22020096;
    private static final String TAG = "UsageEvents";
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final int mEventCount;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private List<Event> mEventsToWrite;
    private final boolean mIncludeTaskRoots;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int mIndex;
    @UnsupportedAppUsage
    private Parcel mParcel;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private String[] mStringPool;

    public static final class Event {
        public static final int ACTIVITY_DESTROYED = 24;
        public static final int ACTIVITY_PAUSED = 2;
        public static final int ACTIVITY_RESUMED = 1;
        public static final int ACTIVITY_STOPPED = 23;
        public static final int CHOOSER_ACTION = 9;
        public static final int CONFIGURATION_CHANGE = 5;
        public static final int CONTINUE_PREVIOUS_DAY = 4;
        public static final int CONTINUING_FOREGROUND_SERVICE = 21;
        public static final String DEVICE_EVENT_PACKAGE_NAME = "android";
        public static final int DEVICE_SHUTDOWN = 26;
        public static final int DEVICE_STARTUP = 27;
        public static final int END_OF_DAY = 3;
        public static final int FLAG_IS_PACKAGE_INSTANT_APP = 1;
        public static final int FLUSH_TO_DISK = 25;
        public static final int FOREGROUND_SERVICE_START = 19;
        public static final int FOREGROUND_SERVICE_STOP = 20;
        public static final int KEYGUARD_HIDDEN = 18;
        public static final int KEYGUARD_SHOWN = 17;
        public static final int MAX_EVENT_TYPE = 27;
        @Deprecated
        public static final int MOVE_TO_BACKGROUND = 2;
        @Deprecated
        public static final int MOVE_TO_FOREGROUND = 1;
        public static final int NONE = 0;
        @SystemApi
        public static final int NOTIFICATION_INTERRUPTION = 12;
        @SystemApi
        public static final int NOTIFICATION_SEEN = 10;
        public static final int ROLLOVER_FOREGROUND_SERVICE = 22;
        public static final int SCREEN_INTERACTIVE = 15;
        public static final int SCREEN_NON_INTERACTIVE = 16;
        public static final int SHORTCUT_INVOCATION = 8;
        @SystemApi
        public static final int SLICE_PINNED = 14;
        @SystemApi
        public static final int SLICE_PINNED_PRIV = 13;
        public static final int STANDBY_BUCKET_CHANGED = 11;
        @SystemApi
        public static final int SYSTEM_INTERACTION = 6;
        public static final int USER_INTERACTION = 7;
        public static final int VALID_FLAG_BITS = 1;
        public String mAction;
        public int mBucketAndReason;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public String mClass;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public Configuration mConfiguration;
        public String[] mContentAnnotations;
        public String mContentType;
        public int mDisplayId;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public int mEventType;
        public int mFlags;
        public int mInstanceId;
        public String mNotificationChannelId;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public String mPackage;
        public String mShortcutId;
        public String mTaskRootClass;
        public String mTaskRootPackage;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public long mTimeStamp;

        @Retention(RetentionPolicy.SOURCE)
        public @interface EventFlags {
        }

        public Event() {
        }

        public Event(int type, long timeStamp) {
            this.mEventType = type;
            this.mTimeStamp = timeStamp;
        }

        public Event(Event orig) {
            this.mPackage = orig.mPackage;
            this.mClass = orig.mClass;
            this.mInstanceId = orig.mInstanceId;
            this.mTaskRootPackage = orig.mTaskRootPackage;
            this.mTaskRootClass = orig.mTaskRootClass;
            this.mTimeStamp = orig.mTimeStamp;
            this.mEventType = orig.mEventType;
            this.mConfiguration = orig.mConfiguration;
            this.mShortcutId = orig.mShortcutId;
            this.mAction = orig.mAction;
            this.mContentType = orig.mContentType;
            this.mContentAnnotations = orig.mContentAnnotations;
            this.mFlags = orig.mFlags;
            this.mBucketAndReason = orig.mBucketAndReason;
            this.mDisplayId = orig.mDisplayId;
            this.mNotificationChannelId = orig.mNotificationChannelId;
        }

        public String getPackageName() {
            return this.mPackage;
        }

        @SystemApi
        public boolean isInstantApp() {
            return (this.mFlags & 1) == 1;
        }

        public String getClassName() {
            return this.mClass;
        }

        @SystemApi
        public int getInstanceId() {
            return this.mInstanceId;
        }

        @SystemApi
        public String getTaskRootPackageName() {
            return this.mTaskRootPackage;
        }

        @SystemApi
        public String getTaskRootClassName() {
            return this.mTaskRootClass;
        }

        public long getTimeStamp() {
            return this.mTimeStamp;
        }

        public int getEventType() {
            return this.mEventType;
        }

        public Configuration getConfiguration() {
            return this.mConfiguration;
        }

        public String getShortcutId() {
            return this.mShortcutId;
        }

        public int getStandbyBucket() {
            return (this.mBucketAndReason & -65536) >>> 16;
        }

        public int getAppStandbyBucket() {
            return (this.mBucketAndReason & -65536) >>> 16;
        }

        public int getStandbyReason() {
            return this.mBucketAndReason & 65535;
        }

        @SystemApi
        public String getNotificationChannelId() {
            return this.mNotificationChannelId;
        }

        public Event getObfuscatedIfInstantApp() {
            if (!isInstantApp()) {
                return this;
            }
            Event ret = new Event(this);
            ret.mPackage = UsageEvents.INSTANT_APP_PACKAGE_NAME;
            ret.mClass = UsageEvents.INSTANT_APP_CLASS_NAME;
            return ret;
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public UsageEvents(Parcel in) {
        this.mEventsToWrite = null;
        this.mParcel = null;
        this.mIndex = 0;
        byte[] bytes = in.readBlob();
        Parcel data = Parcel.obtain();
        data.unmarshall(bytes, 0, bytes.length);
        data.setDataPosition(0);
        this.mEventCount = data.readInt();
        this.mIndex = data.readInt();
        if (this.mEventCount > 0) {
            this.mStringPool = data.createStringArray();
            int listByteLength = data.readInt();
            int positionInParcel = data.readInt();
            this.mParcel = Parcel.obtain();
            this.mParcel.setDataPosition(0);
            this.mParcel.appendFrom(data, data.dataPosition(), listByteLength);
            Parcel parcel = this.mParcel;
            parcel.setDataSize(parcel.dataPosition());
            this.mParcel.setDataPosition(positionInParcel);
        }
        this.mIncludeTaskRoots = true;
    }

    UsageEvents() {
        this.mEventsToWrite = null;
        this.mParcel = null;
        this.mIndex = 0;
        this.mEventCount = 0;
        this.mIncludeTaskRoots = true;
    }

    public UsageEvents(List<Event> events, String[] stringPool) {
        this(events, stringPool, false);
    }

    public UsageEvents(List<Event> events, String[] stringPool, boolean includeTaskRoots) {
        this.mEventsToWrite = null;
        this.mParcel = null;
        this.mIndex = 0;
        this.mStringPool = stringPool;
        this.mEventCount = events.size();
        this.mEventsToWrite = events;
        this.mIncludeTaskRoots = includeTaskRoots;
    }

    public boolean hasNextEvent() {
        return this.mIndex < this.mEventCount;
    }

    public boolean getNextEvent(Event eventOut) {
        if (this.mIndex >= this.mEventCount) {
            return false;
        }
        readEventFromParcel(this.mParcel, eventOut);
        this.mIndex++;
        if (this.mIndex >= this.mEventCount) {
            this.mParcel.recycle();
            this.mParcel = null;
        }
        return true;
    }

    public void resetToStart() {
        this.mIndex = 0;
        Parcel parcel = this.mParcel;
        if (parcel != null) {
            parcel.setDataPosition(0);
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private int findStringIndex(String str) {
        int index = Arrays.binarySearch(this.mStringPool, str);
        if (index >= 0) {
            return index;
        }
        throw new IllegalStateException("String '" + str + "' is not in the string pool");
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private void writeEventToParcel(Event event, Parcel p, int flags) {
        int packageIndex;
        int classIndex;
        int taskRootPackageIndex;
        int taskRootClassIndex;
        if (event.mPackage != null) {
            packageIndex = findStringIndex(event.mPackage);
        } else {
            packageIndex = -1;
        }
        if (event.mClass != null) {
            classIndex = findStringIndex(event.mClass);
        } else {
            classIndex = -1;
        }
        if (!this.mIncludeTaskRoots || event.mTaskRootPackage == null) {
            taskRootPackageIndex = -1;
        } else {
            taskRootPackageIndex = findStringIndex(event.mTaskRootPackage);
        }
        if (!this.mIncludeTaskRoots || event.mTaskRootClass == null) {
            taskRootClassIndex = -1;
        } else {
            taskRootClassIndex = findStringIndex(event.mTaskRootClass);
        }
        p.writeInt(packageIndex);
        p.writeInt(classIndex);
        p.writeInt(event.mInstanceId);
        p.writeInt(taskRootPackageIndex);
        p.writeInt(taskRootClassIndex);
        p.writeInt(event.mEventType);
        p.writeLong(event.mTimeStamp);
        int i = event.mEventType;
        if (i == 5) {
            event.mConfiguration.writeToParcel(p, flags);
        } else if (i == 8) {
            p.writeString(event.mShortcutId);
        } else if (i == 9) {
            p.writeString(event.mAction);
            p.writeString(event.mContentType);
            p.writeStringArray(event.mContentAnnotations);
        } else if (i == 11) {
            p.writeInt(event.mBucketAndReason);
        } else if (i == 12) {
            p.writeString(event.mNotificationChannelId);
        }
        p.writeInt(event.mFlags);
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private void readEventFromParcel(Parcel p, Event eventOut) {
        int packageIndex = p.readInt();
        if (packageIndex >= 0) {
            eventOut.mPackage = this.mStringPool[packageIndex];
        } else {
            eventOut.mPackage = null;
        }
        int classIndex = p.readInt();
        if (classIndex >= 0) {
            eventOut.mClass = this.mStringPool[classIndex];
        } else {
            eventOut.mClass = null;
        }
        eventOut.mInstanceId = p.readInt();
        int taskRootPackageIndex = p.readInt();
        if (taskRootPackageIndex >= 0) {
            eventOut.mTaskRootPackage = this.mStringPool[taskRootPackageIndex];
        } else {
            eventOut.mTaskRootPackage = null;
        }
        int taskRootClassIndex = p.readInt();
        if (taskRootClassIndex >= 0) {
            eventOut.mTaskRootClass = this.mStringPool[taskRootClassIndex];
        } else {
            eventOut.mTaskRootClass = null;
        }
        eventOut.mEventType = p.readInt();
        eventOut.mTimeStamp = p.readLong();
        eventOut.mConfiguration = null;
        eventOut.mShortcutId = null;
        eventOut.mAction = null;
        eventOut.mContentType = null;
        eventOut.mContentAnnotations = null;
        eventOut.mNotificationChannelId = null;
        int i = eventOut.mEventType;
        if (i == 5) {
            eventOut.mConfiguration = Configuration.CREATOR.createFromParcel(p);
        } else if (i == 8) {
            eventOut.mShortcutId = p.readString();
        } else if (i == 9) {
            eventOut.mAction = p.readString();
            eventOut.mContentType = p.readString();
            eventOut.mContentAnnotations = p.createStringArray();
        } else if (i == 11) {
            eventOut.mBucketAndReason = p.readInt();
        } else if (i == 12) {
            eventOut.mNotificationChannelId = p.readString();
        }
        eventOut.mFlags = p.readInt();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        Parcel data = Parcel.obtain();
        data.writeInt(this.mEventCount);
        data.writeInt(this.mIndex);
        if (this.mEventCount > 0) {
            data.writeStringArray(this.mStringPool);
            if (this.mEventsToWrite != null) {
                Parcel p = Parcel.obtain();
                try {
                    p.setDataPosition(0);
                    for (int i = 0; i < this.mEventCount && p.dataPosition() < MAX_EVENT_BUFF_LEN; i++) {
                        writeEventToParcel(this.mEventsToWrite.get(i), p, flags);
                    }
                    int listByteLength = p.dataPosition();
                    data.writeInt(listByteLength);
                    data.writeInt(0);
                    data.appendFrom(p, 0, listByteLength);
                } finally {
                    p.recycle();
                }
            } else {
                Parcel p2 = this.mParcel;
                if (p2 != null) {
                    data.writeInt(p2.dataSize());
                    data.writeInt(this.mParcel.dataPosition());
                    Parcel parcel = this.mParcel;
                    data.appendFrom(parcel, 0, parcel.dataSize());
                } else {
                    throw new IllegalStateException("Either mParcel or mEventsToWrite must not be null");
                }
            }
        }
        dest.writeBlob(data.marshall());
    }
}
