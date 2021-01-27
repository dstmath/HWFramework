package com.android.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;

public class ToneSettings implements Parcelable {
    public static final Parcelable.Creator<ToneSettings> CREATOR = new Parcelable.Creator<ToneSettings>() {
        /* class com.android.internal.telephony.cat.ToneSettings.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public ToneSettings createFromParcel(Parcel in) {
            return new ToneSettings(in);
        }

        @Override // android.os.Parcelable.Creator
        public ToneSettings[] newArray(int size) {
            return new ToneSettings[size];
        }
    };
    public Duration duration;
    public Tone tone;
    public boolean vibrate;

    public ToneSettings(Duration duration2, Tone tone2, boolean vibrate2) {
        this.duration = duration2;
        this.tone = tone2;
        this.vibrate = vibrate2;
    }

    private ToneSettings(Parcel in) {
        this.duration = (Duration) in.readParcelable(null);
        this.tone = (Tone) in.readParcelable(null);
        this.vibrate = in.readInt() != 1 ? false : true;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.duration, 0);
        dest.writeParcelable(this.tone, 0);
        dest.writeInt(this.vibrate ? 1 : 0);
    }
}
