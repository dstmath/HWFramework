package ohos.agp.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.RelativeLayout;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.DependentLayoutAttrsConstants;
import ohos.app.Context;

public class RelativeLayout extends ComponentContainer {
    public static final int ABOVE = 2;
    public static final int ALIGN_BASELINE = 4;
    public static final int ALIGN_BOTTOM = 8;
    public static final int ALIGN_END = 19;
    public static final int ALIGN_LEFT = 5;
    public static final int ALIGN_PARENT_BOTTOM = 12;
    public static final int ALIGN_PARENT_END = 21;
    public static final int ALIGN_PARENT_LEFT = 9;
    public static final int ALIGN_PARENT_RIGHT = 11;
    public static final int ALIGN_PARENT_START = 20;
    public static final int ALIGN_PARENT_TOP = 10;
    public static final int ALIGN_RIGHT = 7;
    public static final int ALIGN_START = 18;
    public static final int ALIGN_TOP = 6;
    public static final int BELOW = 3;
    public static final int CENTER_HORIZONTAL = 14;
    public static final int CENTER_IN_PARENT = 13;
    public static final int CENTER_VERTICAL = 15;
    public static final int END_OF = 17;
    public static final int LEFT_OF = 0;
    public static final int RIGHT_OF = 1;
    public static final int START_OF = 16;
    public static final int TRUE = -1;
    private static final int VERB_COUNT = 22;

    private native int nativeGetGravity(long j);

    private native int nativeGetIgnoreGravity(long j);

    private native long nativeGetRelativeLayoutHandle();

    private native void nativeSetGravity(long j, int i);

    private native void nativeSetIgnoreGravity(long j, int i);

    public RelativeLayout(Context context) {
        this(context, null);
    }

    public RelativeLayout(Context context, AttrSet attrSet) {
        this(context, attrSet, "RelativeLayoutDefaultStyle");
    }

