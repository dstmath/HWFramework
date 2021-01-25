package ohos.global.icu.text;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;
import ohos.com.sun.org.apache.xalan.internal.templates.Constants;
import ohos.com.sun.org.apache.xpath.internal.compiler.PsuedoNames;
import ohos.global.icu.impl.ICUCache;
import ohos.global.icu.impl.ICUData;
import ohos.global.icu.impl.ICUResourceBundle;
import ohos.global.icu.impl.SimpleCache;
import ohos.global.icu.impl.UResource;
import ohos.global.icu.text.DateIntervalFormat;
import ohos.global.icu.util.Calendar;
import ohos.global.icu.util.Freezable;
import ohos.global.icu.util.ICUCloneNotSupportedException;
import ohos.global.icu.util.ICUException;
import ohos.global.icu.util.ULocale;
import ohos.global.icu.util.UResourceBundle;

public class DateIntervalInfo implements Cloneable, Freezable<DateIntervalInfo>, Serializable {
    static final String[] CALENDAR_FIELD_TO_PATTERN_LETTER = {"G", DateFormat.YEAR, DateFormat.NUM_MONTH, "w", "W", DateFormat.DAY, "D", DateFormat.ABBR_WEEKDAY, "F", "a", "h", DateFormat.HOUR24, DateFormat.MINUTE, DateFormat.SECOND, "S", DateFormat.ABBR_SPECIFIC_TZ, " ", "Y", "e", "u", "g", "A", " ", " "};
    private static String CALENDAR_KEY = "calendar";
    private static final ICUCache<String, DateIntervalInfo> DIICACHE = new SimpleCache();
    private static String EARLIEST_FIRST_PREFIX = "earliestFirst:";
    private static String FALLBACK_STRING = Constants.ELEMNAME_FALLBACK_STRING;
    private static String INTERVAL_FORMATS_KEY = "intervalFormats";
    private static String LATEST_FIRST_PREFIX = "latestFirst:";
    private static final int MINIMUM_SUPPORTED_CALENDAR_FIELD = 13;
    static final int currentSerialVersion = 1;
    private static final long serialVersionUID = 1;
    private String fFallbackIntervalPattern;
    private boolean fFirstDateInPtnIsLaterDate;
    private Map<String, Map<String, PatternInfo>> fIntervalPatterns;
    private transient boolean fIntervalPatternsReadOnly;
    private volatile transient boolean frozen;

    private static boolean stringNumeric(int i, int i2, char c) {
        if (c != 'M') {
            return false;
        }
        if (i > 2 || i2 <= 2) {
            return i > 2 && i2 <= 2;
        }
        return true;
    }

    public static final class PatternInfo implements Cloneable, Serializable {
        static final int currentSerialVersion = 1;
        private static final long serialVersionUID = 1;
        private final boolean fFirstDateInPtnIsLaterDate;
        private final String fIntervalPatternFirstPart;
        private final String fIntervalPatternSecondPart;

        public PatternInfo(String str, String str2, boolean z) {
            this.fIntervalPatternFirstPart = str;
            this.fIntervalPatternSecondPart = str2;
            this.fFirstDateInPtnIsLaterDate = z;
        }

        public String getFirstPart() {
            return this.fIntervalPatternFirstPart;
        }

        public String getSecondPart() {
            return this.fIntervalPatternSecondPart;
        }

        public boolean firstDateInPtnIsLaterDate() {
            return this.fFirstDateInPtnIsLaterDate;
        }

        @Override // java.lang.Object
        public boolean equals(Object obj) {
            if (!(obj instanceof PatternInfo)) {
                return false;
            }
            PatternInfo patternInfo = (PatternInfo) obj;
            if (!Objects.equals(this.fIntervalPatternFirstPart, patternInfo.fIntervalPatternFirstPart) || !Objects.equals(this.fIntervalPatternSecondPart, patternInfo.fIntervalPatternSecondPart) || this.fFirstDateInPtnIsLaterDate != patternInfo.fFirstDateInPtnIsLaterDate) {
                return false;
            }
            return true;
        }

        @Override // java.lang.Object
        public int hashCode() {
            String str = this.fIntervalPatternFirstPart;
            int hashCode = str != null ? str.hashCode() : 0;
            String str2 = this.fIntervalPatternSecondPart;
            if (str2 != null) {
                hashCode ^= str2.hashCode();
            }
            return this.fFirstDateInPtnIsLaterDate ? ~hashCode : hashCode;
        }

