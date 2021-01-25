package ohos.agp.components;

import ohos.agp.components.ComponentContainer;
import ohos.agp.components.TableLayout;
import ohos.agp.styles.Style;
import ohos.agp.styles.attributes.TableLayoutAttrsConstants;
import ohos.app.Context;

public class GridLayout extends ComponentContainer {
    public static final int ALIGN_BOUNDS = 0;
    public static final int ALIGN_MARGINS = 1;
    public static final int UNDEFINED = Integer.MIN_VALUE;

    public interface Alignment {
        public static final int BASELINE = 3;
        public static final int BOTTOM = 2;
        public static final int END = 2;
        public static final int FILL = 4;
        public static final int LEADING = 1;
        public static final int START = 1;
        public static final int TOP = 1;
        public static final int TRAILING = 2;
        public static final int UNDEFINED_ALIGNMENT = 0;
    }

    private native int nativeGetGridLayoutAlignmentMode(long j);

    private native int nativeGetGridLayoutColumnCount(long j);

    private native long nativeGetGridLayoutHandle();

    private native int nativeGetGridLayoutOrientation(long j);

    private native int nativeGetGridLayoutRowCount(long j);

    private native boolean nativeGetGridLayoutUseDefaultMargins(long j);

    private native void nativeSetGridLayoutAlignmentMode(long j, int i);

    private native void nativeSetGridLayoutColumnCount(long j, int i);

    private native void nativeSetGridLayoutOrientation(long j, int i);

    private native void nativeSetGridLayoutRowCount(long j, int i);

    private native void nativeSetGridLayoutUseDefaultMargins(long j, boolean z);

    public GridLayout(Context context) {
        this(context, null);
    }

    public GridLayout(Context context, AttrSet attrSet) {
        this(context, attrSet, "GridLayoutDefaultStyle");
    }

    public GridLayout(Context context, AttrSet attrSet, String str) {
        super(context, attrSet, str);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.Component
    public Style convertAttrToStyle(AttrSet attrSet) {
        if (this.mAttrsConstants == null) {
            this.mAttrsConstants = new TableLayoutAttrsConstants();
        }
        return super.convertAttrToStyle(attrSet);
    }

    /* access modifiers changed from: protected */
    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.Component
    public void createNativePtr() {
        if (this.mNativeViewPtr == 0) {
            this.mNativeViewPtr = nativeGetGridLayoutHandle();
        }
    }

    public void setColumnCount(int i) {
        nativeSetGridLayoutColumnCount(this.mNativeViewPtr, i);
    }

    public int getColumnCount() {
        return nativeGetGridLayoutColumnCount(this.mNativeViewPtr);
    }

    public void setRowCount(int i) {
        nativeSetGridLayoutRowCount(this.mNativeViewPtr, i);
    }

    public int getRowCount() {
        return nativeGetGridLayoutRowCount(this.mNativeViewPtr);
    }

    public void setOrientation(int i) {
        nativeSetGridLayoutOrientation(this.mNativeViewPtr, i);
    }

    public int getOrientation() {
        return nativeGetGridLayoutOrientation(this.mNativeViewPtr);
    }

    public void setAlignmentMode(int i) {
        nativeSetGridLayoutAlignmentMode(this.mNativeViewPtr, i);
    }

    public int getAlignmentMode() {
        return nativeGetGridLayoutAlignmentMode(this.mNativeViewPtr);
    }

    public void setUseDefaultMargins(boolean z) {
        nativeSetGridLayoutUseDefaultMargins(this.mNativeViewPtr, z);
    }

    public boolean getUseDefaultMargins() {
        return nativeGetGridLayoutUseDefaultMargins(this.mNativeViewPtr);
    }

    public static class LayoutParams extends ComponentContainer.LayoutConfig {
        private native void nativeSetGridLayoutParams(long j, int[] iArr, int[] iArr2, int[] iArr3, float[] fArr);

        public LayoutParams(Context context, AttrSet attrSet) {
            super(context, attrSet);
        }

        public LayoutParams(int i, int i2) {
            super(i, i2);
        }

        public LayoutParams(LayoutParams layoutParams) {
            super(layoutParams);
        }

        public LayoutParams(ComponentContainer.LayoutConfig layoutConfig) {
            super(layoutConfig);
        }

        public void applyToView(long j, int[] iArr, int[] iArr2, int[] iArr3, float[] fArr) {
            nativeSetGridLayoutParams(j, iArr, iArr2, iArr3, fArr);
        }
    }

    @Override // ohos.agp.components.ComponentContainer, ohos.agp.components.ComponentParent
    public ComponentContainer.LayoutConfig verifyLayoutConfig(ComponentContainer.LayoutConfig layoutConfig) {
        if (!(layoutConfig instanceof TableLayout.LayoutConfig)) {
            return new LayoutParams(layoutConfig);
        }
        TableLayout.LayoutConfig layoutConfig2 = (TableLayout.LayoutConfig) layoutConfig;
        checkLayoutParams(layoutConfig2, true);
        checkLayoutParams(layoutConfig2, false);
        return layoutConfig;
    }

    @Override // ohos.agp.components.ComponentContainer
    public ComponentContainer.LayoutConfig createLayoutConfig(Context context, AttrSet attrSet) {
        return new LayoutParams(context, attrSet);
    }

    private void checkLayoutParams(TableLayout.LayoutConfig layoutConfig, boolean z) {
        String str = z ? "column" : "row";
        TableLayout.CellSpan cellSpan = (z ? layoutConfig.columnSpec : layoutConfig.rowSpec).span;
        if (cellSpan.min != Integer.MIN_VALUE && cellSpan.min < 0) {
            handleInvalidParams(str + " indices must be positive");
        }
        int columnCount = z ? getColumnCount() : getRowCount();
        if (columnCount != Integer.MIN_VALUE) {
            if (cellSpan.size() > columnCount) {
                handleInvalidParams(str + " span must not exceed the " + str + " count");
            }
            if (cellSpan.max > columnCount) {
                handleInvalidParams(str + " indices (start + span) must not exceed the " + str + " count");
            }
        }
    }

    private static void handleInvalidParams(String str) {
        throw new IllegalArgumentException(str + ". ");
    }
}
