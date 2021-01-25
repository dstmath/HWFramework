package ohos.global.icu.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import ohos.global.icu.impl.ICUCache;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.SimpleCache;

@Deprecated
public class GenderInfo {
    private static Cache genderInfoCache = new Cache(null);
    private static GenderInfo neutral = new GenderInfo(ListGenderStyle.NEUTRAL);
    private final ListGenderStyle style;

    @Deprecated
    public enum Gender {
        MALE,
        FEMALE,
        OTHER
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
    public enum ListGenderStyle {
        NEUTRAL,
        MIXED_NEUTRAL,
        MALE_TAINTS;
        
        private static Map<String, ListGenderStyle> fromNameMap = new HashMap(3);

        static {
            fromNameMap.put("neutral", NEUTRAL);
            fromNameMap.put("maleTaints", MALE_TAINTS);
            fromNameMap.put("mixedNeutral", MIXED_NEUTRAL);
        }

        @Deprecated
        public static ListGenderStyle fromName(String str) {
            ListGenderStyle listGenderStyle = fromNameMap.get(str);
            if (listGenderStyle != null) {
                return listGenderStyle;
            }
            throw new IllegalArgumentException("Unknown gender style name: " + str);
        }
    }

    @Deprecated
    public Gender getListGender(Gender... genderArr) {
        return getListGender(Arrays.asList(genderArr));
    }

    @Deprecated
    public Gender getListGender(List<Gender> list) {
        if (list.size() == 0) {
            return Gender.OTHER;
        }
        boolean z = false;
        if (list.size() == 1) {
            return list.get(0);
        }
        int i = AnonymousClass1.$SwitchMap$ohos$global$icu$util$GenderInfo$ListGenderStyle[this.style.ordinal()];
        if (i == 1) {
            return Gender.OTHER;
        }
        if (i == 2) {
            boolean z2 = false;
            for (Gender gender : list) {
                int i2 = AnonymousClass1.$SwitchMap$ohos$global$icu$util$GenderInfo$Gender[gender.ordinal()];
                if (i2 != 1) {
                    if (i2 != 2) {
                        if (i2 == 3) {
                            return Gender.OTHER;
                        }
                    } else if (z2) {
                        return Gender.OTHER;
                    } else {
                        z = true;
                    }
                } else if (z) {
                    return Gender.OTHER;
                } else {
                    z2 = true;
                }
            }
            if (z) {
                return Gender.MALE;
            }
            return Gender.FEMALE;
        } else if (i != 3) {
            return Gender.OTHER;
        } else {
            for (Gender gender2 : list) {
                if (gender2 != Gender.FEMALE) {
                    return Gender.MALE;
                }
            }
            return Gender.FEMALE;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: ohos.global.icu.util.GenderInfo$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$util$GenderInfo$Gender = new int[Gender.values().length];
        static final /* synthetic */ int[] $SwitchMap$ohos$global$icu$util$GenderInfo$ListGenderStyle = new int[ListGenderStyle.values().length];

        static {
            try {
                $SwitchMap$ohos$global$icu$util$GenderInfo$ListGenderStyle[ListGenderStyle.NEUTRAL.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$GenderInfo$ListGenderStyle[ListGenderStyle.MIXED_NEUTRAL.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$GenderInfo$ListGenderStyle[ListGenderStyle.MALE_TAINTS.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$GenderInfo$Gender[Gender.FEMALE.ordinal()] = 1;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$GenderInfo$Gender[Gender.MALE.ordinal()] = 2;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ohos$global$icu$util$GenderInfo$Gender[Gender.OTHER.ordinal()] = 3;
            } catch (NoSuchFieldError unused6) {
            }
        }
    }

    @Deprecated
    public GenderInfo(ListGenderStyle listGenderStyle) {
        this.style = listGenderStyle;
    }

    /* access modifiers changed from: private */
    public static class Cache {
        private final ICUCache<ULocale, GenderInfo> cache;

        private Cache() {
            this.cache = new SimpleCache();
        }

        /* synthetic */ Cache(AnonymousClass1 r1) {
            this();
        }

        public GenderInfo get(ULocale uLocale) {
            GenderInfo genderInfo = (GenderInfo) this.cache.get(uLocale);
            if (genderInfo == null) {
                genderInfo = load(uLocale);
                if (genderInfo == null) {
                    ULocale fallback = uLocale.getFallback();
                    genderInfo = fallback == null ? GenderInfo.neutral : get(fallback);
                }
                this.cache.put(uLocale, genderInfo);
            }
            return genderInfo;
        }

        private static GenderInfo load(ULocale uLocale) {
            try {
                return new GenderInfo(ListGenderStyle.fromName(UResourceBundle.getBundleInstance("ohos/global/icu/impl/data/icudt66b", "genderList", ICUResourceBundle.ICU_DATA_CLASS_LOADER, true).get("genderList").getString(uLocale.toString())));
            } catch (MissingResourceException unused) {
                return null;
            }
        }
    }
}