        @Override // java.lang.Object
        public String toString() {
            return "{first=«" + this.fIntervalPatternFirstPart + "», second=«" + this.fIntervalPatternSecondPart + "», reversed:" + this.fFirstDateInPtnIsLaterDate + "}";
        }
    }

    @Deprecated
    public DateIntervalInfo() {
        this.fFirstDateInPtnIsLaterDate = false;
        this.fIntervalPatterns = null;
        this.frozen = false;
        this.fIntervalPatternsReadOnly = false;
        this.fIntervalPatterns = new HashMap();
        this.fFallbackIntervalPattern = "{0} – {1}";
    }

    public DateIntervalInfo(ULocale uLocale) {
        this.fFirstDateInPtnIsLaterDate = false;
        this.fIntervalPatterns = null;
        this.frozen = false;
        this.fIntervalPatternsReadOnly = false;
        initializeData(uLocale);
    }

    public DateIntervalInfo(Locale locale) {
        this(ULocale.forLocale(locale));
    }

    private void initializeData(ULocale uLocale) {
        String uLocale2 = uLocale.toString();
        DateIntervalInfo dateIntervalInfo = DIICACHE.get(uLocale2);
        if (dateIntervalInfo == null) {
            setup(uLocale);
            this.fIntervalPatternsReadOnly = true;
            DIICACHE.put(uLocale2, ((DateIntervalInfo) clone()).freeze());
            return;
        }
        initializeFromReadOnlyPatterns(dateIntervalInfo);
    }

    private void initializeFromReadOnlyPatterns(DateIntervalInfo dateIntervalInfo) {
        this.fFallbackIntervalPattern = dateIntervalInfo.fFallbackIntervalPattern;
        this.fFirstDateInPtnIsLaterDate = dateIntervalInfo.fFirstDateInPtnIsLaterDate;
        this.fIntervalPatterns = dateIntervalInfo.fIntervalPatterns;
        this.fIntervalPatternsReadOnly = true;
    }

    /* access modifiers changed from: private */
    public static final class DateIntervalSink extends UResource.Sink {
        private static final String ACCEPTED_PATTERN_LETTERS = "GyMdahHms";
        private static final String DATE_INTERVAL_PATH_PREFIX = ("/LOCALE/" + DateIntervalInfo.CALENDAR_KEY + PsuedoNames.PSEUDONAME_ROOT);
        private static final String DATE_INTERVAL_PATH_SUFFIX;
        DateIntervalInfo dateIntervalInfo;
        String nextCalendarType;

        public DateIntervalSink(DateIntervalInfo dateIntervalInfo2) {
            this.dateIntervalInfo = dateIntervalInfo2;
        }

        @Override // ohos.global.icu.impl.UResource.Sink
        public void put(UResource.Key key, UResource.Value value, boolean z) {
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                if (key.contentEquals(DateIntervalInfo.INTERVAL_FORMATS_KEY)) {
                    if (value.getType() == 3) {
                        this.nextCalendarType = getCalendarTypeFromPath(value.getAliasString());
                        return;
                    } else if (value.getType() == 2) {
                        UResource.Table table2 = value.getTable();
                        for (int i2 = 0; table2.getKeyAndValue(i2, key, value); i2++) {
                            if (value.getType() == 2) {
                                processSkeletonTable(key, value);
                            }
                        }
                        return;
                    }
                }
            }
        }

        public void processSkeletonTable(UResource.Key key, UResource.Value value) {
            CharSequence validateAndProcessPatternLetter;
            String key2 = key.toString();
            UResource.Table table = value.getTable();
            for (int i = 0; table.getKeyAndValue(i, key, value); i++) {
                if (value.getType() == 0 && (validateAndProcessPatternLetter = validateAndProcessPatternLetter(key)) != null) {
                    setIntervalPatternIfAbsent(key2, validateAndProcessPatternLetter.toString(), value);
                }
            }
        }

        public String getAndResetNextCalendarType() {
            String str = this.nextCalendarType;
            this.nextCalendarType = null;
            return str;
        }

        static {
            StringBuilder sb = new StringBuilder();
            sb.append(PsuedoNames.PSEUDONAME_ROOT);
            sb.append(DateIntervalInfo.INTERVAL_FORMATS_KEY);
            DATE_INTERVAL_PATH_SUFFIX = sb.toString();
        }

