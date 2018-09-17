package java.sql;

import java.util.Map;
import java.util.Properties;

public interface Connection extends Wrapper, AutoCloseable {
    public static final int TRANSACTION_NONE = 0;
    public static final int TRANSACTION_READ_COMMITTED = 2;
    public static final int TRANSACTION_READ_UNCOMMITTED = 1;
    public static final int TRANSACTION_REPEATABLE_READ = 4;
    public static final int TRANSACTION_SERIALIZABLE = 8;

    void clearWarnings() throws SQLException;

    void close() throws SQLException;

    void commit() throws SQLException;

    Array createArrayOf(String str, Object[] objArr) throws SQLException;

    Blob createBlob() throws SQLException;

    Clob createClob() throws SQLException;

    NClob createNClob() throws SQLException;

    SQLXML createSQLXML() throws SQLException;

    Statement createStatement() throws SQLException;

    Statement createStatement(int i, int i2) throws SQLException;

    Statement createStatement(int i, int i2, int i3) throws SQLException;

    Struct createStruct(String str, Object[] objArr) throws SQLException;

    boolean getAutoCommit() throws SQLException;

    String getCatalog() throws SQLException;

    String getClientInfo(String str) throws SQLException;

    Properties getClientInfo() throws SQLException;

    int getHoldability() throws SQLException;

    DatabaseMetaData getMetaData() throws SQLException;

    int getTransactionIsolation() throws SQLException;

    Map<String, Class<?>> getTypeMap() throws SQLException;

    SQLWarning getWarnings() throws SQLException;

    boolean isClosed() throws SQLException;

    boolean isReadOnly() throws SQLException;

    boolean isValid(int i) throws SQLException;

    String nativeSQL(String str) throws SQLException;

    CallableStatement prepareCall(String str) throws SQLException;

    CallableStatement prepareCall(String str, int i, int i2) throws SQLException;

    CallableStatement prepareCall(String str, int i, int i2, int i3) throws SQLException;

    PreparedStatement prepareStatement(String str) throws SQLException;

    PreparedStatement prepareStatement(String str, int i) throws SQLException;

    PreparedStatement prepareStatement(String str, int i, int i2) throws SQLException;

    PreparedStatement prepareStatement(String str, int i, int i2, int i3) throws SQLException;

    PreparedStatement prepareStatement(String str, int[] iArr) throws SQLException;

    PreparedStatement prepareStatement(String str, String[] strArr) throws SQLException;

    void releaseSavepoint(Savepoint savepoint) throws SQLException;

    void rollback() throws SQLException;

    void rollback(Savepoint savepoint) throws SQLException;

    void setAutoCommit(boolean z) throws SQLException;

    void setCatalog(String str) throws SQLException;

    void setClientInfo(String str, String str2) throws SQLClientInfoException;

    void setClientInfo(Properties properties) throws SQLClientInfoException;

    void setHoldability(int i) throws SQLException;

    void setReadOnly(boolean z) throws SQLException;

    Savepoint setSavepoint() throws SQLException;

    Savepoint setSavepoint(String str) throws SQLException;

    void setTransactionIsolation(int i) throws SQLException;

    void setTypeMap(Map<String, Class<?>> map) throws SQLException;
}
