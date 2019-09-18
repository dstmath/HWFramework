package android.view.inputmethod;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public final class CorrectionInfo implements Parcelable {
    public static final Parcelable.Creator<CorrectionInfo> CREATOR = new Parcelable.Creator<CorrectionInfo>() {
        public CorrectionInfo createFromParcel(Parcel source) {
            return new CorrectionInfo(source);
        }

        public CorrectionInfo[] newArray(int size) {
            return new CorrectionInfo[size];
        }
    };
    private final CharSequence mNewText;
    private final int mOffset;
    private final CharSequence mOldText;

    public CorrectionInfo(int offset, CharSequence oldText, CharSequence newText) {
        this.mOffset = offset;
        this.mOldText = oldText;
        this.mNewText = newText;
    }

    private CorrectionInfo(Parcel source) {
        this.mOffset = source.readInt();
        this.mOldText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.mNewText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
    }

    public int getOffset() {
        return this.mOffset;
    }

    public CharSequence getOldText() {
        return this.mOldText;
    }

    public CharSequence getNewText() {
        return this.mNewText;
    }

    public String toString() {
        return "CorrectionInfo{#" + this.mOffset + " \"" + this.mOldText + "\" -> \"" + this.mNewText + "\"}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mOffset);
        TextUtils.writeToParcel(this.mOldText, dest, flags);
        TextUtils.writeToParcel(this.mNewText, dest, flags);
    }

    public int describeContents() {
        return 0;
    }
}
