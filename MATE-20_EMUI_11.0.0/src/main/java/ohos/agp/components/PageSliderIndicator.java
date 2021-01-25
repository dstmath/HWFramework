package ohos.agp.components;

import ohos.agp.components.PageSlider;
import ohos.agp.components.element.Element;
import ohos.agp.styles.Style;
import ohos.app.Context;

public final class PageSliderIndicator extends ViewPagerIndicator {
    private int mCountListener;

    public PageSliderIndicator(Context context) {
        this(context, null);
    }

    public PageSliderIndicator(Context context, AttrSet attrSet) {
        this(context, attrSet, "PageSliderIndicatorDefaultStyle");
    }

    public PageSliderIndicator(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mCountListener = 0;
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ViewPagerIndicator, ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        return super.convertAttrToStyle(attrSet);
    }

    @Override // ohos.agp.components.ViewPagerIndicator, ohos.agp.components.Component
    public void applyStyle(Style style) {
        super.applyStyle(style);
    }

    public void setPageSlider(PageSlider pageSlider) {
        super.setViewPager(pageSlider);
    }

    public PageSlider getPageSlider() {
        return super.getViewPager();
    }

    public void addSelectionChangedListener(PageSlider.PageChangedListener pageChangedListener) {
        PageSlider pageSlider = getPageSlider();
        if (pageChangedListener != null && pageSlider != null) {
            pageSlider.addPageChangeListener(pageChangedListener);
            this.mCountListener++;
        }
    }

    public void removeSelectionChangedListener(PageSlider.PageChangedListener pageChangedListener) {
        PageSlider pageSlider = getPageSlider();
        if (pageChangedListener != null && pageSlider != null) {
            pageSlider.removePageChangeListener(pageChangedListener);
            this.mCountListener--;
        }
    }

    public int getSelectionChangedListenerCount() {
        return this.mCountListener;
    }

    @Override // ohos.agp.components.ViewPagerIndicator
    public int getCount() {
        return super.getCount();
    }

    @Override // ohos.agp.components.ViewPagerIndicator
    public void setSelected(int i) {
        super.setSelected(i);
    }

    @Override // ohos.agp.components.ViewPagerIndicator
    public int getSelected() {
        return super.getSelected();
    }

    public void setItemElement(Element element, Element element2) {
        super.setItemDrawable(element, element2);
    }

    public Element[] getItemElements() {
        int length = this.mElements.length;
        Element[] elementArr = new Element[length];
        System.arraycopy(this.mElements, 0, elementArr, 0, length);
        return elementArr;
    }

    @Override // ohos.agp.components.ViewPagerIndicator
    public void setItemOffset(int i) {
        super.setItemOffset(i);
    }

    @Override // ohos.agp.components.ViewPagerIndicator
    public int getItemOffset() {
        return super.getItemOffset();
    }
}
