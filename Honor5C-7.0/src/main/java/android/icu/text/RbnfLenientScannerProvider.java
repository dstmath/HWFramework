package android.icu.text;

import android.icu.util.ULocale;

@Deprecated
public interface RbnfLenientScannerProvider {
    @Deprecated
    RbnfLenientScanner get(ULocale uLocale, String str);
}
