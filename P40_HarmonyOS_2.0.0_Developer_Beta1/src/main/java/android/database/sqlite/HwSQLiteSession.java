package android.database.sqlite;

import com.huawei.indexsearch.IndexSearchParser;
import java.util.HashMap;

public class HwSQLiteSession implements IHwSQLiteSession {
    private static volatile HwSQLiteSession mInstance = null;
    private boolean mCommitSuccessful;
    private HashMap<SQLInfo, Integer> mTransactionMap;

    public static HwSQLiteSession getInstance() {
        if (IndexSearchParser.getInstance() != null) {
            mInstance = new HwSQLiteSession();
        }
        return mInstance;
    }

    public HwSQLiteSession() {
        this.mTransactionMap = null;
        this.mCommitSuccessful = false;
        this.mTransactionMap = new HashMap<>();
    }

    public void setCommitSuccessOrFail(boolean success) {
        this.mCommitSuccessful = success;
    }

    public boolean isCommitSuccess() {
        return this.mCommitSuccessful;
    }

    public void insertTransMap(String table, long primaryKey, int status) {
        SQLInfo sqlInfo = new SQLInfo(table, primaryKey);
        HashMap<SQLInfo, Integer> hashMap = this.mTransactionMap;
        if (hashMap != null) {
            hashMap.put(sqlInfo, Integer.valueOf(status));
        }
    }

    public void clearTransMap() {
        HashMap<SQLInfo, Integer> hashMap = this.mTransactionMap;
        if (hashMap != null) {
            hashMap.clear();
        }
    }

    public HashMap<SQLInfo, Integer> getTransMap() {
        return this.mTransactionMap;
    }
}
