package javax.sql;

import java.util.EventObject;

public class RowSetEvent extends EventObject {
    static final long serialVersionUID = -1875450876546332005L;

    public RowSetEvent(RowSet source) {
        super(source);
    }
}
