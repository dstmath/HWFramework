package android.app;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class ExpandableListActivity extends Activity implements View.OnCreateContextMenuListener, ExpandableListView.OnChildClickListener, ExpandableListView.OnGroupCollapseListener, ExpandableListView.OnGroupExpandListener {
    ExpandableListAdapter mAdapter;
    boolean mFinishedStart = false;
    ExpandableListView mList;

    @Override // android.app.Activity, android.view.View.OnCreateContextMenuListener
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    }

    @Override // android.widget.ExpandableListView.OnChildClickListener
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        return false;
    }

    @Override // android.widget.ExpandableListView.OnGroupCollapseListener
    public void onGroupCollapse(int groupPosition) {
    }

    @Override // android.widget.ExpandableListView.OnGroupExpandListener
    public void onGroupExpand(int groupPosition) {
    }

    /* access modifiers changed from: protected */
    @Override // android.app.Activity
    public void onRestoreInstanceState(Bundle state) {
        ensureList();
        super.onRestoreInstanceState(state);
    }

    @Override // android.app.Activity, android.view.Window.Callback
    public void onContentChanged() {
        super.onContentChanged();
        View emptyView = findViewById(16908292);
        this.mList = (ExpandableListView) findViewById(16908298);
        ExpandableListView expandableListView = this.mList;
        if (expandableListView != null) {
            if (emptyView != null) {
                expandableListView.setEmptyView(emptyView);
            }
            this.mList.setOnChildClickListener(this);
            this.mList.setOnGroupExpandListener(this);
            this.mList.setOnGroupCollapseListener(this);
            if (this.mFinishedStart) {
                setListAdapter(this.mAdapter);
            }
            this.mFinishedStart = true;
            return;
        }
        throw new RuntimeException("Your content must have a ExpandableListView whose id attribute is 'android.R.id.list'");
    }

    public void setListAdapter(ExpandableListAdapter adapter) {
        synchronized (this) {
            ensureList();
            this.mAdapter = adapter;
            this.mList.setAdapter(adapter);
        }
    }

    public ExpandableListView getExpandableListView() {
        ensureList();
        return this.mList;
    }

    public ExpandableListAdapter getExpandableListAdapter() {
        return this.mAdapter;
    }

    private void ensureList() {
        if (this.mList == null) {
            setContentView(17367041);
        }
    }

    public long getSelectedId() {
        return this.mList.getSelectedId();
    }

    public long getSelectedPosition() {
        return this.mList.getSelectedPosition();
    }

    public boolean setSelectedChild(int groupPosition, int childPosition, boolean shouldExpandGroup) {
        return this.mList.setSelectedChild(groupPosition, childPosition, shouldExpandGroup);
    }

    public void setSelectedGroup(int groupPosition) {
        this.mList.setSelectedGroup(groupPosition);
    }
}
