package android.preference;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager.OnPreferenceTreeClickListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.R;

public abstract class PreferenceFragment extends Fragment implements OnPreferenceTreeClickListener {
    private static final int FIRST_REQUEST_CODE = 100;
    private static final int MSG_BIND_PREFERENCES = 1;
    private static final String PREFERENCES_TAG = "android:preferences";
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    PreferenceFragment.this.bindPreferences();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHavePrefs;
    private boolean mInitDone;
    private int mLayoutResId = 17367226;
    private ListView mList;
    private OnKeyListener mListOnKeyListener = new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            Object selectedItem = PreferenceFragment.this.mList.getSelectedItem();
            if (!(selectedItem instanceof Preference)) {
                return false;
            }
            return ((Preference) selectedItem).onKey(PreferenceFragment.this.mList.getSelectedView(), keyCode, event);
        }
    };
    private PreferenceManager mPreferenceManager;
    private final Runnable mRequestFocus = new Runnable() {
        public void run() {
            PreferenceFragment.this.mList.focusableViewAvailable(PreferenceFragment.this.mList);
        }
    };

    public interface OnPreferenceStartFragmentCallback {
        boolean onPreferenceStartFragment(PreferenceFragment preferenceFragment, Preference preference);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPreferenceManager = new PreferenceManager(getActivity(), 100);
        this.mPreferenceManager.setFragment(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TypedArray a = getActivity().obtainStyledAttributes(null, R.styleable.PreferenceFragment, android.R.attr.preferenceFragmentStyle, 0);
        this.mLayoutResId = a.getResourceId(0, this.mLayoutResId);
        a.recycle();
        return inflater.inflate(this.mLayoutResId, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TypedArray a = getActivity().obtainStyledAttributes(null, R.styleable.PreferenceFragment, android.R.attr.preferenceFragmentStyle, 0);
        ListView lv = (ListView) view.findViewById(android.R.id.list);
        if (lv != null && a.hasValueOrEmpty(1)) {
            lv.setDivider(a.getDrawable(1));
        }
        a.recycle();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mHavePrefs) {
            bindPreferences();
        }
        this.mInitDone = true;
        if (savedInstanceState != null) {
            Bundle container = savedInstanceState.getBundle(PREFERENCES_TAG);
            if (container != null) {
                PreferenceScreen preferenceScreen = getPreferenceScreen();
                if (preferenceScreen != null) {
                    preferenceScreen.restoreHierarchyState(container);
                }
            }
        }
    }

    public void onStart() {
        super.onStart();
        this.mPreferenceManager.setOnPreferenceTreeClickListener(this);
    }

    public void onStop() {
        super.onStop();
        this.mPreferenceManager.dispatchActivityStop();
        this.mPreferenceManager.setOnPreferenceTreeClickListener(null);
    }

    public void onDestroyView() {
        if (this.mList != null) {
            this.mList.setOnKeyListener(null);
        }
        this.mList = null;
        this.mHandler.removeCallbacks(this.mRequestFocus);
        this.mHandler.removeMessages(1);
        super.onDestroyView();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mPreferenceManager.dispatchActivityDestroy();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            Bundle container = new Bundle();
            preferenceScreen.saveHierarchyState(container);
            outState.putBundle(PREFERENCES_TAG, container);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mPreferenceManager.dispatchActivityResult(requestCode, resultCode, data);
    }

    public PreferenceManager getPreferenceManager() {
        return this.mPreferenceManager;
    }

    public void setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (this.mPreferenceManager.setPreferences(preferenceScreen) && preferenceScreen != null) {
            onUnbindPreferences();
            this.mHavePrefs = true;
            if (this.mInitDone) {
                postBindPreferences();
            }
        }
    }

    public PreferenceScreen getPreferenceScreen() {
        return this.mPreferenceManager.getPreferenceScreen();
    }

    public void addPreferencesFromIntent(Intent intent) {
        requirePreferenceManager();
        setPreferenceScreen(this.mPreferenceManager.inflateFromIntent(intent, getPreferenceScreen()));
    }

    public void addPreferencesFromResource(int preferencesResId) {
        requirePreferenceManager();
        setPreferenceScreen(this.mPreferenceManager.inflateFromResource(getActivity(), preferencesResId, getPreferenceScreen()));
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getFragment() == null || !(getActivity() instanceof OnPreferenceStartFragmentCallback)) {
            return false;
        }
        return ((OnPreferenceStartFragmentCallback) getActivity()).onPreferenceStartFragment(this, preference);
    }

    public Preference findPreference(CharSequence key) {
        if (this.mPreferenceManager == null) {
            return null;
        }
        return this.mPreferenceManager.findPreference(key);
    }

    private void requirePreferenceManager() {
        if (this.mPreferenceManager == null) {
            throw new RuntimeException("This should be called after super.onCreate.");
        }
    }

    private void postBindPreferences() {
        if (!this.mHandler.hasMessages(1)) {
            this.mHandler.obtainMessage(1).sendToTarget();
        }
    }

    private void bindPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        if (preferenceScreen != null) {
            View root = getView();
            if (root != null) {
                View titleView = root.findViewById(android.R.id.title);
                if (titleView instanceof TextView) {
                    CharSequence title = preferenceScreen.getTitle();
                    if (TextUtils.isEmpty(title)) {
                        titleView.setVisibility(8);
                    } else {
                        ((TextView) titleView).setText(title);
                        titleView.setVisibility(0);
                    }
                }
            }
            preferenceScreen.bind(getListView());
        }
        onBindPreferences();
    }

    protected void onBindPreferences() {
    }

    protected void onUnbindPreferences() {
    }

    public ListView getListView() {
        ensureList();
        return this.mList;
    }

    public boolean hasListView() {
        if (this.mList != null) {
            return true;
        }
        View root = getView();
        if (root == null) {
            return false;
        }
        View rawListView = root.findViewById(android.R.id.list);
        if (!(rawListView instanceof ListView)) {
            return false;
        }
        this.mList = (ListView) rawListView;
        return this.mList != null;
    }

    private void ensureList() {
        if (this.mList == null) {
            View root = getView();
            if (root == null) {
                throw new IllegalStateException("Content view not yet created");
            }
            View rawListView = root.findViewById(android.R.id.list);
            if (rawListView instanceof ListView) {
                this.mList = (ListView) rawListView;
                if (this.mList == null) {
                    throw new RuntimeException("Your content must have a ListView whose id attribute is 'android.R.id.list'");
                }
                this.mList.setOnKeyListener(this.mListOnKeyListener);
                this.mHandler.post(this.mRequestFocus);
                return;
            }
            throw new RuntimeException("Content has view with id attribute 'android.R.id.list' that is not a ListView class");
        }
    }
}
