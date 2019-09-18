package android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.PopupWindow;
import com.android.internal.R;
import java.lang.ref.WeakReference;

public class AutoCompleteTextView extends EditText implements Filter.FilterListener {
    static final boolean DEBUG = false;
    static final int EXPAND_MAX = 3;
    static final String TAG = "AutoCompleteTextView";
    /* access modifiers changed from: private */
    public ListAdapter mAdapter;
    private boolean mBlockCompletion;
    private int mDropDownAnchorId;
    private boolean mDropDownDismissedOnCompletion;
    private Filter mFilter;
    private int mHintResource;
    private CharSequence mHintText;
    private TextView mHintView;
    private AdapterView.OnItemClickListener mItemClickListener;
    private AdapterView.OnItemSelectedListener mItemSelectedListener;
    private int mLastKeyCode;
    private PopupDataSetObserver mObserver;
    private boolean mOpenBefore;
    private final PassThroughClickListener mPassThroughClickListener;
    private final ListPopupWindow mPopup;
    private boolean mPopupCanBeUpdated;
    private final Context mPopupContext;
    private int mThreshold;
    private Validator mValidator;

    private class DropDownItemClickListener implements AdapterView.OnItemClickListener {
        private DropDownItemClickListener() {
        }

        public void onItemClick(AdapterView parent, View v, int position, long id) {
            AutoCompleteTextView.this.performCompletion(v, position, id);
        }
    }

    private class MyWatcher implements TextWatcher {
        private MyWatcher() {
        }

        public void afterTextChanged(Editable s) {
            AutoCompleteTextView.this.doAfterTextChanged();
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            AutoCompleteTextView.this.doBeforeTextChanged();
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    private class PassThroughClickListener implements View.OnClickListener {
        /* access modifiers changed from: private */
        public View.OnClickListener mWrapped;

        private PassThroughClickListener() {
        }

        public void onClick(View v) {
            AutoCompleteTextView.this.onClickImpl();
            if (this.mWrapped != null) {
                this.mWrapped.onClick(v);
            }
        }
    }

    private static class PopupDataSetObserver extends DataSetObserver {
        /* access modifiers changed from: private */
        public final WeakReference<AutoCompleteTextView> mViewReference;
        private final Runnable updateRunnable;

        private PopupDataSetObserver(AutoCompleteTextView view) {
            this.updateRunnable = new Runnable() {
                public void run() {
                    AutoCompleteTextView textView = (AutoCompleteTextView) PopupDataSetObserver.this.mViewReference.get();
                    if (textView != null) {
                        ListAdapter adapter = textView.mAdapter;
                        if (adapter != null) {
                            textView.updateDropDownForFilter(adapter.getCount());
                        }
                    }
                }
            };
            this.mViewReference = new WeakReference<>(view);
        }

        public void onChanged() {
            AutoCompleteTextView textView = (AutoCompleteTextView) this.mViewReference.get();
            if (textView != null && textView.mAdapter != null) {
                textView.post(this.updateRunnable);
            }
        }
    }

    public interface Validator {
        CharSequence fixText(CharSequence charSequence);

        boolean isValid(CharSequence charSequence);
    }

    public AutoCompleteTextView(Context context) {
        this(context, null);
    }

    public AutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842859);
    }

