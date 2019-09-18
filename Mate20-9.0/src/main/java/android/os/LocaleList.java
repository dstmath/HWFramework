package android.os;

import android.icu.util.ULocale;
import android.os.Parcelable;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

public final class LocaleList implements Parcelable {
    public static final Parcelable.Creator<LocaleList> CREATOR = new Parcelable.Creator<LocaleList>() {
        public LocaleList createFromParcel(Parcel source) {
            return LocaleList.forLanguageTags(source.readString());
        }

        public LocaleList[] newArray(int size) {
            return new LocaleList[size];
        }
    };
    private static final Locale EN_LATN = Locale.forLanguageTag("en-Latn");
    private static final Locale LOCALE_AR_XB = new Locale("ar", "XB");
    private static final Locale LOCALE_EN_XA = new Locale("en", "XA");
    private static final int NUM_PSEUDO_LOCALES = 2;
    private static final String STRING_AR_XB = "ar-XB";
    private static final String STRING_EN_XA = "en-XA";
    @GuardedBy("sLock")
    private static LocaleList sDefaultAdjustedLocaleList = null;
    @GuardedBy("sLock")
    private static LocaleList sDefaultLocaleList = null;
    private static final Locale[] sEmptyList = new Locale[0];
    private static final LocaleList sEmptyLocaleList = new LocaleList(new Locale[0]);
    @GuardedBy("sLock")
    private static Locale sLastDefaultLocale = null;
    @GuardedBy("sLock")
    private static LocaleList sLastExplicitlySetLocaleList = null;
    private static final Object sLock = new Object();
    private final Locale[] mList;
    private final String mStringRepresentation;

    public Locale get(int index) {
        if (index < 0 || index >= this.mList.length) {
            return null;
        }
        return this.mList[index];
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
            result = (31 * result) + hashCode.hashCode();
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

    public void writeToProto(ProtoOutputStream protoOutputStream, long fieldId) {
        for (Locale locale : this.mList) {
            long token = protoOutputStream.start(fieldId);
            protoOutputStream.write(1138166333441L, locale.getLanguage());
            protoOutputStream.write(1138166333442L, locale.getCountry());
            protoOutputStream.write(1138166333443L, locale.getVariant());
            protoOutputStream.end(token);
        }
    }

    public String toLanguageTags() {
        return this.mStringRepresentation;
    }

    public LocaleList(Locale... list) {
        if (list.length == 0) {
            this.mList = sEmptyList;
            this.mStringRepresentation = "";
            return;
        }
        Locale[] localeList = new Locale[list.length];
        HashSet<Locale> seenLocales = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < list.length) {
            Locale l = list[i];
            if (l == null) {
                throw new NullPointerException("list[" + i + "] is null");
            } else if (!seenLocales.contains(l)) {
                Locale localeClone = (Locale) l.clone();
                localeList[i] = localeClone;
                sb.append(localeClone.toLanguageTag());
                if (i < list.length - 1) {
                    sb.append(',');
                }
                seenLocales.add(localeClone);
                i++;
            } else {
                throw new IllegalArgumentException("list[" + i + "] is a repetition");
            }
        }
        this.mList = localeList;
        this.mStringRepresentation = sb.toString();
    }

    public LocaleList(Locale topLocale, LocaleList otherLocales) {
        if (topLocale != null) {
            int inputLength = otherLocales == null ? 0 : otherLocales.mList.length;
            int topLocaleIndex = -1;
            int i = 0;
            while (true) {
                if (i >= inputLength) {
                    break;
                } else if (topLocale.equals(otherLocales.mList[i])) {
                    topLocaleIndex = i;
                    break;
                } else {
                    i++;
                }
            }
            int outputLength = (topLocaleIndex == -1 ? 1 : 0) + inputLength;
            Locale[] localeList = new Locale[outputLength];
            localeList[0] = (Locale) topLocale.clone();
            if (topLocaleIndex == -1) {
                for (int i2 = 0; i2 < inputLength; i2++) {
                    localeList[i2 + 1] = (Locale) otherLocales.mList[i2].clone();
                }
            } else {
                for (int i3 = 0; i3 < topLocaleIndex; i3++) {
                    localeList[i3 + 1] = (Locale) otherLocales.mList[i3].clone();
                }
                for (int i4 = topLocaleIndex + 1; i4 < inputLength; i4++) {
                    localeList[i4] = (Locale) otherLocales.mList[i4].clone();
                }
            }
            StringBuilder sb = new StringBuilder();
            for (int i5 = 0; i5 < outputLength; i5++) {
                sb.append(localeList[i5].toLanguageTag());
                if (i5 < outputLength - 1) {
                    sb.append(',');
                }
            }
            this.mList = localeList;
            this.mStringRepresentation = sb.toString();
            return;
        }
        throw new NullPointerException("topLocale is null");
    }

