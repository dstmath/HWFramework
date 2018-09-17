package android.rms.iaware;

import android.text.TextUtils;
import java.util.regex.Pattern;

public class DataNormalizer {
    private static final String COLLECT_SEPARATOR = "\u0001";
    private static final Pattern COLLECT_SEPARATOR_PAT = Pattern.compile(Pattern.quote(COLLECT_SEPARATOR));
    private static final String VALUE_SEPARATOR = "Δ";
    private static final Pattern VALUE_SEPARATOR_PAT = Pattern.compile(Pattern.quote(VALUE_SEPARATOR));
    private StringBuilder mSB = new StringBuilder();

    public String toString() {
        return this.mSB.toString();
    }

    public void clean() {
        this.mSB.setLength(0);
    }

    public void appendCondition(String key, String value) {
        String condition = encapsulateCondition(key, value);
        if (condition != null) {
            if (this.mSB.length() > 0) {
                this.mSB.append(VALUE_SEPARATOR);
            }
            this.mSB.append(condition);
        }
    }

    public void appendConditionArray(String[] keyArray, String[] valueArray, int arrayLen) {
        String conditionArray = encapsulateConditionArray(keyArray, valueArray, arrayLen);
        if (conditionArray != null) {
            if (this.mSB.length() > 0) {
                this.mSB.append(VALUE_SEPARATOR);
            }
            this.mSB.append(conditionArray);
        }
    }

    public void appendCondition(String key, String[] valueArray, int arrayLen) {
        String conditionArray = encapsulateCondition(key, valueArray, arrayLen);
        if (conditionArray != null) {
            if (this.mSB.length() > 0) {
                this.mSB.append(VALUE_SEPARATOR);
            }
            this.mSB.append(conditionArray);
        }
    }

    public void appendCollect(String attr, String conditionList) {
        if (!TextUtils.isEmpty(conditionList)) {
            if (this.mSB.length() > 0) {
                this.mSB.append(COLLECT_SEPARATOR);
            }
            this.mSB.append(attr).append(" {").append(conditionList).append("}");
        }
    }

    public void appendCollect(String collect) {
        if (!TextUtils.isEmpty(collect)) {
            if (this.mSB.length() > 0) {
                this.mSB.append(COLLECT_SEPARATOR);
            }
            this.mSB.append(collect);
        }
    }

    private static String encapsulateConditionArray(String[] keyArray, String[] valueArray, int arrayLen) {
        if (arrayLen < 1 || keyArray == null || keyArray.length != arrayLen || valueArray == null || valueArray.length != arrayLen) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arrayLen; i++) {
            sb.append(keyArray[i]).append("=").append(valueArray[i]).append(VALUE_SEPARATOR);
        }
        sb.setLength(sb.length() - VALUE_SEPARATOR.length());
        return sb.toString();
    }

    private static String encapsulateCondition(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(key).append("=").append(value);
        return sb.toString();
    }

    private static String encapsulateCondition(String key, String[] valueArray, int arrayLen) {
        if (TextUtils.isEmpty(key) || arrayLen < 1 || valueArray == null || valueArray.length != arrayLen) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(key).append("={");
        for (int i = 0; i < arrayLen; i++) {
            sb.append(valueArray[i]).append(",");
        }
        sb.setLength(sb.length() - ",".length());
        sb.append(key).append("}");
        return sb.toString();
    }

    public static String[] getCollectArray(String data) {
        if (TextUtils.isEmpty(data)) {
            return new String[0];
        }
        return COLLECT_SEPARATOR_PAT.split(data);
    }

    public static String[] getConditionArray(String conditionList) {
        if (TextUtils.isEmpty(conditionList) || conditionList.charAt(0) != '{' || conditionList.charAt(conditionList.length() - 1) != '}') {
            return new String[0];
        }
        return VALUE_SEPARATOR_PAT.split(conditionList.substring(1, conditionList.length() - 1));
    }

    public static String[] parseCollect(String collect) {
        if (collect == null) {
            return new String[0];
        }
        int end = collect.indexOf(32);
        if (end < 1 || end > collect.length() - 1) {
            return new String[0];
        }
        return new String[]{collect.substring(0, end), collect.substring(end + 1)};
    }

    public static String[] parseCondition(String condition) {
        if (condition == null) {
            return new String[0];
        }
        int end = condition.indexOf(61);
        if (end < 1 || end > condition.length() - 1) {
            return new String[0];
        }
        return new String[]{condition.substring(0, end), condition.substring(end + 1)};
    }

    public static String[] parseConditionExt(String condition) {
        if (condition == null) {
            return new String[0];
        }
        int end = condition.indexOf(61);
        if (end < 1 || end > condition.length() - 1) {
            return new String[0];
        }
        String key = condition.substring(0, end);
        String value = condition.substring(end + 1);
        String[] valueArray = value.substring(1, value.length() - 1).split(",");
        String[] array = new String[(valueArray.length + 1)];
        array[0] = key;
        System.arraycopy(valueArray, 0, array, 1, valueArray.length);
        return array;
    }
}
