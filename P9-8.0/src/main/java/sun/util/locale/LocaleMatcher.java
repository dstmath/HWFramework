package sun.util.locale;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Locale.FilteringMode;
import java.util.Locale.LanguageRange;
import java.util.Map;

public final class LocaleMatcher {
    public static List<Locale> filter(List<LanguageRange> priorityList, Collection<Locale> locales, FilteringMode mode) {
        if (priorityList.isEmpty() || locales.isEmpty()) {
            return new ArrayList();
        }
        List<String> tags = new ArrayList();
        for (Locale locale : locales) {
            tags.-java_util_stream_Collectors-mthref-2(locale.toLanguageTag());
        }
        List<String> filteredTags = filterTags(priorityList, tags, mode);
        List<Locale> filteredLocales = new ArrayList(filteredTags.size());
        for (String tag : filteredTags) {
            filteredLocales.-java_util_stream_Collectors-mthref-2(Locale.forLanguageTag(tag));
        }
        return filteredLocales;
    }

    public static List<String> filterTags(List<LanguageRange> priorityList, Collection<String> tags, FilteringMode mode) {
        if (priorityList.isEmpty() || tags.isEmpty()) {
            return new ArrayList();
        }
        if (mode == FilteringMode.EXTENDED_FILTERING) {
            return filterExtended(priorityList, tags);
        }
        ArrayList<LanguageRange> list = new ArrayList();
        for (LanguageRange lr : priorityList) {
            String range = lr.getRange();
            if (!range.startsWith("*-") && range.indexOf("-*") == -1) {
                list.add(lr);
            } else if (mode == FilteringMode.AUTOSELECT_FILTERING) {
                return filterExtended(priorityList, tags);
            } else {
                if (mode == FilteringMode.MAP_EXTENDED_RANGES) {
                    if (range.charAt(0) == '*') {
                        range = "*";
                    } else {
                        range = range.replaceAll("-[*]", "");
                    }
                    list.add(new LanguageRange(range, lr.getWeight()));
                } else if (mode == FilteringMode.REJECT_EXTENDED_RANGES) {
                    throw new IllegalArgumentException("An extended range \"" + range + "\" found in REJECT_EXTENDED_RANGES mode.");
                }
            }
        }
        return filterBasic(list, tags);
    }

    private static List<String> filterBasic(List<LanguageRange> priorityList, Collection<String> tags) {
        List<String> list = new ArrayList();
        for (LanguageRange lr : priorityList) {
            String range = lr.getRange();
            if (range.equals("*")) {
                return new ArrayList((Collection) tags);
            }
            for (String tag : tags) {
                String tag2 = tag2.toLowerCase();
                if (tag2.startsWith(range)) {
                    int len = range.length();
                    if ((tag2.length() == len || tag2.charAt(len) == '-') && (list.contains(tag2) ^ 1) != 0) {
                        list.-java_util_stream_Collectors-mthref-2(tag2);
                    }
                }
            }
        }
        return list;
    }

    private static List<String> filterExtended(List<LanguageRange> priorityList, Collection<String> tags) {
        List<String> list = new ArrayList();
        for (LanguageRange lr : priorityList) {
            String range = lr.getRange();
            if (range.equals("*")) {
                return new ArrayList((Collection) tags);
            }
            String[] rangeSubtags = range.split(LanguageTag.SEP);
            for (String tag : tags) {
                String tag2 = tag2.toLowerCase();
                String[] tagSubtags = tag2.split(LanguageTag.SEP);
                if (rangeSubtags[0].equals(tagSubtags[0]) || (rangeSubtags[0].equals("*") ^ 1) == 0) {
                    int rangeIndex = 1;
                    int tagIndex = 1;
                    while (rangeIndex < rangeSubtags.length && tagIndex < tagSubtags.length) {
                        if (!rangeSubtags[rangeIndex].equals("*")) {
                            if (!rangeSubtags[rangeIndex].equals(tagSubtags[tagIndex])) {
                                if (tagSubtags[tagIndex].length() == 1 && (tagSubtags[tagIndex].equals("*") ^ 1) != 0) {
                                    break;
                                }
                                tagIndex++;
                            } else {
                                rangeIndex++;
                                tagIndex++;
                            }
                        } else {
                            rangeIndex++;
                        }
                    }
                    if (rangeSubtags.length == rangeIndex && (list.contains(tag2) ^ 1) != 0) {
                        list.-java_util_stream_Collectors-mthref-2(tag2);
                    }
                }
            }
        }
        return list;
    }

