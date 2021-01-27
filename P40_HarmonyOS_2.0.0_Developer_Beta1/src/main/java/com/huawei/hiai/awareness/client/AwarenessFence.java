package com.huawei.hiai.awareness.client;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Locale;

public final class AwarenessFence implements Parcelable {
    public static final Parcelable.Creator<AwarenessFence> CREATOR = new Parcelable.Creator<AwarenessFence>() {
        /* class com.huawei.hiai.awareness.client.AwarenessFence.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AwarenessFence createFromParcel(Parcel in) {
            return new AwarenessFence(in);
        }

        @Override // android.os.Parcelable.Creator
        public AwarenessFence[] newArray(int size) {
            return new AwarenessFence[size];
        }
    };
    public static final String MESSAGE_TYPE = "context_awareness_fence";
    private Bundle args;
    private String identifier;
    private FenceState state;
    private String type;

    private AwarenessFence(Parcel in) {
        this.type = in.readString();
        this.identifier = in.readString();
        this.state = (FenceState) in.readParcelable(FenceState.class.getClassLoader());
        this.args = in.readBundle();
    }

    private AwarenessFence(String type2) {
        this.type = type2;
    }

    public static AwarenessFence create(String type2) {
        return new AwarenessFence(type2);
    }

    public String getType() {
        return this.type;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public AwarenessFence setIdentifier(String identifier2) {
        this.identifier = identifier2;
        return this;
    }

    public FenceState getState() {
        return this.state;
    }

    public void setState(FenceState state2) {
        this.state = state2;
    }

    public Bundle getArgs() {
        Bundle bundle = this.args;
        if (bundle != null) {
            bundle.setClassLoader(AwarenessFence.class.getClassLoader());
        }
        Bundle bundle2 = this.args;
        if (bundle2 != null) {
            return new Bundle(bundle2);
        }
        return null;
    }

    public AwarenessFence putArg(String name, int value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putInt(name, value);
        return this;
    }

    public AwarenessFence putArg(String name, long value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putLong(name, value);
        return this;
    }

    public AwarenessFence putArg(String name, byte value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putByte(name, value);
        return this;
    }

    public AwarenessFence putArg(String name, byte[] value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putByteArray(name, value);
        return this;
    }

    public AwarenessFence putArg(String name, boolean value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putBoolean(name, value);
        return this;
    }

    public AwarenessFence putArg(String name, String value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putString(name, value);
        return this;
    }

    public AwarenessFence putArg(String name, float value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putFloat(name, value);
        return this;
    }

    public AwarenessFence putArg(String name, double value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putDouble(name, value);
        return this;
    }

    public AwarenessFence putArg(String name, String[] value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putStringArray(name, value);
        return this;
    }

    public AwarenessFence putArg(String name, ArrayList<String> value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        this.args.putStringArrayList(name, value);
        return this;
    }

    public AwarenessFence putArg(String name, Parcelable... value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        if (value != null) {
            this.args.putParcelableArray(name, value);
        }
        return this;
    }

    public AwarenessFence putArg(String name, AwarenessFence... value) {
        if (this.args == null) {
            this.args = new Bundle();
        }
        if (value != null) {
            this.args.putParcelableArray(name, value);
        }
        return this;
    }

    public AwarenessFence[] getAwarenessFenceArray(String name) {
        Bundle bundle = this.args;
        if (bundle == null || !bundle.containsKey(name)) {
            return null;
        }
        Parcelable[] parcelables = this.args.getParcelableArray(name);
        if (parcelables instanceof AwarenessFence[]) {
            return (AwarenessFence[]) parcelables;
        }
        return null;
    }

    public AwarenessEnvelope toEnvelope() {
        return AwarenessEnvelope.create(MESSAGE_TYPE).putArg(MESSAGE_TYPE, this);
    }

    public Intent toIntent() {
        return new Intent().putExtra(MESSAGE_TYPE, this);
    }

    public static AwarenessFence parseFrom(Intent intent) {
        if (intent == null || !intent.hasExtra(MESSAGE_TYPE)) {
            return null;
        }
        Parcelable parcelable = intent.getParcelableExtra(MESSAGE_TYPE);
        if (parcelable instanceof AwarenessFence) {
            return (AwarenessFence) parcelable;
        }
        return null;
    }

    public static AwarenessFence parseFrom(AwarenessEnvelope envelope) {
        if (envelope == null || !MESSAGE_TYPE.equals(envelope.getMessageType()) || envelope.getArgs() == null) {
            return null;
        }
        Bundle args2 = envelope.getArgs();
        args2.setClassLoader(AwarenessFence.class.getClassLoader());
        Parcelable parcelable = args2.getParcelable(MESSAGE_TYPE);
        if (parcelable instanceof AwarenessFence) {
            return (AwarenessFence) parcelable;
        }
        return null;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.type);
        dest.writeString(this.identifier);
        dest.writeParcelable(this.state, flags);
        dest.writeBundle(this.args);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ENGLISH, "AwarenessFence(%s)", this.type);
    }
}
