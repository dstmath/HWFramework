package android.app;

import android.R;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ListFragment extends Fragment {
    ListAdapter mAdapter;
    CharSequence mEmptyText;
    View mEmptyView;
    private final Handler mHandler = new Handler();
    ListView mList;
    View mListContainer;
    boolean mListShown;
    private final OnItemClickListener mOnClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            ListFragment.this.onListItemClick((ListView) parent, v, position, id);
        }
    };
    View mProgressContainer;
    private final Runnable mRequestFocus = new Runnable() {
        public void run() {
            ListFragment.this.mList.focusableViewAvailable(ListFragment.this.mList);
        }
    };
    TextView mStandardEmptyView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.list_content, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureList();
    }

    public void onDestroyView() {
        this.mHandler.removeCallbacks(this.mRequestFocus);
        this.mList = null;
        this.mListShown = false;
        this.mListContainer = null;
        this.mProgressContainer = null;
        this.mEmptyView = null;
        this.mStandardEmptyView = null;
        super.onDestroyView();
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
    }

    public void setListAdapter(ListAdapter adapter) {
        boolean hadAdapter = this.mAdapter != null;
        this.mAdapter = adapter;
        if (this.mList != null) {
            this.mList.setAdapter(adapter);
            if (!this.mListShown && (hadAdapter ^ 1) != 0) {
                setListShown(true, getView().getWindowToken() != null);
            }
        }
    }

    public void setSelection(int position) {
        ensureList();
        this.mList.setSelection(position);
    }

    public int getSelectedItemPosition() {
        ensureList();
        return this.mList.getSelectedItemPosition();
    }

    public long getSelectedItemId() {
        ensureList();
        return this.mList.getSelectedItemId();
    }

    public ListView getListView() {
        ensureList();
        return this.mList;
    }

    public void setEmptyText(CharSequence text) {
        ensureList();
        if (this.mStandardEmptyView == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        }
        this.mStandardEmptyView.setText(text);
        if (this.mEmptyText == null) {
            this.mList.setEmptyView(this.mStandardEmptyView);
        }
        this.mEmptyText = text;
    }

    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }

    private void setListShown(boolean shown, boolean animate) {
        ensureList();
        if (this.mProgressContainer == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        } else if (this.mListShown != shown) {
            this.mListShown = shown;
            if (shown) {
                if (animate) {
                    this.mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
                    this.mListContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
                } else {
                    this.mProgressContainer.clearAnimation();
                    this.mListContainer.clearAnimation();
                }
                this.mProgressContainer.setVisibility(8);
                this.mListContainer.setVisibility(0);
            } else {
                if (animate) {
                    this.mProgressContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));
                    this.mListContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_out));
                } else {
                    this.mProgressContainer.clearAnimation();
                    this.mListContainer.clearAnimation();
                }
                this.mProgressContainer.setVisibility(0);
                this.mListContainer.setVisibility(8);
            }
        }
    }

    public ListAdapter getListAdapter() {
        return this.mAdapter;
    }

    private void ensureList() {
        if (this.mList == null) {
            View root = getView();
            if (root == null) {
                throw new IllegalStateException("Content view not yet created");
            }
            if (root instanceof ListView) {
                this.mList = (ListView) root;
            } else {
                this.mStandardEmptyView = (TextView) root.findViewById(16908990);
                if (this.mStandardEmptyView == null) {
                    this.mEmptyView = root.findViewById(R.id.empty);
                } else {
                    this.mStandardEmptyView.setVisibility(8);
                }
                this.mProgressContainer = root.findViewById(16909188);
                this.mListContainer = root.findViewById(16909023);
                View rawListView = root.findViewById(R.id.list);
                if (rawListView instanceof ListView) {
                    this.mList = (ListView) rawListView;
                    if (this.mList == null) {
                        throw new RuntimeException("Your content must have a ListView whose id attribute is 'android.R.id.list'");
                    } else if (this.mEmptyView != null) {
                        this.mList.setEmptyView(this.mEmptyView);
                    } else if (this.mEmptyText != null) {
                        this.mStandardEmptyView.setText(this.mEmptyText);
                        this.mList.setEmptyView(this.mStandardEmptyView);
                    }
                } else {
                    throw new RuntimeException("Content has view with id attribute 'android.R.id.list' that is not a ListView class");
                }
            }
            this.mListShown = true;
            this.mList.setOnItemClickListener(this.mOnClickListener);
            if (this.mAdapter != null) {
                ListAdapter adapter = this.mAdapter;
                this.mAdapter = null;
                setListAdapter(adapter);
            } else if (this.mProgressContainer != null) {
                setListShown(false, false);
            }
            this.mHandler.post(this.mRequestFocus);
        }
    }
}
