package ohos.agp.components;

import ohos.agp.components.PageSlider;
import ohos.agp.components.element.Element;
import ohos.agp.database.Publisher;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.PageSliderIndicatorAttrsConstants;
import ohos.app.Context;

public final class PageSliderIndicator extends Component {
    private static final int NORMAL_INDEX = 0;
    private static final int SELECTED_INDEX = 1;
    private int mCountListener;
    private final Element[] mElements;
    private PageSlider mPageSlider;
    private final IndicatorSelectionHandler mSelectionHandler;

    private native int nativeGetCount(long j);

    private native long nativeGetHandle();

    private native int nativeGetItemOffset(long j);

    private native int nativeGetSelected(long j);

    private native void nativeSetItemElements(long j, long[] jArr);

    private native void nativeSetItemOffset(long j, int i);

    private native void nativeSetPageSlider(long j, long j2);

    private native void nativeSetSelected(long j, int i);

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
                pageChangedListener.onPageChosen(i);
            }
        }
    }

    public PageSliderIndicator(Context context) {
        this(context, null);
    }

    public PageSliderIndicator(Context context, AttrSet attrSet) {
        this(context, attrSet, "PageSliderIndicatorDefaultStyle");
    }

    public PageSliderIndicator(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mCountListener = 0;
        this.mSelectionHandler = new IndicatorSelectionHandler();
        this.mElements = new Element[]{null, null};
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getPageSliderIndicatorAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
        if (style.hasProperty(PageSliderIndicatorAttrsConstants.NORMAL_ELEMENT)) {
            setItemElement(style.getPropertyValue(PageSliderIndicatorAttrsConstants.NORMAL_ELEMENT).asElement(), getItemElements()[1]);
        }
        if (style.hasProperty(PageSliderIndicatorAttrsConstants.SELECTED_ELEMENT)) {
            setItemElement(getItemElements()[0], style.getPropertyValue(PageSliderIndicatorAttrsConstants.SELECTED_ELEMENT).asElement());
        }
    }

    public void setPageSlider(PageSlider pageSlider) {
        nativeSetPageSlider(this.mNativeViewPtr, pageSlider == null ? 0 : pageSlider.getNativeViewPtr());
        this.mPageSlider = pageSlider;
    }

    public PageSlider getPageSlider() {
        return this.mPageSlider;
    }

    public void setViewPager(PageSlider pageSlider) {
        setPageSlider(pageSlider);
    }

    public void addPageChangedListener(PageSlider.PageChangedListener pageChangedListener) {
        PageSlider pageSlider = getPageSlider();
        if (pageChangedListener != null && pageSlider != null) {
            pageSlider.addPageChangedListener(pageChangedListener);
            this.mCountListener++;
        }
    }

    public void removePageChangedListener(PageSlider.PageChangedListener pageChangedListener) {
        PageSlider pageSlider = getPageSlider();
        if (pageChangedListener != null && pageSlider != null) {
            pageSlider.removePageChangedListener(pageChangedListener);
            this.mCountListener--;
        }
    }

    public void addOnSelectionChangedListener(PageSlider.PageChangedListener pageChangedListener) {
        addPageChangedListener(pageChangedListener);
    }

    public void removeOnSelectionChangedListener(PageSlider.PageChangedListener pageChangedListener) {
        removePageChangedListener(pageChangedListener);
    }

    public int getPageChangedListenerCount() {
        return this.mCountListener;
    }

    public int getOnSelectionChangedListenerCount() {
        return getPageChangedListenerCount();
    }

    public int getCount() {
        return nativeGetCount(this.mNativeViewPtr);
    }

    public void setSelected(int i) {
        nativeSetSelected(this.mNativeViewPtr, i);
    }

    public int getSelected() {
        return nativeGetSelected(this.mNativeViewPtr);
    }

    public void setItemElement(Element element, Element element2) {
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
        nativeSetItemElements(this.mNativeViewPtr, new long[]{j, j2});
    }

    public void setItemNormalElement(Element element) {
        setItemElement(element, getItemSelectedElement());
    }

    public void setItemSelectedElement(Element element) {
        setItemElement(getItemNormalElement(), element);
    }

    public Element[] getItemElements() {
        Element[] elementArr = this.mElements;
        int length = elementArr.length;
        Element[] elementArr2 = new Element[length];
        System.arraycopy(elementArr, 0, elementArr2, 0, length);
        return elementArr2;
    }

    public Element getItemNormalElement() {
        return this.mElements[0];
    }

    public Element getItemSelectedElement() {
        return this.mElements[1];
    }

    public void setItemOffset(int i) {
        nativeSetItemOffset(this.mNativeViewPtr, i);
    }

    public int getItemOffset() {
        return nativeGetItemOffset(this.mNativeViewPtr);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetHandle();
        }
    }
}
