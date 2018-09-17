package android.widget;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.LogException;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;

public abstract class CursorAdapter extends BaseAdapter implements Filterable, CursorFilterClient, ThemedSpinnerAdapter {
    @Deprecated
    public static final int FLAG_AUTO_REQUERY = 1;
    public static final int FLAG_REGISTER_CONTENT_OBSERVER = 2;
    protected boolean mAutoRequery;
    protected ChangeObserver mChangeObserver;
    protected Context mContext;
    protected Cursor mCursor;
    protected CursorFilter mCursorFilter;
    protected DataSetObserver mDataSetObserver;
    protected boolean mDataValid;
    protected Context mDropDownContext;
    protected FilterQueryProvider mFilterQueryProvider;
    protected int mRowIDColumn;

    private class ChangeObserver extends ContentObserver {
        public ChangeObserver() {
            super(new Handler());
        }

        public boolean deliverSelfNotifications() {
            return true;
        }

        public void onChange(boolean selfChange) {
            CursorAdapter.this.onContentChanged();
        }
    }

    private class MyDataSetObserver extends DataSetObserver {
        /* synthetic */ MyDataSetObserver(CursorAdapter this$0, MyDataSetObserver -this1) {
            this();
        }

        private MyDataSetObserver() {
        }

        public void onChanged() {
            CursorAdapter.this.mDataValid = true;
            CursorAdapter.this.notifyDataSetChanged();
        }

        public void onInvalidated() {
            CursorAdapter.this.mDataValid = false;
            CursorAdapter.this.notifyDataSetInvalidated();
        }
    }

    public abstract void bindView(View view, Context context, Cursor cursor);

    public abstract View newView(Context context, Cursor cursor, ViewGroup viewGroup);

    @Deprecated
    public CursorAdapter(Context context, Cursor c) {
        init(context, c, 1);
    }

    public CursorAdapter(Context context, Cursor c, boolean autoRequery) {
        init(context, c, autoRequery ? 1 : 2);
    }

    public CursorAdapter(Context context, Cursor c, int flags) {
        init(context, c, flags);
    }

    @Deprecated
    protected void init(Context context, Cursor c, boolean autoRequery) {
        init(context, c, autoRequery ? 1 : 2);
    }

    void init(Context context, Cursor c, int flags) {
        if ((flags & 1) == 1) {
            flags |= 2;
            this.mAutoRequery = true;
        } else {
            this.mAutoRequery = false;
        }
        boolean cursorPresent = c != null;
        this.mCursor = c;
        this.mDataValid = cursorPresent;
        this.mContext = context;
        this.mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
        if ((flags & 2) == 2) {
            this.mChangeObserver = new ChangeObserver();
            this.mDataSetObserver = new MyDataSetObserver(this, null);
        } else {
            this.mChangeObserver = null;
            this.mDataSetObserver = null;
        }
        if (cursorPresent) {
            if (this.mChangeObserver != null) {
                c.registerContentObserver(this.mChangeObserver);
            }
            if (this.mDataSetObserver != null) {
                c.registerDataSetObserver(this.mDataSetObserver);
            }
        }
    }

    public void setDropDownViewTheme(Theme theme) {
        if (theme == null) {
            this.mDropDownContext = null;
        } else if (theme == this.mContext.getTheme()) {
            this.mDropDownContext = this.mContext;
        } else {
            this.mDropDownContext = new ContextThemeWrapper(this.mContext, theme);
        }
    }

    public Theme getDropDownViewTheme() {
        return this.mDropDownContext == null ? null : this.mDropDownContext.getTheme();
    }

    public Cursor getCursor() {
        return this.mCursor;
    }

    public int getCount() {
        if (!this.mDataValid || this.mCursor == null) {
            return 0;
        }
        return this.mCursor.getCount();
    }

    public Object getItem(int position) {
        if (!this.mDataValid || this.mCursor == null) {
            return null;
        }
        this.mCursor.moveToPosition(position);
        return this.mCursor;
    }

    /* JADX WARNING: Missing block: B:9:0x001c, code:
            return 0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long getItemId(int position) {
        if (this.mDataValid && this.mCursor != null && this.mCursor.moveToPosition(position)) {
            return this.mCursor.getLong(this.mRowIDColumn);
        }
        return 0;
    }

    public boolean hasStableIds() {
        return true;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (!this.mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        } else if (this.mCursor.moveToPosition(position)) {
            View v;
            if (convertView == null) {
                v = newView(this.mContext, this.mCursor, parent);
            } else {
                v = convertView;
            }
            bindView(v, this.mContext, this.mCursor);
            return v;
        } else {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (!this.mDataValid) {
            return null;
        }
        View v;
        Context context = this.mDropDownContext == null ? this.mContext : this.mDropDownContext;
        this.mCursor.moveToPosition(position);
        if (convertView == null) {
            v = newDropDownView(context, this.mCursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, context, this.mCursor);
        return v;
    }

    public View newDropDownView(Context context, Cursor cursor, ViewGroup parent) {
        return newView(context, cursor, parent);
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == this.mCursor) {
            return null;
        }
        Cursor oldCursor = this.mCursor;
        if (oldCursor != null) {
            if (this.mChangeObserver != null) {
                oldCursor.unregisterContentObserver(this.mChangeObserver);
            }
            if (this.mDataSetObserver != null) {
                oldCursor.unregisterDataSetObserver(this.mDataSetObserver);
            }
        }
        this.mCursor = newCursor;
        if (newCursor != null) {
            if (this.mChangeObserver != null) {
                newCursor.registerContentObserver(this.mChangeObserver);
            }
            if (this.mDataSetObserver != null) {
                newCursor.registerDataSetObserver(this.mDataSetObserver);
            }
            this.mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            this.mDataValid = true;
            notifyDataSetChanged();
        } else {
            this.mRowIDColumn = -1;
            this.mDataValid = false;
            notifyDataSetInvalidated();
        }
        return oldCursor;
    }

    public CharSequence convertToString(Cursor cursor) {
        return cursor == null ? LogException.NO_VALUE : cursor.toString();
    }

    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (this.mFilterQueryProvider != null) {
            return this.mFilterQueryProvider.runQuery(constraint);
        }
        return this.mCursor;
    }

    public Filter getFilter() {
        if (this.mCursorFilter == null) {
            this.mCursorFilter = new CursorFilter(this);
        }
        return this.mCursorFilter;
    }

    public FilterQueryProvider getFilterQueryProvider() {
        return this.mFilterQueryProvider;
    }

    public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
        this.mFilterQueryProvider = filterQueryProvider;
    }

    protected void onContentChanged() {
        if (this.mAutoRequery && this.mCursor != null && (this.mCursor.isClosed() ^ 1) != 0) {
            this.mDataValid = this.mCursor.requery();
        }
    }
}
