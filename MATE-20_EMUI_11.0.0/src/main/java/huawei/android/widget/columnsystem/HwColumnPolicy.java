package huawei.android.widget.columnsystem;

/* access modifiers changed from: package-private */
public abstract class HwColumnPolicy {
    private static final String TAG = HwColumnPolicy.class.getSimpleName();
    protected float mColumnWidth;
    protected float mDensity;
    protected int mGutter;
    protected int mHeightPixel;
    protected int mMargin;
    protected int mMaxColumn;
    protected int mMinColumn;
    protected int mTotalColumn;
    protected int mWidthPixel;
    protected float mXdpi;

    public abstract float getColumnWidth(int i);

    public abstract int getColumnWidth();

    public abstract int getMaxColumnWidth();

    public abstract int getMinColumnWidth();

    public abstract void onUpdateConfig();

    HwColumnPolicy() {
    }

    public void updateConfigration(int width, int height, float density) {
        this.mWidthPixel = width;
        this.mHeightPixel = height;
        this.mDensity = density;
    }

    public void setColumnConfig(int margin, int gutter, int columnCount, int maxColumnCount, int totalColumn) {
        this.mMargin = margin;
        this.mGutter = gutter;
        this.mMinColumn = columnCount;
        this.mMaxColumn = maxColumnCount;
        this.mTotalColumn = totalColumn;
        onUpdateConfig();
    }

    public void setMinColumn(int minColumn) {
        this.mMinColumn = minColumn;
    }
}
