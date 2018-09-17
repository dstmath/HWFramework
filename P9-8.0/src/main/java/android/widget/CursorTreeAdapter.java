package android.widget;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.util.LogException;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

public abstract class CursorTreeAdapter extends BaseExpandableListAdapter implements Filterable, CursorFilterClient {
    private boolean mAutoRequery;
    SparseArray<MyCursorHelper> mChildrenCursorHelpers;
    private Context mContext;
    CursorFilter mCursorFilter;
    FilterQueryProvider mFilterQueryProvider;
    MyCursorHelper mGroupCursorHelper;
    private Handler mHandler;

    class MyCursorHelper {
        private MyContentObserver mContentObserver;
        private Cursor mCursor;
        private MyDataSetObserver mDataSetObserver;
        private boolean mDataValid;
        private int mRowIDColumn;

        private class MyContentObserver extends ContentObserver {
            public MyContentObserver() {
                super(CursorTreeAdapter.this.mHandler);
            }

            public boolean deliverSelfNotifications() {
                return true;
            }

            public void onChange(boolean selfChange) {
                if (CursorTreeAdapter.this.mAutoRequery && MyCursorHelper.this.mCursor != null && (MyCursorHelper.this.mCursor.isClosed() ^ 1) != 0) {
                    MyCursorHelper.this.mDataValid = MyCursorHelper.this.mCursor.requery();
                }
            }
        }

        private class MyDataSetObserver extends DataSetObserver {
            /* synthetic */ MyDataSetObserver(MyCursorHelper this$1, MyDataSetObserver -this1) {
                this();
            }

            private MyDataSetObserver() {
            }

            public void onChanged() {
                MyCursorHelper.this.mDataValid = true;
                CursorTreeAdapter.this.notifyDataSetChanged();
            }

            public void onInvalidated() {
                MyCursorHelper.this.mDataValid = false;
                CursorTreeAdapter.this.notifyDataSetInvalidated();
            }
        }

        MyCursorHelper(Cursor cursor) {
            boolean cursorPresent = cursor != null;
            this.mCursor = cursor;
            this.mDataValid = cursorPresent;
            this.mRowIDColumn = cursorPresent ? cursor.getColumnIndex("_id") : -1;
            this.mContentObserver = new MyContentObserver();
            this.mDataSetObserver = new MyDataSetObserver(this, null);
            if (cursorPresent) {
                cursor.registerContentObserver(this.mContentObserver);
                cursor.registerDataSetObserver(this.mDataSetObserver);
            }
        }

        Cursor getCursor() {
            return this.mCursor;
        }

        int getCount() {
            if (!this.mDataValid || this.mCursor == null) {
                return 0;
            }
            return this.mCursor.getCount();
        }

