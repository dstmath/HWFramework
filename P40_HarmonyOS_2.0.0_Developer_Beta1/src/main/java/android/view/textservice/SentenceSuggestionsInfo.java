package android.view.textservice;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Arrays;

public final class SentenceSuggestionsInfo implements Parcelable {
    public static final Parcelable.Creator<SentenceSuggestionsInfo> CREATOR = new Parcelable.Creator<SentenceSuggestionsInfo>() {
        /* class android.view.textservice.SentenceSuggestionsInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SentenceSuggestionsInfo createFromParcel(Parcel source) {
            return new SentenceSuggestionsInfo(source);
        }

        @Override // android.os.Parcelable.Creator
        public SentenceSuggestionsInfo[] newArray(int size) {
            return new SentenceSuggestionsInfo[size];
        }
    };
    private final int[] mLengths;
    private final int[] mOffsets;
    private final SuggestionsInfo[] mSuggestionsInfos;

    public SentenceSuggestionsInfo(SuggestionsInfo[] suggestionsInfos, int[] offsets, int[] lengths) {
        if (suggestionsInfos == null || offsets == null || lengths == null) {
            throw new NullPointerException();
        } else if (suggestionsInfos.length == offsets.length && offsets.length == lengths.length) {
            int infoSize = suggestionsInfos.length;
            this.mSuggestionsInfos = (SuggestionsInfo[]) Arrays.copyOf(suggestionsInfos, infoSize);
            this.mOffsets = Arrays.copyOf(offsets, infoSize);
            this.mLengths = Arrays.copyOf(lengths, infoSize);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public SentenceSuggestionsInfo(Parcel source) {
        this.mSuggestionsInfos = new SuggestionsInfo[source.readInt()];
        source.readTypedArray(this.mSuggestionsInfos, SuggestionsInfo.CREATOR);
        this.mOffsets = new int[this.mSuggestionsInfos.length];
        source.readIntArray(this.mOffsets);
        this.mLengths = new int[this.mSuggestionsInfos.length];
        source.readIntArray(this.mLengths);
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSuggestionsInfos.length);
        dest.writeTypedArray(this.mSuggestionsInfos, 0);
        dest.writeIntArray(this.mOffsets);
        dest.writeIntArray(this.mLengths);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public int getSuggestionsCount() {
        return this.mSuggestionsInfos.length;
    }

    public SuggestionsInfo getSuggestionsInfoAt(int i) {
        if (i < 0) {
            return null;
        }
        SuggestionsInfo[] suggestionsInfoArr = this.mSuggestionsInfos;
        if (i < suggestionsInfoArr.length) {
            return suggestionsInfoArr[i];
        }
        return null;
    }

    public int getOffsetAt(int i) {
        if (i < 0) {
            return -1;
        }
        int[] iArr = this.mOffsets;
        if (i < iArr.length) {
            return iArr[i];
        }
        return -1;
    }

    public int getLengthAt(int i) {
        if (i < 0) {
            return -1;
        }
        int[] iArr = this.mLengths;
        if (i < iArr.length) {
            return iArr[i];
        }
        return -1;
    }
}
