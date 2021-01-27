package huawei.android.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.BadParcelableException;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SearchView;
import androidhwext.R;
import com.huawei.anim.dynamicanimation.DynamicAnimation;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;
import java.util.Locale;

public class SearchView extends android.widget.SearchView {
    public static final int QUERY_TEXT_VIEW_GAP = 3;
    private static final String TAG = "HwSearchView";
    private Drawable mBackGroundDrawable;
    private View mBarcodeButton;
    private ImageView mCancelButton;
    private Drawable mCancelButtonDrawable;
    private final View.OnClickListener mCancelOnClickListener;
    private Drawable mFocusedDrawable;
    private HwSearchAutoComplete mHwSearchSrcTextView;
    private ImageView mHwVoiceButton;
    private boolean mIsActionModeEnabled;
    private boolean mIsBarcodeEnabled;
    private boolean mIsInActionMode;
    private final View.OnClickListener mOnClickListener;
    private ResLoader mResLoader;
    private int mSearchViewCornerRadius;
    private int mSearchviewTextMarginEnd;
    private int mSearchviewTextPadding;
    private final View.OnClickListener mSuperOnClickListener;

    public SearchView(Context context) {
        this(context, null);
    }

    public SearchView(Context context, AttributeSet attrs) {
        this(context, attrs, 16843904);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SearchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mOnClickListener = new View.OnClickListener() {
            /* class huawei.android.widget.SearchView.AnonymousClass1 */

            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                if (v == SearchView.this.mBarcodeButton) {
                    SearchView.this.onBarcodeClicked();
                }
            }
        };
        this.mSuperOnClickListener = getSuperOnClickListener(this, "mOnClickListener", android.widget.SearchView.class);
        this.mCancelOnClickListener = new View.OnClickListener() {
            /* class huawei.android.widget.SearchView.AnonymousClass2 */

            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (SearchView.this.mHwSearchSrcTextView != null) {
                    if (!TextUtils.isEmpty(SearchView.this.mHwSearchSrcTextView.getText())) {
                        SearchView.this.mHwSearchSrcTextView.setText("");
                        SearchView.this.mHwSearchSrcTextView.requestFocus();
                    } else if (SearchView.this.mSuperOnClickListener != null) {
                        SearchView.this.mSuperOnClickListener.onClick(view);
                    }
                }
            }
        };
        this.mResLoader = ResLoader.getInstance();
        Resources res = this.mResLoader.getResources(context);
        this.mSearchviewTextMarginEnd = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, ResLoaderUtil.DIMEN, "searchview_src_text_padding_end"));
        this.mSearchviewTextPadding = res.getDimensionPixelSize(34472219);
        this.mSearchViewCornerRadius = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, ResLoaderUtil.DIMEN, "emui_corner_radius_inputbox"));
        reflectMember();
        updateSearchTextViewMargin();
        initialBarcodeButton(context);
        HwWidgetUtils.getHwAnimatedGradientDrawable(context, defStyleAttr).setCornerRadius((float) this.mSearchViewCornerRadius);
        View searchPlate = findViewById(16909365);
        HwSearchAutoComplete hwSearchAutoComplete = this.mHwSearchSrcTextView;
        if (!(hwSearchAutoComplete == null || searchPlate == null)) {
            hwSearchAutoComplete.setSearchPlateView(searchPlate);
            this.mHwSearchSrcTextView.setHapticFeedbackEnabled(isHapticFeedbackEnabled());
        }
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SearchView, defStyleAttr, 0);
        this.mFocusedDrawable = typedArray.getDrawable(5);
        typedArray.recycle();
        HwSearchAutoComplete hwSearchAutoComplete2 = this.mHwSearchSrcTextView;
        if (hwSearchAutoComplete2 != null) {
            hwSearchAutoComplete2.setFocusedDrawable(this.mFocusedDrawable);
        }
        ImageView imageView = this.mCancelButton;
        if (imageView != null) {
            imageView.setOnClickListener(this.mCancelOnClickListener);
        }
    }

    private void initialBarcodeButton(Context context) {
        this.mBarcodeButton = findViewById(34603107);
        View view = this.mBarcodeButton;
        if (view != null) {
            view.setOnClickListener(this.mOnClickListener);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onBarcodeClicked() {
    }

    public boolean isEmuiStyle() {
        return true;
    }

    public boolean isInActionMode() {
        return this.mIsInActionMode;
    }

    public void onInActionMode() {
        if (!this.mIsInActionMode) {
            this.mIsInActionMode = true;
            this.mIsActionModeEnabled = true;
            setSubmitButtonEnabled(true);
        }
    }

    public boolean isActionModeEnabled() {
        return this.mIsActionModeEnabled;
    }

    public void setActionModeEnabled(boolean isEnabled) {
        HwSearchAutoComplete hwSearchAutoComplete = this.mHwSearchSrcTextView;
        if (hwSearchAutoComplete == null) {
            Log.w(TAG, "mHwSearchSrcTextView is null on setActionModeEnabled");
            return;
        }
        this.mIsActionModeEnabled = isEnabled;
        hwSearchAutoComplete.setClickable(!isEnabled);
        this.mHwSearchSrcTextView.setFocusable(!isEnabled);
        this.mHwSearchSrcTextView.setShowSoftInputOnFocus(!isEnabled);
        setClickable(!isEnabled);
        updateViewsVisibility(isIconified());
    }

    public void setQrcodeEnabled(boolean isEnabled) {
        this.mIsBarcodeEnabled = isEnabled;
        updateViewsVisibility(isIconified());
    }

    private boolean canShowSubmitButton() {
        return false;
    }

    private boolean isSubmitAreaEnabled() {
        return isSubmitButtonEnabled() && !isIconified();
    }

    public void updateQrcodeButton(boolean isCollapsed) {
        View view = this.mBarcodeButton;
        if (view != null) {
            view.setVisibility((!this.mIsBarcodeEnabled || isIconfiedByDefault()) ? 8 : 0);
        }
    }

    public Drawable getIcon(Context context) {
        return context.getResources().getDrawable(33751442);
    }

    public void adjustQueryTextView(boolean isHint) {
        HwSearchAutoComplete hwSearchAutoComplete = this.mHwSearchSrcTextView;
        if (hwSearchAutoComplete == null) {
            Log.w(TAG, "mHwSearchSrcTextView is null on setActionModeEnabled");
            return;
        }
        int left = hwSearchAutoComplete.getPaddingLeft();
        int top = this.mHwSearchSrcTextView.getPaddingTop();
        int right = this.mHwSearchSrcTextView.getPaddingRight();
        int bottom = this.mHwSearchSrcTextView.getPaddingBottom();
        int gap = (int) (getResources().getDisplayMetrics().density * 3.0f);
        if (isHint && bottom <= 0) {
            bottom += gap;
        } else if (!isHint && bottom > 0) {
            bottom -= gap;
        }
        this.mHwSearchSrcTextView.setPadding(left, top, right, bottom);
    }

    public void updateViewsVisibility(boolean isCollapsed) {
        ReflectUtil.callMethod(this, "updateViewsVisibility", new Class[]{Boolean.TYPE}, new Object[]{Boolean.valueOf(isCollapsed)}, android.widget.SearchView.class);
        updateQrcodeButton(isCollapsed);
    }

    @Override // android.widget.SearchView
    public void setQuery(CharSequence query, boolean isSubmit) {
        HwSearchAutoComplete hwSearchAutoComplete = this.mHwSearchSrcTextView;
        if (hwSearchAutoComplete == null) {
            Log.w(TAG, "mHwSearchSrcTextView is null");
            return;
        }
        hwSearchAutoComplete.setText(query);
        if (query != null) {
            HwSearchAutoComplete hwSearchAutoComplete2 = this.mHwSearchSrcTextView;
            hwSearchAutoComplete2.setSelection(hwSearchAutoComplete2.length());
            ReflectUtil.setObject("mUserQuery", this, query, android.widget.SearchView.class);
        }
        if (!this.mIsInActionMode && isSubmit && !TextUtils.isEmpty(query)) {
            ReflectUtil.callMethod(this, "onSubmitQuery", null, null, android.widget.SearchView.class);
        }
    }

    private void reflectMember() {
        Object searchSrcTextView = ReflectUtil.getObject(this, "mSearchSrcTextView", android.widget.SearchView.class);
        if (searchSrcTextView instanceof HwSearchAutoComplete) {
            this.mHwSearchSrcTextView = (HwSearchAutoComplete) searchSrcTextView;
            this.mHwSearchSrcTextView.setSearchView(this);
        }
        Object obj = ReflectUtil.getObject(this, "mVoiceButton", android.widget.SearchView.class);
        if (obj instanceof ImageView) {
            this.mHwVoiceButton = (ImageView) obj;
        }
        Object cancelBtn = ReflectUtil.getObject(this, "mCloseButton", android.widget.SearchView.class);
        if (cancelBtn instanceof ImageView) {
            this.mCancelButton = (ImageView) cancelBtn;
        }
    }

    private void updateSearchTextViewMargin() {
        HwSearchAutoComplete hwSearchAutoComplete;
        if ("iw".equals(Locale.getDefault().getLanguage()) && (hwSearchAutoComplete = this.mHwSearchSrcTextView) != null) {
            hwSearchAutoComplete.addTextChangedListener(new TextWatcher() {
                /* class huawei.android.widget.SearchView.AnonymousClass3 */

                @Override // android.text.TextWatcher
                public void onTextChanged(CharSequence sequence, int start, int before, int count) {
                }

                @Override // android.text.TextWatcher
                public void beforeTextChanged(CharSequence sequence, int start, int count, int after) {
                }

                @Override // android.text.TextWatcher
                public void afterTextChanged(Editable editable) {
                    int mHwVoiceButtonVisility = 0;
                    if (SearchView.this.mHwVoiceButton != null) {
                        mHwVoiceButtonVisility = SearchView.this.mHwVoiceButton.getVisibility();
                    }
                    if (!TextUtils.isEmpty(editable) || mHwVoiceButtonVisility != 8) {
                        SearchView.this.mHwSearchSrcTextView.setPadding(0, 0, SearchView.this.mSearchviewTextPadding, 0);
                    } else {
                        SearchView.this.mHwSearchSrcTextView.setPadding(SearchView.this.mSearchviewTextMarginEnd, 0, SearchView.this.mSearchviewTextPadding, 0);
                    }
                }
            });
        }
    }

    /* JADX WARN: Type inference failed for: r0v0, types: [huawei.android.widget.SearchView$HwSearchAutoComplete, android.widget.EditText] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public EditText getSearchSrcTextView() {
        return this.mHwSearchSrcTextView;
    }

    private View.OnClickListener getSuperOnClickListener(Object instance, String reflectName, Class<?> clazz) {
        Object object = ReflectUtil.getObject(instance, reflectName, clazz);
        if (object instanceof View.OnClickListener) {
            return (View.OnClickListener) object;
        }
        return null;
    }

    public static class HwSearchAutoComplete extends SearchView.SearchAutoComplete {
        private static final int ALT_SHIFT_STATUS = 4;
        private static final String CURSOR_STATE = "CursorState";
        private static final int EDIT_STATUS = 2;
        private static final int FOCUSED_STATUS = 1;
        private static final String INSTANCE_STATE = "InstanceState";
        private static final int KEYCODE_INPUTMETHOD_ACTION = 746;
        private static final int TOUCH_STATUS = 3;
        private static final int UNFOCUSED_STATUS = 0;
        private static final String VIEW_STATE = "ViewState";
        private Drawable mFocusedDrawable;
        private boolean mIsOldViewGainFocus;
        private boolean mIsOldWindowGainFocus;
        private boolean mIsViewFocused = false;
        private View mSearchPlateView;
        private SearchView mSearchView;
        private int mStatus = 0;

        public HwSearchAutoComplete(Context context) {
            super(context);
        }

        public HwSearchAutoComplete(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public HwSearchAutoComplete(Context context, AttributeSet attrs, int defStyleAttrs) {
            super(context, attrs, defStyleAttrs);
        }

        public HwSearchAutoComplete(Context context, AttributeSet attrs, int defStyleAttrs, int defStyleRes) {
            super(context, attrs, defStyleAttrs, defStyleRes);
        }

        /* access modifiers changed from: package-private */
        public void setSearchView(SearchView searchView) {
            this.mSearchView = searchView;
        }

        public boolean enoughToFilter() {
            SearchView searchView = this.mSearchView;
            if (searchView == null || !searchView.isInActionMode() || !this.mSearchView.isSubmitButtonEnabled()) {
                return SearchView.super.enoughToFilter();
            }
            return false;
        }

        /* access modifiers changed from: package-private */
        public void setSearchPlateView(View searchPlateView) {
            this.mSearchPlateView = searchPlateView;
        }

        /* access modifiers changed from: protected */
        public void drawableStateChanged() {
            Drawable background;
            SearchView.super.drawableStateChanged();
            View view = this.mSearchPlateView;
            if (view != null && (background = view.getBackground()) != null && background.isStateful()) {
                background.setState(getDrawableState());
            }
        }

        /* access modifiers changed from: protected */
        public void onDraw(Canvas canvas) {
            SearchView.super.onDraw(canvas);
            if (this.mStatus == 1 && this.mIsViewFocused) {
                drawFocusLayer(canvas);
            }
        }

        /* access modifiers changed from: protected */
        public void onAttachedToWindow() {
            SearchView.super.onAttachedToWindow();
            this.mIsOldViewGainFocus = hasFocus();
            this.mIsOldWindowGainFocus = hasWindowFocus();
            this.mIsViewFocused = this.mIsOldViewGainFocus && this.mIsOldWindowGainFocus;
        }

        /* access modifiers changed from: protected */
        public void onFocusChanged(boolean hasGainFocus, int direction, Rect previouslyFocusedRect) {
            SearchView.super.onFocusChanged(hasGainFocus, direction, previouslyFocusedRect);
            if (isFocusChanged(hasGainFocus, this.mIsOldWindowGainFocus)) {
                this.mIsViewFocused = hasGainFocus;
            }
            this.mIsOldViewGainFocus = hasGainFocus;
            if (hasGainFocus && this.mStatus == 0) {
                if (!isInTouchMode()) {
                    this.mStatus = 1;
                    setCursorVisible(false);
                } else {
                    this.mStatus = 3;
                    setCursorVisible(true);
                }
            }
            if (!hasGainFocus) {
                if (this.mStatus != 3) {
                    hideSoftInput();
                }
                this.mStatus = 0;
            }
        }

        public void onWindowFocusChanged(boolean hasWindowFocus) {
            SearchView.super.onWindowFocusChanged(hasWindowFocus);
            if (isFocusChanged(this.mIsOldViewGainFocus, hasWindowFocus)) {
                this.mIsViewFocused = hasWindowFocus;
            }
            this.mIsOldWindowGainFocus = hasWindowFocus;
        }

        private boolean isFocusChanged(boolean isNewViewGainFocus, boolean isNewWindowGainFocus) {
            return (this.mIsOldViewGainFocus && this.mIsOldWindowGainFocus) != (isNewViewGainFocus && isNewWindowGainFocus);
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (event == null) {
                return false;
            }
            if (event.getAction() == 0) {
                setCursorVisible(true);
                this.mStatus = 3;
            }
            return SearchView.super.onTouchEvent(event);
        }

        private void drawFocusLayer(Canvas canvas) {
            ViewParent parent = getParent();
            if (parent instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parent;
                Rect rect = new Rect(0, 0, viewGroup.getWidth(), viewGroup.getHeight());
                rect.offset(getScrollX() - getLeft(), getScrollY() - getTop());
                Drawable drawable = this.mFocusedDrawable;
                if (drawable != null) {
                    drawable.setBounds(rect);
                    this.mFocusedDrawable.draw(canvas);
                }
            }
        }

        private boolean isConfirmKey(int keyCode) {
            if (keyCode == 23 || keyCode == 62 || keyCode == 66 || keyCode == 160) {
                return true;
            }
            return false;
        }

        private boolean isDirectionalNavigationKey(int keyCode) {
            switch (keyCode) {
                case 19:
                case 20:
                case DynamicAnimation.ANDROID_LOLLIPOP /* 21 */:
                case 22:
                    return true;
                default:
                    return false;
            }
        }

        private boolean isAltKey(int keyCode) {
            return keyCode == 57 || keyCode == 58;
        }

        private boolean isShiftKey(int keyCode) {
            return keyCode == 59 || keyCode == 60;
        }

        private void handleAltShiftKeyEvent(int keyCode, KeyEvent event) {
            if (this.mStatus == 4 && event.getAction() == 1 && isAltKey(keyCode)) {
                this.mStatus = 3;
            } else if (isShiftKey(keyCode) && (event.getMetaState() & 2) != 0) {
                this.mStatus = 4;
            }
        }

        private void hideSoftInput() {
            Object object = getContext().getSystemService("input_method");
            if (object instanceof InputMethodManager) {
                InputMethodManager manager = (InputMethodManager) object;
                if (manager.isActive()) {
                    manager.hideSoftInputFromWindow(getWindowToken(), 0);
                }
            }
        }

        /* JADX DEBUG: Multi-variable search result rejected for r6v0, resolved type: huawei.android.widget.SearchView$HwSearchAutoComplete */
        /* JADX WARN: Multi-variable type inference failed */
        private void handleTabKeyEvent(KeyEvent event) {
            View view = null;
            boolean isShiftOn = (event.getMetaState() & 1) != 0;
            FocusFinder focusFinder = FocusFinder.getInstance();
            View rootView = getRootView();
            if (rootView instanceof ViewGroup) {
                if (isShiftOn) {
                    view = focusFinder.findNextFocus((ViewGroup) rootView, this, 1);
                } else {
                    view = focusFinder.findNextFocus((ViewGroup) rootView, this, 2);
                }
            }
            if (view != null) {
                view.requestFocus();
            }
        }

        public boolean onKeyPreIme(int keyCode, KeyEvent event) {
            if (event == null) {
                return false;
            }
            if (keyCode == 4 || keyCode == 3) {
                return SearchView.super.onKeyPreIme(keyCode, event);
            }
            if (this.mStatus == 3 && keyCode != KEYCODE_INPUTMETHOD_ACTION) {
                this.mStatus = 2;
                hideSoftInput();
            }
            if (this.mStatus != 1) {
                handleAltShiftKeyEvent(keyCode, event);
                if (this.mStatus != 2 || keyCode != 111) {
                    return SearchView.super.onKeyPreIme(keyCode, event);
                }
                this.mStatus = 1;
                setCursorVisible(false);
                return true;
            } else if (isDirectionalNavigationKey(keyCode)) {
                return false;
            } else {
                if (keyCode == 61 && event.getAction() == 0) {
                    handleTabKeyEvent(event);
                }
                if (isConfirmKey(keyCode) && event.getAction() == 1) {
                    this.mStatus = 2;
                    setCursorVisible(true);
                }
                return true;
            }
        }

        public boolean dispatchKeyEvent(KeyEvent event) {
            if (event == null) {
                return false;
            }
            if (this.mStatus != 1 || !isDirectionalNavigationKey(event.getKeyCode())) {
                return SearchView.super.dispatchKeyEvent(event);
            }
            return false;
        }

        public Parcelable onSaveInstanceState() {
            Bundle bundle = new Bundle();
            try {
                bundle.putParcelable(INSTANCE_STATE, SearchView.super.onSaveInstanceState());
                bundle.putInt(VIEW_STATE, this.mStatus);
                bundle.putBoolean(CURSOR_STATE, isCursorVisible());
            } catch (BadParcelableException e) {
                Log.e(SearchView.TAG, "Parcelable, onSaveInstanceState error");
            }
            return bundle;
        }

        public void onRestoreInstanceState(Parcelable state) {
            Parcelable parcelableRestoreState;
            if (state instanceof Bundle) {
                Bundle bundle = (Bundle) state;
                try {
                    if (bundle.containsKey(VIEW_STATE)) {
                        this.mStatus = bundle.getInt(VIEW_STATE);
                    }
                    if (bundle.containsKey(CURSOR_STATE)) {
                        setCursorVisible(bundle.getBoolean(CURSOR_STATE));
                    }
                    if (bundle.containsKey(INSTANCE_STATE) && (parcelableRestoreState = bundle.getParcelable(INSTANCE_STATE)) != null) {
                        SearchView.super.onRestoreInstanceState(parcelableRestoreState);
                    }
                } catch (BadParcelableException e) {
                    Log.e(SearchView.TAG, "Parcelable, onRestoreInstanceState error");
                }
            } else {
                SearchView.super.onRestoreInstanceState(state);
            }
        }

        public Drawable getFocusedDrawable() {
            return this.mFocusedDrawable;
        }

        public void setFocusedDrawable(Drawable focusedDrawable) {
            this.mFocusedDrawable = focusedDrawable;
        }
    }

    @Override // android.view.View
    public void draw(Canvas canvas) {
        Drawable backGroundDrawable = this.mBackGroundDrawable;
        if (backGroundDrawable != null) {
            ViewGroup.LayoutParams params = getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
                Drawable backGround = getBackground();
                if (backGround != null) {
                    Rect bounds = backGround.getBounds();
                    backGroundDrawable.setBounds(bounds.left, bounds.top, bounds.right + marginLayoutParams.leftMargin + marginLayoutParams.rightMargin, bounds.bottom);
                    canvas.save();
                    canvas.translate((float) (-marginLayoutParams.leftMargin), 0.0f);
                    backGroundDrawable.draw(canvas);
                    canvas.restore();
                }
            }
        }
        super.draw(canvas);
    }

    public void setBackGroundEx(boolean isEnable) {
        ViewParent parent = getParent();
        ViewGroup viewGroup = null;
        if (parent instanceof ViewGroup) {
            viewGroup = (ViewGroup) parent;
        }
        if (isEnable) {
            Drawable backGround = getBackground();
            if (backGround instanceof ColorDrawable) {
                this.mBackGroundDrawable = new ColorDrawable(((ColorDrawable) backGround).getColor());
                if (viewGroup != null) {
                    viewGroup.setClipChildren(false);
                    return;
                }
                return;
            }
            return;
        }
        this.mBackGroundDrawable = null;
        if (viewGroup != null) {
            viewGroup.setClipChildren(true);
        }
    }

    @Override // android.view.View
    public void setHapticFeedbackEnabled(boolean isEnabled) {
        super.setHapticFeedbackEnabled(isEnabled);
        HwSearchAutoComplete hwSearchAutoComplete = this.mHwSearchSrcTextView;
        if (hwSearchAutoComplete != null) {
            hwSearchAutoComplete.setHapticFeedbackEnabled(isEnabled);
        }
    }
}