        /* JADX WARNING: Missing block: B:9:0x001c, code:
            return 0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        long getId(int position) {
            if (this.mDataValid && this.mCursor != null && this.mCursor.moveToPosition(position)) {
                return this.mCursor.getLong(this.mRowIDColumn);
            }
            return 0;
        }

        Cursor moveTo(int position) {
            if (this.mDataValid && this.mCursor != null && this.mCursor.moveToPosition(position)) {
                return this.mCursor;
            }
            return null;
        }

        void changeCursor(Cursor cursor, boolean releaseCursors) {
            if (cursor != this.mCursor) {
                deactivate();
                this.mCursor = cursor;
                if (cursor != null) {
                    cursor.registerContentObserver(this.mContentObserver);
                    cursor.registerDataSetObserver(this.mDataSetObserver);
                    this.mRowIDColumn = cursor.getColumnIndex("_id");
                    this.mDataValid = true;
                    CursorTreeAdapter.this.notifyDataSetChanged(releaseCursors);
                } else {
                    this.mRowIDColumn = -1;
                    this.mDataValid = false;
                    CursorTreeAdapter.this.notifyDataSetInvalidated();
                }
            }
        }

        void deactivate() {
            if (this.mCursor != null) {
                this.mCursor.unregisterContentObserver(this.mContentObserver);
                this.mCursor.unregisterDataSetObserver(this.mDataSetObserver);
                this.mCursor.close();
                this.mCursor = null;
            }
        }

        boolean isValid() {
            return this.mDataValid && this.mCursor != null;
        }
    }

    protected abstract void bindChildView(View view, Context context, Cursor cursor, boolean z);

    protected abstract void bindGroupView(View view, Context context, Cursor cursor, boolean z);

    protected abstract Cursor getChildrenCursor(Cursor cursor);

    protected abstract View newChildView(Context context, Cursor cursor, boolean z, ViewGroup viewGroup);

    protected abstract View newGroupView(Context context, Cursor cursor, boolean z, ViewGroup viewGroup);

    public CursorTreeAdapter(Cursor cursor, Context context) {
        init(cursor, context, true);
    }

    public CursorTreeAdapter(Cursor cursor, Context context, boolean autoRequery) {
        init(cursor, context, autoRequery);
    }

    private void init(Cursor cursor, Context context, boolean autoRequery) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mAutoRequery = autoRequery;
        this.mGroupCursorHelper = new MyCursorHelper(cursor);
        this.mChildrenCursorHelpers = new SparseArray();
    }

    /* JADX WARNING: Missing block: B:12:0x002b, code:
            return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized MyCursorHelper getChildrenCursorHelper(int groupPosition, boolean requestCursor) {
        MyCursorHelper cursorHelper = (MyCursorHelper) this.mChildrenCursorHelpers.get(groupPosition);
        if (cursorHelper == null) {
            if (this.mGroupCursorHelper.moveTo(groupPosition) == null) {
                return null;
            }
            cursorHelper = new MyCursorHelper(getChildrenCursor(this.mGroupCursorHelper.getCursor()));
            this.mChildrenCursorHelpers.put(groupPosition, cursorHelper);
        }
    }

    public void setGroupCursor(Cursor cursor) {
        this.mGroupCursorHelper.changeCursor(cursor, false);
    }

    public void setChildrenCursor(int groupPosition, Cursor childrenCursor) {
        getChildrenCursorHelper(groupPosition, false).changeCursor(childrenCursor, false);
    }

    public Cursor getChild(int groupPosition, int childPosition) {
        return getChildrenCursorHelper(groupPosition, true).moveTo(childPosition);
    }

    public long getChildId(int groupPosition, int childPosition) {
        return getChildrenCursorHelper(groupPosition, true).getId(childPosition);
    }

    public int getChildrenCount(int groupPosition) {
        MyCursorHelper helper = getChildrenCursorHelper(groupPosition, true);
        return (!this.mGroupCursorHelper.isValid() || helper == null) ? 0 : helper.getCount();
    }

    public Cursor getGroup(int groupPosition) {
        return this.mGroupCursorHelper.moveTo(groupPosition);
    }

    public int getGroupCount() {
        return this.mGroupCursorHelper.getCount();
    }

    public long getGroupId(int groupPosition) {
        return this.mGroupCursorHelper.getId(groupPosition);
    }

    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        Cursor cursor = this.mGroupCursorHelper.moveTo(groupPosition);
        if (cursor == null) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        View v;
        if (convertView == null) {
            v = newGroupView(this.mContext, cursor, isExpanded, parent);
        } else {
            v = convertView;
        }
        bindGroupView(v, this.mContext, cursor, isExpanded);
        return v;
    }

    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        Cursor cursor = getChildrenCursorHelper(groupPosition, true).moveTo(childPosition);
        if (cursor == null) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        View v;
        if (convertView == null) {
            v = newChildView(this.mContext, cursor, isLastChild, parent);
        } else {
            v = convertView;
        }
        bindChildView(v, this.mContext, cursor, isLastChild);
        return v;
    }

    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public boolean hasStableIds() {
        return true;
    }

    private synchronized void releaseCursorHelpers() {
        for (int pos = this.mChildrenCursorHelpers.size() - 1; pos >= 0; pos--) {
            ((MyCursorHelper) this.mChildrenCursorHelpers.valueAt(pos)).deactivate();
        }
        this.mChildrenCursorHelpers.clear();
    }

    public void notifyDataSetChanged() {
        notifyDataSetChanged(true);
    }

    public void notifyDataSetChanged(boolean releaseCursors) {
        if (releaseCursors) {
            releaseCursorHelpers();
        }
        super.notifyDataSetChanged();
    }

    public void notifyDataSetInvalidated() {
        releaseCursorHelpers();
        super.notifyDataSetInvalidated();
    }

    public void onGroupCollapsed(int groupPosition) {
        deactivateChildrenCursorHelper(groupPosition);
    }

    synchronized void deactivateChildrenCursorHelper(int groupPosition) {
        MyCursorHelper cursorHelper = getChildrenCursorHelper(groupPosition, true);
        this.mChildrenCursorHelpers.remove(groupPosition);
        cursorHelper.deactivate();
    }

    public String convertToString(Cursor cursor) {
        return cursor == null ? LogException.NO_VALUE : cursor.toString();
    }

    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (this.mFilterQueryProvider != null) {
            return this.mFilterQueryProvider.runQuery(constraint);
        }
        return this.mGroupCursorHelper.getCursor();
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

    public void changeCursor(Cursor cursor) {
        this.mGroupCursorHelper.changeCursor(cursor, true);
    }

    public Cursor getCursor() {
        return this.mGroupCursorHelper.getCursor();
    }
}
