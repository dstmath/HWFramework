package android.database.sqlite;

import java.util.HashMap;

public interface IHwSQLiteSession {
    void clearTransMap();

    HashMap<SQLInfo, Integer> getTransMap();

    void insertTransMap(String str, long j, int i);

    boolean isCommitSuccess();

    void setCommitSuccessOrFail(boolean z);
}
