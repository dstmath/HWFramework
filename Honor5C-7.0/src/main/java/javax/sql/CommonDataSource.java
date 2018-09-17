package javax.sql;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public interface CommonDataSource {
    PrintWriter getLogWriter() throws SQLException;

    int getLoginTimeout() throws SQLException;

    Logger getParentLogger() throws SQLFeatureNotSupportedException;

    void setLogWriter(PrintWriter printWriter) throws SQLException;

    void setLoginTimeout(int i) throws SQLException;
}
