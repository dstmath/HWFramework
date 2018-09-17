package android.icu.text;

import android.icu.impl.CacheBase;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SoftCache;
import android.icu.util.ULocale;
import android.icu.util.ULocale.Category;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;

public class NumberingSystem {
    private static final String[] OTHER_NS_KEYWORDS = new String[]{"native", "traditional", "finance"};
    private static CacheBase<String, NumberingSystem, LocaleLookupData> cachedLocaleData = new SoftCache<String, NumberingSystem, LocaleLookupData>() {
        protected NumberingSystem createInstance(String key, LocaleLookupData localeLookupData) {
            return NumberingSystem.lookupInstanceByLocale(localeLookupData);
        }
    };
    private static CacheBase<String, NumberingSystem, Void> cachedStringData = new SoftCache<String, NumberingSystem, Void>() {
        protected NumberingSystem createInstance(String key, Void unused) {
            return NumberingSystem.lookupInstanceByName(key);
        }
    };
    private boolean algorithmic = false;
    private String desc = "0123456789";
    private String name = "latn";
    private int radix = 10;

    private static class LocaleLookupData {
        public final ULocale locale;
        public final String numbersKeyword;

        LocaleLookupData(ULocale locale, String numbersKeyword) {
            this.locale = locale;
            this.numbersKeyword = numbersKeyword;
        }
    }

    public static NumberingSystem getInstance(int radix_in, boolean isAlgorithmic_in, String desc_in) {
        return getInstance(null, radix_in, isAlgorithmic_in, desc_in);
    }

    private static NumberingSystem getInstance(String name_in, int radix_in, boolean isAlgorithmic_in, String desc_in) {
        if (radix_in < 2) {
            throw new IllegalArgumentException("Invalid radix for numbering system");
        } else if (isAlgorithmic_in || (desc_in.length() == radix_in && (isValidDigitString(desc_in) ^ 1) == 0)) {
            NumberingSystem ns = new NumberingSystem();
            ns.radix = radix_in;
            ns.algorithmic = isAlgorithmic_in;
            ns.desc = desc_in;
            ns.name = name_in;
            return ns;
        } else {
            throw new IllegalArgumentException("Invalid digit string for numbering system");
        }
    }

    public static NumberingSystem getInstance(Locale inLocale) {
        return getInstance(ULocale.forLocale(inLocale));
    }

    public static NumberingSystem getInstance(ULocale locale) {
        boolean nsResolved = true;
        String numbersKeyword = locale.getKeywordValue("numbers");
        if (numbersKeyword != null) {
            for (String keyword : OTHER_NS_KEYWORDS) {
                if (numbersKeyword.equals(keyword)) {
                    nsResolved = false;
                    break;
                }
            }
        } else {
            numbersKeyword = "default";
            nsResolved = false;
        }
        if (nsResolved) {
            NumberingSystem ns = getInstanceByName(numbersKeyword);
            if (ns != null) {
                return ns;
            }
            numbersKeyword = "default";
        }
        return (NumberingSystem) cachedLocaleData.getInstance(locale.getBaseName() + "@numbers=" + numbersKeyword, new LocaleLookupData(locale, numbersKeyword));
    }

    static NumberingSystem lookupInstanceByLocale(LocaleLookupData localeLookupData) {
        try {
            ICUResourceBundle rb = ((ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, localeLookupData.locale)).getWithFallback("NumberElements");
            String numbersKeyword = localeLookupData.numbersKeyword;
            String resolvedNumberingSystem = null;
            while (true) {
                try {
                    resolvedNumberingSystem = rb.getStringWithFallback(numbersKeyword);
                    break;
                } catch (MissingResourceException e) {
                    if (!numbersKeyword.equals("native") && !numbersKeyword.equals("finance")) {
                        if (!numbersKeyword.equals("traditional")) {
                            break;
                        }
                        numbersKeyword = "native";
                    } else {
                        numbersKeyword = "default";
                    }
                }
            }
            NumberingSystem ns = null;
            if (resolvedNumberingSystem != null) {
                ns = getInstanceByName(resolvedNumberingSystem);
            }
            if (ns == null) {
                ns = new NumberingSystem();
            }
            return ns;
        } catch (MissingResourceException e2) {
            return new NumberingSystem();
        }
    }

    public static NumberingSystem getInstance() {
        return getInstance(ULocale.getDefault(Category.FORMAT));
    }

    public static NumberingSystem getInstanceByName(String name) {
        return (NumberingSystem) cachedStringData.getInstance(name, null);
    }

    private static NumberingSystem lookupInstanceByName(String name) {
        try {
            UResourceBundle nsTop = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "numberingSystems").get("numberingSystems").get(name);
            return getInstance(name, nsTop.get("radix").getInt(), nsTop.get("algorithmic").getInt() == 1, nsTop.getString("desc"));
        } catch (MissingResourceException e) {
            return null;
        }
    }

    public static String[] getAvailableNames() {
        UResourceBundle nsCurrent = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "numberingSystems").get("numberingSystems");
        ArrayList<String> output = new ArrayList();
        UResourceBundleIterator it = nsCurrent.getIterator();
        while (it.hasNext()) {
            output.add(it.next().getKey());
        }
        return (String[]) output.toArray(new String[output.size()]);
    }

    public static boolean isValidDigitString(String str) {
        if (str.codePointCount(0, str.length()) == 10) {
            return true;
        }
        return false;
    }

    public int getRadix() {
        return this.radix;
    }

    public String getDescription() {
        return this.desc;
    }

    public String getName() {
        return this.name;
    }

    public boolean isAlgorithmic() {
        return this.algorithmic;
    }
}
