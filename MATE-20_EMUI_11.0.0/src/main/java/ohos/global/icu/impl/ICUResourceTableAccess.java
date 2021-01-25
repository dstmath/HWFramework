package ohos.global.icu.impl;

import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class ICUResourceTableAccess {
    public static String getTableString(String str, ULocale uLocale, String str2, String str3, String str4) {
        return getTableString(UResourceBundle.getBundleInstance(str, uLocale.getBaseName()), str2, (String) null, str3, str4);
    }

    public static String getTableString(ICUResourceBundle iCUResourceBundle, String str, String str2, String str3, String str4) {
        String str5;
        String str6 = null;
        while (true) {
            try {
                ICUResourceBundle findWithFallback = iCUResourceBundle.findWithFallback(str);
                if (findWithFallback != null) {
                    ICUResourceBundle findWithFallback2 = str2 != null ? findWithFallback.findWithFallback(str2) : findWithFallback;
                    if (findWithFallback2 != null && (str6 = findWithFallback2.findStringWithFallback(str3)) != null) {
                        break;
                    }
                    if (str2 == null) {
                        if (str.equals("Countries")) {
                            str5 = LocaleIDs.getCurrentCountryID(str3);
                        } else {
                            str5 = str.equals("Languages") ? LocaleIDs.getCurrentLanguageID(str3) : null;
                        }
                        if (!(str5 == null || (str6 = findWithFallback.findStringWithFallback(str5)) == null)) {
                            break;
                        }
                    }
                    String findStringWithFallback = findWithFallback.findStringWithFallback("Fallback");
                    if (findStringWithFallback == null) {
                        return str4;
                    }
                    if (findStringWithFallback.length() == 0) {
                        findStringWithFallback = Constants.ELEMNAME_ROOT_STRING;
                    }
                    if (findStringWithFallback.equals(findWithFallback.getULocale().getName())) {
                        return str4;
                    }
                    iCUResourceBundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(iCUResourceBundle.getBaseName(), findStringWithFallback);
                } else {
                    return str4;
                }
            } catch (Exception unused) {
            }
        }
        return (str6 == null || str6.length() <= 0) ? str4 : str6;
    }
}
