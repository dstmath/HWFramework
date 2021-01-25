package ohos.global.configuration;

import java.util.ArrayList;
import java.util.Locale;

public class LocaleProfile {
    private final Locale[] mLocales;

    public LocaleProfile(Locale[] localeArr) {
        this.mLocales = cloneNonNullLocales(localeArr);
    }

    public Locale[] getLocales() {
        return cloneNonNullLocales(this.mLocales);
    }

    static Locale[] cloneNonNullLocales(Locale[] localeArr) {
        if (localeArr == null) {
            return new Locale[0];
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < localeArr.length; i++) {
            if (localeArr[i] != null) {
                Object clone = localeArr[i].clone();
                Locale locale = clone instanceof Locale ? (Locale) clone : null;
                if (locale != null) {
                    arrayList.add(locale);
                }
            }
        }
        return (Locale[]) arrayList.toArray(new Locale[arrayList.size()]);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof LocaleProfile)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        Locale[] localeArr = ((LocaleProfile) obj).mLocales;
        if (localeArr.length != this.mLocales.length) {
            return false;
        }
        int i = 0;
        while (true) {
            Locale[] localeArr2 = this.mLocales;
            if (i >= localeArr2.length) {
                return true;
            }
            if (!localeArr2[i].equals(localeArr[i])) {
                return false;
            }
            i++;
        }
    }

    public int hashCode() {
        int i = 1;
        int i2 = 0;
        while (true) {
            Locale[] localeArr = this.mLocales;
            if (i2 >= localeArr.length) {
                return i;
            }
            i = (i * 31) + localeArr[i2].hashCode();
            i2++;
        }
    }

    public String toString() {
        String str = "[";
        for (int i = 0; i < this.mLocales.length; i++) {
            str = str + this.mLocales[i];
            if (i < this.mLocales.length - 1) {
                str = str + ",";
            }
        }
        return str + "]";
    }
}