    public RelativeLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new DependentLayoutAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        this.mNativeViewPtr = nativeGetRelativeLayoutHandle();
    }

    public int getIgnoreGravity() {
        return nativeGetIgnoreGravity(this.mNativeViewPtr);
    }

    public void setIgnoreGravity(int i) {
        nativeSetIgnoreGravity(this.mNativeViewPtr, i);
    }

    public int getGravity() {
        return nativeGetGravity(this.mNativeViewPtr);
    }

    public void setGravity(int i) {
        nativeSetGravity(this.mNativeViewPtr, i);
    }

    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public ComponentContainer.LayoutConfig verifyLayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
        if (layoutConfig instanceof LayoutParams) {
            return layoutConfig;
        }
        return new LayoutParams(layoutConfig);
    }

    @Override // ohos.agp.components.ComponentContainer
    public ComponentContainer.LayoutConfig createLayoutConfig(Context context, AttrSet attrSet) {
        return new LayoutParams(context, attrSet);
    }

    public static class LayoutParams extends ComponentContainer.LayoutConfig {
        private static Map<String, BiConsumer<LayoutParams, Attr>> sAttrMethodMap = new HashMap<String, BiConsumer<LayoutParams, Attr>>() {
            /* class ohos.agp.components.RelativeLayout.LayoutParams.AnonymousClass1 */

            {
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.LEFT_OF, $$Lambda$RelativeLayout$LayoutParams$1$nBwSUnpXCT0GcQa_X27LzMriWx8.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.RIGHT_OF, $$Lambda$RelativeLayout$LayoutParams$1$d0xdF7q3DxoR3kHSQ8ub8iFXmg.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ABOVE, $$Lambda$RelativeLayout$LayoutParams$1$0IowLR5wI34CRkmOYtbY2e2fFw.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.BELOW, $$Lambda$RelativeLayout$LayoutParams$1$UwMWHklWr6yI0KwhSMyiGpTLicU.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_BASELINE, $$Lambda$RelativeLayout$LayoutParams$1$oNdVKWY4AgIPIjmZNh63hGrSas.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_LEFT, $$Lambda$RelativeLayout$LayoutParams$1$YTp5jYiyPTHPz_7OWAEqqiLoKqE.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_TOP, $$Lambda$RelativeLayout$LayoutParams$1$CeBtzHL5pV2P4gU_zyB0xAKhYY.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_RIGHT, $$Lambda$RelativeLayout$LayoutParams$1$uWYgoTHVgbvR_LKlE_WG7sKguRQ.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_BOTTOM, $$Lambda$RelativeLayout$LayoutParams$1$v9GytlBpnBUrgN64EBKLrJ2_MQ.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_LEFT, $$Lambda$RelativeLayout$LayoutParams$1$sY2Ay38KdPtJRAsoa2H4UU8w1A.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_TOP, $$Lambda$RelativeLayout$LayoutParams$1$OUd4RxPbmtm_TJmLe8iABYHUv9g.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_RIGHT, $$Lambda$RelativeLayout$LayoutParams$1$jCwW_2AwIEmfPTlZRyV6ETwzQQ.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_BOTTOM, $$Lambda$RelativeLayout$LayoutParams$1$yg1LsKmHSbiFyHChM7CgGWlCuMc.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.CENTER_IN_PARENT, $$Lambda$RelativeLayout$LayoutParams$1$eS8PlLWH5W9vrRQvdahxtp57JM.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.CENTER_HORIZONTAL, $$Lambda$RelativeLayout$LayoutParams$1$cOlgkLecaY5l2r_9RzeSZiwxfc.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.CENTER_VERTICAL, $$Lambda$RelativeLayout$LayoutParams$1$35NFFrQgylU9EhwjkdFwVaDa6NU.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.START_OF, $$Lambda$RelativeLayout$LayoutParams$1$uT2kUC6xb34BzI4KNYepfD7TeD4.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.END_OF, $$Lambda$RelativeLayout$LayoutParams$1$ObikwrLHezO94wPQYba9Ryvzg8s.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_START, $$Lambda$RelativeLayout$LayoutParams$1$OqJrSXU0tqmqw7CMbhvkGh6ZLzA.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_END, $$Lambda$RelativeLayout$LayoutParams$1$1jhF1oVGlFUk8g0UuYHXMjcWDHs.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_START, $$Lambda$RelativeLayout$LayoutParams$1$BFxG9DSedbapGJs4csz1xA4atck.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_END, $$Lambda$RelativeLayout$LayoutParams$1$wu25U7ZrsSZqtupfk52oHUxzHQ.INSTANCE);
            }

            static /* synthetic */ void lambda$new$9(LayoutParams layoutParams, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutParams.addRule(9);
                } else {
                    layoutParams.removeRule(9);
                }
            }

            static /* synthetic */ void lambda$new$10(LayoutParams layoutParams, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutParams.addRule(10);
                } else {
                    layoutParams.removeRule(10);
                }
            }

            static /* synthetic */ void lambda$new$11(LayoutParams layoutParams, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutParams.addRule(11);
                } else {
                    layoutParams.removeRule(11);
                }
            }

            static /* synthetic */ void lambda$new$12(LayoutParams layoutParams, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutParams.addRule(12);
                } else {
                    layoutParams.removeRule(12);
                }
            }

            static /* synthetic */ void lambda$new$13(LayoutParams layoutParams, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutParams.addRule(13);
                } else {
                    layoutParams.removeRule(13);
                }
            }

            static /* synthetic */ void lambda$new$14(LayoutParams layoutParams, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutParams.addRule(14);
                } else {
                    layoutParams.removeRule(14);
                }
            }

            static /* synthetic */ void lambda$new$15(LayoutParams layoutParams, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutParams.addRule(15);
                } else {
                    layoutParams.removeRule(15);
                }
            }

            static /* synthetic */ void lambda$new$20(LayoutParams layoutParams, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutParams.addRule(20);
                } else {
                    layoutParams.removeRule(20);
                }
            }

            static /* synthetic */ void lambda$new$21(LayoutParams layoutParams, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutParams.addRule(21);
                } else {
                    layoutParams.removeRule(21);
                }
            }
        };
        private int[] mInitialRules;
        private boolean mNeedsLayoutResolution;
        private int[] mRules;
        private boolean mRulesChanged;

        private boolean isRelativeRule(int i) {
            return i == 16 || i == 17 || i == 18 || i == 19 || i == 20 || i == 21;
        }

        private native void nativeSetRelativeLayoutParams(long j, int[] iArr, int[] iArr2);

        public LayoutParams() {
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mNeedsLayoutResolution = false;
            this.mRulesChanged = false;
        }

        public LayoutParams(Context context, AttrSet attrSet) {
            super(context, attrSet);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mNeedsLayoutResolution = false;
            this.mRulesChanged = false;
            for (int i = 0; i < attrSet.getLength(); i++) {
                attrSet.getAttr(i).ifPresent(new Consumer() {
                    /* class ohos.agp.components.$$Lambda$RelativeLayout$LayoutParams$onJM7Fdmt8Y2GbZzTLliohC60 */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        RelativeLayout.LayoutParams.this.lambda$new$1$RelativeLayout$LayoutParams((Attr) obj);
                    }
                });
            }
            this.mRulesChanged = true;
            System.arraycopy(this.mRules, 0, this.mInitialRules, 0, 22);
        }

        public /* synthetic */ void lambda$new$1$RelativeLayout$LayoutParams(Attr attr) {
            Optional.ofNullable(sAttrMethodMap.get(attr.getName())).ifPresent(new Consumer(attr) {
                /* class ohos.agp.components.$$Lambda$RelativeLayout$LayoutParams$QT2GvkImyItEq_Gl5nbjs8Uorco */
                private final /* synthetic */ Attr f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    RelativeLayout.LayoutParams.this.lambda$new$0$RelativeLayout$LayoutParams(this.f$1, (BiConsumer) obj);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$RelativeLayout$LayoutParams(Attr attr, BiConsumer biConsumer) {
            biConsumer.accept(this, attr);
        }

        public LayoutParams(int i, int i2) {
            super(i, i2);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mNeedsLayoutResolution = false;
            this.mRulesChanged = false;
        }

        public LayoutParams(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mNeedsLayoutResolution = false;
            this.mRulesChanged = false;
        }

        public LayoutParams(LayoutParams layoutParams) {
            super(layoutParams);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mNeedsLayoutResolution = false;
            this.mRulesChanged = false;
            this.mRulesChanged = layoutParams.mRulesChanged;
            System.arraycopy(layoutParams.mRules, 0, this.mRules, 0, 22);
            System.arraycopy(layoutParams.mInitialRules, 0, this.mInitialRules, 0, 22);
        }

        @Override // ohos.agp.components.ComponentContainer.LayoutConfig, java.lang.Object
        public Object clone() throws CloneNotSupportedException {
            Object clone = super.clone();
            if (clone instanceof LayoutParams) {
                LayoutParams layoutParams = (LayoutParams) clone;
                layoutParams.mRules = new int[22];
                System.arraycopy(this.mRules, 0, layoutParams.mRules, 0, 22);
            }
            return clone;
        }

        public void addRule(int i, int i2) {
            if (i < 22 && i >= 0) {
                if (!this.mNeedsLayoutResolution && isRelativeRule(i) && this.mInitialRules[i] != 0 && i2 == 0) {
                    this.mNeedsLayoutResolution = true;
                }
                this.mRules[i] = i2;
                this.mInitialRules[i] = i2;
                this.mRulesChanged = true;
            }
        }

        public void addRule(int i) {
            addRule(i, -1);
        }

        public void removeRule(int i) {
            addRule(i, 0);
        }

        public int getRule(int i) {
            if (i < 0 || i >= 22) {
                return -1;
            }
            return this.mRules[i];
        }

        public int[] getRules() {
            return (int[]) this.mRules.clone();
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.components.ComponentContainer.LayoutConfig
        public void resolveLayoutDirection(Component.LayoutDirection layoutDirection) {
            if (shouldResolveLayoutDirection(layoutDirection)) {
                resolveRules(layoutDirection);
            }
            super.resolveLayoutDirection(layoutDirection);
        }

        private boolean shouldResolveLayoutDirection(Component.LayoutDirection layoutDirection) {
            return (this.mNeedsLayoutResolution || hasRelativeRules()) && (this.mRulesChanged || layoutDirection != getLayoutDirection());
        }

        private void resolveRules(Component.LayoutDirection layoutDirection) {
            char c = layoutDirection == Component.LayoutDirection.RTL ? (char) 1 : 0;
            System.arraycopy(this.mInitialRules, 0, this.mRules, 0, 22);
            int[] iArr = this.mRules;
            char c2 = 7;
            if (!(iArr[18] == 0 && iArr[19] == 0)) {
                int[] iArr2 = this.mRules;
                if (!(iArr2[5] == 0 && iArr2[7] == 0)) {
                    int[] iArr3 = this.mRules;
                    iArr3[5] = 0;
                    iArr3[7] = 0;
                }
            }
            int[] iArr4 = this.mRules;
            if (iArr4[18] != 0) {
                char c3 = c != 0 ? (char) 7 : 5;
                int[] iArr5 = this.mRules;
                iArr4[c3] = iArr5[18];
                iArr5[18] = 0;
            }
            int[] iArr6 = this.mRules;
            if (iArr6[19] != 0) {
                if (c != 0) {
                    c2 = 5;
                }
                int[] iArr7 = this.mRules;
                iArr6[c2] = iArr7[19];
                iArr7[19] = 0;
            }
            int[] iArr8 = this.mRules;
            if (!(iArr8[16] == 0 && iArr8[17] == 0)) {
                int[] iArr9 = this.mRules;
                if (!(iArr9[0] == 0 && iArr9[1] == 0)) {
                    int[] iArr10 = this.mRules;
                    iArr10[0] = 0;
                    iArr10[1] = 0;
                }
            }
            int[] iArr11 = this.mRules;
            if (iArr11[16] != 0) {
                iArr11[c] = iArr11[16];
                iArr11[16] = 0;
            }
            int[] iArr12 = this.mRules;
            if (iArr12[17] != 0) {
                iArr12[c ^ 1] = iArr12[17];
                iArr12[17] = 0;
            }
            int[] iArr13 = this.mRules;
            char c4 = 11;
            if (!(iArr13[20] == 0 && iArr13[21] == 0)) {
                int[] iArr14 = this.mRules;
                if (!(iArr14[9] == 0 && iArr14[11] == 0)) {
                    int[] iArr15 = this.mRules;
                    iArr15[9] = 0;
                    iArr15[11] = 0;
                }
            }
            int[] iArr16 = this.mRules;
            if (iArr16[20] != 0) {
                char c5 = c != 0 ? (char) 11 : '\t';
                int[] iArr17 = this.mRules;
                iArr16[c5] = iArr17[20];
                iArr17[20] = 0;
            }
            int[] iArr18 = this.mRules;
            if (iArr18[21] != 0) {
                if (c != 0) {
                    c4 = '\t';
                }
                int[] iArr19 = this.mRules;
                iArr18[c4] = iArr19[21];
                iArr19[21] = 0;
            }
            this.mRulesChanged = false;
            this.mNeedsLayoutResolution = false;
        }

        private boolean hasRelativeRules() {
            int[] iArr = this.mInitialRules;
            return (iArr[16] == 0 && iArr[17] == 0 && iArr[18] == 0 && iArr[19] == 0 && iArr[20] == 0 && iArr[21] == 0) ? false : true;
        }

        @Override // ohos.agp.components.ComponentContainer.LayoutConfig
        public void applyToComponent(Component component) {
            nativeSetRelativeLayoutParams(component.getNativeViewPtr(), getRules(), new int[]{this.width, this.height, this.leftMargin, this.topMargin, this.rightMargin, this.bottomMargin});
        }
    }
}
