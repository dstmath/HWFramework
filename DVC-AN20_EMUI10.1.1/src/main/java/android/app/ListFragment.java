package android.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.R;

@Deprecated
public class ListFragment extends Fragment {
    ListAdapter mAdapter;
    CharSequence mEmptyText;
    View mEmptyView;
    private final Handler mHandler = new Handler();
    ListView mList;
    View mListContainer;
    boolean mListShown;
    private final AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        /* class android.app.ListFragment.AnonymousClass2 */

        @Override // android.widget.AdapterView.OnItemClickListener
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            ListFragment.this.onListItemClick((ListView) parent, v, position, id);
        }
    };
    View mProgressContainer;
    private final Runnable mRequestFocus = new Runnable() {
        /* class android.app.ListFragment.AnonymousClass1 */

        public void run() {
            ListFragment.this.mList.focusableViewAvailable(ListFragment.this.mList);
        }
    };
    TextView mStandardEmptyView;

    @Override // android.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(17367060, container, false);
    }

    @Override // android.app.Fragment
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ensureList();
    }

    @Override // android.app.Fragment
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
        boolean z = false;
        boolean hadAdapter = this.mAdapter != null;
        this.mAdapter = adapter;
        ListView listView = this.mList;
        if (listView != null) {
            listView.setAdapter(adapter);
            if (!this.mListShown && !hadAdapter) {
                if (getView().getWindowToken() != null) {
                    z = true;
                }
                setListShown(true, z);
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
        TextView textView = this.mStandardEmptyView;
        if (textView != null) {
            textView.setText(text);
            if (this.mEmptyText == null) {
                this.mList.setEmptyView(this.mStandardEmptyView);
            }
            this.mEmptyText = text;
            return;
        }
        throw new IllegalStateException("Can't be used with a custom content view");
    }

    public void setListShown(boolean shown) {
        setListShown(shown, true);
    }

    public void setListShownNoAnimation(boolean shown) {
        setListShown(shown, false);
    }

    private void setListShown(boolean shown, boolean animate) {
        ensureList();
        View view = this.mProgressContainer;
        if (view == null) {
            throw new IllegalStateException("Can't be used with a custom content view");
        } else if (this.mListShown != shown) {
            this.mListShown = shown;
            if (shown) {
                if (animate) {
                    view.startAnimation(AnimationUtils.loadAnimation(getContext(), 17432577));
                    this.mListContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), 17432576));
                } else {
                    view.clearAnimation();
                    this.mListContainer.clearAnimation();
                }
                this.mProgressContainer.setVisibility(8);
                this.mListContainer.setVisibility(0);
                return;
            }
            if (animate) {
                view.startAnimation(AnimationUtils.loadAnimation(getContext(), 17432576));
                this.mListContainer.startAnimation(AnimationUtils.loadAnimation(getContext(), 17432577));
            } else {
                view.clearAnimation();
                this.mListContainer.clearAnimation();
            }
            this.mProgressContainer.setVisibility(0);
            this.mListContainer.setVisibility(8);
        }
    }

    public ListAdapter getListAdapter() {
        return this.mAdapter;
    }

    private void ensureList() {
        if (this.mList == null) {
            View root = getView();
            if (root != null) {
                if (root instanceof ListView) {
                    this.mList = (ListView) root;
                } else {
                    this.mStandardEmptyView = (TextView) root.findViewById(R.id.internalEmpty);
                    TextView textView = this.mStandardEmptyView;
                    if (textView == null) {
                        this.mEmptyView = root.findViewById(16908292);
                    } else {
                        textView.setVisibility(8);
                    }
                    this.mProgressContainer = root.findViewById(R.id.progressContainer);
                    this.mListContainer = root.findViewById(R.id.listContainer);
                    View rawListView = root.findViewById(16908298);
                    if (rawListView instanceof ListView) {
                        this.mList = (ListView) rawListView;
                        ListView listView = this.mList;
                        if (listView != null) {
                            View view = this.mEmptyView;
                            if (view != null) {
                                listView.setEmptyView(view);
                            } else {
                                CharSequence charSequence = this.mEmptyText;
                                if (charSequence != null) {
                                    this.mStandardEmptyView.setText(charSequence);
                                    this.mList.setEmptyView(this.mStandardEmptyView);
                                }
                            }
                        } else {
                            throw new RuntimeException("Your content must have a ListView whose id attribute is 'android.R.id.list'");
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
                return;
            }
            throw new IllegalStateException("Content view not yet created");
        }
    }
}
