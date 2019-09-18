package javax.sql;

import java.util.EventListener;

public interface RowSetListener extends EventListener {
    void cursorMoved(RowSetEvent rowSetEvent);

    void rowChanged(RowSetEvent rowSetEvent);

    void rowSetChanged(RowSetEvent rowSetEvent);
}
