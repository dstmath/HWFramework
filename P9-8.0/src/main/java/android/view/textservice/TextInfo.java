package android.view.textservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.SpellCheckSpan;

public final class TextInfo implements Parcelable {
    public static final Creator<TextInfo> CREATOR = new Creator<TextInfo>() {
        public TextInfo createFromParcel(Parcel source) {
            return new TextInfo(source);
        }

        public TextInfo[] newArray(int size) {
            return new TextInfo[size];
        }
    };
    private static final int DEFAULT_COOKIE = 0;
    private static final int DEFAULT_SEQUENCE_NUMBER = 0;
    private final CharSequence mCharSequence;
    private final int mCookie;
    private final int mSequenceNumber;

    public TextInfo(String text) {
        this(text, 0, getStringLengthOrZero(text), 0, 0);
    }

    public TextInfo(String text, int cookie, int sequenceNumber) {
        this(text, 0, getStringLengthOrZero(text), cookie, sequenceNumber);
    }

    private static int getStringLengthOrZero(String text) {
        return TextUtils.isEmpty(text) ? 0 : text.length();
    }

    public TextInfo(CharSequence charSequence, int start, int end, int cookie, int sequenceNumber) {
        if (TextUtils.isEmpty(charSequence)) {
            throw new IllegalArgumentException("charSequence is empty");
        }
        SpannableStringBuilder spannableString = new SpannableStringBuilder(charSequence, start, end);
        SpellCheckSpan[] spans = (SpellCheckSpan[]) spannableString.getSpans(0, spannableString.length(), SpellCheckSpan.class);
        for (Object removeSpan : spans) {
            spannableString.removeSpan(removeSpan);
        }
        this.mCharSequence = spannableString;
        this.mCookie = cookie;
        this.mSequenceNumber = sequenceNumber;
    }

    public TextInfo(Parcel source) {
        this.mCharSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.mCookie = source.readInt();
        this.mSequenceNumber = source.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.mCharSequence, dest, flags);
        dest.writeInt(this.mCookie);
        dest.writeInt(this.mSequenceNumber);
    }

    public String getText() {
        if (this.mCharSequence == null) {
            return null;
        }
        return this.mCharSequence.toString();
    }

    public CharSequence getCharSequence() {
        return this.mCharSequence;
    }

    public int getCookie() {
        return this.mCookie;
    }

    public int getSequence() {
        return this.mSequenceNumber;
    }

    public int describeContents() {
        return 0;
    }
}
