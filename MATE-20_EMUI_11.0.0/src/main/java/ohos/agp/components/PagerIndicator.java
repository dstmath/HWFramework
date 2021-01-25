package ohos.agp.components;

import ohos.agp.components.PageSlider;
import ohos.agp.components.ViewPagerIndicator;
import ohos.agp.components.element.Element;
import ohos.app.Context;

public class PagerIndicator extends ViewPagerIndicator {
    protected final Element[] mElement;

    protected static class IndicatorSelectionHandler extends ViewPagerIndicator.IndicatorSelectionHandler {
        protected IndicatorSelectionHandler() {
        }
    }

    public PagerIndicator(Context context) {
        super(context);
        this.mElement = new Element[]{null, null};
    }

    public PagerIndicator(Context context, AttrSet attrSet) {
        super(context, attrSet);
        this.mElement = new Element[]{null, null};
    }

    public PagerIndicator(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mElement = new Element[]{null, null};
    }

    public void setPageSlider(PageSlider pageSlider) {
        super.setViewPager(pageSlider);
    }

    public PageSlider getPageSlider() {
        return super.getViewPager();
    }

    public void addPageChangedListener(PageSlider.PageChangedListener pageChangedListener) {
        super.addOnSelectionChangedListener(pageChangedListener);
    }

    public void removePageChangedListener(PageSlider.PageChangedListener pageChangedListener) {
        super.removeOnSelectionChangedListener(pageChangedListener);
    }

    public int getPageChangedListenerCount() {
        return super.getOnSelectionChangedListenerCount();
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
        return super.getItemDrawables();
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
