package android.icu.text;

import android.icu.impl.CacheBase;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SoftCache;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import android.icu.util.UResourceBundleIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;

public class NumberingSystem {
    public static final NumberingSystem LATIN = lookupInstanceByName("latn");
    private static final String[] OTHER_NS_KEYWORDS = {"native", "traditional", "finance"};
    private static CacheBase<String, NumberingSystem, LocaleLookupData> cachedLocaleData = new SoftCache<String, NumberingSystem, LocaleLookupData>() {
        /* access modifiers changed from: protected */
        public NumberingSystem createInstance(String key, LocaleLookupData localeLookupData) {
            return NumberingSystem.lookupInstanceByLocale(localeLookupData);
        }
    };
    private static CacheBase<String, NumberingSystem, Void> cachedStringData = new SoftCache<String, NumberingSystem, Void>() {
        /* access modifiers changed from: protected */
        public NumberingSystem createInstance(String key, Void unused) {
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

        LocaleLookupData(ULocale locale2, String numbersKeyword2) {
            this.locale = locale2;
            this.numbersKeyword = numbersKeyword2;
        }
    }

    public static NumberingSystem getInstance(int radix_in, boolean isAlgorithmic_in, String desc_in) {
        return getInstance(null, radix_in, isAlgorithmic_in, desc_in);
    }

    private static NumberingSystem getInstance(String name_in, int radix_in, boolean isAlgorithmic_in, String desc_in) {
        if (radix_in < 2) {
            throw new IllegalArgumentException("Invalid radix for numbering system");
        } else if (isAlgorithmic_in || (desc_in.codePointCount(0, desc_in.length()) == radix_in && isValidDigitString(desc_in))) {
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
            String[] strArr = OTHER_NS_KEYWORDS;
            int length = strArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (numbersKeyword.equals(strArr[i])) {
                    nsResolved = false;
                    break;
                } else {
                    i++;
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
        String baseName = locale.getBaseName();
        return cachedLocaleData.getInstance(baseName + "@numbers=" + numbersKeyword, new LocaleLookupData(locale, numbersKeyword));
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
        return getInstance(ULocale.getDefault(ULocale.Category.FORMAT));
    }

    public static NumberingSystem getInstanceByName(String name2) {
        return cachedStringData.getInstance(name2, null);
    }

    /* access modifiers changed from: private */
    public static NumberingSystem lookupInstanceByName(String name2) {
        try {
            UResourceBundle nsTop = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "numberingSystems").get("numberingSystems").get(name2);
            String description = nsTop.getString("desc");
            UResourceBundle nsRadixBundle = nsTop.get("radix");
            UResourceBundle nsAlgBundle = nsTop.get("algorithmic");
            int radix2 = nsRadixBundle.getInt();
            boolean isAlgorithmic = true;
            if (nsAlgBundle.getInt() != 1) {
                isAlgorithmic = false;
            }
            return getInstance(name2, radix2, isAlgorithmic, description);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    public static String[] getAvailableNames() {
        UResourceBundle nsCurrent = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "numberingSystems").get("numberingSystems");
        ArrayList<String> output = new ArrayList<>();
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
