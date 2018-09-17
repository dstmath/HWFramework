package android.icu.impl;

import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.util.ArrayList;
import java.util.MissingResourceException;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

public class CalendarData {
    private ICUResourceBundle fBundle;
    private String fFallbackType;
    private String fMainType;

    public CalendarData(ULocale loc, String type) {
        this((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, loc), type);
    }

    public CalendarData(ICUResourceBundle b, String type) {
        this.fBundle = b;
        if (type == null || type.equals(XmlPullParser.NO_NAMESPACE) || type.equals("gregorian")) {
            this.fMainType = "gregorian";
            this.fFallbackType = null;
            return;
        }
        this.fMainType = type;
        this.fFallbackType = "gregorian";
    }

    public ICUResourceBundle get(String key) {
        try {
            return this.fBundle.getWithFallback("calendar/" + this.fMainType + "/" + key);
        } catch (MissingResourceException m) {
            if (this.fFallbackType != null) {
                return this.fBundle.getWithFallback("calendar/" + this.fFallbackType + "/" + key);
            }
            throw m;
        }
    }

    public ICUResourceBundle get(String key, String subKey) {
        try {
            return this.fBundle.getWithFallback("calendar/" + this.fMainType + "/" + key + "/format/" + subKey);
        } catch (MissingResourceException m) {
            if (this.fFallbackType != null) {
                return this.fBundle.getWithFallback("calendar/" + this.fFallbackType + "/" + key + "/format/" + subKey);
            }
            throw m;
        }
    }

    public ICUResourceBundle get(String key, String contextKey, String subKey) {
        try {
            return this.fBundle.getWithFallback("calendar/" + this.fMainType + "/" + key + "/" + contextKey + "/" + subKey);
        } catch (MissingResourceException m) {
            if (this.fFallbackType != null) {
                return this.fBundle.getWithFallback("calendar/" + this.fFallbackType + "/" + key + "/" + contextKey + "/" + subKey);
            }
            throw m;
        }
    }

    public ICUResourceBundle get(String key, String set, String contextKey, String subKey) {
        try {
            return this.fBundle.getWithFallback("calendar/" + this.fMainType + "/" + key + "/" + set + "/" + contextKey + "/" + subKey);
        } catch (MissingResourceException m) {
            if (this.fFallbackType != null) {
                return this.fBundle.getWithFallback("calendar/" + this.fFallbackType + "/" + key + "/" + set + "/" + contextKey + "/" + subKey);
            }
            throw m;
        }
    }

    public String[] getStringArray(String key) {
        return get(key).getStringArray();
    }

    public String[] getStringArray(String key, String subKey) {
        return get(key, subKey).getStringArray();
    }

    public String[] getStringArray(String key, String contextKey, String subKey) {
        return get(key, contextKey, subKey).getStringArray();
    }

    public String[] getEras(String subkey) {
        return get("eras/" + subkey).getStringArray();
    }

    public String[] getDateTimePatterns() {
        ICUResourceBundle bundle = get("DateTimePatterns");
        ArrayList<String> list = new ArrayList();
        UResourceBundleIterator iter = bundle.getIterator();
        while (iter.hasNext()) {
            UResourceBundle patResource = iter.next();
            switch (patResource.getType()) {
                case XmlPullParser.START_DOCUMENT /*0*/:
                    list.add(patResource.getString());
                    break;
                case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                    list.add(patResource.getStringArray()[0]);
                    break;
                default:
                    break;
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public String getDateTimePattern() {
        return _getDateTimePattern(-1);
    }

    public String getDateTimePattern(int style) {
        return _getDateTimePattern(style & 7);
    }

    private String _getDateTimePattern(int offset) {
        String[] strArr = null;
        try {
            strArr = getDateTimePatterns();
        } catch (MissingResourceException e) {
        }
        if (strArr == null || strArr.length < 9) {
            return "{1} {0}";
        }
        if (strArr.length < 13) {
            return strArr[8];
        }
        return strArr[offset + 9];
    }

    public String[] getOverrides() {
        ICUResourceBundle bundle = get("DateTimePatterns");
        ArrayList<String> list = new ArrayList();
        UResourceBundleIterator iter = bundle.getIterator();
        while (iter.hasNext()) {
            UResourceBundle patResource = iter.next();
            switch (patResource.getType()) {
                case XmlPullParser.START_DOCUMENT /*0*/:
                    list.add(null);
                    break;
                case NodeFilter.SHOW_CDATA_SECTION /*8*/:
                    list.add(patResource.getStringArray()[1]);
                    break;
                default:
                    break;
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public ULocale getULocale() {
        return this.fBundle.getULocale();
    }
}
