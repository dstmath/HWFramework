package java.sql;

public interface Savepoint {
    int getSavepointId() throws SQLException;

    String getSavepointName() throws SQLException;
}
