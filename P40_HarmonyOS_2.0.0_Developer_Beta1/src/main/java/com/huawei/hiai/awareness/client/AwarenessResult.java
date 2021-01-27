package com.huawei.hiai.awareness.client;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Locale;

public class AwarenessResult implements Parcelable {
    public static final Parcelable.Creator<AwarenessResult> CREATOR = new Parcelable.Creator<AwarenessResult>() {
        /* class com.huawei.hiai.awareness.client.AwarenessResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public AwarenessResult createFromParcel(Parcel in) {
            return new AwarenessResult(in);
        }

        @Override // android.os.Parcelable.Creator
        public AwarenessResult[] newArray(int size) {
            return new AwarenessResult[size];
        }
    };
    public static final String MESSAGE_TYPE = "context_awareness_result";
    private int code;
    private String reason;

    public static final class Code {
        public static final int FAILURE = 0;
        public static final int SUCCESS = 10000;
    }

    private AwarenessResult(Parcel in) {
        this.code = in.readInt();
        this.reason = in.readString();
    }

    public AwarenessResult(int code2) {
        this.code = code2;
    }

    public AwarenessResult(int code2, String reason2) {
        this.code = code2;
        this.reason = reason2;
    }

    public int code() {
        return this.code;
    }

    public String reason() {
        return this.reason;
    }

    public boolean isSuccessful() {
        return code() >= 10000;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.code);
        dest.writeString(this.reason);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // java.lang.Object
    public String toString() {
        return String.format(Locale.ENGLISH, "AwarenessResult{%d} - %s", Integer.valueOf(code()), reason());
    }
}
