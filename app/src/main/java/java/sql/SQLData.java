package java.sql;

public interface SQLData {
    String getSQLTypeName() throws SQLException;

    void readSQL(SQLInput sQLInput, String str) throws SQLException;

    void writeSQL(SQLOutput sQLOutput) throws SQLException;
}
