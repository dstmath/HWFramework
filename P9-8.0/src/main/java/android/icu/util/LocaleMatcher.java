package android.icu.util;

import android.icu.impl.ICUData;
import android.icu.impl.ICUResourceBundle;
import android.icu.impl.Relation;
import android.icu.impl.Row;
import android.icu.impl.Row.R3;
import android.icu.impl.Utility;
import android.icu.impl.locale.BaseLocale;
import android.icu.impl.locale.LanguageTag;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocaleMatcher {
    @Deprecated
    public static final boolean DEBUG = false;
    private static final double DEFAULT_THRESHOLD = 0.5d;
    private static final ULocale UNKNOWN_LOCALE = new ULocale("und");
    private static HashMap<String, String> canonicalMap = new HashMap();
    private static final LanguageMatcherData defaultWritten = new LanguageMatcherData();
    private final ULocale defaultLanguage;
    Map<String, Set<R3<ULocale, ULocale, Double>>> desiredLanguageToPossibleLocalesToMaxLocaleToData;
    Set<R3<ULocale, ULocale, Double>> localeToMaxLocaleAndWeight;
    LanguageMatcherData matcherData;
    private final double threshold;

    @Deprecated
    public static class LanguageMatcherData implements Freezable<LanguageMatcherData> {
        private static final /* synthetic */ int[] -android-icu-util-LocaleMatcher$LevelSwitchesValues = null;
        private volatile boolean frozen = false;
        private ScoreData languageScores = new ScoreData(Level.language);
        private Relation<String, String> matchingLanguages;
        private ScoreData regionScores = new ScoreData(Level.region);
        private ScoreData scriptScores = new ScoreData(Level.script);

        private static /* synthetic */ int[] -getandroid-icu-util-LocaleMatcher$LevelSwitchesValues() {
            if (-android-icu-util-LocaleMatcher$LevelSwitchesValues != null) {
                return -android-icu-util-LocaleMatcher$LevelSwitchesValues;
            }
            int[] iArr = new int[Level.values().length];
            try {
                iArr[Level.language.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[Level.region.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[Level.script.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            -android-icu-util-LocaleMatcher$LevelSwitchesValues = iArr;
            return iArr;
        }

        @Deprecated
        public Relation<String, String> matchingLanguages() {
            return this.matchingLanguages;
        }

        @Deprecated
        public String toString() {
            return this.languageScores + "\n\t" + this.scriptScores + "\n\t" + this.regionScores;
        }

        @Deprecated
        public double match(ULocale a, ULocale aMax, ULocale b, ULocale bMax) {
            double diff = 0.0d + this.languageScores.getScore(aMax, a.getLanguage(), aMax.getLanguage(), bMax, b.getLanguage(), bMax.getLanguage());
            if (diff > 0.999d) {
                return 0.0d;
            }
            diff = (diff + this.scriptScores.getScore(aMax, a.getScript(), aMax.getScript(), bMax, b.getScript(), bMax.getScript())) + this.regionScores.getScore(aMax, a.getCountry(), aMax.getCountry(), bMax, b.getCountry(), bMax.getCountry());
            if (!a.getVariant().equals(b.getVariant())) {
                diff += 0.01d;
            }
            if (diff < 0.0d) {
                diff = 0.0d;
            } else if (diff > 1.0d) {
                diff = 1.0d;
            }
            return 1.0d - diff;
        }

        @Deprecated
        public LanguageMatcherData addDistance(String desired, String supported, int percent, String comment) {
            return addDistance(desired, supported, percent, false, comment);
        }

        @Deprecated
        public LanguageMatcherData addDistance(String desired, String supported, int percent, boolean oneway) {
            return addDistance(desired, supported, percent, oneway, null);
        }

        private LanguageMatcherData addDistance(String desired, String supported, int percent, boolean oneway, String comment) {
            double score = 1.0d - (((double) percent) / 100.0d);
            LocalePatternMatcher desiredMatcher = new LocalePatternMatcher(desired);
            Level desiredLen = desiredMatcher.getLevel();
            LocalePatternMatcher localePatternMatcher = new LocalePatternMatcher(supported);
            if (desiredLen != localePatternMatcher.getLevel()) {
                throw new IllegalArgumentException("Lengths unequal: " + desired + ", " + supported);
            }
            R3<LocalePatternMatcher, LocalePatternMatcher, Double> data = Row.of(desiredMatcher, localePatternMatcher, Double.valueOf(score));
            R3 data2 = oneway ? null : Row.of(localePatternMatcher, desiredMatcher, Double.valueOf(score));
            boolean desiredEqualsSupported = desiredMatcher.equals(localePatternMatcher);
            switch (-getandroid-icu-util-LocaleMatcher$LevelSwitchesValues()[desiredLen.ordinal()]) {
                case 1:
                    String dlanguage = desiredMatcher.getLanguage();
                    String slanguage = localePatternMatcher.getLanguage();
                    this.languageScores.addDataToScores(dlanguage, slanguage, data);
                    if (!(oneway || (desiredEqualsSupported ^ 1) == 0)) {
                        this.languageScores.addDataToScores(slanguage, dlanguage, data2);
                        break;
                    }
                case 2:
                    String dregion = desiredMatcher.getRegion();
                    String sregion = localePatternMatcher.getRegion();
                    this.regionScores.addDataToScores(dregion, sregion, data);
                    if (!(oneway || (desiredEqualsSupported ^ 1) == 0)) {
                        this.regionScores.addDataToScores(sregion, dregion, data2);
                        break;
                    }
                case 3:
                    String dscript = desiredMatcher.getScript();
                    String sscript = localePatternMatcher.getScript();
                    this.scriptScores.addDataToScores(dscript, sscript, data);
                    if (!(oneway || (desiredEqualsSupported ^ 1) == 0)) {
                        this.scriptScores.addDataToScores(sscript, dscript, data2);
                        break;
                    }
            }
            return this;
        }

        @Deprecated
        public LanguageMatcherData cloneAsThawed() {
            try {
                LanguageMatcherData result = (LanguageMatcherData) clone();
                result.languageScores = this.languageScores.cloneAsThawed();
                result.scriptScores = this.scriptScores.cloneAsThawed();
                result.regionScores = this.regionScores.cloneAsThawed();
                result.frozen = false;
                return result;
            } catch (Throwable e) {
                throw new ICUCloneNotSupportedException(e);
            }
        }

        @Deprecated
        public LanguageMatcherData freeze() {
            this.languageScores.freeze();
            this.regionScores.freeze();
            this.scriptScores.freeze();
            this.matchingLanguages = this.languageScores.getMatchingLanguages();
            this.frozen = true;
            return this;
        }

        @Deprecated
        public boolean isFrozen() {
            return this.frozen;
        }
    }

    enum Level {
        language(0.99d),
        script(0.2d),
        region(0.04d);
        
        final double worst;

        private Level(double d) {
            this.worst = d;
        }
    }

    private static class LocalePatternMatcher {
        static Pattern pattern = Pattern.compile("([a-z]{1,8}|\\*)(?:[_-]([A-Z][a-z]{3}|\\*))?(?:[_-]([A-Z]{2}|[0-9]{3}|\\*))?");
        private String lang;
        private Level level;
        private String region;
        private String script;

        public LocalePatternMatcher(String toMatch) {
            Matcher matcher = pattern.matcher(toMatch);
            if (matcher.matches()) {
                this.lang = matcher.group(1);
                this.script = matcher.group(2);
                this.region = matcher.group(3);
                Level level = this.region != null ? Level.region : this.script != null ? Level.script : Level.language;
                this.level = level;
                if (this.lang.equals("*")) {
                    this.lang = null;
                }
                if (this.script != null && this.script.equals("*")) {
                    this.script = null;
                }
                if (this.region != null && this.region.equals("*")) {
                    this.region = null;
                    return;
                }
                return;
            }
            throw new IllegalArgumentException("Bad pattern: " + toMatch);
        }

        boolean matches(ULocale ulocale) {
            if (this.lang != null && (this.lang.equals(ulocale.getLanguage()) ^ 1) != 0) {
                return false;
            }
            if (this.script != null && (this.script.equals(ulocale.getScript()) ^ 1) != 0) {
                return false;
            }
            if (this.region == null || (this.region.equals(ulocale.getCountry()) ^ 1) == 0) {
                return true;
            }
            return false;
        }

        public Level getLevel() {
            return this.level;
        }

        public String getLanguage() {
            return this.lang == null ? "*" : this.lang;
        }

        public String getScript() {
            return this.script == null ? "*" : this.script;
        }

        public String getRegion() {
            return this.region == null ? "*" : this.region;
        }

        public String toString() {
            String result = getLanguage();
            if (this.level == Level.language) {
                return result;
            }
            result = result + LanguageTag.SEP + getScript();
            if (this.level != Level.script) {
                return result + LanguageTag.SEP + getRegion();
            }
            return result;
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (obj == this) {
                return true;
            }
            if (obj == null || ((obj instanceof LocalePatternMatcher) ^ 1) != 0) {
                return false;
            }
            LocalePatternMatcher other = (LocalePatternMatcher) obj;
            if (Utility.objectEquals(this.level, other.level) && Utility.objectEquals(this.lang, other.lang) && Utility.objectEquals(this.script, other.script)) {
                z = Utility.objectEquals(this.region, other.region);
            }
            return z;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = (this.script == null ? 0 : this.script.hashCode()) ^ (this.level.ordinal() ^ (this.lang == null ? 0 : this.lang.hashCode()));
            if (this.region != null) {
                i = this.region.hashCode();
            }
            return hashCode ^ i;
        }
    }

    @Deprecated
    private static class OutputDouble {
        double value;

        /* synthetic */ OutputDouble(OutputDouble -this0) {
            this();
        }

        private OutputDouble() {
        }
    }

    private static class ScoreData implements Freezable<ScoreData> {
        private static final double maxUnequal_changeD_sameS = 0.5d;
        private static final double maxUnequal_changeEqual = 0.75d;
        private volatile boolean frozen = false;
        final Level level;
        LinkedHashSet<R3<LocalePatternMatcher, LocalePatternMatcher, Double>> scores = new LinkedHashSet();

        public ScoreData(Level level) {
            this.level = level;
        }

        void addDataToScores(String desired, String supported, R3<LocalePatternMatcher, LocalePatternMatcher, Double> data) {
            if (!this.scores.add(data)) {
                throw new ICUException("trying to add duplicate data: " + data);
            }
        }

        double getScore(ULocale dMax, String desiredRaw, String desiredMax, ULocale sMax, String supportedRaw, String supportedMax) {
            if (!desiredMax.equals(supportedMax)) {
                return getRawScore(dMax, sMax);
            }
            if (desiredRaw.equals(supportedRaw)) {
                return 0.0d;
            }
            return 0.001d;
        }

        private double getRawScore(ULocale desiredLocale, ULocale supportedLocale) {
            for (R3<LocalePatternMatcher, LocalePatternMatcher, Double> datum : this.scores) {
                if (((LocalePatternMatcher) datum.get0()).matches(desiredLocale) && ((LocalePatternMatcher) datum.get1()).matches(supportedLocale)) {
                    return ((Double) datum.get2()).doubleValue();
                }
            }
            return this.level.worst;
        }

        public String toString() {
            StringBuilder result = new StringBuilder().append(this.level);
            for (R3<LocalePatternMatcher, LocalePatternMatcher, Double> score : this.scores) {
                result.append("\n\t\t").append(score);
            }
            return result.toString();
        }

        public ScoreData cloneAsThawed() {
            try {
                ScoreData result = (ScoreData) clone();
                result.scores = (LinkedHashSet) result.scores.clone();
                result.frozen = false;
                return result;
            } catch (Throwable e) {
                throw new ICUCloneNotSupportedException(e);
            }
        }

        public ScoreData freeze() {
            return this;
        }

        public boolean isFrozen() {
            return this.frozen;
        }

        public Relation<String, String> getMatchingLanguages() {
            Relation<String, String> desiredToSupported = Relation.of(new LinkedHashMap(), HashSet.class);
            for (R3<LocalePatternMatcher, LocalePatternMatcher, Double> item : this.scores) {
                LocalePatternMatcher desired = (LocalePatternMatcher) item.get0();
                LocalePatternMatcher supported = (LocalePatternMatcher) item.get1();
                if (!(desired.lang == null || supported.lang == null)) {
                    desiredToSupported.put(desired.lang, supported.lang);
                }
            }
            desiredToSupported.freeze();
            return desiredToSupported;
        }
    }

    static {
        canonicalMap.put("iw", "he");
        canonicalMap.put("mo", "ro");
        canonicalMap.put("tl", "fil");
        ICUResourceBundle written = (ICUResourceBundle) getICUSupplementalData().findTopLevel("languageMatching").get("written");
        UResourceBundleIterator iter = written.getIterator();
        while (iter.hasNext()) {
            ICUResourceBundle item = (ICUResourceBundle) iter.next();
            defaultWritten.addDistance(item.getString(0), item.getString(1), Integer.parseInt(item.getString(2)), item.getSize() > 3 ? "1".equals(item.getString(3)) : false);
        }
        defaultWritten.freeze();
    }

    public LocaleMatcher(LocalePriorityList languagePriorityList) {
        this(languagePriorityList, defaultWritten);
    }

    public LocaleMatcher(String languagePriorityListString) {
        this(LocalePriorityList.add(languagePriorityListString).build());
    }

    @Deprecated
    public LocaleMatcher(LocalePriorityList languagePriorityList, LanguageMatcherData matcherData) {
        this(languagePriorityList, matcherData, DEFAULT_THRESHOLD);
    }

    @Deprecated
    public LocaleMatcher(LocalePriorityList languagePriorityList, LanguageMatcherData matcherData, double threshold) {
        ULocale uLocale;
        this.localeToMaxLocaleAndWeight = new LinkedHashSet();
        this.desiredLanguageToPossibleLocalesToMaxLocaleToData = new LinkedHashMap();
        this.matcherData = matcherData == null ? defaultWritten : matcherData.freeze();
        for (ULocale language : languagePriorityList) {
            add(language, languagePriorityList.getWeight(language));
        }
        processMapping();
        Iterator<ULocale> it = languagePriorityList.iterator();
        if (it.hasNext()) {
            uLocale = (ULocale) it.next();
        } else {
            uLocale = null;
        }
        this.defaultLanguage = uLocale;
        this.threshold = threshold;
    }

    public double match(ULocale desired, ULocale desiredMax, ULocale supported, ULocale supportedMax) {
        return this.matcherData.match(desired, desiredMax, supported, supportedMax);
    }

    public ULocale canonicalize(ULocale ulocale) {
        String lang = ulocale.getLanguage();
        String lang2 = (String) canonicalMap.get(lang);
        String script = ulocale.getScript();
        String script2 = (String) canonicalMap.get(script);
        String region = ulocale.getCountry();
        String region2 = (String) canonicalMap.get(region);
        if (lang2 == null && script2 == null && region2 == null) {
            return ulocale;
        }
        if (lang2 != null) {
            lang = lang2;
        }
        if (script2 != null) {
            script = script2;
        }
        if (region2 != null) {
            region = region2;
        }
        return new ULocale(lang, script, region);
    }

    public ULocale getBestMatch(LocalePriorityList languageList) {
        double bestWeight = 0.0d;
        ULocale bestTableMatch = null;
        double penalty = 0.0d;
        OutputDouble matchWeight = new OutputDouble();
        for (ULocale language : languageList) {
            ULocale matchLocale = getBestMatchInternal(language, matchWeight);
            double weight = (matchWeight.value * languageList.getWeight(language).doubleValue()) - penalty;
            if (weight > bestWeight) {
                bestWeight = weight;
                bestTableMatch = matchLocale;
            }
            penalty += 0.07000001d;
        }
        if (bestWeight < this.threshold) {
            return this.defaultLanguage;
        }
        return bestTableMatch;
    }

    public ULocale getBestMatch(String languageList) {
        return getBestMatch(LocalePriorityList.add(languageList).build());
    }

    public ULocale getBestMatch(ULocale ulocale) {
        return getBestMatchInternal(ulocale, null);
    }

    @Deprecated
    public ULocale getBestMatch(ULocale... ulocales) {
        return getBestMatch(LocalePriorityList.add(ulocales).build());
    }

    public String toString() {
        return "{" + this.defaultLanguage + ", " + this.localeToMaxLocaleAndWeight + "}";
    }

    private ULocale getBestMatchInternal(ULocale languageCode, OutputDouble outputWeight) {
        languageCode = canonicalize(languageCode);
        ULocale maximized = addLikelySubtags(languageCode);
        double bestWeight = 0.0d;
        ULocale bestTableMatch = null;
        Set<R3<ULocale, ULocale, Double>> searchTable = (Set) this.desiredLanguageToPossibleLocalesToMaxLocaleToData.get(maximized.getLanguage());
        if (searchTable != null) {
            for (R3<ULocale, ULocale, Double> tableKeyValue : searchTable) {
                ULocale tableKey = (ULocale) tableKeyValue.get0();
                double weight = match(languageCode, maximized, tableKey, (ULocale) tableKeyValue.get1()) * ((Double) tableKeyValue.get2()).doubleValue();
                if (weight > bestWeight) {
                    bestWeight = weight;
                    bestTableMatch = tableKey;
                    if (weight > 0.999d) {
                        break;
                    }
                }
            }
        }
        if (bestWeight < this.threshold) {
            bestTableMatch = this.defaultLanguage;
        }
        if (outputWeight != null) {
            outputWeight.value = bestWeight;
        }
        return bestTableMatch;
    }

    private void add(ULocale language, Double weight) {
        language = canonicalize(language);
        R3<ULocale, ULocale, Double> row = Row.of(language, addLikelySubtags(language), weight);
        row.freeze();
        this.localeToMaxLocaleAndWeight.add(row);
    }

    private void processMapping() {
        for (Entry<String, Set<String>> desiredToMatchingLanguages : this.matcherData.matchingLanguages().keyValuesSet()) {
            String desired = (String) desiredToMatchingLanguages.getKey();
            Set<String> supported = (Set) desiredToMatchingLanguages.getValue();
            for (R3<ULocale, ULocale, Double> localeToMaxAndWeight : this.localeToMaxLocaleAndWeight) {
                if (supported.contains(((ULocale) localeToMaxAndWeight.get0()).getLanguage())) {
                    addFiltered(desired, localeToMaxAndWeight);
                }
            }
        }
        for (R3<ULocale, ULocale, Double> localeToMaxAndWeight2 : this.localeToMaxLocaleAndWeight) {
            addFiltered(((ULocale) localeToMaxAndWeight2.get0()).getLanguage(), localeToMaxAndWeight2);
        }
    }

    private void addFiltered(String desired, R3<ULocale, ULocale, Double> localeToMaxAndWeight) {
        Set<R3<ULocale, ULocale, Double>> map = (Set) this.desiredLanguageToPossibleLocalesToMaxLocaleToData.get(desired);
        if (map == null) {
            Map map2 = this.desiredLanguageToPossibleLocalesToMaxLocaleToData;
            map = new LinkedHashSet();
            map2.put(desired, map);
        }
        map.add(localeToMaxAndWeight);
    }

    private ULocale addLikelySubtags(ULocale languageCode) {
        if (languageCode.equals(UNKNOWN_LOCALE)) {
            return UNKNOWN_LOCALE;
        }
        ULocale result = ULocale.addLikelySubtags(languageCode);
        if (result != null && !result.equals(languageCode)) {
            return result;
        }
        String language = languageCode.getLanguage();
        String script = languageCode.getScript();
        String region = languageCode.getCountry();
        StringBuilder stringBuilder = new StringBuilder();
        if (language.length() == 0) {
            language = "und";
        }
        stringBuilder = stringBuilder.append(language).append(BaseLocale.SEP);
        if (script.length() == 0) {
            script = "Zzzz";
        }
        stringBuilder = stringBuilder.append(script).append(BaseLocale.SEP);
        if (region.length() == 0) {
            region = "ZZ";
        }
        return new ULocale(stringBuilder.append(region).toString());
    }

    @Deprecated
    public static ICUResourceBundle getICUSupplementalData() {
        return (ICUResourceBundle) UResourceBundle.getBundleInstance(ICUData.ICU_BASE_NAME, "supplementalData", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
    }

    @Deprecated
    public static double match(ULocale a, ULocale b) {
        LocaleMatcher matcher = new LocaleMatcher("");
        return matcher.match(a, matcher.addLikelySubtags(a), b, matcher.addLikelySubtags(b));
    }
}
