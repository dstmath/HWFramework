package ohos.agp.components;

import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.styles.attributes.ViewAttrsConstants;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.app.Context;
import ohos.global.configuration.Configuration;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public abstract class ComponentContainer extends Component implements ComponentParent {
    public static final int FOCUS_AFTER_DESCENDANTS = 0;
    public static final int FOCUS_BEFORE_DESCENDANTS = 1;
    public static final int FOCUS_BLOCK_DESCENDANTS = 2;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_ComponentContainer");
    private CopyOnWriteArrayList<Component> mChildren;

    private native void nativeBringToFront(long j, long j2);

    private native boolean nativeGetAutoLayout(long j);

    private static native int nativeGetChildMeasureSpec(int i, int i2, int i3);

    private native int nativeGetDescendantFocusability(long j);

    private native long nativeGetViewGroupHandle();

    private native void nativeRemoveAllViews(long j);

    private native void nativeRemoveView(long j, long j2);

    private native void nativeRemoveViews(long j, int i, int i2);

    private native void nativeSetAutoLayout(long j, boolean z, int i);

    private native void nativeSetDescendantFocusability(long j, int i);

    private native void nativeSetLayoutTransition(long j, long j2);

    private native void nativeaddComponent(long j, long j2, int i);

    protected static class ComponentContainerCleaner extends Component.ComponentCleaner {
        private native void nativeCheckViewRootBeforeRelease(long j);

        ComponentContainerCleaner(long j) {
            super(j);
        }

        @Override // ohos.agp.components.Component.ComponentCleaner, ohos.agp.utils.MemoryCleaner
        public void run() {
            if (this.mNativePtr != 0) {
                nativeCheckViewRootBeforeRelease(this.mNativePtr);
            }
            super.run();
        }
    }

    public ComponentContainer(Context context) {
        this(context, null);
    }

    public ComponentContainer(Context context, AttrSet attrSet) {
        this(context, attrSet, null);
    }

    public ComponentContainer(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
        this.mChildren = new CopyOnWriteArrayList<>();
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetViewGroupHandle();
        }
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public void registerCleaner() {
        MemoryCleanerRegistry.getInstance().registerWithNativeBind(this, new ComponentContainerCleaner(this.mNativeViewPtr), this.mNativeViewPtr);
    }

    @Override // ohos.agp.components.ComponentParent
    public LayoutConfig verifyLayoutConfig(LayoutConfig layoutConfig) {
        return layoutConfig == null ? new LayoutConfig() : layoutConfig;
    }

    public LayoutConfig createLayoutConfig(Context context, AttrSet attrSet) {
        if (this.mLayoutConfig != null) {
            return this.mLayoutConfig;
        }
        return new LayoutConfig(context, attrSet);
    }

    @Override // ohos.agp.components.Component
    public Component findComponentById(int i) {
        if (getId() == i) {
            return this;
        }
        Iterator<Component> it = this.mChildren.iterator();
        while (it.hasNext()) {
            Component findComponentById = it.next().findComponentById(i);
            if (findComponentById != null) {
                return findComponentById;
            }
        }
        return null;
    }

    public void addComponent(Component component) {
        addComponent(component, -1);
    }

    public void addComponent(Component component, int i, int i2) {
        component.setLayoutConfig(new LayoutConfig(i, i2));
        addComponent(component, -1);
    }

    public void addComponent(Component component, LayoutConfig layoutConfig) {
        component.setLayoutConfig(layoutConfig);
        addComponent(component, -1);
    }

    public void addComponent(Component component, int i, LayoutConfig layoutConfig) {
        component.setLayoutConfig(layoutConfig);
        addComponent(component, i);
    }

    public void addComponent(Component component, int i) {
        if (component.getNativeViewPtr() != 0) {
            if (i < 0) {
                i = this.mChildren.size();
            } else if (i > this.mChildren.size()) {
                throw new IllegalArgumentException("Invalid index supplied to " + component);
            }
            component.setLayoutConfig(verifyLayoutConfig(component.getLayoutConfig()));
            component.assignParent(this);
            this.mChildren.add(i, component);
            nativeaddComponent(this.mNativeViewPtr, component.getNativeViewPtr(), i);
            return;
        }
        throw new IllegalStateException("View in layout has already been released.");
    }

    public Component getComponentAt(int i) {
        if (i < 0 || i >= this.mChildren.size()) {
            return null;
        }
        return this.mChildren.get(i);
    }

    @Override // ohos.agp.components.ComponentParent
    public int indexOfChild(Component component) {
        int indexOf = this.mChildren.indexOf(component);
        if (indexOf == -1) {
            HiLog.info(TAG, "can not find child.", new Object[0]);
        }
        return indexOf;
    }

    @Override // ohos.agp.components.ComponentParent
    public void removeComponent(Component component) {
        if (component.getNativeViewPtr() == 0) {
            throw new IllegalStateException("View in layout has already been released.");
        } else if (this.mChildren.removeIf(new Predicate() {
            /* class ohos.agp.components.$$Lambda$ComponentContainer$xpbTwrBJXe4D_TI2HgTvZyVypNI */

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return ((Component) obj).equals(Component.this);
            }
        })) {
            component.assignParent(null);
            component.notifyAllForRemove();
            nativeRemoveView(this.mNativeViewPtr, component.getNativeViewPtr());
        }
    }

    @Override // ohos.agp.components.ComponentParent
    public void removeComponentAt(int i) {
        if (i < 0 || i >= this.mChildren.size()) {
            throw new IllegalStateException("Incorrect index for removeComponentAt.");
        }
        removeComponent(this.mChildren.get(i));
    }

    @Override // ohos.agp.components.ComponentParent
    public void removeComponents(int i, int i2) {
        if (i < 0 || i >= this.mChildren.size() || i2 < 0 || i + i2 >= this.mChildren.size()) {
            throw new IllegalStateException("Incorrect parameters for removeViews.");
        }
        for (int i3 = 0; i3 < i2; i3++) {
            Component remove = this.mChildren.remove(i);
            if (remove != null) {
                remove.assignParent(null);
                remove.notifyAllForRemove();
            }
        }
        nativeRemoveViews(this.mNativeViewPtr, i, i2);
    }

    public void removeAllComponents() {
        if (getChildCount() == 0) {
            HiLog.info(TAG, "removeAllComponents, child count is zero", new Object[0]);
            return;
        }
        Iterator<Component> it = this.mChildren.iterator();
        while (it.hasNext()) {
            Component next = it.next();
            next.assignParent(null);
            next.notifyAllForRemove();
        }
        this.mChildren.clear();
        nativeRemoveAllViews(this.mNativeViewPtr);
    }

    @Override // ohos.agp.components.ComponentParent
    public void bringChildToFront(Component component) {
        if (component.getNativeViewPtr() != 0) {
            int indexOfChild = indexOfChild(component);
            if (indexOfChild >= 0) {
                removeFromArray(indexOfChild);
                addInArray(component, this.mChildren.size());
                nativeBringToFront(this.mNativeViewPtr, component.getNativeViewPtr());
                component.mParent = this;
                return;
            }
            return;
        }
        throw new IllegalStateException("View in layout has already been released.");
    }

    private void removeFromArray(int i) {
        if (i < this.mChildren.size()) {
            this.mChildren.remove(i).mParent = null;
        }
    }

    private void addInArray(Component component, int i) {
        int size = this.mChildren.size();
        if (i == size) {
            this.mChildren.add(component);
        } else if (i < size) {
            this.mChildren.add(i, component);
        } else {
            HiLog.error(TAG, "invalid para.", new Object[0]);
        }
    }

    public int getChildCount() {
        return this.mChildren.size();
    }

    @Override // ohos.agp.components.Component
    public void dispatchConfigurationChanged(Configuration configuration) {
        super.dispatchConfigurationChanged(configuration);
        Iterator<Component> it = this.mChildren.iterator();
        while (it.hasNext()) {
            it.next().dispatchConfigurationChanged(configuration);
        }
    }

    public int getDescendantFocusability() {
        return nativeGetDescendantFocusability(this.mNativeViewPtr);
    }

    public void setDescendantFocusability(int i) {
        nativeSetDescendantFocusability(this.mNativeViewPtr, i);
    }

    public static int getChildMeasureSpec(int i, int i2, int i3) {
        return nativeGetChildMeasureSpec(i, i2, i3);
    }

    public void setAutoLayout(boolean z) {
        nativeSetAutoLayout(this.mNativeViewPtr, z, 0);
    }

    public void setAutoLayout(boolean z, int i) {
        nativeSetAutoLayout(this.mNativeViewPtr, z, i);
    }

    public boolean getAutoLayout() {
        return nativeGetAutoLayout(this.mNativeViewPtr);
    }

    @Override // ohos.agp.components.Component, ohos.agp.components.ComponentParent
    public boolean onDrag(Component component, DragEvent dragEvent) {
        boolean onDrag = super.onDrag(component, dragEvent);
        if (dragEvent.isBroadcast()) {
            Iterator<Component> it = this.mChildren.iterator();
            while (it.hasNext()) {
                if (it.next().onDrag(component, dragEvent)) {
                    onDrag = true;
                }
            }
        }
        return onDrag;
    }

    public void setLayoutTransition(LayoutTransition layoutTransition) {
        nativeSetLayoutTransition(this.mNativeViewPtr, layoutTransition.getNativePtr());
    }

    public static class LayoutConfig implements Cloneable {
        private static final int DEFAULT_MARGIN = 0;
        private static final int DEFAULT_MARGIN_RELATIVE = Integer.MIN_VALUE;
        public static final int MATCH_PARENT = -1;
        private static final int UNDEFINED_MARGIN = Integer.MIN_VALUE;
        public static final int WRAP_CONTENT = -2;
        public int bottomMargin;
        private int endMargin;
        public int height;
        private Component.LayoutDirection layoutDirection;
        public int leftMargin;
        private int leftMarginInitial;
        public int rightMargin;
        private int rightMarginInitial;
        private int startMargin;
        public int topMargin;
        public int width;

        private native void nativeSetMarginLayoutParams(long j, int[] iArr, boolean z);

        @Override // java.lang.Object
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        public LayoutConfig() {
            this(-2, -2);
        }

        public LayoutConfig(Context context, AttrSet attrSet) {
            this.startMargin = Integer.MIN_VALUE;
            this.endMargin = Integer.MIN_VALUE;
            this.leftMarginInitial = Integer.MIN_VALUE;
            this.rightMarginInitial = Integer.MIN_VALUE;
            this.layoutDirection = Component.LayoutDirection.LTR;
            attrSet.getAttr(ViewAttrsConstants.HEIGHT).ifPresent(new Consumer(context) {
                /* class ohos.agp.components.$$Lambda$ComponentContainer$LayoutConfig$2FCZJV_9LT1lC9py1XGY00btV9w */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ComponentContainer.LayoutConfig.this.lambda$new$0$ComponentContainer$LayoutConfig(this.f$1, (Attr) obj);
                }
            });
            attrSet.getAttr(ViewAttrsConstants.WIDTH).ifPresent(new Consumer(context) {
                /* class ohos.agp.components.$$Lambda$ComponentContainer$LayoutConfig$TCba_LluENNFNKv6BDvZ02iIgU */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ComponentContainer.LayoutConfig.this.lambda$new$1$ComponentContainer$LayoutConfig(this.f$1, (Attr) obj);
                }
            });
            attrSet.getAttr(ViewAttrsConstants.MARGIN).ifPresent(new Consumer(context) {
                /* class ohos.agp.components.$$Lambda$ComponentContainer$LayoutConfig$mbFkd28w9QLx9V0oxuc9BfTLAU */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ComponentContainer.LayoutConfig.this.lambda$new$2$ComponentContainer$LayoutConfig(this.f$1, (Attr) obj);
                }
            });
            attrSet.getAttr(ViewAttrsConstants.LEFT_MARGIN).ifPresent(new Consumer(context) {
                /* class ohos.agp.components.$$Lambda$ComponentContainer$LayoutConfig$Fu57Sr99Wx2Da1diYFWHt5TbWI */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ComponentContainer.LayoutConfig.this.lambda$new$3$ComponentContainer$LayoutConfig(this.f$1, (Attr) obj);
                }
            });
            attrSet.getAttr(ViewAttrsConstants.RIGHT_MARGIN).ifPresent(new Consumer(context) {
                /* class ohos.agp.components.$$Lambda$ComponentContainer$LayoutConfig$LirI4RDCnE_BYLpVXhAHXBVM3XI */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ComponentContainer.LayoutConfig.this.lambda$new$4$ComponentContainer$LayoutConfig(this.f$1, (Attr) obj);
                }
            });
            attrSet.getAttr(ViewAttrsConstants.TOP_MARGIN).ifPresent(new Consumer(context) {
                /* class ohos.agp.components.$$Lambda$ComponentContainer$LayoutConfig$tXwXlhpiD29jADtnAybeuU2l6o */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ComponentContainer.LayoutConfig.this.lambda$new$5$ComponentContainer$LayoutConfig(this.f$1, (Attr) obj);
                }
            });
            attrSet.getAttr(ViewAttrsConstants.BOTTOM_MARGIN).ifPresent(new Consumer(context) {
                /* class ohos.agp.components.$$Lambda$ComponentContainer$LayoutConfig$c0b2pV_Gxhcu_zVDvnwv_KfScE4 */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ComponentContainer.LayoutConfig.this.lambda$new$6$ComponentContainer$LayoutConfig(this.f$1, (Attr) obj);
                }
            });
            attrSet.getAttr(ViewAttrsConstants.START_MARGIN).ifPresent(new Consumer(context) {
                /* class ohos.agp.components.$$Lambda$ComponentContainer$LayoutConfig$qYy2GU0z11Wl5ANu9qioKWOaz4k */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ComponentContainer.LayoutConfig.this.lambda$new$7$ComponentContainer$LayoutConfig(this.f$1, (Attr) obj);
                }
            });
            attrSet.getAttr(ViewAttrsConstants.END_MARGIN).ifPresent(new Consumer(context) {
                /* class ohos.agp.components.$$Lambda$ComponentContainer$LayoutConfig$_IKfDZOQrOsuCXbta1v0DAuKTLw */
                private final /* synthetic */ Context f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ComponentContainer.LayoutConfig.this.lambda$new$8$ComponentContainer$LayoutConfig(this.f$1, (Attr) obj);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$ComponentContainer$LayoutConfig(Context context, Attr attr) {
            this.height = new AttrWrapper(context, attr).getDimensionValue();
        }

        public /* synthetic */ void lambda$new$1$ComponentContainer$LayoutConfig(Context context, Attr attr) {
            this.width = new AttrWrapper(context, attr).getDimensionValue();
        }

        public /* synthetic */ void lambda$new$2$ComponentContainer$LayoutConfig(Context context, Attr attr) {
            int dimensionValue = new AttrWrapper(context, attr).getDimensionValue();
            setMargins(dimensionValue, dimensionValue, dimensionValue, dimensionValue);
        }

        public /* synthetic */ void lambda$new$3$ComponentContainer$LayoutConfig(Context context, Attr attr) {
            setMargins(new AttrWrapper(context, attr).getDimensionValue(), this.topMargin, this.rightMargin, this.bottomMargin);
        }

        public /* synthetic */ void lambda$new$4$ComponentContainer$LayoutConfig(Context context, Attr attr) {
            setMargins(this.leftMargin, this.topMargin, new AttrWrapper(context, attr).getDimensionValue(), this.bottomMargin);
        }

        public /* synthetic */ void lambda$new$5$ComponentContainer$LayoutConfig(Context context, Attr attr) {
            setMargins(this.leftMargin, new AttrWrapper(context, attr).getDimensionValue(), this.rightMargin, this.bottomMargin);
        }

        public /* synthetic */ void lambda$new$6$ComponentContainer$LayoutConfig(Context context, Attr attr) {
            setMargins(this.leftMargin, this.topMargin, this.rightMargin, new AttrWrapper(context, attr).getDimensionValue());
        }

        public /* synthetic */ void lambda$new$7$ComponentContainer$LayoutConfig(Context context, Attr attr) {
            setMarginsRelative(new AttrWrapper(context, attr).getDimensionValue(), this.topMargin, getMarginEnd(), this.bottomMargin);
        }

        public /* synthetic */ void lambda$new$8$ComponentContainer$LayoutConfig(Context context, Attr attr) {
            setMarginsRelative(getMarginStart(), this.topMargin, new AttrWrapper(context, attr).getDimensionValue(), this.bottomMargin);
        }

        public LayoutConfig(int i, int i2) {
            this.startMargin = Integer.MIN_VALUE;
            this.endMargin = Integer.MIN_VALUE;
            this.leftMarginInitial = Integer.MIN_VALUE;
            this.rightMarginInitial = Integer.MIN_VALUE;
            this.layoutDirection = Component.LayoutDirection.LTR;
            this.width = i;
            this.height = i2;
        }

        public LayoutConfig(LayoutConfig layoutConfig) {
            this.startMargin = Integer.MIN_VALUE;
            this.endMargin = Integer.MIN_VALUE;
            this.leftMarginInitial = Integer.MIN_VALUE;
            this.rightMarginInitial = Integer.MIN_VALUE;
            this.layoutDirection = Component.LayoutDirection.LTR;
            if (layoutConfig == null) {
                this.width = -2;
                this.height = -2;
                return;
            }
            this.width = layoutConfig.width;
            this.height = layoutConfig.height;
            this.leftMargin = layoutConfig.leftMargin;
            this.topMargin = layoutConfig.topMargin;
            this.rightMargin = layoutConfig.rightMargin;
            this.bottomMargin = layoutConfig.bottomMargin;
            this.startMargin = layoutConfig.startMargin;
            this.endMargin = layoutConfig.endMargin;
            this.leftMarginInitial = layoutConfig.leftMarginInitial;
            this.rightMarginInitial = layoutConfig.rightMarginInitial;
            this.layoutDirection = layoutConfig.layoutDirection;
        }

        public void setMargins(int i, int i2, int i3, int i4) {
            this.startMargin = Integer.MIN_VALUE;
            this.endMargin = Integer.MIN_VALUE;
            this.leftMarginInitial = i;
            this.rightMarginInitial = i3;
            internalSetMargins(i, i2, i3, i4);
        }

        public void setMarginsRelative(int i, int i2, int i3, int i4) {
            this.startMargin = i;
            this.endMargin = i3;
            this.leftMarginInitial = getLayoutDirection() == Component.LayoutDirection.LTR ? i : i3;
            if (getLayoutDirection() == Component.LayoutDirection.LTR) {
                i = i3;
            }
            this.rightMarginInitial = i;
            internalSetMargins(this.leftMarginInitial, i2, this.rightMarginInitial, i4);
        }

        public int getMarginStart() {
            int i = this.startMargin;
            if (i != Integer.MIN_VALUE) {
                return i;
            }
            return getLayoutDirection() == Component.LayoutDirection.LTR ? this.leftMargin : this.rightMargin;
        }

        public int getMarginEnd() {
            int i = this.endMargin;
            if (i != Integer.MIN_VALUE) {
                return i;
            }
            return getLayoutDirection() == Component.LayoutDirection.LTR ? this.rightMargin : this.leftMargin;
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0019: APUT  (r0v1 int[]), (2 ??[int, float, short, byte, char]), (r1v3 int) */
        public void applyToComponent(Component component) {
            int[] iArr = new int[6];
            iArr[0] = this.width;
            iArr[1] = this.height;
            iArr[2] = isMarginsRelative() ? this.startMargin : this.leftMargin;
            iArr[3] = this.topMargin;
            iArr[4] = isMarginsRelative() ? this.endMargin : this.rightMargin;
            iArr[5] = this.bottomMargin;
            nativeSetMarginLayoutParams(component.getNativeViewPtr(), iArr, isMarginsRelative());
        }

        /* access modifiers changed from: protected */
        public void resolveLayoutDirection(Component.LayoutDirection layoutDirection2) {
            if (this.layoutDirection != layoutDirection2) {
                this.layoutDirection = layoutDirection2;
                resolveMargins();
            }
        }

        private void resolveMargins() {
            if (getLayoutDirection() == Component.LayoutDirection.RTL) {
                int i = this.startMargin;
                if (i == Integer.MIN_VALUE && (i = this.rightMarginInitial) == Integer.MIN_VALUE) {
                    i = this.rightMargin;
                }
                this.rightMargin = i;
                int i2 = this.endMargin;
                if (i2 == Integer.MIN_VALUE && (i2 = this.leftMarginInitial) == Integer.MIN_VALUE) {
                    i2 = this.leftMargin;
                }
                this.leftMargin = i2;
                return;
            }
            int i3 = this.startMargin;
            if (i3 == Integer.MIN_VALUE && (i3 = this.leftMarginInitial) == Integer.MIN_VALUE) {
                i3 = this.leftMargin;
            }
            this.leftMargin = i3;
            int i4 = this.endMargin;
            if (i4 == Integer.MIN_VALUE && (i4 = this.rightMarginInitial) == Integer.MIN_VALUE) {
                i4 = this.rightMargin;
            }
            this.rightMargin = i4;
        }

        private boolean isMarginsRelative() {
            return (this.startMargin == Integer.MIN_VALUE || this.endMargin == Integer.MIN_VALUE) ? false : true;
        }

        public Component.LayoutDirection getLayoutDirection() {
            return this.layoutDirection;
        }

        private void internalSetMargins(int i, int i2, int i3, int i4) {
            this.leftMargin = i;
            this.rightMargin = i3;
            this.topMargin = i2;
            this.bottomMargin = i4;
        }
    }
}
