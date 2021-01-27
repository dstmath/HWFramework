package ohos.data.distributed.common;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Query {
    private static final String AND = "^AND";
    private static final String BEGIN_GROUP = "^BEGIN_GROUP";
    private static final String DEVICE_ID = "^DEVICE_ID";
    private static final String EMPTY_STRING = "^EMPTY_STRING";
    private static final String END_GROUP = "^END_GROUP";
    private static final String END_IN = "^END";
    private static final String EQUAL_TO = "^EQUAL";
    private static final String GREATER_THAN = "^GREATER";
    private static final String GREATER_THAN_OR_EQUAL_TO = "^GREATER_EQUAL";
    private static final String IN = "^IN";
    private static final String IS_NOT_NULL = "^IS_NOT_NULL";
    private static final String IS_NULL = "^IS_NULL";
    private static final String KEY_PREFIX = "^KEY_PREFIX";
    private static final String LABEL = "Query";
    private static final String LESS_THAN = "^LESS";
    private static final String LESS_THAN_OR_EQUAL_TO = "^LESS_EQUAL";
    private static final String LIKE = "^LIKE";
    private static final String LIMIT = "^LIMIT";
    private static final String NOT_EQUAL_TO = "^NOT_EQUAL";
    private static final String NOT_IN = "^NOT_IN";
    private static final String NOT_LIKE = "^NOT_LIKE";
    private static final String OR = "^OR";
    private static final String ORDER_BY_ASC = "^ASC";
    private static final String ORDER_BY_DESC = "^DESC";
    private static final String SPACE = " ";
    private static final String SPACE_ESCAPE = "^^";
    private static final String SPECIAL = "^";
    private static final String SPECIAL_ESCAPE = "(^)";
    private static final String START_IN = "^START";
    private StringBuilder sqlLike = new StringBuilder();

    private Query() {
    }

    private Query(StringBuilder sb) {
        this.sqlLike = sb;
    }

    public static Query select() {
        return new Query();
    }

    public Query reset() {
        this.sqlLike.setLength(0);
        return this;
    }

    public Query equalTo(String str, int i) throws KvStoreException {
        validateField(str);
        appendCommon(EQUAL_TO, FieldValueType.INTEGER.getCode(), str, Integer.valueOf(i));
        return this;
    }

    public Query equalTo(String str, long j) throws KvStoreException {
        validateField(str);
        appendCommon(EQUAL_TO, FieldValueType.LONG.getCode(), str, Long.valueOf(j));
        return this;
    }

    public Query equalTo(String str, double d) throws KvStoreException {
        validateField(str);
        appendCommon(EQUAL_TO, FieldValueType.DOUBLE.getCode(), str, Double.valueOf(d));
        return this;
    }

    public Query equalTo(String str, String str2) throws KvStoreException {
        validateField(str);
        validateValue(str2);
        appendCommon(EQUAL_TO, FieldValueType.STRING.getCode(), str, str2);
        return this;
    }

    public Query equalTo(String str, boolean z) throws KvStoreException {
        validateField(str);
        appendCommon(EQUAL_TO, FieldValueType.BOOLEAN.getCode(), str, Boolean.valueOf(z));
        return this;
    }

    public Query notEqualTo(String str, int i) throws KvStoreException {
        validateField(str);
        appendCommon(NOT_EQUAL_TO, FieldValueType.INTEGER.getCode(), str, Integer.valueOf(i));
        return this;
    }

    public Query notEqualTo(String str, long j) throws KvStoreException {
        validateField(str);
        appendCommon(NOT_EQUAL_TO, FieldValueType.LONG.getCode(), str, Long.valueOf(j));
        return this;
    }

    public Query notEqualTo(String str, double d) throws KvStoreException {
        validateField(str);
        appendCommon(NOT_EQUAL_TO, FieldValueType.DOUBLE.getCode(), str, Double.valueOf(d));
        return this;
    }

    public Query notEqualTo(String str, boolean z) throws KvStoreException {
        validateField(str);
        appendCommon(NOT_EQUAL_TO, FieldValueType.BOOLEAN.getCode(), str, Boolean.valueOf(z));
        return this;
    }

    public Query notEqualTo(String str, String str2) throws KvStoreException {
        validateField(str);
        validateValue(str2);
        appendCommon(NOT_EQUAL_TO, FieldValueType.STRING.getCode(), str, str2);
        return this;
    }

    public Query greaterThan(String str, int i) throws KvStoreException {
        validateField(str);
        appendCommon(GREATER_THAN, FieldValueType.INTEGER.getCode(), str, Integer.valueOf(i));
        return this;
    }

    public Query greaterThan(String str, long j) throws KvStoreException {
        validateField(str);
        appendCommon(GREATER_THAN, FieldValueType.LONG.getCode(), str, Long.valueOf(j));
        return this;
    }

    public Query greaterThan(String str, double d) throws KvStoreException {
        validateField(str);
        appendCommon(GREATER_THAN, FieldValueType.DOUBLE.getCode(), str, Double.valueOf(d));
        return this;
    }

    public Query greaterThan(String str, String str2) throws KvStoreException {
        validateField(str);
        validateValue(str2);
        appendCommon(GREATER_THAN, FieldValueType.STRING.getCode(), str, str2);
        return this;
    }

    public Query lessThan(String str, int i) throws KvStoreException {
        validateField(str);
        appendCommon(LESS_THAN, FieldValueType.INTEGER.getCode(), str, Integer.valueOf(i));
        return this;
    }

    public Query lessThan(String str, long j) throws KvStoreException {
        validateField(str);
        appendCommon(LESS_THAN, FieldValueType.LONG.getCode(), str, Long.valueOf(j));
        return this;
    }

    public Query lessThan(String str, double d) throws KvStoreException {
        validateField(str);
        appendCommon(LESS_THAN, FieldValueType.DOUBLE.getCode(), str, Double.valueOf(d));
        return this;
    }

    public Query lessThan(String str, String str2) throws KvStoreException {
        validateField(str);
        validateValue(str2);
        appendCommon(LESS_THAN, FieldValueType.STRING.getCode(), str, str2);
        return this;
    }

    public Query greaterThanOrEqualTo(String str, int i) throws KvStoreException {
        validateField(str);
        appendCommon(GREATER_THAN_OR_EQUAL_TO, FieldValueType.INTEGER.getCode(), str, Integer.valueOf(i));
        return this;
    }

    public Query greaterThanOrEqualTo(String str, long j) throws KvStoreException {
        validateField(str);
        appendCommon(GREATER_THAN_OR_EQUAL_TO, FieldValueType.LONG.getCode(), str, Long.valueOf(j));
        return this;
    }

    public Query greaterThanOrEqualTo(String str, double d) throws KvStoreException {
        validateField(str);
        appendCommon(GREATER_THAN_OR_EQUAL_TO, FieldValueType.DOUBLE.getCode(), str, Double.valueOf(d));
        return this;
    }

    public Query greaterThanOrEqualTo(String str, String str2) throws KvStoreException {
        validateField(str);
        validateValue(str2);
        appendCommon(GREATER_THAN_OR_EQUAL_TO, FieldValueType.STRING.getCode(), str, str2);
        return this;
    }

    public Query lessThanOrEqualTo(String str, int i) throws KvStoreException {
        validateField(str);
        appendCommon(LESS_THAN_OR_EQUAL_TO, FieldValueType.INTEGER.getCode(), str, Integer.valueOf(i));
        return this;
    }

    public Query lessThanOrEqualTo(String str, long j) throws KvStoreException {
        validateField(str);
        appendCommon(LESS_THAN_OR_EQUAL_TO, FieldValueType.LONG.getCode(), str, Long.valueOf(j));
        return this;
    }

    public Query lessThanOrEqualTo(String str, double d) throws KvStoreException {
        validateField(str);
        appendCommon(LESS_THAN_OR_EQUAL_TO, FieldValueType.DOUBLE.getCode(), str, Double.valueOf(d));
        return this;
    }

    public Query lessThanOrEqualTo(String str, String str2) throws KvStoreException {
        validateField(str);
        validateValue(str2);
        appendCommon(LESS_THAN_OR_EQUAL_TO, FieldValueType.STRING.getCode(), str, str2);
        return this;
    }

    public Query isNull(String str) throws KvStoreException {
        validateField(str);
        this.sqlLike.append(" ");
        this.sqlLike.append(IS_NULL);
        this.sqlLike.append(" ");
        this.sqlLike.append(escapeSpace(str));
        return this;
    }

    public Query inInt(String str, List<Integer> list) throws KvStoreException {
        validateField(str);
        validateValueList(list);
        appendCommonList(IN, FieldValueType.INTEGER.getCode(), str, list);
        return this;
    }

    public Query inLong(String str, List<Long> list) throws KvStoreException {
        validateField(str);
        validateValueList(list);
        appendCommonList(IN, FieldValueType.LONG.getCode(), str, list);
        return this;
    }

    public Query inDouble(String str, List<Double> list) throws KvStoreException {
        validateField(str);
        validateValueList(list);
        appendCommonList(IN, FieldValueType.DOUBLE.getCode(), str, list);
        return this;
    }

    public Query inString(String str, List<String> list) throws KvStoreException {
        validateField(str);
        validateValueList(list);
        appendCommonList(IN, FieldValueType.STRING.getCode(), str, list);
        return this;
    }

    public Query notInInt(String str, List<Integer> list) throws KvStoreException {
        validateField(str);
        validateValueList(list);
        appendCommonList(NOT_IN, FieldValueType.INTEGER.getCode(), str, list);
        return this;
    }

    public Query notInLong(String str, List<Long> list) throws KvStoreException {
        validateField(str);
        validateValueList(list);
        appendCommonList(NOT_IN, FieldValueType.LONG.getCode(), str, list);
        return this;
    }

    public Query notInDouble(String str, List<Double> list) throws KvStoreException {
        validateField(str);
        validateValueList(list);
        appendCommonList(NOT_IN, FieldValueType.DOUBLE.getCode(), str, list);
        return this;
    }

    public Query notInString(String str, List<String> list) throws KvStoreException {
        validateField(str);
        validateValueList(list);
        appendCommonList(NOT_IN, FieldValueType.STRING.getCode(), str, list);
        return this;
    }

    public Query like(String str, String str2) throws KvStoreException {
        validateField(str);
        validateValue(str2);
        appendCommon(LIKE, str, str2);
        return this;
    }

    public Query unlike(String str, String str2) throws KvStoreException {
        validateField(str);
        validateValue(str2);
        appendCommon(NOT_LIKE, str, str2);
        return this;
    }

    public Query and() {
        this.sqlLike.append(" ");
        this.sqlLike.append(AND);
        return this;
    }

    public Query or() {
        this.sqlLike.append(" ");
        this.sqlLike.append(OR);
        return this;
    }

    public Query orderByAsc(String str) throws KvStoreException {
        validateField(str);
        this.sqlLike.append(" ");
        this.sqlLike.append(ORDER_BY_ASC);
        this.sqlLike.append(" ");
        this.sqlLike.append(escapeSpace(str));
        return this;
    }

    public Query orderByDesc(String str) throws KvStoreException {
        validateField(str);
        this.sqlLike.append(" ");
        this.sqlLike.append(ORDER_BY_DESC);
        this.sqlLike.append(" ");
        this.sqlLike.append(escapeSpace(str));
        return this;
    }

    public Query limit(int i, int i2) {
        this.sqlLike.append(" ");
        this.sqlLike.append(LIMIT);
        this.sqlLike.append(" ");
        this.sqlLike.append(i);
        this.sqlLike.append(" ");
        this.sqlLike.append(i2);
        return this;
    }

    public Query isNotNull(String str) throws KvStoreException {
        validateField(str);
        this.sqlLike.append(" ");
        this.sqlLike.append(IS_NOT_NULL);
        this.sqlLike.append(" ");
        this.sqlLike.append(escapeSpace(str));
        return this;
    }

    public Query beginGroup() {
        this.sqlLike.append(" ");
        this.sqlLike.append(BEGIN_GROUP);
        return this;
    }

    public Query endGroup() {
        this.sqlLike.append(" ");
        this.sqlLike.append(END_GROUP);
        return this;
    }

    public Query prefixKey(String str) throws KvStoreException {
        if (str == null || str.isEmpty()) {
            LogPrint.error(LABEL, "Invalid prefix param", new Object[0]);
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "Invalid prefix param");
        }
        this.sqlLike.append(" ");
        this.sqlLike.append(KEY_PREFIX);
        this.sqlLike.append(" ");
        this.sqlLike.append(escapeSpace(str));
        return this;
    }

    public Query deviceId(String str) throws KvStoreException {
        if (str == null || str.contains(SPECIAL) || str.contains(" ")) {
            LogPrint.error(LABEL, "Invalid deviceId param", new Object[0]);
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "Invalid deviceId param");
        } else if (str.isEmpty()) {
            LogPrint.info(LABEL, "deviceId is empty String", new Object[0]);
            return this;
        } else {
            StringBuilder sb = new StringBuilder(this.sqlLike.toString());
            sb.insert(0, "^DEVICE_ID " + str);
            return new Query(sb);
        }
    }

    public String getSqlLike() {
        String trim = this.sqlLike.toString().trim();
        if (trim.getBytes(StandardCharsets.UTF_8).length <= 512000) {
            return trim;
        }
        LogPrint.error(LABEL, "Query is too long", new Object[0]);
        return "";
    }

    private void appendCommon(String str, String str2, String str3, Object obj) {
        this.sqlLike.append(" ");
        this.sqlLike.append(str);
        this.sqlLike.append(" ");
        this.sqlLike.append(str2);
        this.sqlLike.append(" ");
        this.sqlLike.append(escapeSpace(str3));
        this.sqlLike.append(" ");
        if (!str2.equals(FieldValueType.STRING.getCode()) || !(obj instanceof String)) {
            this.sqlLike.append(obj);
        } else {
            this.sqlLike.append(escapeSpace((String) obj));
        }
    }

    private void appendCommon(String str, String str2, String str3) {
        this.sqlLike.append(" ");
        this.sqlLike.append(str);
        this.sqlLike.append(" ");
        this.sqlLike.append(escapeSpace(str2));
        this.sqlLike.append(" ");
        this.sqlLike.append(escapeSpace(str3));
    }

    private void appendCommonList(String str, String str2, String str3, List<?> list) {
        this.sqlLike.append(" ");
        this.sqlLike.append(str);
        this.sqlLike.append(" ");
        this.sqlLike.append(str2);
        this.sqlLike.append(" ");
        this.sqlLike.append(escapeSpace(str3));
        this.sqlLike.append(" ");
        this.sqlLike.append(START_IN);
        this.sqlLike.append(" ");
        for (Object obj : list) {
            if (!str2.equals(FieldValueType.STRING.getCode()) || !(obj instanceof String)) {
                this.sqlLike.append(obj);
            } else {
                this.sqlLike.append(escapeSpace((String) obj));
            }
            this.sqlLike.append(" ");
        }
        this.sqlLike.append(END_IN);
    }

    private String escapeSpace(String str) {
        if (str.length() == 0) {
            return EMPTY_STRING;
        }
        return str.replace(SPECIAL, SPECIAL_ESCAPE).replace(" ", SPACE_ESCAPE);
    }

    private void validateField(String str) throws KvStoreException {
        if (str == null || str.isEmpty() || str.contains(SPECIAL)) {
            LogPrint.error(LABEL, "Invalid field param", new Object[0]);
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "Invalid field param");
        }
    }

    private void validateValue(String str) throws KvStoreException {
        if (str == null) {
            LogPrint.error(LABEL, "Invalid value param", new Object[0]);
            throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "Invalid value param");
        }
    }

    private void validateValueList(List<? extends Object> list) throws KvStoreException {
        if (list != null) {
            for (Object obj : list) {
                if (obj == null) {
                    LogPrint.error(LABEL, "Invalid valueList param", new Object[0]);
                    throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "Invalid value param");
                }
            }
            return;
        }
        LogPrint.error(LABEL, "Invalid valueList param", new Object[0]);
        throw new KvStoreException(KvStoreErrorCode.INVALID_ARGUMENT, "Invalid value param");
    }
}
