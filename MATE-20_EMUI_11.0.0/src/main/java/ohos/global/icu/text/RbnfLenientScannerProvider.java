package ohos.global.icu.text;

import ohos.global.icu.util.ULocale;

@Deprecated
public interface RbnfLenientScannerProvider {
    @Deprecated
    RbnfLenientScanner get(ULocale uLocale, String str);
}
