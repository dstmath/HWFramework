package android.icu.util;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

@Deprecated
public class GenderInfo {
    private static Cache genderInfoCache = new Cache();
    /* access modifiers changed from: private */
    public static GenderInfo neutral = new GenderInfo(ListGenderStyle.NEUTRAL);
    private final ListGenderStyle style;

    private static class Cache {
        private final ICUCache<ULocale, GenderInfo> cache;

        private Cache() {
            this.cache = new SimpleCache();
        }

        public GenderInfo get(ULocale locale) {
            GenderInfo result = this.cache.get(locale);
            if (result == null) {
                result = load(locale);
                if (result == null) {
                    ULocale fallback = locale.getFallback();
                    result = fallback == null ? GenderInfo.neutral : get(fallback);
                }
                this.cache.put(locale, result);
            }
            return result;
        }

        private static GenderInfo load(ULocale ulocale) {
            try {
                return new GenderInfo(ListGenderStyle.fromName(UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "genderList", ICUResourceBundle.ICU_DATA_CLASS_LOADER, true).get("genderList").getString(ulocale.toString())));
            } catch (MissingResourceException e) {
                return null;
            }
        }
    }

    @Deprecated
    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    @Deprecated
    public enum ListGenderStyle {
        NEUTRAL,
        MIXED_NEUTRAL,
        MALE_TAINTS;
        
        private static Map<String, ListGenderStyle> fromNameMap;

        static {
            fromNameMap = new HashMap(3);
            fromNameMap.put("neutral", NEUTRAL);
            fromNameMap.put("maleTaints", MALE_TAINTS);
            fromNameMap.put("mixedNeutral", MIXED_NEUTRAL);
        }

        @Deprecated
        public static ListGenderStyle fromName(String name) {
            ListGenderStyle result = fromNameMap.get(name);
            if (result != null) {
                return result;
            }
            throw new IllegalArgumentException("Unknown gender style name: " + name);
        }
    }

    @Deprecated
    public static GenderInfo getInstance(ULocale uLocale) {
        return genderInfoCache.get(uLocale);
    }

    @Deprecated
    public static GenderInfo getInstance(Locale locale) {
        return getInstance(ULocale.forLocale(locale));
    }

    @Deprecated
    public Gender getListGender(Gender... genders) {
        return getListGender((List<Gender>) Arrays.asList(genders));
    }

    @Deprecated
    public Gender getListGender(List<Gender> genders) {
        if (genders.size() == 0) {
            return Gender.OTHER;
        }
        if (genders.size() == 1) {
            return genders.get(0);
        }
        switch (this.style) {
            case NEUTRAL:
                return Gender.OTHER;
            case MIXED_NEUTRAL:
                boolean hasFemale = false;
                boolean hasMale = false;
                Iterator<Gender> it = genders.iterator();
                while (it.hasNext()) {
                    switch (it.next()) {
                        case FEMALE:
                            if (!hasMale) {
                                hasFemale = true;
                                break;
                            } else {
                                return Gender.OTHER;
                            }
                        case MALE:
                            if (!hasFemale) {
                                hasMale = true;
                                break;
                            } else {
                                return Gender.OTHER;
                            }
                        case OTHER:
                            return Gender.OTHER;
                    }
                }
                return hasMale ? Gender.MALE : Gender.FEMALE;
            case MALE_TAINTS:
                for (Gender gender : genders) {
                    if (gender != Gender.FEMALE) {
                        return Gender.MALE;
                    }
                }
                return Gender.FEMALE;
            default:
                return Gender.OTHER;
        }
    }

    @Deprecated
    public GenderInfo(ListGenderStyle genderStyle) {
        this.style = genderStyle;
    }
}
