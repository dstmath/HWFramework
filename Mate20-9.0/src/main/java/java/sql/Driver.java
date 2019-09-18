package java.sql;

import java.util.Properties;

public interface Driver {
    boolean acceptsURL(String str) throws SQLException;

    Connection connect(String str, Properties properties) throws SQLException;

    int getMajorVersion();

    int getMinorVersion();

    DriverPropertyInfo[] getPropertyInfo(String str, Properties properties) throws SQLException;

    boolean jdbcCompliant();
}
