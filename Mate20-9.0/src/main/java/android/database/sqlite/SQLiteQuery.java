package android.database.sqlite;

import android.database.CursorWindow;
import android.os.CancellationSignal;
import android.util.Log;

public final class SQLiteQuery extends SQLiteProgram {
    private static final String TAG = "SQLiteQuery";
    private final CancellationSignal mCancellationSignal;

    SQLiteQuery(SQLiteDatabase db, String query, CancellationSignal cancellationSignal) {
        super(db, query, null, cancellationSignal);
        this.mCancellationSignal = cancellationSignal;
    }

    /* access modifiers changed from: package-private */
    public int fillWindow(CursorWindow window, int startPos, int requiredPos, boolean countAllRows) {
        acquireReference();
        try {
            window.acquireReference();
            int numRows = getSession().executeForCursorWindow(getSql(), getBindArgs(), window, startPos, requiredPos, countAllRows, getConnectionFlags(), this.mCancellationSignal);
            window.releaseReference();
            releaseReference();
            return numRows;
        } catch (SQLiteDatabaseCorruptException ex) {
            onCorruption();
            throw ex;
        } catch (SQLiteException ex2) {
            Log.e(TAG, "exception: " + ex2.getMessage() + "; query: " + getSql());
            throw ex2;
        } catch (Throwable th) {
            releaseReference();
            throw th;
        }
    }

    public String toString() {
        return "SQLiteQuery: " + getSql();
    }
}
