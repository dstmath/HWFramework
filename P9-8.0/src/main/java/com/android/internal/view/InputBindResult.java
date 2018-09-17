package com.android.internal.view;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.view.InputChannel;
import com.android.internal.view.IInputMethodSession.Stub;

public final class InputBindResult implements Parcelable {
    public static final Creator<InputBindResult> CREATOR = new Creator<InputBindResult>() {
        public InputBindResult createFromParcel(Parcel source) {
            return new InputBindResult(source);
        }

        public InputBindResult[] newArray(int size) {
            return new InputBindResult[size];
        }
    };
    static final String TAG = "InputBindResult";
    public final InputChannel channel;
    public final String id;
    public final IInputMethodSession method;
    public final int sequence;
    public final int userActionNotificationSequenceNumber;

    public InputBindResult(IInputMethodSession _method, InputChannel _channel, String _id, int _sequence, int _userActionNotificationSequenceNumber) {
        this.method = _method;
        this.channel = _channel;
        this.id = _id;
        this.sequence = _sequence;
        this.userActionNotificationSequenceNumber = _userActionNotificationSequenceNumber;
    }

    InputBindResult(Parcel source) {
        this.method = Stub.asInterface(source.readStrongBinder());
        if (source.readInt() != 0) {
            this.channel = (InputChannel) InputChannel.CREATOR.createFromParcel(source);
        } else {
            this.channel = null;
        }
        this.id = source.readString();
        this.sequence = source.readInt();
        this.userActionNotificationSequenceNumber = source.readInt();
    }

    public String toString() {
        return "InputBindResult{" + this.method + " " + this.id + " sequence:" + this.sequence + " userActionNotificationSequenceNumber:" + this.userActionNotificationSequenceNumber + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongInterface(this.method);
        if (this.channel != null) {
            dest.writeInt(1);
            this.channel.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeString(this.id);
        dest.writeInt(this.sequence);
        dest.writeInt(this.userActionNotificationSequenceNumber);
    }

    public int describeContents() {
        return this.channel != null ? this.channel.describeContents() : 0;
    }
}
