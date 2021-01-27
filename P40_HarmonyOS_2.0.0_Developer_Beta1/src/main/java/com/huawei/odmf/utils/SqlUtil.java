package com.huawei.odmf.utils;

import com.huawei.odmf.model.api.Attribute;
import java.util.List;

public class SqlUtil {
    private SqlUtil() {
    }

    public static StringBuilder appendProperty(StringBuilder sb, String str, Attribute attribute) {
        if (str != null) {
            sb.append(str);
            sb.append('.');
        }
        sb.append('\"');
        sb.append(attribute.getColumnName());
        sb.append('\"');
        return sb;
    }

    public static StringBuilder appendColumn(StringBuilder sb, String str) {
        sb.append('\"');
        sb.append(str);
        sb.append('\"');
        return sb;
    }

    public static StringBuilder appendColumn(StringBuilder sb, String str, String str2) {
        sb.append(str);
        sb.append(".\"");
        sb.append(str2);
        sb.append('\"');
        return sb;
    }

    public static StringBuilder appendColumns(StringBuilder sb, String str, String[] strArr) {
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            appendColumn(sb, str, strArr[i]);
            if (i < length - 1) {
                sb.append(',');
            }
        }
        return sb;
    }

    public static StringBuilder appendColumns(StringBuilder sb, String[] strArr) {
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            sb.append('\"');
            sb.append(strArr[i]);
            sb.append('\"');
            if (i < length - 1) {
                sb.append(',');
            }
        }
        return sb;
    }

    public static StringBuilder appendPlaceholders(StringBuilder sb, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            if (i2 < i - 1) {
                sb.append("?,");
            } else {
                sb.append('?');
            }
        }
        return sb;
    }

    public static StringBuilder appendColumnsEqualPlaceholders(StringBuilder sb, String[] strArr) {
        for (int i = 0; i < strArr.length; i++) {
            appendColumn(sb, strArr[i]).append("=?");
            if (i < strArr.length - 1) {
                sb.append(',');
            }
        }
        return sb;
    }

    public static StringBuilder appendColumnsEqValue(StringBuilder sb, String str, String[] strArr) {
        for (int i = 0; i < strArr.length; i++) {
            appendColumn(sb, str, strArr[i]).append("=?");
            if (i < strArr.length - 1) {
                sb.append(" AND ");
            }
        }
        return sb;
    }

    public static String createSqlInsert(String str, List<? extends Attribute> list) {
        int size = list.size();
        String[] strArr = new String[size];
        for (int i = 0; i < size; i++) {
            strArr[i] = ((Attribute) list.get(i)).getColumnName();
        }
        return createSqlInsert(str, strArr);
    }

    public static String createSqlInsert(String str, String[] strArr) {
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append('\"');
        sb.append(str);
        sb.append('\"');
        sb.append(" (");
        appendColumns(sb, strArr);
        sb.append(") VALUES (");
        appendPlaceholders(sb, strArr.length);
        sb.append(')');
        return sb.toString();
    }

    public static String createSqlDelete(String str, String[] strArr) {
        String str2 = '\"' + str + '\"';
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        sb.append(str2);
        if (strArr != null && strArr.length > 0) {
            sb.append(" WHERE ");
            appendColumnsEqValue(sb, str2, strArr);
        }
        return sb.toString();
    }

    public static String createSqlUpdate(String str, List<? extends Attribute> list, String[] strArr) {
        int size = list.size();
        String[] strArr2 = new String[size];
        for (int i = 0; i < size; i++) {
            strArr2[i] = ((Attribute) list.get(i)).getColumnName();
        }
        return createSqlUpdate(str, strArr2, strArr);
    }

    public static String createSqlUpdate(String str, String[] strArr, String[] strArr2) {
        String str2 = '\"' + str + '\"';
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(str2);
        sb.append(" SET ");
        appendColumnsEqualPlaceholders(sb, strArr);
        sb.append(" WHERE ");
        appendColumnsEqValue(sb, str2, strArr2);
        return sb.toString();
    }

    public static String createSqlIndex(String str, String str2, List<? extends Attribute> list) {
        int size = list.size();
        String[] strArr = new String[size];
        for (int i = 0; i < size; i++) {
            strArr[i] = ((Attribute) list.get(i)).getColumnName();
        }
        return createSqlIndex(str, str2, strArr);
    }

    public static String createSqlIndex(String str, String str2, String[] strArr) {
        StringBuilder sb = new StringBuilder("CREATE INDEX ");
        sb.append('\"' + str2 + '\"');
        sb.append(" ON ");
        sb.append('\"' + str + '\"');
        sb.append(" (");
        appendColumns(sb, strArr);
        sb.append(" )");
        return sb.toString();
    }
}
