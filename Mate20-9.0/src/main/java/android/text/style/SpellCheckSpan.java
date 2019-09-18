package android.text.style;

import android.os.Parcel;
import android.text.ParcelableSpan;

public class SpellCheckSpan implements ParcelableSpan {
    private boolean mSpellCheckInProgress;

    public SpellCheckSpan() {
        this.mSpellCheckInProgress = false;
    }

    public SpellCheckSpan(Parcel src) {
        this.mSpellCheckInProgress = src.readInt() != 0;
    }

    public void setSpellCheckInProgress(boolean inProgress) {
        this.mSpellCheckInProgress = inProgress;
    }

    public boolean isSpellCheckInProgress() {
        return this.mSpellCheckInProgress;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeInt(this.mSpellCheckInProgress ? 1 : 0);
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 20;
    }
}
