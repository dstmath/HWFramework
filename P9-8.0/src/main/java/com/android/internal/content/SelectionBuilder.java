package com.android.internal.content;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import java.util.ArrayList;

public class SelectionBuilder {
    private StringBuilder mSelection = new StringBuilder();
    private ArrayList<String> mSelectionArgs = new ArrayList();

    public SelectionBuilder reset() {
        this.mSelection.setLength(0);
        this.mSelectionArgs.clear();
        return this;
    }

    public SelectionBuilder append(String selection, Object... selectionArgs) {
        if (!TextUtils.isEmpty(selection)) {
            if (this.mSelection.length() > 0) {
                this.mSelection.append(" AND ");
            }
            this.mSelection.append("(").append(selection).append(")");
            if (selectionArgs != null) {
                for (Object arg : selectionArgs) {
                    this.mSelectionArgs.add(String.valueOf(arg));
                }
            }
            return this;
        } else if (selectionArgs == null || selectionArgs.length <= 0) {
            return this;
        } else {
            throw new IllegalArgumentException("Valid selection required when including arguments");
        }
    }

    public String getSelection() {
        return this.mSelection.toString();
    }

    public String[] getSelectionArgs() {
        return (String[]) this.mSelectionArgs.toArray(new String[this.mSelectionArgs.size()]);
    }

    public Cursor query(SQLiteDatabase db, String table, String[] columns, String orderBy) {
        return query(db, table, columns, null, null, orderBy, null);
    }

    public Cursor query(SQLiteDatabase db, String table, String[] columns, String groupBy, String having, String orderBy, String limit) {
        return db.query(table, columns, getSelection(), getSelectionArgs(), groupBy, having, orderBy, limit);
    }

    public int update(SQLiteDatabase db, String table, ContentValues values) {
        return db.update(table, values, getSelection(), getSelectionArgs());
    }

    public int delete(SQLiteDatabase db, String table) {
        return db.delete(table, getSelection(), getSelectionArgs());
    }
}
