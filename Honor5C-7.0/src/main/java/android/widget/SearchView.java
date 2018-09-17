package android.widget;

import android.app.PendingIntent;
import android.app.SearchableInfo;
import android.app.SearchableInfo.ActionKeyInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.ConstantState;
import android.hwcontrol.HwWidgetFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.CollapsibleActionView;
import android.view.KeyEvent;
import android.view.KeyEvent.DispatcherState;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView.OnEditorActionListener;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.internal.os.HwBootFail;
import com.android.internal.util.Protocol;
import com.huawei.pgmng.log.LogPower;
import huawei.cust.HwCfgFilePolicy;
import java.util.WeakHashMap;
import javax.microedition.khronos.opengles.GL10;

public class SearchView extends LinearLayout implements CollapsibleActionView {
    private static final boolean DBG = false;
    private static final String IME_OPTION_NO_MICROPHONE = "nm";
    private static final String LOG_TAG = "SearchView";
    private Bundle mAppSearchData;
    private boolean mClearingFocus;
    private final ImageView mCloseButton;
    private final ImageView mCollapsedIcon;
    private int mCollapsedImeOptions;
    private final CharSequence mDefaultQueryHint;
    private final View mDropDownAnchor;
    private boolean mExpandedInActionView;
    private final ImageView mGoButton;
    private boolean mIconified;
    private boolean mIconifiedByDefault;
    private int mMaxWidth;
    private CharSequence mOldQueryText;
    private final OnClickListener mOnClickListener;
    private OnCloseListener mOnCloseListener;
    private final OnEditorActionListener mOnEditorActionListener;
    private final OnItemClickListener mOnItemClickListener;
    private final OnItemSelectedListener mOnItemSelectedListener;
    private OnQueryTextListener mOnQueryChangeListener;
    private OnFocusChangeListener mOnQueryTextFocusChangeListener;
    private OnClickListener mOnSearchClickListener;
    private OnSuggestionListener mOnSuggestionListener;
    private final WeakHashMap<String, ConstantState> mOutsideDrawablesCache;
    private CharSequence mQueryHint;
    private boolean mQueryRefinement;
    private Runnable mReleaseCursorRunnable;
    private final ImageView mSearchButton;
    private final View mSearchEditFrame;
    private final Drawable mSearchHintIcon;
    private final View mSearchPlate;
    private final SearchAutoComplete mSearchSrcTextView;
    private Rect mSearchSrcTextViewBounds;
    private Rect mSearchSrtTextViewBoundsExpanded;
    private SearchableInfo mSearchable;
    private Runnable mShowImeRunnable;
    private final View mSubmitArea;
    private boolean mSubmitButtonEnabled;
    private final int mSuggestionCommitIconResId;
    private final int mSuggestionRowLayout;
    private CursorAdapter mSuggestionsAdapter;
    private int[] mTemp;
    private int[] mTemp2;
    OnKeyListener mTextKeyListener;
    private TextWatcher mTextWatcher;
    private UpdatableTouchDelegate mTouchDelegate;
    private Runnable mUpdateDrawableStateRunnable;
    private CharSequence mUserQuery;
    private final Intent mVoiceAppSearchIntent;
    private final ImageView mVoiceButton;
    private boolean mVoiceButtonEnabled;
    private final Intent mVoiceWebSearchIntent;

    public interface OnCloseListener {
        boolean onClose();
    }

    public interface OnQueryTextListener {
        boolean onQueryTextChange(String str);

        boolean onQueryTextSubmit(String str);
    }

    public interface OnSuggestionListener {
        boolean onSuggestionClick(int i);

