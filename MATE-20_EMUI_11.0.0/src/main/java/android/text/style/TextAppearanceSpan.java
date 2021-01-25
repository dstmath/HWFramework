package android.text.style;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.LeakyTypefaceStorage;
import android.graphics.Typeface;
import android.os.LocaleList;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import com.android.internal.R;

public class TextAppearanceSpan extends MetricAffectingSpan implements ParcelableSpan {
    private final boolean mElegantTextHeight;
    private final String mFamilyName;
    private final String mFontFeatureSettings;
    private final String mFontVariationSettings;
    private final boolean mHasElegantTextHeight;
    private final boolean mHasLetterSpacing;
    private final float mLetterSpacing;
    private final int mShadowColor;
    private final float mShadowDx;
    private final float mShadowDy;
    private final float mShadowRadius;
    private final int mStyle;
    private final ColorStateList mTextColor;
    private final ColorStateList mTextColorLink;
    private final int mTextFontWeight;
    private final LocaleList mTextLocales;
    private final int mTextSize;
    private final Typeface mTypeface;

    public TextAppearanceSpan(Context context, int appearance) {
        this(context, appearance, -1);
    }

    public TextAppearanceSpan(Context context, int appearance, int colorList) {
        TypedArray a = context.obtainStyledAttributes(appearance, R.styleable.TextAppearance);
        ColorStateList textColor = a.getColorStateList(3);
        this.mTextColorLink = a.getColorStateList(6);
        this.mTextSize = a.getDimensionPixelSize(0, -1);
        this.mStyle = a.getInt(2, 0);
        if (context.isRestricted() || !context.canLoadUnsafeResources()) {
            this.mTypeface = null;
        } else {
            this.mTypeface = a.getFont(12);
        }
        if (this.mTypeface != null) {
            this.mFamilyName = null;
        } else {
            String family = a.getString(12);
            if (family != null) {
                this.mFamilyName = family;
            } else {
                int tf = a.getInt(1, 0);
                if (tf == 1) {
                    this.mFamilyName = "sans";
                } else if (tf == 2) {
                    this.mFamilyName = "serif";
                } else if (tf != 3) {
                    this.mFamilyName = null;
                } else {
                    this.mFamilyName = "monospace";
                }
            }
        }
        this.mTextFontWeight = a.getInt(18, -1);
        String localeString = a.getString(19);
        if (localeString != null) {
            LocaleList localeList = LocaleList.forLanguageTags(localeString);
            if (!localeList.isEmpty()) {
                this.mTextLocales = localeList;
            } else {
                this.mTextLocales = null;
            }
        } else {
            this.mTextLocales = null;
        }
        this.mShadowRadius = a.getFloat(10, 0.0f);
        this.mShadowDx = a.getFloat(8, 0.0f);
        this.mShadowDy = a.getFloat(9, 0.0f);
        this.mShadowColor = a.getInt(7, 0);
        this.mHasElegantTextHeight = a.hasValue(13);
        this.mElegantTextHeight = a.getBoolean(13, false);
        this.mHasLetterSpacing = a.hasValue(14);
        this.mLetterSpacing = a.getFloat(14, 0.0f);
        this.mFontFeatureSettings = a.getString(15);
        this.mFontVariationSettings = a.getString(16);
        a.recycle();
        if (colorList >= 0) {
            TypedArray a2 = context.obtainStyledAttributes(16973829, R.styleable.Theme);
            textColor = a2.getColorStateList(colorList);
            a2.recycle();
        }
        this.mTextColor = textColor;
    }

    public TextAppearanceSpan(String family, int style, int size, ColorStateList color, ColorStateList linkColor) {
        this.mFamilyName = family;
        this.mStyle = style;
        this.mTextSize = size;
        this.mTextColor = color;
        this.mTextColorLink = linkColor;
        this.mTypeface = null;
        this.mTextFontWeight = -1;
        this.mTextLocales = null;
        this.mShadowRadius = 0.0f;
        this.mShadowDx = 0.0f;
        this.mShadowDy = 0.0f;
        this.mShadowColor = 0;
        this.mHasElegantTextHeight = false;
        this.mElegantTextHeight = false;
        this.mHasLetterSpacing = false;
        this.mLetterSpacing = 0.0f;
        this.mFontFeatureSettings = null;
        this.mFontVariationSettings = null;
    }

