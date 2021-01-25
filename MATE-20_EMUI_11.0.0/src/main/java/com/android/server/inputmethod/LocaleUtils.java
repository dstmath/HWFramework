package com.android.server.inputmethod;

import android.icu.util.ULocale;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.ArrayMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

final class LocaleUtils {

    public interface LocaleExtractor<T> {
        Locale get(T t);
    }

    LocaleUtils() {
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

    private static final class ScoreEntry implements Comparable<ScoreEntry> {
        public int mIndex = -1;
        public final byte[] mScore;

        ScoreEntry(byte[] score, int index) {
            this.mScore = new byte[score.length];
            set(score, index);
        }

        private void set(byte[] score, int index) {
            int i = 0;
            while (true) {
                byte[] bArr = this.mScore;
                if (i < bArr.length) {
                    bArr[i] = score[i];
                    i++;
                } else {
                    this.mIndex = index;
                    return;
                }
            }
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
            return compare(this.mScore, other.mScore) * -1;
        }
    }

    public static <T> void filterByLanguage(List<T> sources, LocaleExtractor<T> extractor, LocaleList preferredLocales, ArrayList<T> dest) {
        int i;
        if (!preferredLocales.isEmpty()) {
            int numPreferredLocales = preferredLocales.size();
            ArrayMap<String, ScoreEntry> scoreboard = new ArrayMap<>();
            byte[] score = new byte[numPreferredLocales];
            ULocale[] preferredULocaleCache = new ULocale[numPreferredLocales];
            int sourceSize = sources.size();
            int i2 = 0;
            while (true) {
                if (i2 >= sourceSize) {
                    break;
                }
                Locale locale = extractor.get(sources.get(i2));
                if (locale != null) {
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
                    if (!canSkip) {
                        String lang = locale.getLanguage();
                        ScoreEntry bestScore = scoreboard.get(lang);
                        if (bestScore == null) {
                            scoreboard.put(lang, new ScoreEntry(score, i2));
                        } else {
                            bestScore.updateIfBetter(score, i2);
                        }
                    }
                }
                i2++;
            }
            int numEntries = scoreboard.size();
            ScoreEntry[] result = new ScoreEntry[numEntries];
            for (int i3 = 0; i3 < numEntries; i3++) {
                result[i3] = scoreboard.valueAt(i3);
            }
            Arrays.sort(result);
            for (ScoreEntry entry : result) {
                dest.add(sources.get(entry.mIndex));
            }
        }
    }
}
