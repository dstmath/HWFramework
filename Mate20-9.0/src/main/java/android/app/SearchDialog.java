package android.app;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import com.android.internal.R;

public class SearchDialog extends Dialog {
    private static final boolean DBG = false;
    private static final String IME_OPTION_NO_MICROPHONE = "nm";
    private static final String INSTANCE_KEY_APPDATA = "data";
    private static final String INSTANCE_KEY_COMPONENT = "comp";
    private static final String INSTANCE_KEY_USER_QUERY = "uQry";
    private static final String LOG_TAG = "SearchDialog";
    private static final int SEARCH_PLATE_LEFT_PADDING_NON_GLOBAL = 7;
    private Context mActivityContext;
    private ImageView mAppIcon;
    private Bundle mAppSearchData;
    private TextView mBadgeLabel;
    private View mCloseSearch;
    private BroadcastReceiver mConfChangeListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
                SearchDialog.this.onConfigurationChanged();
            }
        }
    };
    private ComponentName mLaunchComponent;
    private final SearchView.OnCloseListener mOnCloseListener = new SearchView.OnCloseListener() {
        public boolean onClose() {
            return SearchDialog.this.onClosePressed();
        }
    };
    private final SearchView.OnQueryTextListener mOnQueryChangeListener = new SearchView.OnQueryTextListener() {
        public boolean onQueryTextSubmit(String query) {
            SearchDialog.this.dismiss();
            return false;
        }

        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };
    private final SearchView.OnSuggestionListener mOnSuggestionSelectionListener = new SearchView.OnSuggestionListener() {
        public boolean onSuggestionSelect(int position) {
            return false;
        }

        public boolean onSuggestionClick(int position) {
            SearchDialog.this.dismiss();
            return false;
        }
    };
    private AutoCompleteTextView mSearchAutoComplete;
    private int mSearchAutoCompleteImeOptions;
    private View mSearchPlate;
    private SearchView mSearchView;
    private SearchableInfo mSearchable;
    private String mUserQuery;
    private final Intent mVoiceAppSearchIntent;
    private final Intent mVoiceWebSearchIntent = new Intent("android.speech.action.WEB_SEARCH");
    private Drawable mWorkingSpinner;

    public static class SearchBar extends LinearLayout {
        public SearchBar(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public SearchBar(Context context) {
            super(context);
        }

        public ActionMode startActionModeForChild(View child, ActionMode.Callback callback, int type) {
            if (type != 0) {
                return super.startActionModeForChild(child, callback, type);
            }
            return null;
        }
    }

    static int resolveDialogTheme(Context context) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.searchDialogTheme, outValue, true);
        return outValue.resourceId;
    }

    public SearchDialog(Context context, SearchManager searchManager) {
        super(context, resolveDialogTheme(context));
        this.mVoiceWebSearchIntent.addFlags(268435456);
        this.mVoiceWebSearchIntent.putExtra("android.speech.extra.LANGUAGE_MODEL", "web_search");
        this.mVoiceAppSearchIntent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
        this.mVoiceAppSearchIntent.addFlags(268435456);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window theWindow = getWindow();
        WindowManager.LayoutParams lp = theWindow.getAttributes();
        lp.width = -1;
        lp.height = -1;
        lp.gravity = 55;
        lp.softInputMode = 16;
        theWindow.setAttributes(lp);
        setCanceledOnTouchOutside(true);
    }

    private void createContentView() {
        setContentView((int) R.layout.search_bar);
        this.mSearchView = (SearchView) findViewById(R.id.search_view);
        this.mSearchView.setIconified(false);
        this.mSearchView.setOnCloseListener(this.mOnCloseListener);
        this.mSearchView.setOnQueryTextListener(this.mOnQueryChangeListener);
        this.mSearchView.setOnSuggestionListener(this.mOnSuggestionSelectionListener);
        this.mSearchView.onActionViewExpanded();
        this.mCloseSearch = findViewById(16908327);
        this.mCloseSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SearchDialog.this.dismiss();
            }
        });
        this.mBadgeLabel = (TextView) this.mSearchView.findViewById(R.id.search_badge);
        this.mSearchAutoComplete = (AutoCompleteTextView) this.mSearchView.findViewById(R.id.search_src_text);
        this.mAppIcon = (ImageView) findViewById(R.id.search_app_icon);
        this.mSearchPlate = this.mSearchView.findViewById(R.id.search_plate);
        this.mWorkingSpinner = getContext().getDrawable(R.drawable.search_spinner);
        setWorking(false);
        this.mBadgeLabel.setVisibility(8);
        this.mSearchAutoCompleteImeOptions = this.mSearchAutoComplete.getImeOptions();
    }

    public boolean show(String initialQuery, boolean selectInitialQuery, ComponentName componentName, Bundle appSearchData) {
        boolean success = doShow(initialQuery, selectInitialQuery, componentName, appSearchData);
        if (success) {
            this.mSearchAutoComplete.showDropDownAfterLayout();
        }
        return success;
    }

    private boolean doShow(String initialQuery, boolean selectInitialQuery, ComponentName componentName, Bundle appSearchData) {
        if (!show(componentName, appSearchData)) {
            return false;
        }
        setUserQuery(initialQuery);
        if (selectInitialQuery) {
            this.mSearchAutoComplete.selectAll();
        }
        return true;
    }

    private boolean show(ComponentName componentName, Bundle appSearchData) {
        this.mSearchable = ((SearchManager) this.mContext.getSystemService(Context.SEARCH_SERVICE)).getSearchableInfo(componentName);
        if (this.mSearchable == null) {
            return false;
        }
        this.mLaunchComponent = componentName;
        this.mAppSearchData = appSearchData;
        this.mActivityContext = this.mSearchable.getActivityContext(getContext());
        if (!isShowing()) {
            createContentView();
            this.mSearchView.setSearchableInfo(this.mSearchable);
            this.mSearchView.setAppSearchData(this.mAppSearchData);
            show();
        }
        updateUI();
        return true;
    }

    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        getContext().registerReceiver(this.mConfChangeListener, filter);
    }

    public void onStop() {
        super.onStop();
        getContext().unregisterReceiver(this.mConfChangeListener);
        this.mLaunchComponent = null;
        this.mAppSearchData = null;
        this.mSearchable = null;
        this.mUserQuery = null;
    }

    public void setWorking(boolean working) {
        this.mWorkingSpinner.setAlpha(working ? 255 : 0);
        this.mWorkingSpinner.setVisible(working, false);
        this.mWorkingSpinner.invalidateSelf();
    }

    public Bundle onSaveInstanceState() {
        if (!isShowing()) {
            return null;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_KEY_COMPONENT, this.mLaunchComponent);
        bundle.putBundle("data", this.mAppSearchData);
        bundle.putString(INSTANCE_KEY_USER_QUERY, this.mUserQuery);
        return bundle;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Bundle appSearchData = savedInstanceState.getBundle("data");
            if (doShow(savedInstanceState.getString(INSTANCE_KEY_USER_QUERY), false, (ComponentName) savedInstanceState.getParcelable(INSTANCE_KEY_COMPONENT), appSearchData)) {
            }
        }
    }

    public void onConfigurationChanged() {
        if (this.mSearchable != null && isShowing()) {
            updateSearchAppIcon();
            updateSearchBadge();
            if (isLandscapeMode(getContext())) {
                this.mSearchAutoComplete.ensureImeVisible(true);
            }
        }
    }

    static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }

    private void updateUI() {
        if (this.mSearchable != null) {
            this.mDecor.setVisibility(0);
            updateSearchAutoComplete();
            updateSearchAppIcon();
            updateSearchBadge();
            int inputType = this.mSearchable.getInputType();
            if ((inputType & 15) == 1) {
                inputType &= -65537;
                if (this.mSearchable.getSuggestAuthority() != null) {
                    inputType |= 65536;
                }
            }
            this.mSearchAutoComplete.setInputType(inputType);
            this.mSearchAutoCompleteImeOptions = this.mSearchable.getImeOptions();
            this.mSearchAutoComplete.setImeOptions(this.mSearchAutoCompleteImeOptions);
            if (this.mSearchable.getVoiceSearchEnabled()) {
                this.mSearchAutoComplete.setPrivateImeOptions(IME_OPTION_NO_MICROPHONE);
            } else {
                this.mSearchAutoComplete.setPrivateImeOptions(null);
            }
        }
    }

    private void updateSearchAutoComplete() {
        this.mSearchAutoComplete.setDropDownDismissedOnCompletion(false);
        this.mSearchAutoComplete.setForceIgnoreOutsideTouch(false);
    }

    private void updateSearchAppIcon() {
        Drawable icon;
        PackageManager pm = getContext().getPackageManager();
        try {
            icon = pm.getApplicationIcon(pm.getActivityInfo(this.mLaunchComponent, 0).applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            Drawable icon2 = pm.getDefaultActivityIcon();
            Log.w(LOG_TAG, this.mLaunchComponent + " not found, using generic app icon");
            icon = icon2;
        }
        this.mAppIcon.setImageDrawable(icon);
        this.mAppIcon.setVisibility(0);
        this.mSearchPlate.setPadding(7, this.mSearchPlate.getPaddingTop(), this.mSearchPlate.getPaddingRight(), this.mSearchPlate.getPaddingBottom());
    }

    private void updateSearchBadge() {
        int visibility = 8;
        Drawable icon = null;
        CharSequence text = null;
        if (this.mSearchable.useBadgeIcon()) {
            icon = this.mActivityContext.getDrawable(this.mSearchable.getIconId());
            visibility = 0;
        } else if (this.mSearchable.useBadgeLabel()) {
            text = this.mActivityContext.getResources().getText(this.mSearchable.getLabelId()).toString();
            visibility = 0;
        }
        this.mBadgeLabel.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        this.mBadgeLabel.setText(text);
        this.mBadgeLabel.setVisibility(visibility);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mSearchAutoComplete.isPopupShowing() || !isOutOfBounds(this.mSearchPlate, event)) {
            return super.onTouchEvent(event);
        }
        cancel();
        return true;
    }

    private boolean isOutOfBounds(View v, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        int slop = ViewConfiguration.get(this.mContext).getScaledWindowTouchSlop();
        return x < (-slop) || y < (-slop) || x > v.getWidth() + slop || y > v.getHeight() + slop;
    }

    public void hide() {
        if (isShowing()) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(InputMethodManager.class);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
            }
            super.hide();
        }
    }

    public void launchQuerySearch() {
        launchQuerySearch(0, null);
    }

    /* access modifiers changed from: protected */
    public void launchQuerySearch(int actionKey, String actionMsg) {
        launchIntent(createIntent(Intent.ACTION_SEARCH, null, null, this.mSearchAutoComplete.getText().toString(), actionKey, actionMsg));
    }

    private void launchIntent(Intent intent) {
        if (intent != null) {
            Log.d(LOG_TAG, "launching " + intent);
            try {
                getContext().startActivity(intent);
                dismiss();
            } catch (RuntimeException ex) {
                Log.e(LOG_TAG, "Failed launch activity: " + intent, ex);
            }
        }
    }

    public void setListSelection(int index) {
        this.mSearchAutoComplete.setListSelection(index);
    }

    private Intent createIntent(String action, Uri data, String extraData, String query, int actionKey, String actionMsg) {
        Intent intent = new Intent(action);
        intent.addFlags(268435456);
        if (data != null) {
            intent.setData(data);
        }
        intent.putExtra(SearchManager.USER_QUERY, this.mUserQuery);
        if (query != null) {
            intent.putExtra(SearchManager.QUERY, query);
        }
        if (extraData != null) {
            intent.putExtra(SearchManager.EXTRA_DATA_KEY, extraData);
        }
        if (this.mAppSearchData != null) {
            intent.putExtra(SearchManager.APP_DATA, this.mAppSearchData);
        }
        if (actionKey != 0) {
            intent.putExtra(SearchManager.ACTION_KEY, actionKey);
            intent.putExtra(SearchManager.ACTION_MSG, actionMsg);
        }
        intent.setComponent(this.mSearchable.getSearchActivity());
        return intent;
    }

    private boolean isEmpty(AutoCompleteTextView actv) {
        return TextUtils.getTrimmedLength(actv.getText()) == 0;
    }

    public void onBackPressed() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(InputMethodManager.class);
        if (imm == null || !imm.isFullscreenMode() || !imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0)) {
            cancel();
        }
    }

    /* access modifiers changed from: private */
    public boolean onClosePressed() {
        if (!isEmpty(this.mSearchAutoComplete)) {
            return false;
        }
        dismiss();
        return true;
    }

    private void setUserQuery(String query) {
        if (query == null) {
            query = "";
        }
        this.mUserQuery = query;
        this.mSearchAutoComplete.setText(query);
        this.mSearchAutoComplete.setSelection(query.length());
    }
}