    public static Locale lookup(List<LanguageRange> priorityList, Collection<Locale> locales) {
        if (priorityList.isEmpty() || locales.isEmpty()) {
            return null;
        }
        List<String> tags = new ArrayList();
        for (Locale locale : locales) {
            tags.-java_util_stream_Collectors-mthref-2(locale.toLanguageTag());
        }
        String lookedUpTag = lookupTag(priorityList, tags);
        if (lookedUpTag == null) {
            return null;
        }
        return Locale.forLanguageTag(lookedUpTag);
    }

    public static String lookupTag(List<LanguageRange> priorityList, Collection<String> tags) {
        if (priorityList.isEmpty() || tags.isEmpty()) {
            return null;
        }
        for (LanguageRange lr : priorityList) {
            String range = lr.getRange();
            if (!range.equals("*")) {
                String rangeForRegex = range.replace((CharSequence) "*", (CharSequence) "\\p{Alnum}*");
                while (rangeForRegex.length() > 0) {
                    for (String tag : tags) {
                        String tag2 = tag2.toLowerCase();
                        if (tag2.matches(rangeForRegex)) {
                            return tag2;
                        }
                    }
                    int index = rangeForRegex.lastIndexOf(45);
                    if (index >= 0) {
                        rangeForRegex = rangeForRegex.substring(0, index);
                        if (rangeForRegex.lastIndexOf(45) == rangeForRegex.length() - 2) {
                            rangeForRegex = rangeForRegex.substring(0, rangeForRegex.length() - 2);
                        }
                    } else {
                        rangeForRegex = "";
                    }
                }
                continue;
            }
        }
        return null;
    }

