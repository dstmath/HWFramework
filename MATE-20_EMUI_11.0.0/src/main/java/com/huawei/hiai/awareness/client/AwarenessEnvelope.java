package com.huawei.hiai.awareness.client;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Locale;

public class AwarenessEnvelope implements Parcelable {
    public static final Parcelable.Creator<AwarenessEnvelope> CREATOR = new Parcelable.Creator<AwarenessEnvelope>() {
        /* class com.huawei.hiai.awareness.client.AwarenessEnvelope.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AwarenessEnvelope createFromParcel(Parcel in) {
            return new AwarenessEnvelope(in);
        }

        @Override // android.os.Parcelable.Creator
        public AwarenessEnvelope[] newArray(int size) {
            return new AwarenessEnvelope[size];
        }
    };
    public static final String MESSAGE_TYPE = "context_awareness_envelope";
    private Bundle args;
    private String messageType;

    private AwarenessEnvelope(String messageType2) {
        this.messageType = messageType2;
    }

    private AwarenessEnvelope(Parcel in) {
        this.messageType = in.readString();
        this.args = in.readBundle();
    }

    public static AwarenessEnvelope create(String messageType2) {
        return new AwarenessEnvelope(messageType2);
    }

    public String getMessageType() {
        return this.messageType;
    }

    public Bundle getArgs() {
        Bundle bundle = this.args;
        if (bundle != null) {
            return new Bundle(bundle);
        }
        return null;
    }

    public AwarenessEnvelope putArg(String name, int value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putInt(name, value);
        return this;
    }

    public AwarenessEnvelope putArg(String name, long value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putLong(name, value);
        return this;
    }

    public AwarenessEnvelope putArg(String name, boolean value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putBoolean(name, value);
        return this;
    }

    public AwarenessEnvelope putArg(String name, String value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putString(name, value);
        return this;
    }

    public AwarenessEnvelope putArg(String name, double value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putDouble(name, value);
        return this;
    }

    public AwarenessEnvelope putArg(String name, ArrayList<String> value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putStringArrayList(name, value);
        return this;
    }

    public AwarenessEnvelope putArg(String name, byte value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putByte(name, value);
        return this;
    }

    public AwarenessEnvelope putArg(String name, byte[] value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putByteArray(name, value);
        return this;
    }

    public AwarenessEnvelope putArg(String name, Parcelable value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putParcelable(name, value);
        return this;
    }

    public AwarenessEnvelope putArg(String name, IBinder value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putBinder(name, value);
        return this;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.messageType);
        dest.writeBundle(this.args);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ENGLISH, "AwarenessEnvelope(%s)", this.messageType);
    }
}