    public static LocaleList getEmptyLocaleList() {
        return sEmptyLocaleList;
    }

    public static LocaleList forLanguageTags(String list) {
        if (list == null || list.equals("")) {
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
        if (!script.isEmpty()) {
            return script;
        }
        return ULocale.addLikelySubtags(ULocale.forLocale(locale)).getScript();
    }

    private static boolean isPseudoLocale(String locale) {
        return STRING_EN_XA.equals(locale) || STRING_AR_XB.equals(locale);
    }

    public static boolean isPseudoLocale(Locale locale) {
        return LOCALE_EN_XA.equals(locale) || LOCALE_AR_XB.equals(locale);
    }

    private static int matchScore(Locale supported, Locale desired) {
        int i = 1;
        if (supported.equals(desired)) {
            return 1;
        }
        if (!supported.getLanguage().equals(desired.getLanguage()) || isPseudoLocale(supported) || isPseudoLocale(desired)) {
            return 0;
        }
        String supportedScr = getLikelyScript(supported);
        if (!supportedScr.isEmpty()) {
            return supportedScr.equals(getLikelyScript(desired)) ? 1 : 0;
        }
        String supportedRegion = supported.getCountry();
        if (!supportedRegion.isEmpty() && !supportedRegion.equals(desired.getCountry())) {
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
        return Integer.MAX_VALUE;
    }

    private int computeFirstMatchIndex(Collection<String> supportedLocales, boolean assumeEnglishIsSupported) {
        if (this.mList.length == 1) {
            return 0;
        }
        if (this.mList.length == 0) {
            return -1;
        }
        int bestIndex = Integer.MAX_VALUE;
        if (assumeEnglishIsSupported) {
            int idx = findFirstMatchIndex(EN_LATN);
            if (idx == 0) {
                return 0;
            }
            if (idx < Integer.MAX_VALUE) {
                bestIndex = idx;
            }
        }
        for (String languageTag : supportedLocales) {
            int idx2 = findFirstMatchIndex(Locale.forLanguageTag(languageTag));
            if (idx2 == 0) {
                return 0;
            }
            if (idx2 < bestIndex) {
                bestIndex = idx2;
            }
        }
        if (bestIndex == Integer.MAX_VALUE) {
            return 0;
        }
        return bestIndex;
    }

    private Locale computeFirstMatch(Collection<String> supportedLocales, boolean assumeEnglishIsSupported) {
        int bestIndex = computeFirstMatchIndex(supportedLocales, assumeEnglishIsSupported);
        if (bestIndex == -1) {
            return null;
        }
        return this.mList[bestIndex];
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
        return getFirstMatchIndexWithEnglishSupported((Collection<String>) Arrays.asList(supportedLocales));
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
            if (!defaultLocale.equals(sLastDefaultLocale)) {
                sLastDefaultLocale = defaultLocale;
                if (sDefaultLocaleList == null || !defaultLocale.equals(sDefaultLocaleList.get(0))) {
                    sDefaultLocaleList = new LocaleList(defaultLocale, sLastExplicitlySetLocaleList);
                    sDefaultAdjustedLocaleList = sDefaultLocaleList;
                } else {
                    LocaleList localeList = sDefaultLocaleList;
                    return localeList;
                }
            }
            LocaleList localeList2 = sDefaultLocaleList;
            return localeList2;
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
        } else if (!locales.isEmpty()) {
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
        } else {
            throw new IllegalArgumentException("locales is empty");
        }
    }
}
