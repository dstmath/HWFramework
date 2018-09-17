package android.app.admin;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.SystemProperties;
import android.util.EventLog.Event;
import java.io.IOException;
import java.util.Collection;

public class SecurityLog {
    private static final String PROPERTY_LOGGING_ENABLED = "persist.logd.security";
    public static final int TAG_ADB_SHELL_CMD = 210002;
    public static final int TAG_ADB_SHELL_INTERACTIVE = 210001;
    public static final int TAG_APP_PROCESS_START = 210005;
    public static final int TAG_KEYGUARD_DISMISSED = 210006;
    public static final int TAG_KEYGUARD_DISMISS_AUTH_ATTEMPT = 210007;
    public static final int TAG_KEYGUARD_SECURED = 210008;
    public static final int TAG_SYNC_RECV_FILE = 210003;
    public static final int TAG_SYNC_SEND_FILE = 210004;

    public static final class SecurityEvent implements Parcelable {
        public static final Creator<SecurityEvent> CREATOR = new Creator<SecurityEvent>() {
            public SecurityEvent createFromParcel(Parcel source) {
                return new SecurityEvent(source.createByteArray());
            }

            public SecurityEvent[] newArray(int size) {
                return new SecurityEvent[size];
            }
        };
        private Event mEvent;

        SecurityEvent(byte[] data) {
            this.mEvent = Event.fromBytes(data);
        }

        public long getTimeNanos() {
            return this.mEvent.getTimeNanos();
        }

        public int getTag() {
            return this.mEvent.getTag();
        }

        public Object getData() {
            return this.mEvent.getData();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByteArray(this.mEvent.getBytes());
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return this.mEvent.equals(((SecurityEvent) o).mEvent);
        }

        public int hashCode() {
            return this.mEvent.hashCode();
        }
    }

    public static native boolean isLoggingEnabled();

    public static native void readEvents(Collection<SecurityEvent> collection) throws IOException;

    public static native void readEventsOnWrapping(long j, Collection<SecurityEvent> collection) throws IOException;

    public static native void readEventsSince(long j, Collection<SecurityEvent> collection) throws IOException;

    public static native void readPreviousEvents(Collection<SecurityEvent> collection) throws IOException;

    public static native int writeEvent(int i, String str);

    public static native int writeEvent(int i, Object... objArr);

    public static void setLoggingEnabledProperty(boolean enabled) {
        SystemProperties.set(PROPERTY_LOGGING_ENABLED, enabled ? "true" : "false");
    }

    public static boolean getLoggingEnabledProperty() {
        return SystemProperties.getBoolean(PROPERTY_LOGGING_ENABLED, false);
    }
}
