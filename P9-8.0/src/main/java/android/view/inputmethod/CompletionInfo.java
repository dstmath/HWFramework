package android.view.inputmethod;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;

public final class CompletionInfo implements Parcelable {
    public static final Creator<CompletionInfo> CREATOR = new Creator<CompletionInfo>() {
        public CompletionInfo createFromParcel(Parcel source) {
            return new CompletionInfo(source, null);
        }

        public CompletionInfo[] newArray(int size) {
            return new CompletionInfo[size];
        }
    };
    private final long mId;
    private final CharSequence mLabel;
    private final int mPosition;
    private final CharSequence mText;

    /* synthetic */ CompletionInfo(Parcel source, CompletionInfo -this1) {
        this(source);
    }

    public CompletionInfo(long id, int index, CharSequence text) {
        this.mId = id;
        this.mPosition = index;
        this.mText = text;
        this.mLabel = null;
    }

    public CompletionInfo(long id, int index, CharSequence text, CharSequence label) {
        this.mId = id;
        this.mPosition = index;
        this.mText = text;
        this.mLabel = label;
    }

    private CompletionInfo(Parcel source) {
        this.mId = source.readLong();
        this.mPosition = source.readInt();
        this.mText = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.mLabel = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
    }

    public long getId() {
        return this.mId;
    }

    public int getPosition() {
        return this.mPosition;
    }

    public CharSequence getText() {
        return this.mText;
    }

    public CharSequence getLabel() {
        return this.mLabel;
    }

    public String toString() {
        return "CompletionInfo{#" + this.mPosition + " \"" + this.mText + "\" id=" + this.mId + " label=" + this.mLabel + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mId);
        dest.writeInt(this.mPosition);
        TextUtils.writeToParcel(this.mText, dest, flags);
        TextUtils.writeToParcel(this.mLabel, dest, flags);
    }

    public int describeContents() {
        return 0;
    }
}
