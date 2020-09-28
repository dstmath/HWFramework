package com.android.i18n.phonenumbers.prefixmapper;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;

public class MappingFileProvider implements Externalizable {
    private static final Map<String, String> LOCALE_NORMALIZATION_MAP;
    private List<Set<String>> availableLanguages;
    private int[] countryCallingCodes;
    private int numOfEntries = 0;

    static {
        Map<String, String> normalizationMap = new HashMap<>();
        normalizationMap.put("zh_TW", "zh_Hant");
        normalizationMap.put("zh_HK", "zh_Hant");
        normalizationMap.put("zh_MO", "zh_Hant");
        LOCALE_NORMALIZATION_MAP = Collections.unmodifiableMap(normalizationMap);
    }

    public void readFileConfigs(SortedMap<Integer, Set<String>> availableDataFiles) {
        this.numOfEntries = availableDataFiles.size();
        int i = this.numOfEntries;
        this.countryCallingCodes = new int[i];
        this.availableLanguages = new ArrayList(i);
        int index = 0;
        for (Integer num : availableDataFiles.keySet()) {
            int countryCallingCode = num.intValue();
            this.countryCallingCodes[index] = countryCallingCode;
            this.availableLanguages.add(new HashSet(availableDataFiles.get(Integer.valueOf(countryCallingCode))));
            index++;
        }
    }

    @Override // java.io.Externalizable
    public void readExternal(ObjectInput objectInput) throws IOException {
        this.numOfEntries = objectInput.readInt();
        int[] iArr = this.countryCallingCodes;
        if (iArr == null || iArr.length < this.numOfEntries) {
            this.countryCallingCodes = new int[this.numOfEntries];
        }
        if (this.availableLanguages == null) {
            this.availableLanguages = new ArrayList();
        }
        for (int i = 0; i < this.numOfEntries; i++) {
            this.countryCallingCodes[i] = objectInput.readInt();
            int numOfLangs = objectInput.readInt();
            Set<String> setOfLangs = new HashSet<>();
            for (int j = 0; j < numOfLangs; j++) {
                setOfLangs.add(objectInput.readUTF());
            }
            this.availableLanguages.add(setOfLangs);
        }
    }

    @Override // java.io.Externalizable
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        objectOutput.writeInt(this.numOfEntries);
        for (int i = 0; i < this.numOfEntries; i++) {
            objectOutput.writeInt(this.countryCallingCodes[i]);
            Set<String> setOfLangs = this.availableLanguages.get(i);
            objectOutput.writeInt(setOfLangs.size());
            for (String lang : setOfLangs) {
                objectOutput.writeUTF(lang);
            }
        }
    }

    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < this.numOfEntries; i++) {
            output.append(this.countryCallingCodes[i]);
            output.append('|');
            for (String lang : new TreeSet<>(this.availableLanguages.get(i))) {
                output.append(lang);
                output.append(',');
            }
            output.append('\n');
        }
        return output.toString();
    }

    /* access modifiers changed from: package-private */
    public String getFileName(int countryCallingCode, String language, String script, String region) {
        int index;
        if (language.length() == 0 || (index = Arrays.binarySearch(this.countryCallingCodes, countryCallingCode)) < 0) {
            return "";
        }
        Set<String> setOfLangs = this.availableLanguages.get(index);
        if (setOfLangs.size() > 0) {
            String languageCode = findBestMatchingLanguageCode(setOfLangs, language, script, region);
            if (languageCode.length() > 0) {
                return countryCallingCode + '_' + languageCode;
            }
        }
        return "";
    }

    private String findBestMatchingLanguageCode(Set<String> setOfLangs, String language, String script, String region) {
        String fullLocaleStr = constructFullLocale(language, script, region).toString();
        String normalizedLocale = LOCALE_NORMALIZATION_MAP.get(fullLocaleStr);
        if (normalizedLocale != null && setOfLangs.contains(normalizedLocale)) {
            return normalizedLocale;
        }
        if (setOfLangs.contains(fullLocaleStr)) {
            return fullLocaleStr;
        }
        if (onlyOneOfScriptOrRegionIsEmpty(script, region)) {
            if (setOfLangs.contains(language)) {
                return language;
            }
            return "";
        } else if (script.length() <= 0 || region.length() <= 0) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder(language);
            sb.append('_');
            String langWithScriptStr = sb.append(script).toString();
            if (setOfLangs.contains(langWithScriptStr)) {
                return langWithScriptStr;
            }
            StringBuilder sb2 = new StringBuilder(language);
            sb2.append('_');
            String langWithRegionStr = sb2.append(region).toString();
            if (setOfLangs.contains(langWithRegionStr)) {
                return langWithRegionStr;
            }
            if (setOfLangs.contains(language)) {
                return language;
            }
            return "";
        }
    }

    private boolean onlyOneOfScriptOrRegionIsEmpty(String script, String region) {
        return (script.length() == 0 && region.length() > 0) || (region.length() == 0 && script.length() > 0);
    }

    private StringBuilder constructFullLocale(String language, String script, String region) {
        StringBuilder fullLocale = new StringBuilder(language);
        appendSubsequentLocalePart(script, fullLocale);
        appendSubsequentLocalePart(region, fullLocale);
        return fullLocale;
    }

    private void appendSubsequentLocalePart(String subsequentLocalePart, StringBuilder fullLocale) {
        if (subsequentLocalePart.length() > 0) {
            fullLocale.append('_');
            fullLocale.append(subsequentLocalePart);
        }
    }
}
