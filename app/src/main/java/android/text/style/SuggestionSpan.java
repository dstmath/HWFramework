package android.text.style;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.os.SystemClock;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.R;
import java.util.Arrays;
import java.util.Locale;

public class SuggestionSpan extends CharacterStyle implements ParcelableSpan {
    public static final String ACTION_SUGGESTION_PICKED = "android.text.style.SUGGESTION_PICKED";
    public static final Creator<SuggestionSpan> CREATOR = null;
    public static final int FLAG_AUTO_CORRECTION = 4;
    public static final int FLAG_EASY_CORRECT = 1;
    public static final int FLAG_MISSPELLED = 2;
    public static final int SUGGESTIONS_MAX_SIZE = 5;
    public static final String SUGGESTION_SPAN_PICKED_AFTER = "after";
    public static final String SUGGESTION_SPAN_PICKED_BEFORE = "before";
    public static final String SUGGESTION_SPAN_PICKED_HASHCODE = "hashcode";
    private static final String TAG = "SuggestionSpan";
    private int mAutoCorrectionUnderlineColor;
    private float mAutoCorrectionUnderlineThickness;
    private int mEasyCorrectUnderlineColor;
    private float mEasyCorrectUnderlineThickness;
    private int mFlags;
    private final int mHashCode;
    private final String mLanguageTag;
    private final String mLocaleStringForCompatibility;
    private int mMisspelledUnderlineColor;
    private float mMisspelledUnderlineThickness;
    private final String mNotificationTargetClassName;
    private final String mNotificationTargetPackageName;
    private final String[] mSuggestions;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.text.style.SuggestionSpan.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.text.style.SuggestionSpan.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.style.SuggestionSpan.<clinit>():void");
    }

    public SuggestionSpan(Context context, String[] suggestions, int flags) {
        this(context, null, suggestions, flags, null);
    }

    public SuggestionSpan(Locale locale, String[] suggestions, int flags) {
        this(null, locale, suggestions, flags, null);
    }

    public SuggestionSpan(Context context, Locale locale, String[] suggestions, int flags, Class<?> notificationTargetClass) {
        Locale locale2;
        this.mSuggestions = (String[]) Arrays.copyOf(suggestions, Math.min(SUGGESTIONS_MAX_SIZE, suggestions.length));
        this.mFlags = flags;
        if (locale != null) {
            locale2 = locale;
        } else if (context != null) {
            locale2 = context.getResources().getConfiguration().locale;
        } else {
            Log.e(TAG, "No locale or context specified in SuggestionSpan constructor");
            locale2 = null;
        }
        this.mLocaleStringForCompatibility = locale2 == null ? "" : locale2.toString();
        this.mLanguageTag = locale2 == null ? "" : locale2.toLanguageTag();
        if (context != null) {
            this.mNotificationTargetPackageName = context.getPackageName();
        } else {
            this.mNotificationTargetPackageName = null;
        }
        if (notificationTargetClass != null) {
            this.mNotificationTargetClassName = notificationTargetClass.getCanonicalName();
        } else {
            this.mNotificationTargetClassName = "";
        }
        this.mHashCode = hashCodeInternal(this.mSuggestions, this.mLanguageTag, this.mLocaleStringForCompatibility, this.mNotificationTargetClassName);
        initStyle(context);
    }

    private void initStyle(Context context) {
        if (context == null) {
            this.mMisspelledUnderlineThickness = 0.0f;
            this.mEasyCorrectUnderlineThickness = 0.0f;
            this.mAutoCorrectionUnderlineThickness = 0.0f;
            this.mMisspelledUnderlineColor = View.MEASURED_STATE_MASK;
            this.mEasyCorrectUnderlineColor = View.MEASURED_STATE_MASK;
            this.mAutoCorrectionUnderlineColor = View.MEASURED_STATE_MASK;
            return;
        }
        TypedArray typedArray = context.obtainStyledAttributes(null, R.styleable.SuggestionSpan, R.attr.textAppearanceMisspelledSuggestion, 0);
        this.mMisspelledUnderlineThickness = typedArray.getDimension(FLAG_EASY_CORRECT, 0.0f);
        this.mMisspelledUnderlineColor = typedArray.getColor(0, View.MEASURED_STATE_MASK);
        typedArray = context.obtainStyledAttributes(null, R.styleable.SuggestionSpan, R.attr.textAppearanceEasyCorrectSuggestion, 0);
        this.mEasyCorrectUnderlineThickness = typedArray.getDimension(FLAG_EASY_CORRECT, 0.0f);
        this.mEasyCorrectUnderlineColor = typedArray.getColor(0, View.MEASURED_STATE_MASK);
        typedArray = context.obtainStyledAttributes(null, R.styleable.SuggestionSpan, R.attr.textAppearanceAutoCorrectionSuggestion, 0);
        this.mAutoCorrectionUnderlineThickness = typedArray.getDimension(FLAG_EASY_CORRECT, 0.0f);
        this.mAutoCorrectionUnderlineColor = typedArray.getColor(0, View.MEASURED_STATE_MASK);
    }

    public SuggestionSpan(Parcel src) {
        this.mSuggestions = src.readStringArray();
        this.mFlags = src.readInt();
        this.mLocaleStringForCompatibility = src.readString();
        this.mLanguageTag = src.readString();
        this.mNotificationTargetClassName = src.readString();
        this.mNotificationTargetPackageName = src.readString();
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
        return this.mLanguageTag.isEmpty() ? null : Locale.forLanguageTag(this.mLanguageTag);
    }

    public String getNotificationTargetClassName() {
        return this.mNotificationTargetClassName;
    }

    public int getFlags() {
        return this.mFlags;
    }

    public void setFlags(int flags) {
        this.mFlags = flags;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeStringArray(this.mSuggestions);
        dest.writeInt(this.mFlags);
        dest.writeString(this.mLocaleStringForCompatibility);
        dest.writeString(this.mLanguageTag);
        dest.writeString(this.mNotificationTargetClassName);
        dest.writeString(this.mNotificationTargetPackageName);
        dest.writeInt(this.mHashCode);
        dest.writeInt(this.mEasyCorrectUnderlineColor);
        dest.writeFloat(this.mEasyCorrectUnderlineThickness);
        dest.writeInt(this.mMisspelledUnderlineColor);
        dest.writeFloat(this.mMisspelledUnderlineThickness);
        dest.writeInt(this.mAutoCorrectionUnderlineColor);
        dest.writeFloat(this.mAutoCorrectionUnderlineThickness);
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 19;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof SuggestionSpan)) {
            return false;
        }
        if (((SuggestionSpan) o).hashCode() == this.mHashCode) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        return this.mHashCode;
    }

    private static int hashCodeInternal(String[] suggestions, String languageTag, String localeStringForCompatibility, String notificationTargetClassName) {
        Object[] objArr = new Object[SUGGESTIONS_MAX_SIZE];
        objArr[0] = Long.valueOf(SystemClock.uptimeMillis());
        objArr[FLAG_EASY_CORRECT] = suggestions;
        objArr[FLAG_MISSPELLED] = languageTag;
        objArr[3] = localeStringForCompatibility;
        objArr[FLAG_AUTO_CORRECTION] = notificationTargetClassName;
        return Arrays.hashCode(objArr);
    }

    public void updateDrawState(TextPaint tp) {
        boolean misspelled = (this.mFlags & FLAG_MISSPELLED) != 0;
        boolean easy = (this.mFlags & FLAG_EASY_CORRECT) != 0;
        boolean autoCorrection = (this.mFlags & FLAG_AUTO_CORRECTION) != 0;
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
        boolean misspelled = (this.mFlags & FLAG_MISSPELLED) != 0;
        boolean easy = (this.mFlags & FLAG_EASY_CORRECT) != 0;
        boolean autoCorrection = (this.mFlags & FLAG_AUTO_CORRECTION) != 0;
        if (easy) {
            if (misspelled) {
                return this.mMisspelledUnderlineColor;
            }
            return this.mEasyCorrectUnderlineColor;
        } else if (autoCorrection) {
            return this.mAutoCorrectionUnderlineColor;
        } else {
            return 0;
        }
    }

    public void notifySelection(Context context, String original, int index) {
        Intent intent = new Intent();
        if (context != null && this.mNotificationTargetClassName != null) {
            if (this.mSuggestions == null || index < 0 || index >= this.mSuggestions.length) {
                Log.w(TAG, "Unable to notify the suggestion as the index is out of range index=" + index + " length=" + this.mSuggestions.length);
                return;
            }
            if (this.mNotificationTargetPackageName != null) {
                intent.setClassName(this.mNotificationTargetPackageName, this.mNotificationTargetClassName);
                intent.setAction(ACTION_SUGGESTION_PICKED);
                intent.putExtra(SUGGESTION_SPAN_PICKED_BEFORE, original);
                intent.putExtra(SUGGESTION_SPAN_PICKED_AFTER, this.mSuggestions[index]);
                intent.putExtra(SUGGESTION_SPAN_PICKED_HASHCODE, hashCode());
                context.sendBroadcast(intent);
            } else {
                InputMethodManager imm = InputMethodManager.peekInstance();
                if (imm != null) {
                    imm.notifySuggestionPicked(this, original, index);
                }
            }
        }
    }
}
