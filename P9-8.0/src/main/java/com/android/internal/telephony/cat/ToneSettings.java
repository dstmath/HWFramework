package com.android.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ToneSettings implements Parcelable {
    public static final Creator<ToneSettings> CREATOR = new Creator<ToneSettings>() {
        public ToneSettings createFromParcel(Parcel in) {
            return new ToneSettings(in, null);
        }

        public ToneSettings[] newArray(int size) {
            return new ToneSettings[size];
        }
    };
    public Duration duration;
    public Tone tone;
    public boolean vibrate;

    /* synthetic */ ToneSettings(Parcel in, ToneSettings -this1) {
        this(in);
    }

    public ToneSettings(Duration duration, Tone tone, boolean vibrate) {
        this.duration = duration;
        this.tone = tone;
        this.vibrate = vibrate;
    }

    private ToneSettings(Parcel in) {
        this.duration = (Duration) in.readParcelable(null);
        this.tone = (Tone) in.readParcelable(null);
        this.vibrate = in.readInt() == 1;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i = 0;
        dest.writeParcelable(this.duration, 0);
        dest.writeParcelable(this.tone, 0);
        if (this.vibrate) {
            i = 1;
        }
        dest.writeInt(i);
    }
}
