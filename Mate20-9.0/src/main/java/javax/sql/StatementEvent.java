package javax.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EventObject;

public class StatementEvent extends EventObject {
    private SQLException exception;
    private PreparedStatement statement;

    public StatementEvent(PooledConnection con, PreparedStatement statement2) {
        super(con);
        this.statement = statement2;
        this.exception = null;
    }

    public StatementEvent(PooledConnection con, PreparedStatement statement2, SQLException exception2) {
        super(con);
        this.statement = statement2;
        this.exception = exception2;
    }

    public PreparedStatement getStatement() {
        return this.statement;
    }

    public SQLException getSQLException() {
        return this.exception;
    }
}
