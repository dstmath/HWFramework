package android.text.style;

import android.graphics.Paint;
import android.os.LocaleList;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import com.android.internal.util.Preconditions;
import java.util.Locale;

public class LocaleSpan extends MetricAffectingSpan implements ParcelableSpan {
    private final LocaleList mLocales;

    public LocaleSpan(Locale locale) {
        LocaleList emptyLocaleList;
        if (locale == null) {
            emptyLocaleList = LocaleList.getEmptyLocaleList();
        } else {
            emptyLocaleList = new LocaleList(new Locale[]{locale});
        }
        this.mLocales = emptyLocaleList;
    }

    public LocaleSpan(LocaleList locales) {
        Preconditions.checkNotNull(locales, "locales cannot be null");
        this.mLocales = locales;
    }

    public LocaleSpan(Parcel source) {
        this.mLocales = (LocaleList) LocaleList.CREATOR.createFromParcel(source);
    }

    public int getSpanTypeId() {
        return getSpanTypeIdInternal();
    }

    public int getSpanTypeIdInternal() {
        return 23;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        writeToParcelInternal(dest, flags);
    }

    public void writeToParcelInternal(Parcel dest, int flags) {
        this.mLocales.writeToParcel(dest, flags);
    }

    public Locale getLocale() {
        return this.mLocales.get(0);
    }

    public LocaleList getLocales() {
        return this.mLocales;
    }

    public void updateDrawState(TextPaint ds) {
        apply(ds, this.mLocales);
    }

    public void updateMeasureState(TextPaint paint) {
        apply(paint, this.mLocales);
    }

    private static void apply(Paint paint, LocaleList locales) {
        paint.setTextLocales(locales);
    }
}