    public static List<LanguageRange> parse(String ranges) {
        String r;
        double w;
        ranges = ranges.replace((CharSequence) " ", (CharSequence) "").toLowerCase();
        if (ranges.startsWith("accept-language:")) {
            ranges = ranges.substring(16);
        }
        String[] langRanges = ranges.split(",");
        List<LanguageRange> list = new ArrayList(langRanges.length);
        List<String> tempList = new ArrayList();
        int numOfRanges = 0;
        int i = 0;
        int length = langRanges.length;
        while (true) {
            int i2 = i;
            if (i2 >= length) {
                return list;
            }
            String range = langRanges[i2];
            int index = range.indexOf(";q=");
            if (index == -1) {
                r = range;
                w = 1.0d;
            } else {
                r = range.substring(0, index);
                index += 3;
                try {
                    w = Double.parseDouble(range.substring(index));
                    if (w < 0.0d || w > 1.0d) {
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("weight=\"" + range.substring(index) + "\" for language range \"" + r + "\"");
                }
            }
            if (!tempList.contains(r)) {
                LanguageRange lr = new LanguageRange(r, w);
                index = numOfRanges;
                for (int j = 0; j < numOfRanges; j++) {
                    if (((LanguageRange) list.get(j)).getWeight() < w) {
                        index = j;
                        break;
                    }
                }
                list.add(index, lr);
                numOfRanges++;
                tempList.-java_util_stream_Collectors-mthref-2(r);
                String equivalent = getEquivalentForRegionAndVariant(r);
                if (!(equivalent == null || (tempList.contains(equivalent) ^ 1) == 0)) {
                    list.add(index + 1, new LanguageRange(equivalent, w));
                    numOfRanges++;
                    tempList.-java_util_stream_Collectors-mthref-2(equivalent);
                }
                String[] equivalents = getEquivalentsForLanguage(r);
                if (equivalents != null) {
                    for (String equiv : equivalents) {
                        if (!tempList.contains(equiv)) {
                            list.add(index + 1, new LanguageRange(equiv, w));
                            numOfRanges++;
                            tempList.-java_util_stream_Collectors-mthref-2(equiv);
                        }
                        equivalent = getEquivalentForRegionAndVariant(equiv);
                        if (!(equivalent == null || (tempList.contains(equivalent) ^ 1) == 0)) {
                            list.add(index + 1, new LanguageRange(equivalent, w));
                            numOfRanges++;
                            tempList.-java_util_stream_Collectors-mthref-2(equivalent);
                        }
                    }
                }
            }
            i = i2 + 1;
        }
        throw new IllegalArgumentException("weight=" + w + " for language range \"" + r + "\". It must be between " + 0.0d + " and " + 1.0d + ".");
    }

    private static String replaceFirstSubStringMatch(String range, String substr, String replacement) {
        int pos = range.indexOf(substr);
        if (pos == -1) {
            return range;
        }
        return range.substring(0, pos) + replacement + range.substring(substr.length() + pos);
    }

    private static String[] getEquivalentsForLanguage(String range) {
        String r = range;
        while (r.length() > 0) {
            if (LocaleEquivalentMaps.singleEquivMap.containsKey(r)) {
                return new String[]{replaceFirstSubStringMatch(range, r, (String) LocaleEquivalentMaps.singleEquivMap.get(r))};
            } else if (LocaleEquivalentMaps.multiEquivsMap.containsKey(r)) {
                String[] equivs = (String[]) LocaleEquivalentMaps.multiEquivsMap.get(r);
                String[] result = new String[equivs.length];
                for (int i = 0; i < equivs.length; i++) {
                    result[i] = replaceFirstSubStringMatch(range, r, equivs[i]);
                }
                return result;
            } else {
                int index = r.lastIndexOf(45);
                if (index == -1) {
                    break;
                }
                r = r.substring(0, index);
            }
        }
        return null;
    }

    private static String getEquivalentForRegionAndVariant(String range) {
        int extensionKeyIndex = getExtentionKeyIndex(range);
        for (String subtag : LocaleEquivalentMaps.regionVariantEquivMap.keySet()) {
            int index = range.indexOf(subtag);
            if (index != -1 && (extensionKeyIndex == Integer.MIN_VALUE || index <= extensionKeyIndex)) {
                int len = index + subtag.length();
                if (range.length() == len || range.charAt(len) == '-') {
                    return replaceFirstSubStringMatch(range, subtag, (String) LocaleEquivalentMaps.regionVariantEquivMap.get(subtag));
                }
            }
        }
        return null;
    }

    private static int getExtentionKeyIndex(String s) {
        char[] c = s.toCharArray();
        int index = Integer.MIN_VALUE;
        for (int i = 1; i < c.length; i++) {
            if (c[i] == '-') {
                if (i - index == 2) {
                    return index;
                }
                index = i;
            }
        }
        return Integer.MIN_VALUE;
    }

    public static List<LanguageRange> mapEquivalents(List<LanguageRange> priorityList, Map<String, List<String>> map) {
        if (priorityList.isEmpty()) {
            return new ArrayList();
        }
        if (map == null || map.isEmpty()) {
            return new ArrayList((Collection) priorityList);
        }
        Map<String, String> keyMap = new HashMap();
        for (String key : map.keySet()) {
            keyMap.put(key.toLowerCase(), key);
        }
        List<LanguageRange> list = new ArrayList();
        for (LanguageRange lr : priorityList) {
            String range = lr.getRange();
            String r = range;
            boolean hasEquivalent = false;
            while (r.length() > 0) {
                if (keyMap.containsKey(r)) {
                    hasEquivalent = true;
                    List<String> equivalents = (List) map.get(keyMap.get(r));
                    if (equivalents != null) {
                        int len = r.length();
                        for (String equivalent : equivalents) {
                            list.-java_util_stream_Collectors-mthref-2(new LanguageRange(equivalent.toLowerCase() + range.substring(len), lr.getWeight()));
                        }
                    }
                } else {
                    int index = r.lastIndexOf(45);
                    if (index == -1) {
                        break;
                    }
                    r = r.substring(0, index);
                }
            }
            if (!hasEquivalent) {
                list.-java_util_stream_Collectors-mthref-2(lr);
            }
        }
        return list;
    }

    private LocaleMatcher() {
    }
}
