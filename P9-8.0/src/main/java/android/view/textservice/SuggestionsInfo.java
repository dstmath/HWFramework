package android.view.textservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.internal.util.ArrayUtils;

public final class SuggestionsInfo implements Parcelable {
    public static final Creator<SuggestionsInfo> CREATOR = new Creator<SuggestionsInfo>() {
        public SuggestionsInfo createFromParcel(Parcel source) {
            return new SuggestionsInfo(source);
        }

        public SuggestionsInfo[] newArray(int size) {
            return new SuggestionsInfo[size];
        }
    };
    private static final String[] EMPTY = ((String[]) ArrayUtils.emptyArray(String.class));
    public static final int RESULT_ATTR_HAS_RECOMMENDED_SUGGESTIONS = 4;
    public static final int RESULT_ATTR_IN_THE_DICTIONARY = 1;
    public static final int RESULT_ATTR_LOOKS_LIKE_TYPO = 2;
    private int mCookie;
    private int mSequence;
    private final String[] mSuggestions;
    private final int mSuggestionsAttributes;
    private final boolean mSuggestionsAvailable;

    public SuggestionsInfo(int suggestionsAttributes, String[] suggestions) {
        this(suggestionsAttributes, suggestions, 0, 0);
    }

    public SuggestionsInfo(int suggestionsAttributes, String[] suggestions, int cookie, int sequence) {
        if (suggestions == null) {
            this.mSuggestions = EMPTY;
            this.mSuggestionsAvailable = false;
        } else {
            this.mSuggestions = suggestions;
            this.mSuggestionsAvailable = true;
        }
        this.mSuggestionsAttributes = suggestionsAttributes;
        this.mCookie = cookie;
        this.mSequence = sequence;
    }

    public SuggestionsInfo(Parcel source) {
        boolean z = true;
        this.mSuggestionsAttributes = source.readInt();
        this.mSuggestions = source.readStringArray();
        this.mCookie = source.readInt();
        this.mSequence = source.readInt();
        if (source.readInt() != 1) {
            z = false;
        }
        this.mSuggestionsAvailable = z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mSuggestionsAttributes);
        dest.writeStringArray(this.mSuggestions);
        dest.writeInt(this.mCookie);
        dest.writeInt(this.mSequence);
        dest.writeInt(this.mSuggestionsAvailable ? 1 : 0);
    }

    public void setCookieAndSequence(int cookie, int sequence) {
        this.mCookie = cookie;
        this.mSequence = sequence;
    }

    public int getCookie() {
        return this.mCookie;
    }

    public int getSequence() {
        return this.mSequence;
    }

    public int getSuggestionsAttributes() {
        return this.mSuggestionsAttributes;
    }

    public int getSuggestionsCount() {
        if (this.mSuggestionsAvailable) {
            return this.mSuggestions.length;
        }
        return -1;
    }

    public String getSuggestionAt(int i) {
        return this.mSuggestions[i];
    }

    public int describeContents() {
        return 0;
    }
}
