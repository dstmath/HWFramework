package ohos.data.rdb;

public interface Statement {
    void clearValues();

    void close();

    void execute();

    int executeAndGetChanges();

    long executeAndGetLastInsertRowId();

    long executeAndGetLong();

    String executeAndGetString();

    void setBlob(int i, byte[] bArr);

    void setDouble(int i, double d);

    void setLong(int i, long j);

    void setNull(int i);

    void setObject(int i, Object obj);

    void setString(int i, String str);

    void setStrings(String[] strArr);
}
