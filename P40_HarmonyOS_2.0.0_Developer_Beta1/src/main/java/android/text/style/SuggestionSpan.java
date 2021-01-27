package android.text.style;

import android.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.util.Log;
import com.android.internal.R;
import java.util.Arrays;
import java.util.Locale;

public class SuggestionSpan extends CharacterStyle implements ParcelableSpan {
    @Deprecated
    public static final String ACTION_SUGGESTION_PICKED = "android.text.style.SUGGESTION_PICKED";
    public static final Parcelable.Creator<SuggestionSpan> CREATOR = new Parcelable.Creator<SuggestionSpan>() {
        /* class android.text.style.SuggestionSpan.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public SuggestionSpan createFromParcel(Parcel source) {
            return new SuggestionSpan(source);
        }

        @Override // android.os.Parcelable.Creator
        public SuggestionSpan[] newArray(int size) {
            return new SuggestionSpan[size];
        }
    };
    public static final int FLAG_AUTO_CORRECTION = 4;
    public static final int FLAG_EASY_CORRECT = 1;
    public static final int FLAG_MISSPELLED = 2;
    public static final int SUGGESTIONS_MAX_SIZE = 5;
    @Deprecated
    public static final String SUGGESTION_SPAN_PICKED_AFTER = "after";
    @Deprecated
    public static final String SUGGESTION_SPAN_PICKED_BEFORE = "before";
    @Deprecated
    public static final String SUGGESTION_SPAN_PICKED_HASHCODE = "hashcode";
    private static final String TAG = "SuggestionSpan";
    private int mAutoCorrectionUnderlineColor;
    private float mAutoCorrectionUnderlineThickness;
    @UnsupportedAppUsage
    private int mEasyCorrectUnderlineColor;
    @UnsupportedAppUsage
    private float mEasyCorrectUnderlineThickness;
    private int mFlags;
    private final int mHashCode;
    private final String mLanguageTag;
    private final String mLocaleStringForCompatibility;
    private int mMisspelledUnderlineColor;
    private float mMisspelledUnderlineThickness;
    private final String[] mSuggestions;

    public SuggestionSpan(Context context, String[] suggestions, int flags) {
        this(context, null, suggestions, flags, null);
    }

    public SuggestionSpan(Locale locale, String[] suggestions, int flags) {
        this(null, locale, suggestions, flags, null);
    }

    public SuggestionSpan(Context context, Locale locale, String[] suggestions, int flags, Class<?> cls) {
        Locale sourceLocale;
        this.mSuggestions = (String[]) Arrays.copyOf(suggestions, Math.min(5, suggestions.length));
        this.mFlags = flags;
        if (locale != null) {
            sourceLocale = locale;
        } else if (context != null) {
            sourceLocale = context.getResources().getConfiguration().locale;
        } else {
            Log.e(TAG, "No locale or context specified in SuggestionSpan constructor");
            sourceLocale = null;
        }
        String str = "";
        this.mLocaleStringForCompatibility = sourceLocale == null ? str : sourceLocale.toString();
        this.mLanguageTag = sourceLocale != null ? sourceLocale.toLanguageTag() : str;
        this.mHashCode = hashCodeInternal(this.mSuggestions, this.mLanguageTag, this.mLocaleStringForCompatibility);
        initStyle(context);
    }

    private void initStyle(Context context) {
        if (context == null) {
            this.mMisspelledUnderlineThickness = 0.0f;
            this.mEasyCorrectUnderlineThickness = 0.0f;
            this.mAutoCorrectionUnderlineThickness = 0.0f;
            this.mMisspelledUnderlineColor = -16777216;
            this.mEasyCorrectUnderlineColor = -16777216;
            this.mAutoCorrectionUnderlineColor = -16777216;
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(null, R.styleable.SuggestionSpan, R.attr.textAppearanceMisspelledSuggestion, 0);
        this.mMisspelledUnderlineThickness = typedArray.getDimension(1, 0.0f);
        this.mMisspelledUnderlineColor = typedArray.getColor(0, -16777216);
        TypedArray typedArray2 = context.obtainStyledAttributes(null, R.styleable.SuggestionSpan, R.attr.textAppearanceEasyCorrectSuggestion, 0);
        this.mEasyCorrectUnderlineThickness = typedArray2.getDimension(1, 0.0f);
        this.mEasyCorrectUnderlineColor = typedArray2.getColor(0, -16777216);
        TypedArray typedArray3 = context.obtainStyledAttributes(null, R.styleable.SuggestionSpan, R.attr.textAppearanceAutoCorrectionSuggestion, 0);
        this.mAutoCorrectionUnderlineThickness = typedArray3.getDimension(1, 0.0f);
        this.mAutoCorrectionUnderlineColor = typedArray3.getColor(0, -16777216);
    }

    public SuggestionSpan(Parcel src) {
        this.mSuggestions = src.readStringArray();
        this.mFlags = src.readInt();
        this.mLocaleStringForCompatibility = src.readString();
        this.mLanguageTag = src.readString();
        this.mHashCode = src.readInt();
        this.mEasyCorrectUnderlineColor = src.readInt();
        this.mEasyCorrectUnderlineThickness = src.readFloat();
        this.mMisspelledUnderlineColor = src.readInt();
        this.mMisspelledUnderlineThickness = src.readFloat();
        this.mAutoCorrectionUnderlineColor = src.readInt();
        this.mAutoCorrectionUnderlineThickness = src.readFloat();
    }

    public String[] getSuggestions() {
        return this.mSuggestions;
    }

    @Deprecated
    public String getLocale() {
        return this.mLocaleStringForCompatibility;
    }

    public Locale getLocaleObject() {
        if (this.mLanguageTag.isEmpty()) {
            return null;
        }
        return Locale.forLanguageTag(this.mLanguageTag);
    }

    @UnsupportedAppUsage
    @Deprecated
    public String getNotificationTargetClassName() {
        return null;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public void setFlags(int flags) {
        this.mFlags = flags;
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
        dest.writeStringArray(this.mSuggestions);
        dest.writeInt(this.mFlags);
        dest.writeString(this.mLocaleStringForCompatibility);
        dest.writeString(this.mLanguageTag);
        dest.writeInt(this.mHashCode);
        dest.writeInt(this.mEasyCorrectUnderlineColor);
        dest.writeFloat(this.mEasyCorrectUnderlineThickness);
        dest.writeInt(this.mMisspelledUnderlineColor);
        dest.writeFloat(this.mMisspelledUnderlineThickness);
        dest.writeInt(this.mAutoCorrectionUnderlineColor);
        dest.writeFloat(this.mAutoCorrectionUnderlineThickness);
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeIdInternal() {
        return 19;
    }

    public boolean equals(Object o) {
        if (!(o instanceof SuggestionSpan) || ((SuggestionSpan) o).hashCode() != this.mHashCode) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return this.mHashCode;
    }

    private static int hashCodeInternal(String[] suggestions, String languageTag, String localeStringForCompatibility) {
        return Arrays.hashCode(new Object[]{Long.valueOf(SystemClock.uptimeMillis()), suggestions, languageTag, localeStringForCompatibility});
    }

    @Override // android.text.style.CharacterStyle
    public void updateDrawState(TextPaint tp) {
        boolean autoCorrection = false;
        boolean misspelled = (this.mFlags & 2) != 0;
        boolean easy = (this.mFlags & 1) != 0;
        if ((this.mFlags & 4) != 0) {
            autoCorrection = true;
        }
        if (easy) {
            if (!misspelled) {
                tp.setUnderlineText(this.mEasyCorrectUnderlineColor, this.mEasyCorrectUnderlineThickness);
            } else if (tp.underlineColor == 0) {
                tp.setUnderlineText(this.mMisspelledUnderlineColor, this.mMisspelledUnderlineThickness);
            }
        } else if (autoCorrection) {
            tp.setUnderlineText(this.mAutoCorrectionUnderlineColor, this.mAutoCorrectionUnderlineThickness);
        }
    }

    public int getUnderlineColor() {
        boolean autoCorrection = true;
        boolean misspelled = (this.mFlags & 2) != 0;
        boolean easy = (this.mFlags & 1) != 0;
        if ((this.mFlags & 4) == 0) {
            autoCorrection = false;
        }
        if (easy) {
            if (!misspelled) {
                return this.mEasyCorrectUnderlineColor;
            }
            return this.mMisspelledUnderlineColor;
        } else if (autoCorrection) {
            return this.mAutoCorrectionUnderlineColor;
        } else {
            return 0;
        }
    }

    @UnsupportedAppUsage
    @Deprecated
    public void notifySelection(Context context, String original, int index) {
        Log.w(TAG, "notifySelection() is deprecated.  Does nothing.");
    }
}