        boolean onSuggestionSelect(int i);
    }

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = null;
        boolean isIconified;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.SearchView.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.SearchView.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.SearchView.SavedState.<clinit>():void");
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            this.isIconified = ((Boolean) source.readValue(null)).booleanValue();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeValue(Boolean.valueOf(this.isIconified));
        }

        public String toString() {
            return "SearchView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " isIconified=" + this.isIconified + "}";
        }
    }

    public static class SearchAutoComplete extends AutoCompleteTextView {
        private SearchView mSearchView;
        private int mThreshold;

        public SearchAutoComplete(Context context) {
            super(context);
            this.mThreshold = getThreshold();
        }

        public SearchAutoComplete(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.mThreshold = getThreshold();
        }

        public SearchAutoComplete(Context context, AttributeSet attrs, int defStyleAttrs) {
            super(context, attrs, defStyleAttrs);
            this.mThreshold = getThreshold();
        }

        public SearchAutoComplete(Context context, AttributeSet attrs, int defStyleAttrs, int defStyleRes) {
            super(context, attrs, defStyleAttrs, defStyleRes);
            this.mThreshold = getThreshold();
        }

        protected void onFinishInflate() {
            super.onFinishInflate();
            setMinWidth((int) TypedValue.applyDimension(1, (float) getSearchViewTextMinWidthDp(), getResources().getDisplayMetrics()));
        }

        void setSearchView(SearchView searchView) {
            this.mSearchView = searchView;
        }

        public void setThreshold(int threshold) {
            super.setThreshold(threshold);
            this.mThreshold = threshold;
        }

        private boolean isEmpty() {
            return TextUtils.getTrimmedLength(getText()) == 0 ? true : SearchView.DBG;
        }

        protected void replaceText(CharSequence text) {
        }

        public void performCompletion() {
        }

        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);
            if (hasWindowFocus && this.mSearchView.hasFocus() && getVisibility() == 0) {
                ((InputMethodManager) getContext().getSystemService(InputMethodManager.class)).showSoftInput(this, 0);
                if (SearchView.isLandscapeMode(getContext())) {
                    ensureImeVisible(true);
                }
            }
        }

        protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
            this.mSearchView.onTextFocusChanged();
        }

        public boolean enoughToFilter() {
            if (this.mSearchView.isInActionMode() && this.mSearchView.isSubmitButtonEnabled()) {
                return SearchView.DBG;
            }
            return this.mThreshold > 0 ? super.enoughToFilter() : true;
        }

        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (keyCode == 4) {
                DispatcherState state;
                if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                    state = getKeyDispatcherState();
                    if (state != null) {
                        state.startTracking(event, this);
                    }
                    return true;
                } else if (event.getAction() == 1) {
                    state = getKeyDispatcherState();
                    if (state != null) {
                        state.handleUpEvent(event);
                    }
                    if (event.isTracking() && !event.isCanceled()) {
                        if (this.mSearchView.isInActionMode()) {
                            return SearchView.DBG;
                        }
                        this.mSearchView.clearFocus();
                        this.mSearchView.setImeVisibility(SearchView.DBG);
                        return true;
                    }
                }
            }
            return super.onKeyPreIme(keyCode, event);
        }

        private int getSearchViewTextMinWidthDp() {
            Configuration configuration = getResources().getConfiguration();
            int width = configuration.screenWidthDp;
            int height = configuration.screenHeightDp;
            int orientation = configuration.orientation;
            if (width >= 960 && height >= 720 && orientation == 2) {
                return GL10.GL_DEPTH_BUFFER_BIT;
            }
            if (width >= HwBootFail.PHASE_THIRD_PARTY_APPS_CAN_START || (width >= DisplayMetrics.DENSITY_XXXHIGH && height >= HwBootFail.PHASE_LOCK_SETTINGS_READY)) {
                return MetricsEvent.ACTION_LS_NOTE;
            }
            return LogPower.WAKELOCK_ACQUIRED;
        }
    }

    private static class UpdatableTouchDelegate extends TouchDelegate {
        private final Rect mActualBounds;
        private boolean mDelegateTargeted;
        private final View mDelegateView;
        private final int mSlop;
        private final Rect mSlopBounds;
        private final Rect mTargetBounds;

        public UpdatableTouchDelegate(Rect targetBounds, Rect actualBounds, View delegateView) {
            super(targetBounds, delegateView);
            this.mSlop = ViewConfiguration.get(delegateView.getContext()).getScaledTouchSlop();
            this.mTargetBounds = new Rect();
            this.mSlopBounds = new Rect();
            this.mActualBounds = new Rect();
            setBounds(targetBounds, actualBounds);
            this.mDelegateView = delegateView;
        }

        public void setBounds(Rect desiredBounds, Rect actualBounds) {
            this.mTargetBounds.set(desiredBounds);
            this.mSlopBounds.set(desiredBounds);
            this.mSlopBounds.inset(-this.mSlop, -this.mSlop);
            this.mActualBounds.set(actualBounds);
        }

        public boolean onTouchEvent(MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            boolean sendToDelegate = SearchView.DBG;
            boolean hit = true;
            switch (event.getAction()) {
                case HwCfgFilePolicy.GLOBAL /*0*/:
                    if (this.mTargetBounds.contains(x, y)) {
                        this.mDelegateTargeted = true;
                        sendToDelegate = true;
                        break;
                    }
                    break;
                case HwCfgFilePolicy.EMUI /*1*/:
                case HwCfgFilePolicy.PC /*2*/:
                    sendToDelegate = this.mDelegateTargeted;
                    if (sendToDelegate && !this.mSlopBounds.contains(x, y)) {
                        hit = SearchView.DBG;
                        break;
                    }
                case HwCfgFilePolicy.BASE /*3*/:
                    sendToDelegate = this.mDelegateTargeted;
                    this.mDelegateTargeted = SearchView.DBG;
                    break;
            }
            if (!sendToDelegate) {
                return SearchView.DBG;
            }
            if (!hit || this.mActualBounds.contains(x, y)) {
                event.setLocation((float) (x - this.mActualBounds.left), (float) (y - this.mActualBounds.top));
            } else {
                event.setLocation((float) (this.mDelegateView.getWidth() / 2), (float) (this.mDelegateView.getHeight() / 2));
            }
            return this.mDelegateView.dispatchTouchEvent(event);
        }
    }

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.searchViewStyle);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSearchSrcTextViewBounds = new Rect();
        this.mSearchSrtTextViewBoundsExpanded = new Rect();
        this.mTemp = new int[2];
        this.mTemp2 = new int[2];
        this.mShowImeRunnable = new Runnable() {
            public void run() {
                InputMethodManager imm = (InputMethodManager) SearchView.this.getContext().getSystemService(InputMethodManager.class);
                if (imm != null) {
                    imm.showSoftInputUnchecked(0, null);
                }
            }
        };
        this.mUpdateDrawableStateRunnable = new Runnable() {
            public void run() {
                SearchView.this.updateFocusedState();
            }
        };
        this.mReleaseCursorRunnable = new Runnable() {
            public void run() {
                if (SearchView.this.mSuggestionsAdapter != null && (SearchView.this.mSuggestionsAdapter instanceof SuggestionsAdapter)) {
                    SearchView.this.mSuggestionsAdapter.changeCursor(null);
                }
            }
        };
        this.mOutsideDrawablesCache = new WeakHashMap();
        this.mOnClickListener = new OnClickListener() {
            public void onClick(View v) {
                if (v == SearchView.this.mSearchButton) {
                    SearchView.this.onSearchClicked();
                } else if (v == SearchView.this.mCloseButton) {
                    SearchView.this.onCloseClicked();
                } else if (v == SearchView.this.mGoButton) {
                    SearchView.this.onSubmitQuery();
                } else if (v == SearchView.this.mVoiceButton) {
                    SearchView.this.onVoiceClicked();
                } else if (v == SearchView.this.mSearchSrcTextView) {
                    SearchView.this.forceSuggestionQuery();
                }
            }
        };
        this.mTextKeyListener = new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (SearchView.this.mSearchable == null) {
                    return SearchView.DBG;
                }
                if (SearchView.this.mSearchSrcTextView.isPopupShowing() && SearchView.this.mSearchSrcTextView.getListSelection() != -1) {
                    return SearchView.this.onSuggestionsKey(v, keyCode, event);
                }
                if (!SearchView.this.mSearchSrcTextView.isEmpty() && event.hasNoModifiers()) {
                    if (event.getAction() == 1 && keyCode == 66) {
                        v.cancelLongPress();
                        SearchView.this.launchQuerySearch(0, null, SearchView.this.mSearchSrcTextView.getText().toString());
                        return true;
                    } else if (event.getAction() == 0) {
                        ActionKeyInfo actionKey = SearchView.this.mSearchable.findActionKey(keyCode);
                        if (!(actionKey == null || actionKey.getQueryActionMsg() == null)) {
                            SearchView.this.launchQuerySearch(keyCode, actionKey.getQueryActionMsg(), SearchView.this.mSearchSrcTextView.getText().toString());
                            return true;
                        }
                    }
                }
                return SearchView.DBG;
            }
        };
        this.mOnEditorActionListener = new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                SearchView.this.onSubmitQuery();
                return true;
            }
        };
        this.mOnItemClickListener = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                SearchView.this.onItemClicked(position, 0, null);
            }
        };
        this.mOnItemSelectedListener = new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                SearchView.this.onItemSelected(position);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        };
        this.mTextWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int before, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int after) {
                SearchView.this.onTextChanged(s);
            }

            public void afterTextChanged(Editable s) {
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SearchView, defStyleAttr, defStyleRes);
        inflateSearchView(context, attrs, a);
        this.mSearchSrcTextView = (SearchAutoComplete) findViewById(R.id.search_src_text);
        this.mSearchSrcTextView.setSearchView(this);
        this.mSearchEditFrame = findViewById(R.id.search_edit_frame);
        this.mSearchPlate = findViewById(R.id.search_plate);
        this.mSubmitArea = findViewById(R.id.submit_area);
        this.mSearchButton = (ImageView) findViewById(R.id.search_button);
        this.mGoButton = (ImageView) findViewById(R.id.search_go_btn);
        this.mCloseButton = (ImageView) findViewById(R.id.search_close_btn);
        this.mVoiceButton = (ImageView) findViewById(R.id.search_voice_btn);
        this.mCollapsedIcon = (ImageView) findViewById(R.id.search_mag_icon);
        setIconsAndBackgrounds(a);
        if (a.hasValueOrEmpty(15)) {
            this.mSearchHintIcon = a.getDrawable(15);
        } else {
            this.mSearchHintIcon = a.getDrawable(9);
        }
        this.mSuggestionRowLayout = a.getResourceId(12, R.layout.search_dropdown_item_icons_2line);
        this.mSuggestionCommitIconResId = a.getResourceId(11, 0);
        this.mSearchButton.setOnClickListener(this.mOnClickListener);
        this.mCloseButton.setOnClickListener(this.mOnClickListener);
        this.mGoButton.setOnClickListener(this.mOnClickListener);
        this.mVoiceButton.setOnClickListener(this.mOnClickListener);
        this.mSearchSrcTextView.setOnClickListener(this.mOnClickListener);
        this.mSearchSrcTextView.addTextChangedListener(this.mTextWatcher);
        this.mSearchSrcTextView.setOnEditorActionListener(this.mOnEditorActionListener);
        this.mSearchSrcTextView.setOnItemClickListener(this.mOnItemClickListener);
        this.mSearchSrcTextView.setOnItemSelectedListener(this.mOnItemSelectedListener);
        this.mSearchSrcTextView.setOnKeyListener(this.mTextKeyListener);
        this.mSearchSrcTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (SearchView.this.mOnQueryTextFocusChangeListener != null) {
                    SearchView.this.mOnQueryTextFocusChangeListener.onFocusChange(SearchView.this, hasFocus);
                }
            }
        });
        setIconifiedByDefault(a.getBoolean(5, true));
        int maxWidth = a.getDimensionPixelSize(2, -1);
        if (maxWidth != -1) {
            setMaxWidth(maxWidth);
        }
        this.mDefaultQueryHint = a.getText(16);
        this.mQueryHint = a.getText(6);
        int imeOptions = a.getInt(4, -1);
        if (imeOptions != -1) {
            setImeOptions(imeOptions);
        }
        int inputType = a.getInt(3, -1);
        if (inputType != -1) {
            setInputType(inputType);
        }
        setFocusable(a.getBoolean(0, true));
        a.recycle();
        this.mVoiceWebSearchIntent = new Intent("android.speech.action.WEB_SEARCH");
        this.mVoiceWebSearchIntent.addFlags(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        this.mVoiceWebSearchIntent.putExtra("android.speech.extra.LANGUAGE_MODEL", "web_search");
        this.mVoiceAppSearchIntent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
        this.mVoiceAppSearchIntent.addFlags(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        this.mDropDownAnchor = findViewById(this.mSearchSrcTextView.getDropDownAnchor());
        if (this.mDropDownAnchor != null) {
            this.mDropDownAnchor.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    SearchView.this.adjustDropDownSizeAndPosition();
                }
            });
        }
        updateViewsVisibility(this.mIconifiedByDefault);
        updateQueryHint();
    }

    int getSuggestionRowLayout() {
        return this.mSuggestionRowLayout;
    }

    int getSuggestionCommitIconResId() {
        return this.mSuggestionCommitIconResId;
    }

    public void setSearchableInfo(SearchableInfo searchable) {
        this.mSearchable = searchable;
        if (this.mSearchable != null) {
            updateSearchAutoComplete();
            updateQueryHint();
        }
        this.mVoiceButtonEnabled = hasVoiceSearch();
        if (this.mVoiceButtonEnabled) {
            this.mSearchSrcTextView.setPrivateImeOptions(IME_OPTION_NO_MICROPHONE);
        }
        updateViewsVisibility(isIconified());
    }

    public void setAppSearchData(Bundle appSearchData) {
        this.mAppSearchData = appSearchData;
    }

    public void setImeOptions(int imeOptions) {
        this.mSearchSrcTextView.setImeOptions(imeOptions);
    }

    public int getImeOptions() {
        return this.mSearchSrcTextView.getImeOptions();
    }

    public void setInputType(int inputType) {
        this.mSearchSrcTextView.setInputType(inputType);
    }

    public int getInputType() {
        return this.mSearchSrcTextView.getInputType();
    }

    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        if (this.mClearingFocus || !isFocusable()) {
            return DBG;
        }
        if (isIconified()) {
            return super.requestFocus(direction, previouslyFocusedRect);
        }
        boolean result = this.mSearchSrcTextView.requestFocus(direction, previouslyFocusedRect);
        if (result) {
            updateViewsVisibility(DBG);
        }
        return result;
    }

    public void clearFocus() {
        this.mClearingFocus = true;
        setImeVisibility(DBG);
        super.clearFocus();
        this.mSearchSrcTextView.clearFocus();
        this.mClearingFocus = DBG;
    }

    public void setOnQueryTextListener(OnQueryTextListener listener) {
        this.mOnQueryChangeListener = listener;
    }

    public void setOnCloseListener(OnCloseListener listener) {
        this.mOnCloseListener = listener;
    }

    public void setOnQueryTextFocusChangeListener(OnFocusChangeListener listener) {
        this.mOnQueryTextFocusChangeListener = listener;
    }

    public void setOnSuggestionListener(OnSuggestionListener listener) {
        this.mOnSuggestionListener = listener;
    }

    public void setOnSearchClickListener(OnClickListener listener) {
        this.mOnSearchClickListener = listener;
    }

    public CharSequence getQuery() {
        return this.mSearchSrcTextView.getText();
    }

    public void setQuery(CharSequence query, boolean submit) {
        this.mSearchSrcTextView.setText(query);
        if (query != null) {
            this.mSearchSrcTextView.setSelection(this.mSearchSrcTextView.length());
            this.mUserQuery = query;
        }
        if ((!isEmuiStyle() || !isInActionMode()) && submit && !TextUtils.isEmpty(query)) {
            onSubmitQuery();
        }
    }

    public void setQueryHint(CharSequence hint) {
        this.mQueryHint = hint;
        updateQueryHint();
    }

    public CharSequence getQueryHint() {
        if (this.mQueryHint != null) {
            return this.mQueryHint;
        }
        if (this.mSearchable == null || this.mSearchable.getHintId() == 0) {
            return this.mDefaultQueryHint;
        }
        return getContext().getText(this.mSearchable.getHintId());
    }

    public void setIconifiedByDefault(boolean iconified) {
        if (this.mIconifiedByDefault != iconified) {
            this.mIconifiedByDefault = iconified;
            updateViewsVisibility(iconified);
            updateQueryHint();
        }
    }

    public boolean isIconfiedByDefault() {
        return this.mIconifiedByDefault;
    }

    public void setIconified(boolean iconify) {
        if (iconify) {
            onCloseClicked();
        } else {
            onSearchClicked();
        }
    }

    public boolean isIconified() {
        return this.mIconified;
    }

    public void setSubmitButtonEnabled(boolean enabled) {
        this.mSubmitButtonEnabled = enabled;
        updateViewsVisibility(isIconified());
    }

    public boolean isSubmitButtonEnabled() {
        return this.mSubmitButtonEnabled;
    }

    public void setQueryRefinementEnabled(boolean enable) {
        this.mQueryRefinement = enable;
        if (this.mSuggestionsAdapter instanceof SuggestionsAdapter) {
            ((SuggestionsAdapter) this.mSuggestionsAdapter).setQueryRefinement(enable ? 2 : 1);
        }
    }

    public boolean isQueryRefinementEnabled() {
        return this.mQueryRefinement;
    }

    public void setSuggestionsAdapter(CursorAdapter adapter) {
        this.mSuggestionsAdapter = adapter;
        this.mSearchSrcTextView.setAdapter(this.mSuggestionsAdapter);
    }

    public CursorAdapter getSuggestionsAdapter() {
        return this.mSuggestionsAdapter;
    }

    public void setMaxWidth(int maxpixels) {
        this.mMaxWidth = maxpixels;
        requestLayout();
    }

    public int getMaxWidth() {
        return this.mMaxWidth;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isIconified()) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        switch (widthMode) {
            case RtlSpacingHelper.UNDEFINED /*-2147483648*/:
                if (this.mMaxWidth <= 0) {
                    width = Math.min(getPreferredWidth(), width);
                    break;
                } else {
                    width = Math.min(this.mMaxWidth, width);
                    break;
                }
            case HwCfgFilePolicy.GLOBAL /*0*/:
                if (this.mMaxWidth <= 0) {
                    width = getPreferredWidth();
                    break;
                } else {
                    width = this.mMaxWidth;
                    break;
                }
            case EditorInfo.IME_FLAG_NO_ENTER_ACTION /*1073741824*/:
                if (this.mMaxWidth > 0) {
                    width = Math.min(this.mMaxWidth, width);
                    break;
                }
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        switch (heightMode) {
            case RtlSpacingHelper.UNDEFINED /*-2147483648*/:
            case HwCfgFilePolicy.GLOBAL /*0*/:
                height = Math.min(getPreferredHeight(), height);
                break;
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(width, EditorInfo.IME_FLAG_NO_ENTER_ACTION), MeasureSpec.makeMeasureSpec(height, EditorInfo.IME_FLAG_NO_ENTER_ACTION));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            getChildBoundsWithinSearchView(this.mSearchSrcTextView, this.mSearchSrcTextViewBounds);
            this.mSearchSrtTextViewBoundsExpanded.set(this.mSearchSrcTextViewBounds.left, 0, this.mSearchSrcTextViewBounds.right, bottom - top);
            if (this.mTouchDelegate == null) {
                this.mTouchDelegate = new UpdatableTouchDelegate(this.mSearchSrtTextViewBoundsExpanded, this.mSearchSrcTextViewBounds, this.mSearchSrcTextView);
                setTouchDelegate(this.mTouchDelegate);
                return;
            }
            this.mTouchDelegate.setBounds(this.mSearchSrtTextViewBoundsExpanded, this.mSearchSrcTextViewBounds);
        }
    }

    private void getChildBoundsWithinSearchView(View view, Rect rect) {
        view.getLocationInWindow(this.mTemp);
        getLocationInWindow(this.mTemp2);
        int top = this.mTemp[1] - this.mTemp2[1];
        int left = this.mTemp[0] - this.mTemp2[0];
        rect.set(left, top, view.getWidth() + left, view.getHeight() + top);
    }

    private int getPreferredWidth() {
        return getContext().getResources().getDimensionPixelSize(R.dimen.search_view_preferred_width);
    }

    private int getPreferredHeight() {
        if (HwWidgetFactory.isHwTheme(getContext())) {
            return getContext().getResources().getDimensionPixelSize(androidhwext.R.dimen.search_view_preferred_height_emui);
        }
        return getContext().getResources().getDimensionPixelSize(R.dimen.search_view_preferred_height);
    }

    public void updateViewsVisibility(boolean collapsed) {
        int i;
        int iconVisibility;
        boolean z = DBG;
        this.mIconified = collapsed;
        int visCollapsed = collapsed ? 0 : 8;
        boolean hasText = TextUtils.isEmpty(this.mSearchSrcTextView.getText()) ? DBG : true;
        this.mSearchButton.setVisibility(visCollapsed);
        updateSubmitButton(hasText);
        View view = this.mSearchEditFrame;
        if (collapsed) {
            i = 8;
        } else {
            i = 0;
        }
        view.setVisibility(i);
        if (this.mCollapsedIcon.getDrawable() == null || this.mIconifiedByDefault) {
            iconVisibility = 8;
        } else {
            iconVisibility = 0;
        }
        if (!isEmuiStyle()) {
            this.mCollapsedIcon.setVisibility(iconVisibility);
        }
        updateCloseButton();
        if (!hasText) {
            z = true;
        }
        updateVoiceButton(z);
        updateSubmitArea();
    }

    private boolean hasVoiceSearch() {
        boolean z = DBG;
        if (this.mSearchable != null && this.mSearchable.getVoiceSearchEnabled()) {
            Intent testIntent = null;
            if (this.mSearchable.getVoiceSearchLaunchWebSearch()) {
                testIntent = this.mVoiceWebSearchIntent;
            } else if (this.mSearchable.getVoiceSearchLaunchRecognizer()) {
                testIntent = this.mVoiceAppSearchIntent;
            }
            if (testIntent != null) {
                if (getContext().getPackageManager().resolveActivity(testIntent, Protocol.BASE_SYSTEM_RESERVED) != null) {
                    z = true;
                }
                return z;
            }
        }
        return DBG;
    }

    private boolean isSubmitAreaEnabled() {
        return ((this.mSubmitButtonEnabled || this.mVoiceButtonEnabled) && !isIconified()) ? true : DBG;
    }

    protected void updateSubmitButton(boolean hasText) {
        int visibility = 8;
        if (this.mSubmitButtonEnabled && isSubmitAreaEnabled() && hasFocus() && (hasText || !this.mVoiceButtonEnabled)) {
            visibility = 0;
        }
        this.mGoButton.setVisibility(visibility);
    }

    protected void updateSubmitArea() {
        int visibility = 8;
        if (isSubmitAreaEnabled() && (this.mGoButton.getVisibility() == 0 || this.mVoiceButton.getVisibility() == 0)) {
            visibility = 0;
        }
        this.mSubmitArea.setVisibility(visibility);
    }

    private void updateCloseButton() {
        boolean hasText;
        boolean z = DBG;
        if (TextUtils.isEmpty(this.mSearchSrcTextView.getText())) {
            hasText = DBG;
        } else {
            hasText = true;
        }
        boolean showClose = (hasText || (this.mIconifiedByDefault && !this.mExpandedInActionView)) ? true : DBG;
        this.mCloseButton.setVisibility(showClose ? 0 : 8);
        Drawable closeButtonImg = this.mCloseButton.getDrawable();
        if (isEmuiStyle()) {
            if (!showClose) {
                z = true;
            }
            updateVoiceButton(z);
        }
        if (closeButtonImg != null) {
            int[] iArr;
            if (hasText) {
                iArr = ENABLED_STATE_SET;
            } else {
                iArr = EMPTY_STATE_SET;
            }
            closeButtonImg.setState(iArr);
        }
    }

    private void postUpdateFocusedState() {
        post(this.mUpdateDrawableStateRunnable);
    }

    private void updateFocusedState() {
        int[] stateSet = this.mSearchSrcTextView.hasFocus() ? FOCUSED_STATE_SET : EMPTY_STATE_SET;
        Drawable searchPlateBg = this.mSearchPlate.getBackground();
        if (searchPlateBg != null) {
            searchPlateBg.setState(stateSet);
        }
        Drawable submitAreaBg = this.mSubmitArea.getBackground();
        if (submitAreaBg != null) {
            submitAreaBg.setState(stateSet);
        }
        invalidate();
    }

    protected void onDetachedFromWindow() {
        removeCallbacks(this.mUpdateDrawableStateRunnable);
        post(this.mReleaseCursorRunnable);
        super.onDetachedFromWindow();
    }

    private void setImeVisibility(boolean visible) {
        if (visible) {
            post(this.mShowImeRunnable);
            return;
        }
        removeCallbacks(this.mShowImeRunnable);
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(InputMethodManager.class);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    void onQueryRefine(CharSequence queryText) {
        setQuery(queryText);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mSearchable == null) {
            return DBG;
        }
        ActionKeyInfo actionKey = this.mSearchable.findActionKey(keyCode);
        if (actionKey == null || actionKey.getQueryActionMsg() == null) {
            return super.onKeyDown(keyCode, event);
        }
        launchQuerySearch(keyCode, actionKey.getQueryActionMsg(), this.mSearchSrcTextView.getText().toString());
        return true;
    }

    private boolean onSuggestionsKey(View v, int keyCode, KeyEvent event) {
        if (this.mSearchable != null && this.mSuggestionsAdapter != null && event.getAction() == 0 && event.hasNoModifiers()) {
            if (keyCode == 66 || keyCode == 84 || keyCode == 61) {
                return onItemClicked(this.mSearchSrcTextView.getListSelection(), 0, null);
            }
            if (keyCode == 21 || keyCode == 22) {
                this.mSearchSrcTextView.setSelection(keyCode == 21 ? 0 : this.mSearchSrcTextView.length());
                this.mSearchSrcTextView.setListSelection(0);
                this.mSearchSrcTextView.clearListSelection();
                this.mSearchSrcTextView.ensureImeVisible(true);
                return true;
            } else if (keyCode == 19 && this.mSearchSrcTextView.getListSelection() == 0) {
                return DBG;
            } else {
                ActionKeyInfo actionKey = this.mSearchable.findActionKey(keyCode);
                if (!(actionKey == null || (actionKey.getSuggestActionMsg() == null && actionKey.getSuggestActionMsgColumn() == null))) {
                    int position = this.mSearchSrcTextView.getListSelection();
                    if (position != -1) {
                        Cursor c = this.mSuggestionsAdapter.getCursor();
                        if (c.moveToPosition(position)) {
                            String actionMsg = getActionKeyMessage(c, actionKey);
                            if (actionMsg != null && actionMsg.length() > 0) {
                                return onItemClicked(position, keyCode, actionMsg);
                            }
                        }
                    }
                }
            }
        }
        return DBG;
    }

    private static String getActionKeyMessage(Cursor c, ActionKeyInfo actionKey) {
        String result = null;
        String column = actionKey.getSuggestActionMsgColumn();
        if (column != null) {
            result = SuggestionsAdapter.getColumnString(c, column);
        }
        if (result == null) {
            return actionKey.getSuggestActionMsg();
        }
        return result;
    }

    protected CharSequence getDecoratedHint(CharSequence hintText) {
        if (!this.mIconifiedByDefault || this.mSearchHintIcon == null) {
            return hintText;
        }
        int textSize = (int) (((double) this.mSearchSrcTextView.getTextSize()) * 1.25d);
        this.mSearchHintIcon.setBounds(0, 0, textSize, textSize);
        SpannableStringBuilder ssb = new SpannableStringBuilder("   ");
        ssb.setSpan(new ImageSpan(this.mSearchHintIcon), 1, 2, 33);
        ssb.append(hintText);
        return ssb;
    }

    private void updateQueryHint() {
        CharSequence hint = getQueryHint();
        SearchAutoComplete searchAutoComplete = this.mSearchSrcTextView;
        if (hint == null) {
            hint = "";
        }
        searchAutoComplete.setHint(getDecoratedHint(hint));
    }

    private void updateSearchAutoComplete() {
        int i = 1;
        this.mSearchSrcTextView.setDropDownAnimationStyle(0);
        this.mSearchSrcTextView.setThreshold(this.mSearchable.getSuggestThreshold());
        this.mSearchSrcTextView.setImeOptions(this.mSearchable.getImeOptions());
        int inputType = this.mSearchable.getInputType();
        if ((inputType & 15) == 1) {
            inputType &= -65537;
            if (this.mSearchable.getSuggestAuthority() != null) {
                inputType = (inputType | Protocol.BASE_SYSTEM_RESERVED) | Protocol.BASE_CONNECTIVITY_MANAGER;
            }
        }
        this.mSearchSrcTextView.setInputType(inputType);
        if (this.mSuggestionsAdapter != null) {
            this.mSuggestionsAdapter.changeCursor(null);
        }
        if (this.mSearchable.getSuggestAuthority() != null) {
            this.mSuggestionsAdapter = new SuggestionsAdapter(getContext(), this, this.mSearchable, this.mOutsideDrawablesCache);
            this.mSearchSrcTextView.setAdapter(this.mSuggestionsAdapter);
            SuggestionsAdapter suggestionsAdapter = (SuggestionsAdapter) this.mSuggestionsAdapter;
            if (this.mQueryRefinement) {
                i = 2;
            }
            suggestionsAdapter.setQueryRefinement(i);
        }
    }

    private void updateVoiceButton(boolean empty) {
        int visibility = 8;
        if (this.mVoiceButtonEnabled && !isIconified() && empty) {
            visibility = 0;
            if (!isEmuiStyle()) {
                this.mGoButton.setVisibility(8);
            }
        }
        this.mVoiceButton.setVisibility(visibility);
    }

    private void onTextChanged(CharSequence newText) {
        boolean hasText;
        boolean z = DBG;
        CharSequence text = this.mSearchSrcTextView.getText();
        this.mUserQuery = text;
        if (TextUtils.isEmpty(text)) {
            hasText = DBG;
        } else {
            hasText = true;
        }
        updateSubmitButton(hasText);
        if (!hasText) {
            z = true;
        }
        updateVoiceButton(z);
        updateCloseButton();
        updateSubmitArea();
        if (!(this.mOnQueryChangeListener == null || TextUtils.equals(newText, this.mOldQueryText))) {
            this.mOnQueryChangeListener.onQueryTextChange(newText.toString());
        }
        this.mOldQueryText = newText.toString();
    }

    private void onSubmitQuery() {
        CharSequence query = this.mSearchSrcTextView.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            if (this.mOnQueryChangeListener == null || !this.mOnQueryChangeListener.onQueryTextSubmit(query.toString())) {
                if (this.mSearchable != null) {
                    launchQuerySearch(0, null, query.toString());
                }
                setImeVisibility(DBG);
                dismissSuggestions();
            }
        }
    }

    private void dismissSuggestions() {
        this.mSearchSrcTextView.dismissDropDown();
    }

    private void onCloseClicked() {
        if (!TextUtils.isEmpty(this.mSearchSrcTextView.getText())) {
            this.mSearchSrcTextView.setText((CharSequence) "");
            this.mSearchSrcTextView.requestFocus();
            setImeVisibility(true);
        } else if (!this.mIconifiedByDefault) {
        } else {
            if (this.mOnCloseListener == null || !this.mOnCloseListener.onClose()) {
                clearFocus();
                updateViewsVisibility(true);
            }
        }
    }

    private void onSearchClicked() {
        updateViewsVisibility(DBG);
        this.mSearchSrcTextView.requestFocus();
        setImeVisibility(true);
        if (this.mOnSearchClickListener != null) {
            this.mOnSearchClickListener.onClick(this);
        }
    }

    private void onVoiceClicked() {
        if (this.mSearchable != null) {
            SearchableInfo searchable = this.mSearchable;
            try {
                if (searchable.getVoiceSearchLaunchWebSearch()) {
                    getContext().startActivity(createVoiceWebSearchIntent(this.mVoiceWebSearchIntent, searchable));
                } else if (searchable.getVoiceSearchLaunchRecognizer()) {
                    getContext().startActivity(createVoiceAppSearchIntent(this.mVoiceAppSearchIntent, searchable));
                }
            } catch (ActivityNotFoundException e) {
                Log.w(LOG_TAG, "Could not find voice search activity");
            }
        }
    }

    void onTextFocusChanged() {
        updateViewsVisibility(isIconified());
        postUpdateFocusedState();
        if (this.mSearchSrcTextView.hasFocus()) {
            forceSuggestionQuery();
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        postUpdateFocusedState();
    }

    public void onActionViewCollapsed() {
        setQuery("", DBG);
        clearFocus();
        updateViewsVisibility(true);
        this.mSearchSrcTextView.setImeOptions(this.mCollapsedImeOptions);
        this.mExpandedInActionView = DBG;
    }

    public void onActionViewExpanded() {
        if (!this.mExpandedInActionView) {
            this.mExpandedInActionView = true;
            this.mCollapsedImeOptions = this.mSearchSrcTextView.getImeOptions();
            this.mSearchSrcTextView.setImeOptions(this.mCollapsedImeOptions | EditorInfo.IME_FLAG_NO_FULLSCREEN);
            this.mSearchSrcTextView.setText((CharSequence) "");
            setIconified(DBG);
        }
    }

    protected Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState(super.onSaveInstanceState());
        ss.isIconified = isIconified();
        return ss;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        updateViewsVisibility(ss.isIconified);
        requestLayout();
    }

    public CharSequence getAccessibilityClassName() {
        return SearchView.class.getName();
    }

    private void adjustDropDownSizeAndPosition() {
        if (this.mDropDownAnchor.getWidth() > 1) {
            int iconOffset;
            int offset;
            Resources res = getContext().getResources();
            int anchorPadding = this.mSearchPlate.getPaddingLeft();
            Rect dropDownPadding = new Rect();
            boolean isLayoutRtl = isRtlLocale();
            if (this.mIconifiedByDefault) {
                iconOffset = res.getDimensionPixelSize(R.dimen.dropdownitem_icon_width) + res.getDimensionPixelSize(R.dimen.dropdownitem_text_padding_left);
            } else {
                iconOffset = 0;
            }
            this.mSearchSrcTextView.getDropDownBackground().getPadding(dropDownPadding);
            if (isLayoutRtl) {
                offset = -dropDownPadding.left;
            } else {
                offset = anchorPadding - (dropDownPadding.left + iconOffset);
            }
            this.mSearchSrcTextView.setDropDownHorizontalOffset(offset);
            this.mSearchSrcTextView.setDropDownWidth((((this.mDropDownAnchor.getWidth() + dropDownPadding.left) + dropDownPadding.right) + iconOffset) - anchorPadding);
        }
    }

    private boolean onItemClicked(int position, int actionKey, String actionMsg) {
        if (this.mOnSuggestionListener != null && this.mOnSuggestionListener.onSuggestionClick(position)) {
            return DBG;
        }
        launchSuggestion(position, 0, null);
        setImeVisibility(DBG);
        dismissSuggestions();
        return true;
    }

    private boolean onItemSelected(int position) {
        if (this.mOnSuggestionListener != null && this.mOnSuggestionListener.onSuggestionSelect(position)) {
            return DBG;
        }
        rewriteQueryFromSuggestion(position);
        return true;
    }

    private void rewriteQueryFromSuggestion(int position) {
        CharSequence oldQuery = this.mSearchSrcTextView.getText();
        Cursor c = this.mSuggestionsAdapter.getCursor();
        if (c != null) {
            if (c.moveToPosition(position)) {
                CharSequence newQuery = this.mSuggestionsAdapter.convertToString(c);
                if (newQuery != null) {
                    setQuery(newQuery);
                } else {
                    setQuery(oldQuery);
                }
            } else {
                setQuery(oldQuery);
            }
        }
    }

    private boolean launchSuggestion(int position, int actionKey, String actionMsg) {
        Cursor c = this.mSuggestionsAdapter.getCursor();
        if (c == null || !c.moveToPosition(position)) {
            return DBG;
        }
        launchIntent(createIntentFromSuggestion(c, actionKey, actionMsg));
        return true;
    }

    private void launchIntent(Intent intent) {
        if (intent != null) {
            try {
                getContext().startActivity(intent);
            } catch (RuntimeException ex) {
                Log.e(LOG_TAG, "Failed launch activity: " + intent, ex);
            }
        }
    }

    private void setQuery(CharSequence query) {
        this.mSearchSrcTextView.setText(query, true);
        this.mSearchSrcTextView.setSelection(TextUtils.isEmpty(query) ? 0 : query.length());
    }

    private void launchQuerySearch(int actionKey, String actionMsg, String query) {
        getContext().startActivity(createIntent("android.intent.action.SEARCH", null, null, query, actionKey, actionMsg));
    }

    private Intent createIntent(String action, Uri data, String extraData, String query, int actionKey, String actionMsg) {
        Intent intent = new Intent(action);
        intent.addFlags(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        if (data != null) {
            intent.setData(data);
        }
        intent.putExtra("user_query", this.mUserQuery);
        if (query != null) {
            intent.putExtra("query", query);
        }
        if (extraData != null) {
            intent.putExtra("intent_extra_data_key", extraData);
        }
        if (this.mAppSearchData != null) {
            intent.putExtra("app_data", this.mAppSearchData);
        }
        if (actionKey != 0) {
            intent.putExtra("action_key", actionKey);
            intent.putExtra("action_msg", actionMsg);
        }
        intent.setComponent(this.mSearchable.getSearchActivity());
        return intent;
    }

    private Intent createVoiceWebSearchIntent(Intent baseIntent, SearchableInfo searchable) {
        String str = null;
        Intent voiceIntent = new Intent(baseIntent);
        ComponentName searchActivity = searchable.getSearchActivity();
        String str2 = "calling_package";
        if (searchActivity != null) {
            str = searchActivity.flattenToShortString();
        }
        voiceIntent.putExtra(str2, str);
        return voiceIntent;
    }

    private Intent createVoiceAppSearchIntent(Intent baseIntent, SearchableInfo searchable) {
        String str;
        ComponentName searchActivity = searchable.getSearchActivity();
        Intent queryIntent = new Intent("android.intent.action.SEARCH");
        queryIntent.setComponent(searchActivity);
        PendingIntent pending = PendingIntent.getActivity(getContext(), 0, queryIntent, EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        Bundle queryExtras = new Bundle();
        if (this.mAppSearchData != null) {
            queryExtras.putParcelable("app_data", this.mAppSearchData);
        }
        Intent voiceIntent = new Intent(baseIntent);
        String languageModel = "free_form";
        String prompt = null;
        String language = null;
        int maxResults = 1;
        Resources resources = getResources();
        if (searchable.getVoiceLanguageModeId() != 0) {
            languageModel = resources.getString(searchable.getVoiceLanguageModeId());
        }
        if (searchable.getVoicePromptTextId() != 0) {
            prompt = resources.getString(searchable.getVoicePromptTextId());
        }
        if (searchable.getVoiceLanguageId() != 0) {
            language = resources.getString(searchable.getVoiceLanguageId());
        }
        if (searchable.getVoiceMaxResults() != 0) {
            maxResults = searchable.getVoiceMaxResults();
        }
        voiceIntent.putExtra("android.speech.extra.LANGUAGE_MODEL", languageModel);
        voiceIntent.putExtra("android.speech.extra.PROMPT", prompt);
        voiceIntent.putExtra("android.speech.extra.LANGUAGE", language);
        voiceIntent.putExtra("android.speech.extra.MAX_RESULTS", maxResults);
        String str2 = "calling_package";
        if (searchActivity == null) {
            str = null;
        } else {
            str = searchActivity.flattenToShortString();
        }
        voiceIntent.putExtra(str2, str);
        voiceIntent.putExtra("android.speech.extra.RESULTS_PENDINGINTENT", pending);
        voiceIntent.putExtra("android.speech.extra.RESULTS_PENDINGINTENT_BUNDLE", queryExtras);
        return voiceIntent;
    }

    private Intent createIntentFromSuggestion(Cursor c, int actionKey, String actionMsg) {
        try {
            String action = SuggestionsAdapter.getColumnString(c, "suggest_intent_action");
            if (action == null) {
                action = this.mSearchable.getSuggestIntentAction();
            }
            if (action == null) {
                action = "android.intent.action.SEARCH";
            }
            String data = SuggestionsAdapter.getColumnString(c, "suggest_intent_data");
            if (data == null) {
                data = this.mSearchable.getSuggestIntentData();
            }
            if (data != null) {
                String id = SuggestionsAdapter.getColumnString(c, "suggest_intent_data_id");
                if (id != null) {
                    data = data + "/" + Uri.encode(id);
                }
            }
            return createIntent(action, data == null ? null : Uri.parse(data), SuggestionsAdapter.getColumnString(c, "suggest_intent_extra_data"), SuggestionsAdapter.getColumnString(c, "suggest_intent_query"), actionKey, actionMsg);
        } catch (RuntimeException e) {
            int rowNum;
            try {
                rowNum = c.getPosition();
            } catch (RuntimeException e2) {
                rowNum = -1;
            }
            Log.w(LOG_TAG, "Search suggestions cursor at row " + rowNum + " returned exception.", e);
            return null;
        }
    }

    private void forceSuggestionQuery() {
        this.mSearchSrcTextView.doBeforeTextChanged();
        this.mSearchSrcTextView.doAfterTextChanged();
    }

    static boolean isLandscapeMode(Context context) {
        return context.getResources().getConfiguration().orientation == 2 ? true : DBG;
    }

    public void inflateSearchView(Context context, AttributeSet attrs, TypedArray a) {
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(a.getResourceId(1, R.layout.search_view), (ViewGroup) this, true);
    }

    public boolean isEmuiStyle() {
        return DBG;
    }

    public boolean isInActionMode() {
        return DBG;
    }

    public void onInActionMode() {
    }

    public void setActionModeEnabled(boolean enabled) {
    }

    public boolean isActionModeEnabled() {
        return DBG;
    }

    public void setQrcodeEnabled(boolean enabled) {
    }

    public void updateQrcodeButton(boolean collapsed) {
    }

    public Drawable getIcon(Context context) {
        return null;
    }

    public void adjustQueryTextView(boolean hasHint) {
    }

    public EditText getSearchSrcTextView() {
        return this.mSearchSrcTextView;
    }

    protected ImageView getGoButton() {
        return this.mGoButton;
    }

    protected View getSubmitArea() {
        return this.mSubmitArea;
    }

    public void setIconsAndBackgrounds(TypedArray a) {
        this.mSearchPlate.setBackground(a.getDrawable(13));
        this.mSubmitArea.setBackground(a.getDrawable(14));
        this.mSearchButton.setImageDrawable(a.getDrawable(9));
        this.mGoButton.setImageDrawable(a.getDrawable(8));
        this.mCloseButton.setImageDrawable(a.getDrawable(7));
        this.mVoiceButton.setImageDrawable(a.getDrawable(10));
        this.mCollapsedIcon.setImageDrawable(a.getDrawable(9));
    }
}
