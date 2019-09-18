package android.text.style;

import android.graphics.LeakyTypefaceStorage;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;

public class TypefaceSpan extends MetricAffectingSpan implements ParcelableSpan {
    private final String mFamily;
    private final Typeface mTypeface;

    public TypefaceSpan(String family) {
        this(family, null);
    }

    public TypefaceSpan(Typeface typeface) {
        this(null, typeface);
    }

    public TypefaceSpan(Parcel src) {
        this.mFamily = src.readString();
        this.mTypeface = LeakyTypefaceStorage.readTypefaceFromParcel(src);
    }

    private TypefaceSpan(String family, Typeface typeface) {
        this.mFamily = family;
        this.mTypeface = typeface;
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 13;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        dest.writeString(this.mFamily);
        LeakyTypefaceStorage.writeTypefaceToParcel(this.mTypeface, dest);
    }

    public String getFamily() {
        return this.mFamily;
    }

    public Typeface getTypeface() {
        return this.mTypeface;
    }

    public void updateDrawState(TextPaint ds) {
        updateTypeface(ds);
    }

    public void updateMeasureState(TextPaint paint) {
        updateTypeface(paint);
    }

    private void updateTypeface(Paint paint) {
        if (this.mTypeface != null) {
            paint.setTypeface(this.mTypeface);
        } else if (this.mFamily != null) {
            applyFontFamily(paint, this.mFamily);
        }
    }

    private void applyFontFamily(Paint paint, String family) {
        int style;
        Typeface old = paint.getTypeface();
        if (old == null) {
            style = 0;
        } else {
            style = old.getStyle();
        }
        Typeface styledTypeface = Typeface.create(family, style);
        int fake = (~styledTypeface.getStyle()) & style;
        if ((fake & 1) != 0) {
            paint.setFakeBoldText(true);
        }
        if ((fake & 2) != 0) {
            paint.setTextSkewX(-0.25f);
        }
        paint.setTypeface(styledTypeface);
    }
}
