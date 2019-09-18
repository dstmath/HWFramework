package com.huawei.odmf.core;

import android.database.Cursor;
import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.predicate.FetchRequest;

public class DatabaseQueryService {
    private static final int ODMF_ROWID_INDEX = 0;
    private static final String ODMF_SQLITE_ROWID = "rowid";

    public static String getRowidColumnName() {
        return ODMF_SQLITE_ROWID;
    }

    public static int getOdmfRowidIndex() {
        return 0;
    }

    public static Cursor query(DataBase db, String tableName, FetchRequest request) {
        String[] columns;
        checkParam(db, tableName, request);
        String selection = request.getSqlRequest() == null ? null : request.getSqlRequest().toString();
        String[] selectionArgs = request.getSelectionArgs() == null ? null : request.getSelectionArgs();
        String order = (request.getOrder() == null || request.getOrder().toString().equals("")) ? null : request.getOrder().toString();
        String limit = (request.getLimit() == null || request.getLimit().equals("")) ? null : request.getLimit();
        if (request.getIsJoined()) {
            String[] tableNameSplit = tableName.split("\\s+");
            columns = new String[]{"DISTINCT " + (tableNameSplit[0] + "." + getRowidColumnName()) + " AS " + getRowidColumnName(), tableNameSplit[0] + ".*"};
        } else {
            columns = new String[]{getRowidColumnName() + " AS " + getRowidColumnName(), "*"};
        }
        return db.query(false, tableName, columns, selection, selectionArgs, null, null, order, limit);
    }

    public static Cursor query(DataBase db, String tableName, String[] columns, String selection, String[] selectionArgs) {
        return db.query(tableName, columns, selection, selectionArgs, null, null, null, null);
    }

    public static Cursor query(DataBase db, boolean distinct, String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        return db.commonquery(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public static Cursor queryRowID(DataBase db, String tableName, FetchRequest request) {
        checkParam(db, tableName, request);
        return db.query(false, tableName, request.getIsJoined() ? new String[]{(tableName.split("\\s+")[0] + "." + getRowidColumnName()) + " AS " + getRowidColumnName()} : new String[]{getRowidColumnName() + " AS " + getRowidColumnName()}, request.getSqlRequest() == null ? null : request.getSqlRequest().toString(), request.getSelectionArgs() == null ? null : request.getSelectionArgs(), null, null, (request.getOrder() == null || request.getOrder().toString().equals("")) ? null : request.getOrder().toString(), (request.getLimit() == null || request.getLimit().equals("")) ? null : request.getLimit());
    }

    public static Cursor queryWithAggregateFunction(DataBase db, String tableName, FetchRequest request) {
        checkParam(db, tableName, request);
        return db.query(false, tableName, request.getColumnsWithAggregateFunction() == null ? null : request.getColumnsWithAggregateFunction(), request.getSqlRequest() == null ? null : request.getSqlRequest().toString(), request.getSelectionArgs() == null ? null : request.getSelectionArgs(), null, null, (request.getOrder() == null || request.getOrder().toString().equals("")) ? null : request.getOrder().toString(), (request.getLimit() == null || request.getLimit().equals("")) ? null : request.getLimit());
    }

    public static Cursor commonquery(DataBase db, String tableName, FetchRequest request) {
        String[] columns;
        checkParam(db, tableName, request);
        String selection = request.getSqlRequest() == null ? null : request.getSqlRequest().toString();
        String[] selectionArgs = request.getSelectionArgs() == null ? null : request.getSelectionArgs();
        String order = (request.getOrder() == null || request.getOrder().toString().equals("")) ? null : request.getOrder().toString();
        String limit = (request.getLimit() == null || request.getLimit().equals("")) ? null : request.getLimit();
        if (request.getIsJoined()) {
            String[] tableNameSplit = tableName.split("\\s+");
            columns = new String[]{"DISTINCT " + (tableNameSplit[0] + "." + getRowidColumnName()) + " AS " + getRowidColumnName(), tableNameSplit[0] + ".*"};
        } else {
            columns = new String[]{getRowidColumnName() + " AS " + getRowidColumnName(), "*"};
        }
        return db.commonquery(false, tableName, columns, selection, selectionArgs, null, null, order, limit);
    }

    private static void checkParam(DataBase db, String tableName, FetchRequest request) throws IllegalArgumentException, IllegalStateException {
        if (db == null || tableName == null || tableName.equals("") || request == null) {
            throw new ODMFIllegalArgumentException("Some parameter is null");
        } else if (!db.isOpen()) {
            throw new ODMFIllegalStateException("The database has been closed");
        }
    }
}
