package ohos.agp.components;

import ohos.agp.components.Text;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.ListContainerAttrsConstants;
import ohos.agp.utils.Color;
import ohos.app.Context;

public class ListContainer extends ComponentContainer implements Text.TextObserver {
    public static final int INVALID_POSITION = -1;
    int mFirstPosition;
    ItemClickedListener mItemClickedListener;
    ItemLongClickedListener mItemLongClickedListener;
    private BaseItemProvider mItemProvider;
    ItemSelectedListener mItemSelectedListener;
    int mSelectedPosition;

    public interface ItemClickedListener {
        void onItemClicked(ListContainer listContainer, Component component, int i, long j);
    }

    public interface ItemLongClickedListener {
        boolean onItemLongClicked(ListContainer listContainer, Component component, int i, long j);
    }

    public interface ItemSelectedListener {
        void onItemSelected(ListContainer listContainer, Component component, int i, long j);
    }

    private native int nativeGetCenterFocusablePosition(long j);

    private native int nativeGetFirstVisiblePosition(long j);

    private native int nativeGetLastVisiblePosition(long j);

    private native long nativeGetListContainerHandle();

    private native int nativeGetOrientation(long j);

    private native int nativeGetPositionForView(long j, Component component);

    private native boolean nativeGetReboundEffect(long j);

    private native int nativeGetShaderColor(long j);

    private native void nativeScrollTo(long j, int i);

    private native void nativeScrollToCenter(long j, int i);

    private native void nativeSetAdapter(long j, BaseItemProvider baseItemProvider);

    private native void nativeSetFilterText(long j, String str);

    private native void nativeSetItemClickCallback(long j, ItemClickedListener itemClickedListener);

    private native void nativeSetItemLongClickCallback(long j, ItemLongClickedListener itemLongClickedListener);

    private native void nativeSetItemSelectedCallback(long j, ItemSelectedListener itemSelectedListener);

    private native void nativeSetOrientation(long j, int i);

    private native void nativeSetReboundEffect(long j, boolean z);

    private native void nativeSetShaderColor(long j, int i);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetListContainerHandle();
        }
    }

    public ListContainer(Context context) {
        this(context, null);
    }

    public ListContainer(Context context, AttrSet attrSet) {
        this(context, attrSet, "ListContainerDefaultStyle");
    }

    public ListContainer(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mFirstPosition = 0;
        this.mSelectedPosition = -1;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new ListContainerAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Text.TextObserver
    public void onTextChanged(String str, int i, int i2, int i3) {
        setTextFilter(str);
    }

    public void scrollTo(int i) {
        nativeScrollTo(this.mNativeViewPtr, i);
    }

    public void scrollToCenter(int i) {
        nativeScrollToCenter(this.mNativeViewPtr, i);
    }

    public void setItemClickedListener(ItemClickedListener itemClickedListener) {
        this.mItemClickedListener = itemClickedListener;
        nativeSetItemClickCallback(this.mNativeViewPtr, itemClickedListener);
    }

    public boolean performItemClick(Component component, int i, long j) {
        ItemClickedListener itemClickedListener = this.mItemClickedListener;
        if (itemClickedListener == null) {
            return false;
        }
        itemClickedListener.onItemClicked(this, component, i, j);
        return true;
    }

    public void setItemLongClickedListener(ItemLongClickedListener itemLongClickedListener) {
        this.mItemLongClickedListener = itemLongClickedListener;
        nativeSetItemLongClickCallback(this.mNativeViewPtr, itemLongClickedListener);
    }

    /* access modifiers changed from: protected */
    public boolean onItemLongClicked(ListContainer listContainer, Component component, int i, long j) {
        ItemLongClickedListener itemLongClickedListener = this.mItemLongClickedListener;
        if (itemLongClickedListener != null) {
            return itemLongClickedListener.onItemLongClicked(this, component, i, j);
        }
        return false;
    }

    public void setItemSelectedListener(ItemSelectedListener itemSelectedListener) {
        this.mItemSelectedListener = itemSelectedListener;
        nativeSetItemSelectedCallback(this.mNativeViewPtr, itemSelectedListener);
    }

    /* access modifiers changed from: protected */
    public void onItemSelected(ListContainer listContainer, Component component, int i, long j) {
        ItemSelectedListener itemSelectedListener = this.mItemSelectedListener;
        if (itemSelectedListener != null) {
            itemSelectedListener.onItemSelected(this, component, i, j);
        }
    }

    public int getPositionForComponent(Component component) {
        return nativeGetPositionForView(this.mNativeViewPtr, component);
    }

    public int getLastVisiblePosition() {
        return nativeGetLastVisiblePosition(this.mNativeViewPtr);
    }

    public int getFirstVisiblePosition() {
        return nativeGetFirstVisiblePosition(this.mNativeViewPtr);
    }

    public int getCenterFocusablePosition() {
        return nativeGetCenterFocusablePosition(this.mNativeViewPtr);
    }

    public void setReboundEffect(boolean z) {
        nativeSetReboundEffect(this.mNativeViewPtr, z);
    }

    public boolean getReboundEffect() {
        return nativeGetReboundEffect(this.mNativeViewPtr);
    }

    public void setShaderColor(Color color) {
        nativeSetShaderColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getShaderColor() {
        return new Color(nativeGetShaderColor(this.mNativeViewPtr));
    }

    public void setOrientation(int i) {
        if (i == 1 || i == 0) {
            nativeSetOrientation(this.mNativeViewPtr, i);
        }
    }

    public void setSelectedItemPosition(int i) {
        this.mSelectedPosition = i;
    }

    public int getSelectedItemPosition() {
        return this.mSelectedPosition;
    }

    public void setTextFilter(String str) {
        nativeSetFilterText(this.mNativeViewPtr, str);
    }

    public BaseItemProvider getItemProvider() {
        return this.mItemProvider;
    }

    public void setItemProvider(BaseItemProvider baseItemProvider) {
        nativeSetAdapter(this.mNativeViewPtr, baseItemProvider);
        this.mItemProvider = baseItemProvider;
    }

    public int getOrientation() {
        return nativeGetOrientation(this.mNativeViewPtr);
    }
}
