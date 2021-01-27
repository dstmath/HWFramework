package huawei.android.security.privacyability.diffprivacy;

import android.text.TextUtils;
import android.util.Log;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.BitSet;

public class LocalWordFilter {
    public static final int BUDGET_VALUE = 4;
    private static final int STEP = 1;
    private static final String TAG = "LocalWordFilter";
    public static final int XOR_VALUE = 2;
    private int bloomBits;
    private int hashNum;
    private boolean isHex;
    private int maxLength;
    private double noiseProbability;

    public LocalWordFilter(int hashNum2, int bloomBits2, double epsilon, int maxWordLength, boolean hexFlag) {
        this.hashNum = hashNum2;
        this.bloomBits = bloomBits2;
        this.maxLength = maxWordLength;
        this.noiseProbability = 1.0d / (Math.exp(epsilon / 4.0d) + 1.0d);
        this.isHex = hexFlag;
    }

    public String generateReport(String word) {
        NoSuchAlgorithmException e;
        String reportPartTwo;
        String reportPartOne;
        if (TextUtils.isEmpty(word)) {
            return null;
        }
        String originWord = word;
        if (word.length() == 1) {
            originWord = word.concat("_");
        }
        String newWord = getNewWord(originWord);
        SecureRandom rand = new SecureRandom();
        int stepIndex = rand.nextInt((newWord.length() - 1) / 1);
        int kRandom = rand.nextInt(this.hashNum);
        String strPrevious = getStrPrevious(newWord, stepIndex);
        String strNext = getStrNext(newWord, stepIndex);
        HashNewWord hashNewWord = new HashNewWord(this.hashNum, this.bloomBits);
        try {
            int strPreviousHashIndex = hashNewWord.getStrpIndex(strPrevious, kRandom);
            int strNextHashIndex = hashNewWord.getStrpIndex(originWord, kRandom);
            if (strPreviousHashIndex < 0) {
                return null;
            }
            if (strNextHashIndex < 0) {
                return null;
            }
            String reportPartOne2 = getStrpnVec(strPreviousHashIndex);
            String reportPartTwo2 = getStrpnVec(strNextHashIndex);
            if (this.isHex) {
                try {
                    reportPartOne = hashNewWord.convertReporttoHex(reportPartOne2, reportPartOne2.length());
                    reportPartTwo = hashNewWord.convertReporttoHex(reportPartTwo2, reportPartTwo2.length());
                } catch (NoSuchAlgorithmException e2) {
                    e = e2;
                    Log.e(TAG, "generateReport NoSuchAlgorithmException" + e.getMessage());
                    return null;
                }
            } else {
                reportPartOne = reportPartOne2;
                reportPartTwo = reportPartTwo2;
            }
            try {
                return reportPartOne + "," + reportPartTwo + "," + String.valueOf(strPrevious.length()) + "," + String.valueOf(strNext.length()) + "," + String.valueOf(kRandom);
            } catch (NoSuchAlgorithmException e3) {
                e = e3;
                Log.e(TAG, "generateReport NoSuchAlgorithmException" + e.getMessage());
                return null;
            }
        } catch (NoSuchAlgorithmException e4) {
            e = e4;
            Log.e(TAG, "generateReport NoSuchAlgorithmException" + e.getMessage());
            return null;
        }
    }

    private String getStrPrevious(String word, int stepindex) {
        return word.substring(0, (stepindex + 1) * 1);
    }

    private String getStrNext(String word, int stepindex) {
        return word.substring((stepindex + 1) * 1, word.length());
    }

    private String getStrpnVec(int strpnHashindex) {
        int bloom = this.bloomBits;
        double noise = this.noiseProbability;
        StringBuffer stringBuf = new StringBuffer(256);
        BitSet vector = new BitSet(bloom);
        vector.set(strpnHashindex, true);
        SecureRandom randpn = new SecureRandom();
        BitSet array = new BitSet(bloom);
        for (int n = 0; n < bloom; n++) {
            if (randpn.nextDouble() <= noise) {
                array.set(n, true);
            } else {
                array.set(n, false);
            }
            vector.set(n, vector.get(n) ^ array.get(n));
        }
        for (int i = 0; i < bloom; i++) {
            if (vector.get(i)) {
                stringBuf.append("1");
            } else {
                stringBuf.append("0");
            }
        }
        return stringBuf.toString();
    }

    private String getNewWord(String newWord) {
        StringBuilder builder = new StringBuilder(newWord);
        int len = newWord.length();
        for (int i = 0; i < this.maxLength - len; i++) {
            builder.append("#");
        }
        return builder.toString();
    }
}
