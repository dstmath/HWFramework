package ohos.agp.components;

import java.util.function.Consumer;
import ohos.agp.components.Component;
import ohos.agp.database.Publisher;
import ohos.agp.styles.attributes.ViewPagerAttrsConstants;
import ohos.app.Context;

public class PageSlider extends StackLayout {
    public static final int DEFAULT_CACHED_PAGES_LIMIT = 1;
    public static final int INVALID_POSITION = -1;
    public static final int SLIDING_STATE_DRAGGING = 1;
    public static final int SLIDING_STATE_IDLE = 0;
    public static final int SLIDING_STATE_SETTLING = 2;
    private PageChangePublisher mPageChangePublisher;
    private PageSliderProvider mPageSliderProvider;

    public interface PageChangedListener {
        void onPageScrollStateChanged(int i);

        void onPageScrolled(int i, float f, int i2);

        void onPageSelected(int i);
    }

    private native int nativeGetCurrentItem(long j);

    private native int nativeGetCurrentScrollState(long j);

    private native int nativeGetOffscreenPageLimit(long j);

    private native int nativeGetOrientation(long j);

    private native int nativeGetPageSwitchTime(long j);

    private native boolean nativeGetReboundEffect(long j);

    private native boolean nativeGetSlidingEnabled(long j);

    private native void nativeSetAdapter(long j, PageSliderProvider pageSliderProvider);

    private native void nativeSetCurrentItem(long j, int i, boolean z);

    private native void nativeSetOffscreenPageLimit(long j, int i);

    private native void nativeSetOrientation(long j, int i);

    private native void nativeSetPageSwitchTime(long j, int i);

    private native void nativeSetReboundEffect(long j, boolean z);

    private native void nativeSetSlidingEnabled(long j, boolean z);

    private native long nativeViewPagerGetHandle();

    public PageSlider(Context context) {
        this(context, null);
    }

    public PageSlider(Context context, AttrSet attrSet) {
        this(context, attrSet, "PageSliderDefaultStyle");
    }

