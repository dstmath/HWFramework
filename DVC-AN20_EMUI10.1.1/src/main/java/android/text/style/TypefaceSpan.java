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

    @Override // android.text.ParcelableSpan
    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    @Override // android.text.ParcelableSpan
    public int getSpanTypeIdInternal() {
        return 13;
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
        dest.writeString(this.mFamily);
        LeakyTypefaceStorage.writeTypefaceToParcel(this.mTypeface, dest);
    }

    public String getFamily() {
        return this.mFamily;
    }

    public Typeface getTypeface() {
        return this.mTypeface;
    }

    @Override // android.text.style.CharacterStyle
    public void updateDrawState(TextPaint ds) {
        updateTypeface(ds);
    }

    @Override // android.text.style.MetricAffectingSpan
    public void updateMeasureState(TextPaint paint) {
        updateTypeface(paint);
    }

    private void updateTypeface(Paint paint) {
        Typeface typeface = this.mTypeface;
        if (typeface != null) {
            paint.setTypeface(typeface);
            return;
        }
        String str = this.mFamily;
        if (str != null) {
            applyFontFamily(paint, str);
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
