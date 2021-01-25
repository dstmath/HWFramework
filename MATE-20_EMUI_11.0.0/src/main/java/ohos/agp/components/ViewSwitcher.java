package ohos.agp.components;

import ohos.app.Context;

@Deprecated
public class ViewSwitcher extends ViewAnimator {
    ViewFactory mFactory;

    public interface ViewFactory {
        Component makeView();
    }

    private native long nativeGetViewSwitcherHandle();

    private native void nativeViewSwitcherReset(long j);

    public ViewSwitcher(Context context) {
        this(context, null);
    }

    public ViewSwitcher(Context context, AttrSet attrSet) {
        this(context, attrSet, "ViewSwitcherDefaultStyle");
    }

    public ViewSwitcher(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ViewAnimator, ohos.agp.components.StackLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetViewSwitcherHandle();
        }
    }

    @Override // ohos.agp.components.ViewAnimator, ohos.agp.components.ComponentContainer
    public void addComponent(Component component) {
        if (getChildCount() < 2) {
            super.addComponent(component);
            return;
        }
        throw new IllegalStateException("Can't add more than 2 views to a ViewSwitcher");
    }

    public Component getNextComponent() {
        return getComponentAt(getDisplayedChild() == 0 ? 1 : 0);
    }

    public void setFactory(ViewFactory viewFactory) {
        this.mFactory = viewFactory;
        createComponent();
        createComponent();
    }

    private void createComponent() {
        addComponent(this.mFactory.makeView());
    }

    public void reset() {
        nativeViewSwitcherReset(this.mNativeViewPtr);
    }
}
