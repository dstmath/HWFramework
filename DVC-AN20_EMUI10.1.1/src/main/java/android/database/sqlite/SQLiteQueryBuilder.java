package android.database.sqlite;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.os.CancellationSignal;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.internal.util.ArrayUtils;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import libcore.util.EmptyArray;

public class SQLiteQueryBuilder {
    private static final int STRICT_COLUMNS = 2;
    private static final int STRICT_GRAMMAR = 4;
    private static final int STRICT_PARENTHESES = 1;
    private static final String TAG = "SQLiteQueryBuilder";
    private static final Pattern sAggregationPattern = Pattern.compile("(?i)(AVG|COUNT|MAX|MIN|SUM|TOTAL|GROUP_CONCAT)\\((.+)\\)");
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private boolean mDistinct = false;
    private SQLiteDatabase.CursorFactory mFactory = null;
    private List<Pattern> mProjectionGreylist = null;
    private Map<String, String> mProjectionMap = null;
    private int mStrictFlags;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private String mTables = "";
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private StringBuilder mWhereClause = null;

    public void setDistinct(boolean distinct) {
        this.mDistinct = distinct;
    }

    public boolean isDistinct() {
        return this.mDistinct;
    }

    public String getTables() {
        return this.mTables;
    }

    public void setTables(String inTables) {
        this.mTables = inTables;
    }

    public void appendWhere(CharSequence inWhere) {
        if (this.mWhereClause == null) {
            this.mWhereClause = new StringBuilder(inWhere.length() + 16);
        }
        this.mWhereClause.append(inWhere);
    }

    public void appendWhereEscapeString(String inWhere) {
        if (this.mWhereClause == null) {
            this.mWhereClause = new StringBuilder(inWhere.length() + 16);
        }
        DatabaseUtils.appendEscapedSQLString(this.mWhereClause, inWhere);
    }

    public void appendWhereStandalone(CharSequence inWhere) {
        if (this.mWhereClause == null) {
            this.mWhereClause = new StringBuilder(inWhere.length() + 16);
        }
        if (this.mWhereClause.length() > 0) {
            this.mWhereClause.append(" AND ");
        }
        StringBuilder sb = this.mWhereClause;
        sb.append('(');
        sb.append(inWhere);
        sb.append(')');
    }

    public void setProjectionMap(Map<String, String> columnMap) {
        this.mProjectionMap = columnMap;
    }

    public Map<String, String> getProjectionMap() {
        return this.mProjectionMap;
    }

    public void setProjectionGreylist(List<Pattern> projectionGreylist) {
        this.mProjectionGreylist = projectionGreylist;
    }

    public List<Pattern> getProjectionGreylist() {
        return this.mProjectionGreylist;
    }

    @Deprecated
    public void setProjectionAggregationAllowed(boolean projectionAggregationAllowed) {
    }

    @Deprecated
    public boolean isProjectionAggregationAllowed() {
        return true;
    }

    public void setCursorFactory(SQLiteDatabase.CursorFactory factory) {
        this.mFactory = factory;
    }

    public SQLiteDatabase.CursorFactory getCursorFactory() {
        return this.mFactory;
    }

    public void setStrict(boolean strict) {
        if (strict) {
            this.mStrictFlags |= 1;
        } else {
            this.mStrictFlags &= -2;
        }
    }

    public boolean isStrict() {
        return (this.mStrictFlags & 1) != 0;
    }

    public void setStrictColumns(boolean strictColumns) {
        if (strictColumns) {
            this.mStrictFlags |= 2;
        } else {
            this.mStrictFlags &= -3;
        }
    }

    public boolean isStrictColumns() {
        return (this.mStrictFlags & 2) != 0;
    }

    public void setStrictGrammar(boolean strictGrammar) {
        if (strictGrammar) {
            this.mStrictFlags |= 4;
        } else {
            this.mStrictFlags &= -5;
        }
    }

    public boolean isStrictGrammar() {
        return (this.mStrictFlags & 4) != 0;
    }

