package javax.sql;

import java.sql.SQLException;

public interface ConnectionPoolDataSource extends CommonDataSource {
    PooledConnection getPooledConnection() throws SQLException;

    PooledConnection getPooledConnection(String str, String str2) throws SQLException;
}
