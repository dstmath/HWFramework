package ohos.agp.components;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import ohos.agp.components.Component;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.SearchViewAttrsConstants;
import ohos.agp.utils.Color;
import ohos.agp.utils.TextTool;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.image.PixelMap;

public class SearchBar extends DirectionalLayout {
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "AGP_SearchView");
    private final Image mCloseButton;
    private Component.FocusChangedListener mFocusChangedListener;
    private FoldListener mOnCloseListener;
    private QueryListener mOnQueryTextListener;
    private final Image mSearchButton;
    private Component.ClickedListener mSearchClickListener;
    private final Image mSearchIcon;
    private final TextField mSearchText;
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
        this.mSearchText = new TextField(context);
        this.mSearchButton = new Image(context);
        this.mSearchIcon = new Image(context);
        this.mSubmitButton = new Image(context);
        this.mCloseButton = new Image(context);
        initSearchView();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.DirectionalLayout, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getSearchViewAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        convertElementToPixelmap(style, SearchViewAttrsConstants.SEARCH_BUTTON).ifPresent(new Consumer() {
            /* class ohos.agp.components.$$Lambda$oWekyliaZ4wC0HQ1AcRfmDnfVs */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SearchBar.this.setSearchButton((PixelMap) obj);
            }
        });
        convertElementToPixelmap(style, SearchViewAttrsConstants.SEARCH_ICON).ifPresent(new Consumer() {
            /* class ohos.agp.components.$$Lambda$RiGB8GoqND5Mjo9IH1eH7hVvnnA */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SearchBar.this.setSearchIcon((PixelMap) obj);
            }
        });
        convertElementToPixelmap(style, SearchViewAttrsConstants.CLOSE_BUTTON).ifPresent(new Consumer() {
            /* class ohos.agp.components.$$Lambda$H41DVmsDMue3ZA_S5L0SgYmpVH8 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SearchBar.this.setCloseButton((PixelMap) obj);
            }
        });
        convertElementToPixelmap(style, SearchViewAttrsConstants.SUBMIT_BUTTON).ifPresent(new Consumer() {
            /* class ohos.agp.components.$$Lambda$JlUOMkLC0F9ha4dYpQJELtCR_TQ */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                SearchBar.this.setSubmitButton((PixelMap) obj);
            }
        });
    }

    private Optional<PixelMap> convertElementToPixelmap(Style style, String str) {
        if (style == null || str == null || !style.hasProperty(str)) {
            return Optional.empty();
        }
        Element asElement = style.getPropertyValue(str).asElement();
        if (asElement instanceof PixelMapElement) {
            return Optional.ofNullable(((PixelMapElement) asElement).getPixelMap());
        }
        return Optional.empty();
    }

    private void initSearchView() {
        setOrientation(0);
        this.mSearchButton.setClickable(true);
        this.mSubmitButton.setClickable(true);
        this.mCloseButton.setClickable(true);
        addComponent(this.mSearchButton);
        addComponent(this.mSearchIcon);
        addComponent(this.mSearchText);
        addComponent(this.mCloseButton);
        addComponent(this.mSubmitButton);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.DirectionalLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetSearchViewHandle();
        }
    }

    @Override // ohos.agp.components.ComponentContainer
    public void addComponent(Component component) {
        if (Objects.equals(component, this.mSearchText)) {
            nativeSetSearchTextView(this.mNativeViewPtr, this.mSearchText.getNativeViewPtr());
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

    public TextField getSearchText() {
        return this.mSearchText;
    }

    public Image getSearchButton() {
        return this.mSearchButton;
    }

    public Image getSearchIcon() {
        return this.mSearchIcon;
    }

    public Image getSubmitButton() {
        return this.mSubmitButton;
    }

    public Image getCloseButton() {
        return this.mCloseButton;
    }

    public void setTextSize(int i) {
        if (TextTool.validateTextSizeParam(i)) {
            nativeSetSearchViewTextSize(this.mNativeViewPtr, i);
        }
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

    public void setSearchHint(String str) {
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

    public void setIconifiedState(boolean z) {
        nativeSetSearchViewIconified(this.mNativeViewPtr, z);
    }

    public boolean getIconifiedState() {
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

    public FoldListener getFoldListener() {
        return this.mOnCloseListener;
    }

    public void setQueryListener(QueryListener queryListener) {
        this.mOnQueryTextListener = queryListener;
        nativeSetSearchViewOnQueryTextListener(this.mNativeViewPtr, queryListener);
    }

    public QueryListener getQueryListener() {
        return this.mOnQueryTextListener;
    }

    public void setQueryFocusChangeListener(Component.FocusChangedListener focusChangedListener) {
        this.mFocusChangedListener = focusChangedListener;
        nativeSetSearchViewOnQueryTextFocusChangeListener(this.mNativeViewPtr, focusChangedListener);
    }

    public void setSearchClickListener(Component.ClickedListener clickedListener) {
        this.mSearchClickListener = clickedListener;
        nativeSetSearchViewOnSearchClickListener(this.mNativeViewPtr, clickedListener);
    }

    public Component.ClickedListener getSearchClickListener() {
        return this.mSearchClickListener;
    }

    public void activateSubmitButton(boolean z) {
        nativeSetSearchViewSubmitButtonEnabled(this.mNativeViewPtr, z);
    }

    public boolean isSearchButtonEnable() {
        return nativeIsSearchViewSubmitButtonEnabled(this.mNativeViewPtr);
    }

    public void setSearchButton(PixelMap pixelMap) {
        if (pixelMap != null) {
            this.mSearchButton.setPixelMap(pixelMap);
        }
    }

    public void setSearchIcon(PixelMap pixelMap) {
        if (pixelMap != null) {
            this.mSearchIcon.setPixelMap(pixelMap);
        }
    }

    public void setCloseButton(PixelMap pixelMap) {
        if (pixelMap != null) {
            this.mCloseButton.setPixelMap(pixelMap);
        }
    }

    public void setSubmitButton(PixelMap pixelMap) {
        if (pixelMap != null) {
            this.mSubmitButton.setPixelMap(pixelMap);
        }
    }

    public void setSearchButton(int i) {
        this.mSearchButton.setPixelMap(i);
    }

    public void setSearchIcon(int i) {
        this.mSearchIcon.setPixelMap(i);
    }

    public void setSubmitButton(int i) {
        this.mSubmitButton.setPixelMap(i);
    }

    public void setCloseButton(int i) {
        this.mCloseButton.setPixelMap(i);
    }

    public void setSearchTextColor(Color color) {
        this.mSearchText.setTextColor(color);
    }

    public Color getSearchTextColor() {
        return this.mSearchText.getTextColor();
    }
}
