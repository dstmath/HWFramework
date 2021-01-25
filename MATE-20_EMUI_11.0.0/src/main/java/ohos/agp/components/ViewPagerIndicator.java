package ohos.agp.components;

import ohos.agp.components.PageSlider;
import ohos.agp.components.element.Element;
import ohos.agp.database.Publisher;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.ViewPagerIndicatorAttrsConstants;
import ohos.app.Context;

public class ViewPagerIndicator extends Component {
    private static final int NORMAL_INDEX = 0;
    private static final int SELECTED_INDEX = 1;
    protected final Element[] mElements;
    private IndicatorSelectionHandler mSelectionHandler;
    private PageSlider mViewPager;

    private native long nativeGetViewPagerIndicatorHandle();

    private native void nativeIndicatorRemoveOnSelectedCallback(long j);

    private native void nativeIndicatorSetOnSelectedCallback(long j, IndicatorSelectionHandler indicatorSelectionHandler);

    private native int nativeViewPagerIndicatorGetCount(long j);

    private native int nativeViewPagerIndicatorGetItemOffset(long j);

    private native int nativeViewPagerIndicatorGetSelected(long j);

    private native void nativeViewPagerIndicatorSetItemDrawables(long j, long[] jArr);

    private native void nativeViewPagerIndicatorSetItemOffset(long j, int i);

    private native void nativeViewPagerIndicatorSetSelected(long j, int i);

    private native void nativeViewPagerIndicatorSetViewPager(long j, long j2);

    protected static class IndicatorSelectionHandler extends Publisher<PageSlider.PageChangedListener> {
        protected IndicatorSelectionHandler() {
        }

        /* access modifiers changed from: package-private */
        public int getListenersCount() {
            return this.mSubscribers.size();
        }

        /* access modifiers changed from: package-private */
        public void selectionChanged(int i) {
            for (PageSlider.PageChangedListener pageChangedListener : this.mSubscribers) {
                pageChangedListener.onPageSelected(i);
            }
        }
    }

    public ViewPagerIndicator(Context context) {
        this(context, null);
    }

    public ViewPagerIndicator(Context context, AttrSet attrSet) {
        this(context, attrSet, "ViewPagerIndicatorDefaultStyle");
    }

    public ViewPagerIndicator(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mSelectionHandler = new IndicatorSelectionHandler();
        this.mElements = new Element[]{null, null};
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new ViewPagerIndicatorAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty(ViewPagerIndicatorAttrsConstants.NORMAL_ELEMENT)) {
            setItemDrawable(style.getPropertyValue(ViewPagerIndicatorAttrsConstants.NORMAL_ELEMENT).asElement(), getItemDrawables()[1]);
        }
        if (style.hasProperty(ViewPagerIndicatorAttrsConstants.SELECTED_ELEMENT)) {
            setItemDrawable(getItemDrawables()[0], style.getPropertyValue(ViewPagerIndicatorAttrsConstants.SELECTED_ELEMENT).asElement());
        }
    }

    public void setViewPager(PageSlider pageSlider) {
        nativeViewPagerIndicatorSetViewPager(this.mNativeViewPtr, pageSlider == null ? 0 : pageSlider.getNativeViewPtr());
        this.mViewPager = pageSlider;
    }

    public PageSlider getViewPager() {
        return this.mViewPager;
    }

    public void addOnSelectionChangedListener(PageSlider.PageChangedListener pageChangedListener) {
        if (pageChangedListener != null) {
            if (this.mSelectionHandler.getListenersCount() == 0) {
                nativeIndicatorSetOnSelectedCallback(this.mNativeViewPtr, this.mSelectionHandler);
            }
            this.mSelectionHandler.registerSubscriber(pageChangedListener);
        }
    }

    public void removeOnSelectionChangedListener(PageSlider.PageChangedListener pageChangedListener) {
        this.mSelectionHandler.unregisterSubscriber((IndicatorSelectionHandler) pageChangedListener);
        if (this.mSelectionHandler.getListenersCount() == 0) {
            nativeIndicatorRemoveOnSelectedCallback(this.mNativeViewPtr);
        }
    }

    public int getOnSelectionChangedListenerCount() {
        return this.mSelectionHandler.getListenersCount();
    }

    public int getCount() {
        return nativeViewPagerIndicatorGetCount(this.mNativeViewPtr);
    }

    public void setSelected(int i) {
        nativeViewPagerIndicatorSetSelected(this.mNativeViewPtr, i);
    }

    public int getSelected() {
        return nativeViewPagerIndicatorGetSelected(this.mNativeViewPtr);
    }

    public void setItemDrawable(Element element, Element element2) {
        long j;
        Element[] elementArr = this.mElements;
        System.arraycopy(new Element[]{element, element2}, 0, elementArr, 0, elementArr.length);
        long j2 = 0;
        if (element == null) {
            j = 0;
        } else {
            j = element.getNativeElementPtr();
        }
        if (element2 != null) {
            j2 = element2.getNativeElementPtr();
        }
        nativeViewPagerIndicatorSetItemDrawables(this.mNativeViewPtr, new long[]{j, j2});
    }

    public Element[] getItemDrawables() {
        Element[] elementArr = this.mElements;
        return new Element[]{elementArr[0], elementArr[1]};
    }

    public void setItemOffset(int i) {
        nativeViewPagerIndicatorSetItemOffset(this.mNativeViewPtr, i);
    }

    public int getItemOffset() {
        return nativeViewPagerIndicatorGetItemOffset(this.mNativeViewPtr);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetViewPagerIndicatorHandle();
        }
    }
}
