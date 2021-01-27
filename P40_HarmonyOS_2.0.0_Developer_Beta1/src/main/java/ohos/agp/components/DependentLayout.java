package ohos.agp.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.DependentLayout;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.DependentLayoutAttrsConstants;
import ohos.app.Context;

public class DependentLayout extends ComponentContainer {
    private native int nativeGetAlignment(long j);

    private native long nativeGetHandle();

    private native int nativeGetIgnoreAlignment(long j);

    private native void nativeSetAlignment(long j, int i);

    private native void nativeSetIgnoreAlignment(long j, int i);

    public DependentLayout(Context context) {
        this(context, null);
    }

    public DependentLayout(Context context, AttrSet attrSet) {
        this(context, attrSet, "DependentLayoutDefaultStyle");
    }

    public DependentLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = AttrHelper.getDependentLayoutAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        this.mNativeViewPtr = nativeGetHandle();
    }

    public int getIgnoreAlignment() {
        return nativeGetIgnoreAlignment(this.mNativeViewPtr);
    }

    public void setIgnoreAlignment(int i) {
        nativeSetIgnoreAlignment(this.mNativeViewPtr, i);
    }

    public int getIgnoreGravity() {
        return nativeGetIgnoreAlignment(this.mNativeViewPtr);
    }

    public void setIgnoreGravity(int i) {
        nativeSetIgnoreAlignment(this.mNativeViewPtr, i);
    }

    public int getAlignment() {
        return nativeGetAlignment(this.mNativeViewPtr);
    }

    public void setAlignment(int i) {
        nativeSetAlignment(this.mNativeViewPtr, i);
    }

    public int getGravity() {
        return nativeGetAlignment(this.mNativeViewPtr);
    }

    public void setGravity(int i) {
        nativeSetAlignment(this.mNativeViewPtr, i);
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
        public static final int ABOVE = 0;
        public static final int ALIGN_BASELINE = 8;
        public static final int ALIGN_BOTTOM = 5;
        public static final int ALIGN_END = 19;
        public static final int ALIGN_LEFT = 6;
        public static final int ALIGN_PARENT_BOTTOM = 10;
        public static final int ALIGN_PARENT_END = 21;
        public static final int ALIGN_PARENT_LEFT = 11;
        public static final int ALIGN_PARENT_RIGHT = 12;
        public static final int ALIGN_PARENT_START = 20;
        public static final int ALIGN_PARENT_TOP = 9;
        public static final int ALIGN_RIGHT = 7;
        public static final int ALIGN_START = 18;
        public static final int ALIGN_TOP = 4;
        private static final Map<String, BiConsumer<LayoutConfig, Attr>> ATTR_METHOD_MAP = new HashMap<String, BiConsumer<LayoutConfig, Attr>>() {
            /* class ohos.agp.components.DependentLayout.LayoutConfig.AnonymousClass1 */

            {
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.LEFT_OF, $$Lambda$DependentLayout$LayoutConfig$1$kEWrbfMvq5Rrw9Qz4yZIuwUcc.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.RIGHT_OF, $$Lambda$DependentLayout$LayoutConfig$1$UmgNGA8FeIwpDuBm29KNQhnE.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ABOVE, $$Lambda$DependentLayout$LayoutConfig$1$FVmI7BkM9UPj0Co1j6NWtpSMZ6s.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.BELOW, $$Lambda$DependentLayout$LayoutConfig$1$o6LCSRhqV8p2_IzombQ8TvHUsg.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_BASELINE, $$Lambda$DependentLayout$LayoutConfig$1$7ng5af__CTVnlyA4XukzFsrPtM.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_LEFT, $$Lambda$DependentLayout$LayoutConfig$1$SFcJABAGTpKD9m1RCyCyPV_acg.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_TOP, $$Lambda$DependentLayout$LayoutConfig$1$UJkFpfr0rUbXI0DuKsdjLwExc60.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_RIGHT, $$Lambda$DependentLayout$LayoutConfig$1$PFHpG9pVmOLffVkVsynBGq8EAuA.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_BOTTOM, $$Lambda$DependentLayout$LayoutConfig$1$2ptcKCK0m_rYSD_hlOXpnSdJYPw.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_LEFT, $$Lambda$DependentLayout$LayoutConfig$1$Lkdwnib1ZLsEL2HUevUekWvjJs.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_TOP, $$Lambda$DependentLayout$LayoutConfig$1$c3ebe4ncFmKbOLFgrJ5MDyLyc4k.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_RIGHT, $$Lambda$DependentLayout$LayoutConfig$1$f4T1lqQkSbjENA6uV5JjwX09cas.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_BOTTOM, $$Lambda$DependentLayout$LayoutConfig$1$Rg2nhOdu5hf3ZtrzIPJ4ktKULSw.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.CENTER_IN_PARENT, $$Lambda$DependentLayout$LayoutConfig$1$tWOACVVDIZVzjTdlZ5CIvl87t6U.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.HORIZONTAL_CENTER, $$Lambda$DependentLayout$LayoutConfig$1$UEVNaOMIiTm42Tnkf7lGgR4Jtaw.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.VERTICAL_CENTER, $$Lambda$DependentLayout$LayoutConfig$1$w14wsPiki5CjBb5CHVZYmWqjRCk.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.START_OF, $$Lambda$DependentLayout$LayoutConfig$1$Q7vG5T92BYB1x3oGYonupntJTU.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.END_OF, $$Lambda$DependentLayout$LayoutConfig$1$7d90q4vMFbXutLAxWTuqmLB1qYc.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_START, $$Lambda$DependentLayout$LayoutConfig$1$yzObWzN7J2Hz41kEofzo5qbZWvQ.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_END, $$Lambda$DependentLayout$LayoutConfig$1$PuNRzCLzIo2Gdu2K4Qt2U3r31os.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_START, $$Lambda$DependentLayout$LayoutConfig$1$WfSOsFvRy1o3GVIAUkYyXrhvtPg.INSTANCE);
                put(DependentLayoutAttrsConstants.LayoutConfigAttrsConstants.ALIGN_PARENT_END, $$Lambda$DependentLayout$LayoutConfig$1$zjLDRhzLUZuYFCGAgMnKa_amO8.INSTANCE);
            }

            static /* synthetic */ void lambda$new$9(LayoutConfig layoutConfig, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutConfig.addRule(11);
                } else {
                    layoutConfig.removeRule(11);
                }
            }

            static /* synthetic */ void lambda$new$10(LayoutConfig layoutConfig, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutConfig.addRule(9);
                } else {
                    layoutConfig.removeRule(9);
                }
            }

            static /* synthetic */ void lambda$new$11(LayoutConfig layoutConfig, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutConfig.addRule(12);
                } else {
                    layoutConfig.removeRule(12);
                }
            }

            static /* synthetic */ void lambda$new$12(LayoutConfig layoutConfig, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutConfig.addRule(10);
                } else {
                    layoutConfig.removeRule(10);
                }
            }

            static /* synthetic */ void lambda$new$13(LayoutConfig layoutConfig, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutConfig.addRule(15);
                } else {
                    layoutConfig.removeRule(15);
                }
            }

            static /* synthetic */ void lambda$new$14(LayoutConfig layoutConfig, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutConfig.addRule(13);
                } else {
                    layoutConfig.removeRule(13);
                }
            }

            static /* synthetic */ void lambda$new$15(LayoutConfig layoutConfig, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutConfig.addRule(14);
                } else {
                    layoutConfig.removeRule(14);
                }
            }

            static /* synthetic */ void lambda$new$20(LayoutConfig layoutConfig, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutConfig.addRule(20);
                } else {
                    layoutConfig.removeRule(20);
                }
            }

            static /* synthetic */ void lambda$new$21(LayoutConfig layoutConfig, Attr attr) {
                if (attr.getBoolValue()) {
                    layoutConfig.addRule(21);
                } else {
                    layoutConfig.removeRule(21);
                }
            }
        };
        public static final int BELOW = 1;
        public static final int CENTER_IN_PARENT = 15;
        public static final int END_OF = 17;
        public static final int HORIZONTAL_CENTER = 13;
        public static final int LEFT_OF = 2;
        public static final int RIGHT_OF = 3;
        public static final int START_OF = 16;
        public static final int TRUE = -1;
        private static final int VERB_COUNT = 22;
        public static final int VERTICAL_CENTER = 14;
        private final int[] mInitialRules;
        private boolean mNeedsLayoutResolution;
        private int[] mRules;
        private boolean mRulesChanged;

        private boolean isDependentRule(int i) {
            return i == 16 || i == 17 || i == 18 || i == 19 || i == 20 || i == 21;
        }

        private native void nativeSetConfig(long j, int[] iArr, int[] iArr2, boolean z);

        public LayoutConfig() {
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mNeedsLayoutResolution = false;
            this.mRulesChanged = false;
        }

        public LayoutConfig(Context context, AttrSet attrSet) {
            super(context, attrSet);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mNeedsLayoutResolution = false;
            this.mRulesChanged = false;
            for (int i = 0; i < attrSet.getLength(); i++) {
                attrSet.getAttr(i).ifPresent(new Consumer() {
                    /* class ohos.agp.components.$$Lambda$DependentLayout$LayoutConfig$v9w4ZampJAhlGFti9BNAOgC4uaY */

                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        DependentLayout.LayoutConfig.this.lambda$new$1$DependentLayout$LayoutConfig((Attr) obj);
                    }
                });
            }
            this.mRulesChanged = true;
            System.arraycopy(this.mRules, 0, this.mInitialRules, 0, 22);
        }

        public /* synthetic */ void lambda$new$1$DependentLayout$LayoutConfig(Attr attr) {
            Optional.ofNullable(ATTR_METHOD_MAP.get(attr.getName())).ifPresent(new Consumer(attr) {
                /* class ohos.agp.components.$$Lambda$DependentLayout$LayoutConfig$1AwpEQIjbLUuxyiP0_SUR6P3Q */
                private final /* synthetic */ Attr f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    DependentLayout.LayoutConfig.this.lambda$new$0$DependentLayout$LayoutConfig(this.f$1, (BiConsumer) obj);
                }
            });
        }

        public /* synthetic */ void lambda$new$0$DependentLayout$LayoutConfig(Attr attr, BiConsumer biConsumer) {
            biConsumer.accept(this, attr);
        }

        public LayoutConfig(int i, int i2) {
            super(i, i2);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mNeedsLayoutResolution = false;
            this.mRulesChanged = false;
        }

        public LayoutConfig(LayoutConfig layoutConfig) {
            super(layoutConfig);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mNeedsLayoutResolution = false;
            this.mRulesChanged = false;
            this.mRulesChanged = layoutConfig.mRulesChanged;
            System.arraycopy(layoutConfig.mRules, 0, this.mRules, 0, 22);
            System.arraycopy(layoutConfig.mInitialRules, 0, this.mInitialRules, 0, 22);
        }

        public LayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
            this.mRules = new int[22];
            this.mInitialRules = new int[22];
            this.mNeedsLayoutResolution = false;
            this.mRulesChanged = false;
        }

        @Override // ohos.agp.components.ComponentContainer.LayoutConfig, java.lang.Object
        public Object clone() throws CloneNotSupportedException {
            Object clone = super.clone();
            if (clone instanceof LayoutConfig) {
                LayoutConfig layoutConfig = (LayoutConfig) clone;
                layoutConfig.mRules = new int[22];
                System.arraycopy(this.mRules, 0, layoutConfig.mRules, 0, 22);
            }
            return clone;
        }

        public void addRule(int i, int i2) {
            if (i < 22 && i >= 0) {
                if (!this.mNeedsLayoutResolution && isDependentRule(i) && this.mInitialRules[i] != 0 && i2 == 0) {
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
            return (this.mNeedsLayoutResolution || hasDependentRules()) && (this.mRulesChanged || layoutDirection != getLayoutDirection());
        }

        private void resolveParentRules(boolean z) {
            int[] iArr = this.mRules;
            char c = '\f';
            if (!(iArr[20] == 0 && iArr[21] == 0)) {
                int[] iArr2 = this.mRules;
                if (!(iArr2[11] == 0 && iArr2[12] == 0)) {
                    int[] iArr3 = this.mRules;
                    iArr3[11] = 0;
                    iArr3[12] = 0;
                }
            }
            int[] iArr4 = this.mRules;
            if (iArr4[20] != 0) {
                char c2 = z ? (char) '\f' : 11;
                int[] iArr5 = this.mRules;
                iArr4[c2] = iArr5[20];
                iArr5[20] = 0;
            }
            int[] iArr6 = this.mRules;
            if (iArr6[21] != 0) {
                if (z) {
                    c = 11;
                }
                int[] iArr7 = this.mRules;
                iArr6[c] = iArr7[21];
                iArr7[21] = 0;
            }
        }

        private void resolveRules(Component.LayoutDirection layoutDirection) {
            boolean z = layoutDirection == Component.LayoutDirection.RTL;
            System.arraycopy(this.mInitialRules, 0, this.mRules, 0, 22);
            int[] iArr = this.mRules;
            char c = 7;
            if (!(iArr[18] == 0 && iArr[19] == 0)) {
                int[] iArr2 = this.mRules;
                if (!(iArr2[6] == 0 && iArr2[7] == 0)) {
                    int[] iArr3 = this.mRules;
                    iArr3[6] = 0;
                    iArr3[7] = 0;
                }
            }
            int[] iArr4 = this.mRules;
            if (iArr4[18] != 0) {
                char c2 = z ? (char) 7 : 6;
                int[] iArr5 = this.mRules;
                iArr4[c2] = iArr5[18];
                iArr5[18] = 0;
            }
            int[] iArr6 = this.mRules;
            if (iArr6[19] != 0) {
                if (z) {
                    c = 6;
                }
                int[] iArr7 = this.mRules;
                iArr6[c] = iArr7[19];
                iArr7[19] = 0;
            }
            int[] iArr8 = this.mRules;
            char c3 = 3;
            if (!(iArr8[16] == 0 && iArr8[17] == 0)) {
                int[] iArr9 = this.mRules;
                if (!(iArr9[2] == 0 && iArr9[3] == 0)) {
                    int[] iArr10 = this.mRules;
                    iArr10[2] = 0;
                    iArr10[3] = 0;
                }
            }
            int[] iArr11 = this.mRules;
            if (iArr11[16] != 0) {
                char c4 = z ? (char) 3 : 2;
                int[] iArr12 = this.mRules;
                iArr11[c4] = iArr12[16];
                iArr12[16] = 0;
            }
            int[] iArr13 = this.mRules;
            if (iArr13[17] != 0) {
                if (z) {
                    c3 = 2;
                }
                int[] iArr14 = this.mRules;
                iArr13[c3] = iArr14[17];
                iArr14[17] = 0;
            }
            resolveParentRules(z);
            this.mRulesChanged = false;
            this.mNeedsLayoutResolution = false;
        }

        private boolean hasDependentRules() {
            int[] iArr = this.mInitialRules;
            return (iArr[16] == 0 && iArr[17] == 0 && iArr[18] == 0 && iArr[19] == 0 && iArr[20] == 0 && iArr[21] == 0) ? false : true;
        }

        /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0026: APUT  (r5v0 int[]), (2 ??[int, float, short, byte, char]), (r0v6 int) */
        @Override // ohos.agp.components.ComponentContainer.LayoutConfig
        public void applyToComponent(Component component) {
            int[] iArr = (int[]) this.mInitialRules.clone();
            int[] iArr2 = new int[6];
            iArr2[0] = this.width;
            iArr2[1] = this.height;
            iArr2[2] = isMarginsRelative() ? getHorizontalStartMargin() : getMarginLeft();
            iArr2[3] = getMarginTop();
            iArr2[4] = isMarginsRelative() ? getHorizontalEndMargin() : getMarginRight();
            iArr2[5] = getMarginBottom();
            nativeSetConfig(component.getNativeViewPtr(), iArr, iArr2, isMarginsRelative());
        }
    }
}
