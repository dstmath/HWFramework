package com.android.internal.inputmethod;

import android.icu.util.ULocale;
import android.os.LocaleList;
import android.text.TextUtils;
import com.android.internal.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class LocaleUtils {

    @VisibleForTesting
    public interface LocaleExtractor<T> {
        Locale get(T t);
    }

    private static final class ScoreEntry implements Comparable<ScoreEntry> {
        public int mIndex = -1;
        public final byte[] mScore;

        ScoreEntry(byte[] score, int index) {
            this.mScore = new byte[score.length];
            set(score, index);
        }

        private void set(byte[] score, int index) {
            for (int i = 0; i < this.mScore.length; i++) {
                this.mScore[i] = score[i];
            }
            this.mIndex = index;
        }

        public void updateIfBetter(byte[] score, int index) {
            if (compare(this.mScore, score) == -1) {
                set(score, index);
            }
        }

        private static int compare(byte[] left, byte[] right) {
            for (int i = 0; i < left.length; i++) {
                if (left[i] > right[i]) {
                    return 1;
                }
                if (left[i] < right[i]) {
                    return -1;
                }
            }
            return 0;
        }

        public int compareTo(ScoreEntry other) {
            return -1 * compare(this.mScore, other.mScore);
        }
    }

    private static byte calculateMatchingSubScore(ULocale supported, ULocale desired) {
        if (supported.equals(desired)) {
            return 3;
        }
        String supportedScript = supported.getScript();
        if (supportedScript.isEmpty() || !supportedScript.equals(desired.getScript())) {
            return 1;
        }
        String supportedCountry = supported.getCountry();
        if (supportedCountry.isEmpty() || !supportedCountry.equals(desired.getCountry())) {
            return 2;
        }
        return 3;
    }

    @VisibleForTesting
    public static <T> void filterByLanguage(List<T> sources, LocaleExtractor<T> extractor, LocaleList preferredLocales, ArrayList<T> dest) {
        List<T> list = sources;
        if (!preferredLocales.isEmpty()) {
            int numPreferredLocales = preferredLocales.size();
            HashMap<String, ScoreEntry> scoreboard = new HashMap<>();
            byte[] score = new byte[numPreferredLocales];
            ULocale[] preferredULocaleCache = new ULocale[numPreferredLocales];
            int sourceSize = sources.size();
            for (int i = 0; i < sourceSize; i++) {
                Locale locale = extractor.get(list.get(i));
                if (locale == null) {
                    LocaleList localeList = preferredLocales;
                } else {
                    boolean canSkip = true;
                    for (int j = 0; j < numPreferredLocales; j++) {
                        Locale preferredLocale = preferredLocales.get(j);
                        if (!TextUtils.equals(locale.getLanguage(), preferredLocale.getLanguage())) {
                            score[j] = 0;
                        } else {
                            if (preferredULocaleCache[j] == null) {
                                preferredULocaleCache[j] = ULocale.addLikelySubtags(ULocale.forLocale(preferredLocale));
                            }
                            score[j] = calculateMatchingSubScore(preferredULocaleCache[j], ULocale.addLikelySubtags(ULocale.forLocale(locale)));
                            if (canSkip && score[j] != 0) {
                                canSkip = false;
                            }
                        }
                    }
                    LocaleList localeList2 = preferredLocales;
                    if (!canSkip) {
                        String lang = locale.getLanguage();
                        ScoreEntry bestScore = scoreboard.get(lang);
                        if (bestScore == null) {
                            scoreboard.put(lang, new ScoreEntry(score, i));
                        } else {
                            bestScore.updateIfBetter(score, i);
                        }
                    }
                }
            }
            LocaleExtractor<T> localeExtractor = extractor;
            LocaleList localeList3 = preferredLocales;
            ScoreEntry[] result = (ScoreEntry[]) scoreboard.values().toArray(new ScoreEntry[scoreboard.size()]);
            Arrays.sort(result);
            for (ScoreEntry entry : result) {
                dest.add(list.get(entry.mIndex));
            }
            ArrayList<T> arrayList = dest;
        }
    }
}
