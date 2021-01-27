package ohos.agp.components;

import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.Text;
import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.ListContainerAttrsConstants;
import ohos.agp.utils.CallbackHelper;
import ohos.agp.utils.Color;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.app.Context;

public class ListContainer extends ComponentContainer implements Text.TextObserver {
    public static final int INVALID_INDEX = -1;
    private Element mBoundaryElement;
    int mFirstPosition;
    ItemClickedListener mItemClickedListener;
    ItemLongClickedListener mItemLongClickedListener;
    private BaseItemProvider mItemProvider;
    ItemSelectedListener mItemSelectedListener;
    int mSelectedIndex;

    public interface ItemClickedListener {
        void onItemClicked(ListContainer listContainer, Component component, int i, long j);
    }

    public interface ItemLongClickedListener {
        boolean onItemLongClicked(ListContainer listContainer, Component component, int i, long j);
    }

    public interface ItemSelectedListener {
        void onItemSelected(ListContainer listContainer, Component component, int i, long j);
    }

    private native int nativeGetBoundaryColor(long j);

    private native boolean nativeGetBoundarySwitch(long j);

    private native int nativeGetBoundaryThickness(long j);

    private native int nativeGetCenterFocusablePosition(long j);

    private native int nativeGetContentEndOffSet(long j);

    private native int nativeGetContentStartOffSet(long j);

    private native String nativeGetFilterText(long j);

    private native int nativeGetFirstVisiblePosition(long j);

    private native boolean nativeGetFooterBoundarySwitch(long j);

    private native boolean nativeGetHeaderBoundarySwitch(long j);

    private native int nativeGetLastVisiblePosition(long j);

    private native long nativeGetListContainerHandle();

    private native int nativeGetOrientation(long j);

    private native int nativeGetPositionForView(long j, Component component);

    private native boolean nativeGetReboundEffect(long j);

    private native void nativeGetReboundEffectParams(long j, ReboundEffectParams reboundEffectParams);

    private native int nativeGetSelectedItemIndex(long j);

    private native int nativeGetShaderColor(long j);

    private native void nativeScrollTo(long j, int i);

    private native void nativeScrollToCenter(long j, int i);

    private native long nativeSetAdapter(long j, BaseItemProvider baseItemProvider);

    private native void nativeSetBoundary(long j, long j2);

    private native void nativeSetBoundaryColor(long j, int i);

    private native void nativeSetBoundarySwitch(long j, boolean z);

    private native void nativeSetBoundaryThickness(long j, int i);

    private native void nativeSetContentEndOffSet(long j, int i);

    private native void nativeSetContentOffSet(long j, int i, int i2);

    private native void nativeSetContentStartOffSet(long j, int i);

    private native void nativeSetFilterText(long j, String str);

    private native void nativeSetFooterBoundarySwitch(long j, boolean z);

    private native void nativeSetHeaderBoundarySwitch(long j, boolean z);

    private native void nativeSetItemClickCallback(long j, ItemClickedListener itemClickedListener);

    private native void nativeSetItemLongClickCallback(long j, ItemLongClickedListener itemLongClickedListener);

    private native void nativeSetItemSelectedCallback(long j, ItemSelectedListener itemSelectedListener);

    private native void nativeSetOrientation(long j, int i);

    private native void nativeSetReboundEffect(long j, boolean z);

    private native void nativeSetReboundEffectParams(long j, int i, float f, int i2);

    private native void nativeSetSelectedItemIndex(long j, int i);

