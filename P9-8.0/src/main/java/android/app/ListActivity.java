package android.app;

import android.R;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ListActivity extends Activity {
    protected ListAdapter mAdapter;
    private boolean mFinishedStart = false;
    private Handler mHandler = new Handler();
    protected ListView mList;
    private OnItemClickListener mOnClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            ListActivity.this.onListItemClick((ListView) parent, v, position, id);
        }
    };
    private Runnable mRequestFocus = new Runnable() {
        public void run() {
            ListActivity.this.mList.focusableViewAvailable(ListActivity.this.mList);
        }
    };

    protected void onListItemClick(ListView l, View v, int position, long id) {
    }

    protected void onRestoreInstanceState(Bundle state) {
        ensureList();
        super.onRestoreInstanceState(state);
    }

    protected void onDestroy() {
        this.mHandler.removeCallbacks(this.mRequestFocus);
        super.onDestroy();
    }

    public void onContentChanged() {
        super.onContentChanged();
        View emptyView = findViewById(R.id.empty);
        this.mList = (ListView) findViewById(R.id.list);
        if (this.mList == null) {
            throw new RuntimeException("Your content must have a ListView whose id attribute is 'android.R.id.list'");
        }
        if (emptyView != null) {
            this.mList.setEmptyView(emptyView);
        }
        this.mList.setOnItemClickListener(this.mOnClickListener);
        if (this.mFinishedStart) {
            setListAdapter(this.mAdapter);
        }
        this.mHandler.post(this.mRequestFocus);
        this.mFinishedStart = true;
    }

    public void setListAdapter(ListAdapter adapter) {
        synchronized (this) {
            ensureList();
            this.mAdapter = adapter;
            this.mList.setAdapter(adapter);
        }
    }

    public void setSelection(int position) {
        this.mList.setSelection(position);
    }

    public int getSelectedItemPosition() {
        return this.mList.getSelectedItemPosition();
    }

    public long getSelectedItemId() {
        return this.mList.getSelectedItemId();
    }

    public ListView getListView() {
        ensureList();
        return this.mList;
    }

    public ListAdapter getListAdapter() {
        return this.mAdapter;
    }

    private void ensureList() {
        if (this.mList == null) {
            setContentView(17367162);
        }
    }
}
