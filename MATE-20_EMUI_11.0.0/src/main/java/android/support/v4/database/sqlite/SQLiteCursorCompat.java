package android.support.v4.database.sqlite;

import android.database.sqlite.SQLiteCursor;
import android.support.annotation.NonNull;
import android.support.v4.os.BuildCompat;

public final class SQLiteCursorCompat {
    private SQLiteCursorCompat() {
    }

    public void setFillWindowForwardOnly(@NonNull SQLiteCursor cursor, boolean fillWindowForwardOnly) {
        if (BuildCompat.isAtLeastP()) {
            cursor.setFillWindowForwardOnly(fillWindowForwardOnly);
        }
    }
}
