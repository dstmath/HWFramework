package ohos.data.rdb.impl;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ohos.data.orm.StringUtils;
import ohos.data.rdb.AbsRdbPredicates;
import ohos.data.rdb.RawRdbPredicates;
import ohos.data.rdb.RdbConstraintException;
import ohos.data.rdb.RdbPredicates;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.ValuesBucket;

public class SqliteSqlBuilder {
    private static final int DEFAULT_SQL_LENGTH = 120;
    private static final String[] ON_CONFLICT_CLAUSE = {"", " OR ROLLBACK", " OR ABORT", " OR FAIL", " OR IGNORE", " OR REPLACE"};

    private SqliteSqlBuilder() {
    }

    public static String buildDeleteString(AbsRdbPredicates absRdbPredicates) {
        StringBuilder sb = new StringBuilder(120);
        String tableNames = getTableNames(absRdbPredicates);
        sb.append("Delete ");
        sb.append("FROM ");
        sb.append(tableNames);
        sb.append(buildSqlStringFromPredicates(absRdbPredicates));
        return sb.toString();
    }

    public static String buildUpdateString(ValuesBucket valuesBucket, AbsRdbPredicates absRdbPredicates, Object[] objArr, RdbStore.ConflictResolution conflictResolution) {
        StringBuilder sb = new StringBuilder(120);
        String tableNames = getTableNames(absRdbPredicates);
        if (conflictResolution == null) {
            conflictResolution = RdbStore.ConflictResolution.ON_CONFLICT_NONE;
        }
        sb.append("UPDATE");
        sb.append(ON_CONFLICT_CLAUSE[conflictResolution.getValue()]);
        sb.append(" ");
        sb.append(tableNames);
        sb.append(" SET ");
        int i = 0;
        int i2 = 0;
        for (Map.Entry<String, Object> entry : valuesBucket.getAll()) {
            sb.append(i2 > 0 ? "," : "");
            sb.append(entry.getKey());
            sb.append("=?");
            objArr[i2] = entry.getValue();
            i2++;
        }
        List<String> whereArgs = absRdbPredicates.getWhereArgs();
        if (whereArgs != null && !whereArgs.isEmpty()) {
            while (i < whereArgs.size()) {
                objArr[i2] = whereArgs.get(i);
                i++;
                i2++;
            }
        }
        sb.append(buildSqlStringFromPredicates(absRdbPredicates));
        return sb.toString();
    }

    public static String buildQueryString(AbsRdbPredicates absRdbPredicates, String[] strArr) {
        StringBuilder sb = new StringBuilder(120);
        boolean isDistinct = absRdbPredicates.isDistinct();
        String tableNames = getTableNames(absRdbPredicates);
        sb.append("SELECT ");
        if (isDistinct) {
            sb.append("DISTINCT ");
        }
        if (strArr == null || strArr.length == 0) {
            sb.append("* ");
        } else {
            appendColumns(sb, strArr);
        }
        sb.append("FROM ");
        sb.append(tableNames);
        sb.append(buildSqlStringFromPredicates(absRdbPredicates));
        return sb.toString();
    }

    public static String buildQueryStringWithExpr(AbsRdbPredicates absRdbPredicates, String[] strArr) {
        StringBuilder sb = new StringBuilder(120);
        boolean isDistinct = absRdbPredicates.isDistinct();
        String tableNames = getTableNames(absRdbPredicates);
        sb.append("SELECT ");
        if (isDistinct) {
            sb.append("DISTINCT ");
        }
        if (strArr == null || strArr.length == 0) {
            sb.append("* ");
        } else {
            appendExpr(sb, strArr);
        }
        sb.append("FROM ");
        sb.append(tableNames);
        sb.append(buildSqlStringFromPredicates(absRdbPredicates));
        return sb.toString();
    }

    public static String buildCountString(AbsRdbPredicates absRdbPredicates) {
        StringBuilder sb = new StringBuilder(120);
        String tableNames = getTableNames(absRdbPredicates);
        sb.append("SELECT COUNT(*) FROM ");
        sb.append(tableNames);
        sb.append(buildSqlStringFromPredicates(absRdbPredicates));
        return sb.toString();
    }

