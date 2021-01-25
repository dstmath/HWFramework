package ohos.agp.components;

import java.util.Objects;
import ohos.agp.components.Component;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.SearchViewAttrsConstants;
import ohos.agp.utils.Color;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class SearchBar extends DirectionalLayout {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "AGP_SearchView");
    private final Image mCloseButton;
    private Component.FocusChangedListener mFocusChangedListener;
    private FoldListener mOnCloseListener;
    private QueryListener mOnQueryTextListener;
    private final Image mSearchButton;
    private Component.ClickedListener mSearchClickListener;
    private final Image mSearchIcon;
    private final TextField mSearchTextView;
    private final Image mSubmitButton;

    public interface FoldListener {
        boolean onFold();
    }

    public interface QueryListener {
        boolean onQueryChanged(String str);

        boolean onQuerySubmit(String str);
    }

    private native long nativeGetSearchViewHandle();

    private native int nativeGetSearchViewMaxWidth(long j);

    private native String nativeGetSearchViewQuery(long j);

    private native String nativeGetSearchViewQueryHint(long j);

    private native int nativeGetSearchViewTextSize(long j);

    private native boolean nativeIsSearchViewIconified(long j);

    private native boolean nativeIsSearchViewIconifiedByDefault(long j);

    private native boolean nativeIsSearchViewSubmitButtonEnabled(long j);

    private native void nativeSetCloseButton(long j, long j2);

    private native void nativeSetSearchButton(long j, long j2);

    private native void nativeSetSearchIcon(long j, long j2);

    private native void nativeSetSearchTextView(long j, long j2);

    private native void nativeSetSearchViewIconified(long j, boolean z);

    private native void nativeSetSearchViewIconifiedByDefault(long j, boolean z);

    private native void nativeSetSearchViewMaxWidth(long j, int i);

    private native void nativeSetSearchViewOnCloseListener(long j, FoldListener foldListener);

    private native void nativeSetSearchViewOnQueryTextFocusChangeListener(long j, Component.FocusChangedListener focusChangedListener);

    private native void nativeSetSearchViewOnQueryTextListener(long j, QueryListener queryListener);

    private native void nativeSetSearchViewOnSearchClickListener(long j, Component.ClickedListener clickedListener);

    private native void nativeSetSearchViewQuery(long j, String str, boolean z);

    private native void nativeSetSearchViewQueryHint(long j, String str);

    private native void nativeSetSearchViewSubmitButtonEnabled(long j, boolean z);

    private native void nativeSetSearchViewTextSize(long j, int i);

    private native void nativeSetSubmitButton(long j, long j2);

    public SearchBar(Context context) {
        this(context, null);
    }

    public SearchBar(Context context, AttrSet attrSet) {
        this(context, attrSet, "SearchViewDefaultStyle");
    }

    public SearchBar(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mOnCloseListener = null;
        this.mOnQueryTextListener = null;
        this.mFocusChangedListener = null;
        this.mSearchClickListener = null;
        this.mSearchTextView = new TextField(context);
        this.mSearchButton = new Image(context);
        this.mSearchIcon = new Image(context);
        this.mSubmitButton = new Image(context);
        this.mCloseButton = new Image(context);
        initSearchView();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.DirectionalLayout, ohos.agp.components.LinearLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new SearchViewAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty(SearchViewAttrsConstants.SEARCH_BUTTON_URI)) {
            setSearchButtonUri(style.getPropertyValue(SearchViewAttrsConstants.SEARCH_BUTTON_URI).asString());
        }
        if (style.hasProperty(SearchViewAttrsConstants.SEARCH_ICON_URI)) {
            setSearchIconUri(style.getPropertyValue(SearchViewAttrsConstants.SEARCH_ICON_URI).asString());
        }
        if (style.hasProperty(SearchViewAttrsConstants.CLOSE_BUTTON_URI)) {
            setCloseButtonUri(style.getPropertyValue(SearchViewAttrsConstants.CLOSE_BUTTON_URI).asString());
        }
        if (style.hasProperty(SearchViewAttrsConstants.SUBMIT_BUTTON_URI)) {
            setSubmitButtonUri(style.getPropertyValue(SearchViewAttrsConstants.SUBMIT_BUTTON_URI).asString());
        }
    }

    private void initSearchView() {
        setOrientation(0);
        this.mSearchButton.setClickable(true);
        this.mSubmitButton.setClickable(true);
        this.mCloseButton.setClickable(true);
        addComponent(this.mSearchButton);
        addComponent(this.mSearchIcon);
        addComponent(this.mSearchTextView);
        addComponent(this.mCloseButton);
        addComponent(this.mSubmitButton);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.LinearLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetSearchViewHandle();
        }
    }

    @Override // ohos.agp.components.ComponentContainer
    public void addComponent(Component component) {
        if (Objects.equals(component, this.mSearchTextView)) {
            nativeSetSearchTextView(this.mNativeViewPtr, this.mSearchTextView.getNativeViewPtr());
        } else if (Objects.equals(component, this.mSearchButton)) {
            nativeSetSearchButton(this.mNativeViewPtr, this.mSearchButton.getNativeViewPtr());
        } else if (Objects.equals(component, this.mSearchIcon)) {
            nativeSetSearchIcon(this.mNativeViewPtr, this.mSearchIcon.getNativeViewPtr());
        } else if (Objects.equals(component, this.mCloseButton)) {
            nativeSetCloseButton(this.mNativeViewPtr, this.mCloseButton.getNativeViewPtr());
        } else if (Objects.equals(component, this.mSubmitButton)) {
            nativeSetSubmitButton(this.mNativeViewPtr, this.mSubmitButton.getNativeViewPtr());
        } else {
            HiLog.debug(TAG, "View not found.", new Object[0]);
        }
        super.addComponent(component);
    }

    public void setTextSize(int i) {
        nativeSetSearchViewTextSize(this.mNativeViewPtr, i);
    }

    public int getTextSize() {
        return nativeGetSearchViewTextSize(this.mNativeViewPtr);
    }

    public void setQuery(String str, boolean z) {
        nativeSetSearchViewQuery(this.mNativeViewPtr, str, z);
    }

    public String getQuery() {
        return nativeGetSearchViewQuery(this.mNativeViewPtr);
    }

    public void setQueryHint(String str) {
        nativeSetSearchViewQueryHint(this.mNativeViewPtr, str);
    }

    public String getQueryHint() {
        return nativeGetSearchViewQueryHint(this.mNativeViewPtr);
    }

    public void setMaxWidth(int i) {
        nativeSetSearchViewMaxWidth(this.mNativeViewPtr, i);
    }

    public int getMaxWidth() {
        return nativeGetSearchViewMaxWidth(this.mNativeViewPtr);
    }

    public void setIconified(boolean z) {
        nativeSetSearchViewIconified(this.mNativeViewPtr, z);
    }

    public boolean isIconified() {
        return nativeIsSearchViewIconified(this.mNativeViewPtr);
    }

    public void setIconifiedByDefault(boolean z) {
        nativeSetSearchViewIconifiedByDefault(this.mNativeViewPtr, z);
    }

    public boolean isIconifiedByDefault() {
        return nativeIsSearchViewIconifiedByDefault(this.mNativeViewPtr);
    }

    public void setFoldListener(FoldListener foldListener) {
        this.mOnCloseListener = foldListener;
        nativeSetSearchViewOnCloseListener(this.mNativeViewPtr, foldListener);
    }

    public void setQueryListener(QueryListener queryListener) {
        this.mOnQueryTextListener = queryListener;
        nativeSetSearchViewOnQueryTextListener(this.mNativeViewPtr, queryListener);
    }

    public void setQueryFocusChangeListener(Component.FocusChangedListener focusChangedListener) {
        this.mFocusChangedListener = focusChangedListener;
        nativeSetSearchViewOnQueryTextFocusChangeListener(this.mNativeViewPtr, focusChangedListener);
    }

    public void setSearchClickListener(Component.ClickedListener clickedListener) {
        this.mSearchClickListener = clickedListener;
        nativeSetSearchViewOnSearchClickListener(this.mNativeViewPtr, clickedListener);
    }

    public void setSubmitButtonEnabled(boolean z) {
        nativeSetSearchViewSubmitButtonEnabled(this.mNativeViewPtr, z);
    }

    public boolean isSubmitButtonEnabled() {
        return nativeIsSearchViewSubmitButtonEnabled(this.mNativeViewPtr);
    }

    public void setSearchButtonUri(String str) {
        if (str != null) {
            this.mSearchButton.setImageURI(str);
        }
    }

    public void setSearchIconUri(String str) {
        if (str != null) {
            this.mSearchIcon.setImageURI(str);
        }
    }

    public void setCloseButtonUri(String str) {
        if (str != null) {
            this.mCloseButton.setImageURI(str);
        }
    }

    public void setSubmitButtonUri(String str) {
        if (str != null) {
            this.mSubmitButton.setImageURI(str);
        }
    }

    public void setSearchTextColor(Color color) {
        this.mSearchTextView.setTextColor(color);
    }
}
