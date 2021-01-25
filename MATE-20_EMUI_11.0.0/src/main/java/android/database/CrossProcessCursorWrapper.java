package android.database;

public class CrossProcessCursorWrapper extends CursorWrapper implements CrossProcessCursor {
    public CrossProcessCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    @Override // android.database.CrossProcessCursor
    public void fillWindow(int position, CursorWindow window) {
        if (this.mCursor instanceof CrossProcessCursor) {
            ((CrossProcessCursor) this.mCursor).fillWindow(position, window);
        } else {
            DatabaseUtils.cursorFillWindow(this.mCursor, position, window);
        }
    }

    @Override // android.database.CrossProcessCursor
    public CursorWindow getWindow() {
        if (this.mCursor instanceof CrossProcessCursor) {
            return ((CrossProcessCursor) this.mCursor).getWindow();
        }
        return null;
    }

    @Override // android.database.CrossProcessCursor
    public boolean onMove(int oldPosition, int newPosition) {
        if (this.mCursor instanceof CrossProcessCursor) {
            return ((CrossProcessCursor) this.mCursor).onMove(oldPosition, newPosition);
        }
        return true;
    }
}
