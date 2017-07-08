package android.icu.impl;

import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;

public class ICUResourceTableAccess {
    public static String getTableString(String path, ULocale locale, String tableName, String itemName) {
        return getTableString((ICUResourceBundle) UResourceBundle.getBundleInstance(path, locale.getBaseName()), tableName, null, itemName);
    }

    public static String getTableString(ICUResourceBundle bundle, String tableName, String subtableName, String item) {
        String str = null;
        while (true) {
            try {
                ICUResourceBundle table = bundle.findWithFallback(tableName);
                if (table == null) {
                    return item;
                }
                ICUResourceBundle stable = table;
                if (subtableName != null) {
                    stable = table.findWithFallback(subtableName);
                }
                if (stable != null) {
                    str = stable.findStringWithFallback(item);
                    if (str != null) {
                        break;
                    }
                }
                if (subtableName == null) {
                    String currentName = null;
                    if (tableName.equals("Countries")) {
                        currentName = LocaleIDs.getCurrentCountryID(item);
                    } else if (tableName.equals("Languages")) {
                        currentName = LocaleIDs.getCurrentLanguageID(item);
                    }
                    if (currentName != null) {
                        str = table.findStringWithFallback(currentName);
                        if (str != null) {
                            break;
                        }
                    }
                }
                String fallbackLocale = table.findStringWithFallback("Fallback");
                if (fallbackLocale == null) {
                    return item;
                }
                if (fallbackLocale.length() == 0) {
                    fallbackLocale = "root";
                }
                if (fallbackLocale.equals(table.getULocale().getName())) {
                    return item;
                }
                bundle = (ICUResourceBundle) UResourceBundle.getBundleInstance(bundle.getBaseName(), fallbackLocale);
            } catch (Exception e) {
            }
        }
        if (str == null || str.length() <= 0) {
            str = item;
        }
        return str;
    }
}
