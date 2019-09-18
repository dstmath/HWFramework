package javax.sql;

import java.sql.SQLException;

public interface RowSetReader {
    void readData(RowSetInternal rowSetInternal) throws SQLException;
}
