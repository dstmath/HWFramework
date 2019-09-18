package android.icu.impl.locale;

import android.icu.impl.locale.XCldrStub;
import android.icu.impl.locale.XLikelySubtags;
import android.icu.impl.locale.XLocaleDistance;
import android.icu.util.LocalePriorityList;
import android.icu.util.Output;
import android.icu.util.ULocale;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class XLocaleMatcher {
    private static final XLikelySubtags.LSR UND = new XLikelySubtags.LSR("und", "", "");
    private static final ULocale UND_LOCALE = new ULocale("und");
    private final ULocale defaultLanguage;
    private final int demotionPerAdditionalDesiredLocale;
    private final XLocaleDistance.DistanceOption distanceOption;
    private final Set<ULocale> exactSupportedLocales;
    private final XLocaleDistance localeDistance;
    private final Map<XLikelySubtags.LSR, Set<ULocale>> supportedLanguages;
    private final int thresholdDistance;

    public static class Builder {
        /* access modifiers changed from: private */
        public ULocale defaultLanguage;
        /* access modifiers changed from: private */
        public int demotionPerAdditionalDesiredLocale = -1;
        /* access modifiers changed from: private */
        public XLocaleDistance.DistanceOption distanceOption;
        /* access modifiers changed from: private */
        public XLocaleDistance localeDistance;
        /* access modifiers changed from: private */
        public Set<ULocale> supportedLanguagesList;
        /* access modifiers changed from: private */
        public int thresholdDistance = -1;

        public Builder setSupportedLocales(String languagePriorityList) {
            this.supportedLanguagesList = XLocaleMatcher.asSet(LocalePriorityList.add(languagePriorityList).build());
            return this;
        }

        public Builder setSupportedLocales(LocalePriorityList languagePriorityList) {
            this.supportedLanguagesList = XLocaleMatcher.asSet(languagePriorityList);
            return this;
        }

        public Builder setSupportedLocales(Set<ULocale> languagePriorityList) {
            this.supportedLanguagesList = languagePriorityList;
            return this;
        }

        public Builder setThresholdDistance(int thresholdDistance2) {
            this.thresholdDistance = thresholdDistance2;
            return this;
        }

        public Builder setDemotionPerAdditionalDesiredLocale(int demotionPerAdditionalDesiredLocale2) {
            this.demotionPerAdditionalDesiredLocale = demotionPerAdditionalDesiredLocale2;
            return this;
        }

        public Builder setLocaleDistance(XLocaleDistance localeDistance2) {
            this.localeDistance = localeDistance2;
            return this;
        }

        public Builder setDefaultLanguage(ULocale defaultLanguage2) {
            this.defaultLanguage = defaultLanguage2;
            return this;
        }

        public Builder setDistanceOption(XLocaleDistance.DistanceOption distanceOption2) {
            this.distanceOption = distanceOption2;
            return this;
        }

        public XLocaleMatcher build() {
            return new XLocaleMatcher(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public XLocaleMatcher(String supportedLocales) {
        this(builder().setSupportedLocales(supportedLocales));
    }

    public XLocaleMatcher(LocalePriorityList supportedLocales) {
        this(builder().setSupportedLocales(supportedLocales));
    }

    public XLocaleMatcher(Set<ULocale> supportedLocales) {
        this(builder().setSupportedLocales(supportedLocales));
    }

    private XLocaleMatcher(Builder builder) {
        XLocaleDistance xLocaleDistance;
        int i;
        ULocale uLocale;
        int i2;
        if (builder.localeDistance == null) {
            xLocaleDistance = XLocaleDistance.getDefault();
        } else {
            xLocaleDistance = builder.localeDistance;
        }
        this.localeDistance = xLocaleDistance;
        if (builder.thresholdDistance < 0) {
            i = this.localeDistance.getDefaultScriptDistance();
        } else {
            i = builder.thresholdDistance;
        }
        this.thresholdDistance = i;
        XCldrStub.Multimap<XLikelySubtags.LSR, ULocale> temp2 = extractLsrMap(builder.supportedLanguagesList, extractLsrSet(this.localeDistance.getParadigms()));
        this.supportedLanguages = temp2.asMap();
        this.exactSupportedLocales = XCldrStub.ImmutableSet.copyOf(temp2.values());
        if (builder.defaultLanguage != null) {
            uLocale = builder.defaultLanguage;
        } else if (this.supportedLanguages.isEmpty()) {
            uLocale = null;
        } else {
            uLocale = (ULocale) ((Set) this.supportedLanguages.entrySet().iterator().next().getValue()).iterator().next();
        }
        this.defaultLanguage = uLocale;
        if (builder.demotionPerAdditionalDesiredLocale < 0) {
            i2 = this.localeDistance.getDefaultRegionDistance() + 1;
        } else {
            i2 = builder.demotionPerAdditionalDesiredLocale;
        }
        this.demotionPerAdditionalDesiredLocale = i2;
        this.distanceOption = builder.distanceOption;
    }

    private Set<XLikelySubtags.LSR> extractLsrSet(Set<ULocale> languagePriorityList) {
        Set<XLikelySubtags.LSR> result = new LinkedHashSet<>();
        for (ULocale item : languagePriorityList) {
            result.add(item.equals(UND_LOCALE) ? UND : XLikelySubtags.LSR.fromMaximalized(item));
        }
        return result;
    }

    private XCldrStub.Multimap<XLikelySubtags.LSR, ULocale> extractLsrMap(Set<ULocale> languagePriorityList, Set<XLikelySubtags.LSR> priorities) {
        XCldrStub.Multimap<XLikelySubtags.LSR, ULocale> builder = XCldrStub.LinkedHashMultimap.create();
        for (ULocale item : languagePriorityList) {
            builder.put(item.equals(UND_LOCALE) ? UND : XLikelySubtags.LSR.fromMaximalized(item), item);
        }
        if (builder.size() > 1 && priorities != null) {
            XCldrStub.Multimap<XLikelySubtags.LSR, ULocale> builder2 = XCldrStub.LinkedHashMultimap.create();
            boolean first = true;
            for (Map.Entry<XLikelySubtags.LSR, Set<ULocale>> entry : builder.asMap().entrySet()) {
                XLikelySubtags.LSR key = entry.getKey();
                if (first || priorities.contains(key)) {
                    builder2.putAll(key, (Collection<ULocale>) entry.getValue());
                    first = false;
                }
            }
            builder2.putAll(builder);
            if (builder2.equals(builder)) {
                builder = builder2;
            } else {
                throw new IllegalArgumentException();
            }
        }
        return XCldrStub.ImmutableMultimap.copyOf(builder);
    }

    public ULocale getBestMatch(ULocale ulocale) {
        return getBestMatch(ulocale, (Output<ULocale>) null);
    }

    public ULocale getBestMatch(String languageList) {
        return getBestMatch(LocalePriorityList.add(languageList).build(), (Output<ULocale>) null);
    }

    public ULocale getBestMatch(ULocale... locales) {
        return getBestMatch((Set<ULocale>) new LinkedHashSet(Arrays.asList(locales)), (Output<ULocale>) null);
    }

    public ULocale getBestMatch(Set<ULocale> desiredLanguages) {
        return getBestMatch(desiredLanguages, (Output<ULocale>) null);
    }

    public ULocale getBestMatch(LocalePriorityList desiredLanguages) {
        return getBestMatch(desiredLanguages, (Output<ULocale>) null);
    }

    public ULocale getBestMatch(LocalePriorityList desiredLanguages, Output<ULocale> outputBestDesired) {
        return getBestMatch(asSet(desiredLanguages), outputBestDesired);
    }

    /* access modifiers changed from: private */
    public static Set<ULocale> asSet(LocalePriorityList languageList) {
        Set<ULocale> temp = new LinkedHashSet<>();
        Iterator<ULocale> it = languageList.iterator();
        while (it.hasNext()) {
            temp.add(it.next());
        }
        return temp;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v5, resolved type: java.util.Collection} */
    /* JADX WARNING: Multi-variable type inference failed */
    public ULocale getBestMatch(Set<ULocale> desiredLanguages, Output<ULocale> outputBestDesired) {
        Output<ULocale> output = outputBestDesired;
        if (desiredLanguages.size() == 1) {
            return getBestMatch(desiredLanguages.iterator().next(), output);
        }
        XCldrStub.Multimap<XLikelySubtags.LSR, ULocale> desiredLSRs = extractLsrMap(desiredLanguages, null);
        int bestDistance = Integer.MAX_VALUE;
        ULocale bestDesiredLocale = null;
        Collection<ULocale> bestSupportedLocales = null;
        int delta = 0;
        Iterator<Map.Entry<XLikelySubtags.LSR, ULocale>> it = desiredLSRs.entries().iterator();
        loop0:
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            Map.Entry<XLikelySubtags.LSR, ULocale> desiredLsrAndLocale = it.next();
            ULocale desiredLocale = desiredLsrAndLocale.getValue();
            XLikelySubtags.LSR desiredLSR = desiredLsrAndLocale.getKey();
            if (delta < bestDistance) {
                if (this.exactSupportedLocales.contains(desiredLocale)) {
                    if (output != null) {
                        output.value = desiredLocale;
                    }
                    return desiredLocale;
                }
                Collection<ULocale> found = this.supportedLanguages.get(desiredLSR);
                if (found != null) {
                    if (output != null) {
                        output.value = desiredLocale;
                    }
                    return found.iterator().next();
                }
            }
            for (Map.Entry<XLikelySubtags.LSR, Set<ULocale>> supportedLsrAndLocale : this.supportedLanguages.entrySet()) {
                XCldrStub.Multimap<XLikelySubtags.LSR, ULocale> desiredLSRs2 = desiredLSRs;
                int distance = this.localeDistance.distanceRaw(desiredLSR, supportedLsrAndLocale.getKey(), this.thresholdDistance, this.distanceOption) + delta;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    bestDesiredLocale = desiredLocale;
                    bestSupportedLocales = supportedLsrAndLocale.getValue();
                    if (distance == 0) {
                        break loop0;
                    }
                }
                desiredLSRs = desiredLSRs2;
                Set<ULocale> set = desiredLanguages;
            }
            delta += this.demotionPerAdditionalDesiredLocale;
            Set<ULocale> set2 = desiredLanguages;
        }
        if (bestDistance >= this.thresholdDistance) {
            if (output != null) {
                output.value = null;
            }
            return this.defaultLanguage;
        }
        if (output != null) {
            output.value = bestDesiredLocale;
        }
        if (bestSupportedLocales.contains(bestDesiredLocale)) {
            return bestDesiredLocale;
        }
        return bestSupportedLocales.iterator().next();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r7v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v4, resolved type: java.util.Collection} */
    /* JADX WARNING: Multi-variable type inference failed */
    public ULocale getBestMatch(ULocale desiredLocale, Output<ULocale> outputBestDesired) {
        int bestDistance = Integer.MAX_VALUE;
        ULocale bestDesiredLocale = null;
        Collection<ULocale> bestSupportedLocales = null;
        XLikelySubtags.LSR desiredLSR = desiredLocale.equals(UND_LOCALE) ? UND : XLikelySubtags.LSR.fromMaximalized(desiredLocale);
        if (this.exactSupportedLocales.contains(desiredLocale)) {
            if (outputBestDesired != null) {
                outputBestDesired.value = desiredLocale;
            }
            return desiredLocale;
        }
        if (this.distanceOption == XLocaleDistance.DistanceOption.NORMAL) {
            Collection<ULocale> found = this.supportedLanguages.get(desiredLSR);
            if (found != null) {
                if (outputBestDesired != null) {
                    outputBestDesired.value = desiredLocale;
                }
                return found.iterator().next();
            }
        }
        for (Map.Entry<XLikelySubtags.LSR, Set<ULocale>> supportedLsrAndLocale : this.supportedLanguages.entrySet()) {
            int distance = this.localeDistance.distanceRaw(desiredLSR, supportedLsrAndLocale.getKey(), this.thresholdDistance, this.distanceOption);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestDesiredLocale = desiredLocale;
                bestSupportedLocales = supportedLsrAndLocale.getValue();
                if (distance == 0) {
                    break;
                }
            }
        }
        if (bestDistance >= this.thresholdDistance) {
            if (outputBestDesired != null) {
                outputBestDesired.value = null;
            }
            return this.defaultLanguage;
        }
        if (outputBestDesired != null) {
            outputBestDesired.value = bestDesiredLocale;
        }
        if (bestSupportedLocales.contains(bestDesiredLocale)) {
            return bestDesiredLocale;
        }
        return bestSupportedLocales.iterator().next();
    }

    public static ULocale combine(ULocale bestSupported, ULocale bestDesired) {
        if (bestSupported.equals(bestDesired) || bestDesired == null) {
            return bestSupported;
        }
        ULocale.Builder b = new ULocale.Builder().setLocale(bestSupported);
        String region = bestDesired.getCountry();
        if (!region.isEmpty()) {
            b.setRegion(region);
        }
        String variants = bestDesired.getVariant();
        if (!variants.isEmpty()) {
            b.setVariant(variants);
        }
        for (Character charValue : bestDesired.getExtensionKeys()) {
            char extensionKey = charValue.charValue();
            b.setExtension(extensionKey, bestDesired.getExtension(extensionKey));
        }
        return b.build();
    }

    public int distance(ULocale desired, ULocale supported) {
        return this.localeDistance.distanceRaw(XLikelySubtags.LSR.fromMaximalized(desired), XLikelySubtags.LSR.fromMaximalized(supported), this.thresholdDistance, this.distanceOption);
    }

    public int distance(String desiredLanguage, String supportedLanguage) {
        return this.localeDistance.distanceRaw(XLikelySubtags.LSR.fromMaximalized(new ULocale(desiredLanguage)), XLikelySubtags.LSR.fromMaximalized(new ULocale(supportedLanguage)), this.thresholdDistance, this.distanceOption);
    }

    public String toString() {
        return this.exactSupportedLocales.toString();
    }

    public double match(ULocale desired, ULocale supported) {
        return ((double) (100 - distance(desired, supported))) / 100.0d;
    }

    @Deprecated
    public double match(ULocale desired, ULocale desiredMax, ULocale supported, ULocale supportedMax) {
        return match(desired, supported);
    }

    public ULocale canonicalize(ULocale ulocale) {
        return null;
    }

    public int getThresholdDistance() {
        return this.thresholdDistance;
    }
}