    private static String buildSqlStringFromPredicates(AbsRdbPredicates absRdbPredicates) {
        String str;
        StringBuilder sb = new StringBuilder(120);
        String index = absRdbPredicates.getIndex();
        String whereClause = absRdbPredicates.getWhereClause();
        String group = absRdbPredicates.getGroup();
        String order = absRdbPredicates.getOrder();
        Integer limit = absRdbPredicates.getLimit();
        Integer offset = absRdbPredicates.getOffset();
        String str2 = null;
        if (limit == null) {
            str = null;
        } else {
            str = limit.toString();
        }
        if (offset != null) {
            str2 = offset.toString();
        }
        appendClause(sb, " INDEXED BY ", index);
        appendClause(sb, " WHERE ", whereClause);
        appendClause(sb, " GROUP BY ", group);
        appendClause(sb, " ORDER BY ", order);
        appendClause(sb, " LIMIT ", str);
        appendClause(sb, " OFFSET ", str2);
        return sb.toString();
    }

    private static void appendClause(StringBuilder sb, String str, String str2) {
        if (isNotEmptyString(str2)) {
            sb.append(str);
            sb.append(str2);
        }
    }

    private static void appendColumns(StringBuilder sb, String[] strArr) {
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            String str = strArr[i];
            if (str != null) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(normalize(str));
            }
        }
        sb.append(' ');
    }

    private static void appendExpr(StringBuilder sb, String[] strArr) {
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            String str = strArr[i];
            if (str != null) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(str);
            }
        }
        sb.append(' ');
    }

    private static boolean isNotEmptyString(String str) {
        return str != null && !str.isEmpty();
    }

    private static String getTableNames(AbsRdbPredicates absRdbPredicates) {
        if (absRdbPredicates instanceof RawRdbPredicates) {
            return absRdbPredicates.getTableName();
        }
        if (!(absRdbPredicates instanceof RdbPredicates)) {
            return "";
        }
        RdbPredicates rdbPredicates = (RdbPredicates) absRdbPredicates;
        return rdbPredicates.getJoinClause() == null ? rdbPredicates.getTableName() : rdbPredicates.getJoinClause();
    }

    public static String normalize(String str) {
        if (StringUtils.isEmpty(str)) {
            return "";
        }
        if (Pattern.compile("^(\\w+)$").matcher(str).find()) {
            return StringUtils.surroundWithQuote(str, "`");
        }
        if ("*".equals(str)) {
            return "*";
        }
        if (Pattern.compile("^(['\"`])?([^`\"']+)\\1$").matcher(str).find()) {
            return str;
        }
        return normalizeComplexPattern(str);
    }

    private static String normalizeComplexPattern(String str) {
        String trim = Pattern.compile("['\"`]").matcher(str).replaceAll("").trim();
        Matcher matcher = Pattern.compile("^(\\w+|\\*)\\s*([.]\\s*(\\w+|\\*))?\\s*((?i)as)?\\s*(\\w+)?$").matcher(trim);
        if (!matcher.find()) {
            return StringUtils.surroundWithQuote(trim, "`");
        }
        String group = matcher.group(1);
        String group2 = matcher.group(3);
        String group3 = matcher.group(5);
        if (StringUtils.isEmpty(group)) {
            return StringUtils.surroundWithQuote(trim, "`");
        }
        StringBuilder sb = new StringBuilder(StringUtils.surroundWithQuote(group, "`"));
        if (!StringUtils.isEmpty(group2)) {
            if ("*".equals(group2)) {
                sb.append(".");
                sb.append(group2);
            } else {
                sb.append(".");
                sb.append(StringUtils.surroundWithQuote(group2, "`"));
            }
        }
        if (!StringUtils.isEmpty(group3)) {
            if (!"*".equals(group2)) {
                sb.append(" as ");
                sb.append(StringUtils.surroundWithQuote(group3, "`"));
            } else {
                throw new RdbConstraintException("Cannot set alias to *!");
            }
        }
        return sb.toString();
    }
}
