package android.telecom;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ParcelableCallAnalytics implements Parcelable {
    public static final int CALLTYPE_INCOMING = 1;
    public static final int CALLTYPE_OUTGOING = 2;
    public static final int CALLTYPE_UNKNOWN = 0;
    public static final int CDMA_PHONE = 1;
    public static final Creator<ParcelableCallAnalytics> CREATOR = new Creator<ParcelableCallAnalytics>() {
        public ParcelableCallAnalytics createFromParcel(Parcel in) {
            return new ParcelableCallAnalytics(in);
        }

        public ParcelableCallAnalytics[] newArray(int size) {
            return new ParcelableCallAnalytics[size];
        }
    };
    public static final int GSM_PHONE = 2;
    public static final int IMS_PHONE = 4;
    public static final long MILLIS_IN_1_SECOND = 1000;
    public static final long MILLIS_IN_5_MINUTES = 300000;
    public static final int SIP_PHONE = 8;
    public static final int STILL_CONNECTED = -1;
    public static final int THIRD_PARTY_PHONE = 16;
    private final List<AnalyticsEvent> analyticsEvents;
    private final long callDurationMillis;
    private final int callTechnologies;
    private final int callTerminationCode;
    private final int callType;
    private final String connectionService;
    private final List<EventTiming> eventTimings;
    private final boolean isAdditionalCall;
    private final boolean isCreatedFromExistingConnection;
    private final boolean isEmergencyCall;
    private final boolean isInterrupted;
    private boolean isVideoCall = false;
    private final long startTimeMillis;
    private List<VideoEvent> videoEvents;

    public static final class AnalyticsEvent implements Parcelable {
        public static final int AUDIO_ROUTE_BT = 204;
        public static final int AUDIO_ROUTE_EARPIECE = 205;
        public static final int AUDIO_ROUTE_HEADSET = 206;
        public static final int AUDIO_ROUTE_SPEAKER = 207;
        public static final int BIND_CS = 5;
        public static final int BLOCK_CHECK_FINISHED = 105;
        public static final int BLOCK_CHECK_INITIATED = 104;
        public static final int CONFERENCE_WITH = 300;
        public static final Creator<AnalyticsEvent> CREATOR = new Creator<AnalyticsEvent>() {
            public AnalyticsEvent createFromParcel(Parcel in) {
                return new AnalyticsEvent(in);
            }

            public AnalyticsEvent[] newArray(int size) {
                return new AnalyticsEvent[size];
            }
        };
        public static final int CS_BOUND = 6;
        public static final int DIRECT_TO_VM_FINISHED = 103;
        public static final int DIRECT_TO_VM_INITIATED = 102;
        public static final int FILTERING_COMPLETED = 107;
        public static final int FILTERING_INITIATED = 106;
        public static final int FILTERING_TIMED_OUT = 108;
        public static final int MUTE = 202;
        public static final int REMOTELY_HELD = 402;
        public static final int REMOTELY_UNHELD = 403;
        public static final int REQUEST_ACCEPT = 7;
        public static final int REQUEST_HOLD = 400;
        public static final int REQUEST_PULL = 500;
        public static final int REQUEST_REJECT = 8;
        public static final int REQUEST_UNHOLD = 401;
        public static final int SCREENING_COMPLETED = 101;
        public static final int SCREENING_SENT = 100;
        public static final int SET_ACTIVE = 1;
        public static final int SET_DIALING = 4;
        public static final int SET_DISCONNECTED = 2;
        public static final int SET_HOLD = 404;
        public static final int SET_PARENT = 302;
        public static final int SET_SELECT_PHONE_ACCOUNT = 0;
        public static final int SILENCE = 201;
        public static final int SKIP_RINGING = 200;
        public static final int SPLIT_CONFERENCE = 301;
        public static final int START_CONNECTION = 3;
        public static final int SWAP = 405;
        public static final int UNMUTE = 203;
        private int mEventName;
        private long mTimeSinceLastEvent;

        public AnalyticsEvent(int eventName, long timestamp) {
            this.mEventName = eventName;
            this.mTimeSinceLastEvent = timestamp;
        }

        AnalyticsEvent(Parcel in) {
            this.mEventName = in.readInt();
            this.mTimeSinceLastEvent = in.readLong();
        }

        public int getEventName() {
            return this.mEventName;
        }

        public long getTimeSinceLastEvent() {
            return this.mTimeSinceLastEvent;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.mEventName);
            out.writeLong(this.mTimeSinceLastEvent);
        }
    }

    public static final class EventTiming implements Parcelable {
        public static final int ACCEPT_TIMING = 0;
        public static final int BIND_CS_TIMING = 6;
        public static final int BLOCK_CHECK_FINISHED_TIMING = 9;
        public static final Creator<EventTiming> CREATOR = new Creator<EventTiming>() {
            public EventTiming createFromParcel(Parcel in) {
                return new EventTiming(in, null);
            }

            public EventTiming[] newArray(int size) {
                return new EventTiming[size];
            }
        };
        public static final int DIRECT_TO_VM_FINISHED_TIMING = 8;
        public static final int DISCONNECT_TIMING = 2;
        public static final int FILTERING_COMPLETED_TIMING = 10;
        public static final int FILTERING_TIMED_OUT_TIMING = 11;
        public static final int HOLD_TIMING = 3;
        public static final int INVALID = 999999;
        public static final int OUTGOING_TIME_TO_DIALING_TIMING = 5;
        public static final int REJECT_TIMING = 1;
        public static final int SCREENING_COMPLETED_TIMING = 7;
        public static final int UNHOLD_TIMING = 4;
        private int mName;
        private long mTime;

        public EventTiming(int name, long time) {
            this.mName = name;
            this.mTime = time;
        }

        private EventTiming(Parcel in) {
            this.mName = in.readInt();
            this.mTime = in.readLong();
        }

        public int getName() {
            return this.mName;
        }

        public long getTime() {
            return this.mTime;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.mName);
            out.writeLong(this.mTime);
        }
    }

    public static final class VideoEvent implements Parcelable {
        public static final Creator<VideoEvent> CREATOR = new Creator<VideoEvent>() {
            public VideoEvent createFromParcel(Parcel in) {
                return new VideoEvent(in);
            }

            public VideoEvent[] newArray(int size) {
                return new VideoEvent[size];
            }
        };
        public static final int RECEIVE_REMOTE_SESSION_MODIFY_REQUEST = 2;
        public static final int RECEIVE_REMOTE_SESSION_MODIFY_RESPONSE = 3;
        public static final int SEND_LOCAL_SESSION_MODIFY_REQUEST = 0;
        public static final int SEND_LOCAL_SESSION_MODIFY_RESPONSE = 1;
        private int mEventName;
        private long mTimeSinceLastEvent;
        private int mVideoState;

        public VideoEvent(int eventName, long timeSinceLastEvent, int videoState) {
            this.mEventName = eventName;
            this.mTimeSinceLastEvent = timeSinceLastEvent;
            this.mVideoState = videoState;
        }

        VideoEvent(Parcel in) {
            this.mEventName = in.readInt();
            this.mTimeSinceLastEvent = in.readLong();
            this.mVideoState = in.readInt();
        }

        public int getEventName() {
            return this.mEventName;
        }

        public long getTimeSinceLastEvent() {
            return this.mTimeSinceLastEvent;
        }

        public int getVideoState() {
            return this.mVideoState;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.mEventName);
            out.writeLong(this.mTimeSinceLastEvent);
            out.writeInt(this.mVideoState);
        }
    }

    public ParcelableCallAnalytics(long startTimeMillis, long callDurationMillis, int callType, boolean isAdditionalCall, boolean isInterrupted, int callTechnologies, int callTerminationCode, boolean isEmergencyCall, String connectionService, boolean isCreatedFromExistingConnection, List<AnalyticsEvent> analyticsEvents, List<EventTiming> eventTimings) {
        this.startTimeMillis = startTimeMillis;
        this.callDurationMillis = callDurationMillis;
        this.callType = callType;
        this.isAdditionalCall = isAdditionalCall;
        this.isInterrupted = isInterrupted;
        this.callTechnologies = callTechnologies;
        this.callTerminationCode = callTerminationCode;
        this.isEmergencyCall = isEmergencyCall;
        this.connectionService = connectionService;
        this.isCreatedFromExistingConnection = isCreatedFromExistingConnection;
        this.analyticsEvents = analyticsEvents;
        this.eventTimings = eventTimings;
    }

    public ParcelableCallAnalytics(Parcel in) {
        this.startTimeMillis = in.readLong();
        this.callDurationMillis = in.readLong();
        this.callType = in.readInt();
        this.isAdditionalCall = readByteAsBoolean(in);
        this.isInterrupted = readByteAsBoolean(in);
        this.callTechnologies = in.readInt();
        this.callTerminationCode = in.readInt();
        this.isEmergencyCall = readByteAsBoolean(in);
        this.connectionService = in.readString();
        this.isCreatedFromExistingConnection = readByteAsBoolean(in);
        this.analyticsEvents = new ArrayList();
        in.readTypedList(this.analyticsEvents, AnalyticsEvent.CREATOR);
        this.eventTimings = new ArrayList();
        in.readTypedList(this.eventTimings, EventTiming.CREATOR);
        this.isVideoCall = readByteAsBoolean(in);
        this.videoEvents = new LinkedList();
        in.readTypedList(this.videoEvents, VideoEvent.CREATOR);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(this.startTimeMillis);
        out.writeLong(this.callDurationMillis);
        out.writeInt(this.callType);
        writeBooleanAsByte(out, this.isAdditionalCall);
        writeBooleanAsByte(out, this.isInterrupted);
        out.writeInt(this.callTechnologies);
        out.writeInt(this.callTerminationCode);
        writeBooleanAsByte(out, this.isEmergencyCall);
        out.writeString(this.connectionService);
        writeBooleanAsByte(out, this.isCreatedFromExistingConnection);
        out.writeTypedList(this.analyticsEvents);
        out.writeTypedList(this.eventTimings);
        writeBooleanAsByte(out, this.isVideoCall);
        out.writeTypedList(this.videoEvents);
    }

    public void setIsVideoCall(boolean isVideoCall) {
        this.isVideoCall = isVideoCall;
    }

    public void setVideoEvents(List<VideoEvent> videoEvents) {
        this.videoEvents = videoEvents;
    }

    public long getStartTimeMillis() {
        return this.startTimeMillis;
    }

    public long getCallDurationMillis() {
        return this.callDurationMillis;
    }

    public int getCallType() {
        return this.callType;
    }

    public boolean isAdditionalCall() {
        return this.isAdditionalCall;
    }

    public boolean isInterrupted() {
        return this.isInterrupted;
    }

    public int getCallTechnologies() {
        return this.callTechnologies;
    }

    public int getCallTerminationCode() {
        return this.callTerminationCode;
    }

    public boolean isEmergencyCall() {
        return this.isEmergencyCall;
    }

    public String getConnectionService() {
        return this.connectionService;
    }

    public boolean isCreatedFromExistingConnection() {
        return this.isCreatedFromExistingConnection;
    }

    public List<AnalyticsEvent> analyticsEvents() {
        return this.analyticsEvents;
    }

    public List<EventTiming> getEventTimings() {
        return this.eventTimings;
    }

    public boolean isVideoCall() {
        return this.isVideoCall;
    }

    public List<VideoEvent> getVideoEvents() {
        return this.videoEvents;
    }

    public int describeContents() {
        return 0;
    }

    private static void writeBooleanAsByte(Parcel out, boolean b) {
        out.writeByte((byte) (b ? 1 : 0));
    }

    private static boolean readByteAsBoolean(Parcel in) {
        return in.readByte() == (byte) 1;
    }
}
