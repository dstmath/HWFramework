package com.android.internal.telephony.cat;

import android.os.Parcel;
import android.os.Parcelable;

public enum Tone implements Parcelable {
    DIAL(1),
    BUSY(2),
    CONGESTION(3),
    RADIO_PATH_ACK(4),
    RADIO_PATH_NOT_AVAILABLE(5),
    ERROR_SPECIAL_INFO(6),
    CALL_WAITING(7),
    RINGING(8),
    GENERAL_BEEP(16),
    POSITIVE_ACK(17),
    NEGATIVE_ACK(18),
    INCOMING_SPEECH_CALL(19),
    INCOMING_SMS(20),
    CRITICAL_ALERT(21),
    VIBRATE_ONLY(32),
    HAPPY(48),
    SAD(49),
    URGENT(50),
    QUESTION(51),
    MESSAGE_RECEIVED(52),
    MELODY_1(64),
    MELODY_2(65),
    MELODY_3(66),
    MELODY_4(67),
    MELODY_5(68),
    MELODY_6(69),
    MELODY_7(70),
    MELODY_8(71);
    
    public static final Parcelable.Creator<Tone> CREATOR = new Parcelable.Creator<Tone>() {
        /* class com.android.internal.telephony.cat.Tone.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public Tone createFromParcel(Parcel in) {
            return Tone.values()[in.readInt()];
        }

        @Override // android.os.Parcelable.Creator
        public Tone[] newArray(int size) {
            return new Tone[size];
        }
    };
    private int mValue;

    private Tone(int value) {
        this.mValue = value;
    }

    public static Tone fromInt(int value) {
        Tone[] values = values();
        for (Tone e : values) {
            if (e.mValue == value) {
                return e;
            }
        }
        return null;
    }

    private Tone(Parcel in) {
        this.mValue = in.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ordinal());
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
