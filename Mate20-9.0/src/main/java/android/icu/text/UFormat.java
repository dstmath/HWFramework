package android.icu.text;

import android.icu.util.ULocale;
import java.text.Format;

public abstract class UFormat extends Format {
    private static final long serialVersionUID = -4964390515840164416L;
    private ULocale actualLocale;
    private ULocale validLocale;

    public final ULocale getLocale(ULocale.Type type) {
        return type == ULocale.ACTUAL_LOCALE ? this.actualLocale : this.validLocale;
    }

    /* access modifiers changed from: package-private */
    public final void setLocale(ULocale valid, ULocale actual) {
        boolean z = false;
        boolean z2 = valid == null;
        if (actual == null) {
            z = true;
        }
        if (z2 == z) {
            this.validLocale = valid;
            this.actualLocale = actual;
            return;
        }
        throw new IllegalArgumentException();
    }
}