        private String getCalendarTypeFromPath(String str) {
            if (str.startsWith(DATE_INTERVAL_PATH_PREFIX) && str.endsWith(DATE_INTERVAL_PATH_SUFFIX)) {
                return str.substring(DATE_INTERVAL_PATH_PREFIX.length(), str.length() - DATE_INTERVAL_PATH_SUFFIX.length());
            }
            throw new ICUException("Malformed 'intervalFormat' alias path: " + str);
        }

        private CharSequence validateAndProcessPatternLetter(CharSequence charSequence) {
            if (charSequence.length() != 1) {
                return null;
            }
            char charAt = charSequence.charAt(0);
            if (ACCEPTED_PATTERN_LETTERS.indexOf(charAt) < 0) {
                return null;
            }
            return charAt == DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[11].charAt(0) ? DateIntervalInfo.CALENDAR_FIELD_TO_PATTERN_LETTER[10] : charSequence;
        }

        private void setIntervalPatternIfAbsent(String str, String str2, UResource.Value value) {
            Map map = (Map) this.dateIntervalInfo.fIntervalPatterns.get(str);
            if (map == null || !map.containsKey(str2)) {
                this.dateIntervalInfo.setIntervalPatternInternally(str, str2, value.toString());
            }
        }
    }

    private void setup(ULocale uLocale) {
        this.fIntervalPatterns = new HashMap(19);
        this.fFallbackIntervalPattern = "{0} – {1}";
        try {
            String keywordValue = uLocale.getKeywordValue("calendar");
            if (keywordValue == null) {
                keywordValue = Calendar.getKeywordValuesForLocale("calendar", uLocale, true)[0];
            }
            if (keywordValue == null) {
                keywordValue = "gregorian";
            }
            DateIntervalSink dateIntervalSink = new DateIntervalSink(this);
            ICUResourceBundle bundleInstance = UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, uLocale);
            setFallbackIntervalPattern(bundleInstance.getStringWithFallback(CALENDAR_KEY + PsuedoNames.PSEUDONAME_ROOT + keywordValue + PsuedoNames.PSEUDONAME_ROOT + INTERVAL_FORMATS_KEY + PsuedoNames.PSEUDONAME_ROOT + FALLBACK_STRING));
            HashSet hashSet = new HashSet();
            while (keywordValue != null) {
                if (!hashSet.contains(keywordValue)) {
                    hashSet.add(keywordValue);
                    bundleInstance.getAllItemsWithFallback(CALENDAR_KEY + PsuedoNames.PSEUDONAME_ROOT + keywordValue, dateIntervalSink);
                    keywordValue = dateIntervalSink.getAndResetNextCalendarType();
                } else {
                    throw new ICUException("Loop in calendar type fallback: " + keywordValue);
                }
            }
        } catch (MissingResourceException unused) {
        }
    }

    private static int splitPatternInto2Part(String str) {
        boolean z;
        int[] iArr = new int[58];
        int i = 0;
        int i2 = 0;
        char c = 0;
        int i3 = 0;
        boolean z2 = false;
        while (true) {
            z = true;
            if (i2 >= str.length()) {
                z = false;
                break;
            }
            char charAt = str.charAt(i2);
            if (charAt != c && i3 > 0) {
                int i4 = c - 'A';
                if (iArr[i4] != 0) {
                    break;
                }
                iArr[i4] = 1;
                i3 = 0;
            }
            if (charAt == '\'') {
                int i5 = i2 + 1;
                if (i5 >= str.length() || str.charAt(i5) != '\'') {
                    z2 = !z2;
                } else {
                    i2 = i5;
                }
            } else if (!z2 && ((charAt >= 'a' && charAt <= 'z') || (charAt >= 'A' && charAt <= 'Z'))) {
                i3++;
                c = charAt;
            }
            i2++;
        }
        if (i3 <= 0 || z || iArr[c - 'A'] != 0) {
            i = i3;
        }
        return i2 - i;
    }

    public void setIntervalPattern(String str, int i, String str2) {
        if (this.frozen) {
            throw new UnsupportedOperationException("no modification is allowed after DII is frozen");
        } else if (i <= 13) {
            if (this.fIntervalPatternsReadOnly) {
                this.fIntervalPatterns = cloneIntervalPatterns(this.fIntervalPatterns);
                this.fIntervalPatternsReadOnly = false;
            }
            PatternInfo intervalPatternInternally = setIntervalPatternInternally(str, CALENDAR_FIELD_TO_PATTERN_LETTER[i], str2);
            if (i == 11) {
                setIntervalPattern(str, CALENDAR_FIELD_TO_PATTERN_LETTER[9], intervalPatternInternally);
                setIntervalPattern(str, CALENDAR_FIELD_TO_PATTERN_LETTER[10], intervalPatternInternally);
            } else if (i == 5 || i == 7) {
                setIntervalPattern(str, CALENDAR_FIELD_TO_PATTERN_LETTER[5], intervalPatternInternally);
            }
        } else {
            throw new IllegalArgumentException("calendar field is larger than MINIMUM_SUPPORTED_CALENDAR_FIELD");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PatternInfo setIntervalPatternInternally(String str, String str2, String str3) {
        boolean z;
        Map<String, PatternInfo> map = this.fIntervalPatterns.get(str);
        boolean z2 = false;
        if (map == null) {
            map = new HashMap<>();
            z = true;
        } else {
            z = false;
        }
        boolean z3 = this.fFirstDateInPtnIsLaterDate;
        if (str3.startsWith(LATEST_FIRST_PREFIX)) {
            str3 = str3.substring(LATEST_FIRST_PREFIX.length(), str3.length());
            z2 = true;
        } else if (str3.startsWith(EARLIEST_FIRST_PREFIX)) {
            str3 = str3.substring(EARLIEST_FIRST_PREFIX.length(), str3.length());
        } else {
            z2 = z3;
        }
        PatternInfo genPatternInfo = genPatternInfo(str3, z2);
        map.put(str2, genPatternInfo);
        if (z) {
            this.fIntervalPatterns.put(str, map);
        }
        return genPatternInfo;
    }

    private void setIntervalPattern(String str, String str2, PatternInfo patternInfo) {
        this.fIntervalPatterns.get(str).put(str2, patternInfo);
    }

    @Deprecated
    public static PatternInfo genPatternInfo(String str, boolean z) {
        int splitPatternInto2Part = splitPatternInto2Part(str);
        return new PatternInfo(str.substring(0, splitPatternInto2Part), splitPatternInto2Part < str.length() ? str.substring(splitPatternInto2Part, str.length()) : null, z);
    }

    public PatternInfo getIntervalPattern(String str, int i) {
        PatternInfo patternInfo;
        if (i <= 13) {
            Map<String, PatternInfo> map = this.fIntervalPatterns.get(str);
            if (map == null || (patternInfo = map.get(CALENDAR_FIELD_TO_PATTERN_LETTER[i])) == null) {
                return null;
            }
            return patternInfo;
        }
        throw new IllegalArgumentException("no support for field less than SECOND");
    }

    public String getFallbackIntervalPattern() {
        return this.fFallbackIntervalPattern;
    }

    public void setFallbackIntervalPattern(String str) {
        if (!this.frozen) {
            int indexOf = str.indexOf("{0}");
            int indexOf2 = str.indexOf("{1}");
            if (indexOf == -1 || indexOf2 == -1) {
                throw new IllegalArgumentException("no pattern {0} or pattern {1} in fallbackPattern");
            }
            if (indexOf > indexOf2) {
                this.fFirstDateInPtnIsLaterDate = true;
            }
            this.fFallbackIntervalPattern = str;
            return;
        }
        throw new UnsupportedOperationException("no modification is allowed after DII is frozen");
    }

    public boolean getDefaultOrder() {
        return this.fFirstDateInPtnIsLaterDate;
    }

    @Override // java.lang.Object
    public Object clone() {
        if (this.frozen) {
            return this;
        }
        return cloneUnfrozenDII();
    }

    private Object cloneUnfrozenDII() {
        try {
            DateIntervalInfo dateIntervalInfo = (DateIntervalInfo) super.clone();
            dateIntervalInfo.fFallbackIntervalPattern = this.fFallbackIntervalPattern;
            dateIntervalInfo.fFirstDateInPtnIsLaterDate = this.fFirstDateInPtnIsLaterDate;
            if (this.fIntervalPatternsReadOnly) {
                dateIntervalInfo.fIntervalPatterns = this.fIntervalPatterns;
                dateIntervalInfo.fIntervalPatternsReadOnly = true;
            } else {
                dateIntervalInfo.fIntervalPatterns = cloneIntervalPatterns(this.fIntervalPatterns);
                dateIntervalInfo.fIntervalPatternsReadOnly = false;
            }
            dateIntervalInfo.frozen = false;
            return dateIntervalInfo;
        } catch (CloneNotSupportedException e) {
            throw new ICUCloneNotSupportedException("clone is not supported", e);
        }
    }

    private static Map<String, Map<String, PatternInfo>> cloneIntervalPatterns(Map<String, Map<String, PatternInfo>> map) {
        HashMap hashMap = new HashMap();
        for (Map.Entry<String, Map<String, PatternInfo>> entry : map.entrySet()) {
            String key = entry.getKey();
            HashMap hashMap2 = new HashMap();
            for (Map.Entry<String, PatternInfo> entry2 : entry.getValue().entrySet()) {
                hashMap2.put(entry2.getKey(), entry2.getValue());
            }
            hashMap.put(key, hashMap2);
        }
        return hashMap;
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public DateIntervalInfo freeze() {
        this.fIntervalPatternsReadOnly = true;
        this.frozen = true;
        return this;
    }

    public DateIntervalInfo cloneAsThawed() {
        return (DateIntervalInfo) cloneUnfrozenDII();
    }

    static void parseSkeleton(String str, int[] iArr) {
        for (int i = 0; i < str.length(); i++) {
            int charAt = str.charAt(i) - 'A';
            iArr[charAt] = iArr[charAt] + 1;
        }
    }

    /* access modifiers changed from: package-private */
    public DateIntervalFormat.BestMatchInfo getBestSkeleton(String str) {
        boolean z;
        String str2;
        String str3 = str;
        int[] iArr = new int[58];
        int[] iArr2 = new int[58];
        if (str3.indexOf(122) != -1) {
            str2 = str3.replace('z', 'v');
            z = true;
        } else {
            str2 = str3;
            z = false;
        }
        parseSkeleton(str2, iArr);
        Iterator<String> it = this.fIntervalPatterns.keySet().iterator();
        int i = Integer.MAX_VALUE;
        int i2 = 0;
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            String next = it.next();
            for (int i3 = 0; i3 < iArr2.length; i3++) {
                iArr2[i3] = 0;
            }
            parseSkeleton(next, iArr2);
            int i4 = 0;
            int i5 = 1;
            for (int i6 = 0; i6 < iArr.length; i6++) {
                int i7 = iArr[i6];
                int i8 = iArr2[i6];
                if (i7 != i8) {
                    if (i7 == 0 || i8 == 0) {
                        i4 += 4096;
                        i5 = -1;
                    } else if (stringNumeric(i7, i8, (char) (i6 + 65))) {
                        i4 += 256;
                    } else {
                        i4 += Math.abs(i7 - i8);
                    }
                }
            }
            if (i4 < i) {
                str3 = next;
                i = i4;
                i2 = i5;
                continue;
            }
            if (i4 == 0) {
                i2 = 0;
                break;
            }
        }
        if (z && i2 != -1) {
            i2 = 2;
        }
        return new DateIntervalFormat.BestMatchInfo(str3, i2);
    }

    @Override // java.lang.Object
    public boolean equals(Object obj) {
        if (obj instanceof DateIntervalInfo) {
            return this.fIntervalPatterns.equals(((DateIntervalInfo) obj).fIntervalPatterns);
        }
        return false;
    }

    @Override // java.lang.Object
    public int hashCode() {
        return this.fIntervalPatterns.hashCode();
    }

    @Deprecated
    public Map<String, Set<String>> getPatterns() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Map.Entry<String, Map<String, PatternInfo>> entry : this.fIntervalPatterns.entrySet()) {
            linkedHashMap.put(entry.getKey(), new LinkedHashSet(entry.getValue().keySet()));
        }
        return linkedHashMap;
    }

    @Deprecated
    public Map<String, Map<String, PatternInfo>> getRawPatterns() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        for (Map.Entry<String, Map<String, PatternInfo>> entry : this.fIntervalPatterns.entrySet()) {
            linkedHashMap.put(entry.getKey(), new LinkedHashMap(entry.getValue()));
        }
        return linkedHashMap;
    }
}
