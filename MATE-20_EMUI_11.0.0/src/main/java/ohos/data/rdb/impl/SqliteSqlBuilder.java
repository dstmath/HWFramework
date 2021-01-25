package ohos.data.rdb.impl;

import java.util.List;
import java.util.Map;
import ohos.data.rdb.RdbPredicates;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.ValuesBucket;

public class SqliteSqlBuilder {
    private static final int DEFAULT_SQL_LENGTH = 120;
    private static final String[] ON_CONFLICT_CLAUSE = {"", " OR ROLLBACK", " OR ABORT", " OR FAIL", " OR IGNORE", " OR REPLACE"};

    private SqliteSqlBuilder() {
    }

    public static String buildDeleteString(RdbPredicates rdbPredicates) {
        StringBuilder sb = new StringBuilder(120);
        String tableName = rdbPredicates.getJoinClause() == null ? rdbPredicates.getTableName() : rdbPredicates.getJoinClause();
        sb.append("Delete ");
        sb.append("FROM ");
        sb.append(tableName);
        sb.append(buildSqlStringFromPredicates(rdbPredicates));
        return sb.toString();
    }

    public static String buildUpdateString(ValuesBucket valuesBucket, RdbPredicates rdbPredicates, Object[] objArr, RdbStore.ConflictResolution conflictResolution) {
        StringBuilder sb = new StringBuilder(120);
        String tableName = rdbPredicates.getJoinClause() == null ? rdbPredicates.getTableName() : rdbPredicates.getJoinClause();
        if (conflictResolution == null) {
            conflictResolution = RdbStore.ConflictResolution.ON_CONFLICT_NONE;
        }
        sb.append("UPDATE");
        sb.append(ON_CONFLICT_CLAUSE[conflictResolution.getValue()]);
        sb.append(" ");
        sb.append(tableName);
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
        List<String> whereArgs = rdbPredicates.getWhereArgs();
        if (whereArgs != null && !whereArgs.isEmpty()) {
            while (i < whereArgs.size()) {
                objArr[i2] = whereArgs.get(i);
                i++;
                i2++;
            }
        }
        sb.append(buildSqlStringFromPredicates(rdbPredicates));
        return sb.toString();
    }

    public static String buildQueryString(RdbPredicates rdbPredicates, String[] strArr) {
        StringBuilder sb = new StringBuilder(120);
        boolean isDistinct = rdbPredicates.isDistinct();
        String tableName = rdbPredicates.getJoinClause() == null ? rdbPredicates.getTableName() : rdbPredicates.getJoinClause();
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
        sb.append(tableName);
        sb.append(buildSqlStringFromPredicates(rdbPredicates));
        return sb.toString();
    }

    public static String buildCountString(RdbPredicates rdbPredicates) {
        StringBuilder sb = new StringBuilder(120);
        String tableName = rdbPredicates.getJoinClause() == null ? rdbPredicates.getTableName() : rdbPredicates.getJoinClause();
        sb.append("SELECT COUNT(*) FROM ");
        sb.append(tableName);
        sb.append(buildSqlStringFromPredicates(rdbPredicates));
        return sb.toString();
    }

    private static String buildSqlStringFromPredicates(RdbPredicates rdbPredicates) {
        String str;
        StringBuilder sb = new StringBuilder(120);
        String index = rdbPredicates.getIndex();
        String whereClause = rdbPredicates.getWhereClause();
        String group = rdbPredicates.getGroup();
        String order = rdbPredicates.getOrder();
        Integer limit = rdbPredicates.getLimit();
        Integer offset = rdbPredicates.getOffset();
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
                sb.append(str);
            }
        }
        sb.append(' ');
    }

    private static boolean isNotEmptyString(String str) {
        return str != null && !str.isEmpty();
    }
}
