package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Event implements Parcelable {
    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<Event>() {
        /* class com.huawei.airsharing.api.Event.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Event createFromParcel(Parcel in) {
            int eventId = in.readInt();
            Log.d(Event.TAG, "eventId " + eventId);
            if (eventId == 3600) {
                return new ErrorInfoEvent(eventId, in);
            }
            switch (eventId) {
                case 3100:
                case 3101:
                case IEventListener.EVENT_ID_HISIGHT_STATE_DISCONNECTED /*{ENCODED_INT: 3102}*/:
                    break;
                default:
                    switch (eventId) {
                        case IEventListener.EVENT_ID_HISIGHT_STATE_PLAYING /*{ENCODED_INT: 3104}*/:
                        case IEventListener.EVENT_ID_HISIGHT_STATE_PAUSED /*{ENCODED_INT: 3105}*/:
                        case IEventListener.EVENT_ID_HISIGHT_STATE_CONNECTING /*{ENCODED_INT: 3106}*/:
                            break;
                        case IEventListener.EVENT_ID_HISIGHT_STATE_AUTH_CODE_REQ /*{ENCODED_INT: 3107}*/:
                            return new AuthRequestEvent(eventId, in);
                        default:
                            return new Event(eventId);
                    }
            }
            return new ConnectionStatusEvent(eventId, in);
        }

        @Override // android.os.Parcelable.Creator
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
    protected static final String TAG = "AirsharingEvent";
    protected final int mEventId;

    public Event(int eventId) {
        this.mEventId = eventId;
    }

    protected Event(Parcel in) {
        this.mEventId = in.readInt();
    }

    public int getEventId() {
        return this.mEventId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mEventId);
    }
}
