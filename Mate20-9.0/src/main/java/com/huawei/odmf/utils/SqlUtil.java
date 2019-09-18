package com.huawei.odmf.utils;

import com.huawei.odmf.model.api.Attribute;
import java.util.List;

public class SqlUtil {
    public static StringBuilder appendProperty(StringBuilder builder, String tablePrefix, Attribute attribute) {
        if (tablePrefix != null) {
            builder.append(tablePrefix).append('.');
        }
        builder.append('\"').append(attribute.getColumnName()).append('\"');
        return builder;
    }

    public static StringBuilder appendColumn(StringBuilder builder, String column) {
        builder.append('\"').append(column).append('\"');
        return builder;
    }

    public static StringBuilder appendColumn(StringBuilder builder, String tableAlias, String column) {
        builder.append(tableAlias).append(".\"").append(column).append('\"');
        return builder;
    }

    public static StringBuilder appendColumns(StringBuilder builder, String tableAlias, String[] columns) {
        int length = columns.length;
        for (int i = 0; i < length; i++) {
            appendColumn(builder, tableAlias, columns[i]);
            if (i < length - 1) {
                builder.append(',');
            }
        }
        return builder;
    }

    public static StringBuilder appendColumns(StringBuilder builder, String[] columns) {
        int length = columns.length;
        for (int i = 0; i < length; i++) {
            builder.append('\"').append(columns[i]).append('\"');
            if (i < length - 1) {
                builder.append(',');
            }
        }
        return builder;
    }

    public static StringBuilder appendPlaceholders(StringBuilder builder, int count) {
        for (int i = 0; i < count; i++) {
            if (i < count - 1) {
                builder.append("?,");
            } else {
                builder.append('?');
            }
        }
        return builder;
    }

    public static StringBuilder appendColumnsEqualPlaceholders(StringBuilder builder, String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            appendColumn(builder, columns[i]).append("=?");
            if (i < columns.length - 1) {
                builder.append(',');
            }
        }
        return builder;
    }

    public static StringBuilder appendColumnsEqValue(StringBuilder builder, String tableAlias, String[] columns) {
        for (int i = 0; i < columns.length; i++) {
            appendColumn(builder, tableAlias, columns[i]).append("=?");
            if (i < columns.length - 1) {
                builder.append(" AND ");
            }
        }
        return builder;
    }

    public static String createSqlInsert(String tableName, List<? extends Attribute> attributes) {
        int size = attributes.size();
        String[] columns = new String[size];
        for (int i = 0; i < size; i++) {
            columns[i] = ((Attribute) attributes.get(i)).getColumnName();
        }
        return createSqlInsert(tableName, columns);
    }

    public static String createSqlInsert(String tableName, String[] columns) {
        StringBuilder builder = new StringBuilder("INSERT INTO ");
        builder.append('\"').append(tableName).append('\"').append(" (");
        appendColumns(builder, columns);
        builder.append(") VALUES (");
        appendPlaceholders(builder, columns.length);
        builder.append(')');
        return builder.toString();
    }

    public static String createSqlDelete(String tableName, String[] columns) {
        String quotedTableName = '\"' + tableName + '\"';
        StringBuilder builder = new StringBuilder("DELETE FROM ");
        builder.append(quotedTableName);
        if (columns != null && columns.length > 0) {
            builder.append(" WHERE ");
            appendColumnsEqValue(builder, quotedTableName, columns);
        }
        return builder.toString();
    }

    public static String createSqlUpdate(String tableName, List<? extends Attribute> attributes, String[] whereColumns) {
        int size = attributes.size();
        String[] columns = new String[size];
        for (int i = 0; i < size; i++) {
            columns[i] = ((Attribute) attributes.get(i)).getColumnName();
        }
        return createSqlUpdate(tableName, columns, whereColumns);
    }

    public static String createSqlUpdate(String tableName, String[] updateColumns, String[] whereColumns) {
        String quotedTableName = '\"' + tableName + '\"';
        StringBuilder builder = new StringBuilder("UPDATE ");
        builder.append(quotedTableName).append(" SET ");
        appendColumnsEqualPlaceholders(builder, updateColumns);
        builder.append(" WHERE ");
        appendColumnsEqValue(builder, quotedTableName, whereColumns);
        return builder.toString();
    }

    public static String createSqlIndex(String tableName, String indexName, List<? extends Attribute> attributes) {
        int size = attributes.size();
        String[] columns = new String[size];
        for (int i = 0; i < size; i++) {
            columns[i] = ((Attribute) attributes.get(i)).getColumnName();
        }
        return createSqlIndex(tableName, indexName, columns);
    }

    public static String createSqlIndex(String tableName, String indexName, String[] columns) {
        StringBuilder builder = new StringBuilder("CREATE INDEX ");
        builder.append('\"' + indexName + '\"').append(" ON ").append('\"' + tableName + '\"').append(" (");
        appendColumns(builder, columns);
        builder.append(" )");
        return builder.toString();
    }
}
