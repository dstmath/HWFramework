package java.sql;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class SQLException extends Exception implements Iterable<Throwable> {
    private static final AtomicReferenceFieldUpdater<SQLException, SQLException> nextUpdater = AtomicReferenceFieldUpdater.newUpdater(SQLException.class, SQLException.class, "next");
    private static final long serialVersionUID = 2135244094396331484L;
    private String SQLState;
    private volatile SQLException next;
    private int vendorCode;

    public SQLException(String reason, String SQLState, int vendorCode) {
        super(reason);
        this.SQLState = SQLState;
        this.vendorCode = vendorCode;
        if (!(this instanceof SQLWarning) && DriverManager.getLogWriter() != null) {
            DriverManager.println("SQLState(" + SQLState + ") vendor code(" + vendorCode + ")");
            printStackTrace(DriverManager.getLogWriter());
        }
    }

    public SQLException(String reason, String SQLState) {
        super(reason);
        this.SQLState = SQLState;
        this.vendorCode = 0;
        if (!(this instanceof SQLWarning) && DriverManager.getLogWriter() != null) {
            printStackTrace(DriverManager.getLogWriter());
            DriverManager.println("SQLException: SQLState(" + SQLState + ")");
        }
    }

    public SQLException(String reason) {
        super(reason);
        this.SQLState = null;
        this.vendorCode = 0;
        if (!(this instanceof SQLWarning) && DriverManager.getLogWriter() != null) {
            printStackTrace(DriverManager.getLogWriter());
        }
    }

    public SQLException() {
        this.SQLState = null;
        this.vendorCode = 0;
        if (!(this instanceof SQLWarning) && DriverManager.getLogWriter() != null) {
            printStackTrace(DriverManager.getLogWriter());
        }
    }

    public SQLException(Throwable cause) {
        super(cause);
        if (!(this instanceof SQLWarning) && DriverManager.getLogWriter() != null) {
            printStackTrace(DriverManager.getLogWriter());
        }
    }

    public SQLException(String reason, Throwable cause) {
        super(reason, cause);
        if (!(this instanceof SQLWarning) && DriverManager.getLogWriter() != null) {
            printStackTrace(DriverManager.getLogWriter());
        }
    }

    public SQLException(String reason, String sqlState, Throwable cause) {
        super(reason, cause);
        this.SQLState = sqlState;
        this.vendorCode = 0;
        if (!(this instanceof SQLWarning) && DriverManager.getLogWriter() != null) {
            printStackTrace(DriverManager.getLogWriter());
            DriverManager.println("SQLState(" + this.SQLState + ")");
        }
    }

    public SQLException(String reason, String sqlState, int vendorCode, Throwable cause) {
        super(reason, cause);
        this.SQLState = sqlState;
        this.vendorCode = vendorCode;
        if (!(this instanceof SQLWarning) && DriverManager.getLogWriter() != null) {
            DriverManager.println("SQLState(" + this.SQLState + ") vendor code(" + vendorCode + ")");
            printStackTrace(DriverManager.getLogWriter());
        }
    }

    public String getSQLState() {
        return this.SQLState;
    }

    public int getErrorCode() {
        return this.vendorCode;
    }

    public SQLException getNextException() {
        return this.next;
    }

    public void setNextException(SQLException ex) {
        SQLException current = this;
        while (true) {
            SQLException next = current.next;
            if (next != null) {
                current = next;
            } else if (!nextUpdater.compareAndSet(current, null, ex)) {
                current = current.next;
            } else {
                return;
            }
        }
    }

    public Iterator<Throwable> iterator() {
        return new Iterator<Throwable>() {
            Throwable cause = this.firstException.getCause();
            SQLException firstException = SQLException.this;
            SQLException nextException = this.firstException.getNextException();

            public boolean hasNext() {
                if (this.firstException == null && this.nextException == null && this.cause == null) {
                    return false;
                }
                return true;
            }

            public Throwable next() {
                Throwable throwable;
                if (this.firstException != null) {
                    throwable = this.firstException;
                    this.firstException = null;
                    return throwable;
                } else if (this.cause != null) {
                    throwable = this.cause;
                    this.cause = this.cause.getCause();
                    return throwable;
                } else if (this.nextException != null) {
                    throwable = this.nextException;
                    this.cause = this.nextException.getCause();
                    this.nextException = this.nextException.getNextException();
                    return throwable;
                } else {
                    throw new NoSuchElementException();
                }
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
