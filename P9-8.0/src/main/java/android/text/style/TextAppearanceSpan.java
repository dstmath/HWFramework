package android.text.style;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import com.android.internal.R;

public class TextAppearanceSpan extends MetricAffectingSpan implements ParcelableSpan {
    private final int mStyle;
    private final ColorStateList mTextColor;
    private final ColorStateList mTextColorLink;
    private final int mTextSize;
    private final String mTypeface;

    public TextAppearanceSpan(Context context, int appearance) {
        this(context, appearance, -1);
    }

    public TextAppearanceSpan(Context context, int appearance, int colorList) {
        TypedArray a = context.obtainStyledAttributes(appearance, R.styleable.TextAppearance);
        ColorStateList textColor = a.getColorStateList(3);
        this.mTextColorLink = a.getColorStateList(6);
        this.mTextSize = a.getDimensionPixelSize(0, -1);
        this.mStyle = a.getInt(2, 0);
        String family = a.getString(12);
        if (family == null) {
            switch (a.getInt(1, 0)) {
                case 1:
                    this.mTypeface = "sans";
                    break;
                case 2:
                    this.mTypeface = "serif";
                    break;
                case 3:
                    this.mTypeface = "monospace";
                    break;
                default:
                    this.mTypeface = null;
                    break;
            }
        }
        this.mTypeface = family;
        a.recycle();
        if (colorList >= 0) {
            a = context.obtainStyledAttributes(R.style.Theme, R.styleable.Theme);
            textColor = a.getColorStateList(colorList);
            a.recycle();
        }
        this.mTextColor = textColor;
    }

    public TextAppearanceSpan(String family, int style, int size, ColorStateList color, ColorStateList linkColor) {
        this.mTypeface = family;
        this.mStyle = style;
        this.mTextSize = size;
        this.mTextColor = color;
        this.mTextColorLink = linkColor;
    }

    public TextAppearanceSpan(Parcel src) {
        this.mTypeface = src.readString();
        this.mStyle = src.readInt();
        this.mTextSize = src.readInt();
        if (src.readInt() != 0) {
            this.mTextColor = (ColorStateList) ColorStateList.CREATOR.createFromParcel(src);
        } else {
            this.mTextColor = null;
        }
        if (src.readInt() != 0) {
            this.mTextColorLink = (ColorStateList) ColorStateList.CREATOR.createFromParcel(src);
        } else {
            this.mTextColorLink = null;
        }
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 17;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeString(this.mTypeface);
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
            return;
        }
        dest.writeInt(0);
    }

    public String getFamily() {
        return this.mTypeface;
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

    public void updateDrawState(TextPaint ds) {
        updateMeasureState(ds);
        if (this.mTextColor != null) {
            ds.setColor(this.mTextColor.getColorForState(ds.drawableState, 0));
        }
        if (this.mTextColorLink != null) {
            ds.linkColor = this.mTextColorLink.getColorForState(ds.drawableState, 0);
        }
    }

    public void updateMeasureState(TextPaint ds) {
        if (!(this.mTypeface == null && this.mStyle == 0)) {
            Typeface tf = ds.getTypeface();
            int style = 0;
            if (tf != null) {
                style = tf.getStyle();
            }
            style |= this.mStyle;
            if (this.mTypeface != null) {
                tf = Typeface.create(this.mTypeface, style);
            } else if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }
            int fake = style & (~tf.getStyle());
            if ((fake & 1) != 0) {
                ds.setFakeBoldText(true);
            }
            if ((fake & 2) != 0) {
                ds.setTextSkewX(-0.25f);
            }
            ds.setTypeface(tf);
        }
        if (this.mTextSize > 0) {
            ds.setTextSize((float) this.mTextSize);
        }
    }
}
