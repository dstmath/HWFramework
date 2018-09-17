package javax.sql;

import java.util.EventListener;

public interface StatementEventListener extends EventListener {
    void statementClosed(StatementEvent statementEvent);

    void statementErrorOccurred(StatementEvent statementEvent);
}