    public PageSlider(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mPageChangePublisher = new PageChangePublisher();
        AttrSet mergeStyle = AttrHelper.mergeStyle(context, attrSet, 0);
        for (int i = 0; i < mergeStyle.getLength(); i++) {
            mergeStyle.getAttr(i).ifPresent(new Consumer(context) {
                /* class ohos.agp.components.$$Lambda$PageSlider$N4MkTUGIBvs0QHBhIvpa1ZuPe0Q */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    PageSlider.this.lambda$new$0$PageSlider(this.f$1, (Attr) obj);
                }
            });
        }
        DirectionalLayout directionalLayout = new DirectionalLayout(context);
        directionalLayout.setLayoutDirection(Component.LayoutDirection.INHERIT);
        int nativeGetOffscreenPageLimit = nativeGetOffscreenPageLimit(this.mNativeViewPtr);
        int i2 = nativeGetOffscreenPageLimit + 1 + nativeGetOffscreenPageLimit;
        for (int i3 = 0; i3 < i2; i3++) {
            StackLayout stackLayout = new StackLayout(context);
            stackLayout.setLayoutDirection(Component.LayoutDirection.INHERIT);
            directionalLayout.addComponent(stackLayout);
        }
        addComponent(directionalLayout);
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0032  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x003d  */
    public /* synthetic */ void lambda$new$0$PageSlider(Context context, Attr attr) {
        char c;
        AttrWrapper attrWrapper = new AttrWrapper(context, attr);
        String name = attr.getName();
        int hashCode = name.hashCode();
        if (hashCode != -1439500848) {
            if (hashCode == 1180941550 && name.equals(ViewPagerAttrsConstants.OFF_SCREEN_PAGE_LIMIT)) {
                c = 0;
                if (c == 0) {
                    setCachedPagesLimit(attrWrapper.getIntegerValue());
                    return;
                } else if (c == 1) {
                    setOrientation(attrWrapper.getIntegerValue());
                    return;
                } else {
                    return;
                }
            }
        } else if (name.equals("orientation")) {
            c = 1;
            if (c == 0) {
            }
        }
        c = 65535;
        if (c == 0) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.StackLayout, ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeViewPagerGetHandle();
        }
    }

    public void setProvider(PageSliderProvider pageSliderProvider) {
        nativeSetAdapter(this.mNativeViewPtr, pageSliderProvider);
        this.mPageSliderProvider = pageSliderProvider;
    }

    public PageSliderProvider getProvider() {
        return this.mPageSliderProvider;
    }

    public int getCurrentPage() {
        return nativeGetCurrentItem(this.mNativeViewPtr);
    }

    public void addPageChangeListener(PageChangedListener pageChangedListener) {
        this.mPageChangePublisher.registerSubscriber(pageChangedListener);
    }

    public void removePageChangeListener(PageChangedListener pageChangedListener) {
        this.mPageChangePublisher.unregisterSubscriber((PageChangePublisher) pageChangedListener);
    }

    public int getCurrentSlidingState() {
        return nativeGetCurrentScrollState(this.mNativeViewPtr);
    }

    public void setOrientation(int i) {
        nativeSetOrientation(this.mNativeViewPtr, i);
    }

    public int getOrientation() {
        return nativeGetOrientation(this.mNativeViewPtr);
    }

    public void setCachedPagesLimit(int i) {
        nativeSetOffscreenPageLimit(this.mNativeViewPtr, i);
    }

    public int getCachedPagesLimit() {
        return nativeGetOffscreenPageLimit(this.mNativeViewPtr);
    }

    public void setCurrentPage(int i) {
        nativeSetCurrentItem(this.mNativeViewPtr, i, true);
    }

    public void setCurrentPage(int i, boolean z) {
        nativeSetCurrentItem(this.mNativeViewPtr, i, z);
    }

    public void setSlidingPossible(boolean z) {
        nativeSetSlidingEnabled(this.mNativeViewPtr, z);
    }

    public boolean getSlidingPossible() {
        return nativeGetSlidingEnabled(this.mNativeViewPtr);
    }

    public void setReboundEffect(boolean z) {
        nativeSetReboundEffect(this.mNativeViewPtr, z);
    }

    public boolean getReboundEffect() {
        return nativeGetReboundEffect(this.mNativeViewPtr);
    }

    /* access modifiers changed from: package-private */
    public void setPageSwitchTime(int i) {
        nativeSetPageSwitchTime(this.mNativeViewPtr, i);
    }

    /* access modifiers changed from: package-private */
    public int getPageSwitchTime() {
        return nativeGetPageSwitchTime(this.mNativeViewPtr);
    }

    static class PageChangePublisher extends Publisher<PageChangedListener> {
        PageChangePublisher() {
        }

        /* access modifiers changed from: package-private */
        public void notifyPageScrolled(int i, float f, int i2) {
            for (int size = this.mSubscribers.size() - 1; size >= 0; size--) {
                ((PageChangedListener) this.mSubscribers.get(size)).onPageScrolled(i, f, i2);
            }
        }

        /* access modifiers changed from: package-private */
        public void notifyPageScrollStateChanged(int i) {
            for (int size = this.mSubscribers.size() - 1; size >= 0; size--) {
                ((PageChangedListener) this.mSubscribers.get(size)).onPageScrollStateChanged(i);
            }
        }

        /* access modifiers changed from: package-private */
        public void notifyPageSelected(int i) {
            for (int size = this.mSubscribers.size() - 1; size >= 0; size--) {
                ((PageChangedListener) this.mSubscribers.get(size)).onPageSelected(i);
            }
        }
    }

    private void notifyPageScrolled(int i, float f, int i2) {
        this.mPageChangePublisher.notifyPageScrolled(i, f, i2);
    }

    private void notifyPageScrollStateChanged(int i) {
        this.mPageChangePublisher.notifyPageScrollStateChanged(i);
    }

    private void notifyPageSelected(int i) {
        this.mPageChangePublisher.notifyPageSelected(i);
    }
}
