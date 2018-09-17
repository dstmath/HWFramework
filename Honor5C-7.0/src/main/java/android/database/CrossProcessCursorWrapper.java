package android.database;

public class CrossProcessCursorWrapper extends CursorWrapper implements CrossProcessCursor {
    public CrossProcessCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public void fillWindow(int position, CursorWindow window) {
        if (this.mCursor instanceof CrossProcessCursor) {
            this.mCursor.fillWindow(position, window);
        } else {
            DatabaseUtils.cursorFillWindow(this.mCursor, position, window);
        }
    }

    public CursorWindow getWindow() {
        if (this.mCursor instanceof CrossProcessCursor) {
            return this.mCursor.getWindow();
        }
        return null;
    }

    public boolean onMove(int oldPosition, int newPosition) {
        if (this.mCursor instanceof CrossProcessCursor) {
            return this.mCursor.onMove(oldPosition, newPosition);
        }
        return true;
    }
}
