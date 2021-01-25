package ohos.data.orm.impl;

import ohos.data.orm.OrmObject;
import ohos.data.orm.StringUtils;
import ohos.data.rdb.RdbPredicates;
import ohos.data.rdb.RdbStore;
import ohos.data.rdb.Statement;
import ohos.data.rdb.ValuesBucket;
import ohos.data.resultset.ResultSet;

public class RdbStoreAdapter {
    private static final int ODMF_ROWID_INDEX = 0;
    private static final String ODMF_SQLITE_ROWID = "rowid";

    public static int getOdmfRowidIndex() {
        return 0;
    }

    public static String getRowidColumnName() {
        return ODMF_SQLITE_ROWID;
    }

    private RdbStoreAdapter() {
    }

    public static <T extends OrmObject> ResultSet query(RdbStore rdbStore, RdbPredicates rdbPredicates) {
        checkParam(rdbStore, rdbPredicates);
        String tableName = rdbPredicates.getTableName();
        return rdbStore.queryByStep(rdbPredicates, new String[]{StringUtils.surroundWithQuote(tableName, "`") + "." + StringUtils.surroundWithQuote(getRowidColumnName(), "`") + " AS " + getRowidColumnName(), StringUtils.surroundWithQuote(tableName, "`") + ".*"});
    }

    public static ResultSet query(RdbStore rdbStore, RdbPredicates rdbPredicates, String[] strArr) {
        checkParam(rdbStore, rdbPredicates);
        return rdbStore.query(rdbPredicates, strArr);
    }

    public static <T extends OrmObject> int delete(RdbStore rdbStore, RdbPredicates rdbPredicates) {
        return rdbStore.delete(rdbPredicates);
    }

    public static <T extends OrmObject> int update(RdbStore rdbStore, RdbPredicates rdbPredicates, ValuesBucket valuesBucket) {
        return rdbStore.update(valuesBucket, rdbPredicates);
    }

    public static <T extends OrmObject> String aggregateQuery(RdbStore rdbStore, RdbPredicates rdbPredicates, String str, String str2) {
        StringBuilder sb = new StringBuilder(120);
        String str3 = rdbPredicates.isDistinct() ? "distinct" : "";
        sb.append("SELECT ");
        sb.append(str);
        sb.append("(");
        sb.append(str3);
        sb.append(str2);
        sb.append(")");
        String tableName = rdbPredicates.getJoinClause() == null ? rdbPredicates.getTableName() : rdbPredicates.getJoinClause();
        sb.append(" FROM ");
        sb.append(tableName);
        String whereClause = rdbPredicates.getWhereClause();
        if (whereClause != null && !"".equals(whereClause)) {
            sb.append(" WHERE ");
            sb.append(whereClause);
        }
        Statement buildStatement = rdbStore.buildStatement(sb.toString());
        buildStatement.setStrings((String[]) rdbPredicates.getWhereArgs().toArray(new String[0]));
        return buildStatement.executeAndGetString();
    }

    private static void checkParam(RdbStore rdbStore, RdbPredicates rdbPredicates) throws IllegalArgumentException, IllegalStateException {
        if (rdbStore == null || rdbPredicates == null) {
            throw new IllegalArgumentException("Some parameter is null");
        } else if (!rdbStore.isOpen()) {
            throw new IllegalStateException("The database has been closed");
        }
    }
}
