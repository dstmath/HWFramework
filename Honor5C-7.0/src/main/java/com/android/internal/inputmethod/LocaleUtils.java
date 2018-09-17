package com.android.internal.inputmethod;

import android.icu.util.ULocale;
import android.os.LocaleList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public final class LocaleUtils {

    public interface LocaleExtractor<T> {
        Locale get(T t);
    }

    private static final class ScoreEntry implements Comparable<ScoreEntry> {
        public int mIndex;
        public final byte[] mScore;

        ScoreEntry(byte[] score, int index) {
            this.mIndex = -1;
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
            return compare(this.mScore, other.mScore) * -1;
        }
    }

    private static byte calculateMatchingSubScore(ULocale supported, ULocale desired) {
        if (supported.equals(desired)) {
            return (byte) 3;
        }
        String supportedScript = supported.getScript();
        if (supportedScript.isEmpty() || !supportedScript.equals(desired.getScript())) {
            return (byte) 1;
        }
        String supportedCountry = supported.getCountry();
        if (supportedCountry.isEmpty() || !supportedCountry.equals(desired.getCountry())) {
            return (byte) 2;
        }
        return (byte) 3;
    }

    private static boolean calculateMatchingScore(ULocale supported, LocaleList desired, byte[] out) {
        boolean z = false;
        if (desired.isEmpty()) {
            return false;
        }
        boolean allZeros = true;
        int N = desired.size();
        int i = 0;
        while (i < N) {
            Locale locale = desired.get(i);
            if (locale.getLanguage().equals(supported.getLanguage())) {
                out[i] = calculateMatchingSubScore(supported, ULocale.addLikelySubtags(ULocale.forLocale(locale)));
                if (allZeros && out[i] != null) {
                    allZeros = false;
                }
            } else {
                out[i] = (byte) 0;
            }
            i++;
        }
        if (!allZeros) {
            z = true;
        }
        return z;
    }

    public static <T> void filterByLanguage(List<T> sources, LocaleExtractor<T> extractor, LocaleList preferredLanguages, ArrayList<T> dest) {
        HashMap<String, ScoreEntry> scoreboard = new HashMap();
        byte[] score = new byte[preferredLanguages.size()];
        int sourceSize = sources.size();
        for (int i = 0; i < sourceSize; i++) {
            Locale locale = extractor.get(sources.get(i));
            if (locale != null && calculateMatchingScore(ULocale.addLikelySubtags(ULocale.forLocale(locale)), preferredLanguages, score)) {
                String lang = locale.getLanguage();
                ScoreEntry bestScore = (ScoreEntry) scoreboard.get(lang);
                if (bestScore == null) {
                    scoreboard.put(lang, new ScoreEntry(score, i));
                } else {
                    bestScore.updateIfBetter(score, i);
                }
            }
        }
        ScoreEntry[] result = (ScoreEntry[]) scoreboard.values().toArray(new ScoreEntry[scoreboard.size()]);
        Arrays.sort(result);
        for (ScoreEntry entry : result) {
            dest.add(sources.get(entry.mIndex));
        }
    }
}
