package android.database.sqlite;

public interface IHwSQLiteDatabase {
    public static final int DELETE_STATUS = 2;
    public static final int INSERT_STATUS = 0;
    public static final int UPDATE_STATUS = 1;

    void triggerAddingIndex(SQLiteDatabase sQLiteDatabase, String str, long j);

    void triggerSQLIndex(SQLiteDatabase sQLiteDatabase, String str, Object[] objArr);

    void triggerTransactionIndex(SQLiteDatabase sQLiteDatabase);

    void triggerUpdatingOrDeletingIndex(SQLiteDatabase sQLiteDatabase, String str, String str2, String[] strArr, int i);
}
