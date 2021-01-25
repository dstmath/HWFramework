package android.widget;

public class CursorAdapterEx {
    public static boolean isDataValid(CursorAdapter cursorAdapter) {
        if (cursorAdapter != null) {
            return cursorAdapter.mDataValid;
        }
        return false;
    }
}
