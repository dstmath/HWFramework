package android.os;

import android.icu.util.ULocale;
import android.net.ProxyInfo;
import android.os.Parcelable.Creator;
import android.preference.Preference;
import com.android.internal.annotations.GuardedBy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

public final class LocaleList implements Parcelable {
    public static final Creator<LocaleList> CREATOR = null;
    private static final Locale EN_LATN = null;
    private static final Locale LOCALE_AR_XB = null;
    private static final Locale LOCALE_EN_XA = null;
    private static final int NUM_PSEUDO_LOCALES = 2;
    private static final String STRING_AR_XB = "ar-XB";
    private static final String STRING_EN_XA = "en-XA";
    @GuardedBy("sLock")
    private static LocaleList sDefaultAdjustedLocaleList;
    @GuardedBy("sLock")
    private static LocaleList sDefaultLocaleList;
    private static final Locale[] sEmptyList = null;
    private static final LocaleList sEmptyLocaleList = null;
    @GuardedBy("sLock")
    private static Locale sLastDefaultLocale;
    @GuardedBy("sLock")
    private static LocaleList sLastExplicitlySetLocaleList;
    private static final Object sLock = null;
    private final Locale[] mList;
    private final String mStringRepresentation;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.LocaleList.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.LocaleList.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.LocaleList.<clinit>():void");
    }

    public Locale get(int index) {
        return (index < 0 || index >= this.mList.length) ? null : this.mList[index];
    }

    public boolean isEmpty() {
        return this.mList.length == 0;
    }

    public int size() {
        return this.mList.length;
    }

    public int indexOf(Locale locale) {
        for (int i = 0; i < this.mList.length; i++) {
            if (this.mList[i].equals(locale)) {
                return i;
            }
        }
        return -1;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LocaleList)) {
            return false;
        }
        Locale[] otherList = ((LocaleList) other).mList;
        if (this.mList.length != otherList.length) {
            return false;
        }
        for (int i = 0; i < this.mList.length; i++) {
            if (!this.mList[i].equals(otherList[i])) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int result = 1;
        for (Locale hashCode : this.mList) {
            result = (result * 31) + hashCode.hashCode();
        }
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < this.mList.length; i++) {
            sb.append(this.mList[i]);
            if (i < this.mList.length - 1) {
                sb.append(',');
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeString(this.mStringRepresentation);
    }

    public String toLanguageTags() {
        return this.mStringRepresentation;
    }

    public LocaleList(Locale... list) {
        if (list.length == 0) {
            this.mList = sEmptyList;
            this.mStringRepresentation = ProxyInfo.LOCAL_EXCL_LIST;
            return;
        }
        Locale[] localeList = new Locale[list.length];
        HashSet<Locale> seenLocales = new HashSet();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < list.length) {
            Locale l = list[i];
            if (l == null) {
                throw new NullPointerException("list[" + i + "] is null");
            } else if (seenLocales.contains(l)) {
                throw new IllegalArgumentException("list[" + i + "] is a repetition");
            } else {
                Locale localeClone = (Locale) l.clone();
                localeList[i] = localeClone;
                sb.append(localeClone.toLanguageTag());
                if (i < list.length - 1) {
                    sb.append(',');
                }
                seenLocales.add(localeClone);
                i++;
            }
        }
        this.mList = localeList;
        this.mStringRepresentation = sb.toString();
    }

    public LocaleList(Locale topLocale, LocaleList otherLocales) {
        if (topLocale == null) {
            throw new NullPointerException("topLocale is null");
        }
        int i;
        int i2;
        int inputLength = otherLocales == null ? 0 : otherLocales.mList.length;
        int topLocaleIndex = -1;
        for (i = 0; i < inputLength; i++) {
            if (topLocale.equals(otherLocales.mList[i])) {
                topLocaleIndex = i;
                break;
            }
        }
        if (topLocaleIndex == -1) {
            i2 = 1;
        } else {
            i2 = 0;
        }
        int outputLength = inputLength + i2;
        Locale[] localeList = new Locale[outputLength];
        localeList[0] = (Locale) topLocale.clone();
        if (topLocaleIndex == -1) {
            for (i = 0; i < inputLength; i++) {
                localeList[i + 1] = (Locale) otherLocales.mList[i].clone();
            }
        } else {
            for (i = 0; i < topLocaleIndex; i++) {
                localeList[i + 1] = (Locale) otherLocales.mList[i].clone();
            }
            for (i = topLocaleIndex + 1; i < inputLength; i++) {
                localeList[i] = (Locale) otherLocales.mList[i].clone();
            }
        }
        StringBuilder sb = new StringBuilder();
        for (i = 0; i < outputLength; i++) {
            sb.append(localeList[i].toLanguageTag());
            if (i < outputLength - 1) {
                sb.append(',');
            }
        }
        this.mList = localeList;
        this.mStringRepresentation = sb.toString();
    }

    public static LocaleList getEmptyLocaleList() {
        return sEmptyLocaleList;
    }

    public static LocaleList forLanguageTags(String list) {
        if (list == null || list.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
            return getEmptyLocaleList();
        }
        String[] tags = list.split(",");
        Locale[] localeArray = new Locale[tags.length];
        for (int i = 0; i < localeArray.length; i++) {
            localeArray[i] = Locale.forLanguageTag(tags[i]);
        }
        return new LocaleList(localeArray);
    }

    private static String getLikelyScript(Locale locale) {
        String script = locale.getScript();
        if (script.isEmpty()) {
            return ULocale.addLikelySubtags(ULocale.forLocale(locale)).getScript();
        }
        return script;
    }

    private static boolean isPseudoLocale(String locale) {
        return !STRING_EN_XA.equals(locale) ? STRING_AR_XB.equals(locale) : true;
    }

    private static boolean isPseudoLocale(Locale locale) {
        return !LOCALE_EN_XA.equals(locale) ? LOCALE_AR_XB.equals(locale) : true;
    }

    private static int matchScore(Locale supported, Locale desired) {
        int i = 1;
        int i2 = 0;
        if (supported.equals(desired)) {
            return 1;
        }
        if (!supported.getLanguage().equals(desired.getLanguage()) || isPseudoLocale(supported) || isPseudoLocale(desired)) {
            return 0;
        }
        String supportedScr = getLikelyScript(supported);
        if (supportedScr.isEmpty()) {
            String supportedRegion = supported.getCountry();
            if (supportedRegion.isEmpty() || supportedRegion.equals(desired.getCountry())) {
                i2 = 1;
            }
            return i2;
        }
        if (!supportedScr.equals(getLikelyScript(desired))) {
            i = 0;
        }
        return i;
    }

    private int findFirstMatchIndex(Locale supportedLocale) {
        for (int idx = 0; idx < this.mList.length; idx++) {
            if (matchScore(supportedLocale, this.mList[idx]) > 0) {
                return idx;
            }
        }
        return Preference.DEFAULT_ORDER;
    }

    private int computeFirstMatchIndex(Collection<String> supportedLocales, boolean assumeEnglishIsSupported) {
        if (this.mList.length == 1) {
            return 0;
        }
        if (this.mList.length == 0) {
            return -1;
        }
        int idx;
        int bestIndex = Preference.DEFAULT_ORDER;
        if (assumeEnglishIsSupported) {
            idx = findFirstMatchIndex(EN_LATN);
            if (idx == 0) {
                return 0;
            }
            if (idx < Preference.DEFAULT_ORDER) {
                bestIndex = idx;
            }
        }
        for (String languageTag : supportedLocales) {
            idx = findFirstMatchIndex(Locale.forLanguageTag(languageTag));
            if (idx == 0) {
                return 0;
            }
            if (idx < bestIndex) {
                bestIndex = idx;
            }
        }
        if (bestIndex == Preference.DEFAULT_ORDER) {
            return 0;
        }
        return bestIndex;
    }

    private Locale computeFirstMatch(Collection<String> supportedLocales, boolean assumeEnglishIsSupported) {
        int bestIndex = computeFirstMatchIndex(supportedLocales, assumeEnglishIsSupported);
        return bestIndex == -1 ? null : this.mList[bestIndex];
    }

    public Locale getFirstMatch(String[] supportedLocales) {
        return computeFirstMatch(Arrays.asList(supportedLocales), false);
    }

    public int getFirstMatchIndex(String[] supportedLocales) {
        return computeFirstMatchIndex(Arrays.asList(supportedLocales), false);
    }

    public Locale getFirstMatchWithEnglishSupported(String[] supportedLocales) {
        return computeFirstMatch(Arrays.asList(supportedLocales), true);
    }

    public int getFirstMatchIndexWithEnglishSupported(Collection<String> supportedLocales) {
        return computeFirstMatchIndex(supportedLocales, true);
    }

    public int getFirstMatchIndexWithEnglishSupported(String[] supportedLocales) {
        return getFirstMatchIndexWithEnglishSupported(Arrays.asList(supportedLocales));
    }

    public static boolean isPseudoLocalesOnly(String[] supportedLocales) {
        if (supportedLocales == null) {
            return true;
        }
        if (supportedLocales.length > 3) {
            return false;
        }
        for (String locale : supportedLocales) {
            if (!locale.isEmpty() && !isPseudoLocale(locale)) {
                return false;
            }
        }
        return true;
    }

    public static LocaleList getDefault() {
        Locale defaultLocale = Locale.getDefault();
        synchronized (sLock) {
            LocaleList localeList;
            if (!defaultLocale.equals(sLastDefaultLocale)) {
                sLastDefaultLocale = defaultLocale;
                if (sDefaultLocaleList == null || !defaultLocale.equals(sDefaultLocaleList.get(0))) {
                    sDefaultLocaleList = new LocaleList(defaultLocale, sLastExplicitlySetLocaleList);
                    sDefaultAdjustedLocaleList = sDefaultLocaleList;
                } else {
                    localeList = sDefaultLocaleList;
                    return localeList;
                }
            }
            localeList = sDefaultLocaleList;
            return localeList;
        }
    }

    public static LocaleList getAdjustedDefault() {
        LocaleList localeList;
        getDefault();
        synchronized (sLock) {
            localeList = sDefaultAdjustedLocaleList;
        }
        return localeList;
    }

    public static void setDefault(LocaleList locales) {
        setDefault(locales, 0);
    }

    public static void setDefault(LocaleList locales, int localeIndex) {
        if (locales == null) {
            throw new NullPointerException("locales is null");
        } else if (locales.isEmpty()) {
            throw new IllegalArgumentException("locales is empty");
        } else {
            synchronized (sLock) {
                sLastDefaultLocale = locales.get(localeIndex);
                Locale.setDefault(sLastDefaultLocale);
                sLastExplicitlySetLocaleList = locales;
                sDefaultLocaleList = locales;
                if (localeIndex == 0) {
                    sDefaultAdjustedLocaleList = sDefaultLocaleList;
                } else {
                    sDefaultAdjustedLocaleList = new LocaleList(sLastDefaultLocale, sDefaultLocaleList);
                }
            }
        }
    }
}