    private native void nativeSetShaderColor(long j, int i);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetListContainerHandle();
        }
    }

    protected static class ListContainerCleaner extends ComponentContainer.ComponentContainerCleaner {
        private native void nativeClearData(long j);

        ListContainerCleaner(long j) {
            super(j);
        }

        @Override // ohos.agp.components.ComponentContainer.ComponentContainerCleaner, ohos.agp.components.Component.ComponentCleaner, ohos.agp.utils.MemoryCleaner
        public void run() {
            if (this.mNativePtr != 0) {
                nativeClearData(this.mNativePtr);
            }
            super.run();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void registerCleaner() {
        MemoryCleanerRegistry.getInstance().registerWithNativeBind(this, new ListContainerCleaner(this.mNativeViewPtr), this.mNativeViewPtr);
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
        this.mSelectedIndex = -1;
        DirectionalLayoutManager directionalLayoutManager = new DirectionalLayoutManager();
        if (attrSet != null) {
            attrSet.getAttr("orientation").ifPresent(new Consumer(directionalLayoutManager) {
                /* class ohos.agp.components.$$Lambda$ListContainer$3UyIb41I0fV_y0GOl5FyomBDFC8 */
                private final /* synthetic */ LayoutManager f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$1.setOrientation(new AttrWrapper(Context.this, (Attr) obj).getIntegerValue());
                }
            });
        }
        setLayoutManager(directionalLayoutManager);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getListContainerAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty(ListContainerAttrsConstants.BOUNDARY)) {
            setBoundary(style.getPropertyValue(ListContainerAttrsConstants.BOUNDARY).asElement());
        }
    }

    @Override // ohos.agp.components.Text.TextObserver
    public void onTextUpdated(String str, int i, int i2, int i3) {
        setTextFilter(str);
    }

    public void scrollTo(int i) {
        nativeScrollTo(this.mNativeViewPtr, i);
    }

    public void setContentStartOffSet(int i) {
        nativeSetContentStartOffSet(this.mNativeViewPtr, i);
    }

    public void setContentEndOffSet(int i) {
        nativeSetContentEndOffSet(this.mNativeViewPtr, i);
    }

    public void setContentOffSet(int i, int i2) {
        nativeSetContentOffSet(this.mNativeViewPtr, i, i2);
    }

    public int getContentStartOffset() {
        return nativeGetContentStartOffSet(this.mNativeViewPtr);
    }

    public int getContentEndOffset() {
        return nativeGetContentEndOffSet(this.mNativeViewPtr);
    }

    public void scrollToCenter(int i) {
        nativeScrollToCenter(this.mNativeViewPtr, i);
    }

    public static class ReboundEffectParams {
        private int overscrollPercent;
        private float overscrollRate;
        private int remainVisiblePercent;

        public ReboundEffectParams(int i, float f, int i2) {
            this.overscrollPercent = i;
            this.overscrollRate = f;
            this.remainVisiblePercent = i2;
        }

        public int getOverscrollPercent() {
            return this.overscrollPercent;
        }

        public float getOverscrollRate() {
            return this.overscrollRate;
        }

        public int getRemainVisiblePercent() {
            return this.remainVisiblePercent;
        }

        public void setOverscrollPercent(int i) {
            this.overscrollPercent = i;
        }

        public void setOverscrollRate(float f) {
            this.overscrollRate = f;
        }

        public void setRemainVisiblePercent(int i) {
            this.remainVisiblePercent = i;
        }
    }

    public void setItemClickedListener(ItemClickedListener itemClickedListener) {
        this.mItemClickedListener = itemClickedListener;
        nativeSetItemClickCallback(this.mNativeViewPtr, itemClickedListener);
    }

    public boolean executeItemClick(Component component, int i, long j) {
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

    public int getIndexForComponent(Component component) {
        return nativeGetPositionForView(this.mNativeViewPtr, component);
    }

    public int getLastVisibleItemPosition() {
        return nativeGetLastVisiblePosition(this.mNativeViewPtr);
    }

    public int getFirstVisibleItemPosition() {
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

    public void setBoundary(Element element) {
        this.mBoundaryElement = element;
        nativeSetBoundary(this.mNativeViewPtr, element == null ? 0 : element.getNativeElementPtr());
    }

    public Element getBoundary() {
        return this.mBoundaryElement;
    }

    public void setFooterBoundarySwitch(boolean z) {
        nativeSetFooterBoundarySwitch(this.mNativeViewPtr, z);
    }

    public boolean getFooterBoundarySwitch() {
        return nativeGetFooterBoundarySwitch(this.mNativeViewPtr);
    }

    public void setHeaderBoundarySwitch(boolean z) {
        nativeSetHeaderBoundarySwitch(this.mNativeViewPtr, z);
    }

    public boolean getHeaderBoundarySwitch() {
        return nativeGetHeaderBoundarySwitch(this.mNativeViewPtr);
    }

    public void setBoundarySwitch(boolean z) {
        nativeSetBoundarySwitch(this.mNativeViewPtr, z);
    }

    public boolean getBoundarySwitch() {
        return nativeGetBoundarySwitch(this.mNativeViewPtr);
    }

    public void setBoundaryColor(Color color) {
        nativeSetBoundaryColor(this.mNativeViewPtr, color.getValue());
    }

    public Color getBoundaryColor() {
        return new Color(nativeGetBoundaryColor(this.mNativeViewPtr));
    }

    static /* synthetic */ boolean lambda$setBoundaryThickness$1(Integer num) {
        return num.intValue() >= 0;
    }

    public void setBoundaryThickness(int i) {
        if (validateParam(Integer.valueOf(i), $$Lambda$ListContainer$50VhX8zDLjGAkKsA8mbrmujGyyY.INSTANCE, "negative thickness")) {
            nativeSetBoundaryThickness(this.mNativeViewPtr, i);
        }
    }

    public int getBoundaryThickness() {
        return nativeGetBoundaryThickness(this.mNativeViewPtr);
    }

    public void setSelectedItemIndex(int i) {
        nativeSetSelectedItemIndex(this.mNativeViewPtr, i);
    }

    public int getSelectedItemIndex() {
        return nativeGetSelectedItemIndex(this.mNativeViewPtr);
    }

    public void setTextFilter(String str) {
        nativeSetFilterText(this.mNativeViewPtr, str);
    }

    public String getTextFilter() {
        return nativeGetFilterText(this.mNativeViewPtr);
    }

    public BaseItemProvider getItemProvider() {
        return this.mItemProvider;
    }

    public void setItemProvider(BaseItemProvider baseItemProvider) {
        long nativeSetAdapter = nativeSetAdapter(this.mNativeViewPtr, baseItemProvider);
        if (nativeSetAdapter != 0) {
            CallbackHelper.add(nativeSetAdapter, new WeakReference(baseItemProvider));
        }
        this.mItemProvider = baseItemProvider;
    }

    public int getOrientation() {
        return nativeGetOrientation(this.mNativeViewPtr);
    }

    public void setReboundEffectParams(int i, float f, int i2) {
        nativeSetReboundEffectParams(this.mNativeViewPtr, i, f, i2);
    }

    public void setReboundEffectParams(ReboundEffectParams reboundEffectParams) {
        nativeSetReboundEffectParams(this.mNativeViewPtr, reboundEffectParams.overscrollPercent, reboundEffectParams.overscrollRate, reboundEffectParams.remainVisiblePercent);
    }

    public ReboundEffectParams getReboundEffectParams() {
        ReboundEffectParams reboundEffectParams = new ReboundEffectParams(0, 0.0f, 0);
        nativeGetReboundEffectParams(this.mNativeViewPtr, reboundEffectParams);
        return reboundEffectParams;
    }
}
