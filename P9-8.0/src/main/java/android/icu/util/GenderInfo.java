package android.icu.util;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

@Deprecated
public class GenderInfo {
    private static final /* synthetic */ int[] -android-icu-util-GenderInfo$GenderSwitchesValues = null;
    private static final /* synthetic */ int[] -android-icu-util-GenderInfo$ListGenderStyleSwitchesValues = null;
    private static Cache genderInfoCache = new Cache();
    private static GenderInfo neutral = new GenderInfo(ListGenderStyle.NEUTRAL);
    private final ListGenderStyle style;

    private static class Cache {
        private final ICUCache<ULocale, GenderInfo> cache;

        /* synthetic */ Cache(Cache -this0) {
            this();
        }

        private Cache() {
            this.cache = new SimpleCache();
        }

        public GenderInfo get(ULocale locale) {
            GenderInfo result = (GenderInfo) this.cache.get(locale);
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
            ListGenderStyle result = (ListGenderStyle) fromNameMap.get(name);
            if (result != null) {
                return result;
            }
            throw new IllegalArgumentException("Unknown gender style name: " + name);
        }
    }

    private static /* synthetic */ int[] -getandroid-icu-util-GenderInfo$GenderSwitchesValues() {
        if (-android-icu-util-GenderInfo$GenderSwitchesValues != null) {
            return -android-icu-util-GenderInfo$GenderSwitchesValues;
        }
        int[] iArr = new int[Gender.values().length];
        try {
            iArr[Gender.FEMALE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Gender.MALE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Gender.OTHER.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -android-icu-util-GenderInfo$GenderSwitchesValues = iArr;
        return iArr;
    }

    private static /* synthetic */ int[] -getandroid-icu-util-GenderInfo$ListGenderStyleSwitchesValues() {
        if (-android-icu-util-GenderInfo$ListGenderStyleSwitchesValues != null) {
            return -android-icu-util-GenderInfo$ListGenderStyleSwitchesValues;
        }
        int[] iArr = new int[ListGenderStyle.values().length];
        try {
            iArr[ListGenderStyle.MALE_TAINTS.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ListGenderStyle.MIXED_NEUTRAL.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ListGenderStyle.NEUTRAL.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        -android-icu-util-GenderInfo$ListGenderStyleSwitchesValues = iArr;
        return iArr;
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
        return getListGender(Arrays.asList(genders));
    }

    @Deprecated
    public Gender getListGender(List<Gender> genders) {
        if (genders.size() == 0) {
            return Gender.OTHER;
        }
        if (genders.size() == 1) {
            return (Gender) genders.get(0);
        }
        switch (-getandroid-icu-util-GenderInfo$ListGenderStyleSwitchesValues()[this.style.ordinal()]) {
            case 1:
                for (Gender gender : genders) {
                    if (gender != Gender.FEMALE) {
                        return Gender.MALE;
                    }
                }
                return Gender.FEMALE;
            case 2:
                boolean hasFemale = false;
                boolean hasMale = false;
                for (Gender gender2 : genders) {
                    switch (-getandroid-icu-util-GenderInfo$GenderSwitchesValues()[gender2.ordinal()]) {
                        case 1:
                            if (!hasMale) {
                                hasFemale = true;
                                break;
                            }
                            return Gender.OTHER;
                        case 2:
                            if (!hasFemale) {
                                hasMale = true;
                                break;
                            }
                            return Gender.OTHER;
                        case 3:
                            return Gender.OTHER;
                        default:
                            break;
                    }
                }
                return hasMale ? Gender.MALE : Gender.FEMALE;
            case 3:
                return Gender.OTHER;
            default:
                return Gender.OTHER;
        }
    }

    @Deprecated
    public GenderInfo(ListGenderStyle genderStyle) {
        this.style = genderStyle;
    }
}