    public static String buildQueryString(boolean distinct, String tables, String[] columns, String where, String groupBy, String having, String orderBy, String limit) {
        if (!TextUtils.isEmpty(groupBy) || TextUtils.isEmpty(having)) {
            StringBuilder query = new StringBuilder(120);
            query.append("SELECT ");
            if (distinct) {
                query.append("DISTINCT ");
            }
            if (columns == null || columns.length == 0) {
                query.append("* ");
            } else {
                appendColumns(query, columns);
            }
            query.append("FROM ");
            query.append(tables);
            appendClause(query, " WHERE ", where);
            appendClause(query, " GROUP BY ", groupBy);
            appendClause(query, " HAVING ", having);
            appendClause(query, " ORDER BY ", orderBy);
            appendClause(query, " LIMIT ", limit);
            return query.toString();
        }
        throw new IllegalArgumentException("HAVING clauses are only permitted when using a groupBy clause");
    }

    private static void appendClause(StringBuilder s, String name, String clause) {
        if (!TextUtils.isEmpty(clause)) {
            s.append(name);
            s.append(clause);
        }
    }

    public static void appendColumns(StringBuilder s, String[] columns) {
        int n = columns.length;
        for (int i = 0; i < n; i++) {
            String column = columns[i];
            if (column != null) {
                if (i > 0) {
                    s.append(", ");
                }
                s.append(column);
            }
        }
        s.append(' ');
    }

    public Cursor query(SQLiteDatabase db, String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder) {
        return query(db, projectionIn, selection, selectionArgs, groupBy, having, sortOrder, null, null);
    }

    public Cursor query(SQLiteDatabase db, String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit) {
        return query(db, projectionIn, selection, selectionArgs, groupBy, having, sortOrder, limit, null);
    }

    public Cursor query(SQLiteDatabase db, String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit, CancellationSignal cancellationSignal) {
        String sql;
        if (this.mTables == null) {
            return null;
        }
        String unwrappedSql = buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
        if (isStrictColumns()) {
            enforceStrictColumns(projectionIn);
        }
        if (isStrictGrammar()) {
            enforceStrictGrammar(selection, groupBy, having, sortOrder, limit);
        }
        if (isStrict()) {
            db.validateSql(unwrappedSql, cancellationSignal);
            sql = buildQuery(projectionIn, wrap(selection), groupBy, wrap(having), sortOrder, limit);
        } else {
            sql = unwrappedSql;
        }
        if (Log.isLoggable(TAG, 3)) {
            if (Build.IS_DEBUGGABLE) {
                Log.d(TAG, sql + " with args " + Arrays.toString(selectionArgs));
            } else {
                Log.d(TAG, sql);
            }
        }
        return db.rawQueryWithFactory(this.mFactory, sql, selectionArgs, SQLiteDatabase.findEditTable(this.mTables), cancellationSignal);
    }

    public long insert(SQLiteDatabase db, ContentValues values) {
        Objects.requireNonNull(this.mTables, "No tables defined");
        Objects.requireNonNull(db, "No database defined");
        Objects.requireNonNull(values, "No values defined");
        if (isStrictColumns()) {
            enforceStrictColumns(values);
        }
        String sql = buildInsert(values);
        ArrayMap<String, Object> rawValues = values.getValues();
        Object[] sqlArgs = new Object[rawValues.size()];
        for (int i = 0; i < sqlArgs.length; i++) {
            sqlArgs[i] = rawValues.valueAt(i);
        }
        if (Log.isLoggable(TAG, 3)) {
            if (Build.IS_DEBUGGABLE) {
                Log.d(TAG, sql + " with args " + Arrays.toString(sqlArgs));
            } else {
                Log.d(TAG, sql);
            }
        }
        return (long) db.executeSql(sql, sqlArgs);
    }

