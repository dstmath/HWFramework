package com.huawei.odmf.core;

import android.database.Cursor;
import com.huawei.odmf.database.DataBase;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.predicate.FetchRequest;

public class DatabaseQueryService {
    private static final int ODMF_ROWID_INDEX = 0;
    private static final String ODMF_SQLITE_ROWID = "rowid";

    public static int getOdmfRowidIndex() {
        return 0;
    }

    public static String getRowidColumnName() {
        return ODMF_SQLITE_ROWID;
    }

    private DatabaseQueryService() {
    }

    public static Cursor query(DataBase dataBase, String str, FetchRequest fetchRequest) {
        String[] strArr;
        checkParam(dataBase, str, fetchRequest);
        String str2 = null;
        String sb = fetchRequest.getSqlRequest() == null ? null : fetchRequest.getSqlRequest().toString();
        String[] selectionArgs = fetchRequest.getSelectionArgs();
        String checkOrderNull = checkOrderNull(fetchRequest.getOrder());
        if (fetchRequest.getLimit() != null && !fetchRequest.getLimit().equals("")) {
            str2 = fetchRequest.getLimit();
        }
        if (fetchRequest.getIsJoined()) {
            String[] split = str.split("\\s+");
            strArr = new String[]{"DISTINCT " + (split[0] + "." + getRowidColumnName()) + " AS " + getRowidColumnName(), split[0] + ".*"};
        } else {
            strArr = new String[]{getRowidColumnName() + " AS " + getRowidColumnName(), "*"};
        }
        return dataBase.query(false, str, strArr, sb, selectionArgs, null, null, checkOrderNull, str2);
    }

    private static String checkOrderNull(StringBuilder sb) {
        if (sb == null || "".equals(sb.toString())) {
            return null;
        }
        return sb.toString();
    }

    public static Cursor query(DataBase dataBase, String str, String[] strArr, String str2, String[] strArr2) {
        return dataBase.query(str, strArr, str2, strArr2, null, null, null, null);
    }

    public static Cursor query(DataBase dataBase, boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        return dataBase.commonquery(z, str, strArr, str2, strArr2, str3, str4, str5, str6);
    }

    public static Cursor queryRowId(DataBase dataBase, String str, FetchRequest fetchRequest) {
        String[] strArr;
        checkParam(dataBase, str, fetchRequest);
        if (fetchRequest.getIsJoined()) {
            String[] split = str.split("\\s+");
            strArr = new String[]{(split[0] + "." + getRowidColumnName()) + " AS " + getRowidColumnName()};
        } else {
            strArr = new String[]{getRowidColumnName() + " AS " + getRowidColumnName()};
        }
        return dataBase.query(false, str, strArr, fetchRequest.getSqlRequest() == null ? null : fetchRequest.getSqlRequest().toString(), fetchRequest.getSelectionArgs(), null, null, checkOrderNull(fetchRequest.getOrder()), (fetchRequest.getLimit() == null || fetchRequest.getLimit().equals("")) ? null : fetchRequest.getLimit());
    }

    public static Cursor queryWithAggregateFunction(DataBase dataBase, String str, FetchRequest fetchRequest) {
        checkParam(dataBase, str, fetchRequest);
        String str2 = null;
        String sb = fetchRequest.getSqlRequest() == null ? null : fetchRequest.getSqlRequest().toString();
        String[] selectionArgs = fetchRequest.getSelectionArgs();
        String checkOrderNull = checkOrderNull(fetchRequest.getOrder());
        if (fetchRequest.getLimit() != null && !fetchRequest.getLimit().equals("")) {
            str2 = fetchRequest.getLimit();
        }
        return dataBase.query(false, str, fetchRequest.getColumnsWithAggregateFunction(), sb, selectionArgs, null, null, checkOrderNull, str2);
    }

    public static Cursor commonQuery(DataBase dataBase, String str, FetchRequest fetchRequest) {
        String[] strArr;
        checkParam(dataBase, str, fetchRequest);
        String str2 = null;
        String sb = fetchRequest.getSqlRequest() == null ? null : fetchRequest.getSqlRequest().toString();
        String[] selectionArgs = fetchRequest.getSelectionArgs();
        String checkOrderNull = checkOrderNull(fetchRequest.getOrder());
        if (fetchRequest.getLimit() != null && !"".equals(fetchRequest.getLimit())) {
            str2 = fetchRequest.getLimit();
        }
        if (fetchRequest.getIsJoined()) {
            String[] split = str.split("\\s+");
            strArr = new String[]{"DISTINCT " + (split[0] + "." + getRowidColumnName()) + " AS " + getRowidColumnName(), split[0] + ".*"};
        } else {
            strArr = new String[]{getRowidColumnName() + " AS " + getRowidColumnName(), "*"};
        }
        return dataBase.commonquery(false, str, strArr, sb, selectionArgs, null, null, checkOrderNull, str2);
    }

    private static void checkParam(DataBase dataBase, String str, FetchRequest fetchRequest) throws IllegalArgumentException, IllegalStateException {
        if (dataBase == null || str == null || str.equals("") || fetchRequest == null) {
            throw new ODMFIllegalArgumentException("Some parameter is null");
        } else if (!dataBase.isOpen()) {
            throw new ODMFIllegalStateException("The database has been closed");
        }
    }
}