    public TextAppearanceSpan(Parcel src) {
        this.mFamilyName = src.readString();
        this.mStyle = src.readInt();
        this.mTextSize = src.readInt();
        if (src.readInt() != 0) {
            this.mTextColor = ColorStateList.CREATOR.createFromParcel(src);
        } else {
            this.mTextColor = null;
        }
        if (src.readInt() != 0) {
            this.mTextColorLink = ColorStateList.CREATOR.createFromParcel(src);
        } else {
            this.mTextColorLink = null;
        }
        this.mTypeface = LeakyTypefaceStorage.readTypefaceFromParcel(src);
        this.mTextFontWeight = src.readInt();
        this.mTextLocales = (LocaleList) src.readParcelable(LocaleList.class.getClassLoader());
        this.mShadowRadius = src.readFloat();
        this.mShadowDx = src.readFloat();
        this.mShadowDy = src.readFloat();
        this.mShadowColor = src.readInt();
        this.mHasElegantTextHeight = src.readBoolean();
        this.mElegantTextHeight = src.readBoolean();
        this.mHasLetterSpacing = src.readBoolean();
        this.mLetterSpacing = src.readFloat();
        this.mFontFeatureSettings = src.readString();
        this.mFontVariationSettings = src.readString();
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeIdInternal() {
        return 17;
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
        dest.writeString(this.mFamilyName);
        dest.writeInt(this.mStyle);
        dest.writeInt(this.mTextSize);
        if (this.mTextColor != null) {
            dest.writeInt(1);
            this.mTextColor.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        if (this.mTextColorLink != null) {
            dest.writeInt(1);
            this.mTextColorLink.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        LeakyTypefaceStorage.writeTypefaceToParcel(this.mTypeface, dest);
        dest.writeInt(this.mTextFontWeight);
        dest.writeParcelable(this.mTextLocales, flags);
        dest.writeFloat(this.mShadowRadius);
        dest.writeFloat(this.mShadowDx);
        dest.writeFloat(this.mShadowDy);
        dest.writeInt(this.mShadowColor);
        dest.writeBoolean(this.mHasElegantTextHeight);
        dest.writeBoolean(this.mElegantTextHeight);
        dest.writeBoolean(this.mHasLetterSpacing);
        dest.writeFloat(this.mLetterSpacing);
        dest.writeString(this.mFontFeatureSettings);
        dest.writeString(this.mFontVariationSettings);
    }

    public String getFamily() {
        return this.mFamilyName;
    }

    public ColorStateList getTextColor() {
        return this.mTextColor;
    }

    public ColorStateList getLinkTextColor() {
        return this.mTextColorLink;
    }

    public int getTextSize() {
        return this.mTextSize;
    }

    public int getTextStyle() {
        return this.mStyle;
    }

    public int getTextFontWeight() {
        return this.mTextFontWeight;
    }

    public LocaleList getTextLocales() {
        return this.mTextLocales;
    }

    public Typeface getTypeface() {
        return this.mTypeface;
    }

    public int getShadowColor() {
        return this.mShadowColor;
    }

    public float getShadowDx() {
        return this.mShadowDx;
    }

    public float getShadowDy() {
        return this.mShadowDy;
    }

    public float getShadowRadius() {
        return this.mShadowRadius;
    }

    public String getFontFeatureSettings() {
        return this.mFontFeatureSettings;
    }

    public String getFontVariationSettings() {
        return this.mFontVariationSettings;
    }

    public boolean isElegantTextHeight() {
        return this.mElegantTextHeight;
    }

    @Override // android.text.style.CharacterStyle
    public void updateDrawState(TextPaint ds) {
        updateMeasureState(ds);
        ColorStateList colorStateList = this.mTextColor;
        if (colorStateList != null) {
            ds.setColor(colorStateList.getColorForState(ds.drawableState, 0));
        }
        ColorStateList colorStateList2 = this.mTextColorLink;
        if (colorStateList2 != null) {
            ds.linkColor = colorStateList2.getColorForState(ds.drawableState, 0);
        }
        int i = this.mShadowColor;
        if (i != 0) {
            ds.setShadowLayer(this.mShadowRadius, this.mShadowDx, this.mShadowDy, i);
        }
    }

    @Override // android.text.style.MetricAffectingSpan
    public void updateMeasureState(TextPaint ds) {
        Typeface styledTypeface;
        Typeface readyTypeface;
        int style = 0;
        Typeface typeface = this.mTypeface;
        if (typeface != null) {
            style = this.mStyle;
            styledTypeface = Typeface.create(typeface, style);
        } else if (this.mFamilyName == null && this.mStyle == 0) {
            styledTypeface = null;
        } else {
            Typeface tf = ds.getTypeface();
            if (tf != null) {
                style = tf.getStyle();
            }
            style |= this.mStyle;
            String str = this.mFamilyName;
            if (str != null) {
                styledTypeface = Typeface.create(str, style);
            } else if (tf == null) {
                styledTypeface = Typeface.defaultFromStyle(style);
            } else {
                styledTypeface = Typeface.create(tf, style);
            }
        }
        if (styledTypeface != null) {
            int i = this.mTextFontWeight;
            if (i >= 0) {
                readyTypeface = ds.setTypeface(Typeface.create(styledTypeface, Math.min(1000, i), (style & 2) != 0));
            } else {
                readyTypeface = styledTypeface;
            }
            int fake = (~readyTypeface.getStyle()) & style;
            if ((fake & 1) != 0) {
                ds.setFakeBoldText(true);
            }
            if ((fake & 2) != 0) {
                ds.setTextSkewX(-0.25f);
            }
            ds.setTypeface(readyTypeface);
        }
        int i2 = this.mTextSize;
        if (i2 > 0) {
            ds.setTextSize((float) i2);
        }
        LocaleList localeList = this.mTextLocales;
        if (localeList != null) {
            ds.setTextLocales(localeList);
        }
        if (this.mHasElegantTextHeight) {
            ds.setElegantTextHeight(this.mElegantTextHeight);
        }
        if (this.mHasLetterSpacing) {
            ds.setLetterSpacing(this.mLetterSpacing);
        }
        String str2 = this.mFontFeatureSettings;
        if (str2 != null) {
            ds.setFontFeatureSettings(str2);
        }
        String str3 = this.mFontVariationSettings;
        if (str3 != null) {
            ds.setFontVariationSettings(str3);
        }
    }
}