    public int update(SQLiteDatabase db, ContentValues values, String selection, String[] selectionArgs) {
        String sql;
        Objects.requireNonNull(this.mTables, "No tables defined");
        Objects.requireNonNull(db, "No database defined");
        Objects.requireNonNull(values, "No values defined");
        String unwrappedSql = buildUpdate(values, selection);
        if (isStrictColumns()) {
            enforceStrictColumns(values);
        }
        if (isStrictGrammar()) {
            enforceStrictGrammar(selection, null, null, null, null);
        }
        if (isStrict()) {
            db.validateSql(unwrappedSql, null);
            sql = buildUpdate(values, wrap(selection));
        } else {
            sql = unwrappedSql;
        }
        if (selectionArgs == null) {
            selectionArgs = EmptyArray.STRING;
        }
        ArrayMap<String, Object> rawValues = values.getValues();
        int valuesLength = rawValues.size();
        Object[] sqlArgs = new Object[(selectionArgs.length + valuesLength)];
        for (int i = 0; i < sqlArgs.length; i++) {
            if (i < valuesLength) {
                sqlArgs[i] = rawValues.valueAt(i);
            } else {
                sqlArgs[i] = selectionArgs[i - valuesLength];
            }
        }
        if (Log.isLoggable(TAG, 3)) {
            if (Build.IS_DEBUGGABLE) {
                Log.d(TAG, sql + " with args " + Arrays.toString(sqlArgs));
            } else {
                Log.d(TAG, sql);
            }
        }
        return db.executeSql(sql, sqlArgs);
    }

    public int delete(SQLiteDatabase db, String selection, String[] selectionArgs) {
        String sql;
        Objects.requireNonNull(this.mTables, "No tables defined");
        Objects.requireNonNull(db, "No database defined");
        String unwrappedSql = buildDelete(selection);
        if (isStrictGrammar()) {
            enforceStrictGrammar(selection, null, null, null, null);
        }
        if (isStrict()) {
            db.validateSql(unwrappedSql, null);
            sql = buildDelete(wrap(selection));
        } else {
            sql = unwrappedSql;
        }
        if (Log.isLoggable(TAG, 3)) {
            if (Build.IS_DEBUGGABLE) {
                Log.d(TAG, sql + " with args " + Arrays.toString(selectionArgs));
            } else {
                Log.d(TAG, sql);
            }
        }
        return db.executeSql(sql, selectionArgs);
    }

    private void enforceStrictColumns(String[] projection) {
        Objects.requireNonNull(this.mProjectionMap, "No projection map defined");
        computeProjection(projection);
    }

    private void enforceStrictColumns(ContentValues values) {
        Objects.requireNonNull(this.mProjectionMap, "No projection map defined");
        ArrayMap<String, Object> rawValues = values.getValues();
        for (int i = 0; i < rawValues.size(); i++) {
            String column = rawValues.keyAt(i);
            if (!this.mProjectionMap.containsKey(column)) {
                throw new IllegalArgumentException("Invalid column " + column);
            }
        }
    }

