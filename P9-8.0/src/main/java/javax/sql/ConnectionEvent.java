package javax.sql;

import java.sql.SQLException;
import java.util.EventObject;

public class ConnectionEvent extends EventObject {
    static final long serialVersionUID = -4843217645290030002L;
    private SQLException ex = null;

    public ConnectionEvent(PooledConnection con) {
        super(con);
    }

    public ConnectionEvent(PooledConnection con, SQLException ex) {
        super(con);
        this.ex = ex;
    }

    public SQLException getSQLException() {
        return this.ex;
    }
}
