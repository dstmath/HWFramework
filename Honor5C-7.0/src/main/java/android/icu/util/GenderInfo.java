package android.icu.util;

import android.icu.impl.ICUCache;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.SimpleCache;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import org.w3c.dom.traversal.NodeFilter;
import org.xmlpull.v1.XmlPullParser;

@Deprecated
public class GenderInfo {
    private static final /* synthetic */ int[] -android-icu-util-GenderInfo$GenderSwitchesValues = null;
    private static final /* synthetic */ int[] -android-icu-util-GenderInfo$ListGenderStyleSwitchesValues = null;
    private static Cache genderInfoCache;
    private static GenderInfo neutral;
    private final ListGenderStyle style;

    private static class Cache {
        private final ICUCache<ULocale, GenderInfo> cache;

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
                return new GenderInfo(ListGenderStyle.fromName(UResourceBundle.getBundleInstance(ICUResourceBundle.ICU_BASE_NAME, "genderList", ICUResourceBundle.ICU_DATA_CLASS_LOADER, true).get("genderList").getString(ulocale.toString())));
            } catch (MissingResourceException e) {
                return null;
            }
        }
    }

    @Deprecated
    public enum Gender {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.GenderInfo.Gender.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.GenderInfo.Gender.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.GenderInfo.Gender.<clinit>():void");
        }
    }

    @Deprecated
    public enum ListGenderStyle {
        ;
        
        private static Map<String, ListGenderStyle> fromNameMap;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.GenderInfo.ListGenderStyle.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.GenderInfo.ListGenderStyle.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.util.GenderInfo.ListGenderStyle.<clinit>():void");
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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.icu.util.GenderInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.icu.util.GenderInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.util.GenderInfo.<clinit>():void");
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
            case NodeFilter.SHOW_ELEMENT /*1*/:
                for (Gender gender : genders) {
                    if (gender != Gender.FEMALE) {
                        return Gender.MALE;
                    }
                }
                return Gender.FEMALE;
            case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                boolean hasFemale = false;
                boolean hasMale = false;
                for (Gender gender2 : genders) {
                    switch (-getandroid-icu-util-GenderInfo$GenderSwitchesValues()[gender2.ordinal()]) {
                        case NodeFilter.SHOW_ELEMENT /*1*/:
                            if (!hasMale) {
                                hasFemale = true;
                                break;
                            }
                            return Gender.OTHER;
                        case NodeFilter.SHOW_ATTRIBUTE /*2*/:
                            if (!hasFemale) {
                                hasMale = true;
                                break;
                            }
                            return Gender.OTHER;
                        case XmlPullParser.END_TAG /*3*/:
                            return Gender.OTHER;
                        default:
                            break;
                    }
                }
                return hasMale ? Gender.MALE : Gender.FEMALE;
            case XmlPullParser.END_TAG /*3*/:
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
