package android.text.style;

import android.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.text.ParcelableSpan;

public class SpellCheckSpan implements ParcelableSpan {
    private boolean mSpellCheckInProgress;

    @UnsupportedAppUsage
    public SpellCheckSpan() {
        this.mSpellCheckInProgress = false;
    }

    @UnsupportedAppUsage
    public SpellCheckSpan(Parcel src) {
        this.mSpellCheckInProgress = src.readInt() != 0;
    }

    @UnsupportedAppUsage
    public void setSpellCheckInProgress(boolean inProgress) {
        this.mSpellCheckInProgress = inProgress;
    }

    @UnsupportedAppUsage
    public boolean isSpellCheckInProgress() {
        return this.mSpellCheckInProgress;
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    @Override // android.text.ParcelableSpan
    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeInt(this.mSpellCheckInProgress ? 1 : 0);
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeIdInternal() {
        return 20;
    }
}
