package android.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.android.internal.R;

public class ListActivity extends Activity {
    protected ListAdapter mAdapter;
    private boolean mFinishedStart = false;
    private Handler mHandler = new Handler();
    protected ListView mList;
    private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            ListActivity.this.onListItemClick((ListView) parent, v, position, id);
        }
    };
    private Runnable mRequestFocus = new Runnable() {
        public void run() {
            ListActivity.this.mList.focusableViewAvailable(ListActivity.this.mList);
        }
    };

    /* access modifiers changed from: protected */
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Bundle state) {
        ensureList();
        super.onRestoreInstanceState(state);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        this.mHandler.removeCallbacks(this.mRequestFocus);
        super.onDestroy();
    }

    public void onContentChanged() {
        super.onContentChanged();
        View emptyView = findViewById(16908292);
        this.mList = (ListView) findViewById(16908298);
        if (this.mList != null) {
            if (emptyView != null) {
                this.mList.setEmptyView(emptyView);
            }
            this.mList.setOnItemClickListener(this.mOnClickListener);
            if (this.mFinishedStart) {
                setListAdapter(this.mAdapter);
            }
            this.mHandler.post(this.mRequestFocus);
            this.mFinishedStart = true;
            return;
        }
        throw new RuntimeException("Your content must have a ListView whose id attribute is 'android.R.id.list'");
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
            setContentView((int) R.layout.list_content_simple);
        }
    }
}
