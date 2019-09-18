package javax.sql;

import java.util.EventListener;

public interface ConnectionEventListener extends EventListener {
    void connectionClosed(ConnectionEvent connectionEvent);

    void connectionErrorOccurred(ConnectionEvent connectionEvent);
}
