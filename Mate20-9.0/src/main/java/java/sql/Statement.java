package java.sql;

public interface Statement extends Wrapper, AutoCloseable {
    public static final int CLOSE_ALL_RESULTS = 3;
    public static final int CLOSE_CURRENT_RESULT = 1;
    public static final int EXECUTE_FAILED = -3;
    public static final int KEEP_CURRENT_RESULT = 2;
    public static final int NO_GENERATED_KEYS = 2;
    public static final int RETURN_GENERATED_KEYS = 1;
    public static final int SUCCESS_NO_INFO = -2;

    void addBatch(String str) throws SQLException;

    void cancel() throws SQLException;

    void clearBatch() throws SQLException;

    void clearWarnings() throws SQLException;

    void close() throws SQLException;

    boolean execute(String str) throws SQLException;

    boolean execute(String str, int i) throws SQLException;

    boolean execute(String str, int[] iArr) throws SQLException;

    boolean execute(String str, String[] strArr) throws SQLException;

    int[] executeBatch() throws SQLException;

    ResultSet executeQuery(String str) throws SQLException;

    int executeUpdate(String str) throws SQLException;

    int executeUpdate(String str, int i) throws SQLException;

    int executeUpdate(String str, int[] iArr) throws SQLException;

    int executeUpdate(String str, String[] strArr) throws SQLException;

    Connection getConnection() throws SQLException;

    int getFetchDirection() throws SQLException;

    int getFetchSize() throws SQLException;

    ResultSet getGeneratedKeys() throws SQLException;

    int getMaxFieldSize() throws SQLException;

    int getMaxRows() throws SQLException;

    boolean getMoreResults() throws SQLException;

    boolean getMoreResults(int i) throws SQLException;

    int getQueryTimeout() throws SQLException;

    ResultSet getResultSet() throws SQLException;

    int getResultSetConcurrency() throws SQLException;

    int getResultSetHoldability() throws SQLException;

    int getResultSetType() throws SQLException;

    int getUpdateCount() throws SQLException;

    SQLWarning getWarnings() throws SQLException;

    boolean isClosed() throws SQLException;

    boolean isPoolable() throws SQLException;

    void setCursorName(String str) throws SQLException;

    void setEscapeProcessing(boolean z) throws SQLException;

    void setFetchDirection(int i) throws SQLException;

    void setFetchSize(int i) throws SQLException;

    void setMaxFieldSize(int i) throws SQLException;

    void setMaxRows(int i) throws SQLException;

    void setPoolable(boolean z) throws SQLException;

    void setQueryTimeout(int i) throws SQLException;
}
