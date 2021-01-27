package com.huawei.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;

public class ErrorInfoEvent extends Event implements Parcelable {
    public static final int BASE = 100;
    public static final int CODE_APP_STREAM_UNSUPPORTED = 102;
    public static final int CODE_LISTENER_UNREGISTERED = 101;
    public static final Parcelable.Creator<ErrorInfoEvent> CREATOR = new Parcelable.Creator<ErrorInfoEvent>() {
        /* class com.huawei.airsharing.api.ErrorInfoEvent.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ErrorInfoEvent createFromParcel(Parcel in) {
            return new ErrorInfoEvent(in);
        }

        @Override // android.os.Parcelable.Creator
        public ErrorInfoEvent[] newArray(int size) {
            return new ErrorInfoEvent[size];
        }
    };
    public static final String DESC_APP_STREAM_UNSUPPORTED = "peer device doesn't support app stream.";
    public static final String DESC_LISTENER_UNREGISTERED = "IAidlMediaPlayerListener unregistered.";
    private static final String NO_DESCRIPTION = "no description";
    private int mCode;
    private String mDescription;

    public ErrorInfoEvent(int eventId, int code) {
        this(eventId, code, NO_DESCRIPTION);
    }

    public ErrorInfoEvent(int eventId, int code, String description) {
        super(eventId);
        this.mCode = 0;
        this.mDescription = NO_DESCRIPTION;
        this.mCode = code;
        this.mDescription = description;
    }

    protected ErrorInfoEvent(Parcel in) {
        super(in);
        this.mCode = 0;
        this.mDescription = NO_DESCRIPTION;
        this.mCode = in.readInt();
        this.mDescription = in.readString();
    }

    protected ErrorInfoEvent(int eventId, Parcel in) {
        super(eventId);
        this.mCode = 0;
        this.mDescription = NO_DESCRIPTION;
        this.mCode = in.readInt();
        this.mDescription = in.readString();
    }

    @Override // com.huawei.airsharing.api.Event, android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mCode);
        dest.writeString(this.mDescription);
    }

    @Override // com.huawei.airsharing.api.Event, android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int getErrorCode() {
        return this.mCode;
    }

    public String getErrorDescription() {
        return this.mDescription;
    }
}
