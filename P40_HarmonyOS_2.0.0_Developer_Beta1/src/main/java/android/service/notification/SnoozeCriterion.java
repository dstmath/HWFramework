package android.service.notification;

import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

@SystemApi
public final class SnoozeCriterion implements Parcelable {
    public static final Parcelable.Creator<SnoozeCriterion> CREATOR = new Parcelable.Creator<SnoozeCriterion>() {
        /* class android.service.notification.SnoozeCriterion.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SnoozeCriterion createFromParcel(Parcel in) {
            return new SnoozeCriterion(in);
        }

        @Override // android.os.Parcelable.Creator
        public SnoozeCriterion[] newArray(int size) {
            return new SnoozeCriterion[size];
        }
    };
    private final CharSequence mConfirmation;
    private final CharSequence mExplanation;
    private final String mId;

    public SnoozeCriterion(String id, CharSequence explanation, CharSequence confirmation) {
        this.mId = id;
        this.mExplanation = explanation;
        this.mConfirmation = confirmation;
    }

    protected SnoozeCriterion(Parcel in) {
        if (in.readByte() != 0) {
            this.mId = in.readString();
        } else {
            this.mId = null;
        }
        if (in.readByte() != 0) {
            this.mExplanation = in.readCharSequence();
        } else {
            this.mExplanation = null;
        }
        if (in.readByte() != 0) {
            this.mConfirmation = in.readCharSequence();
        } else {
            this.mConfirmation = null;
        }
    }

    public String getId() {
        return this.mId;
    }

    public CharSequence getExplanation() {
        return this.mExplanation;
    }

    public CharSequence getConfirmation() {
        return this.mConfirmation;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        if (this.mId != null) {
            dest.writeByte((byte) 1);
            dest.writeString(this.mId);
        } else {
            dest.writeByte((byte) 0);
        }
        if (this.mExplanation != null) {
            dest.writeByte((byte) 1);
            dest.writeCharSequence(this.mExplanation);
        } else {
            dest.writeByte((byte) 0);
        }
        if (this.mConfirmation != null) {
            dest.writeByte((byte) 1);
            dest.writeCharSequence(this.mConfirmation);
            return;
        }
        dest.writeByte((byte) 0);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SnoozeCriterion that = (SnoozeCriterion) o;
        String str = this.mId;
        if (str == null ? that.mId != null : !str.equals(that.mId)) {
            return false;
        }
        CharSequence charSequence = this.mExplanation;
        if (charSequence == null ? that.mExplanation != null : !charSequence.equals(that.mExplanation)) {
            return false;
        }
        CharSequence charSequence2 = this.mConfirmation;
        if (charSequence2 != null) {
            return charSequence2.equals(that.mConfirmation);
        }
        if (that.mConfirmation == null) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        String str = this.mId;
        int i = 0;
        int hashCode = (str != null ? str.hashCode() : 0) * 31;
        CharSequence charSequence = this.mExplanation;
        int result = (hashCode + (charSequence != null ? charSequence.hashCode() : 0)) * 31;
        CharSequence charSequence2 = this.mConfirmation;
        if (charSequence2 != null) {
            i = charSequence2.hashCode();
        }
        return result + i;
    }
}