    private void enforceStrictGrammar(String selection, String groupBy, String having, String sortOrder, String limit) {
        SQLiteTokenizer.tokenize(selection, 0, new Consumer() {
            /* class android.database.sqlite.$$Lambda$SQLiteQueryBuilder$8Xdbi4e9qj6B20afMr13v8eErCU */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SQLiteQueryBuilder.this.enforceStrictGrammarWhereHaving((String) obj);
            }
        });
        SQLiteTokenizer.tokenize(groupBy, 0, new Consumer() {
            /* class android.database.sqlite.$$Lambda$SQLiteQueryBuilder$aB4dQvsjvz__mPSMsicSH9kEAM */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SQLiteQueryBuilder.this.enforceStrictGrammarGroupBy((String) obj);
            }
        });
        SQLiteTokenizer.tokenize(having, 0, new Consumer() {
            /* class android.database.sqlite.$$Lambda$SQLiteQueryBuilder$8Xdbi4e9qj6B20afMr13v8eErCU */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SQLiteQueryBuilder.this.enforceStrictGrammarWhereHaving((String) obj);
            }
        });
        SQLiteTokenizer.tokenize(sortOrder, 0, new Consumer() {
            /* class android.database.sqlite.$$Lambda$SQLiteQueryBuilder$RN4X37kr4P69Zco8q57Mc6Pcc */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SQLiteQueryBuilder.this.enforceStrictGrammarOrderBy((String) obj);
            }
        });
        SQLiteTokenizer.tokenize(limit, 0, new Consumer() {
            /* class android.database.sqlite.$$Lambda$SQLiteQueryBuilder$Anx1KCnouTw7k46VFCNhOpzf6o */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SQLiteQueryBuilder.this.enforceStrictGrammarLimit((String) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    public void enforceStrictGrammarWhereHaving(String token) {
        if (!isTableOrColumn(token) && !SQLiteTokenizer.isFunction(token) && !SQLiteTokenizer.isType(token)) {
            String upperCase = token.toUpperCase(Locale.US);
            char c = 65535;
            switch (upperCase.hashCode()) {
                case -2125979215:
                    if (upperCase.equals("ISNULL")) {
                        c = 15;
                        break;
                    }
                    break;
                case -1986874255:
                    if (upperCase.equals("NOCASE")) {
                        c = 18;
                        break;
                    }
                    break;
                case -1881469687:
                    if (upperCase.equals("REGEXP")) {
                        c = 23;
                        break;
                    }
                    break;
                case -1447470406:
                    if (upperCase.equals("NOTNULL")) {
                        c = 20;
                        break;
                    }
                    break;
                case 2098:
                    if (upperCase.equals("AS")) {
                        c = 1;
                        break;
                    }
                    break;
                case 2341:
                    if (upperCase.equals("IN")) {
                        c = '\r';
                        break;
                    }
                    break;
                case 2346:
                    if (upperCase.equals("IS")) {
                        c = 14;
                        break;
                    }
                    break;
                case 2531:
                    if (upperCase.equals("OR")) {
                        c = 22;
                        break;
                    }
                    break;
                case 64951:
                    if (upperCase.equals("AND")) {
                        c = 0;
                        break;
                    }
                    break;
                case 68795:
                    if (upperCase.equals("END")) {
                        c = '\t';
                        break;
                    }
                    break;
                case 77491:
                    if (upperCase.equals("NOT")) {
                        c = 19;
                        break;
                    }
                    break;
                case 2061104:
                    if (upperCase.equals("CASE")) {
                        c = 4;
                        break;
                    }
                    break;
                case 2061119:
                    if (upperCase.equals("CAST")) {
                        c = 5;
                        break;
                    }
                    break;
                case 2131257:
                    if (upperCase.equals("ELSE")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 2190712:
                    if (upperCase.equals("GLOB")) {
                        c = '\f';
                        break;
                    }
                    break;
                case 2336663:
                    if (upperCase.equals("LIKE")) {
                        c = 16;
                        break;
                    }
                    break;
                case 2407815:
                    if (upperCase.equals(WifiEnterpriseConfig.EMPTY_VALUE)) {
                        c = 21;
                        break;
                    }
                    break;
                case 2573853:
                    if (upperCase.equals("THEN")) {
                        c = 25;
                        break;
                    }
                    break;
                case 2663226:
                    if (upperCase.equals("WHEN")) {
                        c = 26;
                        break;
                    }
                    break;
                case 73130405:
                    if (upperCase.equals("MATCH")) {
                        c = 17;
                        break;
                    }
                    break;
                case 78312308:
                    if (upperCase.equals("RTRIM")) {
                        c = 24;
                        break;
                    }
                    break;
                case 501348328:
                    if (upperCase.equals("BETWEEN")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1071324924:
                    if (upperCase.equals("DISTINCT")) {
                        c = 7;
                        break;
                    }
                    break;
                case 1667424262:
                    if (upperCase.equals("COLLATE")) {
                        c = 6;
                        break;
                    }
                    break;
                case 1959329793:
                    if (upperCase.equals("BINARY")) {
                        c = 3;
                        break;
                    }
                    break;
                case 2054124673:
                    if (upperCase.equals("ESCAPE")) {
                        c = '\n';
                        break;
                    }
                    break;
                case 2058938460:
                    if (upperCase.equals("EXISTS")) {
                        c = 11;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case '\b':
                case '\t':
                case '\n':
                case 11:
                case '\f':
                case '\r':
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                case 26:
                    return;
                default:
                    throw new IllegalArgumentException("Invalid token " + token);
            }
        }
    }

    /* access modifiers changed from: private */
    public void enforceStrictGrammarGroupBy(String token) {
        if (!isTableOrColumn(token)) {
            throw new IllegalArgumentException("Invalid token " + token);
        }
    }

    /* access modifiers changed from: private */
    public void enforceStrictGrammarOrderBy(String token) {
        if (!isTableOrColumn(token)) {
            String upperCase = token.toUpperCase(Locale.US);
            char c = 65535;
            switch (upperCase.hashCode()) {
                case -1986874255:
                    if (upperCase.equals("NOCASE")) {
                        c = 5;
                        break;
                    }
                    break;
                case 65105:
                    if (upperCase.equals("ASC")) {
                        c = 1;
                        break;
                    }
                    break;
                case 2094737:
                    if (upperCase.equals("DESC")) {
                        c = 2;
                        break;
                    }
                    break;
                case 78312308:
                    if (upperCase.equals("RTRIM")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1667424262:
                    if (upperCase.equals("COLLATE")) {
                        c = 0;
                        break;
                    }
                    break;
                case 1959329793:
                    if (upperCase.equals("BINARY")) {
                        c = 3;
                        break;
                    }
                    break;
            }
            if (c != 0 && c != 1 && c != 2 && c != 3 && c != 4 && c != 5) {
                throw new IllegalArgumentException("Invalid token " + token);
            }
        }
    }

    /* access modifiers changed from: private */
    public void enforceStrictGrammarLimit(String token) {
        String upperCase = token.toUpperCase(Locale.US);
        if (((upperCase.hashCode() == -1966450541 && upperCase.equals("OFFSET")) ? (char) 0 : 65535) != 0) {
            throw new IllegalArgumentException("Invalid token " + token);
        }
    }

    public String buildQuery(String[] projectionIn, String selection, String groupBy, String having, String sortOrder, String limit) {
        return buildQueryString(this.mDistinct, this.mTables, computeProjection(projectionIn), computeWhere(selection), groupBy, having, sortOrder, limit);
    }

    @Deprecated
    public String buildQuery(String[] projectionIn, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder, String limit) {
        return buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
    }

    public String buildInsert(ContentValues values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Empty values");
        }
        StringBuilder sql = new StringBuilder(120);
        sql.append("INSERT INTO ");
        sql.append(SQLiteDatabase.findEditTable(this.mTables));
        sql.append(" (");
        ArrayMap<String, Object> rawValues = values.getValues();
        for (int i = 0; i < rawValues.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append(rawValues.keyAt(i));
        }
        sql.append(") VALUES (");
        for (int i2 = 0; i2 < rawValues.size(); i2++) {
            if (i2 > 0) {
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(")");
        return sql.toString();
    }

    public String buildUpdate(ContentValues values, String selection) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Empty values");
        }
        StringBuilder sql = new StringBuilder(120);
        sql.append("UPDATE ");
        sql.append(SQLiteDatabase.findEditTable(this.mTables));
        sql.append(" SET ");
        ArrayMap<String, Object> rawValues = values.getValues();
        for (int i = 0; i < rawValues.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append(rawValues.keyAt(i));
            sql.append("=?");
        }
        appendClause(sql, " WHERE ", computeWhere(selection));
        return sql.toString();
    }

    public String buildDelete(String selection) {
        StringBuilder sql = new StringBuilder(120);
        sql.append("DELETE FROM ");
        sql.append(SQLiteDatabase.findEditTable(this.mTables));
        appendClause(sql, " WHERE ", computeWhere(selection));
        return sql.toString();
    }

    public String buildUnionSubQuery(String typeDiscriminatorColumn, String[] unionColumns, Set<String> columnsPresentInTable, int computedColumnsOffset, String typeDiscriminatorValue, String selection, String groupBy, String having) {
        int unionColumnsCount = unionColumns.length;
        String[] projectionIn = new String[unionColumnsCount];
        for (int i = 0; i < unionColumnsCount; i++) {
            String unionColumn = unionColumns[i];
            if (unionColumn.equals(typeDiscriminatorColumn)) {
                projectionIn[i] = "'" + typeDiscriminatorValue + "' AS " + typeDiscriminatorColumn;
            } else {
                if (i > computedColumnsOffset) {
                    if (!columnsPresentInTable.contains(unionColumn)) {
                        projectionIn[i] = "NULL AS " + unionColumn;
                    }
                }
                projectionIn[i] = unionColumn;
            }
        }
        return buildQuery(projectionIn, selection, groupBy, having, null, null);
    }

    @Deprecated
    public String buildUnionSubQuery(String typeDiscriminatorColumn, String[] unionColumns, Set<String> columnsPresentInTable, int computedColumnsOffset, String typeDiscriminatorValue, String selection, String[] selectionArgs, String groupBy, String having) {
        return buildUnionSubQuery(typeDiscriminatorColumn, unionColumns, columnsPresentInTable, computedColumnsOffset, typeDiscriminatorValue, selection, groupBy, having);
    }

    public String buildUnionQuery(String[] subQueries, String sortOrder, String limit) {
        StringBuilder query = new StringBuilder(128);
        int subQueryCount = subQueries.length;
        String unionOperator = this.mDistinct ? " UNION " : " UNION ALL ";
        for (int i = 0; i < subQueryCount; i++) {
            if (i > 0) {
                query.append(unionOperator);
            }
            query.append(subQueries[i]);
        }
        appendClause(query, " ORDER BY ", sortOrder);
        appendClause(query, " LIMIT ", limit);
        return query.toString();
    }

    private static String maybeWithOperator(String operator, String column) {
        if (operator == null) {
            return column;
        }
        return operator + "(" + column + ")";
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public String[] computeProjection(String[] projectionIn) {
        if (!ArrayUtils.isEmpty(projectionIn)) {
            String[] projectionOut = new String[projectionIn.length];
            for (int i = 0; i < projectionIn.length; i++) {
                projectionOut[i] = computeSingleProjectionOrThrow(projectionIn[i]);
            }
            return projectionOut;
        }
        Map<String, String> map = this.mProjectionMap;
        if (map == null) {
            return null;
        }
        Set<Map.Entry<String, String>> entrySet = map.entrySet();
        String[] projection = new String[entrySet.size()];
        int i2 = 0;
        for (Map.Entry<String, String> entry : entrySet) {
            if (!entry.getKey().equals(BaseColumns._COUNT)) {
                projection[i2] = entry.getValue();
                i2++;
            }
        }
        return projection;
    }

    private String computeSingleProjectionOrThrow(String userColumn) {
        String column = computeSingleProjection(userColumn);
        if (column != null) {
            return column;
        }
        throw new IllegalArgumentException("Invalid column " + userColumn);
    }

    private String computeSingleProjection(String userColumn) {
        Map<String, String> map = this.mProjectionMap;
        if (map == null) {
            return userColumn;
        }
        String operator = null;
        String column = map.get(userColumn);
        if (column == null) {
            Matcher matcher = sAggregationPattern.matcher(userColumn);
            if (matcher.matches()) {
                operator = matcher.group(1);
                userColumn = matcher.group(2);
                column = this.mProjectionMap.get(userColumn);
            }
        }
        if (column != null) {
            return maybeWithOperator(operator, column);
        }
        if (this.mStrictFlags == 0 && (userColumn.contains(" AS ") || userColumn.contains(" as "))) {
            return maybeWithOperator(operator, userColumn);
        }
        List<Pattern> list = this.mProjectionGreylist;
        if (list == null) {
            return null;
        }
        boolean match = false;
        Iterator<Pattern> it = list.iterator();
        while (true) {
            if (it.hasNext()) {
                if (it.next().matcher(userColumn).matches()) {
                    match = true;
                    break;
                }
            } else {
                break;
            }
        }
        if (!match) {
            return null;
        }
        Log.w(TAG, "Allowing abusive custom column: " + userColumn);
        return maybeWithOperator(operator, userColumn);
    }

    private boolean isTableOrColumn(String token) {
        if (!this.mTables.equals(token) && computeSingleProjection(token) == null) {
            return false;
        }
        return true;
    }

    public String computeWhere(String selection) {
        boolean hasInternal = !TextUtils.isEmpty(this.mWhereClause);
        boolean hasExternal = !TextUtils.isEmpty(selection);
        if (!hasInternal && !hasExternal) {
            return null;
        }
        StringBuilder where = new StringBuilder();
        if (hasInternal) {
            where.append('(');
            where.append((CharSequence) this.mWhereClause);
            where.append(')');
        }
        if (hasInternal && hasExternal) {
            where.append(" AND ");
        }
        if (hasExternal) {
            where.append('(');
            where.append(selection);
            where.append(')');
        }
        return where.toString();
    }

    private String wrap(String arg) {
        if (TextUtils.isEmpty(arg)) {
            return arg;
        }
        return "(" + arg + ")";
    }
}
