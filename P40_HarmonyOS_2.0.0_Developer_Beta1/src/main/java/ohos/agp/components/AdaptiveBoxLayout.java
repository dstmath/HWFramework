package ohos.agp.components;

import java.util.function.Consumer;
import java.util.function.Predicate;
import ohos.agp.components.AdaptiveBoxLayout;
import ohos.agp.components.ComponentContainer;
import ohos.app.Context;

public class AdaptiveBoxLayout extends ComponentContainer {
    static /* synthetic */ boolean lambda$addAdaptiveRule$0(int i, Integer num) {
        return i >= 0;
    }

    private native void nativeAddAdaptiveRule(long j, int i, int i2, int i3);

    private native void nativeClearAdaptiveRules(long j);

    private native long nativeGetAdaptiveBoxLayoutHandle();

    private native void nativeremoveAdaptiveRule(long j, int i, int i2, int i3);

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetAdaptiveBoxLayoutHandle();
        }
    }

    public AdaptiveBoxLayout(Context context) {
        this(context, null);
    }

    public AdaptiveBoxLayout(Context context, AttrSet attrSet) {
        this(context, attrSet, "AdaptiveBoxLayoutDefaultStyle");
    }

    public AdaptiveBoxLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    public void addAdaptiveRule(int i, int i2, int i3) {
        validateParam(Integer.valueOf(i), new Predicate(i) {
            /* class ohos.agp.components.$$Lambda$AdaptiveBoxLayout$KsRDcDCEEEUVGTsIuVI1PCDtW8 */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return AdaptiveBoxLayout.lambda$addAdaptiveRule$0(this.f$0, (Integer) obj);
            }
        }, "width must not be negative");
        validateParam(Integer.valueOf(i2 - i), $$Lambda$AdaptiveBoxLayout$o_lQk6pKBgb8y0aY1FvvETYL2d4.INSTANCE, "maxWidth must be greater than minWidth");
        validateParam(Integer.valueOf(i3), $$Lambda$AdaptiveBoxLayout$FLXLbtc2x_9YrESM6Z0fdBk_cw.INSTANCE, "Columns must be positive");
        nativeAddAdaptiveRule(this.mNativeViewPtr, i, i2, i3);
    }

    static /* synthetic */ boolean lambda$addAdaptiveRule$1(Integer num) {
        return num.intValue() > 0;
    }

    static /* synthetic */ boolean lambda$addAdaptiveRule$2(Integer num) {
        return num.intValue() > 0;
    }

    public void clearAdaptiveRules() {
        nativeClearAdaptiveRules(this.mNativeViewPtr);
    }

    public void removeAdaptiveRule(int i, int i2, int i3) {
        nativeremoveAdaptiveRule(this.mNativeViewPtr, i, i2, i3);
    }

    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public ComponentContainer.LayoutConfig verifyLayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
        if (layoutConfig instanceof LayoutConfig) {
            return layoutConfig;
        }
        return new LayoutConfig(layoutConfig);
    }

    @Override // ohos.agp.components.ComponentContainer
    public ComponentContainer.LayoutConfig createLayoutConfig(Context context, AttrSet attrSet) {
        return new LayoutConfig(context, attrSet);
    }

    public static class LayoutConfig extends ComponentContainer.LayoutConfig {
        public int alignment = 17;

        private native void nativeSetAdaptiveBoxLayoutParams(long j, int[] iArr, boolean z);

        public LayoutConfig() {
        }

        public LayoutConfig(int i, int i2) {
            super(i, i2);
        }

        public LayoutConfig(int i, int i2, int i3) {
            super(i, i2);
            this.alignment = i3;
        }

        public LayoutConfig(Context context, AttrSet attrSet) {
            super(context, attrSet);
            attrSet.getAttr("layout_alignment").ifPresent(new Consumer() {
                /* class ohos.agp.components.$$Lambda$AdaptiveBoxLayout$LayoutConfig$w0CSVTVp9zouMdU6rpPJ4vGGxmA */

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    AdaptiveBoxLayout.LayoutConfig.this.lambda$new$0$AdaptiveBoxLayout$LayoutConfig((Attr) obj);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$AdaptiveBoxLayout$LayoutConfig(Attr attr) {
            this.alignment = attr.getIntegerValue();
        }

        public LayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
        }

        public LayoutConfig(LayoutConfig layoutConfig) {
            super(layoutConfig);
            this.alignment = layoutConfig.alignment;
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x001d: APUT  (r0v1 int[]), (2 ??[int, float, short, byte, char]), (r1v3 int) */
        @Override // ohos.agp.components.ComponentContainer.LayoutConfig
        public void applyToComponent(Component component) {
            int[] iArr = new int[7];
            iArr[0] = this.width;
            iArr[1] = this.height;
            iArr[2] = isMarginsRelative() ? getHorizontalStartMargin() : getMarginLeft();
            iArr[3] = getMarginTop();
            iArr[4] = isMarginsRelative() ? getHorizontalEndMargin() : getMarginRight();
            iArr[5] = getMarginBottom();
            iArr[6] = this.alignment;
            nativeSetAdaptiveBoxLayoutParams(component.getNativeViewPtr(), iArr, isMarginsRelative());
            super.applyToComponent(component);
        }
    }
}