    public AutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr, defStyleRes, null);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public AutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, Resources.Theme popupTheme) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray pa;
        Context context2 = context;
        AttributeSet attributeSet = attrs;
        int i = defStyleAttr;
        int i2 = defStyleRes;
        Resources.Theme theme = popupTheme;
        this.mDropDownDismissedOnCompletion = true;
        this.mLastKeyCode = 0;
        this.mValidator = null;
        this.mPopupCanBeUpdated = true;
        TypedArray a = context2.obtainStyledAttributes(attributeSet, R.styleable.AutoCompleteTextView, i, i2);
        if (theme != null) {
            this.mPopupContext = new ContextThemeWrapper(context2, theme);
        } else {
            int popupThemeResId = a.getResourceId(8, 0);
            if (popupThemeResId != 0) {
                this.mPopupContext = new ContextThemeWrapper(context2, popupThemeResId);
            } else {
                this.mPopupContext = context2;
            }
        }
        if (this.mPopupContext != context2) {
            pa = this.mPopupContext.obtainStyledAttributes(attributeSet, R.styleable.AutoCompleteTextView, i, i2);
        } else {
            pa = a;
        }
        Drawable popupListSelector = pa.getDrawable(3);
        int popupWidth = pa.getLayoutDimension(5, -2);
        int popupHeight = pa.getLayoutDimension(7, -2);
        int popupHintLayoutResId = pa.getResourceId(1, 17367283);
        CharSequence popupHintText = pa.getText(0);
        if (pa != a) {
            pa.recycle();
        }
        this.mPopup = new ListPopupWindow(this.mPopupContext, attributeSet, i, i2);
        this.mPopup.setSoftInputMode(16);
        this.mPopup.setPromptPosition(1);
        this.mPopup.setListSelector(popupListSelector);
        this.mPopup.setOnItemClickListener(new DropDownItemClickListener());
        this.mPopup.setWidth(popupWidth);
        this.mPopup.setHeight(popupHeight);
        this.mHintResource = popupHintLayoutResId;
        setCompletionHint(popupHintText);
        this.mDropDownAnchorId = a.getResourceId(6, -1);
        this.mThreshold = a.getInt(2, 2);
        a.recycle();
        int inputType = getInputType();
        if ((inputType & 15) == 1) {
            setRawInputType(inputType | 65536);
        }
        setFocusable(true);
        addTextChangedListener(new MyWatcher());
        this.mPassThroughClickListener = new PassThroughClickListener();
        super.setOnClickListener(this.mPassThroughClickListener);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        View.OnClickListener unused = this.mPassThroughClickListener.mWrapped = listener;
    }

    /* access modifiers changed from: private */
    public void onClickImpl() {
        if (isPopupShowing()) {
            ensureImeVisible(true);
        }
    }

    public void setCompletionHint(CharSequence hint) {
        this.mHintText = hint;
        if (hint == null) {
            this.mPopup.setPromptView(null);
            this.mHintView = null;
        } else if (this.mHintView == null) {
            TextView hintView = (TextView) LayoutInflater.from(this.mPopupContext).inflate(this.mHintResource, (ViewGroup) null).findViewById(16908308);
            hintView.setText(this.mHintText);
            this.mHintView = hintView;
            this.mPopup.setPromptView(hintView);
        } else {
            this.mHintView.setText(hint);
        }
    }

    public CharSequence getCompletionHint() {
        return this.mHintText;
    }

    public int getDropDownWidth() {
        return this.mPopup.getWidth();
    }

    public void setDropDownWidth(int width) {
        this.mPopup.setWidth(width);
    }

    public int getDropDownHeight() {
        return this.mPopup.getHeight();
    }

    public void setDropDownHeight(int height) {
        this.mPopup.setHeight(height);
    }

    public int getDropDownAnchor() {
        return this.mDropDownAnchorId;
    }

    public void setDropDownAnchor(int id) {
        this.mDropDownAnchorId = id;
        this.mPopup.setAnchorView(null);
    }

    public Drawable getDropDownBackground() {
        return this.mPopup.getBackground();
    }

    public void setDropDownBackgroundDrawable(Drawable d) {
        this.mPopup.setBackgroundDrawable(d);
    }

    public void setDropDownBackgroundResource(int id) {
        this.mPopup.setBackgroundDrawable(getContext().getDrawable(id));
    }

    public void setDropDownVerticalOffset(int offset) {
        this.mPopup.setVerticalOffset(offset);
    }

    public int getDropDownVerticalOffset() {
        return this.mPopup.getVerticalOffset();
    }

    public void setDropDownHorizontalOffset(int offset) {
        this.mPopup.setHorizontalOffset(offset);
    }

    public int getDropDownHorizontalOffset() {
        return this.mPopup.getHorizontalOffset();
    }

    public void setDropDownAnimationStyle(int animationStyle) {
        this.mPopup.setAnimationStyle(animationStyle);
    }

    public int getDropDownAnimationStyle() {
        return this.mPopup.getAnimationStyle();
    }

    public boolean isDropDownAlwaysVisible() {
        return this.mPopup.isDropDownAlwaysVisible();
    }

    public void setDropDownAlwaysVisible(boolean dropDownAlwaysVisible) {
        this.mPopup.setDropDownAlwaysVisible(dropDownAlwaysVisible);
    }

    public boolean isDropDownDismissedOnCompletion() {
        return this.mDropDownDismissedOnCompletion;
    }

    public void setDropDownDismissedOnCompletion(boolean dropDownDismissedOnCompletion) {
        this.mDropDownDismissedOnCompletion = dropDownDismissedOnCompletion;
    }

    public int getThreshold() {
        return this.mThreshold;
    }

    public void setThreshold(int threshold) {
        if (threshold <= 0) {
            threshold = 1;
        }
        this.mThreshold = threshold;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener l) {
        this.mItemClickListener = l;
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener l) {
        this.mItemSelectedListener = l;
    }

    @Deprecated
    public AdapterView.OnItemClickListener getItemClickListener() {
        return this.mItemClickListener;
    }

    @Deprecated
    public AdapterView.OnItemSelectedListener getItemSelectedListener() {
        return this.mItemSelectedListener;
    }

    public AdapterView.OnItemClickListener getOnItemClickListener() {
        return this.mItemClickListener;
    }

    public AdapterView.OnItemSelectedListener getOnItemSelectedListener() {
        return this.mItemSelectedListener;
    }

    public void setOnDismissListener(final OnDismissListener dismissListener) {
        PopupWindow.OnDismissListener wrappedListener = null;
        if (dismissListener != null) {
            wrappedListener = new PopupWindow.OnDismissListener() {
                public void onDismiss() {
                    dismissListener.onDismiss();
                }
            };
        }
        this.mPopup.setOnDismissListener(wrappedListener);
    }

    public ListAdapter getAdapter() {
        return this.mAdapter;
    }

    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        if (this.mObserver == null) {
            this.mObserver = new PopupDataSetObserver();
        } else if (this.mAdapter != null) {
            this.mAdapter.unregisterDataSetObserver(this.mObserver);
        }
        this.mAdapter = adapter;
        if (this.mAdapter != null) {
            this.mFilter = ((Filterable) this.mAdapter).getFilter();
            adapter.registerDataSetObserver(this.mObserver);
        } else {
            this.mFilter = null;
        }
        this.mPopup.setAdapter(this.mAdapter);
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == 4 && isPopupShowing() && !this.mPopup.isDropDownAlwaysVisible()) {
            if (event.getAction() == 0 && event.getRepeatCount() == 0) {
                KeyEvent.DispatcherState state = getKeyDispatcherState();
                if (state != null) {
                    state.startTracking(event, this);
                }
                return true;
            } else if (event.getAction() == 1) {
                KeyEvent.DispatcherState state2 = getKeyDispatcherState();
                if (state2 != null) {
                    state2.handleUpEvent(event);
                }
                if (event.isTracking() && !event.isCanceled()) {
                    dismissDropDown();
                    return true;
                }
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (this.mPopup.onKeyUp(keyCode, event) && (keyCode == 23 || keyCode == 61 || keyCode == 66)) {
            if (event.hasNoModifiers()) {
                performCompletion();
            }
            return true;
        } else if (!isPopupShowing() || keyCode != 61 || !event.hasNoModifiers()) {
            return super.onKeyUp(keyCode, event);
        } else {
            performCompletion();
            return true;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (this.mPopup.onKeyDown(keyCode, event)) {
            return true;
        }
        if (!isPopupShowing() && keyCode == 20 && event.hasNoModifiers()) {
            performValidation();
        }
        if (isPopupShowing() && keyCode == 61 && event.hasNoModifiers()) {
            return true;
        }
        this.mLastKeyCode = keyCode;
        boolean handled = super.onKeyDown(keyCode, event);
        this.mLastKeyCode = 0;
        if (handled && isPopupShowing()) {
            clearListSelection();
        }
        return handled;
    }

    public boolean enoughToFilter() {
        return getText().length() >= this.mThreshold;
    }

    /* access modifiers changed from: package-private */
    public void doBeforeTextChanged() {
        if (!this.mBlockCompletion) {
            this.mOpenBefore = isPopupShowing();
        }
    }

    /* access modifiers changed from: package-private */
    public void doAfterTextChanged() {
        if (!this.mBlockCompletion) {
            if (!this.mOpenBefore || isPopupShowing()) {
                if (!enoughToFilter()) {
                    if (!this.mPopup.isDropDownAlwaysVisible()) {
                        dismissDropDown();
                    }
                    if (this.mFilter != null) {
                        this.mFilter.filter(null);
                    }
                } else if (this.mFilter != null) {
                    this.mPopupCanBeUpdated = true;
                    performFiltering(getText(), this.mLastKeyCode);
                }
            }
        }
    }

    public boolean isPopupShowing() {
        return this.mPopup.isShowing();
    }

    /* access modifiers changed from: protected */
    public CharSequence convertSelectionToString(Object selectedItem) {
        return this.mFilter.convertResultToString(selectedItem);
    }

    public void clearListSelection() {
        this.mPopup.clearListSelection();
    }

    public void setListSelection(int position) {
        this.mPopup.setSelection(position);
    }

    public int getListSelection() {
        return this.mPopup.getSelectedItemPosition();
    }

    /* access modifiers changed from: protected */
    public void performFiltering(CharSequence text, int keyCode) {
        this.mFilter.filter(text, this);
    }

    public void performCompletion() {
        performCompletion(null, -1, -1);
    }

    public void onCommitCompletion(CompletionInfo completion) {
        if (isPopupShowing()) {
            this.mPopup.performItemClick(completion.getPosition());
        }
    }

    /* access modifiers changed from: private */
    public void performCompletion(View selectedView, int position, long id) {
        Object selectedItem;
        if (isPopupShowing()) {
            if (position < 0) {
                selectedItem = this.mPopup.getSelectedItem();
            } else {
                selectedItem = this.mAdapter.getItem(position);
            }
            if (selectedItem == null) {
                Log.w(TAG, "performCompletion: no selected item");
                return;
            }
            this.mBlockCompletion = true;
            replaceText(convertSelectionToString(selectedItem));
            this.mBlockCompletion = false;
            if (this.mItemClickListener != null) {
                ListPopupWindow list = this.mPopup;
                if (selectedView == null || position < 0) {
                    selectedView = list.getSelectedView();
                    position = list.getSelectedItemPosition();
                    id = list.getSelectedItemId();
                }
                this.mItemClickListener.onItemClick(list.getListView(), selectedView, position, id);
            }
        }
        if (this.mDropDownDismissedOnCompletion && !this.mPopup.isDropDownAlwaysVisible()) {
            dismissDropDown();
        }
    }

    public boolean isPerformingCompletion() {
        return this.mBlockCompletion;
    }

    public void setText(CharSequence text, boolean filter) {
        if (filter) {
            setText(text);
            return;
        }
        this.mBlockCompletion = true;
        setText(text);
        this.mBlockCompletion = false;
    }

    /* access modifiers changed from: protected */
    public void replaceText(CharSequence text) {
        clearComposingText();
        setText(text);
        Editable spannable = getText();
        Selection.setSelection(spannable, spannable.length());
    }

    public void onFilterComplete(int count) {
        updateDropDownForFilter(count);
    }

    /* access modifiers changed from: private */
    public void updateDropDownForFilter(int count) {
        if (getWindowVisibility() != 8) {
            boolean dropDownAlwaysVisible = this.mPopup.isDropDownAlwaysVisible();
            boolean enoughToFilter = enoughToFilter();
            if ((count > 0 || dropDownAlwaysVisible) && enoughToFilter) {
                if (hasFocus() && hasWindowFocus() && this.mPopupCanBeUpdated) {
                    showDropDown();
                }
            } else if (!dropDownAlwaysVisible && isPopupShowing()) {
                dismissDropDown();
                this.mPopupCanBeUpdated = true;
            }
        }
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus && !this.mPopup.isDropDownAlwaysVisible()) {
            dismissDropDown();
        }
    }

    /* access modifiers changed from: protected */
    public void onDisplayHint(int hint) {
        super.onDisplayHint(hint);
        if (hint == 4 && !this.mPopup.isDropDownAlwaysVisible()) {
            dismissDropDown();
        }
    }

    /* access modifiers changed from: protected */
    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (!isTemporarilyDetached()) {
            if (!focused) {
                performValidation();
            }
            if (!focused && !this.mPopup.isDropDownAlwaysVisible()) {
                dismissDropDown();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        dismissDropDown();
        super.onDetachedFromWindow();
    }

    public void dismissDropDown() {
        InputMethodManager imm = InputMethodManager.peekInstance();
        if (imm != null) {
            imm.displayCompletions(this, null);
        }
        this.mPopup.dismiss();
        this.mPopupCanBeUpdated = false;
    }

    /* access modifiers changed from: protected */
    public boolean setFrame(int l, int t, int r, int b) {
        boolean result = super.setFrame(l, t, r, b);
        if (isPopupShowing()) {
            showDropDown();
        }
        return result;
    }

    public void showDropDownAfterLayout() {
        this.mPopup.postShow();
    }

    public void ensureImeVisible(boolean visible) {
        this.mPopup.setInputMethodMode(visible ? 1 : 2);
        if (this.mPopup.isDropDownAlwaysVisible() || (this.mFilter != null && enoughToFilter())) {
            showDropDown();
        }
    }

    public boolean isInputMethodNotNeeded() {
        return this.mPopup.getInputMethodMode() == 2;
    }

    public void showDropDown() {
        buildImeCompletions();
        if (this.mPopup.getAnchorView() == null) {
            if (this.mDropDownAnchorId != -1) {
                this.mPopup.setAnchorView(getRootView().findViewById(this.mDropDownAnchorId));
            } else {
                this.mPopup.setAnchorView(this);
            }
        }
        if (!isPopupShowing()) {
            this.mPopup.setInputMethodMode(1);
            this.mPopup.setListItemExpandMax(3);
        }
        this.mPopup.show();
        this.mPopup.getListView().setOverScrollMode(0);
    }

    public void setForceIgnoreOutsideTouch(boolean forceIgnoreOutsideTouch) {
        this.mPopup.setForceIgnoreOutsideTouch(forceIgnoreOutsideTouch);
    }

    private void buildImeCompletions() {
        ListAdapter adapter = this.mAdapter;
        if (adapter != null) {
            InputMethodManager imm = InputMethodManager.peekInstance();
            if (imm != null) {
                int count = Math.min(adapter.getCount(), 20);
                CompletionInfo[] completions = new CompletionInfo[count];
                int realCount = 0;
                for (int i = 0; i < count; i++) {
                    if (adapter.isEnabled(i)) {
                        completions[realCount] = new CompletionInfo(adapter.getItemId(i), realCount, convertSelectionToString(adapter.getItem(i)));
                        realCount++;
                    }
                }
                if (realCount != count) {
                    CompletionInfo[] tmp = new CompletionInfo[realCount];
                    System.arraycopy(completions, 0, tmp, 0, realCount);
                    completions = tmp;
                }
                imm.displayCompletions(this, completions);
            }
        }
    }

    public void setValidator(Validator validator) {
        this.mValidator = validator;
    }

    public Validator getValidator() {
        return this.mValidator;
    }

    public void performValidation() {
        if (this.mValidator != null) {
            CharSequence text = getText();
            if (!TextUtils.isEmpty(text) && !this.mValidator.isValid(text)) {
                setText(this.mValidator.fixText(text));
            }
        }
    }

    /* access modifiers changed from: protected */
    public Filter getFilter() {
        return this.mFilter;
    }
}
