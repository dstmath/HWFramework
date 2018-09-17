package android.app.usage;

import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.Arrays;
import java.util.List;

public final class UsageEvents implements Parcelable {
    public static final Creator<UsageEvents> CREATOR = null;
    private final int mEventCount;
    private List<Event> mEventsToWrite;
    private int mIndex;
    private Parcel mParcel;
    private String[] mStringPool;

    public static final class Event {
        public static final int CONFIGURATION_CHANGE = 5;
        public static final int CONTINUE_PREVIOUS_DAY = 4;
        public static final int END_OF_DAY = 3;
        public static final int MOVE_TO_BACKGROUND = 2;
        public static final int MOVE_TO_FOREGROUND = 1;
        public static final int NONE = 0;
        public static final int SYSTEM_INTERACTION = 6;
        public static final int USER_INTERACTION = 7;
        public String mClass;
        public Configuration mConfiguration;
        public int mEventType;
        public String mPackage;
        public long mTimeStamp;

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
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.usage.UsageEvents.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.usage.UsageEvents.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.app.usage.UsageEvents.<clinit>():void");
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
        if (event.mEventType == 5) {
            event.mConfiguration.writeToParcel(p, flags);
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
        if (eventOut.mEventType == 5) {
            eventOut.mConfiguration = (Configuration) Configuration.CREATOR.createFromParcel(p);
        } else {
            eventOut.mConfiguration = null;
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
