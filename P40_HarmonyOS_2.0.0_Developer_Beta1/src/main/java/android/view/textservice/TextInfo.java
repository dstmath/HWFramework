package android.view.textservice;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.SpellCheckSpan;

public final class TextInfo implements Parcelable {
    public static final Parcelable.Creator<TextInfo> CREATOR = new Parcelable.Creator<TextInfo>() {
        /* class android.view.textservice.TextInfo.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public TextInfo createFromParcel(Parcel source) {
            return new TextInfo(source);
        }

        @Override // android.os.Parcelable.Creator
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
        if (TextUtils.isEmpty(text)) {
            return 0;
        }
        return text.length();
    }

    public TextInfo(CharSequence charSequence, int start, int end, int cookie, int sequenceNumber) {
        SpellCheckSpan[] spans;
        if (!TextUtils.isEmpty(charSequence)) {
            SpannableStringBuilder spannableString = new SpannableStringBuilder(charSequence, start, end);
            for (SpellCheckSpan spellCheckSpan : (SpellCheckSpan[]) spannableString.getSpans(0, spannableString.length(), SpellCheckSpan.class)) {
                spannableString.removeSpan(spellCheckSpan);
            }
            this.mCharSequence = spannableString;
            this.mCookie = cookie;
            this.mSequenceNumber = sequenceNumber;
            return;
        }
        throw new IllegalArgumentException("charSequence is empty");
    }

    public TextInfo(Parcel source) {
        this.mCharSequence = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(source);
        this.mCookie = source.readInt();
        this.mSequenceNumber = source.readInt();
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        TextUtils.writeToParcel(this.mCharSequence, dest, flags);
        dest.writeInt(this.mCookie);
        dest.writeInt(this.mSequenceNumber);
    }

    public String getText() {
        CharSequence charSequence = this.mCharSequence;
        if (charSequence == null) {
            return null;
        }
        return charSequence.toString();
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

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }
}
