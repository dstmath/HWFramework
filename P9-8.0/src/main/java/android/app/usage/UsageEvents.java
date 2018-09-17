package android.app.usage;

import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;
import java.util.List;

public final class UsageEvents implements Parcelable {
    public static final Creator<UsageEvents> CREATOR = new Creator<UsageEvents>() {
        public UsageEvents createFromParcel(Parcel source) {
            return new UsageEvents(source);
        }

        public UsageEvents[] newArray(int size) {
            return new UsageEvents[size];
        }
    };
    public static final String INSTANT_APP_CLASS_NAME = "android.instant_class";
    public static final String INSTANT_APP_PACKAGE_NAME = "android.instant_app";
    private final int mEventCount;
    private List<Event> mEventsToWrite;
    private int mIndex;
    private Parcel mParcel;
    private String[] mStringPool;

    public static final class Event {
        public static final int CHOOSER_ACTION = 9;
        public static final int CONFIGURATION_CHANGE = 5;
        public static final int CONTINUE_PREVIOUS_DAY = 4;
        public static final int END_OF_DAY = 3;
        public static final int FLAG_IS_PACKAGE_INSTANT_APP = 1;
        public static final int MOVE_TO_BACKGROUND = 2;
        public static final int MOVE_TO_FOREGROUND = 1;
        public static final int NONE = 0;
        public static final int SHORTCUT_INVOCATION = 8;
        public static final int SYSTEM_INTERACTION = 6;
        public static final int USER_INTERACTION = 7;
        public String mAction;
        public String mClass;
        public Configuration mConfiguration;
        public String[] mContentAnnotations;
        public String mContentType;
        public int mDisplayId;
        public int mEventType;
        public int mFlags;
        public String mPackage;
        public String mShortcutId;
        public long mTimeStamp;

        public Event(Event orig) {
            this.mPackage = orig.mPackage;
            this.mClass = orig.mClass;
            this.mTimeStamp = orig.mTimeStamp;
            this.mEventType = orig.mEventType;
            this.mConfiguration = orig.mConfiguration;
            this.mShortcutId = orig.mShortcutId;
            this.mAction = orig.mAction;
            this.mContentType = orig.mContentType;
            this.mContentAnnotations = orig.mContentAnnotations;
            this.mFlags = orig.mFlags;
            this.mDisplayId = orig.mDisplayId;
        }

        public String getPackageName() {
            return this.mPackage;
        }

        public String getClassName() {
            return this.mClass;
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

        public Event getObfuscatedIfInstantApp() {
            if ((this.mFlags & 1) == 0) {
                return this;
            }
            Event ret = new Event(this);
            ret.mPackage = UsageEvents.INSTANT_APP_PACKAGE_NAME;
            ret.mClass = UsageEvents.INSTANT_APP_CLASS_NAME;
            return ret;
        }
    }

    public UsageEvents(Parcel in) {
        this.mEventsToWrite = null;
        this.mParcel = null;
        this.mIndex = 0;
        this.mEventCount = in.readInt();
        this.mIndex = in.readInt();
        if (this.mEventCount > 0) {
            this.mStringPool = in.createStringArray();
            int listByteLength = in.readInt();
            int positionInParcel = in.readInt();
            this.mParcel = Parcel.obtain();
            this.mParcel.setDataPosition(0);
            this.mParcel.appendFrom(in, in.dataPosition(), listByteLength);
            this.mParcel.setDataSize(this.mParcel.dataPosition());
            this.mParcel.setDataPosition(positionInParcel);
        }
    }

    UsageEvents() {
        this.mEventsToWrite = null;
        this.mParcel = null;
        this.mIndex = 0;
        this.mEventCount = 0;
    }

    public UsageEvents(List<Event> events, String[] stringPool) {
        this.mEventsToWrite = null;
        this.mParcel = null;
        this.mIndex = 0;
        this.mStringPool = stringPool;
        this.mEventCount = events.size();
        this.mEventsToWrite = events;
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
        if (this.mParcel != null) {
            this.mParcel.setDataPosition(0);
        }
    }

    private int findStringIndex(String str) {
        int index = Arrays.binarySearch(this.mStringPool, str);
        if (index >= 0) {
            return index;
        }
        throw new IllegalStateException("String '" + str + "' is not in the string pool");
    }

    private void writeEventToParcel(Event event, Parcel p, int flags) {
        int packageIndex;
        int classIndex;
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
        p.writeInt(packageIndex);
        p.writeInt(classIndex);
        p.writeInt(event.mEventType);
        p.writeLong(event.mTimeStamp);
        switch (event.mEventType) {
            case 5:
                event.mConfiguration.writeToParcel(p, flags);
                return;
            case 8:
                p.writeString(event.mShortcutId);
                return;
            case 9:
                p.writeString(event.mAction);
                p.writeString(event.mContentType);
                p.writeStringArray(event.mContentAnnotations);
                return;
            default:
                return;
        }
    }

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
        eventOut.mEventType = p.readInt();
        eventOut.mTimeStamp = p.readLong();
        eventOut.mConfiguration = null;
        eventOut.mShortcutId = null;
        eventOut.mAction = null;
        eventOut.mContentType = null;
        eventOut.mContentAnnotations = null;
        switch (eventOut.mEventType) {
            case 5:
                eventOut.mConfiguration = (Configuration) Configuration.CREATOR.createFromParcel(p);
                return;
            case 8:
                eventOut.mShortcutId = p.readString();
                return;
            case 9:
                eventOut.mAction = p.readString();
                eventOut.mContentType = p.readString();
                eventOut.mContentAnnotations = p.createStringArray();
                return;
            default:
                return;
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEventCount);
        dest.writeInt(this.mIndex);
        if (this.mEventCount > 0) {
            dest.writeStringArray(this.mStringPool);
            if (this.mEventsToWrite != null) {
                Parcel p = Parcel.obtain();
                try {
                    p.setDataPosition(0);
                    for (int i = 0; i < this.mEventCount; i++) {
                        writeEventToParcel((Event) this.mEventsToWrite.get(i), p, flags);
                    }
                    int listByteLength = p.dataPosition();
                    dest.writeInt(listByteLength);
                    dest.writeInt(0);
                    dest.appendFrom(p, 0, listByteLength);
                } finally {
                    p.recycle();
                }
            } else if (this.mParcel != null) {
                dest.writeInt(this.mParcel.dataSize());
                dest.writeInt(this.mParcel.dataPosition());
                dest.appendFrom(this.mParcel, 0, this.mParcel.dataSize());
            } else {
                throw new IllegalStateException("Either mParcel or mEventsToWrite must not be null");
            }
        }
    }
}
